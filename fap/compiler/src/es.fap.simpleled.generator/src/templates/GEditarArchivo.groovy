package templates;

import java.util.Set;
import java.util.Stack;

import es.fap.simpleled.led.*;
import es.fap.simpleled.led.util.ModelUtils;
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
		Pagina pagina = ModelUtils.getContenedorPadre(campo.campo, LedFactory.eINSTANCE.getLedPackage().getPagina());
		String result = "";
		if ((pagina != null) && (pagina.copia)){
			campo.ultimaEntidad.attributes.eachWithIndex { item, i ->
				if (!item.name.startsWith("id"))
					result += """if ((db${campo.str}.${item.name} != null) && (${campo.firstLower()}.${item.name} != null) && (!${campo.firstLower()}.${item.name}.toString().equals(db${campo.str}.toString()))){
					valoresNuevos = new ArrayList<String>();
					if (db${campo.str}.${item.name} != null)
						valoresAntiguos.add(db${campo.str}.${item.name}.toString());
					valoresNuevos = new ArrayList<String>();
					hayModificaciones = true;
					peticionModificacion.setValorModificado("${campo.firstLower()}.${item.name}", valoresAntiguos, valoresNuevos);
					hayModificaciones = true;
					db${campo.str}.${item.name} = ${campo.firstLower()}.${item.name};
				}
			"""
			}
			return result;
		}else
			return validate(validatedFields) + """ int DESCMAXIMA = 255;  
 ${campo.dbStr()}.tipo = ${campo.firstLower()}.tipo; 
 if (${campo.firstLower()}.descripcion.length() > DESCMAXIMA){
    validation.addError("${campo.firstLower()}.descripcion", "La descripción excede el tamaño máximo permitido de "+DESCMAXIMA+" caracteres");
 }
    ${campo.dbStr()}.descripcion = ${campo.firstLower()}.descripcion;
		""";
	}
	
}