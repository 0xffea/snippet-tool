package org.abratuhi.snippettool.controller;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.abratuhi.snippettool.gui._panel_Text;
import org.abratuhi.snippettool.model.SnippetTool;

public class _controller_Text implements MouseListener{
	_panel_Text _panel;
	SnippetTool snippettool;
	
	public _controller_Text(_panel_Text panel, SnippetTool st){
		this._panel = panel;
		this.snippettool = st;
	}
	
	
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {
		//root.text.requestFocusInWindow();
	}
	public void mouseReleased(MouseEvent e) {
		//
		int selected_begin = _panel.text_in.getSelectionStart();
		int selected_end = _panel.text_in.getSelectionEnd();
		
		if(selected_end - selected_begin > 0){
			// calculate row number of the selected character
			int selected_in_row = 0;
			String text = _panel.text_in.getText().substring(0, selected_begin);
			for(int i=0; i<text.length(); i++){
				if(text.charAt(i) == '\n') selected_in_row++;
			}

			// set active marking snippet corresponding to selected character
			snippettool.inscript.activeCharacter = snippettool.inscript.getCharacter(selected_begin-selected_in_row, 0);	// +1: added for compatibility of numbering starting from 1 and no from 0 as thougth
		}
	}
}
