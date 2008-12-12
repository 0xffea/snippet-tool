package src.gui;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import src.model.HiWi_Object_Character;
import src.model.HiWi_Object_Inscript;

/**
 * Snippet-tool Charachter Information component.
 * Shows attributes of currently selected character in form:
 * 	character:	{CharacterRepresentation}({OriginalCharacterRepresentation})
 * 	x,y:	{CharacterMarkingLeftUpperCornerX},{CharacterMarkingLeftUpperCornerY}
 * 	width,height:	{CharacterMarkingWidth},{CharacterMarkingHeight}
 * 	number, row, column: {CharacterContinuousNumber},{CharacterRowNumber},{CharacterColumnNumber}
 * 
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class HiWi_GUI_info extends JPanel{
	
	/** Reference to Inscript Object, connected to HiWi_GUI **/
	HiWi_Object_Inscript s;
	
	/****/
	JTextArea jta_info = new JTextArea();
	
	public HiWi_GUI_info(HiWi_Object_Inscript sutra){
		// super
		super();
		setVisible(true);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder("info"));
		// 
		Box box3 = new Box(BoxLayout.X_AXIS);
		box3.add(new JScrollPane(jta_info));
		jta_info.setWrapStyleWord(true);
		jta_info.setLineWrap(true);
		jta_info.setColumns(20);
		jta_info.setRows(10);
		//
		add(box3);
		//
		this.s = sutra;
	}
	
	/**
	 * Show information to currently selected character
	 * @param i		character's continuous number
	 */
	public void showInfo(int i){
		// get character object
		HiWi_Object_Character sn = s.getCharacter(i, 0);
		
		// show info
		String info = new String();
		info += "character:\t"+sn.characterStandard+" ("+sn.characterOriginal+")"+"\n";
		info += "x, y:\t"+sn.s.x+", "+sn.s.y+"\n";
		info += "width, height:\t"+sn.s.width+", "+sn.s.height+"\n";
		info += "number:\t"+sn.number+" (r:"+sn.row+", c:"+sn.column+")"+"\n";
		jta_info.setText(info);
	}

}
