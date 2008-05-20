package src.util.xml;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
import org.xml.sax.InputSource;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XUpdateQueryService;

import src.gui.HiWi_GUI;
import src.util.file.HiWi_FileIO;
import src.util.num.NumUtil;

public class XMLUtil {

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

	@SuppressWarnings("unchecked")
	public static String fetchXML(String hosturi, String user, String pass, String collection, String xml){
		try {
			String driver = "org.exist.xmldb.DatabaseImpl";    
			Class cl = Class.forName(driver);   
			Database database = (Database) cl.newInstance();   
			DatabaseManager.registerDatabase(database);   

			// get the collection   
			//Collection col = DatabaseManager.getCollection(hosturi + collection);   
			Collection col = DatabaseManager.getCollection(collection, user, pass);
			if(col == null) {
				System.out.println("Trying to get NULL Collection:\tDatabaseManager.getCollection("+collection+", "+user+", "+pass+")");
				return null;
			}
			//col.setProperty(OutputKeys.INDENT, "yes");
			XMLResource text = (XMLResource)col.getResource(xml);
			if(text != null) {
				return (String) text.getContent();
			}
			else{
				System.out.println("XMLResource is NULL");
				return null;
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
		return null;
	}

	public static String transformXML(String xml, String xslt){
		//System.out.println("starting transformXML()");
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

	@SuppressWarnings("unchecked")
	public static void updateXML(String id, String xupdate, String hosturi, String user, String pass, String out){
		try {
			//System.out.println("starting updateXML()");
			String driver = "org.exist.xmldb.DatabaseImpl";    
			Class cl = Class.forName(driver);  
			Database database = (Database) cl.newInstance();   
			DatabaseManager.registerDatabase(database);   

			// get the collection
			Collection col = DatabaseManager.getCollection(hosturi+out, user, pass);
			if(col == null){
				System.out.println("Trying to get NULL Collection:\tDatabaseManager.getCollection("+(hosturi+out)+", "+user+", "+pass+")");
				System.out.println("Aborting operation updateXML");
				return;
			}
			System.out.println("updating collection:\t"+col.getName());
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
			long modified = service.updateResource(xml_out[index], xupdate);
			System.out.println("updating:\t"+"id = "+id+" in "+xml_out[index]+"; modified:\t"+modified+" nodes");
			System.out.println(xupdate);
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
	
	@SuppressWarnings("unchecked")
	public static void clearAppearances(String hosturi, String user, String pass, String out, String regexp){
		//System.out.println("starting clearAppearances()");
		try {
			String driver = "org.exist.xmldb.DatabaseImpl";
			String xupdate = 	"<xu:modifications version=\'1.0\' xmlns:xu=\'http://www.xmldb.org/xupdate\'> " +
									"<xu:remove select=\"//*[substring(@id,0,"+(regexp.length()+1)+")='"+regexp+"']\" /> " +
									//"<xu:remove select=\"//*[substring(@id,0,6)='HDS_7'] /> " +
									//"<xu:remove select=\"//*[string-length(@id)>=0] /> " +
								"</xu:modifications>";
			System.out.println(xupdate);
			Class cl = Class.forName(driver);  
			Database database = (Database) cl.newInstance();   
			DatabaseManager.registerDatabase(database);   

			// get the collection   
			//Collection col = DatabaseManager.getCollection(hosturi + collection);   
			Collection col = DatabaseManager.getCollection(hosturi+out, user, pass);
			String[] xml_out = col.listResources();
			XUpdateQueryService service = (XUpdateQueryService) col.getService("XUpdateQueryService", "1.0");
			//long modified = 0;
			for(int i=0; i<xml_out.length; i++){
				long modified = service.updateResource(xml_out[i], xupdate);
				System.out.println("cleaning:\t"+"in "+xml_out[i]+"; modified:\t"+modified+" nodes");
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
	}
}
