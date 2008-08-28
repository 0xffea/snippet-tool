package src.util.unicode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;

import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.XMLResource;

import src.gui.HiWi_GUI;
import src.util.string.HiWi_StringIO;

/**
 * Contains functionality for generating unicode_xxxxx_1000.xml files.
 * 
 * @author Alexei Bratuhin
 *
 */
public class UnicodeTXT2XML {
	
	/**
	 * Generate xml from Unihan.txt
	 * Notice: generated file are saved in same directory that is containing Unihan.txt
	 * @param dir	directory containing Unihan.txt
	 * @param upload	whether generated files should be uploaded onto server
	 */
	public static void generateUnicodeDBFiles(String dir, boolean upload){
		//
		System.out.println("generating unicode db files...");
		//
		String file_out_base = dir + "unicode";
		String file_in_unihan = dir + "Unihan.txt";
		
		try {
			FileInputStream fis_unihan = new FileInputStream(file_in_unihan);
			//InputStream fis_unihan = cl.getResourceAsStream("data/unicode/Unihan.txt");
			FileOutputStream fos = null;
			BufferedReader br_unihan = new BufferedReader(new InputStreamReader(fis_unihan, "UTF-8"));
			BufferedWriter bw = null;

			String str_unihan = new String();
			
			ArrayList<String> als_unihan = new ArrayList<String>();
			
			int counter = 0;
			int slice = 1000;
			String n_curr_id = new String();
			String curr_file = new String();
			for(;;){
				str_unihan = br_unihan.readLine();
				if(str_unihan == null || str_unihan.length()<1){
					bw.write("\t</charProp>"+"\n");
					bw.write("</char>"+"\n");
					bw.write("</unihandb>"+"\n");
					if(fos != null && bw != null){
						fos.flush();
						bw.flush();
						fos.close();
						bw.close();
					}
					if(upload){
						File f = new File(curr_file);
						uploadUnicodeDBFile(f);
					}
					break;
				}
				if(str_unihan.startsWith("#")) continue;
				HiWi_StringIO.String2ArrayListOfString(str_unihan, als_unihan, "\t", 3);
				if(als_unihan==null || als_unihan.size()<1){
					bw.write("\t</charProp>"+"\n");
					bw.write("</char>"+"\n");
					bw.write("</unihandb>"+"\n");
					if(fos != null && bw != null){
						fos.flush();
						bw.flush();
						fos.close();
						bw.close();
					}
					if(upload){
						File f = new File(curr_file);
						uploadUnicodeDBFile(f);
					}
					break;
				}
				
				if(!n_curr_id.equals(als_unihan.get(0))){
					//close previous <char> tag if any
					if(counter > 0){
						bw.write("\t</charProp>"+"\n");
						bw.write("</char>"+"\n");
					}
					if(counter % slice == 0){//if should change the output file
						if(fos != null && bw != null){
							bw.write("</unihandb>");
							if(fos != null && bw != null){
								fos.flush();
								bw.flush();
								fos.close();
								bw.close();
							}
							if(upload){
								File f = new File(curr_file);
								uploadUnicodeDBFile(f);
							}
						}
						
						counter = counter % slice;
						curr_file = file_out_base+"_"+als_unihan.get(0).substring(2)+"_"+slice+".xml";
						fos = new FileOutputStream(curr_file);
						bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
					}
					//start new <char> tag
					n_curr_id = als_unihan.get(0); counter++;
					if(counter % slice == 1){
						bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+"\n");
						bw.write("<unihandb>"+"\n");
					}
					bw.write("<char xmlid=\""+n_curr_id+"\">"+"\n");
					bw.write("\t<charProp>"+"\n");
					bw.write("\t\t"+"<"+als_unihan.get(1)+">"+als_unihan.get(2).replaceAll("<", "-").replaceAll(">", "-")+"</"+als_unihan.get(1)+">"+"\n");
				}
				else{
					bw.write("\t\t"+"<"+als_unihan.get(1)+">"+als_unihan.get(2).replaceAll("<", "-").replaceAll(">", "-")+"</"+als_unihan.get(1)+">"+"\n");
				}
			}
			
			br_unihan.close();
			fis_unihan.close();
			
			//
			System.out.println("finished generating unicode db files");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void uploadUnicodeDBFile(File f){
		Properties props = null;
		try {
	        props.load(new FileInputStream(HiWi_GUI.PROPERTIES_FILE));
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	return;
	    }
	    
	    String db_uri = props.getProperty("db.uri");
	    String db_user = props.getProperty("db.user");
	    String db_passwd = props.getProperty("db.passwd");
	    String db_collection = props.getProperty("db.dir.out");
	    
		try {
			String driver = "org.exist.xmldb.DatabaseImpl";    
			Class cl = Class.forName(driver); 
			Database database = (Database) cl.newInstance();   
			DatabaseManager.registerDatabase(database);
			Collection current = DatabaseManager.getCollection(db_uri+db_collection, db_user, db_passwd);
			if(current == null){
				Collection root = DatabaseManager.getCollection(db_uri, db_user, db_passwd);   
	            CollectionManagementService mgtService = (CollectionManagementService) root.getService("CollectionManagementService", "1.0");   
	            current = mgtService.createCollection(db_collection);  
			}
	        XMLResource resource = (XMLResource) current.createResource(f.getName(), "XMLResource");
	        resource.setContent(f);
	        current.storeResource(resource);
	        System.out.println("wrote xml to database:\t"+f.getName());
	        f.delete();
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

