package src.util.num;

public class NumUtil {
	
	public static int hex2dec(String s){
		return Integer.parseInt(s, 16);
	}
	
	public static String dec2hex(int n){
		return Integer.toHexString(n);
	}
	
	public static int myIndex(String[] file_ids, String id){
		//System.out.println("calling myIndex() with id="+id);
		int[] f = new int[file_ids.length];
		int i, index, maxmin;
		int index_supplementary_file = -1;
		
		//convert to int
		for(int j=0; j<file_ids.length; j++){
			// do not try to convert unicode_suppl_1000.xml
			if(file_ids[j].equals("suppl")){
				System.out.println("found supplementary xml db file");
				index_supplementary_file = j;
				continue;
			}
			// convert
			f[j] = hex2dec(file_ids[j]);
		}
		
		//search
		i = hex2dec(id);		
		index = -1;
		maxmin = 0;
		for(int j=0; j<f.length; j++){
			if(f[j]<=i && f[j]>=maxmin) {index = j; maxmin = f[index];}
		}
		
		// handle H, D - stored in unicode_suppl_1000.xml
		if(id.equals("24b9") || id.equals("24bd")){
			index = index_supplementary_file;
		}
		
		//
		return index;
	}

}
