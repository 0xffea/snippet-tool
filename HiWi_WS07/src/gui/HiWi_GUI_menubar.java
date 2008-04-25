package src.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import src.model.HiWi_Object_Sutra;

@SuppressWarnings("serial")
public class HiWi_GUI_menubar extends JMenuBar implements ActionListener{
	HiWi_GUI root;
	HiWi_Object_Sutra s;
	
	JMenu m_file = new JMenu("File");
	JMenu m_edit = new JMenu("Edit");
	JMenu m_help = new JMenu("Help");
	
	JMenuItem mi_pref = new JMenuItem("Preferences");
	JMenuItem mi_exit = new JMenuItem("Exit");
	JMenuItem mi_clear = new JMenuItem("Clear Appearance");
	JMenuItem mi_about = new JMenuItem("About");
	JMenuItem mi_help = new JMenuItem("Help");
	
	
	public HiWi_GUI_menubar(HiWi_GUI r, HiWi_Object_Sutra str){
		super();
		this.root = r;
		this.s = str;
		mi_pref.addActionListener(this);
		mi_exit.addActionListener(this);
		mi_clear.addActionListener(this);
		mi_about.addActionListener(this);
		mi_help.addActionListener(this);
		m_file.add(mi_pref);
		m_file.add(mi_exit);
		m_edit.add(mi_clear);
		m_help.add(mi_about);
		m_help.add(mi_help);
		add(m_file);
		add(m_edit);
		add(m_help);
	}


	@SuppressWarnings("static-access")
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(mi_pref.getActionCommand())){
			new HiWi_GUI_preferences(root, s);
		}
		if(e.getActionCommand().equals(mi_exit.getActionCommand())){
			root.exit();
		}
		if(e.getActionCommand().equals(mi_clear.getActionCommand())){
			new HiWi_GUI_clearapp();
		}
		if(e.getActionCommand().equals(mi_about.getActionCommand())){
			String text = "Zeichen Tool\n" +
							"v.0.99\n" +
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
