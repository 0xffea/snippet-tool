package org.stonesutras.snippettool.gui;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import org.stonesutras.snippettool.model.Inscript;

/**
 * Snippet-tool Log Component.
 * Used to show the information to currently performed operations.
 * Has 2 tabs for each of 2 levels: USER and DEVELOPER.
 * USER level - only performed operations relevant messages are displayed
 * DEVELOPER level - all messages are displayed
 * 
 * Notice: Log messages are added from HiWi_GUI
 * 
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class _panel_Log extends JPanel{
	
	/** Reference to parent component **/
	_frame_SnippetTool root;
	
	/** Reference to Inscript Object, connected to HiWi_GUI **/
	Inscript s;
	
	JTabbedPane jtp = new JTabbedPane();
	
	/** USER level Tab for messages **/
	JTextArea jta_user = new JTextArea();
	
	/** DEVELOPER level Tab for messages **/
	JTextArea jta_dev = new JTextArea();
	
	
	public _panel_Log(_frame_SnippetTool r, Inscript su){
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
