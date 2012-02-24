package utils;

import java.util.regex.Pattern;

import properties.FapProperties;

public class MimeUtils {
	
	public static boolean acceptMime(String mimeType){
		String type = mimeType.split("/")[0];
		if (FapProperties.get("fap.gestordocumental.mimes") == null)
			return true;
		Pattern pattern = Pattern.compile("[\\w-]+/(\\*|[\\w-]+)");
		for (String mime: FapProperties.get("fap.gestordocumental.mimes").split(",")){
			mime = mime.trim();
			if (pattern.matcher(mime).matches()){
				if (mime.split("/")[1].equals("*")){
					if (type.equals(mime.split("/")[0]))
						return true;
				}
				else{
					if (mime.equals(mimeType))
						return true;
				}
			}
		}
		return false;
	}
	
}
