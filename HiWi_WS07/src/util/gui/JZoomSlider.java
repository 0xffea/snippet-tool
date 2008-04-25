package src.util.gui;

import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;

@SuppressWarnings("serial")
public class JZoomSlider extends JSlider{
		
	public JZoomSlider(int orientation, int min, int max, int value){
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
	
	public void setZoom(double zoom){
		//if(zoom <= 0) return;
		//if(zoom > getMaximum()) setValue(getMaximum());
		if(zoom >= 1){
			setValue((int) (zoom-1));
		}
		if(zoom < 1){
			setValue((int) (-1/(zoom) + 1));
		}
	}

}
