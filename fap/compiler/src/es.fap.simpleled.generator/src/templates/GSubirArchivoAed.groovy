package templates;

import utils.StringUtils;
import es.fap.simpleled.led.*;
import es.fap.simpleled.led.impl.CampoListaImpl;
import generator.utils.HashStack;
import generator.utils.HashStack.HashStackName;
import generator.utils.CampoUtils
import generator.utils.StringUtils;
import generator.utils.TagParameters;
import generator.utils.EntidadUtils;

public class GSubirArchivoAed {

	SubirArchivoAed subirArchivoAed
	CampoUtils campo;
	
	public static String generate(SubirArchivoAed subirArchivoAed){
		def g = new GSubirArchivoAed();
		g.subirArchivoAed = subirArchivoAed;
		return g.view();
	}
	
	public String view(){
		// Añado la entidad que lo engloba a los parametros del Save
		campo = CampoUtils.create(subirArchivoAed.campo);
		EntidadUtils.addToSaveEntity(campo);
		
		HashStack.push(HashStackName.SAVE_EXTRA, "java.io.File ${subirArchivoAed.name}")
		HashStack.push(HashStackName.SAVE_CODE, this);
		HashStack.push(HashStackName.SUBIR_ARCHIVO, subirArchivoAed.name)
		
		TagParameters params = new TagParameters()
		
		params.putStr("id", subirArchivoAed.name)
		params.putStr("tipo", "tipoCiudadano")
		params.putStr("campo", campo.firstLower())
		if(subirArchivoAed.requerido != null)
			params.put("requerido", subirArchivoAed.requerido)
			
		if (subirArchivoAed.tramite != null && subirArchivoAed.tramite.trim() != "")
			params.putStr("tramite", subirArchivoAed.tramite);
		
		if (subirArchivoAed.aportadoPor != null && subirArchivoAed.aportadoPor.trim() != "")
			params.putStr("aportadoPor", subirArchivoAed.aportadoPor)
		
		return "#{fap.uploadAed ${params.lista()} /}	"
	}
	
	public List<String> typesAccepted(){
		List<String> types = new ArrayList<String>();
		for (String mime: subirArchivoAed.mimes){
			if (mime.split("/")[1].equals("*"))
				types.add(mime.split("/")[0]);
		}
		return types;
	}

	public List<String> mimesAccepted(){
		List<String> mimes = new ArrayList<String>();
		for (String mime: subirArchivoAed.mimes){
			if (!mime.split("/")[1].equals("*"))
				mimes.add(mime);
		}
		return mimes;
	}
	
	public String saveCode(){
		String checkFile = "";
		if (subirArchivoAed.mimes.size > 0){
			String check = "";
			for (String type: typesAccepted()){
				if (!check.equals(""))
					check += " && ";
				check += """!type.equals("${type}")""";
			}
			for (String mime: mimesAccepted()){
				if (!check.equals(""))
					check += " && ";
				check += """!mimeType.equals("${mime}")""";
			}
			checkFile = """
				String mimeType = play.libs.MimeTypes.getMimeType(${subirArchivoAed.name}.getAbsolutePath());
				String type = mimeType.split("/")[0];
				if (${check})
					validation.addError("${subirArchivoAed.name}", "El tipo mime \\"" + mimeType + "\\" del documento a incorporar, no es válido. Compruebe los formatos de documentos aceptados.");
			""";
		}
		else if (subirArchivoAed.extensiones.size > 0){
			String check = "";
			for (String extension: subirArchivoAed.extensiones){
				if (!check.equals(""))
					check += " && ";
				check += """!extension.equals("${extension.toLowerCase()}")""";
			}
			checkFile = """
				String extension = GestorDocumentalUtils.getExtension(${subirArchivoAed.name});
				if (${check})
					validation.addError("${subirArchivoAed.name}", "La extensión \\"" + extension + "\\" del documento a incorporar, no es válida. Compruebe los formatos de documentos aceptados.");
			""";
		}
		else{
			checkFile = """
				String extension = GestorDocumentalUtils.getExtension(${subirArchivoAed.name});
				String mimeType = play.libs.MimeTypes.getMimeType(${subirArchivoAed.name}.getAbsolutePath());
				if (!utils.GestorDocumentalUtils.acceptExtension(extension))
					validation.addError("${subirArchivoAed.name}", "La extensión \\"" + extension + "\\" del documento a incorporar, no es válida. Compruebe los formatos de documentos aceptados.");
				if (!utils.GestorDocumentalUtils.acceptMime(mimeType))
					validation.addError("${subirArchivoAed.name}", "El tipo mime \\"" + mimeType + "\\" del documento a incorporar, no es válido. Compruebe los formatos de documentos aceptados.");
			""";
		}
		
		return """
			if(${subirArchivoAed.name} == null) validation.addError("${subirArchivoAed.name}", "Archivo requerido");
			else{
				${checkFile}
			}
			if(!validation.hasErrors()){
				try {
					aed.AedClient.saveDocumentoTemporal(db${campo.str}, ${subirArchivoAed.name});
				}
				catch(es.gobcan.eadmon.aed.ws.AedExcepcion e){
					validation.addError("", "Error al subir el documento al Archivo Electrónico");
				}
			}
		""";
	}
	
}
