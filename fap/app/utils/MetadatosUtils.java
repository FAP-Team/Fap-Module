package utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.List;

import play.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.ListaMetadatos;

import messages.Messages;
import models.EsquemaMetadato;
import models.Metadato;
import models.MetadatoTipoPatron;
import models.MetadatoTipoTabla;
import models.Metadatos;
import models.MetadatosDocumento;

public class MetadatosUtils {
	
	public static class SimpleFactory {
		public static Metadato getMetadato(String nombreEnEsquema) {
			Metadato metadato = new Metadato();
			EsquemaMetadato esquema = EsquemaMetadato.get(nombreEnEsquema);
			
			if("tabla codificada".equals(esquema.tipoDeDato)) {
				metadato = new MetadatoTipoTabla();
			} else if(("texto".equals(esquema.tipoDeDato)) && (esquema.patron != null)) {
				metadato = new MetadatoTipoPatron();
			}

			metadato.nombre = nombreEnEsquema;
				
			return metadato;
		}
	}
	
	public static void esquemaFromJson(String jsonString) {
		StringReader stringReader = new StringReader(jsonString);
		JsonReader jsonReader = new JsonReader(stringReader);
		esquemaFromJson(jsonReader);
	}
	
	
	public static void esquemaFromJsonFile() {
		esquemaFromJsonFile(null);
	}
	
	public static void esquemaFromJsonFile(String path) {
		FileReader esquema;
		path = (path != null)? path : "conf/initial-data/esquema-metadatos.json";
		try {
			esquema = new FileReader(path);
			JsonReader jsonReader = new JsonReader(esquema);
			esquemaFromJson(jsonReader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Logger.error("Excepción al actualizar el esquema de metadatos. Fichero no encontrado");
		}
	}
	
	private static void esquemaFromJson(JsonReader jsonReader) {
		EsquemaMetadato esquemaMetadato = new EsquemaMetadato();
		JsonParser parser = new JsonParser();
		JsonElement jelement= parser.parse(jsonReader).
				getAsJsonObject().get("esquema metadatos");
		JsonArray jArray = jelement.getAsJsonArray();
		Gson gParser = new Gson();
		for (JsonElement elemento: jArray) {
			esquemaMetadato = gParser.fromJson(elemento, EsquemaMetadato.class);
			esquemaMetadato.save();
			Logger.info("Nuevo metadato: " + esquemaMetadato.nombre);
		}
	}
	
	// Lectura de los metadatos asociados a los tipos de documento desde el JSON
	public static void metadatosFromJsonFile(String path){
		FileReader metadatos;
	
		path = (path != null) ? path : "conf/initial-data/metadatos-documento.json";
		try {
			metadatos = new FileReader(path);
			JsonReader jsonReader = new JsonReader(metadatos);
			metadatosFromJson(jsonReader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.error("Excepción al actualizar la lista de metadatos. Fichero no encontrado");
		}
	}
	
	// Procesamos el fichero JSon con la lista de metadatos asociados y lo validamos contra el esquema de metadatos del Gobierno
	public static void metadatosFromJson(JsonReader jsonReader){
		MetadatosDocumento metadatosDocumento = new MetadatosDocumento();
				
		JsonParser parser = new JsonParser();
		JsonElement jelement = parser.parse(jsonReader).
				getAsJsonObject().get("metadatos documento");
		JsonArray jArray = jelement.getAsJsonArray();
		Gson gParser = new Gson();
		for(JsonElement elemento: jArray){
			metadatosDocumento = gParser.fromJson(elemento, MetadatosDocumento.class);
			// TODO: Validar metadatos antes de guardar
			boolean metadatosValidos = validarMetadatos(metadatosDocumento);
			if(metadatosValidos){
				//metadatosDocumento.save();
				Messages.ok("Metadatos actualizados correctamente para el tipo de documento "+metadatosDocumento.tipoDocumento);
				
				Messages.keep();
				
				Logger.info("Se han asociado correctamente los metadatos para el tipo de documento: "+metadatosDocumento.tipoDocumento);
			}else {
				
				Messages.error("No se han podido actualizar los metadatos");
				
				Messages.keep();
				
				Logger.error("Tipo de documento con metadatos erroneos "+metadatosDocumento.tipoDocumento);
			}
		
		}
	}

	// Metodo que valida los metadatos asociados al documento segun el esquema
	public static boolean validarMetadatos(MetadatosDocumento metadatosDocumento){
		// Lista con los metadatos asociados al tipo de documento para su validacion
		List<Metadato> listaMetadatos = metadatosDocumento.listaMetadatos;
		// Lista con los metadatos cargados desde el esquema
		for(Metadato metadato : listaMetadatos){
			Logger.info("Metadato: "+metadato.nombre+", Valor: "+metadato.valor);
			Metadato temp = SimpleFactory.getMetadato(metadato.nombre);
			temp.valor = metadato.valor;
			if(!temp.esValido()){
				Logger.error("Metadato NO valido: "+metadato.nombre+ ", con valor: "+metadato.valor);
				return false;
			}
		}		
		return true;
	}
}
