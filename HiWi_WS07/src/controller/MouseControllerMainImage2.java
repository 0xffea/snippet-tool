package src.controller;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JOptionPane;

import src.gui.HiWi_GUI_main_image;
import src.model.HiWi_Object_Character;
import src.util.prefs.PrefUtil;

public class MouseControllerMainImage2 implements MouseListener, MouseMotionListener{

	HiWi_GUI_main_image main_image;

	Point mouse_pressed = new Point();
	Point mouse_released = new Point();
	Point mouse_current_old = new Point();
	Point mouse_current_new = new Point();

	public int currentIndex = 0;

	public MouseControllerMainImage2(HiWi_GUI_main_image mi){
		super();
		this.main_image = mi;

		this.currentIndex = 0;
	}

	public void reset(){
		this.currentIndex = 0;
		main_image.s.setActiveNumber(currentIndex);
		main_image.root.info.showInfo(currentIndex);
		main_image.root.text.setSelected(main_image.s.getActiveSign());
	}

	public void mouseClicked(MouseEvent me) {}

	public void mouseEntered(MouseEvent me) {}

	public void mouseExited(MouseEvent me) {}

	@SuppressWarnings("static-access")
	public void mousePressed(MouseEvent me) {
		if(me.getButton() == me.BUTTON1){
			main_image.requestFocusInWindow();
			mouse_pressed = new Point((int)(me.getX()/main_image.scale), (int)(me.getY()/main_image.scale));
			//
			//main_image.s.setActiveSignNumber(currentIndex+1);

			// repaint
			main_image.root.main.repaint();
		}
	}

	@SuppressWarnings("static-access")
	public void mouseReleased(MouseEvent me) {
		if(me.getButton() == me.BUTTON1){
			mouse_released = new Point((int)(me.getX()/main_image.scale), (int)(me.getY()/main_image.scale));
			int dx = mouse_current_new.x - mouse_current_old.x;
			int dy = mouse_current_new.y - mouse_current_old.y;

			Dimension dr = new Dimension(Math.abs(mouse_current_new.x-mouse_pressed.x), Math.abs(mouse_current_new.y-mouse_pressed.y));
			Point pr = (Point) mouse_pressed.clone();
			if(mouse_current_new.x-mouse_pressed.x<0) pr.x -= dr.width;
			if(mouse_current_new.y-mouse_pressed.y<0) pr.y -= dr.height;

			Rectangle r = new Rectangle(pr, dr);

			int min = PrefUtil.string2integer(main_image.root.props.getProperty("local.snippet.minsize"));
			if(r.height<min || r.width <min){
				JOptionPane.showMessageDialog(main_image.root, "Markup rectangle too small!", "Alert!", JOptionPane.ERROR_MESSAGE);
				return;
			}

			main_image.s.updateSnippet(r, currentIndex);
			//
			currentIndex++;

			// set next active
			main_image.s.setActiveSignNumber(currentIndex+1);
			if(currentIndex<main_image.s.sutra_text.size()) main_image.root.info.showInfo(currentIndex);
			if(currentIndex<main_image.s.sutra_text.size()) main_image.root.text.setSelected(main_image.s.getActiveSign());

			// repaint
			main_image.root.main.repaint();
		}
	}

	@SuppressWarnings("static-access")
	public void mouseDragged(MouseEvent me) {
		if(me.getModifiers() == me.BUTTON1_MASK){
			mouse_current_old = new Point(mouse_current_new.x, mouse_current_new.y);
			mouse_current_new = new Point((int)(me.getX()/main_image.scale), (int)(me.getY()/main_image.scale));

			int dx = mouse_current_new.x - mouse_current_old.x;
			int dy = mouse_current_new.y - mouse_current_old.y;

			Dimension dr = new Dimension(Math.abs(mouse_current_new.x-mouse_pressed.x), Math.abs(mouse_current_new.y-mouse_pressed.y));
			Point pr = (Point) mouse_pressed.clone();
			if(mouse_current_new.x-mouse_pressed.x<0) pr.x -= dr.width;
			if(mouse_current_new.y-mouse_pressed.y<0) pr.y -= dr.height;

			Rectangle r = new Rectangle(pr, dr);

			main_image.s.updateSnippet(r, currentIndex);
			// repaint
			main_image.root.main.repaint();
		}
	}

	public void mouseMoved(MouseEvent me) {
	}

	public void setActiveSign(int n){
		// set currently active sign
		//main_image.sn = main_image.s.getSign(n, 0);
		main_image.s.setActiveSignNumber(n);
		// update reference for showing info about currently active sign
		main_image.root.info.showInfo(n);
	}
}
