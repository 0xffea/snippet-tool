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
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;

import src.gui.HiWi_GUI;
import src.gui.HiWi_GUI_options;
import src.util.num.NumUtil;
import src.util.prefs.PrefUtil;

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
		// get information needed for generating default markup
		//int dim_x = sutra_image.getWidth();
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
				// usual sign
				if(l.get(i).getName().equals("span")){
					ArrayList<ArrayList<HiWi_Object_Sign>> signVariants = new ArrayList<ArrayList<HiWi_Object_Sign>>();
					ArrayList<HiWi_Object_Sign> signs = new ArrayList<HiWi_Object_Sign>();
					HiWi_Object_Sign sign = new HiWi_Object_Sign();

					if(!l.get(i).getAttributeValue("class").equals("supplied")){
						String ch = l.get(i).getText();
						boolean preferred = true;
						float cert = 1.0f;
						int var = 0;
						sign = new HiWi_Object_Sign(this, ch, cert, preferred, var, current_row, current_column, current_number, new Point(0,0), new Dimension(0,0));

						signs.add(sign);
						signVariants.add(signs);
						sutra_text.add(signVariants);
						//
						current_column++;
						current_number++;

						//System.out.println(sign.getInfo()+"sutra text size = "+sutra_text.size());
						root.addLogEntry(sign.getInfo()+"sutra text size = "+sutra_text.size(), 1, 1);
					}
					else{ // do not include supplied characters in db
						current_column++;
					}
				}
				
				// normalized sign
				if(l.get(i).getName().equals("norm")){
					ArrayList<ArrayList<HiWi_Object_Sign>> signVariants = new ArrayList<ArrayList<HiWi_Object_Sign>>();
					ArrayList<HiWi_Object_Sign> signs = new ArrayList<HiWi_Object_Sign>();
					HiWi_Object_Sign sign = new HiWi_Object_Sign();

					if(!((Element) l.get(i).getChildren().get(0)).getAttributeValue("class").equals("supplied")){
						String ch = ((Element) l.get(i).getChildren().get(0)).getText();
						boolean preferred = true;
						float cert = 1.0f;
						int var = 0;
						sign = new HiWi_Object_Sign(this, ch, cert, preferred, var, current_row, current_column, current_number, new Point(0,0), new Dimension(0,0));

						signs.add(sign);
						signVariants.add(signs);
						sutra_text.add(signVariants);
						//
						current_column++;
						current_number++;

						//System.out.println(sign.getInfo()+"sutra text size = "+sutra_text.size());
						root.addLogEntry(sign.getInfo()+"sutra text size = "+sutra_text.size(), 1, 1);
					}
					else{ // do not include supplied characters in db
						current_column++;
					}
				}
				
				// handle lem/rdg tags from app
				if(l.get(i).getName().equals("app")){
					// length of app'ed char sequence
					int length = 0;
					
					// get all lem and rdg tags					
					List<org.jdom.Element> lvariants = l.get(i).getChildren();
					for(int q=0; q<lvariants.size(); q++){
						if(lvariants.get(q).getName().equals("lem")){	// assuming lem is preferred over rdg
							length = lvariants.get(q).getChildren().size();
						}
					}

					for(int q=0; q<length; q++){
						ArrayList<ArrayList<HiWi_Object_Sign>> signVariants = new ArrayList<ArrayList<HiWi_Object_Sign>>();
						ArrayList<HiWi_Object_Sign> signs = new ArrayList<HiWi_Object_Sign>();
						HiWi_Object_Sign sign = new HiWi_Object_Sign();
						
						for(int p=0; p<lvariants.size(); p++){
							signs.clear();
							
							boolean preferred = false;
							if (p == 0) preferred = true;
							// 
							float cert = 1.0f;
							int variantnumber = p;
							Element tempspan = (Element) lvariants.get(p).getChildren().get(q);
							if(tempspan!=null){
								if(!tempspan.getAttributeValue("class").equals("supplied")){
									String ch = tempspan.getText();
									sign = new HiWi_Object_Sign(this, ch, cert, preferred, variantnumber, current_row, current_column, current_number, new Point(0,0), new Dimension(0,0));

									signs.add(sign);
									signVariants.add((ArrayList<HiWi_Object_Sign>) signs.clone());

									//System.out.println(tempsign.getInfo()+"sutra text size = "+sutra_text.size());
									root.addLogEntry(sign.getInfo()+"sutra text size = "+sutra_text.size(), 1, 1);
								}
							}
						}
						// add variants to sutra text
						sutra_text.add(signVariants);
						
						//
						current_column++;
						current_number++;
					}
				}
				
				//TODO: set length of the reading, using the preferred standard reading,
				//TODO: map rest of signs to last sign in standard reading
				// there are several readings
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
					
					// search for variant with maximum stringlength
					int maxlength = 0;
					for(int v=0; v<lvariants.size(); v++){
						if(lvariants.get(v).getChildren().size() > maxlength){
							maxlength = lvariants.get(v).getChildren().size();
						}
					}
					
					//
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
									csign = new HiWi_Object_Sign(this, ch, cert, preferred, variantnumber, current_row, current_column, current_number, new Point(0,0), new Dimension(0,0));

									if(j<basiclength){
										signs.add(csign);
										signVariants.add((ArrayList<HiWi_Object_Sign>) signs.clone());
									}
									else{
										//System.out.println(current_number+"/"+current_row+"/"+current_column);
										sutra_text.get(current_number-1).get(variantnumber).add(csign);
									}

									//System.out.println(tempsign.getInfo()+"sutra text size = "+sutra_text.size());
									root.addLogEntry(csign.getInfo()+"sutra text size = "+sutra_text.size(), 1, 1);
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
			//load image
			XMLResource xmlr = (XMLResource) query_result.getResource(0);
			SAXBuilder builder = new SAXBuilder();
			Document dr = builder.build(new StringReader((String) xmlr.getContent()));
			Element xmlelem = (Element) dr.getRootElement();	// it's appearance tag
			String rub = xmlelem.getChildText("rubbing");
			this.sutra_path_rubbing = rub;
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
				Element xmle = d.getRootElement();	// it's appearance tag
				String ch = xmle.getAttributeValue("character");
				String signid = xmle.getAttributeValue("id");
				boolean preferred = Boolean.parseBoolean(xmle.getAttributeValue("preferred_reading"));
				float cert = Float.parseFloat(xmle.getAttributeValue("cert"));
				int var = Integer.parseInt(xmle.getAttributeValue("variant"));
				String rc = signid.substring((id+"_").length());
				int r = Integer.parseInt(rc.substring(0, rc.indexOf("_")));
				int c = Integer.parseInt(rc.substring(rc.indexOf("_")+1));
				int n = Integer.parseInt(xmle.getAttributeValue("nr").substring((id+"_").length()));
				xmle = d.getRootElement().getChild("coordinates");
				int x = Integer.parseInt(xmle.getAttributeValue("x"));
				int y = Integer.parseInt(xmle.getAttributeValue("y"));
				int width = Integer.parseInt(xmle.getAttributeValue("width"));
				int height = Integer.parseInt(xmle.getAttributeValue("height"));

				HiWi_Object_Sign sign = new HiWi_Object_Sign(this, ch, cert, preferred, var, r, c, n, new Point(x,y), new Dimension(width, height));				
				sign.id = signid; // just to be sure, since it's the only value not mentioned in constructor explicit
				
				tarrayOfSigns.add(sign);
				//System.out.println(sign.getInfo());
			}
			
			root.addLogEntry("found signs in db: "+tarrayOfSigns.size(), 0, 1);
			
			setTextFromArrayList(tarrayOfSigns);
			
			root.addLogEntry("Sutra text size: "+sutra_text.size(), 1, 1);
			
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
	

	public void addText(String id, String xml){
		try {
			// get needed properties
			String dbURI = root.props.getProperty("db.uri");
			String dbOut = root.props.getProperty("db.dir.out");
			//
			/*String query = "for $doc in collection('"+dbOut+"?select=*.xml')" +
							"let $d := $doc " +
							"for $appearance in $d/unihandb/char/appearance "+
							"where $appearance/source='"+id+"' " +
							" return $appearance";*/
			String query = "//unihandb/char/appearance[contains(@id, '"+id+"')]";
			
			root.addLogEntry("query="+query, 0, 1);
			
			String driver = "org.exist.xmldb.DatabaseImpl";  
			Class cl = Class.forName(driver);	           
			Database database = (Database)cl.newInstance();  
			DatabaseManager.registerDatabase(database);  

			Collection col = DatabaseManager.getCollection(dbURI);  
			XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");  
			service.setProperty("indent", "yes");

			ResourceSet result = service.query(query);

			if(result.getSize()<1){
				//System.out.println("updateOnly=false");
				updateOnly = false;
				setTextFromXML(xml);
			}
			else{
				//System.out.println("updateOnly=true");
				updateOnly = true;
				setTextFromDB(id, result);
			}

			// repaint
			//root.repaint();

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
					Element app = new Element("appearance");
					app.setAttribute("character", csign.character);
					app.setAttribute("id", csign.id);
					app.setAttribute("preferred_reading", String.valueOf(csign.preferred_reading));
					app.setAttribute("variant", String.valueOf(csign.variant));
					app.setAttribute("cert", String.valueOf(csign.cert));
					app.setAttribute("nr", String.valueOf(sutra_id+"_"+csign.number));

					Element source = new Element("source");
					source.setText(sutra_id);

					Element rubbing = new Element("rubbing");
					rubbing.setText(sutra_path_rubbing);

					Element graphic = new Element("graphic");
					graphic.setText(csign.sign_path_snippet);

					Element coordinates = new Element("coordinates");
					coordinates.setAttribute("x", String.valueOf(csign.s.x));
					coordinates.setAttribute("y", String.valueOf(csign.s.y));
					coordinates.setAttribute("height", String.valueOf(csign.s.height));
					coordinates.setAttribute("width", String.valueOf(csign.s.width));

					app.addContent(source);
					app.addContent(rubbing);
					app.addContent(graphic);
					app.addContent(coordinates);

					docout.getRootElement().addContent(app);
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
				String ch = xmle.getAttributeValue("character");
				String signid = xmle.getAttributeValue("id");
				boolean preferred = Boolean.parseBoolean(xmle.getAttributeValue("preferred_reading"));
				float cert = Float.parseFloat(xmle.getAttributeValue("cert"));
				int var = Integer.parseInt(xmle.getAttributeValue("variant"));
				String rc = signid.substring((this.sutra_id+"_").length());
				int r = Integer.parseInt(rc.substring(0, rc.indexOf("_")));
				int c = Integer.parseInt(rc.substring(rc.indexOf("_")+1));
				int n = Integer.parseInt(xmle.getAttributeValue("nr").substring((this.sutra_id+"_").length()));
				Element xmlc = xmle.getChild("coordinates");
				int x = Integer.parseInt(xmlc.getAttributeValue("x"));
				int y = Integer.parseInt(xmlc.getAttributeValue("y"));
				int width = Integer.parseInt(xmlc.getAttributeValue("width"));
				int height = Integer.parseInt(xmlc.getAttributeValue("height"));

				HiWi_Object_Sign sign = new HiWi_Object_Sign(this, ch, cert, preferred, var, r, c, n, new Point(x,y), new Dimension(width, height));				
				sign.id = signid; // just to be sure, since it's the only value not mentioned in constructor explicit
				
				tarrayOfSigns.add(sign);
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
	
	
	

	/**
	 * 
	 */

	public class HiWi_Object_Sign {
		//
		public HiWi_Object_Sutra sutra;
		public String sign_path_snippet = new String();

		//
		public Rectangle s;			//marked area on sutra's .jpg

		//
		public String character = new String();					// unicode sign

		//
		public String id;	// id, e.g. HDS_1_2_1
		public int row;	// numeration starts with 1 because of compatibility with existing scripts and numeration
		public int column;	// numeration starts with 1 because of compatibility with existing scripts and numeration
		public int number;	// numeration starts with 1 because of compatibility with existing scripts and numeration
		public float cert;	// certainty -> max=1.0, min=0.0
		boolean preferred_reading;	// whether this reading is preferred by heidelberger academy of science
		int groupnumber;
		int variant;	// reading variant, numeration starts with 0, no compatibilitiy needed (yet?)

		//
		public String getInfo(){
			String out = new String();
			out += "id="+id+",";
			out += "ch="+character+"/"+character.codePointAt(0)+",";
			out += "number="+getNumber()+",";
			out += "cert="+cert+",";
			out += "variant="+variant+",";
			out += "preferred_reading="+preferred_reading+",";
			out += "row="+getRow()+",";
			out += "column="+getColumn()+",";
			out += "point=("+s.x+","+s.y+")"+",";
			out += "dimension=("+s.width+"x"+s.height+")"+",";
			out += ";";
			return out;
		}

		//
		public HiWi_Object_Sign(){

		}
		public HiWi_Object_Sign(HiWi_Object_Sutra s, String ch, int r, int c, int n){
			// get needed properties
			String dbSnips = root.props.getProperty("db.dir.snippet");
			//
			this.sutra = s;
			this.character = ch;
			this.setColumn(c);
			this.setRow(r);
			this.setNumber(n);
			this.id = this.sutra.sutra_id+"_"+this.row+"_"+this.column;
			this.sign_path_snippet = dbSnips+"/subimage_"+sutra_id+"_"+r+"_"+c+".png";
		}
		public HiWi_Object_Sign(HiWi_Object_Sutra s, String ch, float cert, boolean preferred, int var, int r, int c, int n){
			// get needed properties
			String dbSnips = root.props.getProperty("db.dir.snippet");
			//
			this.sutra = s;
			this.character = ch;
			this.cert = cert;
			this.preferred_reading = preferred;
			this.variant = var;
			this.setColumn(c);
			this.setRow(r);
			this.setNumber(n);
			this.id = this.sutra.sutra_id+"_"+this.row+"_"+this.column;
			this.sign_path_snippet = dbSnips+"/subimage_"+sutra_id+"_"+r+"_"+c+".png";
		}
		public HiWi_Object_Sign(HiWi_Object_Sutra s, String ch, int r, int c, int n, Point base, Dimension delta){
			// get needed properties
			String dbSnips = root.props.getProperty("db.dir.snippet");
			//
			this.sutra = s;
			this.character = ch;
			this.setColumn(c);
			this.setRow(r);
			this.setNumber(n);
			this.id = this.sutra.sutra_id+"_"+this.row+"_"+this.column;
			this.sign_path_snippet = dbSnips+"/subimage_"+sutra_id+"_"+r+"_"+c+".png";
			// 
			this.s = new Rectangle(base, delta);
		}
		public HiWi_Object_Sign(HiWi_Object_Sutra s, String ch, float cert, boolean preferred, int var, int r, int c, int n, Point base, Dimension delta){
			// get needed properties
			String dbSnips = root.props.getProperty("db.dir.snippet");
			//
			this.sutra = s;
			this.character = ch;
			this.cert = cert;
			this.preferred_reading = preferred;
			this.variant = var;
			this.setColumn(c);
			this.setRow(r);
			this.setNumber(n);
			this.id = this.sutra.sutra_id+"_"+this.row+"_"+this.column;
			this.sign_path_snippet = dbSnips+"/subimage_"+sutra_id+"_"+r+"_"+c+".png";
			// 
			this.s = new Rectangle(base, delta);
		}
		
		public void setNumber(int n){
			this.number = n;
		}
		public int getNumber(){
			return this.number;
		}
		public void setColumn(int c){
			this.column = c;
		}
		public int getColumn(){
			return this.column;
		}
		public void setRow(int r){
			this.row = r;
		}
		public int getRow(){
			return this.row;
		}

		//
		public void resizeSnippet(String direction, int dx, int dy){
			if(direction == null) return;
			if(direction.equals("nw")){s.setBounds(s.x+dx, s.y+dy, s.width-dx, s.height-dy);return;}
			if(direction.equals("n")){s.setBounds(s.x, s.y+dy, s.width, s.height-dy);return;}
			if(direction.equals("ne")){s.setBounds(s.x, s.y+dy, s.width+dx, s.height-dy);return;}
			if(direction.equals("e")){s.setBounds(s.x, s.y, s.width+dx, s.height);return;}
			if(direction.equals("se")){s.setBounds(s.x, s.y, s.width+dx, s.height+dy);return;}
			if(direction.equals("s")){s.setBounds(s.x, s.y, s.width, s.height+dy);return;}
			if(direction.equals("sw")){s.setBounds(s.x+dx, s.y, s.width-dx, s.height+dy);return;}
			if(direction.equals("w")){s.setBounds(s.x+dx, s.y, s.width-dx, s.height);return;}
		}
		public void moveSnippet(int dx, int dy){
			s.setLocation(s.x+dx, s.y+dy);
		}
		public String computeMoveDirection(Cursor c){
			if(c.getType() == Cursor.NW_RESIZE_CURSOR) return new String("nw");
			if(c.getType() == Cursor.N_RESIZE_CURSOR) return new String("n");
			if(c.getType() == Cursor.NE_RESIZE_CURSOR) return new String("ne");
			if(c.getType() == Cursor.E_RESIZE_CURSOR) return new String("e");
			if(c.getType() == Cursor.SE_RESIZE_CURSOR) return new String("se");
			if(c.getType() == Cursor.S_RESIZE_CURSOR) return new String("s");
			if(c.getType() == Cursor.SW_RESIZE_CURSOR) return new String("sw");
			if(c.getType() == Cursor.W_RESIZE_CURSOR) return new String("w");
			return null;
		}
		public String placeOnBorder(Point p){
			if(!s.contains(p)) return new String("none");
			//1     2
			// 11 22
			// 33 44
			//3     4			
			float part = 0.1f;
			int x0 = s.getLocation().x;
			int y0 = s.getLocation().y;
			int x1 = x0 + s.width;
			int y1 = y0 + s.height;
			Point p1 = new Point(x0, y0);
			Point p2 = new Point(x1, y0);
			Point p3 = new Point(x0, y1);
			Point p4 = new Point(x1, y1);
			int x = x1 - x0;
			int y = y1 - y0;
			int dx = (int)(x*part);
			int dy = (int)(y*part);
			if(new Rectangle(p1.x, p1.y, dx, dy).contains(p)) return new String("nw");
			if(new Rectangle(p1.x+dx, p1.y, x-dx-dx, dy).contains(p)) return new String("n");
			if(new Rectangle(p2.x-dx, p2.y, dx, dy).contains(p)) return new String("ne");
			if(new Rectangle(p2.x-dx, p2.y+dy, dx, y-dy-dy).contains(p)) return new String("e");
			if(new Rectangle(p4.x-dx, p4.y-dy, dx, dy).contains(p)) return new String("se");
			if(new Rectangle(p3.x+dx, p3.y-dy, x-dx-dx, dy).contains(p)) return new String("s");
			if(new Rectangle(p3.x, p3.y-dy, dx, dy).contains(p)) return new String("sw");
			if(new Rectangle(p1.x, p1.y+dy, dx, y-dy-dy).contains(p)) return new String("w");
			return null;
		}
		public void draw(Graphics2D g){
			adjustFont();
			drawBorder(g);
			drawMarkup(g);
			if(showId) drawID(g); 
			if(showNumber) drawN(g);
			if(showRowColumn) drawRC(g);
		}
		public void adjustFont(){
			if(f != null){
				Font f2 = f.deriveFont((float)(Math.min(s.width, s.height)));
				f = f2;
			}
		}
		public void setAlpha(Graphics2D g, float alpha){
			int rule = AlphaComposite.SRC_OVER;
			AlphaComposite ac;
			ac = AlphaComposite.getInstance(rule, alpha);
			g.setComposite(ac);
		}
		public void drawBorder(Graphics2D g){
			// get needed properties
			Float alpha = Float.parseFloat(root.props.getProperty("local.alpha.markup.border"));
			Color color = PrefUtil.String2Color(root.props.getProperty("local.color.markup.border"));
			// draw
			setAlpha(g, alpha);
			g.setColor(color);
			g.draw(s);
		}
		public void drawMarkup(Graphics2D g){
			if(this.number!=sutra.getActiveSign()) {
				// get needed properties
				Float alpha = Float.parseFloat(root.props.getProperty("local.alpha.markup.p"));
				Color color = PrefUtil.String2Color(root.props.getProperty("local.color.markup.p"));
				// draw
				setAlpha(g, alpha);
				g.setColor(color);
			}
			else {
				// get needed properties
				Float alpha = Float.parseFloat(root.props.getProperty("local.alpha.markup.a"));
				Color color = PrefUtil.String2Color(root.props.getProperty("local.color.markup.a"));
				// draw
				setAlpha(g, alpha);
				g.setColor(color);
			}
			g.fill(s);
		}
		public void drawID(Graphics2D g){
			// get needed properties
			Float alpha = Float.parseFloat(root.props.getProperty("local.alpha.text"));
			Color color = PrefUtil.String2Color(root.props.getProperty("local.color.text"));
			// draw
			if(f != null) g.setFont(f);
			setAlpha(g, alpha);
			g.setColor(color);
			g.drawString(character, s.getBounds().x, s.getBounds().y+g.getFontMetrics().getHeight()*25/40);
		}
		public void drawN(Graphics2D g){
			// get needed properties
			Float alpha = Float.parseFloat(root.props.getProperty("local.alpha.text"));
			Color color = PrefUtil.String2Color(root.props.getProperty("local.color.text"));
			// draw
			if(f != null) g.setFont(f.deriveFont(f.getSize()/3.0f));
			setAlpha(g, alpha);
			g.setColor(color);
			g.drawString(String.valueOf(number), s.getBounds().x, s.getBounds().y+g.getFontMetrics().getAscent());
		}
		public void drawRC(Graphics2D g){
			// get needed properties
			Float alpha = Float.parseFloat(root.props.getProperty("local.alpha.text"));
			Color color = PrefUtil.String2Color(root.props.getProperty("local.color.text"));
			// draw
			if(f != null) g.setFont(f.deriveFont(f.getSize()/5.0f));
			setAlpha(g, alpha);
			g.setColor(color);
			g.drawString("("+String.valueOf(row)+","+String.valueOf(column)+")", s.getBounds().x, s.getBounds().y+g.getFontMetrics().getAscent());
		}

		//
		public String getXUpdate(boolean updateOnly){
			if(!updateOnly){
				String xupdate = 
					"<xu:modifications version=\'1.0\' xmlns:xu=\'http://www.xmldb.org/xupdate\'>" +
					//"    <xu:append select=\"//unihandb/char[@xmlid=\'U+"+NumUtil.dec2hex(character.codePointAt(0)).toUpperCase()+"\']\">" +
					"    <xu:append select=\"//char[@xmlid=\'U+"+NumUtil.dec2hex(character.codePointAt(0)).toUpperCase()+"\']\">" +
					"       <xu:element name=\"appearance\">" +
					"           <xu:attribute name=\"character\">"+this.character+"</xu:attribute>" +
					"           <xu:attribute name=\"id\">"+this.id+"</xu:attribute>" +
					"           <xu:attribute name=\"preferred_reading\">"+this.preferred_reading+"</xu:attribute>" +
					"           <xu:attribute name=\"variant\">"+this.variant+"</xu:attribute>" +
					"           <xu:attribute name=\"cert\">"+this.cert+"</xu:attribute>" +
					"           <xu:attribute name=\"nr\">"+sutra.sutra_id+"_"+this.number+"</xu:attribute>" +
					"           <source>"+sutra.sutra_id+"</source>" +
					"           <rubbing>"+sutra.sutra_path_rubbing+"</rubbing>" +
					"           <graphic>"+sign_path_snippet+"</graphic>" +
					"           <coordinates x=\""+s.x+"\" y=\""+s.y+"\" width=\""+s.width+"\" height=\""+s.height+"\" />" +
					"       </xu:element>" +
					"    </xu:append>" +
					"</xu:modifications>";
				/*String xupdate = "update insert " +
									"<appearance character=\""+this.character+"\" id=\""+this.id+"\" preferred_reading=\""+this.preferred_reading+"\" variant=\""+this.variant+"\" cert=\""+this.cert+"\" nr=\""+sutra.sutra_id+"_"+this.number+"\">" +
									"<source>"+sutra.sutra_id+"</source>" +
									"<rubbing>"+sutra.sutra_path_rubbing+"</rubbing>" +
									"<graphic>"+sign_path_snippet+"</graphic>" +
									"<coordinates x=\""+s.x+"\" y=\""+s.y+"\" width=\""+s.width+"\" height=\""+s.height+"\" />" +
									"</appearance> " +
									" into //unihandb/char[@xmlid=\'U+"+NumUtil.dec2hex(character.codePointAt(0)).toUpperCase()+"\']";*/
				return xupdate;
			}
			else{
				String xupdate = 
					"<xu:modifications version=\'1.0\' xmlns:xu=\'http://www.xmldb.org/xupdate\'>" +
					"	<xu:update select=\"//unihandb/char[@xmlid=\'U+"+NumUtil.dec2hex(character.codePointAt(0)).toUpperCase()+"\']/appearance[@id='"+this.id+"']/coordinates/@x\">" + this.s.x + "</xu:update>" +
					"	<xu:update select=\"//unihandb/char[@xmlid=\'U+"+NumUtil.dec2hex(character.codePointAt(0)).toUpperCase()+"\']/appearance[@id='"+this.id+"']/coordinates/@y\">" + this.s.y + "</xu:update>" + 
					"	<xu:update select=\"//unihandb/char[@xmlid=\'U+"+NumUtil.dec2hex(character.codePointAt(0)).toUpperCase()+"\']/appearance[@id='"+this.id+"']/coordinates/@width\">" + this.s.width + "</xu:update>" + 
					"	<xu:update select=\"//unihandb/char[@xmlid=\'U+"+NumUtil.dec2hex(character.codePointAt(0)).toUpperCase()+"\']/appearance[@id='"+this.id+"']/coordinates/@height\">" + this.s.height + "</xu:update>" + 
					"</xu:modifications>";
				return xupdate;
			}
		}
	}

}

