package org.abratuhi.snippettool.gui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.abratuhi.snippettool.model.SnippetTool;
import org.abratuhi.snippettool.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;

/**
 * Snippet-tool Explorer Component. Represents database structure in tree view
 * for navigation and selection purposes. Double-click on file forces this file
 * to be loaded into application either as image or as .xml inscript text.
 * 
 * @author Alexei Bratuhin
 * 
 */
@SuppressWarnings("serial")
public class _panel_Explorer extends JPanel implements TreeSelectionListener {

	private static final Logger logger = LoggerFactory
			.getLogger(_panel_Explorer.class);

	/** Reference to parent component **/
	_frame_SnippetTool root;

	SnippetTool snippettool;

	/**
	 * Address of the database containing inscript .xml files and .png or .jpeg
	 * images
	 **/
	String db_data_uri;
	String db_data_user;
	String db_data_password;

	/** Root node of file tree structure of the database **/
	DefaultMutableTreeNode rootnode;

	/**  **/
	JTree explorer;

	/**
	 * Flag indicating, whether files should be loaded on double-click. Inserted
	 * because of the extended ImageCutter functionality
	 **/
	boolean autoload = false;

	/** absolute path to selected resource of the database **/
	public String selected;

	/** absolute path to selected collection of the database **/
	public String selectedCollection;

	/**
	 * relative path to selected resource of the database (realtive to
	 * selectedCollection)
	 **/
	public String selectedResource;

	public _panel_Explorer(_frame_SnippetTool r, SnippetTool snippettool,
			boolean load) {
		super();
		setLayout(new GridLayout(1, 1));
		setBorder(new TitledBorder("explorer"));
		setPreferredSize(new Dimension(200, 400));

		this.root = r;
		this.snippettool = snippettool;
		this.autoload = load;
		db_data_uri = snippettool.props.getProperty("db.data.uri");
		db_data_user = snippettool.props.getProperty("db.data.user");
		db_data_password = snippettool.props.getProperty("db.data.password");

		rootnode = new DefaultMutableTreeNode(db_data_uri);
		explorer = new JTree(rootnode);

		explorer.addTreeSelectionListener(this);

		add(new JScrollPane(explorer));

	}

	@Override
	public void valueChanged(TreeSelectionEvent tse) {
		TreePath tp = tse.getPath();
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tp
				.getLastPathComponent();
		Object[] path = tp.getPath();
		String selectedDir = rootnode.toString();
		for (int i = 1; i < path.length; i++) { // starting with index=1 to
			// avoid adding pcname to path
			selectedDir += (String) ((DefaultMutableTreeNode) path[i])
					.getUserObject();
			selectedDir += "/";
		}
		selectedDir = selectedDir.substring(0, selectedDir.length() - 1);
		try {

			Collection col = DatabaseManager.getCollection(selectedDir,
					db_data_user, db_data_password);

			// get child collections
			if (col != null && col.getChildCollectionCount() > 0) {
				String[] children = col.listChildCollections();
				StringUtil.sortArrayofString(children);
				for (String element : children) {
					selectedNode.add(new DefaultMutableTreeNode(element));
				}
			}

			// get child resources
			if (col != null && col.getResourceCount() > 0) {
				String[] resources = col.listResources();
				StringUtil.sortArrayofString(resources);
				for (String resource : resources) {
					selectedNode.add(new DefaultMutableTreeNode(resource));
				}
			}

			// set selected collection and resource
			if (col == null) {
				// set selected fields
				selected = selectedDir;
				selectedCollection = selectedDir.substring(0, selectedDir
						.lastIndexOf("/") + 1);
				selectedResource = selectedDir.substring(selectedDir
						.lastIndexOf("/") + 1);

				// perform loading of selected resource
				// if resource is an image
				if (autoload
						&& (selectedResource.endsWith(".png")
								|| selectedResource.endsWith(".jpeg")
								|| selectedResource.endsWith(".jpg")
								|| selectedResource.endsWith(".tiff") || selectedResource
								.endsWith(".tif"))) {

					Thread t3 = new Thread() {
						@Override
						public void run() {
							snippettool
									.setInscriptImageToRemoteRessource(selected);
							root.status("Loaded Image");
						}
					};
					t3.start();
				} else if (autoload && selectedResource.endsWith(".xml")) {
					// inscript loading thread
					Thread t1 = new Thread() {
						@Override
						public void run() {
							snippettool.loadInscriptTextFromRemoteResource(
									selectedCollection, selectedResource);
							snippettool
									.updateInscriptImagePathFromAppearances("remote");
							root.status("Loaded Inscript");
						}
					};
					t1.start();
					try {
						t1.join();
					} catch (InterruptedException e1) {
					}

					// image loading thread
					if (snippettool.inscript.getAbsoluteRubbingPath() != null
							&& snippettool.inscript.getAbsoluteRubbingPath() != ""
							&& snippettool.inscript.getAbsoluteRubbingPath()
									.contains("/")) {
						Thread t3 = new Thread() {
							@Override
							public void run() {
								snippettool
										.setInscriptImageToRemoteRessource(snippettool.inscript
												.getAbsoluteRubbingPath());
								root.status("Loaded Image");
							}
						};
						t3.start();
					}

					// coordinates loading thread
					if (snippettool.inscript.getAbsoluteRubbingPath() != null
							&& snippettool.inscript.getAbsoluteRubbingPath() != "") {
						Thread t2 = new Thread() {
							@Override
							public void run() {
								snippettool.updateInscriptCoordinates();
								root.status("Loaded Coordinates");
							}
						};
						t2.start();
					}
				}
			}
		} catch (XMLDBException e) {
			logger.error("XMLDBException in valueChanged(): ", e);
			JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
