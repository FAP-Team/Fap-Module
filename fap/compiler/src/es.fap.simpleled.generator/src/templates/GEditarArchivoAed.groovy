package templates;

import es.fap.simpleled.led.*;
import generator.utils.HashStack;
import generator.utils.HashStack.HashStackName;
import generator.utils.CampoUtils
import generator.utils.StringUtils;
import generator.utils.TagParameters;
import generator.utils.EntidadUtils;

public class GEditarArchivoAed {

	EditarArchivoAed editarArchivoAed
	
	public static String generate(EditarArchivoAed editarArchivoAed){
		def g = new GEditarArchivoAed();
		g.editarArchivoAed = editarArchivoAed;
		return g.view();
	}
	
	
	public String view(){
		EntidadUtils.addToSaveEntity("Documento");
		
		HashStack.push(HashStackName.SAVE_CODE, this);
		
		TagParameters params = new TagParameters()
		
		if(editarArchivoAed.name != null)
			params.putStr("id", editarArchivoAed.name)
			
		CampoUtils campo = CampoUtils.create(editarArchivoAed.campo);
		params.putStr("campo", campo.firstLower())
		
		if(editarArchivoAed.requerido != null)
			params.put("requerido", editarArchivoAed.requerido)
		if (editarArchivoAed.tramite != null && editarArchivoAed.tramite.trim() != "")
			params.putStr("tramite", editarArchivoAed.tramite);
		if (editarArchivoAed.aportadoPor != null && editarArchivoAed.aportadoPor.trim() != "")
			params.putStr("aportadoPor", editarArchivoAed.aportadoPor)
		
		params.put("upload", false);
		params.put("download", true);
		params.putStr("tipo", "tipoCiudadano")
		
		return "#{fap.uploadAed ${params.lista()} /}	"
	}
	
	public String saveCode(){
		String saveCode = """
		// No es necesario ya ponerlo aqui
		// dbDocumento.tipo = documento.tipo;
		// dbDocumento.descripcion = documento.descripcion;
		if(!validation.hasErrors()){
			try {
				aed.AedClient.actualizarTipoDescripcion(dbDocumento);
			}catch(es.gobcan.eadmon.aed.ws.AedExcepcion e){
				validation.addError("", "Error al actualizar el tipo y la descripción en el AED");
			}
		}
		"""
		return saveCode;
	}
	
}
