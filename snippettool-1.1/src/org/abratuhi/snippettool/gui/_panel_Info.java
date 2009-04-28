package org.abratuhi.snippettool.gui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;

import org.abratuhi.snippettool.model.Inscript;
import org.abratuhi.snippettool.model.InscriptCharacter;
import org.abratuhi.snippettool.model.SnippetTool;
import org.abratuhi.snippettool.util.SpringUtilities;

/**
 * Snippet-tool Charachter Information component. Shows attributes of currently
 * selected character in form: character:
 * {CharacterRepresentation}({OriginalCharacterRepresentation}) x,y:
 * {CharacterMarkingLeftUpperCornerX},{CharacterMarkingLeftUpperCornerY}
 * width,height: {CharacterMarkingWidth},{CharacterMarkingHeight} number, row,
 * column:
 * {CharacterContinuousNumber},{CharacterRowNumber},{CharacterColumnNumber}
 * 
 * @author Alexei Bratuhin
 * 
 */
@SuppressWarnings("serial")
public class _panel_Info extends JPanel implements Observer {

	private final Inscript inscript;

	/****/
	final JTextArea jta_info = new JTextArea();

	public _panel_Info(SnippetTool snippettool) {
		// super
		super();
		//
		this.inscript = snippettool.inscript;
		inscript.addObserver(this);
		//
		jta_info.setColumns(20);
		jta_info.setRows(5);
		//
		setVisible(true);
		setLayout(new SpringLayout());
		add(new JScrollPane(jta_info));
		SpringUtilities.makeCompactGrid(this, 1, 1, 0, 0, 0, 0);
	}

	/**
	 * Show information to currently selected character
	 * 
	 * @param i
	 *            character's continuous number
	 */
	private String getInfo(InscriptCharacter sn) {
		StringBuilder info = new StringBuilder();
		if (sn != null) {
			info.append("character:\t" + sn.characterStandard + " ("
					+ sn.characterOriginal + ")" + "\n");
			info.append("x, y:\t" + sn.shape.base.x + ", " + sn.shape.base.y
					+ "\n");
			info.append("width, height:\t" + sn.shape.base.width + ", "
					+ sn.shape.base.height + "\n");
			info.append("angle:\t" + sn.shape.angle + "\n");
			info.append("number:\t" + sn.number + " (r:" + sn.row + ", c:"
					+ sn.column + ")");
			return info.toString();
		} else
			return "";
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		InscriptCharacter activeCharacter = inscript.getActiveCharacter();
		if (activeCharacter != null) {
			setBorder(new TitledBorder("info: [" + activeCharacter.id + "]"));
		} else {
			setBorder(new TitledBorder("info: [ ]"));
		}
		jta_info.setText(getInfo(activeCharacter));
	}
}
