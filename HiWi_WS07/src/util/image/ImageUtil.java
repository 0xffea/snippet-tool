package src.util.image;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
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
import src.model.HiWi_Object_Character;
import src.util.db.DbUtil;

import com.sun.image.codec.jpeg.ImageFormatException;

public class ImageUtil {
	
	@SuppressWarnings("unchecked")
	public static BufferedImage fetchImage(HiWi_GUI root, String collection, String img){
		//
		root.addLogEntry("* started fetching image *", 1, 1);
		root.addLogEntry("\thosturi="+collection, 0, 1);
		root.addLogEntry("\timg="+img, 0, 1);
		//
		BufferedImage bi = null;
		/*try {
			bi = ImageIO.read(new File("C:\\Users\\abratuhi\\Downloads\\tieshan_alleZeichen_2mm_kepeng_cutms SW.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		try {
			String driver = "org.exist.xmldb.DatabaseImpl";    
			Class cl = Class.forName(driver);   
			Database database = (Database) cl.newInstance();   
			DatabaseManager.registerDatabase(database);   

			// get the collection 
			Collection col = DatabaseManager.getCollection(collection);
			if(col == null){
				//System.out.println("NULL COLLECTION, aborting operation fetchImage");
				root.addLogEntry("NULL COLLECTION, aborting operation fetchImage", 1, 1);
			}
			else{
				//col.setProperty(OutputKeys.INDENT, "yes");
				BinaryResource image = (BinaryResource)col.getResource(img);
				byte[] bytear = (byte[]) image.getContent();
				//ByteArrayInputStream bis = new ByteArrayInputStream (bytear,0,bytear.length);
				bi = ImageIO.read (new ByteArrayInputStream(bytear));
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (XMLDBException e) {
			e.printStackTrace();
		} catch (ImageFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//
		root.addLogEntry("* ended fetching image *", 1, 1);
		//
		return bi;
	}

	public static File[] cutImageDueCoordinates(HiWi_GUI root, String img_collection, String img_resource, String coordinatesId){
		try {
			String coordinatesCollection = root.props.getProperty("db.dir.out");
			
			BufferedImage bi = fetchImage(root, img_collection, img_resource);

			String coordinatesQuery = "for $a in //appearance where $a/source='"+coordinatesId+"' and $a/@variant='0' return $a";
			ResourceSet rs = DbUtil.executeQuery(coordinatesCollection, coordinatesQuery);
			int rsSize = (int) rs.getSize();
			int biCutsCounter = 0;
			
			
			BufferedImage[] biCuts = new BufferedImage[rsSize];
			File[] iCuts = new File[rsSize];

			SAXBuilder builder = new SAXBuilder();
			ResourceIterator iterator;
			iterator = rs.getIterator();
			
			while(iterator.hasMoreResources()) {  
				Resource res = iterator.nextResource();
				XMLResource xmlres = (XMLResource) res;
				Document d = builder.build(new StringReader((String) xmlres.getContent()));
				Element appearance = d.getRootElement();	// it's appearance tag

				String chId = appearance.getAttributeValue("id");
				Element xmlc = appearance.getChild("coordinates");
				int n = Integer.parseInt(appearance.getAttributeValue("nr").substring((coordinatesId+"_").length()));
				String rc = chId.substring((coordinatesId+"_").length());
				int r = Integer.parseInt(rc.substring(0, rc.indexOf("_")));
				int c = Integer.parseInt(rc.substring(rc.indexOf("_")+1));
				int x = Integer.parseInt(xmlc.getAttributeValue("x"));
				int y = Integer.parseInt(xmlc.getAttributeValue("y"));
				int width = Integer.parseInt(xmlc.getAttributeValue("width"));
				int height = Integer.parseInt(xmlc.getAttributeValue("height"));
				
				Rectangle2D rectangle = new Rectangle(new Point(x,y), new Dimension(width, height));
				biCuts[biCutsCounter] = bi.getSubimage((int)Math.max(0,rectangle.getX()), (int)Math.max(0, rectangle.getY()), (int)Math.min(bi.getWidth()-rectangle.getX(), rectangle.getWidth()), (int)Math.min(bi.getHeight()-rectangle.getY(), rectangle.getHeight()));
				iCuts[biCutsCounter] = new File("tmp\\img\\subimage_"+coordinatesId+"_"+r+"_"+c+".png");
				ImageIO.write(biCuts[biCutsCounter], "png", iCuts[biCutsCounter]);
				
				biCutsCounter++;
			}
			
			return iCuts;
		}catch (XMLDBException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void saveSubimages(HiWi_GUI root, File[] subimages){
		try {
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
			
			for(int i=0; i<subimages.length; i++){
		        root.addLogEntry("storing subimage:\t"+subimages[i].getName(), 1, 1);
				BinaryResource resource = (BinaryResource) current.createResource(subimages[i].getName(), "BinaryResource");
		        resource.setContent(subimages[i]);
		        current.storeResource(resource);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (XMLDBException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
        
	}

}
