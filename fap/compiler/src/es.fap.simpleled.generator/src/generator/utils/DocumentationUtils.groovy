package generator.utils

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;
import java.io.FilenameFilter;

import com.google.gson.Gson;
import es.fap.simpleled.led.util.DocElemento
import es.fap.simpleled.led.util.DocParametro
import es.fap.simpleled.led.util.LedDocumentationUtils;

public class DocumentationUtils {

	static List<String> elementos = new ArrayList<String>();
	
	public static void makeDocumentation() {
		try{
			File dir = new File(FileUtils.getRoute('FAP_DOCUMENTATION'));
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File d, String name) {
					return name.startsWith("dsl-");
				}
			};
			File[] files = dir.listFiles(filter);
			for (File file: files) {
				getElemento(file.getAbsolutePath());
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		FileUtils.overwrite(FileUtils.getRoute('JSON_DOCUMENTATION'), "all.json", new Gson().toJson(elementos.sort()));
	}
	
	private static void getElemento (String filename) {
		
		boolean findDescriptionInit = false;
		boolean findDescriptionEnd = false;
		boolean findParamsInit = false;
		boolean findParamsEnd = false;
		DocElemento elemento = new DocElemento();
		String descripcion = "";
		String name;
		
		try {
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream input = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String strLine;
			
			// Obtenemos el nombre del elemento
			Pattern pa = Pattern.compile(".*.dsl-(.*).textile\$");
			Matcher ma = pa.matcher(filename);
			if (!ma.matches()) {
				return;
			}
			elemento.nombre = ma.group(1);
			
			// Buscamos la descripción primero
			while ((strLine = br.readLine()) != null)   {
				if (strLine.trim().equals("h2. Descripción")) {
					findDescriptionInit = true;
					continue;
				} else if ((findDescriptionInit) && (strLine.trim().startsWith("h2") || strLine.trim().startsWith("bc"))) {
					findDescriptionEnd = true;
				}
				
				if ((findDescriptionInit) && (!findDescriptionEnd)) {
					descripcion += strLine;
				}
				
				// Buscamos los parametros
				if (strLine.trim().startsWith("h2. Parámetros")) {
					findParamsInit = true;
					continue;
				} else if ((findParamsInit) && strLine.trim().startsWith("h2")) {
					findParamsEnd = true;
					break;
				}
				
				if ((findParamsInit) && (!findParamsEnd)) {
					processParam(elemento, strLine);
				}

			}
			elemento.descripcion = processText(elemento, descripcion);
			FileUtils.overwrite(FileUtils.getRoute('JSON_DOCUMENTATION'), "${elemento.nombre}.json", new Gson().toJson(elemento));
			elementos.add(elemento.nombre);
			input.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private static void processParam(DocElemento elemento, String line){
		Pattern p = Pattern.compile(".*?[*][ \t\n\f\r]*[*][*](.*?)[*][*](.*?):(.*)");
		Matcher matcher = p.matcher(line);
		if (matcher.matches()) {
			if (!line.trim().equals("")) {
				DocParametro param = new DocParametro();
				param.keyword = matcher.group(1).trim().split(" ")[0].replace("_[", "").replace("]_", "");
				if (param.keyword == null){
					param.keyword = "";
				}
				param.nombre = matcher.group(1).trim();
				param.descripcion = matcher.group(3).trim();
				param.descripcion = processText(elemento, param.descripcion);
				param.tipo = matcher.group(2).trim().equals("") ? "requerido" : matcher.group(2).trim().replaceAll("[()]", "");
				elemento.parametros.add(param);
			}
		}
	}

	private static String processText(DocElemento elemento, String text){
		Pattern p = Pattern.compile("\"(.*?)\":(#?(\\w|-)+)");
		Matcher matcher = p.matcher(text);
		while (matcher.find()){
			if (text.contains("#")){
				text = text.replace(matcher.group(), LedDocumentationUtils.getHref("${elemento.nombre}${matcher.group(2)}", matcher.group(1)));
			}
			else{
				text = text.replace(matcher.group(), LedDocumentationUtils.getHrefNoDsl("${matcher.group(2)}", matcher.group(1)));
			}
		}
		return text;
	}
}
