package org.abratuhi.snippettool.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.abratuhi.snippettool.model.Inscript;
import org.abratuhi.snippettool.model.InscriptCharacter;
import org.abratuhi.snippettool.model.SnippetShape;
import org.abratuhi.snippettool.model.SnippetTool;

@SuppressWarnings("serial")
public class _panel_Main extends JPanel implements ActionListener, ChangeListener{
	
	/** as reference **/
	_frame_SnippetTool root;

	SnippetTool snippettool;
	Inscript inscript;
	
	/** Sub JPanel containing image, where the marking is done **/
	public _panel_Mainimage main_image;
	
	/** JPanel containing Zoom functionality **/
	JPanel main_navigation = new JPanel();
	
	/** JPanel containing CLEAR ad SUBMIT Buttons**/
	JPanel main_buttons = new JPanel();
	
	/** JPanel containing 2 further buttons for scaling image to one of predefined sizes **/
	JPanel main_workspace = new JPanel();
	
	JButton zoom_in = new JButton("+");
	JButton zoom_out = new JButton("-");
	_slider_JZoomSlider zoomer = new _slider_JZoomSlider(_slider_JZoomSlider.HORIZONTAL, -10, 10, 0);
	
	/** Fit whole image in Sub JPanel **/
	JButton fit_image_max = new JButton("Full");
	/** Fit width image in Sub JPanel **/
	JButton fit_image_min = new JButton("Fit");
	
	/** Clear marking **/
	JButton clear = new JButton("Clear");
	/** Submit marking **/
	JButton submit =  new JButton("Submit");
	
	
	/**
	 * 
	 * @param jf reference to parent JFrame
	 * @param sutra refernce to parent's inscript
	 */
	public _panel_Main(_frame_SnippetTool jf, SnippetTool snippettool){
		super();

		this.root = jf;
		this.snippettool = snippettool;
		this.inscript = snippettool.inscript;
		this.main_image = new _panel_Mainimage(jf, snippettool);
		
		setLayout(new BorderLayout());
		
		/* creating Zoom JPanel */
		main_navigation.add(zoom_out);
		main_navigation.add(zoomer);
		main_navigation.add(zoom_in);
		
		zoom_in.addActionListener(this);
		zoomer.addChangeListener(this);
		zoom_out.addActionListener(this);
		
		/* creating extra Zoom options JPanel */
		main_workspace.setLayout(new BoxLayout(main_workspace, BoxLayout.Y_AXIS));
		Box box1 = new Box(BoxLayout.Y_AXIS);
		box1.add(fit_image_max);
		box1.add(fit_image_min);
		main_workspace.add(box1);
		
		fit_image_max.addActionListener(this);
		fit_image_min.addActionListener(this);
		
		
		/* creating CLEAR/SUBMIT JPanel*/
		main_buttons.add(clear);
		main_buttons.add(submit);
		
		clear.addActionListener(this);
		submit.addActionListener(this);
		
		/* creating overall layout */
		add(main_navigation, BorderLayout.NORTH);
		add(main_image, BorderLayout.CENTER);
		add(main_buttons, BorderLayout.SOUTH);
		add(main_workspace, BorderLayout.EAST);
	}

	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		if(inscript != null){
			setBorder(new TitledBorder("main: [ " + inscript.path_rubbing + " ]"));
		}
		else{
			setBorder(new TitledBorder("main: "));
		}
	}
	
	public void actionPerformed(ActionEvent ae) {
		String cmd = ae.getActionCommand();
		if(cmd.equals(zoom_in.getActionCommand())){
			// compute new scale factor
			snippettool.scale *= snippettool.scaleFactor;
			// set zoomer jslider
			zoomer.setZoom(snippettool.scale);
			// adjust view
			main_image.sub.setPreferredSize(new Dimension((int)(snippettool.scale*inscript.image.getWidth(main_image)), (int)(snippettool.scale*inscript.image.getHeight(main_image))));
			main_image.sub.revalidate();
			// repaint
			root.main.main_image.repaint();
		}
		if(cmd.equals(zoom_out.getActionCommand())){
			// compute new scale factor
			snippettool.scale *= 1 / snippettool.scaleFactor;
			// set zoomer jslider
			zoomer.setZoom(snippettool.scale);
			// adjust view
			main_image.sub.setPreferredSize(new Dimension((int)(snippettool.scale*inscript.image.getWidth(main_image)), (int)(snippettool.scale*inscript.image.getHeight(main_image))));
			main_image.sub.revalidate();
			// repaint
			root.main.main_image.repaint();
		}
		if(cmd.equals(fit_image_max.getActionCommand())){
			// compute scale ratio
			Dimension dim_workspace = main_image.getSize();
			Dimension dim_image = new Dimension(inscript.image.getWidth(),inscript.image.getHeight());
			double horizontal_ratio = dim_workspace.width / (double)(dim_image.width);
			double vertical_ratio = dim_workspace.height / (double)(dim_image.height);
			double scale_ratio = ((horizontal_ratio < vertical_ratio)? horizontal_ratio : vertical_ratio);
			// set zoomer jslider
			zoomer.setZoom(scale_ratio);
			// scale
			snippettool.scale = scale_ratio;
			main_image.sub.setPreferredSize(new Dimension((int)(snippettool.scale*inscript.image.getWidth(main_image)), (int)(snippettool.scale*inscript.image.getHeight(main_image))));
			main_image.sub.revalidate();
			root.main.main_image.repaint();
		}
		if(cmd.equals(fit_image_min.getActionCommand())){
			// compute scale ratio
			Dimension dim_workspace = main_image.getSize();
			Dimension dim_image = new Dimension(inscript.image.getWidth(),inscript.image.getHeight());
			double horizontal_ratio = dim_workspace.width / (double)(dim_image.width);
			double vertical_ratio = dim_workspace.height / (double)(dim_image.height);
			double scale_ratio = ((horizontal_ratio > vertical_ratio)? horizontal_ratio : vertical_ratio);
			// set zoomer jslider
			zoomer.setZoom(scale_ratio);
			// scale
			snippettool.scale = scale_ratio;
			main_image.sub.setPreferredSize(new Dimension((int)(snippettool.scale*inscript.image.getWidth(main_image)), (int)(snippettool.scale*inscript.image.getHeight(main_image))));
			main_image.sub.revalidate();
			root.main.main_image.repaint();
		}
		if(cmd.equals(clear.getActionCommand())){
			
			// clear shapes
			for(int i=0; i<inscript.text.size(); i++){
				for(int j=0; j<inscript.text.get(i).size(); j++){
					for(int k=0; k<inscript.text.get(i).get(j).size(); k++){
						InscriptCharacter csign = inscript.text.get(i).get(j).get(k);
						Rectangle zeroRectangle = new Rectangle(0,0,0,0);
						csign.shape = new SnippetShape(zeroRectangle);
					}
				}
			}
			
			// reset mouse2 counter
			main_image.mouse2.reset();
			
			
			root.repaint();
		}
		if(cmd.equals(submit.getActionCommand())){
			Thread t1 = new Thread(){
				public void run(){
					snippettool.submitInscript();
					root.status("Finished Submit");
				}
			};
			t1.start();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// scale
		snippettool.scale = zoomer.getZoom();
		main_image.sub.setPreferredSize(new Dimension((int)(snippettool.scale*inscript.image.getWidth(main_image)), (int)(snippettool.scale*inscript.image.getHeight(main_image))));
		main_image.sub.revalidate();
		root.main.main_image.repaint();
	}
}
