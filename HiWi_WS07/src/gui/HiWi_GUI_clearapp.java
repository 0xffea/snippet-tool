package src.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import src.util.xml.XMLUtil;

public class HiWi_GUI_clearapp extends JFrame implements ActionListener{
	
	HiWi_GUI root;
	
	JTextField jtf_regexp = new JTextField();
	
	JButton jb_ok = new JButton("OK");
	JButton jb_cancel = new JButton("Cancel");
	
	public HiWi_GUI_clearapp(HiWi_GUI r){
		// super
		super("Clear Appearances");
		
		//
		this.root = r;
		
		// button <-> action listener
		jb_ok.addActionListener(this);
		jb_cancel.addActionListener(this);
		
		// hiwi_gui_clearapp
		setLayout(new GridLayout(2,2, 0,0));
		setVisible(true);
		add(new JLabel("ID:"));
		add(jtf_regexp);
		add(jb_ok);
		add(jb_cancel);
		
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(jb_ok)){
			// load needed properties
			Properties props = new Properties();
			try {
		        props.load(new FileInputStream(HiWi_GUI.PROPERTIES_FILE));
		    } catch (IOException ioe) {
		    }
		    String dbURI = props.getProperty("db.uri");
		    String dbUser = props.getProperty("db.user");
		    String dbPass = props.getProperty("db.passwd");
		    String dbOut = props.getProperty("db.dir.out");
		    // clear appearances
			XMLUtil.clearAppearances(root, dbURI, dbUser, dbPass, dbOut, jtf_regexp.getText());
		}
		if(e.getSource().equals(jb_cancel)){
			dispose();
		}
	}

}
