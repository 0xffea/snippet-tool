package src.util.db;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;

/**
 * Collection of functions to ease the use of XMLDB:API and to improve code readability. 
 * 
 * @author Alexei Bratuhin
 *
 */
public class DbUtil {
	
	@SuppressWarnings("unchecked")
	public static ResourceSet executeQuery(String collection, String query){
		try {
			String driver = "org.exist.xmldb.DatabaseImpl";  
			Class cl = Class.forName(driver);	           
			Database database = (Database)cl.newInstance();  
			DatabaseManager.registerDatabase(database);

			Collection col = DatabaseManager.getCollection(collection);  
			XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");  
			service.setProperty("indent", "yes");

			ResourceSet result = service.query(query);
			return result;
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

}
