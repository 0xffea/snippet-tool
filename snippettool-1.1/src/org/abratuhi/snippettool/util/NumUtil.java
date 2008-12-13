package org.abratuhi.snippettool.util;

public class NumUtil {
	
	public static int hex2dec(String s){
		return Integer.parseInt(s, 16);
	}
	
	public static String dec2hex(int n){
		return Integer.toHexString(n);
	}

}
