package es.fap.simpleled.ui.documentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.Keyword;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import es.fap.simpleled.led.util.DocElemento;
import es.fap.simpleled.led.util.DocParametro;

public class JsonDocumentation {

	static Map<String, DocElemento> mapa;
	
	private static String firstLower(String name){
		return name.substring(0, 1).toLowerCase() + name.substring(1);
	}
	
	public static String getRuleName(EObject semantic){
		return semantic.getClass().getSimpleName().split("Impl")[0];
	}
	
	public static DocElemento getElemento(Keyword keyword, EObject semantic){
		initializeMap();
		String ruleName = getRuleName(semantic);
		String key = firstLower(ruleName);
		DocElemento elemento = mapa.get(key);
		if (keyword.getValue().equals(FapDocumentationProvider.docRules.get(ruleName))){
			elemento.keyword = keyword.getValue();
			return elemento;
		}
		return null;
	}
	
	public static DocParametro getParametro(Keyword keyword, EObject semantic){
		return getParametro(keyword.getValue(), semantic);
	}
	
	public static DocParametro getParametro(String keyword, EObject semantic){
		initializeMap();
		String key = firstLower(semantic.getClass().getSimpleName().split("Impl")[0]);
		DocElemento elemento = mapa.get(key);
		if (elemento == null){
			return null;
		}
		for (DocParametro p: elemento.parametros){
			if (p.keyword.equals(keyword)){
				return p;
			}
		}
		return null;
	}
	
	private static void initializeMap(){
//		mapa = null; // Para debuggear
		if (mapa == null){
			mapa = new HashMap<String, DocElemento>();
			Gson gson = new Gson();
			Type tDocElemento = new TypeToken<DocElemento>(){}.getType();
			Type tListString = new TypeToken<List<String>>(){}.getType();
			try {
				List<String> elementos = gson.fromJson(convertStreamToString(JsonDocumentation.class.getResourceAsStream("json/all.json")), tListString);
				for (String name: elementos){
					DocElemento e = gson.fromJson(convertStreamToString(JsonDocumentation.class.getResourceAsStream("json/" + name + ".json")), tDocElemento);
					mapa.put(e.nombre, e);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	public static String convertStreamToString(InputStream is){
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    try {
			while ((line = reader.readLine()) != null) {
			  sb.append(line + "\n");
			}
		    is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return sb.toString();
	  }
	
}
