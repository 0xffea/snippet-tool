package org.abratuhi.snippettool.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.jdom.Element;


public class SnippetToolCharacterMetrics {
	
	public static void main(String [] args){
		
		String infilename = "character-metrics.txt";
		String outfilename = "character-metrics.xml";
		Integer lines = 0;
		
		//open streams
		try {
			FileInputStream fis=new FileInputStream(infilename);
			FileOutputStream fos = new FileOutputStream(outfilename);
			BufferedReader br = null;
			BufferedWriter bw = null;
			
			try {
				br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
				bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			//read line from stream
			String instr=new String();
			String outstr = new String();
			Element templ = new Element("character", "c")
							.setAttribute("char", "")
							.setAttribute("column", "")
							.setAttribute("height", "")
							.setAttribute("row", "")
							.setAttribute("width", "")
							.addContent(new Element("engraving", "c")
								.addContent(new Element("width", "c")
									.setAttribute("type", "")
									.addContent(new Element("point", "c"))
									.addContent(new Element("range", "c")
										.setAttribute("lower", "")
										.setAttribute("upper", "")))
								.addContent(new Element("depth", "c")
									.setAttribute("type", "")
									.addContent(new Element("point", "c"))
									.addContent(new Element("range", "c")
										.setAttribute("lower", "")
										.setAttribute("upper", ""))))
							.addContent(new Element("condition", "c")
								.setAttribute("grade", ""));
			for(;;){
				try {
					instr=br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(instr==null){
					break;
				}
				else{
					instr = instr.replaceAll("\t", ":");
					String[] strsplit = new String[7];
					String[] strspl = instr.split(":");
					for(int i=0; i<strspl.length; i++){
						strsplit[i] = strspl[i];
					}
					
					String column = new String();
					String row = new String();
					String width = new String();
					String height = new String();
					String ewidthp = new String();
					String ewidthl = new String();
					String ewidthu = new String();;
					String edepthp = new String();
					String edepthl = new String();
					String edepthu = new String();
					String condition = new String();
					
					if(strsplit[0] != null){
						Boolean isInteger = true;
						try {Integer.parseInt(strsplit[0]);} catch(NumberFormatException e){isInteger = false; System.out.println("Cannot convert to Integer: "+strsplit[0]);}
						if(isInteger) column = strsplit[0];
					}
					if(strsplit[1] != null){
						Boolean isInteger = true;
						try {Integer.parseInt(strsplit[0]);} catch(NumberFormatException e){isInteger = false; System.out.println("Cannot convert to Integer: "+strsplit[1]);}
						if(isInteger) row = strsplit[1];
					}
					if(strsplit[2] != null){
						Boolean isFloat = true;
						strsplit[2] = strsplit[2].replace("*", "");
						strsplit[2] = strsplit[2].replace(" ", "");
						try {Float.parseFloat(strsplit[2]);} catch(NumberFormatException e){isFloat = false; System.out.println("Cannot convert to Float: "+strsplit[2]);}
						if(isFloat) width = strsplit[2];
					}
					if(strsplit[3] != null){
						Boolean isFloat = true;
						strsplit[3] = strsplit[3].replace("*", "");
						strsplit[3] = strsplit[3].replace(" ", "");
						try {Float.parseFloat(strsplit[3]);} catch(NumberFormatException e){isFloat = false; System.out.println("Cannot convert to Float: "+strsplit[3]);}
						if(isFloat) height = strsplit[3];
					}
					if(strsplit[4] != null){
						if(strsplit[4].contains("-")){
							String lower = strsplit[4].split("-")[0];
							String upper = strsplit[4].split("-")[1];
							try{
								Float.parseFloat(lower);
								Float.parseFloat(upper);
								
								ewidthl = lower;
								ewidthu = upper;
							}
							catch(NumberFormatException e){
								
							}
						}
						else{
							try{
								Float.parseFloat(strsplit[4]);
								
								ewidthp = strsplit[4];
							}
							catch(NumberFormatException e){
								
							}
						}
					}
					if(strsplit[5] != null){
						if(strsplit[5].contains("-")){
							String lower = strsplit[5].split("-")[0];
							String upper = strsplit[5].split("-")[1];
							try{
								Float.parseFloat(lower);
								Float.parseFloat(upper);
								
								edepthl = lower;
								edepthu = upper;
							}
							catch(NumberFormatException e){
								
							}
						}
						else{
							try{
								Float.parseFloat(strsplit[5]);
								
								edepthp = strsplit[5];
							}
							catch(NumberFormatException e){
								
							}
						}
					}
					if(strsplit[6] != null){
						condition = strsplit[6];
					}
					
					outstr = new String();
					outstr += "<c:character char=\"\" column=\""+column+"\" height=\""+height+"\" row=\""+row+"\" width=\""+width+"\">"+
				              "<c:engraving>"+
				              "<c:width type=\"\">"+
				              "<c:point>"+ewidthp+"</c:point>"+
				              "<c:range lower=\""+ewidthl+"\" upper=\""+ewidthu+"\"/>"+
				              "</c:width>"+
				              "<c:depth type=\"\">"+
				              "<c:point>"+edepthp+"</c:point>"+
				              "<c:range lower=\""+edepthl+"\" upper=\""+edepthu+"\"/>"+
				              "</c:depth>"+
				              "</c:engraving>"+
				              "<c:condition grade=\""+condition+"\"/>"+
				              "</c:character>";

					bw.write(outstr+"\n");
					
				}
				
				lines++;
				//if(lines > 10) break;
				
			}
			//close streams
			try {
				br.close();
				fis.close();
				
				bw.flush();
				fos.flush();
				bw.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
