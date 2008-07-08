package src.gui;

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
import java.util.Properties;

import javax.swing.JFrame;

import org.jdesktop.swingx.MultiSplitLayout;
import org.jdesktop.swingx.MultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout.Node;

import src.model.HiWi_Object_Sutra;
import src.util.file.HiWi_FileIO;

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
	
	//String defaultModel = "(COLUMN (ROW (LEAF name=explorer weight=0.2) (LEAF name=main weight=0.9) (LEAF name=options weight=0.2)) (ROW (LEAF name=text) (LEAF name=log)))";
	String defaultModel = "(column (row explorer main options) (row text info log))";
	Node defaultLayout = MultiSplitLayout.parseModel(defaultModel);
	MultiSplitPane multiSplitPane = new MultiSplitPane();
	
	public String log_user = new String();
	public String log_dev = new String();
	
	public HiWi_GUI(){
		// call superconstuctor
		super();
		// load all user settings
		loadProperties();
		loadLayout();
		s.loadFont();	// first then, when properties are loaded and application knows the path to font
		// initialize subparts
		menubar = new HiWi_GUI_menubar(this, s);
		main = new HiWi_GUI_main(this, s);
		text = new HiWi_GUI_text(this, s);
		log = new HiWi_GUI_log(this, s);
		explorer = new HiWi_GUI_explorer(this);
		options = new HiWi_GUI_options(this, s);
		info = new HiWi_GUI_info(s);
		// adjust jframe settings
		setVisible(true);
		setLocation(0, 0);
		setPreferredSize(getToolkit().getScreenSize());
		setResizable(true);
		setTitle("HiWi_GUI");
		// construct GUI
		setJMenuBar(menubar);
		multiSplitPane.add(explorer, "explorer");
		multiSplitPane.add(main, "main");
		multiSplitPane.add(options, "options");
		multiSplitPane.add(text, "text");
		multiSplitPane.add(log, "log");
		multiSplitPane.add(info, "info");
		setContentPane(multiSplitPane);
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
	}
	
	public void saveLayout(){
		XMLEncoder e;
		try {
			e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(props.getProperty("local.window.layout"))));
			MultiSplitLayout.Node model = multiSplitPane.getMultiSplitLayout().getModel();
			e.writeObject(model);
			e.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	public void loadLayout(){
		try {
		    XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream(props.getProperty("local.window.layout"))));
		    Node model = (Node) (d.readObject());
		    multiSplitPane.getMultiSplitLayout().setModel(model);
		    multiSplitPane.getMultiSplitLayout().setFloatingDividers(true);
		    d.close();
		}
		catch (Exception exc) { 
			System.out.println("Couldn't load user-defined layout - loading default instead");
			Node model = MultiSplitLayout.parseModel(defaultModel);
		    multiSplitPane.getMultiSplitLayout().setModel((Node) model);
		}
		//repaint();
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
