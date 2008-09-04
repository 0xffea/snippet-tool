package src.model;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.BinaryResource;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;

import src.gui.HiWi_GUI;
import src.gui.HiWi_GUI_options;
import src.util.db.DbUtil;
import src.util.file.HiWi_FileIO;
import src.util.image.ImageUtil;
import src.util.num.NumUtil;
import src.util.xml.XMLUtil;

/**
 * Inscript class. Stores all inscript relevant attributes from inscript's .xml description and inscript's text structured due reading variants.
 * 
 * @author Alexei Bratuhin
 *
 */
public class HiWi_Object_Inscript {
	
	/** Reference to parent component **/
	HiWi_GUI root;

	/** Inscript's id, e.g., HDS_1**/
	public String inscript_id = new String();
	
	/** Absolute database server path to inscript's image **/
	public String inscript_path_rubbing = new String();
	
	/** Absolute database server path to inscript's .xml description **/
	public String inscript_path_file = new String();

	/** Inscript's image **/
	public BufferedImage inscript_image = null;
	
	/** Inscript's text.
	 * Structure:
	 * 1st index: continuous character numbering;
	 * 2nd index: continuous character's variant numbering;
	 * 3rd index: supplementary index for the case, when not preferred reading contains more characters, as preferred one**/
	public ArrayList<ArrayList<ArrayList<HiWi_Object_Character>>> inscript_text = new ArrayList<ArrayList<ArrayList<HiWi_Object_Character>>>();

	/** Whether text is read left-to-right **/
	public boolean is_left_to_right = false;
	
	/** Whether character should be drawn **/
	public boolean showCharacter = true;
	
	/** Whether character's number should be drawn **/
	public boolean showNumber = false;
	
	/** Whether character's row, column must be drawn **/
	public boolean showRowColumn = false;
	
	/** Whether sutra was already loaded in db, now updating appearance **/
	public boolean updateOnly = false;
	
	/** Currently selected character index **/
	public int activeSign = -1;	// initialize a default value, numbered 0 to size-1
	
	/** Font used to draw characters **/
	Font f;
	
	/** Marking values**/
	
	/** X Offset **/
	int oa;
	
	/** Y Offset **/
	int ob;

	/** Snippet width **/
	int a;
	
	/** Snippet height **/
	int b;
	
	/** X distance between snippets **/
	int da;
	
	/** Y distance between snippets **/
	int db;
	

	public HiWi_Object_Inscript(HiWi_GUI r){
		this.root = r;
	}
	
	
	/**
	 * Load inscript image from local file
	 * @param img	absolute path to image
	 */
	public void setImage(File img){
		this.inscript_path_rubbing = img.getAbsolutePath();
		try {
			this.inscript_image = ImageIO.read(img);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadText(String mode, String col, String res){
		//
		root.addLogEntry("*** started loading text ***", 1, 1);
		
		// get needed properties
		//String dbURI = root.props.getProperty("db.uri");
		//String dbUser = root.props.getProperty("db.user");
		//String dbPass = root.props.getProperty("db.passwd");
		//String localXSLTFile = root.props.getProperty("local.file.xslt");
		
		//
		inscript_path_file = col + res;
		//inscript_path_file = inscript_path_file.substring(dbURI.length());
		inscript_id = res;
		inscript_id = inscript_id.substring(0, inscript_id.length()-".xml".length());	// NOTICE: filename containing sutratext must be of form <SUTRA_ID>.xml
		
		//perform the transformation and extraction of data
		String xml = new String();
		String xslt = new String();
		String out = new String();
		String out_st = new String();
		
		StringWriter xmlWriter = new StringWriter();
		StringWriter xsltWriter = new StringWriter();
		
		// load xml and xslt
		if(mode.equals("local")){
			if(col == null && res == null){
				loadTextLocalSelect(xmlWriter, xsltWriter);
			}
			else{
				loadTextLocal(new File(res), xmlWriter, xsltWriter);
			}
		}
		else if(mode.equals("remote") && col!=null && res!=null){
			loadTextRemote(col, res, xmlWriter, xsltWriter);
		}
		
		xml = xmlWriter.toString();
		xslt = xsltWriter.toString();
		
		// 
		out = XMLUtil.transformXML(xml, xslt);
		if(out == null || out == "") JOptionPane.showMessageDialog(root, "Bad out after transformation xml->xslt->out", "Alert!", JOptionPane.ERROR_MESSAGE);
		
		// standardize transformed inscript
		out_st = XMLUtil.standardizeXML(out);
		
		// save original and transformed xml in application directory
		HiWi_FileIO.writeStringToFile("inscript-original.xml", xml);
		HiWi_FileIO.writeStringToFile("inscript-transformed.xml", out);
		HiWi_FileIO.writeStringToFile("inscript-transformed-standardized.xml", out_st);
		
		// add information to sutra_text
		addText(inscript_id, out_st);
		
		// show extracted text in text-window
		root.text.text_in.setText(XMLUtil.getPlainTextFromApp(this));
		
		// repaint
		if(updateOnly) root.repaint();
		else root.text.repaint();		
		//
		root.addLogEntry("*** ended loading text ***", 1, 1);
	}
	
	public void loadTextRemote(String col, String res, StringWriter xmlWriter, StringWriter xsltWriter){
		// get needed properties
		//String dbURI = root.props.getProperty("db.uri");
		String dbUser = root.props.getProperty("db.user");
		String dbPass = root.props.getProperty("db.passwd");
		String localXSLTFile = root.props.getProperty("local.file.xslt");
		
		// load xml
		xmlWriter.append(XMLUtil.fetchXML(root, dbUser, dbPass, col, res));
		if(xmlWriter.toString() == null || xmlWriter.toString() == "") JOptionPane.showMessageDialog(root, "XML not fetched properly", "Alert!", JOptionPane.ERROR_MESSAGE);
		
		// load xslt
		xsltWriter.append(HiWi_FileIO.readXMLStringFromFile(localXSLTFile));
		if(xsltWriter.toString() == null || xsltWriter.toString() == "") JOptionPane.showMessageDialog(root, "XSLT not fetched properly", "Alert!", JOptionPane.ERROR_MESSAGE);
	}
	
	public void loadTextLocal(File f, StringWriter xmlWriter, StringWriter xsltWriter){
		// get needed properties
		String localXSLTFile = root.props.getProperty("local.file.xslt");
		
		// load xml
		if(f != null){
			xmlWriter.append(HiWi_FileIO.readXMLStringFromFile(f.getAbsolutePath()));
			if(xmlWriter.toString() == null || xmlWriter.toString() == "") JOptionPane.showMessageDialog(root, "XML not fetched properly", "Alert!", JOptionPane.ERROR_MESSAGE);
		}
		
		// load xslt
		xsltWriter.append(HiWi_FileIO.readXMLStringFromFile(localXSLTFile));
		if(xsltWriter.toString() == null || xsltWriter.toString() == "") JOptionPane.showMessageDialog(root, "XSLT not fetched properly", "Alert!", JOptionPane.ERROR_MESSAGE);
	}
	
	public void loadTextLocalSelect(StringWriter xmlWriter, StringWriter xsltWriter){
		// get needed properties
		String localXSLTFile = root.props.getProperty("local.file.xslt");
		
		// load xml
		JFileChooser fc = new JFileChooser();
		fc.showOpenDialog(root);
		File selectedInscript = fc.getSelectedFile();
		if(selectedInscript != null){
			xmlWriter.append(HiWi_FileIO.readXMLStringFromFile(selectedInscript.getAbsolutePath()));
			if(xmlWriter.toString() == null || xmlWriter.toString() == "") JOptionPane.showMessageDialog(root, "XML not fetched properly", "Alert!", JOptionPane.ERROR_MESSAGE);
		}
		
		// load xslt
		xsltWriter.append(HiWi_FileIO.readXMLStringFromFile(localXSLTFile));
		if(xsltWriter.toString() == null || xsltWriter.toString() == "") JOptionPane.showMessageDialog(root, "XSLT not fetched properly", "Alert!", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * 
	 * @param mode		"local"/"remote"
	 * @param col		remote:collection
	 * @param res		remote:resource
	 */
	public void loadImage(String mode, String col, String res){
		if(mode.equals("local")){
			if(col == null && res == null){
				loadImageLocalSelect();
			}
			else{
				loadImageLocal(new File(res));
			}
		}
		else if(mode.equals("remote") && res != null && col != null){
			loadImageRemote(col, res);
		}
	}
	
	/**
	 * 
	 * @param col		remote:collection
	 * @param res		remote:resource
	 */
	public void loadImageRemote(String col, String res){
		//
		root.addLogEntry("*** started loading image ***", 1, 1);
		
		// get needed properties
		//String dbURI = root.props.getProperty("db.uri");
		
		//
		inscript_image = ImageUtil.fetchImage(root, col, res);
		inscript_path_rubbing = col + res;
		//inscript_path_rubbing = inscript_path_rubbing.substring(dbURI.length());
		
		root.main.main_image.scale = 1;
		root.main.main_image.sub.setPreferredSize(new Dimension(inscript_image.getWidth(root.main.main_image), inscript_image.getHeight(root.main.main_image)));
		root.main.main_image.sub.revalidate();

		//
		root.addLogEntry("*** ended loaded image ***", 1, 1);
	}
	
	public void loadImageLocal(File f){
		//
		root.addLogEntry("*** started loading image ***", 1, 1);
		
		//
		File selectedImage = f;
		if(selectedImage != null){
			//
			setImage(selectedImage);
			inscript_path_rubbing = selectedImage.getAbsolutePath();
			
			root.main.main_image.scale = 1;
			root.main.main_image.sub.setPreferredSize(new Dimension(inscript_image.getWidth(root.main.main_image), inscript_image.getHeight(root.main.main_image)));
			root.main.main_image.sub.revalidate();
		}
		
		root.repaint();
		
		//
		root.addLogEntry("*** ended loaded image ***", 1, 1);
	}
	
	/**
	 * 
	 */
	public void loadImageLocalSelect(){
		//
		root.addLogEntry("*** started loading image ***", 1, 1);
		
		//
		JFileChooser fc = new JFileChooser();
		fc.showOpenDialog(root);
		File selectedImage = fc.getSelectedFile();
		if(selectedImage != null){
			//
			setImage(selectedImage);
			inscript_path_rubbing = selectedImage.getAbsolutePath();
			
			root.main.main_image.scale = 1;
			root.main.main_image.sub.setPreferredSize(new Dimension(inscript_image.getWidth(root.main.main_image), inscript_image.getHeight(root.main.main_image)));
			root.main.main_image.sub.revalidate();
		}
		
		root.repaint();
		
		//
		root.addLogEntry("*** ended loaded image ***", 1, 1);
	}

	
	/**
	 * Load text from inscript's .xml description
	 * @param xml	content of inscript's .xml description
	 */
	@SuppressWarnings("unchecked")
	public void setTextFromXML(String xml){
		// clear old text
		inscript_text.clear();
		
		// prepare index variables
		int current_number = 1;
		int current_row = 1;
		int current_column = 1;
		
		// start
		try {
			// parse XML document
			SAXBuilder builder = new SAXBuilder();
			org.jdom.Document doc = builder.build(new InputSource(new StringReader(xml)));
			List<org.jdom.Element> l = doc.getRootElement().getChildren();
			for(int i=0; i<l.size(); i++){
				// line break
				if(l.get(i).getName().equals("br")){
					// remove starting newline if any
					if(i!=0) current_row++; // some texts start with leading linebreak, which needs to be eliminated explicitly
					current_column = 1;	// line break -> start numbering of columns from beginning 
				}
				
				// choice, which may possibly mean no choice, but just a sign formatted to choice
				// to achieve compatibility
				if(l.get(i).getName().equals("choice")){
					//List<org.jdom.Element> lvariants = l.get(i).getChildren("variant");
					List<org.jdom.Element> lvariants = l.get(i).getChildren();
					
					// set the length of preferred reading
					int basicvariant = 0;
					int basiclength = 0;
					float basiccert = 0.0f;
					for(int v=0; v<lvariants.size(); v++){
						float cert = Float.parseFloat(lvariants.get(v).getAttributeValue("cert"));
						if(cert > basiccert){
							basicvariant = v;
							basiclength = lvariants.get(v).getChildren().size();
							basiccert = cert;
						}
					}
					
					// preferred reading should be the first variant
					if(basicvariant!=0){
						Element tvariant = lvariants.get(0);
						lvariants.set(0, lvariants.remove(basicvariant));
						lvariants.add(tvariant);
					}
					
					// if preferred reading is empty, dismiss the whole choice
					if(lvariants.get(0).getChildren().size() == 0){
						continue;
					}
					
					
					// search for variant with maximum stringlength
					int maxlength = 0;
					for(int v=0; v<lvariants.size(); v++){
						if(lvariants.get(v).getChildren().size() > maxlength){
							maxlength = lvariants.get(v).getChildren().size();
						}
					}
					
					
					// proceed basic length
					for(int j=0; j<basiclength; j++){
						ArrayList<ArrayList<HiWi_Object_Character>> signVariants = new ArrayList<ArrayList<HiWi_Object_Character>>();
						ArrayList<HiWi_Object_Character> signs = new ArrayList<HiWi_Object_Character>();
						HiWi_Object_Character csign = new HiWi_Object_Character();
						boolean supplied = false;
						
						for(int v=0; v<lvariants.size(); v++){
							
							if(lvariants.get(v).getChildren().size() == 0){
								continue;
							}
							
							signs.clear();
							
							// the first reading in choice schema is preferred
							boolean preferred = (v==0)? true:false;
							// load cert form parent tag choice
							float cert = Float.parseFloat(lvariants.get(v).getAttributeValue("cert"));
							// create variant number
							int variantnumber = v;
							
							if(j < lvariants.get(variantnumber).getChildren().size()){ // if there is a sign with indexed j in this variant
								
								Element cspan = (Element) lvariants.get(v).getChildren().get(j);
								
								if(cspan == null) System.out.println("NULL Element cspan while proceeding.");
								if(cspan.getAttribute("class") == null) System.out.println("Element doesn't have 'class' attribute;\n"+cspan.toString());
								
								if(cspan.getAttributeValue("class").equals("supplied")){
									supplied = true;
								}
								else{
									String ch = cspan.getText();
									String chOriginal = (cspan.getAttribute("original") != null)? cspan.getAttributeValue("original") : ch;
									
									csign = new HiWi_Object_Character(this, ch, chOriginal, cert, preferred, variantnumber, current_row, current_column, current_number, new Point(0,0), new Dimension(0,0));

									signs.add(csign);
									signVariants.add((ArrayList<HiWi_Object_Character>) signs.clone());

									//System.out.println(tempsign.getInfo()+"sutra text size = "+sutra_text.size());
									root.addLogEntry(csign.getInfo(), 1, 1);
								}
							}
						}
						
						if(!supplied){
							// add variants arraylist to sutra text
							inscript_text.add((ArrayList<ArrayList<HiWi_Object_Character>>) signVariants.clone());
							//
							current_column++;
							current_number++;
						}
						else{
							current_column++;
						}
					}
					
					// proceed extra length
					for(int j = basiclength; j<maxlength; j++){
						HiWi_Object_Character csign = new HiWi_Object_Character();
						int current_number_for_extra = current_number -1; // current_number was already incremented
						int current_column_for_extra = current_column -1;
						
						for(int v=0; v<lvariants.size(); v++){
							
							if(lvariants.get(v).getChildren().size() < basiclength){
								continue;
							}
							
							// the first reading in choice schema is preferred
							boolean preferred = (v==0)? true:false;
							// load cert form parent tag choice
							float cert = Float.parseFloat(lvariants.get(v).getAttributeValue("cert"));
							// create variant number
							int variantnumber = v;
							
							if(j < lvariants.get(variantnumber).getChildren().size()){ // if there is a sign with indexed j in this variant
								
								Element cspan = (Element) lvariants.get(v).getChildren().get(j);
								
								if(cspan == null) System.out.println("NULL Element cspan while proceeding.");
								if(cspan.getAttribute("class") == null) System.out.println("Element doesn't have 'class' attribute;\n"+cspan.toString());
								
								if(cspan.getAttributeValue("class").equals("supplied")){
									
								}
								else{
									String ch = cspan.getText();
									String chOriginal = (cspan.getAttribute("original") != null)? cspan.getAttributeValue("original") : ch;
									
									csign = new HiWi_Object_Character(this, ch, chOriginal, cert, preferred, variantnumber, current_row, current_column_for_extra, current_number_for_extra, new Point(0,0), new Dimension(0,0));

									// no imagesign -> attach it to last placed sign
									// -1, current_number starts with 1, not 0 and in arraylist numbering starts with 0
									inscript_text.get(current_number_for_extra-1).get(variantnumber).add(csign);
									

									//System.out.println(tempsign.getInfo()+"sutra text size = "+sutra_text.size());
									root.addLogEntry(csign.getInfo(), 1, 1);
								}
							}
						}
					}
				}
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load text from inscript's database
	 * @param id			inscript's id
	 * @param query_result	<appearance>s as result of querying database
	 */
	public void setTextFromDB(String id, ResourceSet query_result){
		try{
			// get needed properties
			String dbURI = root.props.getProperty("db.uri");
			// build JDOM document from response
			XMLResource xmlr = (XMLResource) query_result.getResource(0);
			SAXBuilder builder = new SAXBuilder();
			Document dr = builder.build(new StringReader((String) xmlr.getContent()));
			// get/set path to rubbing
			Element xmlelem = (Element) dr.getRootElement();	// it's appearance tag
			String rub = xmlelem.getChildText("rubbing");
			this.inscript_path_rubbing = rub;
			//load image
			String path = dbURI+this.inscript_path_rubbing;
			String collection = path.substring(0, path.lastIndexOf("/"));
			String resource = path.substring(path.lastIndexOf("/"));
			//root.main.loadImage(collection, resource);
			loadImage("remote", collection, resource);

			// clear sutra_text
			this.inscript_text.clear();

			// get markup
			ArrayList<HiWi_Object_Character> tarrayOfSigns = new ArrayList<HiWi_Object_Character>();
			ResourceIterator iterator = query_result.getIterator();
			while(iterator.hasMoreResources()) {  
				Resource res = iterator.nextResource();
				XMLResource xmlres = (XMLResource) res;
				Document d = builder.build(new StringReader((String) xmlres.getContent()));
				Element appearance = d.getRootElement();	// it's appearance tag
				tarrayOfSigns.add(HiWi_Object_Character.fromAppearance(this, appearance));
			}
			
			root.addLogEntry("Found appearances in db: "+tarrayOfSigns.size(), 0, 1);
			
			setTextFromArrayList(tarrayOfSigns);
		} catch(XMLDBException e){
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Update characters' snippet marking coordinates and dimension using results from querying the inscript database
	 * @param query_result	<appearance>s as result of querying database
	 */
	public void setCoordinatesFromDB(ResourceSet query_result){
		try{
			SAXBuilder builder = new SAXBuilder();
			// get appearances
			ArrayList<HiWi_Object_Character> tarrayOfSigns = new ArrayList<HiWi_Object_Character>();
			ResourceIterator iterator = query_result.getIterator();
			while(iterator.hasMoreResources()) {  
				Resource res = iterator.nextResource();
				XMLResource xmlres = (XMLResource) res;
				Document d = builder.build(new StringReader((String) xmlres.getContent()));
				Element appearance = d.getRootElement();	// it's the appearance tag
				HiWi_Object_Character csign = HiWi_Object_Character.fromAppearance(this, appearance);
				tarrayOfSigns.add(csign);
				//System.out.println("fetched sign: "+csign.getInfo());
			}
			
			// use each appearance's number to update coordinates
			for(int i=0; i<tarrayOfSigns.size(); i++){
				HiWi_Object_Character csign = tarrayOfSigns.get(i);
				Rectangle rectangle = csign.s;
				int column = csign.column;
				int row = csign.row;
				this.updateSnippet(rectangle, row, column);
			}
			
			// check, whether all signs have become coordinates assigned, if not, mark those as missing
			for(int i=0; i<inscript_text.size(); i++){
				for(int j=0; j<inscript_text.get(i).size(); j++){
					for(int k=0; k<inscript_text.get(i).get(j).size(); k++){
						HiWi_Object_Character csign = inscript_text.get(i).get(j).get(k);
						if(csign.s.width == 0 || csign.s.height == 0){
							csign.missing = true;
						}
					}
				}
			}
		} catch(XMLDBException e){
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load image from the inscript database
	 * @param id			inscript's id
	 * @param query_result	<appearance>s as result of querying database
	 */
	public void setImageFromDB(String id, ResourceSet query_result){
		try{
			// get needed properties
			//String dbURI = root.props.getProperty("db.uri");
			
			// build JDOM document from response
			XMLResource xmlr = (XMLResource) query_result.getResource(0);
			SAXBuilder builder = new SAXBuilder();
			Document dr = builder.build(new StringReader((String) xmlr.getContent()));
			
			// get/set path to rubbing
			Element xmlelem = (Element) dr.getRootElement();	// it's appearance tag
			String rub = xmlelem.getChildText("rubbing");
			this.inscript_path_rubbing = rub;
			
			//load image
			String path = this.inscript_path_rubbing;
			String collection = path.substring(0, path.lastIndexOf("/"));
			String resource = path.substring(path.lastIndexOf("/"));
			//root.main.loadImage(collection, resource);
			loadImage("remote", collection, resource);
		} catch(XMLDBException e){
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Fill the inscript_text structure from a plain array
	 * @param list	list of character objects
	 */
	@SuppressWarnings("unchecked")
	public void setTextFromArrayList(ArrayList<HiWi_Object_Character> list){
		// sort tarrayOfSigns after their continuous number
		for(int i=0; i< list.size(); i++){
			for(int j=i; j<list.size(); j++){
				if(list.get(i).number > list.get(j).number){	// handle different signs
					HiWi_Object_Character tempsign = list.get(i);
					list.set(i, list.get(j));
					list.set(j, tempsign);
				}
				else{
					if(list.get(i).number == list.get(j).number
							&& list.get(i).variant > list.get(j).variant){ // handle variants of the same sign
						HiWi_Object_Character tempsign = list.get(i);
						list.set(i, list.get(j));
						list.set(j, tempsign);
					}
				}
			}
		}
		for(int i=0; i<list.size(); i++){
			root.addLogEntry(list.get(i).getInfo(), 0, 1);
		}
		
		root.addLogEntry("Found signs in db after sort: "+list.size(), 0, 1);
		
		// add sign-variants to inscript's signs
		int lnumber = 0;	// rememebering that numbers start from 1
		int lvariant = 0;
		
		ArrayList<ArrayList<HiWi_Object_Character>> signVariants = new ArrayList<ArrayList<HiWi_Object_Character>>();
		ArrayList<HiWi_Object_Character> signs = new ArrayList<HiWi_Object_Character>();
		HiWi_Object_Character csign = new HiWi_Object_Character();
		
		for(int i=0; i<list.size(); i++){
			csign = list.get(i);
			int cnumber = csign.number;
			int cvariant = csign.variant;
			if(cnumber == lnumber){
				if(cvariant == lvariant){
					signVariants = inscript_text.get(cnumber);
					signs = signVariants.get(cvariant);
					signs.add(csign);
				}
				else{
					signs = new ArrayList<HiWi_Object_Character>();
					
					signs.add(csign);
					signVariants.add(signs);
				}
			}
			else{
				lvariant = 0;
				
				signVariants = new ArrayList<ArrayList<HiWi_Object_Character>>();
				signs = new ArrayList<HiWi_Object_Character>();
				
				signs.add(csign);
				signVariants.add(signs);
				inscript_text.add((ArrayList<ArrayList<HiWi_Object_Character>>) signVariants.clone());
			}
			
			lnumber = cnumber;				
			lvariant = cvariant;
		}
		
	}
	

	/**
	 * Set Inscript's text either from database or from an .xml file
	 * Notice: Snippet-Tool first tries to load inforamtion from eXist. Only on having received empty response the .xml file is proceeded.
	 * @param id
	 * @param xml
	 */
	@SuppressWarnings("unchecked")
	public void addText(String id, String xml){
		// get needed properties
		//String dbURI = root.props.getProperty("db.uri");
		String dbOut = root.props.getProperty("db.dir.out");
		
		// generate xquery
		String query = "//appearance[contains(@id, '"+id+"_')]";

		ResourceSet result = DbUtil.executeQuery(dbOut, query);

		try {
			if(result.getSize()<1){
				updateOnly = false;
				setTextFromXML(xml);
			}
			else{
				//updateOnly = true;
				updateOnly = false; // experimental: try to load data from .xml, coordinates from db, delete old appearances on submit
				//setTextFromDB(id, result);
				// experimental
				setTextFromXML(xml);
				setCoordinatesFromDB(result);
				setImageFromDB(id, result);
			}
		} catch (XMLDBException e) {
			e.printStackTrace();
		}

		root.addLogEntry("Inscript size (preferred reading) = "+this.inscript_text.size(), 1, 1);

	}
	
	
	public HiWi_Object_Character getCharacter(int n, int v){
		return inscript_text.get(n).get(v).get(0);
	}
	
	/**
	 * Resize character's snippet's marking.
	 * Notice: all corresponding characters (those with the same continuous number) will be automatically resized too.
	 * Notice: generally applies - all variants marking are adjusted using preferred reading's marking.
	 * @param sn		character
	 * @param dir		resize direction
	 * @param dx		x resize
	 * @param dy		y resize
	 */
	public void resizeSnippet(HiWi_Object_Character sn, String dir, int dx, int dy){
		int index = sn.getNumber()-1;	// all variants must be resized
		for(int j=0; j<this.inscript_text.get(index).size(); j++){
			for(int k=0; k<this.inscript_text.get(index).get(j).size(); k++){
				this.inscript_text.get(index).get(j).get(k).resizeSnippet(dir, dx, dy);
			}
		}
	}
	
	/**
	 * Move character's snippet's marking.
	 * Notice: all corresponding characters (those with the same continuous number) will be automatically resized too.
	 * Notice: generally applies - all variants marking are adjusted using preferred reading's marking.
	 * @param sn		character
	 * @param dx		x shift
	 * @param dy		y shift
	 */
	public void moveSnippet(HiWi_Object_Character sn, int dx, int dy){
		int index = sn.getNumber()-1;	// all variants must be moved
		for(int j=0; j<this.inscript_text.get(index).size(); j++){
			for(int k=0; k<this.inscript_text.get(index).get(j).size(); k++){
				this.inscript_text.get(index).get(j).get(k).moveSnippet( dx, dy);
			}
		}
	}
	
	/**
	 * Update character's snippet's marking
	 * Notice. all corresponding characters (those with the same continuous number) will be automatically updated too
	 * Notice: generally applies - all variants marking are adjusted using preferred reading's marking.
	 * @param rectangle		new snippet marking
	 * @param r				character's row number
	 * @param c				character's column number
	 */
	public void updateSnippet(Rectangle rectangle, int r, int c){
		int indexTarget = -1;
		// find sign using row,column signature and binary search for row
		/*int leftIndex = 0;
		int rightIndex = this.sutra_text.size() - 1;
		int crow = -1;
		while(crow != r){
			int middleIndex = (int)((rightIndex-leftIndex)/2.0f);
			if(this.sutra_text.get(middleIndex).get(0).get(0).row > crow){
				rightIndex = middleIndex + 1;
			}
			else{
				leftIndex = middleIndex;
			}
		}
		for(int i=leftIndex; i<rightIndex; i++){
			if(this.sutra_text.get(i).get(0).get(0).column == c){
				indexTarget = i;
				break;
			}
		}*/
		for(int i=0; i<this.inscript_text.size(); i++){
			if(this.inscript_text.get(i).get(0).get(0).row == r &&
					this.inscript_text.get(i).get(0).get(0).column == c){
				indexTarget = i;
				break;
			}
		}
		
		// check whether indexTarget found
		if(indexTarget == -1){
			root.addLogEntry("Couldn't find target sign for setting coordinates from DB for:\trow="+r+", column="+c, 1, 1);
			
			return;
		}
		
		// update sign
		updateSnippet(rectangle, indexTarget);
	}
	
	public void updateSnippet(Rectangle rectangle, int indexTarget){
		// update sign
		for(int j=0; j<this.inscript_text.get(indexTarget).size(); j++){
			for(int k=0; k<this.inscript_text.get(indexTarget).get(j).size(); k++){
				this.inscript_text.get(indexTarget).get(j).get(k).updateSnippet(rectangle);
			}
		}
	}
	
	public void setActiveNumber(int n){
		this.activeSign = n;
	}
	
	public int getActiveNumber(){
		return this.activeSign;
	}
	
	public void setActiveCharacterNumber(int n){
		this.activeSign = n-1;
		root.addLogEntry("Set active character #"+this.getActiveSignNumber(), 1, 1);
	}
	public int getActiveSignNumber(){
		return this.activeSign+1;
	}
	
	public HiWi_Object_Character getActiveCharacter(){
		if(activeSign == -1) return null;
		return this.inscript_text.get(this.getActiveNumber()).get(0).get(0);
	}
	
	/**
	 * Load values for autoguided marking from Snippet-tool's Options component.
	 * @param options	Snippet-Tool options component
	 * @param missingOnly	whether marking schema should be applied for characters not already in database only
	 */
	public void loadMarkingSchema(HiWi_GUI_options options, boolean missingOnly){
		// load markup parameters
		oa = Integer.valueOf(options.jtf_oa.getText());
		ob = Integer.valueOf(options.jtf_ob.getText());
		a = Integer.valueOf(options.jtf_a.getText());
		b = Integer.valueOf(options.jtf_b.getText());
		da = Integer.valueOf(options.jtf_da.getText());
		db = Integer.valueOf(options.jtf_db.getText());
		
		// check whether all markup snippets are seen
		// use preferred reading's signs
		int dim_x = inscript_image.getWidth();
		int dim_y = inscript_image.getHeight();
		int x_width = 0;
		int y_height = 0;
		int max_row = 0;
		int max_column = 0;
		for(int i=0; i<inscript_text.size(); i++){
			if(inscript_text.get(i).get(0).get(0).column > max_column) max_column = inscript_text.get(i).get(0).get(0).column;
			if(inscript_text.get(i).get(0).get(0).row > max_row) max_row = inscript_text.get(i).get(0).get(0).row;
		}
		x_width = oa+(max_row-1)*(a+da);
		y_height = ob+(max_column-1)*(b+db);
		if(x_width > dim_x || y_height > dim_y){	// show warning message
			JOptionPane.showMessageDialog(root, "Some of markup snippet don't pass on image!\nPlease use \"Full\" button to see whole image", "Alert!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// apply parameters if check passed
		for(int i=0; i<inscript_text.size(); i++){
			ArrayList<ArrayList<HiWi_Object_Character>> signvariants = inscript_text.get(i);
			for(int j=0; j<signvariants.size(); j++){
				for(int k=0; k<signvariants.get(j).size(); k++){
					HiWi_Object_Character csign = signvariants.get(j).get(k);
					
					// if dimension greater 0, sign already has coordinates assigned -> no need to generate
					if(missingOnly && csign.missing){
						continue;
					}
					else{
						int r = csign.getRow()-1;
						int c = csign.getColumn()-1;
						if(is_left_to_right){
							csign.s = new Rectangle(new Point(oa+(a+da)*r, ob+(b+db)*c), new Dimension(a, b));
						}
						else {
							csign.s = new Rectangle(new Point(dim_x-oa-a-(a+da)*r, ob+(b+db)*c), new Dimension(a, b));
						} 
					 }
				}
			}
		}
		
				
		// repaint
		root.repaint();
	}
	
	/**
	 * Load Font used for drawing of character from local file.
	 * Notice: font file, specified in snippet-tool.properties must be present
	 */
	public void loadFont(){
		try {
			// get needed properties
			String localFont = root.props.getProperty("local.font");
			// load font
			FileInputStream fontStream = new FileInputStream(localFont);
			f = java.awt.Font.createFont( java.awt.Font.TRUETYPE_FONT, fontStream  );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Save insript's marking to local file.
	 * Structure:
	 * <inscript>
	 * 	<appearance/>
	 * 	<appearance/>
	 * 	...
	 * </inscript>
	 */
	public void saveTempMarking(){
		Document docout = new Document(new Element("inscript")
										.setAttribute("id", inscript_id)
										.setAttribute("xml", inscript_path_file)
										.setAttribute("img", inscript_path_rubbing));
		for(int i=0; i<inscript_text.size(); i++){
			for(int j=0; j<inscript_text.get(i).size(); j++){
				for(int k=0; k<inscript_text.get(i).get(j).size(); k++){
					HiWi_Object_Character csign = inscript_text.get(i).get(j).get(k);
					docout.getRootElement().addContent(csign.toAppearance());
				}
			}
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(new File("tmp\\xml\\temporary_"+this.inscript_id+".xml"));
			XMLOutputter xmlout = new XMLOutputter();
			xmlout.setFormat(Format.getPrettyFormat());
			xmlout.output(docout, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load inscript's marking from local file.
	 * Notice: since it uses inscript's id, an inscript (and its image) must be already loaded in Snippet-Tool application
	 */
	@SuppressWarnings("unchecked")
	public void loadTempMarking(){
		try {
			//
			JFileChooser fc = new JFileChooser();
			fc.showOpenDialog(root);
			File selectedMarking = fc.getSelectedFile();
			//
			SAXBuilder builder = new SAXBuilder();
			Document docin = builder.build(new FileInputStream(selectedMarking));
			//
			Element docinroot = docin.getRootElement();
			//String id = docinroot.getAttributeValue("id");
			String xml = docinroot.getAttributeValue("xml");
			String img = docinroot.getAttributeValue("img");
			
			String xml_collection = null;
			String xml_resource = xml;
			String xml_method = "local";
			String img_collection = null;
			String img_resource = img;
			String img_method = "local";
			
			if(xml.startsWith("xmldb:")){
				xml_collection = xml.substring(0, xml.lastIndexOf("/"));
				xml_resource = xml.substring(xml.lastIndexOf("/")+1);
				xml_method = "remote";
			}
			
			if(img.startsWith("xmldb:")){
				img_collection = img.substring(0, img.lastIndexOf("/"));
				img_resource = img.substring(img.lastIndexOf("/")+1);
				img_method = "remote";
			}
			
			loadText(xml_method, xml_collection, xml_resource);
			loadImage(img_method, img_collection, img_resource);
			
			List<Element> apps = docinroot.getChildren("appearance");
			
			// clear sutra_text
			this.inscript_text.clear();

			// get markup
			ArrayList<HiWi_Object_Character> tarrayOfSigns = new ArrayList<HiWi_Object_Character>();
			for(int i=0; i<apps.size(); i++) {
				Element xmle = apps.get(i);	// it's appearance tag								
				tarrayOfSigns.add(HiWi_Object_Character.fromAppearance(this, xmle));
			}
			
			root.addLogEntry("found signs in tempfile: "+tarrayOfSigns.size(), 0, 1);
			
			setTextFromArrayList(tarrayOfSigns);

			root.addLogEntry("Sutra text size: "+inscript_text.size(), 1, 1);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * clear inscript's information
	 */
	public void clear(){
		inscript_image = null;
		inscript_text.clear();
		inscript_id = new String();
		inscript_path_file = new String();
		inscript_path_rubbing = new String();
		
		activeSign = -1;
	}
	
	/**
	 * Submit inscript's marking to database
	 */
	@SuppressWarnings("unchecked")
	public void submit() {
		// get neede properties
		String dbOut = root.props.getProperty("db.dir.out");
		String dbUser = root.props.getProperty("db.user");
		String dbPass = root.props.getProperty("db.passwd");
		
		// clear old appearances
		// experimental, to use with setTextFromXML, setCoordinatesFromDB, setImageFromDB
		XMLUtil.clearAppearances(root, dbUser, dbPass, dbOut, this.inscript_id);
		
		//proceed for each sign - submit coordinates
		for(int i=0; i<this.inscript_text.size(); i++){
			for(int j=0; j<this.inscript_text.get(i).size(); j++){
				for(int k=0; k<this.inscript_text.get(i).get(j).size(); k++){
					HiWi_Object_Character csign = this.inscript_text.get(i).get(j).get(k);
					
					root.addLogEntry("storing coordinates of nr.="+csign.number, 1, 1);
					
					XMLUtil.updateXML(root, NumUtil.dec2hex(csign.characterStandard.codePointAt(0)), csign.getXUpdate(this.updateOnly), dbUser, dbPass, dbOut);
				}
			}
		}
		//proceed for each sign - submit snippet
		BufferedImage img_in = this.inscript_image;
		BufferedImage img_out_t;
		for(int i=0; i<this.inscript_text.size(); i++){
			Rectangle2D r = this.inscript_text.get(i).get(0).get(0).s.getBounds2D();
			img_out_t = img_in.getSubimage((int)Math.max(0,r.getX()), (int)Math.max(0, r.getY()), (int)Math.min(img_in.getWidth()-r.getX(), r.getWidth()), (int)Math.min(img_in.getHeight()-r.getY(), r.getHeight()));
			try {
				//write image to local temporary file
				File f = new File("tmp\\img\\subimage_"+this.inscript_id+"_"+this.inscript_text.get(i).get(0).get(0).getNumber()+".png");
				ImageIO.write(img_out_t, "png", f);
				
				//copy image resource to selected collection
				String driver = "org.exist.xmldb.DatabaseImpl";    
				Class cl = Class.forName(driver);  
				Database database = (Database) cl.newInstance();   
				DatabaseManager.registerDatabase(database);
				Collection current = DatabaseManager.getCollection(root.props.getProperty("db.uri")+root.props.getProperty("db.dir.snippet"), root.props.getProperty("db.user"), root.props.getProperty("db.passwd"));
				if(current == null){   
					Collection rootCollection = DatabaseManager.getCollection(root.props.getProperty("db.uri"), root.props.getProperty("db.user"), root.props.getProperty("db.passwd"));
					CollectionManagementService mgtService = (CollectionManagementService) rootCollection.getService("CollectionManagementService", "1.0");
					current = mgtService.createCollection(root.props.getProperty("db.dir.snippet"));
				}
	            BinaryResource resource = (BinaryResource) current.createResource(this.inscript_text.get(i).get(0).get(0).sign_path_snippet.substring(this.inscript_text.get(i).get(0).get(0).sign_path_snippet.lastIndexOf("/")), "BinaryResource");
	            root.addLogEntry("storing subimage:\t"+f.getName()+"\tas "+this.inscript_text.get(i).get(0).get(0).sign_path_snippet.substring(this.inscript_text.get(i).get(0).get(0).sign_path_snippet.lastIndexOf("/")), 1, 1);
	            resource.setContent(f);
	            current.storeResource(resource);
	            
	            //delete temporary file
	            f.delete();
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

