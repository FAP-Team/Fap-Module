package templates;

import java.util.Set;
import java.util.Stack;

import es.fap.simpleled.led.*;
import generator.utils.CampoUtils
import generator.utils.Entidad;
import generator.utils.StringUtils;
import generator.utils.TagParameters;

public class GEditarArchivo extends GSaveCampoElement{

	EditarArchivo editarArchivo;
	
	public GEditarArchivo(EditarArchivo editarArchivo, GElement container){
		super(editarArchivo, container);
		this.editarArchivo = editarArchivo;
		campo = CampoUtils.create(editarArchivo.campo);
	}
	
	public String view(){
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
		params.put("conMetadato", true);	// editando siempre damos la posibilidad de añadir metadatos al documento
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
	}
	
	public String validateCopy(Stack<Set<String>> validatedFields){
		String codigo = """if(${campo.firstLower()}.estadoElaboracion != null) {
					${campo.dbStr()}.estadoElaboracion = ${campo.firstLower()}.estadoElaboracion;
					${campo.firstLower()}.sinMetadatos = false;
				}
				else {
					${campo.firstLower()}.sinMetadatos = true;
				}
				${campo.dbStr()}.sinMetadatos = ${campo.firstLower()}.sinMetadatos;
			""";	
		return codigo + validate(validatedFields) + """ ${campo.dbStr()}.tipo = ${campo.firstLower()}.tipo;
 ${campo.dbStr()}.descripcion = ${campo.firstLower()}.descripcion;
		""";
	}
	
}