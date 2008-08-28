package src.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import src.model.HiWi_Object_Character;
import src.model.HiWi_Object_Inscript;

/**
 * Snippet-Tool Text Component.
 * Holds text of the loaded inscript.
 * Currently selected marking snippet is correspodingly highlighted.
 * If using MouseController3 (manual selective marking), first select a character then perform the corresponding marking in Snippet-Tool main_image component.
 * 
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class HiWi_GUI_text extends JPanel implements MouseListener{
	
	/** Reference to parent component **/
	HiWi_GUI root;
	
	/** Reference to Inscript Object, connected to HiWi_GUI **/
	HiWi_Object_Inscript s;
	
	/** Inscript text **/
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
	
	/**
	 * Highlight currently selected (active) charachter (snippet marking)
	 * @param character		selected character object
	 */
	public void setSelected(HiWi_Object_Character character){
		int n = character.number;
		int r = character.row;
		int c = character.column;
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
	
	/**
	 * Following code part implements Text Selection Listener 
	 * **/
	
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent e) {
		//
		int selected_begin = text_in.getSelectionStart();
		//int selected_end = text_in.getSelectionEnd();
		
		// calculate row number of the selected character
		int selected_in_row = 0;
		String text = text_in.getText().substring(0, selected_begin);
		for(int i=0; i<text.length(); i++){
			if(text.charAt(i) == '\n') selected_in_row++;
		}
		
		// set active marking snippet corresponding to selected character
		String selected = text_in.getSelectedText();
		if(selected==null || selected.equals("")) return;
		//if(selected.length()>1) {JOptionPane.showMessageDialog(root, "Select single character only!", "Alert!", JOptionPane.ERROR_MESSAGE); text_in.select(0, 0); return;}
		s.setActiveCharacterNumber(selected_begin-selected_in_row+1);	// +1: added for compatibility of numbering starting from 1 and no from 0 as thougth
		root.info.showInfo(selected_begin-selected_in_row);
		
		// repaint
		root.main.repaint();
	}
	
}
