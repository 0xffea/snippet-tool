package src.util.string;

import java.util.ArrayList;

/**
 * @author Alexei Bratuhin
 *
 */
public class HiWi_StringIO {
	
	/**
	 * Splits String to ArrayList of its Characters
	 * @param str in:string
	 * @param als out:array list
	 */
	public static void String2ArrayListOfString(String str, ArrayList<String> als){
		als.clear();
		for(int i=0; i<str.length(); i++){
			als.add(new String(String.valueOf(str.charAt(i))));
		}
	}
	
	/**
	 * Splits String to ArrayList using separator String
	 * Notice: StringTokenizer can be used instead.
	 * @param str	in:string
	 * @param als	out:list
	 * @param sep	in:separator
	 */
	public static void String2ArrayListOfString(String str, ArrayList<String> als, String sep){
		als.clear();
		String str_t = new String(str);
		while(str_t.contains(sep)){
			if(str_t.indexOf(sep) != -1) als.add(new String(str_t.substring(0, str_t.indexOf(sep))));
			else als.add(new String(str_t));
			str_t = str_t.substring(str_t.indexOf(sep)+1);
		}
	}
	
	/**
	 * Splits String to ArrayList using separator String
	 * Notice: StringTokenizer can be used instead.
	 * @param str	in:string
	 * @param als	out:list
	 * @param sep	in:separator
	 * @param cnt	in:number of tokens
	 */
	public static void String2ArrayListOfString(String str, ArrayList<String> als, String sep, int cnt){
		als.clear();
		String str_t = new String(str);
		for(int i=0; i<cnt; i++){
			if(str_t.indexOf(sep) != -1) als.add(new String(str_t.substring(0, str_t.indexOf(sep))));
			else als.add(new String(str_t));
			str_t = str_t.substring(str_t.indexOf(sep)+1);
		}
	}
	
	/**
	 * Sort String array alphabetically
	 * @param array in:array to sort
	 */
	public static void sortArrayofString(String[] array){
		for(int i=0; i<array.length-1; i++){
			for(int j=i+1; j<array.length; j++){
				if(array[i].compareToIgnoreCase(array[j])>0){
					String temp = array[j];
					array[j] = array[i];
					array[i] = temp;
				}
			}
		}
	}

}
