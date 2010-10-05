package org.stonesutras.snippettool.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.stonesutras.snippettool.util.StringUtil;

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
	public static void generateUnicodeDBFiles(String dir){
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
					break;
				}
				if(str_unihan.startsWith("#")) continue;
				StringUtil.String2ArrayListOfString(str_unihan, als_unihan, "\t", 3);
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
}

