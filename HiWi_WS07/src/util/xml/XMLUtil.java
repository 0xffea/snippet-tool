package src.util.xml;


import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XUpdateQueryService;

import src.gui.HiWi_GUI;
import src.model.HiWi_Object_Inscript;
import src.model.HiWi_Object_Character;
import src.util.num.NumUtil;

/**
 * Collection of functions for dealing with XML data of inscript's .xml description.
 * Primarily created for transformation functions, used to extract needed data from inscripts .xml description.
 * 
 * @author Alexei Bratuhin
 *
 */
public class XMLUtil {
	
	/**
	 * Extracts preferred reading text of inscript from Inscript object.
	 * @param inscript inscript
	 * @return
	 */
	public static String getPlainTextFromApp(HiWi_Object_Inscript inscript){
		String out = new String();
		int row = 1;
		int crow = 1;
		for(int i=0; i<inscript.inscript_text.size(); i++){
			HiWi_Object_Character csign = inscript.inscript_text.get(i).get(0).get(0);
			crow = csign.row;

			if(crow != row){	// add breakline
				out += "\n";
				row = crow;
			}


			out += csign.characterStandard;
		}
		if(out.startsWith("\n")){
			out = out.substring(1);
		}
		return out;
	}

	/**
	 * Extracts preferred reading text from inscript from inscript's .xml description.
	 * @param xml	contents of inscript's .xml description
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String getPlainTextFromXML(String xml){
		String out = new String();
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(new InputSource(new StringReader(xml)));
			List<org.jdom.Element> l = doc.getRootElement().getChildren();
			for(int i=0; i<l.size(); i++){
				// line break
				if(l.get(i).getName().equals("br")){
					out += "\n";
				}
				// normal sign
				if(l.get(i).getName().equals("span")){
					out += l.get(i).getText();
				}
				// handle lem/rdg
				if(l.get(i).getName().equals("app")){
					// assuming preferred reading is in lem
					//Element lem = l.get(i).getChild("lem");
					Element lem = (Element) l.get(i).getChildren().get(0);
					//List<Element> spans = lem.getChildren("span");
					List<Element> spans = lem.getChildren();
					for(int c=0; c<spans.size(); c++){
						out += spans.get(c).getText();
					}
				}
				// there are several readings
				if(l.get(i).getName().equals("choice")){
					// find variant with max cert
					//List<org.jdom.Element> lvariants = l.get(i).getChildren("variant");
					List<org.jdom.Element> lvariants = l.get(i).getChildren();
					int maxcertindex = 0;
					float maxcert = 0.0f;
					for(int v=0; v<lvariants.size(); v++){
						if(Float.parseFloat(lvariants.get(v).getAttributeValue("cert")) > maxcert){
							maxcertindex = new Integer(v);
							maxcert = Float.parseFloat(lvariants.get(v).getAttributeValue("cert"));
						}
					}
					// output variant with max cert
					org.jdom.Element choice = lvariants.get(maxcertindex);
					List<org.jdom.Element> spans = choice.getChildren();
					for(int c = 0; c<spans.size(); c++){
						out += spans.get(c).getText();
					}
				}
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//remove starting newline, if any
		if(out.startsWith("\n")){
			out = out.substring(1);
		}

		//
		return out;
	}
	
	/**
	 * Fetch content of inscript's .xml description from database
	 * @param root		reference to HiWi_GUI
	 * @param user		database's username
	 * @param pass		database's password
	 * @param collection	database's collection
	 * @param xml			database's resource
	 * @return				content of inscript's .xml
	 */
	@SuppressWarnings("unchecked")
	public static String fetchXML(HiWi_GUI root, String user, String pass, String collection, String xml){
		//
		root.addLogEntry("** started fetching XML **", 1, 1);
		root.addLogEntry("\thosturi="+collection, 0, 1);
		root.addLogEntry("\tuser="+user, 0, 1);
		root.addLogEntry("\tpass="+pass, 0, 1);
		root.addLogEntry("\txml="+xml, 0, 1);
		//
		String resultxml = null;
		try {
			String driver = "org.exist.xmldb.DatabaseImpl";    
			Class cl = Class.forName(driver);   
			Database database = (Database) cl.newInstance();   
			DatabaseManager.registerDatabase(database);   

			// get the collection   
			Collection col = DatabaseManager.getCollection(collection, user, pass);
			if(col == null) {
				//System.out.println("Trying to get NULL Collection:\tDatabaseManager.getCollection("+collection+", "+user+", "+pass+")");
				root.addLogEntry("Error fetching XML: trying to get NULL Collection:\tDatabaseManager.getCollection("+collection+", "+user+", "+pass+")", 1, 1);
			}
			else{
				//col.setProperty(OutputKeys.INDENT, "yes");
				XMLResource text = (XMLResource)col.getResource(xml);
				if(text != null) {
					resultxml = (String) text.getContent();
				}
				else{
					//System.out.println("XMLResource is NULL");
					root.addLogEntry("Error fetching XML: XMLResource is NULL", 1, 1);
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (XMLDBException e) {
			e.printStackTrace();
		}
		//
		root.addLogEntry("** ended fetching XML **", 1, 1);
		//
		return resultxml;
	}
	
	/**
	 * Apply XSL Transformation to XML
	 * @param xml	content of XML
	 * @param xslt	content of XSLT
	 * @return		content of resultant transformation
	 */
	public static String transformXML(String xml, String xslt){
		StringWriter sw = new StringWriter();
		TransformerFactory  tFactory = TransformerFactory.newInstance();
		Source xslSource = new StreamSource(new StringReader(xslt));
		Transformer transformer;
		try {
			transformer = tFactory.newTransformer(xslSource);
			if(transformer!=null) transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(sw));
			return sw.toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Perform an XUpdate on eXist database
	 * @param root	reference to HiWi_GUI
	 * @param id	id of inscript to update
	 * @param xupdate	xupdate text
	 * @param user		database's username
	 * @param pass		database's password
	 * @param out		dtabase's collection to update
	 */
	@SuppressWarnings("unchecked")
	public static void updateXML(HiWi_GUI root, String id, String xupdate, String user, String pass, String out){
		//
		root.addLogEntry("** started updating XML **", 1, 1);
		root.addLogEntry("\tid="+id, 0, 1);
		root.addLogEntry("\txupdate="+xupdate, 0, 1);
		root.addLogEntry("\thosturi="+out, 0, 1);
		root.addLogEntry("\tuser="+user, 0, 1);
		root.addLogEntry("\tpass="+pass, 0, 1);
		//
		try {
			//System.out.println("starting updateXML()");
			String driver = "org.exist.xmldb.DatabaseImpl";    
			Class cl = Class.forName(driver);  
			Database database = (Database) cl.newInstance();   
			DatabaseManager.registerDatabase(database);   

			// get the collection
			Collection col = DatabaseManager.getCollection(out, user, pass);
			if(col == null){
				root.addLogEntry("Error updating XML: trying to get NULL Collection:\tDatabaseManager.getCollection("+(out)+", "+user+", "+pass+")", 1, 1);
			}
			else{
				root.addLogEntry("updating collection:\t"+col.getName(), 0, 1);
				// find out which file to update
				String[] xml_out = col.listResources();
				ArrayList<String> xml_out_a = new ArrayList<String>(Arrays.asList(xml_out));
				for(int i=0; i<xml_out_a.size(); i++){	// clean file list from files with inproper filenames
					if(!xml_out_a.get(i).startsWith("unicode_") ||
							!xml_out_a.get(i).contains("_1000.xml"))
						xml_out_a.remove(i);
				}
				xml_out = (String[])xml_out_a.toArray(new String[xml_out_a.size()]);

				String[] xml_out_mod = new String[xml_out.length]; // create file list, containing extracted starting codepoints as name
				for(int i=0; i<xml_out.length; i++){
					xml_out_mod[i] = xml_out[i].substring("unicode_".length(), xml_out[i].length()-"_1000.xml".length());
				}
				int index = NumUtil.myIndex(xml_out_mod, id);	// get index of file to update
				// what if index==-1 -> this char is not in db yet?

				// update
				XUpdateQueryService service = (XUpdateQueryService) col.getService("XUpdateQueryService", "1.0");
				service.setCollection(col);
				long modified = service.updateResource(xml_out[index], xupdate);
				
				root.addLogEntry("updating:\t"+"id = "+id+" in "+xml_out[index]+"; modified:\t"+modified+" nodes", 1, 1);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (XMLDBException e) {
			e.printStackTrace();
		} 
		//
		root.addLogEntry("** ended updating XML **", 1, 1);
	}

	/**
	 * Clear all <appearance>s to given inscript id, meaning - clear marking of inscript with given id
	 * Notice: regexp must end with an '_'(underscore), since matching is done by startsWith() mean.
	 * @param root		reference to HiWi_GUI
	 * @param user		database's username
	 * @param pass		database's password
	 * @param out		database's collection, containing inscript's marking
	 * @param regexp	inscript's id
	 */
	@SuppressWarnings("unchecked")
	public static void clearAppearances(HiWi_GUI root, String user, String pass, String out, String regexp){
		// avoid accidentaly deleting all appearances
		if(regexp.length() < 2) return; 
		
		// remove appearances
		try {
			String driver = "org.exist.xmldb.DatabaseImpl";
			String xupdate = 	"<xu:modifications version=\'1.0\' xmlns:xu=\'http://www.xmldb.org/xupdate\'> " +
			"<xu:remove select=\"//appearance[substring(@id,0,"+(regexp.length()+1)+")='"+regexp+"']\" /> " +
			"</xu:modifications>";
			
			root.addLogEntry("xupdate="+xupdate, 0, 1);
			Class cl = Class.forName(driver);  
			Database database = (Database) cl.newInstance();   
			DatabaseManager.registerDatabase(database);   

			// get the collection   
			//Collection col = DatabaseManager.getCollection(hosturi + collection);   
			Collection col = DatabaseManager.getCollection(out, user, pass);
			String[] xml_out = col.listResources();
			XUpdateQueryService service = (XUpdateQueryService) col.getService("XUpdateQueryService", "1.0");
			long modifiedTotal = 0;
			for(int i=0; i<xml_out.length; i++){
				long modified = service.updateResource(xml_out[i], xupdate);
				modifiedTotal += modified;
				//System.out.println("cleaning:\t"+"in "+xml_out[i]+"; modified:\t"+modified+" nodes");
				root.addLogEntry("cleaning:\t"+"in "+xml_out[i]+"; modified:\t"+modified+" nodes", 1, 1);
			}
			root.addLogEntry("cleaned totally:\t"+modifiedTotal+" nodes", 1, 1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (XMLDBException e) {
			e.printStackTrace();
		} 

		// TODO: remove snippets
	}

	/**
	 * Standardize xml after applying the xsl transformation. This is done to make the implementation of HiWi_Object_Inscript.readTextFromXML() easier.
	 * Notice: It's better to have an agreement of what the inscript's xml structure may be like, to avoid complicated transformations
	 * @param xml
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String standardizeXML(String xml){
		try {
			StringWriter sw = new StringWriter();
			SAXBuilder builder = new SAXBuilder();
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getPrettyFormat());
			Document docIn = builder.build(new StringReader(xml));
			Document docOut= new Document(new Element("text"));

			Element rootIn = docIn.getRootElement();
			Element rootOut= docOut.getRootElement();

			rootOut.removeContent();

			ArrayList<Element> elementsIn = new ArrayList<Element>(rootIn.getChildren());
			
			/*
			 * Possible children:
			 * 
			 * <br/>
			 * 
			 * <span/>
			 * 
			 * <norm/>
			 * 
			 * <choice>
			 * 	<variant>
			 * 		<span/>
			 * 		<span/>
			 * 	</variant>
			 * 	<variant/>
			 * </choice>
			 * 
			 * <supplied rend="ignore">
			 * 	<choice>
			 * 		<variant>
			 * 			<span/>
			 * 			<span/>
			 * 		</variant>
			 * 		<variant/>
			 * 	</choice>
			 * </supplied>
			 * 
			 * */

			for(int i=0; i<elementsIn.size(); i++){

				Element element = elementsIn.get(i);
				

				// newline
				if(element.getName().equals("br")){
					element.getParentElement().removeContent(element);

					rootOut.addContent(element);
				}

				// supplied
				if(element.getName().equals("supplied")){					
					element.getParentElement().removeContent(element);
					
					boolean ignoreSupplied = (element.getAttribute("rend") != null && element.getAttributeValue("rend") != "")? true : false;
					List<Element> suppliedChildren = element.getChildren();
					
					int iterations = suppliedChildren.size();
					for(int j=0; j<iterations; j++){						
						Element suppliedElement = suppliedChildren.remove(0);
						
						if(suppliedElement.getName().equals("span")){
							if(ignoreSupplied) suppliedElement.setAttribute("class", "supplied");
							else suppliedElement.setAttribute("class", "quasi-supplied");

							Element choice = new Element("choice");
							Element variant = new Element("variant");

							choice.addContent(variant);
							variant.setAttribute("cert", "1.0");
							variant.addContent(suppliedElement);

							rootOut.addContent(choice);
						}
						if(suppliedElement.getName().equals("norm")){
							Element choice = new Element("choice");
							Element variant = new Element("variant");

							Element span = (Element) suppliedElement.getChildren().get(0);
							suppliedElement.removeContent(span);

							choice.addContent(variant);
							variant.setAttribute("cert", "1.0");
							span.setAttribute("class", "supplied");
							span.setAttribute("original", suppliedElement.getAttributeValue("orig"));
							variant.addContent(span);

							rootOut.addContent(choice);
						}
						if(suppliedElement.getName().equals("choice")){
							for(int k=0; k<suppliedElement.getChildren().size(); k++){
								Element cvariant = (Element) suppliedElement.getChildren().get(k);
								for(int l=0; l<cvariant.getChildren().size(); l++){
									Element cspan = (Element) cvariant.getChildren().get(l);
									cspan.setAttribute("class", "supplied");
								}
							}

							rootOut.addContent(suppliedElement);
						}
					}
				}
				// other
				else{
					if(element.getName().equals("span")){
						element.getParentElement().removeContent(element);
						Element choice = new Element("choice");
						Element variant = new Element("variant");

						choice.addContent(variant);
						variant.setAttribute("cert", "1.0");
						variant.addContent(element);

						rootOut.addContent(choice);
					}
					if(element.getName().equals("norm")){
						element.getParentElement().removeContent(element);
						Element choice = new Element("choice");
						Element variant = new Element("variant");

						Element span = (Element) element.getChildren().get(0);
						element.removeContent(span);

						choice.addContent(variant);
						variant.setAttribute("cert", "1.0");
						span.setAttribute("class", "normalized");
						span.setAttribute("original", element.getAttributeValue("orig"));
						variant.addContent(span);

						rootOut.addContent(choice);
					}
					if(element.getName().equals("choice")){
						element.getParentElement().removeContent(element);
						rootOut.addContent(element);
					}
				}

			}

			outputter.output(docOut, sw);

			return sw.toString();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
