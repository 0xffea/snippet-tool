package org.abratuhi.snippettool.gui;

import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;

/**
 * JSlider extended to provide use for zoom functionality.
 * Notice: zoom(+2) ~ zoomFactor=2
 * Notice: zoom(-2) ~ zoomFactor=1/2
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class _slider_JZoomSlider extends JSlider{
		
	public _slider_JZoomSlider(int orientation, int min, int max, int value){
		// call super
		super(orientation, min, max, value);
		// add labels
		Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
		labels.put(new Integer(min), new JLabel("x1/"+String.valueOf(Math.abs(min))));
		labels.put(new Integer(value), new JLabel("x1"));
		labels.put(new Integer(max), new JLabel("x"+String.valueOf(max)));
		setLabelTable(labels);
		setPaintLabels(true);
	}
	
	/**
	 * Get Zoom Factor
	 * @return
	 */
	public double getZoom(){
		int z = getValue();
		double zoom;
		if(z > 0){	// zoom in
			zoom = 1+z;
		}
		else{	// zoom out
			zoom = 1/(double)(Math.abs(z)+1);
		}
		return zoom;
	}
	
	/**
	 * Set Zoom Factor
	 * @param zoom	zoom factor
	 */
	public void setZoom(double zoom){
		if(zoom >= 1){
			setValue((int) (zoom-1));
		}
		if(zoom < 1){
			setValue((int) (-1/(zoom) + 1));
		}
	}

}
