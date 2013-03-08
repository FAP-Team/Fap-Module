package templates;

import es.fap.simpleled.led.EditarFactura
import generator.utils.*;


public class GEditarFactura extends GSaveCampoElement {
	
	EditarFactura editarFactura;
	
	public GEditarFactura(EditarFactura editarFactura, GElement container) {
		super(editarFactura, container);
		this.editarFactura = editarFactura;
		campo = CampoUtils.create(editarFactura.campo);
	}
	
	public String view() {
		TagParameters params = new TagParameters()
		if(editarFactura.name != null)
			params.putStr("id", editarFactura.name)
		params.putStr("campo", campo.firstLower())
		if(editarFactura.requerido != null)
			params.put("requerido", editarFactura.requerido)
		params.put("upload", false);
		params.put("download", true);
		return "#{fap.factura ${params.lista()} /}	";
	}
	
	public String validateCopy(Stack<Set<String>> validatedFields){
		return validate(validatedFields) + copy();
	}
	
	public String saveCode() {
		return """
			if(!validation.hasErrors()){
				try {
					services.GestorDocumentalService gestorDocumentalService = config.InjectorConfig.getInjector().getInstance(services.GestorDocumentalService.class);
					gestorDocumentalService.updateDocumento(${campo.dbStr()}.documento);
				}
				catch(services.GestorDocumentalServiceException e){
					play.Logger.error(e, "Error al actualizar el documento en el Gestor Documental");
					validation.addError("", "Error al actualizar el documento el documento en el Gestor Documental");
				}
			}
		""";
	}
	
	public String copy() {
		return GSaveCampoElement.copyCamposFiltrados(campo, ["documento.tipo","documento.descripcion"]);
	}
	
	
}