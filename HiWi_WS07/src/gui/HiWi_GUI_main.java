package src.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.BinaryResource;
import org.xmldb.api.modules.CollectionManagementService;

import src.model.HiWi_Object_Sutra;
import src.model.HiWi_Object_Sutra.HiWi_Object_Sign;
import src.util.gui.JZoomSlider;
import src.util.image.ImageUtil;
import src.util.num.NumUtil;
import src.util.xml.XMLUtil;

@SuppressWarnings("serial")
public class HiWi_GUI_main extends JPanel implements ActionListener, ChangeListener{
	
	HiWi_GUI root;	
	HiWi_Object_Sutra s;
	
	HiWi_GUI_main_image main_image;
	
	JPanel main_navigation = new JPanel();
	JPanel main_buttons = new JPanel();
	JPanel main_workspace = new JPanel();
	
	JButton zoom_in = new JButton("+");
	JButton zoom_out = new JButton("-");
	JZoomSlider zoomer = new JZoomSlider(JZoomSlider.HORIZONTAL, -10, 10, 0);
	
	JButton fit_image_max = new JButton("Full");
	JButton fit_image_min = new JButton("Fit");
	
	HiWi_GUI_info sign_info= new HiWi_GUI_info();
	JButton load_image = new JButton("Load Image");
	JButton load_text  = new JButton("Load Text");
	JButton submit =  new JButton("Submit");
	
	public HiWi_GUI_main(HiWi_GUI jf, HiWi_Object_Sutra sutra){
		super();
		
		this.main_image = new HiWi_GUI_main_image(jf, sutra);
		this.root = jf;
		this.s = sutra;
		
		setLayout(new BorderLayout());
		setBorder(new TitledBorder("main"));
		setPreferredSize(new Dimension(800,600));
		
		
		main_navigation.add(zoom_out);
		main_navigation.add(zoomer);
		main_navigation.add(zoom_in);
		
		zoom_in.addActionListener(this);
		zoomer.addChangeListener(this);
		zoom_out.addActionListener(this);
		
		main_workspace.setLayout(new BoxLayout(main_workspace, BoxLayout.Y_AXIS));
		Box box1 = new Box(BoxLayout.Y_AXIS);
		box1.add(fit_image_max);
		box1.add(fit_image_min);
		main_workspace.add(box1);
		
		fit_image_max.addActionListener(this);
		fit_image_min.addActionListener(this);
		

		main_buttons.add(sign_info);
		//main_buttons.add(load_text);
		//main_buttons.add(load_image);
		main_buttons.add(submit);
		
		load_image.addActionListener(this);
		load_text.addActionListener(this);
		submit.addActionListener(this);

		add(main_navigation, BorderLayout.NORTH);
		add(main_image, BorderLayout.CENTER);
		add(main_buttons, BorderLayout.SOUTH);
		add(main_workspace, BorderLayout.EAST);
	}
	
	public void paintComponent(Graphics g){
		// super 
		super.paintComponent(g);
		// beginning custom part
		// print info about currently active sign
		sign_info.showInfo();
	}
	
	public void actionPerformed(ActionEvent ae) {
		String cmd = ae.getActionCommand();
		if(cmd.equals(zoom_in.getActionCommand())){
			// compute new scale factor
			main_image.scale *= main_image.scaleFactor;
			// set zoomer jslider
			zoomer.setZoom(main_image.scale);
			// adjust view
			main_image.sub.setPreferredSize(new Dimension((int)(main_image.scale*s.sutra_image.getWidth(main_image)), (int)(main_image.scale*s.sutra_image.getHeight(main_image))));
			main_image.sub.revalidate();
			// repaint
			root.main.main_image.repaint();
		}
		if(cmd.equals(zoom_out.getActionCommand())){
			// compute new scale factor
			main_image.scale *= 1 / main_image.scaleFactor;
			// set zoomer jslider
			zoomer.setZoom(main_image.scale);
			// adjust view
			main_image.sub.setPreferredSize(new Dimension((int)(main_image.scale*s.sutra_image.getWidth(main_image)), (int)(main_image.scale*s.sutra_image.getHeight(main_image))));
			main_image.sub.revalidate();
			// repaint
			root.main.main_image.repaint();
		}
		if(cmd.equals(fit_image_max.getActionCommand())){
			// compute scale ratio
			Dimension dim_workspace = main_image.getSize();
			Dimension dim_image = new Dimension(s.sutra_image.getWidth(),s.sutra_image.getHeight());
			double horizontal_ratio = dim_workspace.width / (double)(dim_image.width);
			double vertical_ratio = dim_workspace.height / (double)(dim_image.height);
			double scale_ratio = ((horizontal_ratio < vertical_ratio)? horizontal_ratio : vertical_ratio);
			// set zoomer jslider
			zoomer.setZoom(scale_ratio);
			// scale
			main_image.scale = scale_ratio;
			main_image.sub.setPreferredSize(new Dimension((int)(main_image.scale*s.sutra_image.getWidth(main_image)), (int)(main_image.scale*s.sutra_image.getHeight(main_image))));
			main_image.sub.revalidate();
			root.main.main_image.repaint();
		}
		if(cmd.equals(fit_image_min.getActionCommand())){
			// compute scale ratio
			Dimension dim_workspace = main_image.getSize();
			Dimension dim_image = new Dimension(s.sutra_image.getWidth(),s.sutra_image.getHeight());
			double horizontal_ratio = dim_workspace.width / (double)(dim_image.width);
			double vertical_ratio = dim_workspace.height / (double)(dim_image.height);
			double scale_ratio = ((horizontal_ratio > vertical_ratio)? horizontal_ratio : vertical_ratio);
			// set zoomer jslider
			zoomer.setZoom(scale_ratio);
			// scale
			main_image.scale = scale_ratio;
			main_image.sub.setPreferredSize(new Dimension((int)(main_image.scale*s.sutra_image.getWidth(main_image)), (int)(main_image.scale*s.sutra_image.getHeight(main_image))));
			main_image.sub.revalidate();
			root.main.main_image.repaint();
		}
		if(cmd.equals(load_text.getActionCommand())){
			loadText();
		}
		if(cmd.equals(load_image.getActionCommand())){
			loadImage();
		}
		if(cmd.equals(submit.getActionCommand())){
			// get neede properties
			String dbURI = root.props.getProperty("db.uri");
			String dbUser = root.props.getProperty("db.user");
			String dbPass = root.props.getProperty("db.passwd");
			String dbOut = root.props.getProperty("db.dir.out");
			// check whether submit possible, e.g. image and text loaded, text added to image
			if(s.sutra_text.size()<1){
				JOptionPane.showMessageDialog(root, "Nothing to submit", "Alert!", JOptionPane.ERROR_MESSAGE);
				return;
			}
			//proceed for each sign - submit coordinates
			for(int i=0; i<s.sutra_text.size(); i++){
				for(int j=0; j<s.sutra_text.get(i).size(); j++){
					for(int k=0; k<s.sutra_text.get(i).get(j).size(); k++){
						HiWi_Object_Sign csign = s.sutra_text.get(i).get(j).get(k);
						
						root.addLogEntry("storing coordinates of nr.="+csign.number, 1, 1);
						
						XMLUtil.updateXML(root, NumUtil.dec2hex(csign.character.codePointAt(0)), csign.getXUpdate(s.updateOnly), dbURI, dbUser, dbPass, dbOut);
					}
				}
			}
			//proceed for each sign - submit snippet
			BufferedImage img_in = s.sutra_image;
			BufferedImage img_out_t;
			for(int i=0; i<s.sutra_text.size(); i++){
				Rectangle2D r = s.sutra_text.get(i).get(0).get(0).s.getBounds2D();
				//System.out.println(i+": ("+(int)r.getX()+","+(int)r.getY()+","+(int)r.getWidth()+","+(int)r.getHeight()+")");
				img_out_t = img_in.getSubimage((int)Math.max(0,r.getX()), (int)Math.max(0, r.getY()), (int)Math.min(img_in.getWidth()-r.getX(), r.getWidth()), (int)Math.min(img_in.getHeight()-r.getY(), r.getHeight()));
				try {
					//write image to local temporary file
					File f = new File("tmp\\img\\subimage_"+s.sutra_id+"_"+s.sutra_text.get(i).get(0).get(0).getNumber()+".png");
					ImageIO.write(img_out_t, "png", f);
					//copy image resource to selected collection
					String driver = "org.exist.xmldb.DatabaseImpl";    
					Class cl = Class.forName(driver);  
					Database database = (Database) cl.newInstance();   
					DatabaseManager.registerDatabase(database);
					Collection current = DatabaseManager.getCollection(root.props.getProperty("db.uri")+root.props.getProperty("db.dir.snippet"), root.props.getProperty("db.user"), root.props.getProperty("db.passwd"));
					if(current == null){
						//Collection root = DatabaseManager.getCollection(Preferences.DB_URI, Preferences.DB_USER, Preferences.DB_PASSWD);   
						Collection rootCollection = DatabaseManager.getCollection(root.props.getProperty("db.uri"), root.props.getProperty("db.user"), root.props.getProperty("db.passwd"));
						CollectionManagementService mgtService = (CollectionManagementService) rootCollection.getService("CollectionManagementService", "1.0");   
						//current = mgtService.createCollection(Preferences.DB_COLLECTION_SNIPPET);  
						current = mgtService.createCollection(root.props.getProperty("db.dir.snippet"));
					}
		            BinaryResource resource = (BinaryResource) current.createResource(s.sutra_text.get(i).get(0).get(0).sign_path_snippet.substring(s.sutra_text.get(i).get(0).get(0).sign_path_snippet.lastIndexOf("/")), "BinaryResource");
		            //System.out.println("storing subimage:\t"+f.getName()+"\tas "+s.sutra_text.get(i).get(0).sign_path_snippet.substring(s.sutra_text.get(i).get(0).sign_path_snippet.lastIndexOf("/")));
		            root.addLogEntry("storing subimage:\t"+f.getName()+"\tas "+s.sutra_text.get(i).get(0).get(0).sign_path_snippet.substring(s.sutra_text.get(i).get(0).get(0).sign_path_snippet.lastIndexOf("/")), 1, 1);
		            resource.setContent(f);
		            current.storeResource(resource);
		            
		            //delete temporary file
		            //f.delete();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (XMLDBException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// scale
		main_image.scale = zoomer.getZoom();
		main_image.sub.setPreferredSize(new Dimension((int)(main_image.scale*s.sutra_image.getWidth(main_image)), (int)(main_image.scale*s.sutra_image.getHeight(main_image))));
		main_image.sub.revalidate();
		root.main.main_image.repaint();
	}
	
	public void loadText(){
		//
		root.addLogEntry("*** started loading text ***", 1, 1);
		// simple validation of input
		if(root.explorer.selected == null || root.explorer.selected == "" ||
				root.explorer.selectedCollection == null || root.explorer.selectedCollection == "" ||
				root.explorer.selectedResource == null || root.explorer.selectedResource == ""){
			JOptionPane.showMessageDialog(root, "select a resource form explorer panel first", "Alert!", JOptionPane.ERROR_MESSAGE);
		}
		// get needed properties
		String dbURI = root.props.getProperty("db.uri");
		String dbUser = root.props.getProperty("db.user");
		String dbPass = root.props.getProperty("db.passwd");
		String dbXSLTDir = root.props.getProperty("db.dir.xslt");
		String dbXSLTFile = root.props.getProperty("db.file.xslt");
		
		// 
		String col = root.explorer.selectedCollection;
		String res = root.explorer.selectedResource;
		
		//
		root.text.setBorder(new TitledBorder("text"+" - "+res));
		
		//
		s.sutra_path_file = col + res;
		s.sutra_path_file = s.sutra_path_file.substring(dbURI.length());
		s.sutra_id = res;
		s.sutra_id = s.sutra_id.substring(0, s.sutra_id.length()-".xml".length());	// NOTICE: filename containing sutratext must be of form <SUTRA_ID>.xml
		//perform the transformation and extraction of data
		String xml=new String();
		String xslt=new String();
		String out=new String();
		// 
		xml = XMLUtil.fetchXML(root, dbURI, dbUser, dbPass, col, res);
		if(xml == null || xml == "") JOptionPane.showMessageDialog(root, "XML not fetched properly", "Alert!", JOptionPane.ERROR_MESSAGE);
		// 
		xslt = XMLUtil.fetchXML(root, dbURI, dbUser, dbPass, dbURI+dbXSLTDir, dbXSLTFile);
		if(xslt == null || xslt == "") JOptionPane.showMessageDialog(root, "XSLT not fetched properly", "Alert!", JOptionPane.ERROR_MESSAGE);
		// 
		out = XMLUtil.transformXML(xml, xslt);
		if(out == null || out == "") JOptionPane.showMessageDialog(root, "Bad out after transformation xml->xslt->out", "Alert!", JOptionPane.ERROR_MESSAGE);
		// show extracted text in text-window
		root.text.text_in.setText(XMLUtil.getPlainTextFromXML(out));
		// add information to sutra_text
		s.addText(s.sutra_id, out);
		// repaint
		if(s.updateOnly) root.repaint();
		else root.text.repaint();		
		//
		root.addLogEntry("*** ended loading text ***", 1, 1);
	}
	
	public void loadImage(){
		//
		root.addLogEntry("*** started loading image ***", 1, 1);
		
		// simple validation of input
		if(root.explorer.selected == null || root.explorer.selected == "" ||
				root.explorer.selectedCollection == null || root.explorer.selectedCollection == "" ||
				root.explorer.selectedResource == null || root.explorer.selectedResource == ""){
			JOptionPane.showMessageDialog(root, "select a resource form explorer panel first", "Alert!", JOptionPane.ERROR_MESSAGE);
		}
		// get needed properties
		String dbURI = root.props.getProperty("db.uri");
		
		//
		String col = root.explorer.selectedCollection;
		String res = root.explorer.selectedResource;
		
		// 
		root.main.setBorder(new TitledBorder("main"+" - "+res));
		
		//
		s.sutra_image = ImageUtil.fetchImage(root, dbURI, col, res);
		s.sutra_path_rubbing = col + res;
		s.sutra_path_rubbing = s.sutra_path_rubbing.substring(dbURI.length());
		
		main_image.scale = 1;
		main_image.sub.setPreferredSize(new Dimension(s.sutra_image.getWidth(main_image), s.sutra_image.getHeight(main_image)));
		main_image.sub.revalidate();
		
		// repaint
		root.main.repaint();
		
		//
		root.addLogEntry("*** ended loaded image ***", 1, 1);
	}
	
	public void loadImage(String col, String res){
		//
		root.addLogEntry("*** started loading image ***", 1, 1);
		
		// simple validation of input
		if(root.explorer.selected == null || root.explorer.selected == "" ||
				root.explorer.selectedCollection == null || root.explorer.selectedCollection == "" ||
				root.explorer.selectedResource == null || root.explorer.selectedResource == ""){
			JOptionPane.showMessageDialog(root, "select a resource form explorer panel first", "Alert!", JOptionPane.ERROR_MESSAGE);
		}
		// get needed properties
		String dbURI = root.props.getProperty("db.uri");
		
		// 
		root.main.setBorder(new TitledBorder("main"+" - "+res));
		
		//
		s.sutra_image = ImageUtil.fetchImage(root, dbURI, col, res);
		s.sutra_path_rubbing = col + res;
		s.sutra_path_rubbing = s.sutra_path_rubbing.substring(dbURI.length());
		
		main_image.scale = 1;
		main_image.sub.setPreferredSize(new Dimension(s.sutra_image.getWidth(main_image), s.sutra_image.getHeight(main_image)));
		main_image.sub.revalidate();

		//
		root.addLogEntry("*** ended loaded image ***", 1, 1);
	}
}
