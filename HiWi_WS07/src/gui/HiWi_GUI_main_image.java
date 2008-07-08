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

import src.controller.MouseControllerMainImage1;
import src.controller.MouseControllerMainImage2;
import src.model.HiWi_Object_Sutra;
import src.model.HiWi_Object_Sign;
import src.util.prefs.PrefUtil;

@SuppressWarnings("serial")
public class HiWi_GUI_main_image extends JPanel{
	
	public HiWi_GUI root;	
	public HiWi_Object_Sutra s;	// current sutra
	//public HiWi_Object_Sign sn = null;	//	current sign
	
	public HiWi_GUI_main_image_sub sub;
	
	public JScrollPane scroll_image;
	
	MouseControllerMainImage1 mouse1;
	MouseControllerMainImage2 mouse2;
	
	public boolean existingSign = false;
	
	public double scale = 1.0;
	public double scaleFactor = 1.1;

	public HiWi_GUI_main_image(HiWi_GUI jf, HiWi_Object_Sutra sutra){
		super();
		setLayout(new BorderLayout());
		setFocusable(true);

		this.root = jf;
		this.s = sutra;
		
		this.mouse1 = new MouseControllerMainImage1(this);
		this.mouse2 = new MouseControllerMainImage2(this);
		
		sub = new HiWi_GUI_main_image_sub();
		
		scroll_image = new JScrollPane(sub);
		//scroll_image.setPreferredSize(new Dimension(800, 600));
		/*scroll_image.getHorizontalScrollBar().setUnitIncrement(100);
		scroll_image.getHorizontalScrollBar().setBlockIncrement(200);
		scroll_image.getVerticalScrollBar().setUnitIncrement(100);
		scroll_image.getVerticalScrollBar().setBlockIncrement(200);*/
		
		add(scroll_image, BorderLayout.CENTER);
	}
		
	 /** 
	  * The component inside the scroll pane. 
	  * **/
    @SuppressWarnings("serial")
	public class HiWi_GUI_main_image_sub extends JPanel {
    	protected void clear(){
    		super.paintComponent(this.getGraphics());
    	}
        protected void paintComponent(Graphics gg) {
            super.paintComponent(gg);
            //
            Color rubbingColor = PrefUtil.String2Color(root.props.getProperty("local.color.rubbing"));
            Float rubbingAlpha = Float.parseFloat(root.props.getProperty("local.alpha.rubbing"));
            
            Graphics2D g = (Graphics2D) gg;
            g.scale(scale, scale);
            g.setBackground(rubbingColor);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rubbingAlpha));
            if(s.sutra_image != null) g.drawImage(s.sutra_image, 0, 0, this);
    		for(int i=0; i<s.sutra_text.size(); i++){
    			ArrayList<ArrayList<HiWi_Object_Sign>> signvariants = s.sutra_text.get(i);
    			ArrayList<HiWi_Object_Sign> signs = signvariants.get(0);
    			HiWi_Object_Sign sign = signs.get(0);
    			sign.draw(g);
    		}
    		
    		// adjust scrolling speed
    		if(s.sutra_image != null){
    			int hspeed = s.sutra_image.getWidth() / 10;
    			int vspeed = s.sutra_image.getHeight()/ 10;
    			scroll_image.getHorizontalScrollBar().setUnitIncrement(hspeed/2);
    			scroll_image.getVerticalScrollBar().setUnitIncrement(vspeed/2);
    			//scroll_image.getHorizontalScrollBar().setBlockIncrement(hspeed*2);
    			//scroll_image.getVerticalScrollBar().setBlockIncrement(hspeed*2);
    		}
        }
    }
    
    public void clearMouseControllers(){
    	for(int i=0; i<sub.getMouseListeners().length; i++){
    		sub.removeMouseListener(sub.getMouseListeners()[0]);
    	}
    	for(int i=0; i<sub.getMouseMotionListeners().length; i++){
    		sub.removeMouseMotionListener(sub.getMouseMotionListeners()[0]);
    	}
    }
    
    public void changeMouseController(String type){
    	if(type.equals("auto")){
    		clearMouseControllers();
    		
    		this.sub.addMouseListener(mouse1);
    		this.sub.addMouseMotionListener(mouse1);
    		
    		//System.out.println("Mouse Controller:\t"+sub.getMouseListeners()[0].getClass().toString());
    	}
    	if(type.equals("manual")){
    		clearMouseControllers();
    		
    		this.sub.addMouseListener(mouse2);
    		this.sub.addMouseMotionListener(mouse2);
    		
    		//System.out.println("Mouse Controller:\t"+sub.getMouseListeners()[0].getClass().toString());
    	}
    }
}
