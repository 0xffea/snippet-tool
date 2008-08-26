package src.controller;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import src.gui.HiWi_GUI_main_image;
import src.model.HiWi_Object_Character;

public class MouseControllerMainImage1 implements MouseListener, MouseMotionListener{
	
	HiWi_GUI_main_image main_image;
	
	Point mouse_pressed = new Point();
	Point mouse_released = new Point();
	Point mouse_current_old = new Point();
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
		main_image.requestFocusInWindow();
		mouse_pressed = new Point((int)(me.getX()/main_image.scale), (int)(me.getY()/main_image.scale));
		//left button used
		if(me.getButton() == me.BUTTON1){
			if(main_image.s.getActiveSign()==null || !main_image.s.getActiveSign().s.getBounds2D().contains(mouse_pressed)){	// if not still the same sign used as active
				for(int i=0; i<main_image.s.sutra_text.size(); i++){	// find sign that should be active
					//System.out.println("Looking for active sign for LEFT_BUTTON_PRESSED");
					HiWi_Object_Character sign = main_image.s.sutra_text.get(i).get(0).get(0);
					if(sign.s.getBounds2D().contains(mouse_current_new)){
						// found existing sign
						main_image.existingSign = true;
						// 
						setActiveSign(i);
						// set selected sign as active
						main_image.s.setActiveSignNumber(i+1);
						// no need to look for a valid sign further
						break;
					}
				}
			}
			else{
				if(main_image.s.getActiveSign()!=null) main_image.existingSign = true;
				else main_image.existingSign = false;
			}
			// mark selected sign in text JPanel
			if(main_image.s.getActiveSign() != null) main_image.root.text.setSelected(main_image.s.getActiveSign());
			// if mouse pressed outside any existing markup field, set the information fields correspondingly
			if(!main_image.existingSign){
				setActiveSign(-1);
				main_image.s.setActiveSignNumber(-1);
				main_image.root.text.setSelected(main_image.s.getActiveSign());
			}
		}
		//right button used
		if(me.getButton() == me.BUTTON3){
			// select active sign
			if(main_image.s.getActiveSign()==null || !main_image.s.getActiveSign().s.getBounds2D().contains(mouse_pressed)){	// if not still the same sign used as active
				for(int i=0; i<main_image.s.sutra_text.size(); i++){	// find sign that should be active
					//System.out.println("Looking for active sign for RIGHT_BUTTON_PRESSED");
					HiWi_Object_Character sign = main_image.s.sutra_text.get(i).get(0).get(0);
					if(sign.s.contains(mouse_current_new)){
						// found existing sign
						main_image.existingSign = true;
						// 
						setActiveSign(i);
						// set selected sign as active
						main_image.s.setActiveSignNumber(i+1);
						// no need to look for a valid sign further
						break;
					}
				}
			}
			else{
				if(main_image.s.getActiveSign()!=null) main_image.existingSign = true;
				else main_image.existingSign = false;
			}
			// mark selected sign in text JPanel
			main_image.root.text.setSelected(main_image.s.getActiveSign());
			// if mouse pressed outside any existing markup field, set the information fields correspondingly
			if(!main_image.existingSign){
				setActiveSign(-1);
				main_image.s.setActiveSignNumber(-1);
				main_image.root.text.setSelected(main_image.s.getActiveSign());
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
		if(me.getButton() == me.BUTTON1 && main_image.s.getActiveSign()!=null){
			//main_image.sn.resizeSnippet(main_image.sn.computeMoveDirection(getCursor()), dx, dy);
			main_image.s.resizeSnippet(main_image.s.getActiveSign(), main_image.s.getActiveSign().computeMoveDirection(main_image.getCursor()), dx, dy);
		}
		//right button used
		if(me.getButton() == me.BUTTON3 && main_image.s.getActiveSign()!=null){
			//main_image.sn.moveSnippet(dx, dy);
			main_image.s.moveSnippet(main_image.s.getActiveSign(), dx, dy);
			main_image.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		//
		//existingSign = false;
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
		if(me.getModifiers() == me.BUTTON1_MASK && main_image.s.getActiveSign()!=null){
			//main_image.sn.resizeSnippet(main_image.sn.computeMoveDirection(getCursor()), dx, dy);
			main_image.s.resizeSnippet(main_image.s.getActiveSign(), main_image.s.getActiveSign().computeMoveDirection(main_image.getCursor()), dx, dy);
		}
		//right button used
		if(me.getModifiers() == me.BUTTON3_MASK && main_image.s.getActiveSign()!=null){
			//main_image.sn.moveSnippet(dx, dy);
			main_image.s.moveSnippet(main_image.s.getActiveSign(), dx, dy);
		}
		//
		main_image.root.main.repaint();
	}

	public void mouseMoved(MouseEvent me) {
		main_image.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		mouse_current_old = new Point(mouse_current_new.x, mouse_current_new.y);
		mouse_current_new = new Point((int)(me.getX()/main_image.scale), (int)(me.getY()/main_image.scale));
		
		// change cursor appearance
		if(main_image.s.getActiveSign()!=null){
			String cursorPlace = main_image.s.getActiveSign().placeOnBorder(mouse_current_new);
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

	public void setActiveSign(int n){
		// set currently active sign
		//main_image.sn = main_image.s.getSign(n, 0);
		main_image.s.setActiveSignNumber(n);
		// update reference for showing info about currently active sign
		main_image.root.info.showInfo(n);
	}
}
