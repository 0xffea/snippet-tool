package src.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import src.controller.MouseControllerMainImage1;
import src.controller.MouseControllerMainImage2;
import src.controller.MouseControllerMainImage3;
import src.model.HiWi_Object_Inscript;
import src.model.HiWi_Object_Character;
import src.util.prefs.PrefUtil;

/**
 * Snippet-Tool main_image component, subcomponent of Snippet-tool's main component.
 * Holds image and attached mouse listeners for marking the inscript.
 * 
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class HiWi_GUI_main_image extends JPanel{
	
	/** Reference to parent component **/
	public HiWi_GUI root;
	
	/** Reference to Inscript Object, connected to HiWi_GUI **/
	public HiWi_Object_Inscript s;	// current sutra
	
	/** Component actually holding the image, inserted for less complicated coding **/
	public HiWi_GUI_main_image_sub sub;
	
	/** Scroll component holding the image pane **/
	public JScrollPane scroll_image;
	
	/** 'Main' mouse controller. Implements drag-n-resize functionality. **/
	MouseControllerMainImage1 mouse1;
	
	/** 'Complementary' mouse controller. Implements continuous manual marking. **/
	MouseControllerMainImage2 mouse2;
	
	/** 'Complementary' mouse controller. Implements selective manual marking. **/
	MouseControllerMainImage3 mouse3;
	
	public boolean existingSign = false;
	
	/** Current scale factor of the image **/
	public double scale = 1.0;
	
	/** Constant, holding the scale factor for manual scaling with' zoom-in' and 'zoom-out' buttons **/
	public double scaleFactor = 1.1;

	
	public HiWi_GUI_main_image(HiWi_GUI jf, HiWi_Object_Inscript sutra){
		super();
		setLayout(new BorderLayout());
		setFocusable(true);

		this.root = jf;
		this.s = sutra;
		
		this.mouse1 = new MouseControllerMainImage1(this);
		this.mouse2 = new MouseControllerMainImage2(this);
		this.mouse3 = new MouseControllerMainImage3(this);
		
		sub = new HiWi_GUI_main_image_sub();
		
		scroll_image = new JScrollPane(sub);
		
		add(scroll_image, BorderLayout.CENTER);
	}
		
	 /** 
	  * The component inside the scroll pane, actually holding the image. 
	  * **/
    @SuppressWarnings("serial")
	public class HiWi_GUI_main_image_sub extends JPanel {
    	protected void clear(){
    		super.paintComponent(this.getGraphics());
    	}
        protected void paintComponent(Graphics gg) {
            super.paintComponent(gg);
            // load paint properties
            Color rubbingColor = PrefUtil.String2Color(root.props.getProperty("local.color.rubbing"));
            Float rubbingAlpha = Float.parseFloat(root.props.getProperty("local.alpha.rubbing"));
            
            // draw background
            Graphics2D g = (Graphics2D) gg;
            g.scale(scale, scale);
            g.setBackground(rubbingColor);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rubbingAlpha));
            
            // draw image
            if(s.sutra_image != null) g.drawImage(s.sutra_image, 0, 0, this);
            
            // draw marking
    		for(int i=0; i<s.sutra_text.size(); i++){
    			ArrayList<ArrayList<HiWi_Object_Character>> signvariants = s.sutra_text.get(i);
    			ArrayList<HiWi_Object_Character> signs = signvariants.get(0);
    			HiWi_Object_Character sign = signs.get(0);
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
    
    /**
     * Remove all mouse listeners attached to this JPanel
     */
    public void clearMouseControllers(){
    	for(int i=0; i<sub.getMouseListeners().length; i++){
    		sub.removeMouseListener(sub.getMouseListeners()[0]);
    	}
    	for(int i=0; i<sub.getMouseMotionListeners().length; i++){
    		sub.removeMouseMotionListener(sub.getMouseMotionListeners()[0]);
    	}
    }
    
    /**
     * Change currently active mouse controller
     * @param type	name of controller to be used. Possible values are:
     * 	<ul>
     * 	<li>auto - for MouseController1</li>
     * 	<li>manual1 - for MouseController2</li>
     * 	<li>manual2 - for Mousecontroller3</li>
     *  </ul>
     */
    public void changeMouseController(String type){
    	if(type.equals("auto")){
    		clearMouseControllers();
    		
    		this.sub.addMouseListener(mouse1);
    		this.sub.addMouseMotionListener(mouse1);
    		
    		//System.out.println("Mouse Controller:\t"+sub.getMouseListeners()[0].getClass().toString());
    	}
    	if(type.equals("manual1")){
    		clearMouseControllers();
    		
    		this.sub.addMouseListener(mouse2);
    		this.sub.addMouseMotionListener(mouse2);
    		
    		s.setActiveNumber(mouse2.currentIndex);
    		root.info.showInfo(mouse2.currentIndex);
    		root.text.setSelected(s.getActiveSign());
    		
    		//System.out.println("Mouse Controller:\t"+sub.getMouseListeners()[0].getClass().toString());
    	}
    	if(type.equals("manual2")){
    		clearMouseControllers();
    		
    		this.sub.addMouseListener(mouse3);
    		this.sub.addMouseMotionListener(mouse3);
    		
    		//System.out.println("Mouse Controller:\t"+sub.getMouseListeners()[0].getClass().toString());
    	}
    }
}
