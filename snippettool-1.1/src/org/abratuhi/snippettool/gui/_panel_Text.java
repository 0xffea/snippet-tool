package org.abratuhi.snippettool.gui;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
 * Snippet-Tool Text Component. Holds text of the loaded inscript. Currently
 * selected marking snippet is correspodingly highlighted. If using
 * MouseController3 (manual selective marking), first select a character then
 * perform the corresponding marking in Snippet-Tool main_image component.
 * 
 * @author Alexei Bratuhin
 * 
 */
@SuppressWarnings("serial")
public class _panel_Text extends JPanel implements Observer, MouseListener {

	/** Reference to parent component **/
	private final _frame_SnippetTool root;

	private final Inscript inscript;

	/** Inscript text **/
	public JTextArea text_in = new JTextArea();

	public _panel_Text(_frame_SnippetTool r, SnippetTool snippettool) {
		super();

		this.root = r;

		this.inscript = snippettool.inscript;
		inscript.addObserver(this);

		setLayout(new SpringLayout());
		setVisible(true);

		text_in.addMouseListener(this);
		text_in.setRows(10);
		text_in.setColumns(30);

		add(new JScrollPane(text_in));

		SpringUtilities.makeCompactGrid(this, 1, 1, 0, 0, 0, 0);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}

	/**
	 * Highlight currently selected (active) character (snippet marking)
	 * 
	 * @param character
	 *            selected character object
	 */
	public void setSelected(InscriptCharacter character) {
		if (character != null) {
			int n = character.getNumber();
			int r = character.getRow();
			text_in.setSelectionStart(n - 1 + r - 1);
			text_in.setSelectionEnd(n - 1 + r - 1 + 1);
		} else {
			text_in.setSelectionStart(0);
			text_in.setSelectionEnd(0);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

		int selectionBegin = text_in.getSelectionStart();
		int selectionEnd = text_in.getSelectionEnd();

		if (inscript != null) {
			text_in.setText(inscript.getPlainText());
			setBorder(new TitledBorder("text: [ " + inscript.getPath() + " ]"));
			if (inscript.getActiveCharacter() != null) {
				if (selectionBegin != inscript.getActiveCharacter().getNumber() - 1
						+ inscript.getActiveCharacter().getRow() - 1) {
					text_in.setSelectionStart(selectionBegin);
					text_in.setSelectionEnd(selectionEnd);
				} else {
					setSelected(inscript.getActiveCharacter());
				}
			} else {
				text_in.setSelectionStart(selectionBegin);
				text_in.setSelectionEnd(selectionEnd);
			}
		} else {
			setBorder(new TitledBorder("text: "));
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		int selected_begin = text_in.getSelectionStart();
		int selected_end = text_in.getSelectionEnd();

		if (selected_end - selected_begin > 0) {
			// calculate row number of the selected character
			int selected_in_row = 0;
			String text = text_in.getText().substring(0, selected_begin);
			for (int i = 0; i < text.length(); i++) {
				if (text.charAt(i) == '\n')
					selected_in_row++;
			}

			// set active marking snippet corresponding to selected character
			// +1: added for compatibility of numbering starting from 1 and no
			// from 0 as thougth
			inscript.setActiveCharacter(inscript.getCharacterNV(selected_begin
					- selected_in_row, 0));
		}
	}
}
