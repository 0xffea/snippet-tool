package org.abratuhi.snippettool.gui;

import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;

import org.abratuhi.snippettool.model.InscriptCharacter;
import org.abratuhi.snippettool.model.Inscript;
import org.abratuhi.snippettool.model.SnippetTool;
import org.abratuhi.snippettool.util.SpringUtilities;

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
public class _panel_Info extends JPanel{
	

	SnippetTool snippettool;
	Inscript inscript;
	
	/****/
	public JTextArea jta_info = new JTextArea();
	
	public _panel_Info(SnippetTool snippettool){
		// super
		super();
		//
		this.snippettool = snippettool;
		this.inscript = snippettool.inscript;
		//
		jta_info.setColumns(20);
		jta_info.setRows(4);
		//
		setVisible(true);
		setLayout(new SpringLayout());
		add(new JScrollPane(jta_info));
		SpringUtilities.makeCompactGrid(this, 1, 1, 0, 0, 0, 0);
	}
	
	public void paint(Graphics g){
		super.paint(g);
		if(inscript.activeCharacter != null){
			setBorder(new TitledBorder("info: ["+inscript.activeCharacter.id+"]"));
			showInfo(inscript.activeCharacter);
		}
		else{
			setBorder(new TitledBorder("info: [ ]"));
		}
	}
	
	/**
	 * Show information to currently selected character
	 * @param i		character's continuous number
	 */
	public void showInfo(InscriptCharacter sn){
		String info = new String();
		if(sn != null) {
			info += "character:\t"+sn.characterStandard+" ("+sn.characterOriginal+")"+"\n";
			info += "x, y:\t"+sn.shape.x+", "+sn.shape.y+"\n";
			info += "width, height:\t"+sn.shape.width+", "+sn.shape.height+"\n";
			info += "number:\t"+sn.number+" (r:"+sn.row+", c:"+sn.column+")";
		}
		jta_info.setText(info);
	}

}
