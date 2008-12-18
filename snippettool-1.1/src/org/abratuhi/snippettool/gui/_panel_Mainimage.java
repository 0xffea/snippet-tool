package org.abratuhi.snippettool.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.abratuhi.snippettool.controller._controller_AutoGuided;
import org.abratuhi.snippettool.controller._controller_Keyboard;
import org.abratuhi.snippettool.controller._controller_ManualSequential;
import org.abratuhi.snippettool.controller._controller_ManualSelective;
import org.abratuhi.snippettool.model.InscriptCharacter;
import org.abratuhi.snippettool.model.SnippetTool;
import org.abratuhi.snippettool.util.PrefUtil;

/**
 * Snippet-Tool main_image component, subcomponent of Snippet-tool's main component.
 * Holds image and attached mouse listeners for marking the inscript.
 * 
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class _panel_Mainimage extends JPanel{
	
	/** Reference to parent component **/
	public _frame_SnippetTool root;
	
	SnippetTool snippettool;
	Properties preferences;
	
	/** Component actually holding the image, inserted for less complicated coding **/
	public HiWi_GUI_main_image_sub sub;
	
	/** Scroll component holding the image pane **/
	public JScrollPane scroll_image;
	
	/** 'Main' mouse controller. Implements drag-n-resize functionality. **/
	_controller_AutoGuided mouse1;
	
	/** 'Complementary' mouse controller. Implements continuous manual marking. **/
	_controller_ManualSequential mouse2;
	
	/** 'Complementary' mouse controller. Implements selective manual marking. **/
	_controller_ManualSelective mouse3;

	
	public _panel_Mainimage(_frame_SnippetTool jf, SnippetTool snippettool){
		super();
		setLayout(new BorderLayout());
		setFocusable(true);

		this.root = jf;
		this.snippettool = snippettool;
		this.preferences = this.root.preferences;
		
		this.mouse1 = new _controller_AutoGuided(this);
		this.mouse2 = new _controller_ManualSequential(this);
		this.mouse3 = new _controller_ManualSelective(this);
		
		sub = new HiWi_GUI_main_image_sub();
		
		scroll_image = new JScrollPane(sub);
		
		add(scroll_image, BorderLayout.CENTER);
		
		changeMouseController("auto");
		addKeyListener(new _controller_Keyboard(this));
	}
		
	 /** 
	  * The component inside the scroll pane, actually holding the image. 
	  * **/
    public class HiWi_GUI_main_image_sub extends JPanel {
    	protected void clear(){
    		super.paintComponent(this.getGraphics());
    	}
    	public void setAlpha(Graphics2D g, float alpha){
    		int rule = AlphaComposite.SRC_OVER;
    		AlphaComposite ac;
    		ac = AlphaComposite.getInstance(rule, alpha);
    		g.setComposite(ac);
    	}
    	public void drawCharacter(Graphics2D g, InscriptCharacter character){
    		// produce graphics derivatives
    		character.shape.derivate();
    		
    		// adjust font
    		Font f = character.inscript.getFont();
    		float fontBaseSize = (float)(Math.min(character.shape.base.width, character.shape.base.height));
    		
    		Float alpha;
    		Color color;
    		
    		// draw border
    		alpha = Float.parseFloat(preferences.getProperty("local.alpha.marking.border"));
    		color = PrefUtil.String2Color(preferences.getProperty("local.color.marking.border"));
    		setAlpha(g, alpha);
    		g.setColor(color);
    		g.draw(character.shape.main);
    		
    		// draw marking
    		if(!character.equals(character.inscript.activeCharacter)) {
    			alpha = Float.parseFloat(preferences.getProperty("local.alpha.marking.p"));
    			color = PrefUtil.String2Color(preferences.getProperty("local.color.marking.p"));
    		}
    		else {
    			alpha = Float.parseFloat(preferences.getProperty("local.alpha.marking.a"));
    			color = PrefUtil.String2Color(preferences.getProperty("local.color.marking.a"));
    		}
    		setAlpha(g, alpha);
    		g.setColor(color);
    		g.fill(character.shape.main);
    		
    		// draw text
    		alpha = Float.parseFloat(preferences.getProperty("local.alpha.text"));
    		color = PrefUtil.String2Color(preferences.getProperty("local.color.text"));
    		setAlpha(g, alpha);
    		g.setColor(color);
    		
    		AffineTransform textrotator = g.getTransform();
    		textrotator.rotate(-character.shape.angle, character.shape.center.x, character.shape.center.y);
    		g.setTransform(textrotator);
    		if(character.inscript.showCharacter){
    			g.setFont(f.deriveFont(fontBaseSize));
    			g.drawString(character.characterStandard, character.shape.base.x, character.shape.base.y+g.getFontMetrics().getHeight()*25/40);
    		}
    		if(character.inscript.showNumber){
    			g.setFont(f.deriveFont(fontBaseSize/3.0f));
    			g.drawString(String.valueOf(character.number), character.shape.base.x, character.shape.base.y+g.getFontMetrics().getAscent());
    		}
    		if(character.inscript.showRowColumn){
    			g.setFont(f.deriveFont(fontBaseSize/5.0f));
    			g.drawString("("+String.valueOf(character.row)+","+String.valueOf(character.column)+")", character.shape.base.x, character.shape.base.y+g.getFontMetrics().getAscent());
    		}
    		AffineTransform textderotator = g.getTransform();
    		textderotator.rotate(character.shape.angle, character.shape.center.x, character.shape.center.y);
    		g.setTransform(textderotator);
    	}
        protected void paintComponent(Graphics gg) {
            super.paintComponent(gg);
            // load paint properties
            Color rubbingColor = PrefUtil.String2Color(preferences.getProperty("local.color.rubbing"));
            Float rubbingAlpha = Float.parseFloat(preferences.getProperty("local.alpha.rubbing"));
            
            // draw background
            Graphics2D g = (Graphics2D) gg;
            g.scale(snippettool.scale, snippettool.scale);
            g.setBackground(rubbingColor);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rubbingAlpha));
            
            // draw image
            if(snippettool.inscript.image != null) 
            	g.drawImage(snippettool.inscript.image, 0, 0, this);
            
            // draw marking
    		for(int i=0; i<snippettool.inscript.text.size(); i++){
    			InscriptCharacter sign = snippettool.inscript.text.get(i).get(0).get(0);
    			if(sign.shape.base.width>0 && sign.shape.base.height>0) drawCharacter(g, sign);
    		}
    		
    		// adjust scrolling speed
    		if(snippettool.inscript.image != null){
    			int hspeed = snippettool.inscript.image.getWidth() / 10;
    			int vspeed = snippettool.inscript.image.getHeight()/ 10;
    			scroll_image.getHorizontalScrollBar().setUnitIncrement(hspeed/2);
    			scroll_image.getVerticalScrollBar().setUnitIncrement(vspeed/2);
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
    	}
    	if(type.equals("manual1")){
    		clearMouseControllers();
    		
    		this.sub.addMouseListener(mouse2);
    		this.sub.addMouseMotionListener(mouse2);
    	}
    	if(type.equals("manual2")){
    		clearMouseControllers();
    		
    		this.sub.addMouseListener(mouse3);
    		this.sub.addMouseMotionListener(mouse3);
    	}
    }
}
