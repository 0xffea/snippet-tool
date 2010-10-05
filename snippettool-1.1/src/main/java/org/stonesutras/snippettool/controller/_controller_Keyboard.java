package org.stonesutras.snippettool.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import org.stonesutras.snippettool.gui._panel_Mainimage;
import org.stonesutras.snippettool.model.Inscript;
import org.stonesutras.snippettool.model.InscriptCharacter;
import org.stonesutras.snippettool.model.SnippetTool;

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
        boolean all = e.isAltDown();
		switch (code) {
        case 81: // 'q'
			inscript.rotateSnippet(inscript.getActiveCharacter() , Math.toRadians(1));
			break;
        case 69: // 'e'
			inscript.rotateSnippet(inscript.getActiveCharacter() , Math.toRadians(-1));
			break;
        case 87: // 'w'
            if (all)
                inscript.scaleAllSnippets(1.1);
            else
                inscript.resizeSnippet(inscript.getActiveCharacter(), "n", 0, -step * resizeSign);
			break;
        case 65: // 'a'
            if (all)
                inscript.scaleAllSnippets(0.97);
            else
                inscript.resizeSnippet(inscript.getActiveCharacter(), "w", -step * resizeSign, 0);
			break;
        case 83: // 's'
            if (all)
                inscript.scaleAllSnippets(0.9);
            else
                inscript.resizeSnippet(inscript.getActiveCharacter(), "s", 0, step * resizeSign);
			break;
        case 68: // 'd'
            if (all)
                inscript.scaleAllSnippets(1.03);
            else
                inscript.resizeSnippet(inscript.getActiveCharacter(), "e", step * resizeSign, 0);
			break;
        case 73: // 'i'
            if (all)
                inscript.moveAllSnippets(0, -step);
            else
                inscript.moveSnippet(inscript.getActiveCharacter(), 0, -step);
            break;
        case 74: // 'j'
            if (all)
                inscript.moveAllSnippets(-step, 0);
            else
                inscript.moveSnippet(inscript.getActiveCharacter(), -step, 0);
            break;
        case 75: // 'k'
            if (all)
                inscript.moveAllSnippets(0, step);
            else
                inscript.moveSnippet(inscript.getActiveCharacter(), 0, step);
            break;
        case 76: // 'l'
            if (all)
                inscript.moveAllSnippets(step, 0);
            else
                inscript.moveSnippet(inscript.getActiveCharacter(), step, 0);
            break;
		case 37:	// l_arrow
			InscriptCharacter chl = inscript.getCharacterRC(inscript.getActiveCharacter().getRow()+1, inscript.getActiveCharacter().getColumn());
			if(chl != null) inscript.setActiveCharacter(chl);
			break;
		case 38:	// u_arrow
			InscriptCharacter chu = inscript.getCharacterRC(inscript.getActiveCharacter().getRow(), inscript.getActiveCharacter().getColumn()-1);
			if(chu != null) inscript.setActiveCharacter(chu);
			break;
		case 39:	// r_arrow
			InscriptCharacter chr = inscript.getCharacterRC(inscript.getActiveCharacter().getRow()-1, inscript.getActiveCharacter().getColumn());
			if(chr != null) inscript.setActiveCharacter(chr);
			break;
		case 40:	// d_arrow
			InscriptCharacter chd = inscript.getCharacterRC(inscript.getActiveCharacter().getRow(), inscript.getActiveCharacter().getColumn()+1);
			if(chd != null) inscript.setActiveCharacter(chd);
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
