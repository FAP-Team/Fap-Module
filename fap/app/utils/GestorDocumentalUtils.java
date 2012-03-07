package utils;

import java.io.File;
import java.util.regex.Pattern;

import properties.FapProperties;

public class GestorDocumentalUtils {
	
	/*
	 * Comprueba si en la property fap.gestordocumental.mimes está configurado
	 * que se acepte el tipo mime que se recibe por parámetro.
	 */
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
	
	/*
	 * Comprueba si en la property fap.gestordocumental.extensiones está configurado
	 * que se acepte la extensión de archivo que se recibe por parámetro.
	 */
	public static boolean acceptExtension(String extension){
		if (FapProperties.get("fap.gestordocumental.extensions") == null)
			return true;
		for (String ext: FapProperties.get("fap.gestordocumental.extensions").split(",")){
			if (extension.toLowerCase().equals(ext.trim().toLowerCase()))
				return true;
		}
		return false;
	}
	
	/*
	 * C:\documentos\doc.txt	--> "txt"
	 * C:\documentos\doc  		--> ""
	 */
	public static String getExtension(File file){
		String name = file.getName();
		int dot = name.lastIndexOf(".");
		if (dot != -1)
			return name.substring(dot + 1);
		return "";
	}
	
}
