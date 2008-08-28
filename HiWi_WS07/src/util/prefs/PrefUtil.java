package src.util.prefs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

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
	
	public static Point string2point(String windowSize){
		int hsize = Integer.valueOf(windowSize.substring(0, windowSize.indexOf("x")));
		int vsize = Integer.valueOf(windowSize.substring(windowSize.indexOf("x")+1));
		return new Point(hsize, vsize);
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

}
