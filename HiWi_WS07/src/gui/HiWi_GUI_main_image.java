package src.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import src.model.HiWi_Object_Sutra;
import src.model.HiWi_Object_Sutra.HiWi_Object_Sign;
import src.util.prefs.PrefUtil;

@SuppressWarnings("serial")
public class HiWi_GUI_main_image extends JPanel implements MouseListener, MouseMotionListener{
	
	HiWi_GUI root;	
	HiWi_Object_Sutra s;	// current sutra
	HiWi_Object_Sign sn = null;	//	current sign
	
	HiWi_GUI_main_image_sub sub;
	
	JScrollPane scroll_image;

	Point mouse_pressed = new Point();
	Point mouse_released = new Point();
	Point mouse_current_old = new Point();
	Point mouse_current_new = new Point();
	
	public boolean existingSign = false;
	
	public double scale = 1.0;
	public double scaleFactor = 1.1;

	public HiWi_GUI_main_image(HiWi_GUI jf, HiWi_Object_Sutra sutra){
		super();
		setLayout(new BorderLayout());
		setFocusable(true);

		this.root = jf;
		this.s = sutra;
		
		sub = new HiWi_GUI_main_image_sub();
		sub.addMouseListener(this);
		sub.addMouseMotionListener(this);
		
		scroll_image = new JScrollPane(sub);
		//scroll_image.setPreferredSize(new Dimension(800, 600));
		scroll_image.getHorizontalScrollBar().setUnitIncrement(100);
		scroll_image.getHorizontalScrollBar().setBlockIncrement(200);
		scroll_image.getVerticalScrollBar().setUnitIncrement(100);
		scroll_image.getVerticalScrollBar().setBlockIncrement(200);
		
		add(scroll_image, BorderLayout.CENTER);
	}
		
	 /** 
	  * The component inside the scroll pane. 
	  * **/
    @SuppressWarnings("serial")
	public class HiWi_GUI_main_image_sub extends JPanel {
        protected void paintComponent(Graphics gg) {
            super.paintComponent(gg);
            Color rubbingColor = PrefUtil.String2Color(root.props.getProperty("local.color.rubbing"));
            Float rubbingAlpha = Float.parseFloat(root.props.getProperty("local.alpha.rubbing"));
            Graphics2D g = (Graphics2D) gg;
            g.scale(scale, scale);
            g.setBackground(rubbingColor);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rubbingAlpha));
    		g.drawImage(s.sutra_image, 0, 0, this);
    		for(int i=0; i<s.sutra_text.size(); i++){
    			ArrayList<HiWi_Object_Sign> signvariants = s.sutra_text.get(i);
    			HiWi_Object_Sign sign = signvariants.get(0);
    			sign.draw(g);
    		}
        }
    }
    
    public void setActiveSign(int n){
    	// set currently active sign
    	sn = s.getSign(n, 0);
    	// update reference for showing info about currently active sign
    	root.main.sign_info.setSign(sn);
    	root.main.sign_info.showInfo();
    }
    public void setActiveSign(HiWi_Object_Sign ss){
    	// set currently active sign
    	sn = ss;
    	// update reference for showing info about currently active sign
    	root.main.sign_info.setSign(sn);
    	root.main.sign_info.showInfo();
    }
	
	/** 
	 * handle mouse events 
	 * **/
	public void mouseClicked(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	@SuppressWarnings("static-access")
	public void mousePressed(MouseEvent me) {
		requestFocusInWindow();
		mouse_pressed = new Point((int)(me.getX()/scale), (int)(me.getY()/scale));
		//left button used
		if(me.getButton() == me.BUTTON1){
			if(sn==null || !sn.s.getBounds2D().contains(mouse_pressed)){	// if not still the same sign used as active
				for(int i=0; i<s.sutra_text.size(); i++){	// find sign that should be active
					//System.out.println("Looking for active sign for LEFT_BUTTON_PRESSED");
					HiWi_Object_Sign sign = s.sutra_text.get(i).get(0);
					if(sign.s.getBounds2D().contains(mouse_current_new)){
						// found existing sign
						existingSign = true;
						// 
						setActiveSign(sign);
						// set selected sign as active
						s.setActiveSign(i+1);
						// no need to look for a valid sign further
						break;
					}
				}
			}
			else{
				if(sn!=null) existingSign = true;
				else existingSign = false;
			}
			// mark selected sign in text JPanel
			root.text.setSelected(sn.number, sn.row);
			// if mouse pressed outside any existing markup field, set the information fields correspondingly
			if(!existingSign){
				setActiveSign(null);
				s.setActiveSign(-1);
				root.text.setSelected(-1, -1);
			}
		}
		//right button used
		if(me.getButton() == me.BUTTON3){
			// select active sign
			if(sn==null || !sn.s.getBounds2D().contains(mouse_pressed)){	// if not still the same sign used as active
				for(int i=0; i<s.sutra_text.size(); i++){	// find sign that should be active
					//System.out.println("Looking for active sign for RIGHT_BUTTON_PRESSED");
					HiWi_Object_Sign sign = s.sutra_text.get(i).get(0);
					if(sign.s.contains(mouse_current_new)){
						// found existing sign
						existingSign = true;
						// 
						setActiveSign(sign);
						// set selected sign as active
						s.setActiveSign(i+1);
						// no need to look for a valid sign further
						break;
					}
				}
			}
			else{
				if(sn!=null) existingSign = true;
				else existingSign = false;
			}
			// mark selected sign in text JPanel
			root.text.setSelected(sn.number, sn.row);
			// if mouse pressed outside any existing markup field, set the information fields correspondingly
			if(!existingSign){
				setActiveSign(null);
				s.setActiveSign(-1);
				root.text.setSelected(-1, -1);
			}
			// change Cursor appearance
			setCursor(new Cursor(Cursor.MOVE_CURSOR));
		}
		// repaint
		root.main.repaint();
	}
	@SuppressWarnings("static-access")
	public void mouseReleased(MouseEvent me) {
		mouse_released = new Point((int)(me.getX()/scale), (int)(me.getY()/scale));
		int dx = mouse_released.x - mouse_current_new.x;
		int dy = mouse_released.y - mouse_current_new.y;
		//left button used
		if(me.getButton() == me.BUTTON1 && sn!=null){
			//sn.resizeSnippet(sn.computeMoveDirection(getCursor()), dx, dy);
			s.resizeSnippet(sn, sn.computeMoveDirection(getCursor()), dx, dy);
		}
		//right button used
		if(me.getButton() == me.BUTTON3 && sn!=null){
			//sn.moveSnippet(dx, dy);
			s.moveSnippet(sn, dx, dy);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		//
		//existingSign = false;
		// repaint
		root.main.repaint();
	}
	@SuppressWarnings("static-access")
	public void mouseDragged(MouseEvent me) {
		mouse_current_old = new Point(mouse_current_new.x, mouse_current_new.y);
		mouse_current_new = new Point((int)(me.getX()/scale), (int)(me.getY()/scale));
		int dx = mouse_current_new.x - mouse_current_old.x;
		int dy = mouse_current_new.y - mouse_current_old.y;
		//left button used
		if(me.getModifiers() == me.BUTTON1_MASK && sn!=null){
			//sn.resizeSnippet(sn.computeMoveDirection(getCursor()), dx, dy);
			s.resizeSnippet(sn, sn.computeMoveDirection(getCursor()), dx, dy);
		}
		//right button used
		if(me.getModifiers() == me.BUTTON3_MASK && sn!=null){
			//sn.moveSnippet(dx, dy);
			s.moveSnippet(sn, dx, dy);
		}
		//
		root.main.repaint();
	}
	public void mouseMoved(MouseEvent me) {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		mouse_current_old = new Point(mouse_current_new.x, mouse_current_new.y);
		mouse_current_new = new Point((int)(me.getX()/scale), (int)(me.getY()/scale));
		// get active sign
		//if(sn==null || !sn.s.contains(mouse_current_new)){// if not still the same active sign		
		//	for(int i=0; i<s.sutra_text.size(); i++){	// find out which sing should be active
		//		HiWi_Object_Sign sign = s.sutra_text.get(i).get(0);
		//		if(sign.s.getBounds2D().contains(mouse_current_new)){
					// select the sign the mouse cursor is currently over
		//			sn = sign;
		//			s.setActiveSign(i+1);	// matthias: it's annoying and inapplicable in case of one sign's snippet completely covering another 
		//		}
		//	}
		//}
		// change cursor appearance
		if(sn!=null){
			String cursorPlace = sn.placeOnBorder(mouse_current_new);
			if(cursorPlace!=null && !cursorPlace.equals("none")){
				if(cursorPlace.equals("nw")) {setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("n")) {setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("ne")) {setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("e")) {setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("se")) {setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("s")) {setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("sw")) {setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));return;}
				if(cursorPlace.equals("w")) {setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));return;}
			}
		}
	}
}
