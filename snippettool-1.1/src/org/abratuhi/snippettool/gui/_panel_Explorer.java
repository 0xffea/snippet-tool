package org.abratuhi.snippettool.gui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;

import org.abratuhi.snippettool.model.SnippetTool;
import org.abratuhi.snippettool.util.StringUtil;

/**
 * Snippet-tool Explorer Component.
 * Represents database structure in tree view for navigation and selection purposes.
 * Double-click on file forces this file to be loaded into application either as image or as .xml inscript text.
 * 
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class _panel_Explorer extends JPanel implements TreeSelectionListener{
	
	/** Reference to parent component **/
	_frame_SnippetTool root;
	
	SnippetTool snippettool;
	
	/** Address of the database containing inscript .xml files and .png or .jpeg images**/
	String db_data_uri;
	String db_data_user;
	String db_data_password;
	
	/** Root node of file tree structure of the database **/
	DefaultMutableTreeNode rootnode;
	
	/**  **/
	JTree explorer;
	
	/** Flag indicating, whether files should be loaded on double-click.
	 *  Inserted because of the extended ImageCutter functionality**/
	boolean autoload = false;
	
	/** absolute path to selected resource of the database **/
	public String selected;
	
	/** absolute path to selected collection of the database **/
	public String selectedCollection;
	
	/** relative path to selected resource of the database (realtive to selectedCollection) **/
	public String selectedResource;
	
	
	public _panel_Explorer(_frame_SnippetTool r, SnippetTool snippettool, boolean load){		
		super();
		setLayout(new GridLayout(1,1));
		setBorder(new TitledBorder("explorer"));
		setPreferredSize(new Dimension(200, 400));
		
		this.root = r;
		this.snippettool = snippettool;
		this.autoload = load;
		db_data_uri = snippettool.props.getProperty("db.data.uri");
		db_data_user = snippettool.props.getProperty("db.data.user");
		db_data_password = snippettool.props.getProperty("db.data.password");
		
		rootnode = new DefaultMutableTreeNode(new String(db_data_uri));
		explorer = new JTree(rootnode);
		
		explorer.addTreeSelectionListener(this);
		
		add(new JScrollPane(explorer));
		
	}


	@Override
	public void valueChanged(TreeSelectionEvent tse) {
		TreePath tp = tse.getPath();
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tp.getLastPathComponent();
		Object[] path = tp.getPath();
		String selectedDir = rootnode.toString();
		for(int i=1; i<path.length; i++){	//starting with index=1 to avoid adding pcname to path
			selectedDir += (String) ((DefaultMutableTreeNode)path[i]).getUserObject();
			selectedDir += "/";
		}
		selectedDir = selectedDir.substring(0, selectedDir.length()-1);
		try {  
			Database database = (Database) Class.forName("org.exist.xmldb.DatabaseImpl").newInstance();   
			DatabaseManager.registerDatabase(database);
			Collection col = DatabaseManager.getCollection(selectedDir, db_data_user, db_data_password);
			
			//get child collections
			if(col!=null && col.getChildCollectionCount()>0){
				String[] children = col.listChildCollections();
				StringUtil.sortArrayofString(children);
				for(int i=0; i<children.length; i++){
					selectedNode.add(new DefaultMutableTreeNode(children[i]));
				}
			}
			
			//get child resources
			if(col!=null && col.getResourceCount()>0){
				String[] resources = col.listResources();
				StringUtil.sortArrayofString(resources);
				for(int i=0; i<resources.length; i++){
					selectedNode.add(new DefaultMutableTreeNode(resources[i]));
				}
			}
			
			//set selected collection and resource
			if(col == null){
				// set selected fields
				selected = selectedDir;
				selectedCollection = selectedDir.substring(0, selectedDir.lastIndexOf("/")+1);
				selectedResource = selectedDir.substring(selectedDir.lastIndexOf("/")+1);
				
				// perform loading of selected resource
				if(autoload && 
						(selectedResource.endsWith(".png") ||	// check whether resource is an image
						selectedResource.endsWith(".jpeg") ||
						selectedResource.endsWith(".jpg"))){
					
					Thread t3 = new Thread(){
						public void run(){
							snippettool.setInscriptImage("remote", 
									selectedCollection,
									selectedResource);
							root.status("Loaded Image");
							try{
								sleep(10);
							} catch(InterruptedException e){}
						}
					};
					t3.start();
				}
				else if(autoload && selectedResource.endsWith(".xml")){
					// inscript loading thread
					Thread t1 = new Thread(){
						public void run(){
							snippettool.setInscriptText("remote", 
									selectedCollection, 
									selectedResource);
							snippettool.updateInscriptImagePathFromAppearances("remote");
							root.status("Loaded Inscript");
							try{
								sleep(10);
							} catch(InterruptedException e){}
						}
					};
					t1.start();
					try {
						t1.join();
					} catch (InterruptedException e1) {}
					
					// image loading thread
					if(snippettool.inscript.path_rubbing != null && 
							snippettool.inscript.path_rubbing != new String() &&
							snippettool.inscript.path_rubbing.contains("/")){
					Thread t3 = new Thread(){
						public void run(){
							snippettool.setInscriptImage("remote", 
									snippettool.inscript.path_rubbing.substring(0, snippettool.inscript.path_rubbing.lastIndexOf("/")),
									snippettool.inscript.path_rubbing.substring(snippettool.inscript.path_rubbing.lastIndexOf("/")+1));
							root.status("Loaded Image");
							try{
								sleep(10);
							} catch(InterruptedException e){}
						}
					};
					t3.start();
					}
					
					// coordinates loading thread
					if(snippettool.inscript.path_rubbing != null && snippettool.inscript.path_rubbing != new String()){
					Thread t2 = new Thread(){
						public void run(){
							snippettool.updateInscriptCoordinates("remote");
							root.status("Loaded Coordinates");
							try{
								sleep(10);
							} catch(InterruptedException e){}
						}
					};
					t2.start();
					}
					/*
					try {
						t2.join();
					} catch (InterruptedException e1) {}
					*/
				}
			}
		} catch (ClassNotFoundException ee) {
			ee.printStackTrace();
		} catch (InstantiationException ee) {
			ee.printStackTrace();
		} catch (IllegalAccessException ee) {
			ee.printStackTrace();
		} catch (XMLDBException ee) {
			ee.printStackTrace();
		}
	}
}
