package src.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;

import src.imagecutter.ImageCutter;
import src.model.HiWi_Object_Inscript;
import src.util.file.HiWi_FileIO;
import src.util.xml.XMLUtil;

/**
 * Snippet-Tool menubar component.
 * File -> 
 * 		+ Load Marking- load marking from file stored locally in /tmp/xml
 * 		+ Save Marking - save marking to file stores locally in /tmp/xml
 * 		//+ Preferences
 * 		+ Exit - leave application
 * Edit -> 
 * 		+ Clear Appearances - open HiWi_GUI_clearapp Window
 * 		+ Image Cutter - open ImageCutter Window
 * Help ->
 * 		+ About
 * 		+ Help
 * 
 * @author Alexei Bratuhin
 *
 */
@SuppressWarnings("serial")
public class HiWi_GUI_menubar extends JMenuBar implements ActionListener{
	
	/** Reference to parent component **/
	HiWi_GUI root;
	
	/** Reference to Inscript Object, connected to HiWi_GUI **/
	HiWi_Object_Inscript s;
	
	JMenu m_file = new JMenu("File");
	JMenu m_edit = new JMenu("Edit");
	JMenu m_help = new JMenu("Help");

	JMenuItem mi_loadm = new JMenuItem("Load Marking");
	JMenuItem mi_savem = new JMenuItem("Save Marking");
	JMenuItem mi_loads = new JMenuItem("Load Inscript");
	JMenuItem mi_loadi = new JMenuItem("Load Image");
	JMenuItem mi_pref = new JMenuItem("Preferences");
	JMenuItem mi_exit = new JMenuItem("Exit");
	JMenuItem mi_clear = new JMenuItem("Clear Appearance");
	JMenuItem mi_cutter = new JMenuItem("Image Cutter");
	JMenuItem mi_about = new JMenuItem("About");
	JMenuItem mi_help = new JMenuItem("Help");
	
	
	public HiWi_GUI_menubar(HiWi_GUI r, HiWi_Object_Inscript str){
		super();
		this.root = r;
		this.s = str;
		
		// 
		mi_savem.addActionListener(this);
		mi_loadm.addActionListener(this);
		mi_loadi.addActionListener(this);
		mi_loads.addActionListener(this);
		//mi_pref.addActionListener(this);
		mi_exit.addActionListener(this);
		mi_clear.addActionListener(this);
		mi_cutter.addActionListener(this);
		mi_about.addActionListener(this);
		mi_help.addActionListener(this);
		
		// 
		m_file.add(mi_loads);
		m_file.add(mi_loadi);
		m_file.add(mi_savem);
		m_file.add(mi_loadm);
		//m_file.add(mi_pref);
		m_file.add(mi_exit);
		m_edit.add(mi_clear);
		m_edit.add(mi_cutter);
		m_help.add(mi_about);
		m_help.add(mi_help);
		
		// 
		add(m_file);
		add(m_edit);
		add(m_help);
	}


	@SuppressWarnings("static-access")
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(mi_loads.getActionCommand())){
			JFileChooser fc = new JFileChooser();
			fc.showOpenDialog(root);
			File selectedInscript = fc.getSelectedFile();
			if(selectedInscript != null){
				String localXSLTFile = root.props.getProperty("local.file.xslt");
				//
				root.text.setBorder(new TitledBorder("text"+" - "+selectedInscript.getName()));

				//
				s.inscript_path_file = selectedInscript.getAbsolutePath();
				s.inscript_id = selectedInscript.getName();
				s.inscript_id = s.inscript_id.substring(0, s.inscript_id.length()-".xml".length());	// NOTICE: filename containing sutratext must be of form <SUTRA_ID>.xml

				//perform the transformation and extraction of data
				String xml = new String();
				String xslt = new String();
				String out = new String();
				String out_st = new String();
				// 
				xml = HiWi_FileIO.readXMLStringFromFile(selectedInscript.getAbsolutePath());
				if(xml == null || xml == "") JOptionPane.showMessageDialog(root, "XML not fetched properly", "Alert!", JOptionPane.ERROR_MESSAGE);
				HiWi_FileIO.writeStringToFile("inscript-original.xml", xml);
				//
				xslt = HiWi_FileIO.readXMLStringFromFile(localXSLTFile);
				if(xslt == null || xslt == "") JOptionPane.showMessageDialog(root, "XSLT not fetched properly", "Alert!", JOptionPane.ERROR_MESSAGE);
				// 
				out = XMLUtil.transformXML(xml, xslt);
				if(out == null || out == "") JOptionPane.showMessageDialog(root, "Bad out after transformation xml->xslt->out", "Alert!", JOptionPane.ERROR_MESSAGE);
				HiWi_FileIO.writeStringToFile("inscript-transformed.xml", out);

				// standardize transformed inscript
				out_st = XMLUtil.standardizeXML(out);
				HiWi_FileIO.writeStringToFile("inscript-transformed-standardized.xml", out_st);

				// add information to sutra_text
				s.addText(s.inscript_id, out_st);

				// show extracted text in text-window
				root.text.text_in.setText(XMLUtil.getPlainTextFromApp(s));

				// repaint
				if(s.updateOnly) root.repaint();
				else root.text.repaint();		
				//
				root.addLogEntry("*** ended loading text ***", 1, 1);
			}
		}
		if(e.getActionCommand().equals(mi_loadi.getActionCommand())){
			JFileChooser fc = new JFileChooser();
			fc.showOpenDialog(root);
			File selectedImage = fc.getSelectedFile();
			if(selectedImage != null){
				s.setImage(selectedImage);
			}
			
			root.repaint();
		}
		if(e.getActionCommand().equals(mi_loadm.getActionCommand())){
			s.loadTemp();
		}
		if(e.getActionCommand().equals(mi_savem.getActionCommand())){
			s.saveTemp();
		}
		/*if(e.getActionCommand().equals(mi_pref.getActionCommand())){
			new HiWi_GUI_preferences(root, s);
		}*/
		if(e.getActionCommand().equals(mi_exit.getActionCommand())){
			root.exit();
		}
		if(e.getActionCommand().equals(mi_clear.getActionCommand())){
			new HiWi_GUI_clearapp(root);
		}
		if(e.getActionCommand().equals(mi_cutter.getActionCommand())){
			new ImageCutter(root);
		}
		if(e.getActionCommand().equals(mi_about.getActionCommand())){
			String text = "Zeichen Tool\n" +
							"v.0.99\n" +
							"author: Alexei Bratuhin\n" +
							"produced for: Heidelberger Academy of Science";
			JOptionPane.showMessageDialog(root, text, "About", JOptionPane.INFORMATION_MESSAGE);
		}
		if(e.getActionCommand().equals(mi_help.getActionCommand())){
			String text = "";
			JOptionPane.showMessageDialog(root, text, "Help", JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
