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

/**
 * GUI interface for cleaning appearances from the database.
 * Use with care! Could potentially destroy all saved appearances data.
 * 
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class HiWi_GUI_clearapp extends JFrame implements ActionListener{
	
	/** Reference to parent component **/
	HiWi_GUI root;
	
	/** Regular Expression to search for appearances to remove **/
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
		    String dbOut = props.getProperty("db.dir.out");
		    String dbUser = props.getProperty("db.user");
		    String dbPass = props.getProperty("db.passwd");
		    
		    // clear appearances
			XMLUtil.clearAppearances(root, dbUser, dbPass, dbOut, jtf_regexp.getText());
			
			// close
			dispose();
		}
		if(e.getSource().equals(jb_cancel)){
			dispose();
		}
	}

}
