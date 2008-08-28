package src.controller;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import src.gui.HiWi_GUI_main_image;
import src.model.HiWi_Object_Character;

/**
 * 'Main' mouse controllerr.
 * Used for drag-n-resize of marking snippets.
 * Left button is used for selecting and resizing.
 * Right button is used for moving.
 * 
 * @author Alexei Bratuhin.
 *
 */
public class MouseControllerMainImage1 implements MouseListener, MouseMotionListener{
	
	/** Refernce to parent component **/
	HiWi_GUI_main_image main_image;
	
	/** Holds the last mousePressed event coordinates **/
	Point mouse_pressed = new Point();
	
	/** Holds the last mouseReleased event coordinates **/
	Point mouse_released = new Point();
	
	/** Holds the prelast mouse event coordinates **/
	Point mouse_current_old = new Point();
	
	/** Holds the last mouseevent coordinates **/
	Point mouse_current_new = new Point();
	
	
	public MouseControllerMainImage1(HiWi_GUI_main_image mi){
		super();
		this.main_image = mi;
	}

	public void mouseClicked(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}

	@SuppressWarnings("static-access")
	public void mousePressed(MouseEvent me) {
		//
		main_image.requestFocusInWindow();
		mouse_pressed = new Point((int)(me.getX()/main_image.scale), (int)(me.getY()/main_image.scale));
		
		//left button used
		if(me.getButton() == me.BUTTON1){
			// if not still the same sign used as active
			if(main_image.s.getActiveCharacter()==null || !main_image.s.getActiveCharacter().s.getBounds2D().contains(mouse_pressed)){
				// find sign that should be active
				for(int i=0; i<main_image.s.sutra_text.size(); i++){
					// check, whether marking bounds contain mousePressed coordinates
					HiWi_Object_Character sign = main_image.s.sutra_text.get(i).get(0).get(0);
					if(sign.s.getBounds2D().contains(mouse_current_new)){
						// set flag: found existing sign
						main_image.existingSign = true;
						// set character
						setActiveCharacter(i);
						// set selected sign as active
						main_image.s.setActiveCharacterNumber(i+1);
						// no need to look for a valid sign further
						break;
					}
				}
			}
			// same (old) sign used as active
			else{
				if(main_image.s.getActiveCharacter()!=null) main_image.existingSign = true;
				else main_image.existingSign = false;
			}
			
			// mark selected sign in text JPanel
			if(main_image.s.getActiveCharacter() != null) main_image.root.text.setSelected(main_image.s.getActiveCharacter());
			
			// if mouse pressed outside any existing markup field, set the information fields correspondingly
			if(!main_image.existingSign){
				setActiveCharacter(-1);
				main_image.s.setActiveCharacterNumber(-1);
				main_image.root.text.setSelected(main_image.s.getActiveCharacter());
			}
		}
		
		//right button used
		if(me.getButton() == me.BUTTON3){
			// select active sign
			// if not still the same sign used as active
			if(main_image.s.getActiveCharacter()==null || !main_image.s.getActiveCharacter().s.getBounds2D().contains(mouse_pressed)){
				// find sign that should be active
				for(int i=0; i<main_image.s.sutra_text.size(); i++){
					//
					HiWi_Object_Character sign = main_image.s.sutra_text.get(i).get(0).get(0);
					if(sign.s.contains(mouse_current_new)){
						// found existing sign
						main_image.existingSign = true;
						// 
						setActiveCharacter(i);
						// set selected sign as active
						main_image.s.setActiveCharacterNumber(i+1);
						// no need to look for a valid sign further
						break;
					}
				}
			}
			else{
				if(main_image.s.getActiveCharacter()!=null) main_image.existingSign = true;
				else main_image.existingSign = false;
			}
			
			// mark selected sign in text JPanel
			main_image.root.text.setSelected(main_image.s.getActiveCharacter());
			
			// if mouse pressed outside any existing markup field, set the information fields correspondingly
			if(!main_image.existingSign){
				setActiveCharacter(-1);
				main_image.s.setActiveCharacterNumber(-1);
				main_image.root.text.setSelected(main_image.s.getActiveCharacter());
			}
			
			// change Cursor appearance
			main_image.setCursor(new Cursor(Cursor.MOVE_CURSOR));
		}
		
		
		// repaint
		main_image.root.main.repaint();
	}

	@SuppressWarnings("static-access")
	public void mouseReleased(MouseEvent me) {
		mouse_released = new Point((int)(me.getX()/main_image.scale), (int)(me.getY()/main_image.scale));
		int dx = mouse_released.x - mouse_current_new.x;
		int dy = mouse_released.y - mouse_current_new.y;
		//left button used
		if(me.getButton() == me.BUTTON1 && main_image.s.getActiveCharacter()!=null){
			main_image.s.resizeSnippet(main_image.s.getActiveCharacter(), main_image.s.getActiveCharacter().computeMoveDirection(main_image.getCursor()), dx, dy);
		}
		//right button used
		if(me.getButton() == me.BUTTON3 && main_image.s.getActiveCharacter()!=null){
			main_image.s.moveSnippet(main_image.s.getActiveCharacter(), dx, dy);
			main_image.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		// repaint
		main_image.root.main.repaint();
	}

	@SuppressWarnings("static-access")
	public void mouseDragged(MouseEvent me) {
		mouse_current_old = new Point(mouse_current_new.x, mouse_current_new.y);
		mouse_current_new = new Point((int)(me.getX()/main_image.scale), (int)(me.getY()/main_image.scale));
		int dx = mouse_current_new.x - mouse_current_old.x;
		int dy = mouse_current_new.y - mouse_current_old.y;
		
		//left button used
		if(me.getModifiers() == me.BUTTON1_MASK && main_image.s.getActiveCharacter()!=null){
			//main_image.sn.resizeSnippet(main_image.sn.computeMoveDirection(getCursor()), dx, dy);
			main_image.s.resizeSnippet(main_image.s.getActiveCharacter(), main_image.s.getActiveCharacter().computeMoveDirection(main_image.getCursor()), dx, dy);
		}
		
		//right button used
		if(me.getModifiers() == me.BUTTON3_MASK && main_image.s.getActiveCharacter()!=null){
			//main_image.sn.moveSnippet(dx, dy);
			main_image.s.moveSnippet(main_image.s.getActiveCharacter(), dx, dy);
		}
		
		// repaint
		main_image.root.main.repaint();
	}

	public void mouseMoved(MouseEvent me) {
		main_image.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		mouse_current_old = new Point(mouse_current_new.x, mouse_current_new.y);
		mouse_current_new = new Point((int)(me.getX()/main_image.scale), (int)(me.getY()/main_image.scale));
		
		// change cursor appearance
		if(main_image.s.getActiveCharacter()!=null){
			String cursorPlace = main_image.s.getActiveCharacter().placeOnBorder(mouse_current_new);
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

	public void setActiveCharacter(int n){
		// set currently active character
		main_image.s.setActiveCharacterNumber(n);
		
		// update reference for showing info about currently active sign
		main_image.root.info.showInfo(n);
	}
}
