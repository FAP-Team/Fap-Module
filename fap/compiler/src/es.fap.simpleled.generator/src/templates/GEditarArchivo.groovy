package templates;

import es.fap.simpleled.led.*;
import generator.utils.HashStack;
import generator.utils.HashStack.HashStackName;
import generator.utils.CampoUtils
import generator.utils.StringUtils;
import generator.utils.TagParameters;
import generator.utils.EntidadUtils;

public class GEditarArchivo {

	EditarArchivo editarArchivo;
	CampoUtils campo;
	
	public static String generate(EditarArchivo editarArchivo){
		def g = new GEditarArchivo();
		g.editarArchivo = editarArchivo;
		return g.view();
	}
	
	public String view(){
		// Añado la entidad que lo engloba a los parametros del Save
		campo = CampoUtils.create(editarArchivo.campo);
		EntidadUtils.addToSaveEntity(campo);
		HashStack.push(HashStackName.SAVE_CODE, this);
		TagParameters params = new TagParameters()
		if(editarArchivo.name != null)
			params.putStr("id", editarArchivo.name)
		params.putStr("campo", campo.firstLower())
		if(editarArchivo.requerido != null)
			params.put("requerido", editarArchivo.requerido)
		if (editarArchivo.tramite != null && editarArchivo.tramite.trim() != "")
			params.putStr("tramite", editarArchivo.tramite);
		if (editarArchivo.aportadoPor != null && editarArchivo.aportadoPor.trim() != "")
			params.putStr("aportadoPor", editarArchivo.aportadoPor)
		params.put("upload", false);
		params.put("download", true);
		params.putStr("tipo", "tipoCiudadano")
		return "#{fap.documento ${params.lista()} /}	";
	}
	
	public String saveCode(){
		return """
			if(!validation.hasErrors()){
				try {
					services.GestorDocumentalService gestorDocumentalService = config.InjectorConfig.getInjector().getInstance(services.GestorDocumentalService.class);
					gestorDocumentalService.updateDocumento(${campo.dbStr()});
				}
				catch(services.GestorDocumentalServiceException e){
                	play.Logger.error(e, "Error al actualizar el documento en el Gestor Documental");
					validation.addError("", "Error al actualizar el documento el documento en el Gestor Documental");
				}
			}
		""";
		return saveCode;
	}
	
}