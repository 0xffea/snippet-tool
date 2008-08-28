package src.gui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.jdom.Document;
import org.jdom.Element;

import src.model.HiWi_Object_Inscript;
import src.util.file.HiWi_FileIO;
import src.util.prefs.PrefUtil;

/**
 * Snippet-tool application window. Contains all other components and a reference to Inscript Object
 * Reference to HiWi_GUI is contained in every sub-component to provide the possibility to influence one component from the other.
 * 
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class HiWi_GUI extends JFrame{
	
	/** File storing all application vital properties, like database address, username, passwords, etc. **/
	public final static String PROPERTIES_FILE = "snippet-tool.properties";
	
	/** Application is initialized together with an Inscript Object **/
	HiWi_Object_Inscript s = new HiWi_Object_Inscript(this);
	
	public Properties props = new Properties();
	
	/** Menubar **/
	public HiWi_GUI_menubar menubar;
	/** Main panel, marking is done here **/
	public HiWi_GUI_main main;
	/** Text panel, inscript text is shown here **/
	public HiWi_GUI_text text;
	/** Log panel, all log messages are shown here (except for exceptions and errors that occure) **/
	public HiWi_GUI_log log;
	/** Explorer panel, tree-like explorer of eXist database for selecting and loading resources **/
	public HiWi_GUI_explorer explorer;
	/** Options panel, all adjustments for automatic marking, opacities, colors, text direction and displayed information are done here**/
	public HiWi_GUI_options options;
	/** Info panel, information about currently selected character is shown here **/
	public HiWi_GUI_info info;
	
	/** Stores all USER level log messages **/
	public String log_user = new String();
	/** Stores all DEVELOPER level log messages **/
	public String log_dev = new String();
	
	public HiWi_GUI(){
		// call superconstuctor
		super();
		
		// load all user settings
		loadProperties();
		s.loadFont();	// must be calles only after loadProperties(), since path_to_font is one of the properties specified
		
		// initialize subparts
		menubar = new HiWi_GUI_menubar(this, s);
		main = new HiWi_GUI_main(this, s);
		text = new HiWi_GUI_text(this, s);
		log = new HiWi_GUI_log(this, s);
		explorer = new HiWi_GUI_explorer(this, true);
		options = new HiWi_GUI_options(this, s);
		info = new HiWi_GUI_info(s);
		
		// adjust jframe settings
		setVisible(true);
		setPreferredSize(PrefUtil.string2dimesion(props.getProperty("local.window.size")));
		setResizable(true);
		setTitle("HiWi_GUI");
		
		// construct GUI as consisting of JSplitPanes
		setJMenuBar(menubar);
		
		JSplitPane mainoption = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, main, options);
		JSplitPane up = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, explorer, mainoption);
		JSplitPane textinfo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, text, info);
		JSplitPane down = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, textinfo, log);
		JSplitPane all = new JSplitPane(JSplitPane.VERTICAL_SPLIT, up, down);
		
		mainoption.setBorder(null);
		up.setBorder(null);
		textinfo.setBorder(null);
		down.setBorder(null);
		all.setBorder(null);
		
		mainoption.setDividerSize(PrefUtil.string2integer(props.getProperty("local.window.divider.width")));
		up.setDividerSize(PrefUtil.string2integer(props.getProperty("local.window.divider.width")));
		textinfo.setDividerSize(PrefUtil.string2integer(props.getProperty("local.window.divider.width")));
		down.setDividerSize(PrefUtil.string2integer(props.getProperty("local.window.divider.width")));
		all.setDividerSize(PrefUtil.string2integer(props.getProperty("local.window.divider.width")));
		
		setContentPane(all);
		
		loadLayout();
		
		// add own windowlistener
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				exit();
			}
		});
		
		// end
		pack();
	}
	
	/**
	 * Save application properties back to PROPERTIES_FILE, since some of the properties can be changed during runtime 
	 */
	public void saveProperties(){
		props.setProperty("local.window.size", this.getWidth()+"x"+this.getHeight());
		props.setProperty("local.window.position", this.getLocation().x+"x"+this.getLocation().y);
		
		try {
	        props.store(new FileOutputStream(PROPERTIES_FILE), null);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	/**
	 * Load application properties from PROPERTIES_FILE
	 */
	public void loadProperties(){
		try {
	        props.load(new FileInputStream(PROPERTIES_FILE));
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }

		this.setSize(PrefUtil.string2dimesion(props.getProperty("local.window.size")));
		this.setLocation(PrefUtil.string2point(props.getProperty("local.window.position")));
	}
	
	/**
	 * Save Layout of application window. Saved are the sizes of each of JPanel parts of GUI
	 */
	public void saveLayout(){
		/*Dimension dim_main = main.getPreferredSize();
		Dimension dim_explorer = explorer.getPreferredSize();
		Dimension dim_options = options.getPreferredSize();
		Dimension dim_text = text.getPreferredSize();
		Dimension dim_info = info.getPreferredSize();
		Dimension dim_log = log.getPreferredSize();*/
		
		Dimension dim_main = main.getSize();
		Dimension dim_explorer = explorer.getSize();
		Dimension dim_options = options.getSize();
		Dimension dim_text = text.getSize();
		Dimension dim_info = info.getSize();
		Dimension dim_log = log.getSize();
		
		String layout = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<layout>" +
				"<panel name=\""+"explorer"+"\" dimension=\""+dim_explorer.width+"x"+dim_explorer.height+"\" />" +
				"<panel name=\""+"main"+"\" dimension=\""+dim_main.width+"x"+dim_main.height+"\" />" +
				"<panel name=\""+"options"+"\" dimension=\""+dim_options.width+"x"+dim_options.height+"\" />" +
				"<panel name=\""+"text"+"\" dimension=\""+dim_text.width+"x"+dim_text.height+"\" />" +
				"<panel name=\""+"info"+"\" dimension=\""+dim_info.width+"x"+dim_info.height+"\" />" +
				"<panel name=\""+"log"+"\" dimension=\""+dim_log.width+"x"+dim_log.height+"\" />" +
				"</layout>";
		
		HiWi_FileIO.writeXMLStringToFile(props.getProperty("local.window.layout"), layout);
	}
	
	/**
	 * Load Layout of application window. Size of each JPnale part of GUI is restores from previous session
	 */
	@SuppressWarnings("unchecked")
	public void loadLayout(){
		Document d = HiWi_FileIO.readXMLDocumentFromFile(props.getProperty("local.window.layout"));
		if(d == null) return;
		
		Element layoutRoot = d.getRootElement();
		List<Element> panels = layoutRoot.getChildren("panel");
		
		for(int i=0; i<panels.size(); i++){
			Element panel = panels.get(i);
			if(panel.getAttributeValue("name").equals("main")){
				main.setPreferredSize(PrefUtil.string2dimesion(panel.getAttributeValue("dimension")));
			}
			if(panel.getAttributeValue("name").equals("explorer")){
				explorer.setPreferredSize(PrefUtil.string2dimesion(panel.getAttributeValue("dimension")));
			}
			if(panel.getAttributeValue("name").equals("options")){
				options.setPreferredSize(PrefUtil.string2dimesion(panel.getAttributeValue("dimension")));
			}
			if(panel.getAttributeValue("name").equals("text")){
				text.setPreferredSize(PrefUtil.string2dimesion(panel.getAttributeValue("dimension")));
			}
			if(panel.getAttributeValue("name").equals("info")){
				info.setPreferredSize(PrefUtil.string2dimesion(panel.getAttributeValue("dimension")));
			}
			if(panel.getAttributeValue("name").equals("log")){
				log.setPreferredSize(PrefUtil.string2dimesion(panel.getAttributeValue("dimension")));
			}
		}
	}
	
	/**
	 * Adds log entry - as string - to log panel of the application
	 * @param logmsg	message
	 * @param u			whether message is shown to USER level, 0/1
	 * @param d			whether message is shown to DEVELOPER level, 0/1
	 */
	public void addLogEntry(String logmsg, int u, int d){
		// user log
		if(u>0){
			log_user += logmsg;
			log.jta_user.append(logmsg+"\n");
		}
		// developer log
		if(d>0){
			log_dev += logmsg;
			log.jta_dev.append(logmsg+"\n");
		}
		//
		this.repaint();
	}
	
	/**
	 * Overriden exit(). Layout and properties changed must be saved before exiting.
	 */
	public void exit(){
		saveLayout();
		saveProperties();
		System.exit(0);
	}
	
	public static void main(String[] args){
		new HiWi_GUI();
	}

}
