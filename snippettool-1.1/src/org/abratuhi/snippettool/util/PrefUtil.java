package org.abratuhi.snippettool.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Collection of function to convert between different Properties and Preferences.
 * 
 * @author Alexei Bratuhin
 *
 */
public class PrefUtil {
	
	public static Dimension string2dimesion(String windowSize){
		int hsize = Integer.valueOf(windowSize.substring(0, windowSize.indexOf("x")));
		int vsize = Integer.valueOf(windowSize.substring(windowSize.indexOf("x")+1));
		return new Dimension(hsize, vsize);
	}
	
	public static String dimension2string(Dimension dimension){
		return new String(dimension.width+"x"+dimension.height);
	}
	
	public static Point string2point(String windowSize){
		int hsize = Integer.valueOf(windowSize.substring(0, windowSize.indexOf(",")));
		int vsize = Integer.valueOf(windowSize.substring(windowSize.indexOf(",")+1));
		return new Point(hsize, vsize);
	}
	
	public static String point2string(Point point){
		return new String(point.x+","+point.y);
	}
	
	public static Integer string2integer(String size){
		return Integer.valueOf(size);
	}
	
	public static Color String2Color(String s){
		return new Color(Integer.parseInt(s, 16));
	}
	public static String Color2String(Color c){
		String r = Integer.toHexString(c.getRed());
		String g = Integer.toHexString(c.getGreen());
		String b = Integer.toHexString(c.getBlue());
		return new String(r+g+b);
	}
	
	public static void saveProperties(Properties properties, String filename){
		try {
	        properties.store(new FileOutputStream(filename), null);
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
	
	public static Properties loadProperties(String filename){
		Properties props = new Properties();
		try {
	        props.load(new FileInputStream(filename));
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	    return props;
	}

}
