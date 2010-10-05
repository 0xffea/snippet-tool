package org.stonesutras.snippettool.util;

import java.util.HashMap;

public class GetOpts {
	
	HashMap<String, String> map = new HashMap<String, String>();
	
	public GetOpts(String[] args){
		for(int i=0; i<args.length; i++){
			if(args[i].startsWith("-") && i<args.length-1 && !args[i+1].startsWith("-")){
				map.put(args[i].substring(1), args[i+1]);
			}
		}
	}
	
	public String getOpt(String opt){
		return map.get(opt);
	}

}
