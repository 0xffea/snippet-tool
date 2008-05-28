package src.util.file;

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

public class HiWi_FileIO {
	
	public static String readStringFromFile(String pathname){
		ArrayList<String> out=new ArrayList<String>();
		
		//open streams
		try {
			FileInputStream fis=new FileInputStream(pathname);
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			//read line from stream
			String str=new String();
			for(;;){
				try {
					str=br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(str==null || str.length()<1){
					break;
				}
				else{
					out.add(str);
				}
			}
			//close streams
			try {
				br.close();
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
		String s = new String();
		for(int i=0; i<out.size(); i++){
			s += out.get(i);
		}
		return s;
	}
	public static ArrayList<String> readFromFile(String pathname){
		ArrayList<String> out=new ArrayList<String>();
		
		//open streams
		try {
			FileInputStream fis=new FileInputStream(pathname);
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			//read line from stream
			String str=new String();
			for(;;){
				try {
					str=br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(str==null || str.length()<1){
					break;
				}
				else{
					out.add(str);
				}
			}
			//close streams
			try {
				br.close();
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return out;
	}
	
	public static void writeStringToFile(String file, String str){
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos,"UTF-8"));
			bw.write(str);
			bw.flush();
			bw.close();
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}