package src.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import src.model.HiWi_Object_Character;
import src.model.HiWi_Object_Inscript;

@SuppressWarnings("serial")
public class HiWi_GUI_text extends JPanel implements ActionListener, MouseListener{
	
	HiWi_GUI root;
	HiWi_Object_Inscript s;
		
	JTextArea text_in = new JTextArea();
	
	public HiWi_GUI_text(HiWi_GUI r, HiWi_Object_Inscript su){
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder("text"));
		
		this.root = r;
		this.s = su;
		
		Box box3 = new Box(BoxLayout.X_AXIS);
		box3.add(new JScrollPane(text_in));
		text_in.setEditable(true);
		text_in.setLineWrap(true);
		text_in.setWrapStyleWord(true);
		text_in.setRows(10);
		text_in.setColumns(30);
		
		text_in.addMouseListener(this);
		
		add(box3);
		
		setVisible(true);
	}
	
	public void setSelected(HiWi_Object_Character sign){
		int n = sign.number;
		int r = sign.row;
		int c = sign.column;
		if(r<0 || c<0){
			text_in.requestFocusInWindow();
			text_in.setSelectionStart(0);
			text_in.setSelectionEnd(0);
			root.text.repaint();
		}
		else{
			text_in.requestFocusInWindow();
			text_in.setSelectionStart(n-1+r-1);
			text_in.setSelectionEnd(n-1+r-1 + 1);
			root.text.repaint();
		}
	}

	public void actionPerformed(ActionEvent ae) {
		String cmd = ae.getActionCommand();
	}
	
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {
		int selected_begin = text_in.getSelectionStart();
		//int selected_end = text_in.getSelectionEnd();
		int selected_in_row = 0;
		String text = text_in.getText().substring(0, selected_begin);
		for(int i=0; i<text.length(); i++){
			if(text.charAt(i) == '\n') selected_in_row++;
		}
		String selected = text_in.getSelectedText();
		if(selected==null || selected.equals("")) return;
		//if(selected.length()>1) {JOptionPane.showMessageDialog(root, "Select single character only!", "Alert!", JOptionPane.ERROR_MESSAGE); text_in.select(0, 0); return;}
		// adjust active sign number and currently active sign
		s.setActiveSignNumber(selected_begin-selected_in_row+1);	// +1: added for compatibility of numbering starting from 1 and no from 0 as thougth
		root.info.showInfo(selected_begin-selected_in_row);
		//root.main.main_image.sn = s.getSign(s.getActiveSign()-1, 0);	// -1: added for compatibility of numbering starting from 1 and no from 0 as thougth
		// repaint
		root.main.repaint();
	}
	
}
