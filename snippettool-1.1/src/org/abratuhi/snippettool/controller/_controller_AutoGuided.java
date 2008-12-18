package org.abratuhi.snippettool.controller;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.abratuhi.snippettool.gui._panel_Mainimage;
import org.abratuhi.snippettool.model.Inscript;
import org.abratuhi.snippettool.model.InscriptCharacter;
import org.abratuhi.snippettool.model.SnippetTool;

/**
 * 'Main' mouse controllerr.
 * Used for drag-n-resize of marking snippets.
 * Left button is used for selecting and resizing.
 * Right button is used for moving.
 * 
 * @author Alexei Bratuhin.
 *
 */
public class _controller_AutoGuided implements MouseListener, MouseMotionListener{
	
	/** Refernce to parent component **/
	_panel_Mainimage main_image;
	
	SnippetTool snippettool;
	Inscript inscript;
	
	/** Holds the last mousePressed event coordinates **/
	Point mouse_pressed = new Point();
	
	/** Holds the last mouseReleased event coordinates **/
	Point mouse_released = new Point();
	
	/** Holds the prelast mouse event coordinates **/
	Point mouse_current_old = new Point();
	
	/** Holds the last mouseevent coordinates **/
	Point mouse_current_new = new Point();
	
	
	public _controller_AutoGuided(_panel_Mainimage mi){
		this.main_image = mi;
		this.snippettool = mi.root.snippettool;
		this.inscript = snippettool.inscript;
	}

	public void mouseClicked(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}

	@SuppressWarnings("static-access")
	public void mousePressed(MouseEvent me) {
		//
		main_image.requestFocusInWindow();
		mouse_pressed = new Point((int)(me.getX()/snippettool.scale), (int)(me.getY()/snippettool.scale));
		
		//left button used
		if(me.getButton() == me.BUTTON1){
			// if not still the same sign used as active
			if(inscript.activeCharacter==null || !inscript.activeCharacter.shape.main.contains(mouse_pressed)){
				// find sign that should be active
				for(int i=0; i<inscript.text.size(); i++){
					// check, whether marking bounds contain mousePressed coordinates
					InscriptCharacter sign = inscript.text.get(i).get(0).get(0);
					if(sign.shape.main.contains(mouse_current_new)){
						// set flag: found existing sign
						snippettool.existingSign = true;
						// set character
						inscript.activeCharacter = sign;
						// no need to look for a valid sign further
						break;
					}
				}
			}
			// same (old) sign used as active
			else{
				if(inscript.activeCharacter!=null) snippettool.existingSign = true;
				else snippettool.existingSign = false;
			}
			
			// mark selected sign in text JPanel
			if(inscript.activeCharacter != null) main_image.root.text.setSelected(inscript.activeCharacter);
			
			// if mouse pressed outside any existing markup field, set the information fields correspondingly
			if(!snippettool.existingSign){
				inscript.activeCharacter = null;
				main_image.root.text.setSelected(inscript.activeCharacter);
			}
		}
		
		//right button used
		if(me.getButton() == me.BUTTON3){
			// select active sign
			// if not still the same sign used as active
			if(inscript.activeCharacter==null || !inscript.activeCharacter.shape.main.contains(mouse_pressed)){
				// find sign that should be active
				for(int i=0; i<inscript.text.size(); i++){
					//
					InscriptCharacter sign = inscript.text.get(i).get(0).get(0);
					if(sign.shape.main.contains(mouse_current_new)){
						// found existing sign
						snippettool.existingSign = true;
						// set selected sign as active
						inscript.activeCharacter = sign;
						// no need to look for a valid sign further
						break;
					}
				}
			}
			else{
				if(inscript.activeCharacter!=null) snippettool.existingSign = true;
				else snippettool.existingSign = false;
			}
			
			// mark selected sign in text JPanel
			main_image.root.text.setSelected(inscript.activeCharacter);
			
			// if mouse pressed outside any existing markup field, set the information fields correspondingly
			if(!snippettool.existingSign){
				inscript.activeCharacter = null;
				main_image.root.text.setSelected(inscript.activeCharacter);
			}
			
			// change Cursor appearance
			main_image.setCursor(new Cursor(Cursor.MOVE_CURSOR));
		}
		
		
		// repaint
		main_image.root.repaint();
	}

	@SuppressWarnings("static-access")
	public void mouseReleased(MouseEvent me) {
		mouse_released = new Point((int)(me.getX()/snippettool.scale), (int)(me.getY()/snippettool.scale));
		int dx = mouse_released.x - mouse_current_new.x;
		int dy = mouse_released.y - mouse_current_new.y;
		//left button used
		if(me.getButton() == me.BUTTON1 && inscript.activeCharacter!=null){
			inscript.resizeSnippet(inscript.activeCharacter, inscript.activeCharacter.computeMoveDirection(main_image.getCursor()), dx, dy);
		}
		//right button used
		if(me.getButton() == me.BUTTON3 && inscript.activeCharacter!=null){
			inscript.moveSnippet(inscript.activeCharacter, dx, dy);
			main_image.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		// repaint
		main_image.root.repaint();
	}

	@SuppressWarnings("static-access")
	public void mouseDragged(MouseEvent me) {
		mouse_current_old = new Point(mouse_current_new);
		mouse_current_new = new Point((int)(me.getX()/snippettool.scale), (int)(me.getY()/snippettool.scale));
		int dx = mouse_current_new.x - mouse_current_old.x;
		int dy = mouse_current_new.y - mouse_current_old.y;
		
		System.out.println(MouseEvent.getMouseModifiersText(me.getModifiers()));
		
		// left button used
		if(me.getModifiers() == me.BUTTON1_MASK && inscript.activeCharacter!=null){
			inscript.resizeSnippet(inscript.activeCharacter, inscript.activeCharacter.computeMoveDirection(main_image.getCursor()), dx, dy);
		}
		
		// shift + left button 
		if(MouseEvent.getMouseModifiersText(me.getModifiers()).equals("Shift+Button1") && inscript.activeCharacter!=null){
			inscript.rotateSnippet(inscript.activeCharacter, Math.toRadians(1.0)*(dx>0?-1:1));
		}
		
		// right button used
		if(me.getModifiers() == me.BUTTON3_MASK && inscript.activeCharacter!=null){
			inscript.moveSnippet(inscript.activeCharacter, dx, dy);
		}
		
		// repaint
		main_image.root.repaint();
	}

	public void mouseMoved(MouseEvent me) {
		main_image.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		mouse_current_old = new Point(mouse_current_new.x, mouse_current_new.y);
		mouse_current_new = new Point((int)(me.getX()/snippettool.scale), (int)(me.getY()/snippettool.scale));
		
		// change cursor appearance
		if(inscript.activeCharacter!=null){
			String cursorPlace = "none";
			if(cursorPlace!=null && !cursorPlace.equals("none")){
				if(cursorPlace.equals("nw")) {main_image.setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("n")) {main_image.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("ne")) {main_image.setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("e")) {main_image.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("se")) {main_image.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("s")) {main_image.setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("sw")) {main_image.setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("w")) {main_image.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));return;}
			}
		}
	}
}
