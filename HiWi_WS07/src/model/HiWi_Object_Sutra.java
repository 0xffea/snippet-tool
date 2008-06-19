package src.model;

import java.awt.AlphaComposite;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
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
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
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
import org.xmldb.api.modules.XPathQueryService;

import src.gui.HiWi_GUI;
import src.gui.HiWi_GUI_options;
import src.util.num.NumUtil;
import src.util.prefs.PrefUtil;
import src.util.xml.XMLUtil;

public class HiWi_Object_Sutra {
	//
	HiWi_GUI root;

	//
	public String sutra_id = new String();
	public String sutra_path_rubbing = new String();	//.image of sutra
	public String sutra_path_file = new String();	//.xml of sutra - correspondance between image and HiWi_Object_Signs

	//	
	public BufferedImage sutra_image = null;
	//public ArrayList<HiWi_Object_Sign> sutra_text = new ArrayList<HiWi_Object_Sign>();
	public ArrayList<ArrayList<ArrayList<HiWi_Object_Sign>>> sutra_text = new ArrayList<ArrayList<ArrayList<HiWi_Object_Sign>>>();

	//
	public boolean is_left_to_right = false;
	public boolean showId = true;
	public boolean showNumber = false;
	public boolean showRowColumn = false;
	public boolean updateOnly = false;	//sutra was already loaded in db, now updating appearance
	public int activeSign = -1;	// initialize a default value
	Font f;
	int oa, ob, a, b, da, db; //oa=x_offset; ob=y_offset; a=snippet_width; b=snippet_height; da=x_distance_between_snippets; db=y_distance_between_snippets

	public HiWi_Object_Sutra(HiWi_GUI r){
		this.root = r;
		//loadFont();
	}
	
	

	public void setImage(String img){
		this.sutra_path_rubbing = img;
		try {
			this.sutra_image = ImageIO.read(new File(sutra_path_rubbing));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	@SuppressWarnings("unchecked")
	public void setTextFromXML(String xml){
		// clear text
		sutra_text.clear();
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
				
				// choice, which may possibly mean no choice, but jaust a sign formatted to choice
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
					
					
					// search for variant with maximum stringlength
					int maxlength = 0;
					for(int v=0; v<lvariants.size(); v++){
						if(lvariants.get(v).getChildren().size() > maxlength){
							maxlength = lvariants.get(v).getChildren().size();
						}
					}
					
					// proceed
					for(int j=0; j<maxlength; j++){
						ArrayList<ArrayList<HiWi_Object_Sign>> signVariants = new ArrayList<ArrayList<HiWi_Object_Sign>>();
						ArrayList<HiWi_Object_Sign> signs = new ArrayList<HiWi_Object_Sign>();
						HiWi_Object_Sign csign = new HiWi_Object_Sign();
						
						for(int v=0; v<lvariants.size(); v++){
							signs.clear();
							
							// the first reading in choice schema is preferred
							boolean preferred = (v==0)? true:false;
							// load cert form parent tag choice
							float cert = Float.parseFloat(lvariants.get(v).getAttributeValue("cert"));
							// create variant number
							int variantnumber = v;
							
							if(j < lvariants.get(variantnumber).getChildren().size()){ // if there is a sign with indexed j in this variant
								
								Element cspan = (Element) lvariants.get(v).getChildren().get(j);
								
								if(!cspan.getAttributeValue("class").equals("supplied")){
									String ch = cspan.getText();
									String chOriginal = (cspan.getAttribute("original") != null)? cspan.getAttributeValue("original") : ch;
									
									csign = new HiWi_Object_Sign(this, ch, chOriginal, cert, preferred, variantnumber, current_row, current_column, current_number, new Point(0,0), new Dimension(0,0));

									if(j<basiclength){
										signs.add(csign);
										signVariants.add((ArrayList<HiWi_Object_Sign>) signs.clone());
									}
									else{
										//System.out.println(current_number+"/"+current_row+"/"+current_column);
										sutra_text.get(current_number-1).get(variantnumber).add(csign);
									}

									//System.out.println(tempsign.getInfo()+"sutra text size = "+sutra_text.size());
									root.addLogEntry(csign.getInfo(), 1, 1);
								}
							}
						}
						
						if(signVariants.size()>0 && signVariants.get(0).size()>0){
							// add variants arraylist to sutra text
							sutra_text.add(signVariants);
							//
							if(j == maxlength-1){
								current_column++;
								current_number++;
							}
							else{
								if(j >= basiclength-1){
									
								}
								else{
									current_column++;
									current_number++;
								}
							}
						}
						else{//supplied
							current_column++;
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
			this.sutra_path_rubbing = rub;
			//load image
			String path = dbURI+this.sutra_path_rubbing;
			String collection = path.substring(0, path.lastIndexOf("/"));
			String resource = path.substring(path.lastIndexOf("/"));
			root.main.loadImage(collection, resource);

			// clear sutra_text
			this.sutra_text.clear();

			// get markup
			ArrayList<HiWi_Object_Sign> tarrayOfSigns = new ArrayList<HiWi_Object_Sign>();
			ResourceIterator iterator = query_result.getIterator();
			while(iterator.hasMoreResources()) {  
				Resource res = iterator.nextResource();
				XMLResource xmlres = (XMLResource) res;
				Document d = builder.build(new StringReader((String) xmlres.getContent()));
				Element appearance = d.getRootElement();	// it's appearance tag
				tarrayOfSigns.add(HiWi_Object_Sign.fromAppearance(this, appearance));
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
	
	public void setCoordinatesFromDB(ResourceSet query_result){
		try{
			SAXBuilder builder = new SAXBuilder();
			// get appearances
			ArrayList<HiWi_Object_Sign> tarrayOfSigns = new ArrayList<HiWi_Object_Sign>();
			ResourceIterator iterator = query_result.getIterator();
			while(iterator.hasMoreResources()) {  
				Resource res = iterator.nextResource();
				XMLResource xmlres = (XMLResource) res;
				Document d = builder.build(new StringReader((String) xmlres.getContent()));
				Element appearance = d.getRootElement();	// it's appearance tag
				tarrayOfSigns.add(HiWi_Object_Sign.fromAppearance(this, appearance));
			}
			// use each appearance's number to update coordinates
			for(int i=0; i<tarrayOfSigns.size(); i++){
				HiWi_Object_Sign csign = tarrayOfSigns.get(i);
				Rectangle rectangle = csign.s;
				int indexTarget = csign.number - 1;
				int indexSource = indexTarget;
				this.updateSnippet(indexTarget, indexSource, rectangle);
			}
		} catch(XMLDBException e){
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setImageFromDB(String id, ResourceSet query_result){
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
			this.sutra_path_rubbing = rub;
			//load image
			String path = dbURI+this.sutra_path_rubbing;
			String collection = path.substring(0, path.lastIndexOf("/"));
			String resource = path.substring(path.lastIndexOf("/"));
			root.main.loadImage(collection, resource);
		} catch(XMLDBException e){
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setTextFromArrayList(ArrayList<HiWi_Object_Sign> list){
		// sort tarrayOfSigns after their number
		for(int i=0; i< list.size(); i++){
			for(int j=i; j<list.size(); j++){
				if(list.get(i).number > list.get(j).number){	// handle different signs
					HiWi_Object_Sign tempsign = list.get(i);
					list.set(i, list.get(j));
					list.set(j, tempsign);
				}
				else{
					if(list.get(i).number == list.get(j).number
							&& list.get(i).variant > list.get(j).variant){ // handle variants of the same sign
						HiWi_Object_Sign tempsign = list.get(i);
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
		
		// add sign-variants to sutra's signs
		int lnumber = 0;	// rememebering that numbers start from 1
		int lvariant = 0;
		
		ArrayList<ArrayList<HiWi_Object_Sign>> signVariants = new ArrayList<ArrayList<HiWi_Object_Sign>>();
		ArrayList<HiWi_Object_Sign> signs = new ArrayList<HiWi_Object_Sign>();
		HiWi_Object_Sign csign = new HiWi_Object_Sign();
		
		for(int i=0; i<list.size(); i++){
			csign = list.get(i);
			int cnumber = csign.number;
			int cvariant = csign.variant;
			if(cnumber == lnumber){
				if(cvariant == lvariant){
					signVariants = sutra_text.get(cnumber);
					signs = signVariants.get(cvariant);
					signs.add(csign);
				}
				else{
					signs = new ArrayList<HiWi_Object_Sign>();
					
					signs.add(csign);
					signVariants.add(signs);
				}
			}
			else{
				lvariant = 0;
				
				signVariants = new ArrayList<ArrayList<HiWi_Object_Sign>>();
				signs = new ArrayList<HiWi_Object_Sign>();
				
				signs.add(csign);
				signVariants.add(signs);
				sutra_text.add((ArrayList<ArrayList<HiWi_Object_Sign>>) signVariants.clone());
			}
			
			lnumber = cnumber;				
			lvariant = cvariant;
		}
		
	}
	

	@SuppressWarnings("unchecked")
	public void addText(String id, String xml){
		try {
			// get needed properties
			//String dbURI = root.props.getProperty("db.uri");
			String dbOut = root.props.getProperty("db.dir.out");
			// 
			String query = "//appearance[contains(@id, '"+id+"')]";
			
			root.addLogEntry("query="+query, 0, 1);
			
			String driver = "org.exist.xmldb.DatabaseImpl";  
			Class cl = Class.forName(driver);	           
			Database database = (Database)cl.newInstance();  
			DatabaseManager.registerDatabase(database);

			Collection col = DatabaseManager.getCollection(dbOut);  
			XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");  
			service.setProperty("indent", "yes");

			ResourceSet result = service.query(query);

			if(result.getSize()<1){
				updateOnly = false;
				setTextFromXML(xml);
			}
			else{
				//updateOnly = true;
				updateOnly = false; // experimantal: try to load data from .xml, coordinates from db, delete old appearances on submit
				//setTextFromDB(id, result);
				// experimental
				setTextFromXML(xml);
				setCoordinatesFromDB(result);
				setImageFromDB(id, result);
			}
			
			root.addLogEntry("Inscript size (preferred reading) = "+this.sutra_text.size(), 1, 1);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (XMLDBException e) {
			e.printStackTrace();
		}
	}
	
	public HiWi_Object_Sign getSign(int n, int v){
		return sutra_text.get(n).get(v).get(0);
	}
	
	public void resizeSnippet(HiWi_Object_Sign sn, String dir, int dx, int dy){
		int index = sn.getNumber()-1;	// all variants must be resized
		for(int j=0; j<this.sutra_text.get(index).size(); j++){
			for(int k=0; k<this.sutra_text.get(index).get(j).size(); k++){
				this.sutra_text.get(index).get(j).get(k).resizeSnippet(dir, dx, dy);
			}
		}
	}
	public void moveSnippet(HiWi_Object_Sign sn, int dx, int dy){
		int index = sn.getNumber()-1;	// all variants must be moved
		for(int j=0; j<this.sutra_text.get(index).size(); j++){
			for(int k=0; k<this.sutra_text.get(index).get(j).size(); k++){
				this.sutra_text.get(index).get(j).get(k).moveSnippet( dx, dy);
			}
		}
	}
	public void updateSnippet(int indexTarget, int indexSource, Rectangle rectangle){
		for(int j=0; j<this.sutra_text.get(indexTarget).size(); j++){
			for(int k=0; k<this.sutra_text.get(indexTarget).get(j).size(); k++){
				this.sutra_text.get(indexTarget).get(j).get(k).updateSnippet(rectangle);
			}
		}
	}

	public void setActiveSign(int n){
		this.activeSign = n;
		//System.out.println("Set active sign #"+this.getActiveSign());
		root.addLogEntry("Set active sign #"+this.getActiveSign(), 1, 1);
	}
	public int getActiveSign(){
		return this.activeSign;
	}

	public void loadMarkupSchema(HiWi_GUI_options options){
		// load markup parameters
		oa = Integer.valueOf(options.jtf_oa.getText());
		ob = Integer.valueOf(options.jtf_ob.getText());
		a = Integer.valueOf(options.jtf_a.getText());
		b = Integer.valueOf(options.jtf_b.getText());
		da = Integer.valueOf(options.jtf_da.getText());
		db = Integer.valueOf(options.jtf_db.getText());
		
		// apply parameters
		for(int i=0; i<sutra_text.size(); i++){
			ArrayList<ArrayList<HiWi_Object_Sign>> signvariants = sutra_text.get(i);
			for(int j=0; j<signvariants.size(); j++){
				for(int k=0; k<signvariants.get(j).size(); k++){
					HiWi_Object_Sign csign = signvariants.get(j).get(k);
					int r = csign.getRow()-1;
					int c = csign.getColumn()-1;
					if(is_left_to_right){
						csign.s = new Rectangle(new Point(oa+(a+da)*r, ob+(b+db)*c), new Dimension(a, b));
					}
					else {
						int dim_x = sutra_image.getWidth();
						csign.s = new Rectangle(new Point(dim_x-oa-a-(a+da)*r, ob+(b+db)*c), new Dimension(a, b));
					}
				}
			}
		}
		
		// check whether all markup snippets are seen
		// use preferred reading's signs
		int dim_x = sutra_image.getWidth();
		int dim_y = sutra_image.getHeight();
		int x_width = 0;
		int y_height = 0;
		int max_row = 0;
		int max_column = 0;
		for(int i=0; i<sutra_text.size(); i++){
			if(sutra_text.get(i).get(0).get(0).column > max_column) max_column = sutra_text.get(i).get(0).get(0).column;
			if(sutra_text.get(i).get(0).get(0).row > max_row) max_row = sutra_text.get(i).get(0).get(0).row;
		}
		y_height = oa+(max_column-1)*(a+da)+a;
		x_width = ob+(max_row-1)*(b+db)+b;
		if(x_width > dim_x || y_height > dim_y){	// show warning message
			JOptionPane.showMessageDialog(root, "Some of markup snippet don't pass on screen!\nPlease use \"Full\" button to see whole image", "Alert!", JOptionPane.ERROR_MESSAGE);
		}
		
		// repaint
		root.repaint();
	}

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
	
	public void saveTemp(){
		Document docout = new Document(new Element("sutra"));
		for(int i=0; i<sutra_text.size(); i++){
			for(int j=0; j<sutra_text.get(i).size(); j++){
				for(int k=0; k<sutra_text.get(i).get(j).size(); k++){
					HiWi_Object_Sign csign = sutra_text.get(i).get(j).get(k);
					docout.getRootElement().addContent(csign.toAppearance());
				}
			}
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(new File("tmp\\xml\\temporary_"+this.sutra_id+".xml"));
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
	
	public void loadTemp(){
		try {
			//
			SAXBuilder builder = new SAXBuilder();
			Document docin = builder.build(new FileInputStream(new File("tmp\\xml\\temporary_"+this.sutra_id+".xml")));
			//
			Element docinroot = docin.getRootElement();
			List<Element> apps = docinroot.getChildren("appearance");
			
			// clear sutra_text
			this.sutra_text.clear();

			// get markup
			ArrayList<HiWi_Object_Sign> tarrayOfSigns = new ArrayList<HiWi_Object_Sign>();
			for(int i=0; i<apps.size(); i++) {
				Element xmle = apps.get(i);	// it's appearance tag								
				tarrayOfSigns.add(HiWi_Object_Sign.fromAppearance(this, xmle));
			}
			
			root.addLogEntry("found signs in tempfile: "+tarrayOfSigns.size(), 0, 1);
			
			setTextFromArrayList(tarrayOfSigns);

			root.addLogEntry("Sutra text size: "+sutra_text.size(), 1, 1);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public void submit() {
		// get neede properties
		String dbOut = root.props.getProperty("db.dir.out");
		String dbUser = root.props.getProperty("db.user");
		String dbPass = root.props.getProperty("db.passwd");
		
		// clear old appearances
		// experimental, to use with setTextFromXML, setCoordinatesFromDB, setImageFromDB
		XMLUtil.clearAppearances(root, dbUser, dbPass, dbOut, this.sutra_id);
		
		//proceed for each sign - submit coordinates
		for(int i=0; i<this.sutra_text.size(); i++){
			for(int j=0; j<this.sutra_text.get(i).size(); j++){
				for(int k=0; k<this.sutra_text.get(i).get(j).size(); k++){
					HiWi_Object_Sign csign = this.sutra_text.get(i).get(j).get(k);
					
					root.addLogEntry("storing coordinates of nr.="+csign.number, 1, 1);
					
					XMLUtil.updateXML(root, NumUtil.dec2hex(csign.characterStandard.codePointAt(0)), csign.getXUpdate(this.updateOnly), dbUser, dbPass, dbOut);
				}
			}
		}
		//proceed for each sign - submit snippet
		BufferedImage img_in = this.sutra_image;
		BufferedImage img_out_t;
		for(int i=0; i<this.sutra_text.size(); i++){
			Rectangle2D r = this.sutra_text.get(i).get(0).get(0).s.getBounds2D();
			//System.out.println(i+": ("+(int)r.getX()+","+(int)r.getY()+","+(int)r.getWidth()+","+(int)r.getHeight()+")");
			img_out_t = img_in.getSubimage((int)Math.max(0,r.getX()), (int)Math.max(0, r.getY()), (int)Math.min(img_in.getWidth()-r.getX(), r.getWidth()), (int)Math.min(img_in.getHeight()-r.getY(), r.getHeight()));
			try {
				//write image to local temporary file
				File f = new File("tmp\\img\\subimage_"+this.sutra_id+"_"+this.sutra_text.get(i).get(0).get(0).getNumber()+".png");
				ImageIO.write(img_out_t, "png", f);
				//copy image resource to selected collection
				String driver = "org.exist.xmldb.DatabaseImpl";    
				Class cl = Class.forName(driver);  
				Database database = (Database) cl.newInstance();   
				DatabaseManager.registerDatabase(database);
				Collection current = DatabaseManager.getCollection(root.props.getProperty("db.uri")+root.props.getProperty("db.dir.snippet"), root.props.getProperty("db.user"), root.props.getProperty("db.passwd"));
				if(current == null){
					//Collection root = DatabaseManager.getCollection(Preferencethis.DB_URI, Preferencethis.DB_USER, Preferencethis.DB_PASSWD);   
					Collection rootCollection = DatabaseManager.getCollection(root.props.getProperty("db.uri"), root.props.getProperty("db.user"), root.props.getProperty("db.passwd"));
					CollectionManagementService mgtService = (CollectionManagementService) rootCollection.getService("CollectionManagementService", "1.0");   
					//current = mgtService.createCollection(Preferencethis.DB_COLLECTION_SNIPPET);  
					current = mgtService.createCollection(root.props.getProperty("db.dir.snippet"));
				}
	            BinaryResource resource = (BinaryResource) current.createResource(this.sutra_text.get(i).get(0).get(0).sign_path_snippet.substring(this.sutra_text.get(i).get(0).get(0).sign_path_snippet.lastIndexOf("/")), "BinaryResource");
	            //System.out.println("storing subimage:\t"+f.getName()+"\tas "+this.sutra_text.get(i).get(0).sign_path_snippet.substring(this.sutra_text.get(i).get(0).sign_path_snippet.lastIndexOf("/")));
	            root.addLogEntry("storing subimage:\t"+f.getName()+"\tas "+this.sutra_text.get(i).get(0).get(0).sign_path_snippet.substring(this.sutra_text.get(i).get(0).get(0).sign_path_snippet.lastIndexOf("/")), 1, 1);
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

