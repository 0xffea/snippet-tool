package src.gui;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import src.model.HiWi_Object_Sutra;

public class HiWi_GUI_log extends JPanel{
	
	HiWi_GUI root;	
	HiWi_Object_Sutra s;
	
	JTabbedPane jtp = new JTabbedPane();
	JTextArea jta_user = new JTextArea();
	JTextArea jta_dev = new JTextArea();
	
	
	public HiWi_GUI_log(HiWi_GUI r, HiWi_Object_Sutra su){
		//
		super();
		setLayout(new GridLayout(1,1));
		setBorder(new TitledBorder("log"));
		setVisible(true);
		//
		this.root = r;
		this.s = su;
		//
		this.add(jtp);
		//
		jta_user.setLineWrap(true);
		jta_user.setWrapStyleWord(true);
		
		jta_dev.setLineWrap(true);
		jta_dev.setWrapStyleWord(true);		
		//
		jtp.addTab("User", new JScrollPane(jta_user));
		jtp.addTab("Developer", new JScrollPane(jta_dev));
	}

}
