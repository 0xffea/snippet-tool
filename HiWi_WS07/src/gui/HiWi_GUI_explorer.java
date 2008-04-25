package src.gui;

import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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

@SuppressWarnings("serial")
public class HiWi_GUI_explorer extends JPanel{
	
	HiWi_GUI root;
	
	String pcname;
	DefaultMutableTreeNode rootnode;// = new DefaultMutableTreeNode(new String(pcname));
	JTree explorer;// = new JTree(rootnode);
	
	public String selected;
	public String selectedCollection;
	public String selectedResource;
	
	public HiWi_GUI_explorer(HiWi_GUI r){		
		super();
		setLayout(new GridLayout(1,1));
		setBorder(new TitledBorder("explorer"));
		
		root = r;
		pcname = root.props.getProperty("db.uri");
		rootnode = new DefaultMutableTreeNode(new String(pcname));
		explorer = new JTree(rootnode);
		
		/*// add custom mouse listener to implement "loading on double-click" feature
		addMouseListener(new MouseAdapter(){
			int clicks = 0;	// clicks counter
			public void mouseClicked(MouseEvent e){
				// increment click counter
				clicks++;
				// if double click
				if(clicks>1) {
					//
					System.out.println("double-click detected");
					// reset click counter if needed
					clicks = 0;
					// perform loading of selected resource
					if(selectedResource.endsWith(".png") ||	// check whether resource is an image
							selectedResource.endsWith(".jpeg") ||
							selectedResource.endsWith(".jpg")){
						root.main.load_image.setEnabled(true);
					}
					if(selectedResource.endsWith(".xml")){
						root.main.load_text.setEnabled(true);
					}
				}
			}
		});*/
		
		// add custom treeselectionlistener for browsing in directory tree
		explorer.addTreeSelectionListener(new TreeSelectionListener(){
			@SuppressWarnings("unchecked")
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
					String driver = "org.exist.xmldb.DatabaseImpl";
					Class cl = Class.forName(driver);   
					Database database = (Database) cl.newInstance();   
					DatabaseManager.registerDatabase(database);
					Collection col = DatabaseManager.getCollection(selectedDir);
					//get child collections
					if(col!=null && col.getChildCollectionCount()>0){
						String[] children = col.listChildCollections();
						for(int i=0; i<children.length; i++){
							selectedNode.add(new DefaultMutableTreeNode(children[i]));
						}
					}
					//get child resources
					if(col!=null && col.getResourceCount()>0){
						String[] resources = col.listResources();
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
						if(selectedResource.endsWith(".png") ||	// check whether resource is an image
								selectedResource.endsWith(".jpeg") ||
								selectedResource.endsWith(".jpg")){
							root.main.loadImage();
						}
						if(selectedResource.endsWith(".xml")){
							root.main.loadText();
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
