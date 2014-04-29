package utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import messages.Messages;
import models.DefinicionMetadatos;
import models.TipoDocumento;
import services.TiposDocumentosService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import config.InjectorConfig;

import static config.InjectorConfig.getInjector;

public class MetadatosUtils {
	
	@Inject
	static TiposDocumentosService tiposDocumentosService; 
	
	public static void cargarJsonMetadatosTipoDocumento() {
		cargarJsonMetadatosTipoDocumento(null);
	}
	
	public static void cargarJsonMetadatosTipoDocumento(String path) {
		FileReader metadatosReader;
		path = (path != null)? path : "conf/initial-data/metadatos-tipodocumento.json";
		try {
			metadatosReader = new FileReader(path);
			JsonReader jsonReader = new JsonReader(metadatosReader);
			metadatosFromJson(jsonReader);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			play.Logger.error("Excepción al actualizar el esquema de metadatos. fichero no encontrado");
		}
		
	}

	public static void cargarStringJsonMetadatosTipoDocumento(String jsonString) {
		JsonReader jsonReader = new JsonReader(new StringReader(jsonString));
		metadatosFromJson(jsonReader);
	}
	
	private static void metadatosFromJson(JsonReader jsonReader) {
		JsonParser parser = new JsonParser();
		JsonElement jelement = parser.parse(jsonReader).getAsJsonObject().get("metadatos documento");
		JsonArray tiposDocumentosJson = jelement.getAsJsonArray();
		for (JsonElement tipoDocumentoJson : tiposDocumentosJson) {
			String uriTipoDocumento = tipoDocumentoJson.getAsJsonObject().get("tipoDocumento").getAsString();
			TipoDocumento tipoDocumento = (TipoDocumento)TipoDocumento.find("byUri", uriTipoDocumento).first();
			if (tipoDocumento == null) {
				throw new NullPointerException("Tipo de documento no existente");
			}
			JsonArray listaMetadatosJson = tipoDocumentoJson.getAsJsonObject().get("listaMetadatos").getAsJsonArray();
			parseMetadatosDocumentoJson(tipoDocumento, listaMetadatosJson);
		}

		
	}

	private static void parseMetadatosDocumentoJson(TipoDocumento tipoDocumento, JsonArray listaMetadatosJson) {
		for (JsonElement metadatoJson : listaMetadatosJson){
			String nombre = metadatoJson.getAsJsonObject().get("nombre").getAsString();
			String valor = metadatoJson.getAsJsonObject().get("valor").getAsString();
			play.Logger.info("Metadato %s con valor %s desde Json para tipoDocumento %s", nombre, valor, tipoDocumento.uri);
			
			DefinicionMetadatos defMetadato = tipoDocumento.getDefinicionMetadatos(nombre);
			if (defMetadato == null) {
				throw new IllegalArgumentException(String.format(
						"Definición de metadato '%s' no encontrada para el tipo de documento '%s'",
						nombre, 
						tipoDocumento.uri));
			}
			if(defMetadato.esValido(valor)) {
				defMetadato.valoresPorDefecto.add(valor);
				defMetadato.save();
			} else {
				throw new IllegalArgumentException(String.format(
						"Valor '%s' de metadato '%s' no válido", valor, nombre));
			}
		}
	}

	public static void cargarDefinicionesMetadatosPorUri(List<String> uris) {
        if (tiposDocumentosService == null) {
            tiposDocumentosService = getInjector().getInstance(TiposDocumentosService.class);
        }
		for (String uri : uris) {
			TipoDocumento tipoDoc = TipoDocumento.find("byUri", uri).first();
			if (tipoDoc == null) {
				Messages.error("Tipo de documento " + uri + " no encontrado");
				throw new IllegalArgumentException("Tipo de documento " + uri + " no encontrado");
			}
			List<DefinicionMetadatos> definiciones = 
					tiposDocumentosService.getDefinicionesMetadatos(uri);
			for (DefinicionMetadatos def : definiciones) {
                play.Logger.info("Cargando definición %s para uri %s", def.nombre, uri);
				def.save();
			}
			tipoDoc.definicionMetadatos.addAll(definiciones);
			tipoDoc.save();	
		}
	}
	
	public static void cargarDefinicionesMetadatosPorTipo(List<TipoDocumento> tiposDocumento) {
		List<String> uris = new ArrayList<String>();
		for (TipoDocumento tipoDoc : tiposDocumento) {
			uris.add(tipoDoc.uri);
		}
		cargarDefinicionesMetadatosPorUri(uris);
	}
}
