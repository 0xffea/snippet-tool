package src.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import src.model.HiWi_Object_Inscript;

@SuppressWarnings("serial")
public class HiWi_GUI_preferences extends JFrame implements ActionListener{
	HiWi_GUI root;
	HiWi_Object_Inscript s;
	
	JPanel p = new JPanel();
	JButton button_ok = new JButton("Ok");
	JButton button_cancel = new JButton("Cancel");
	
	public HiWi_GUI_preferences(HiWi_GUI r, HiWi_Object_Inscript str){
		super("HiWi_GUI Preferences");
		this.root = r;
		this.s = str;
		setVisible(true);
		button_ok.addActionListener(this);
		button_cancel.addActionListener(this);
		p.add(button_ok);
		p.add(button_cancel);
		add(p);
		pack();
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(button_ok.getActionCommand())){
			// reload application with new properties
			root.saveProperties();
			root.loadProperties();
			// dispose preferences window
			dispose();
		}
		if(e.getActionCommand().equals(button_cancel.getActionCommand())){
			dispose();
		}
	}

}
