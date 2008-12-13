package org.abratuhi.snippettool.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
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
	JMenu m_tool = new JMenu("Tool");
	JMenu m_help = new JMenu("Help");

	JMenuItem mi_loadm = new JMenuItem("Load Marking From Local File");
	JMenuItem mi_savem = new JMenuItem("Save Marking To Local File");
	JMenuItem mi_loads = new JMenuItem("Load Inscript From Local File");
	JMenuItem mi_loadi = new JMenuItem("Load Image From Local File");
	JMenuItem mi_exit = new JMenuItem("Exit");
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
		mi_cutter.addActionListener(this);
		mi_about.addActionListener(this);
		mi_help.addActionListener(this);
		
		// 
		m_file.add(mi_loads);
		m_file.add(mi_loadi);
		m_file.add(mi_savem);
		m_file.add(mi_loadm);
		m_file.add(mi_exit);
		m_tool.add(mi_cutter);
		m_help.add(mi_about);
		//m_help.add(mi_help);
		
		// 
		add(m_file);
		add(m_tool);
		add(m_help);
	}


	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(mi_loads.getActionCommand())){
			Thread t1 = new Thread(){
				public void run(){
					JFileChooser fc = new JFileChooser(snippettool.props.getProperty("local.inscript.dir"));
					fc.showOpenDialog(root);
					snippettool.setInscriptText("local", fc.getSelectedFile().getParent(), fc.getSelectedFile().getName());
					root.status("Loaded Inscript.");
				}
			};
			t1.start();
		}
		if(e.getActionCommand().equals(mi_loadi.getActionCommand())){
			Thread t1 = new Thread(){
				public void run(){
					JFileChooser fc = new JFileChooser(snippettool.props.getProperty("local.image.dir"));
					fc.showOpenDialog(root);
					snippettool.setInscriptImage("local", fc.getSelectedFile().getParent(), fc.getSelectedFile().getName());
					root.status("Loaded Image.");
				}
			};
			t1.start();
		}
		if(e.getActionCommand().equals(mi_loadm.getActionCommand())){
			Thread t1 = new Thread(){
				public void run(){
					JFileChooser fc = new JFileChooser(snippettool.props.getProperty("local.unicode.dir"));
					fc.showOpenDialog(root);
					snippettool.loadLocal(fc.getSelectedFile());
					root.status("Loaded Marking.");
				}
			};
			t1.start();
		}
		if(e.getActionCommand().equals(mi_savem.getActionCommand())){
			Thread t1 = new Thread(){
				public void run(){
					snippettool.saveLocal();
					root.status("Saved Marking.");
				}
			};
			t1.start();
		}
		if(e.getActionCommand().equals(mi_exit.getActionCommand())){
			root.exit();
		}
		if(e.getActionCommand().equals(mi_cutter.getActionCommand())){
			//new ImageCutter(root);
		}
		if(e.getActionCommand().equals(mi_about.getActionCommand())){
			String text = "Snippet Tool\n" +
							"Version: 1.1beta\n" +
							"Author: Alexei Bratuhin\n" +
							"Produced for: Heidelberger Academy of Science";
			JOptionPane.showMessageDialog(root, text, "About", JOptionPane.INFORMATION_MESSAGE);
		}
		if(e.getActionCommand().equals(mi_help.getActionCommand())){
			String text = "";
			JOptionPane.showMessageDialog(root, text, "Help", JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
