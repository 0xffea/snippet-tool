package src.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.jdesktop.swingx.MultiSplitLayout;
import org.jdesktop.swingx.MultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout.Node;
import org.jdom.Document;
import org.jdom.Element;

import src.model.HiWi_Object_Sutra;
import src.util.file.HiWi_FileIO;
import src.util.prefs.PrefUtil;

@SuppressWarnings("serial")
public class HiWi_GUI extends JFrame{
	
	public final static String PROPERTIES_FILE = "snippet-tool.properties";
	
	HiWi_Object_Sutra s = new HiWi_Object_Sutra(this);
	
	public Properties props = new Properties();
	
	public HiWi_GUI_menubar menubar;// = new HiWi_GUI_menubar(this, s);
	public HiWi_GUI_main main;// = new HiWi_GUI_main(this, s);
	public HiWi_GUI_text text;// = new HiWi_GUI_text(this, s);
	public HiWi_GUI_log log;
	public HiWi_GUI_explorer explorer;// = new HiWi_GUI_explorer(this);
	public HiWi_GUI_options options;// = new HiWi_GUI_options(this, s);
	public HiWi_GUI_info info;
	
	public String log_user = new String();
	public String log_dev = new String();
	
	public HiWi_GUI(){
		// call superconstuctor
		super();
		// load all user settings
		loadProperties();
		s.loadFont();	// first then, when properties are loaded and application knows the path to font
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
		//setPreferredSize(getToolkit().getScreenSize());
		setPreferredSize(PrefUtil.string2dimesion(props.getProperty("local.window.size")));
		setResizable(true);
		setTitle("HiWi_GUI");
		
		
		// construct GUI
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
		
		mainoption.setDividerSize(PrefUtil.getDividerWidth(props.getProperty("local.window.divider.width")));
		up.setDividerSize(PrefUtil.getDividerWidth(props.getProperty("local.window.divider.width")));
		textinfo.setDividerSize(PrefUtil.getDividerWidth(props.getProperty("local.window.divider.width")));
		down.setDividerSize(PrefUtil.getDividerWidth(props.getProperty("local.window.divider.width")));
		all.setDividerSize(PrefUtil.getDividerWidth(props.getProperty("local.window.divider.width")));
		
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
	
	public void saveProperties(){
		props.setProperty("local.window.size", this.getWidth()+"x"+this.getHeight());
		props.setProperty("local.window.position", this.getLocation().x+"x"+this.getLocation().y);
		
		try {
	        props.store(new FileOutputStream(PROPERTIES_FILE), null);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	public void loadProperties(){
		try {
	        props.load(new FileInputStream(PROPERTIES_FILE));
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }

		this.setSize(PrefUtil.string2dimesion(props.getProperty("local.window.size")));
		this.setLocation(PrefUtil.string2point(props.getProperty("local.window.position")));
	}
	
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
	
	public void exit(){
		saveLayout();
		saveProperties();
		System.exit(0);
	}
	
	public static void main(String[] args){
		new HiWi_GUI();
	}

}
