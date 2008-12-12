package src.gui;

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

import src.util.string.HiWi_StringIO;

/**
 * Snippet-tool Explorer Component.
 * Represents database structure in tree view for navigation and selection purposes.
 * Double-click on file forces this file to be loaded into application either as image or as .xml inscript text.
 * 
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class HiWi_GUI_explorer extends JPanel{
	
	/** Reference to parent component **/
	HiWi_GUI root;
	
	/** Address of the database containing inscript .xml files and .png or .jpeg images**/
	String pcname;
	
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
	
	
	public HiWi_GUI_explorer(HiWi_GUI r, boolean load){		
		super();
		setLayout(new GridLayout(1,1));
		setBorder(new TitledBorder("explorer"));
		setPreferredSize(new Dimension(200, 400));
		
		root = r;
		pcname = root.props.getProperty("db.uri");
		rootnode = new DefaultMutableTreeNode(new String(pcname));
		explorer = new JTree(rootnode);
		
		this.autoload = load;
		
		// add custom treeselectionlistener for browsing in directory tree
		explorer.addTreeSelectionListener(new TreeSelectionListener(){
			@SuppressWarnings("unchecked")
			public void valueChanged(TreeSelectionEvent tse) {
				String user = root.props.getProperty("db.user");
				String passwd = root.props.getProperty("db.passwd");
				
				
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
					String driver = "org.exist.xmldb.DatabaseImpl";
					Class cl = Class.forName(driver);   
					Database database = (Database) cl.newInstance();   
					DatabaseManager.registerDatabase(database);
					Collection col = DatabaseManager.getCollection(selectedDir, user, passwd);
					
					//get child collections
					if(col!=null && col.getChildCollectionCount()>0){
						String[] children = col.listChildCollections();
						HiWi_StringIO.sortArrayofString(children);
						for(int i=0; i<children.length; i++){
							selectedNode.add(new DefaultMutableTreeNode(children[i]));
						}
					}
					
					//get child resources
					if(col!=null && col.getResourceCount()>0){
						String[] resources = col.listResources();
						HiWi_StringIO.sortArrayofString(resources);
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
							
							//root.main.loadImage();
							root.s.loadImage("remote", selectedCollection, selectedResource);
							root.main.main_image.mouse2.reset();
						}
						if(autoload && selectedResource.endsWith(".xml")){
							//root.main.loadText();
							root.s.loadText("remote", selectedCollection, selectedResource);
							root.main.main_image.mouse2.reset();
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (XMLDBException e) {
					e.printStackTrace();
				}
			}			
		});
		
		add(new JScrollPane(explorer));
		
	}
}
