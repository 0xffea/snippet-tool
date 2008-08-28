package src.controller;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JOptionPane;

import src.gui.HiWi_GUI_main_image;
import src.util.prefs.PrefUtil;

/**
 * 'Complementary' Mouse controller.
 * Used for selective manual marking. If selected, starts marking selected character. Current character to mark must be selected in Snippet-Tool Text component.
 * Left button is used for marking.
 * 
 * @author Alexei Bratuhin
 *
 */
public class MouseControllerMainImage3 implements MouseListener, MouseMotionListener{

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

	public MouseControllerMainImage3(HiWi_GUI_main_image mi){
		super();
		this.main_image = mi;
	}

	public void mouseClicked(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}

	@SuppressWarnings("static-access")
	public void mousePressed(MouseEvent me) {
		if(me.getButton() == me.BUTTON1){
			main_image.requestFocusInWindow();
			mouse_pressed = new Point((int)(me.getX()/main_image.scale), (int)(me.getY()/main_image.scale));

			// repaint
			main_image.root.main.repaint();
		}
	}

	@SuppressWarnings("static-access")
	public void mouseReleased(MouseEvent me) {
		if(me.getButton() == me.BUTTON1){
			mouse_released = new Point((int)(me.getX()/main_image.scale), (int)(me.getY()/main_image.scale));
			//int dx = mouse_released.x - mouse_current_new.x;
			//int dy = mouse_released.y - mouse_current_new.y;
			
			// compute new dimension of snippet marking
			Dimension dr = new Dimension(Math.abs(mouse_current_new.x-mouse_pressed.x), Math.abs(mouse_current_new.y-mouse_pressed.y));
			Point pr = (Point) mouse_pressed.clone();
			if(mouse_current_new.x-mouse_pressed.x<0) pr.x -= dr.width;
			if(mouse_current_new.y-mouse_pressed.y<0) pr.y -= dr.height;

			Rectangle r = new Rectangle(pr, dr);

			// check whether new dimension satisfies prefernces selected (minimal width and minimal height)
			int min = PrefUtil.string2integer(main_image.root.props.getProperty("local.snippet.minsize"));
			if(r.height<min || r.width <min){
				JOptionPane.showMessageDialog(main_image.root, "Markup rectangle too small!", "Alert!", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			// update dimension of snippet marking
			main_image.s.updateSnippet(r, main_image.s.getActiveSignNumber()-1);
			
			// set active
			main_image.root.text.setSelected(main_image.s.getActiveCharacter());
			
			// repaint
			main_image.root.main.repaint();
		}
	}

	@SuppressWarnings("static-access")
	public void mouseDragged(MouseEvent me) {
		if(me.getModifiers() == me.BUTTON1_MASK){
			mouse_current_old = new Point(mouse_current_new.x, mouse_current_new.y);
			mouse_current_new = new Point((int)(me.getX()/main_image.scale), (int)(me.getY()/main_image.scale));
			//int dx = mouse_current_new.x - mouse_current_old.x;
			//int dy = mouse_current_new.y - mouse_current_old.y;
			
			// compute new dimension of snippet marking
			Dimension dr = new Dimension(Math.abs(mouse_current_new.x-mouse_pressed.x), Math.abs(mouse_current_new.y-mouse_pressed.y));
			Point pr = (Point) mouse_pressed.clone();
			if(mouse_current_new.x-mouse_pressed.x<0) pr.x -= dr.width;
			if(mouse_current_new.y-mouse_pressed.y<0) pr.y -= dr.height;

			Rectangle r = new Rectangle(pr, dr);
			
			// update dimension of snippet marking
			main_image.s.updateSnippet(r, main_image.s.getActiveSignNumber()-1);
			
			// set active
			main_image.root.text.setSelected(main_image.s.getActiveCharacter());
			
			// repaint
			main_image.root.main.repaint();
		}
	}

	public void mouseMoved(MouseEvent me) {
	}

	public void setActiveSign(int n){
		// set currently active sign
		//main_image.sn = main_image.s.getSign(n, 0);
		main_image.s.setActiveCharacterNumber(n);
		// update reference for showing info about currently active sign
		main_image.root.info.showInfo(n);
	}
}
