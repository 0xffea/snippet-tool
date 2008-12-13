package org.abratuhi.snippettool.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.abratuhi.snippettool.imagecutter.ImageCutter;
import org.abratuhi.snippettool.model.SnippetTool;

/**
 * Snippet-Tool menubar component.
 * File -> 
 * 		+ Load Marking- load marking from file stored locally in /tmp/xml
 * 		+ Save Marking - save marking to file stores locally in /tmp/xml
 * 		+ Load Inscript - load inscript from local file
 * 		+ Load Image - load image from local file
 * 		+ Exit - leave application
 * Edit -> 
 * 		+ Clear Appearances - open HiWi_GUI_clearapp Window
 * 		+ Image Cutter - open ImageCutter Window
 * Help ->
 * 		+ About
 * 		+ Help
 * 
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class _menubar_SnippetTool extends JMenuBar implements ActionListener{
	
	/** Reference to parent component **/
	_frame_SnippetTool root;
	
	SnippetTool snippettool;
	
	
	
	JMenu m_file = new JMenu("File");
	JMenu m_edit = new JMenu("Edit");
	JMenu m_help = new JMenu("Help");

	JMenuItem mi_loadm = new JMenuItem("Load Marking");
	JMenuItem mi_savem = new JMenuItem("Save Marking");
	JMenuItem mi_loads = new JMenuItem("Load Inscript");
	JMenuItem mi_loadi = new JMenuItem("Load Image");
	JMenuItem mi_pref = new JMenuItem("Preferences");
	JMenuItem mi_exit = new JMenuItem("Exit");
	JMenuItem mi_clear = new JMenuItem("Clear Appearance");
	JMenuItem mi_cutter = new JMenuItem("Image Cutter");
	JMenuItem mi_about = new JMenuItem("About");
	JMenuItem mi_help = new JMenuItem("Help");
	
	
	public _menubar_SnippetTool(_frame_SnippetTool r, SnippetTool snippettool){
		super();
		this.root = r;
		this.snippettool = snippettool;
		
		// 
		mi_savem.addActionListener(this);
		mi_loadm.addActionListener(this);
		mi_loadi.addActionListener(this);
		mi_loads.addActionListener(this);
		mi_exit.addActionListener(this);
		mi_clear.addActionListener(this);
		mi_cutter.addActionListener(this);
		mi_about.addActionListener(this);
		mi_help.addActionListener(this);
		
		// 
		m_file.add(mi_loads);
		m_file.add(mi_loadi);
		m_file.add(mi_savem);
		m_file.add(mi_loadm);
		m_file.add(mi_exit);
		m_edit.add(mi_clear);
		m_edit.add(mi_cutter);
		m_help.add(mi_about);
		m_help.add(mi_help);
		
		// 
		add(m_file);
		add(m_edit);
		add(m_help);
	}


	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(mi_loads.getActionCommand())){
		}
		if(e.getActionCommand().equals(mi_loadi.getActionCommand())){
		}
		if(e.getActionCommand().equals(mi_loadm.getActionCommand())){
			//snippettool.inscript.loadTempMarking();
		}
		if(e.getActionCommand().equals(mi_savem.getActionCommand())){
			//snippettool.inscript.saveTempMarking();
		}
		if(e.getActionCommand().equals(mi_exit.getActionCommand())){
			//snippettool.exit();
		}
		if(e.getActionCommand().equals(mi_clear.getActionCommand())){
			//new _frame_Clearapp(root, snippettool);
		}
		if(e.getActionCommand().equals(mi_cutter.getActionCommand())){
			//new ImageCutter(root);
		}
		if(e.getActionCommand().equals(mi_about.getActionCommand())){
			String text = "Snippet Tool\n" +
							"v.2.0beta\n" +
							"author: Alexei Bratuhin\n" +
							"produced for: Heidelberger Academy of Science";
			JOptionPane.showMessageDialog(root, text, "About", JOptionPane.INFORMATION_MESSAGE);
		}
		if(e.getActionCommand().equals(mi_help.getActionCommand())){
			String text = "";
			JOptionPane.showMessageDialog(root, text, "Help", JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
