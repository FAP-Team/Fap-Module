package templates;

import utils.StringUtils;
import es.fap.simpleled.led.*;
import generator.utils.HashStack;
import generator.utils.HashStack.HashStackName;
import generator.utils.CampoUtils
import generator.utils.StringUtils;
import generator.utils.TagParameters;
import generator.utils.EntidadUtils;

public class GSubirArchivo {

	SubirArchivo subirArchivo;
	CampoUtils campo;
	
	public static String generate(SubirArchivo subirArchivo){
		def g = new GSubirArchivo();
		g.subirArchivo = subirArchivo;
		return g.view();
	}
	
	public String view(){
		// Añado la entidad que lo engloba a los parametros del Save
		campo = CampoUtils.create(subirArchivo.campo);
		EntidadUtils.addToSaveEntity(campo);
		HashStack.push(HashStackName.SAVE_EXTRA, "java.io.File ${subirArchivo.name}")
		HashStack.push(HashStackName.SAVE_CODE, this);
		HashStack.push(HashStackName.SUBIR_ARCHIVO, subirArchivo.name)
		TagParameters params = new TagParameters()
		params.putStr("id", subirArchivo.name)
		params.putStr("tipo", "tipoCiudadano")
		params.putStr("campo", campo.firstLower())
		params.putStr("accept", subirArchivo.mimes.join(","))
		if(subirArchivo.requerido != null)
			params.put("requerido", subirArchivo.requerido)
		if (subirArchivo.tramite != null && subirArchivo.tramite.trim() != "")
			params.putStr("tramite", subirArchivo.tramite);
		if (subirArchivo.aportadoPor != null && subirArchivo.aportadoPor.trim() != "")
			params.putStr("aportadoPor", subirArchivo.aportadoPor)
		return "#{fap.documento ${params.lista()} /}	";
	}
	
	public List<String> typesAccepted(){
		List<String> types = new ArrayList<String>();
		for (String mime: subirArchivo.mimes){
			if (mime.split("/")[1].equals("*"))
				types.add(mime.split("/")[0]);
		}
		return types;
	}
	
	public List<String> mimesAccepted(){
		List<String> mimes = new ArrayList<String>();
		for (String mime: subirArchivo.mimes){
			if (!mime.split("/")[1].equals("*"))
				mimes.add(mime);
		}
		return mimes;
	}
	
	public String saveCode(){
		String mimesCheck = "";
		if (subirArchivo.mimes.size > 0){
			for (String type: typesAccepted()){
				if (!mimesCheck.equals(""))
					mimesCheck += " && ";
				mimesCheck += """!type.equals("${type}")""";
			}
			for (String mime: mimesAccepted()){
				if (!mimesCheck.equals(""))
					mimesCheck += " && ";
				mimesCheck += """!mimeType.equals("${mime}")""";
			}
		}
		else
			mimesCheck = "!utils.MimeUtils.acceptMime(mimeType)";
		return """
			if(${subirArchivo.name} == null)
				validation.addError("${subirArchivo.name}", "Archivo requerido");
			else{
				String mimeType = play.libs.MimeTypes.getMimeType(${subirArchivo.name}.getAbsolutePath());
				String type = mimeType.split("/")[0];
				if (${mimesCheck})
					validation.addError("${subirArchivo.name}", "El tipo mime del archivo no es aceptado por el servidor");
			}
			if(!validation.hasErrors()){
				try {
					services.GestorDocumentalService gestorDocumentalService = config.InjectorConfig.getInjector().getInstance(services.GestorDocumentalService.class);
					gestorDocumentalService.saveDocumentoTemporal(${campo.dbStr()}, ${subirArchivo.name});
				}
				catch(services.GestorDocumentalServiceException e){
                	play.Logger.error(e, "Error al subir el documento al Gestor Documental");
					validation.addError("", "Error al subir el documento al Gestor Documental");
				}
			}
		""";
	}
	
}
