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

import com.sun.image.codec.jpeg.ImageFormatException;

public class ImageUtil {
	
	@SuppressWarnings("unchecked")
	public static BufferedImage fetchImage(String hosturi, String collection, String img){
		//System.out.println("fetchImage(): "+hosturi+", "+collection+", "+img);
		try {
			String driver = "org.exist.xmldb.DatabaseImpl";    
			Class cl = Class.forName(driver);   
			Database database = (Database) cl.newInstance();   
			DatabaseManager.registerDatabase(database);   

			// get the collection   
			//Collection col = DatabaseManager.getCollection(hosturi + collection);   
			Collection col = DatabaseManager.getCollection(collection);
			if(col == null){
				System.out.println("NULL COLLECTION, aborting operation fetchImage");
				return null;
			}
			//col.setProperty(OutputKeys.INDENT, "yes");
			BinaryResource image = (BinaryResource)col.getResource(img);
			byte[] bytear = (byte[]) image.getContent();
			//ByteArrayInputStream bis = new ByteArrayInputStream (bytear,0,bytear.length);
			BufferedImage bim = ImageIO.read (new ByteArrayInputStream(bytear));
			return bim;
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
		return null;
	}

}
