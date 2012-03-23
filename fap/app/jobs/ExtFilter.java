package jobs;

import java.io.*; 

public class ExtFilter implements FilenameFilter { 

	String ext;
	
	public ExtFilter (String ext) {
		this.ext = "." + ext;
	}

	public boolean accept(File dir, String name) { 
		return name.endsWith(ext);
	}
}
