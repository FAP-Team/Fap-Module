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
	
	public String saveCode(){
		String saveCode = """
		if(${subirArchivoAed.name} == null) validation.addError("${subirArchivoAed.name}", "Archivo requerido");

		if(!validation.hasErrors()){
			try {
				aed.AedClient.saveDocumentoTemporal(db${campo.str}, ${subirArchivoAed.name});
			}catch(es.gobcan.eadmon.aed.ws.AedExcepcion e){
				validation.addError("", "Error al subir el documento al Archivo Electrónico");
			}
		}
		"""
		return saveCode;
	}
	
}
