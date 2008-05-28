package src.util.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.BinaryResource;

import src.gui.HiWi_GUI;

import com.sun.image.codec.jpeg.ImageFormatException;

public class ImageUtil {
	
	@SuppressWarnings("unchecked")
	public static BufferedImage fetchImage(HiWi_GUI root, String hosturi, String collection, String img){
		//
		root.addLogEntry("* started fetching image *", 1, 1);
		root.addLogEntry("\thosturi="+hosturi, 0, 1);
		root.addLogEntry("\tcollection="+collection, 0, 1);
		root.addLogEntry("\timg="+img, 0, 1);
		//
		BufferedImage bi = null;
		try {
			String driver = "org.exist.xmldb.DatabaseImpl";    
			Class cl = Class.forName(driver);   
			Database database = (Database) cl.newInstance();   
			DatabaseManager.registerDatabase(database);   

			// get the collection   
			//Collection col = DatabaseManager.getCollection(hosturi + collection);   
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

}
