package org.abratuhi.snippettool.gui;

import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;

import org.abratuhi.snippettool.controller._controller_Text;
import org.abratuhi.snippettool.model.InscriptCharacter;
import org.abratuhi.snippettool.model.Inscript;
import org.abratuhi.snippettool.model.SnippetTool;
import org.abratuhi.snippettool.util.SpringUtilities;

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
public class _panel_Text extends JPanel{
	
	/** Reference to parent component **/
	_frame_SnippetTool root;
	
	SnippetTool snippettool;
	Inscript inscript;
	
	_controller_Text controller;
	
	/** Inscript text **/
	public JTextArea text_in = new JTextArea();
	
	public _panel_Text(_frame_SnippetTool r, SnippetTool snippettool){
		super();
		
		this.root = r;
		this.snippettool = snippettool;
		this.inscript = snippettool.inscript;
		this.controller = new _controller_Text(this, snippettool);
		
		setLayout(new SpringLayout());
		setVisible(true);
		
		text_in.addMouseListener(controller);
		text_in.setRows(10);
		text_in.setColumns(30);
		
		add(new JScrollPane(text_in));
		
		SpringUtilities.makeCompactGrid(this, 1, 1, 0, 0, 0, 0);
	}
	
	public void paint(Graphics g){
		super.paint(g);
		
		int selectionBegin = text_in.getSelectionStart();
		int selectionEnd = text_in.getSelectionEnd();
		
		if(inscript != null){
			text_in.setText(inscript.getPlainText());
			setBorder(new TitledBorder("text: [ " + inscript.path_file + " ]"));
			if(inscript.activeCharacter != null){
				if(selectionBegin != inscript.activeCharacter.number-1+inscript.activeCharacter.row-1){
					text_in.setSelectionStart(selectionBegin);
					text_in.setSelectionEnd(selectionEnd);
				}
				else{
					setSelected(inscript.activeCharacter);
				}
			}
			else{
				text_in.setSelectionStart(selectionBegin);
				text_in.setSelectionEnd(selectionEnd);
			}
		}
		else{
			setBorder(new TitledBorder("text: "));
		}
	}
	
	/**
	 * Highlight currently selected (active) charachter (snippet marking)
	 * @param character		selected character object
	 */
	public void setSelected(InscriptCharacter character){
		if(character != null){
			int n = character.number;
			int r = character.row;
			//text_in.requestFocusInWindow();
			text_in.setSelectionStart(n-1+r-1);
			text_in.setSelectionEnd(n-1+r-1 + 1);
		}
		else{
			//text_in.requestFocusInWindow();
			text_in.setSelectionStart(0);
			text_in.setSelectionEnd(0);
		}
	}
}
