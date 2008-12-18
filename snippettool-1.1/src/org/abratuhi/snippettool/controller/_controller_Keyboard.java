package org.abratuhi.snippettool.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.abratuhi.snippettool.gui._panel_Mainimage;
import org.abratuhi.snippettool.model.Inscript;
import org.abratuhi.snippettool.model.InscriptCharacter;
import org.abratuhi.snippettool.model.SnippetTool;

public class _controller_Keyboard implements KeyListener{

	_panel_Mainimage main_image;
	SnippetTool snippettool;
	Inscript inscript;
	
	public _controller_Keyboard(_panel_Mainimage mi){
		this.main_image = mi;
		this.snippettool = mi.root.snippettool;
		this.inscript = snippettool.inscript;
	}
	
	
	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		int step = (e.isControlDown())? 10 : 1;
		int resizeSign = (e.isShiftDown())? -1 : 1;
		switch (code) {
		case 81:	// 'q'
			inscript.rotateSnippet(inscript.activeCharacter , Math.toRadians(1));
			break;
		case 69:	// 'e'
			inscript.rotateSnippet(inscript.activeCharacter , Math.toRadians(-1));
			break;
		case 87:	// 'w'
			inscript.resizeSnippet(inscript.activeCharacter, "n", 0, step * resizeSign);
			break;
		case 65:	// 'a'
			inscript.resizeSnippet(inscript.activeCharacter, "w", step * resizeSign, 0);
			break;
		case 83:	// 's'
			inscript.resizeSnippet(inscript.activeCharacter, "s", 0, step * resizeSign);
			break;
		case 68:	// 'd' 
			inscript.resizeSnippet(inscript.activeCharacter, "e", step * resizeSign, 0);
			break;
		case 73:	// 'i'
			inscript.moveSnippet(inscript.activeCharacter, 0, -step);
			break;
		case 74:	// 'j'
			inscript.moveSnippet(inscript.activeCharacter, -step, 0);
			break;
		case 75:	// 'k'
			inscript.moveSnippet(inscript.activeCharacter, 0, step);
			break;
		case 76:	// 'l'
			inscript.moveSnippet(inscript.activeCharacter, step, 0);
			break;
		case 37:	// l_arrow
			InscriptCharacter chl = inscript.getCharacterRC(inscript.activeCharacter.row+1, inscript.activeCharacter.column);
			if(chl != null) inscript.activeCharacter = chl;
			break;
		case 38:	// u_arrow
			InscriptCharacter chu = inscript.getCharacterRC(inscript.activeCharacter.row, inscript.activeCharacter.column-1);
			if(chu != null) inscript.activeCharacter = chu;
			break;
		case 39:	// r_arrow
			InscriptCharacter chr = inscript.getCharacterRC(inscript.activeCharacter.row-1, inscript.activeCharacter.column);
			if(chr != null) inscript.activeCharacter = chr;
			break;
		case 40:	// d_arrow
			InscriptCharacter chd = inscript.getCharacterRC(inscript.activeCharacter.row, inscript.activeCharacter.column+1);
			if(chd != null) inscript.activeCharacter = chd;
			break;

		default:
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

}
