package templates

import java.util.List;
import java.util.Set;
import java.util.Stack;

import es.fap.simpleled.led.*;
import generator.utils.*;

class GSubirFactura extends GSaveCampoElement {
	
	SubirFactura subirFactura;
	
	public GSubirFactura(SubirFactura subirFactura, GElement container) {
		super(subirFactura, container);
		this.subirFactura = subirFactura;
		campo = CampoUtils.create(subirFactura.campo);
	}
	
	public String view() {
		TagParameters params = new TagParameters()
		params.putStr("id", subirFactura.name)
		params.putStr("campo", campo.firstLower())
		if(subirFactura.requerido != null)
			params.put("requerido", subirFactura.requerido)
			
		return "#{fap.factura ${params.lista()} /}	";
	}
	
	public String validateCopy(Stack<Set<String>> validatedFields) {
		String validationSaveCampo = validate(validatedFields) + copy();
		String out ="""${validationSaveCampo}
					""";
		out += """
					if(${subirFactura.name}_documento == null) validation.addError("${subirFactura.name}_documento", "Archivo requerido");
					else if (${subirFactura.name}_documento.length() > properties.FapProperties.getLong("fap.file.maxsize")) validation.addError("${subirFactura.name}_documento", "Tamaño del archivo superior al máximo permitido ("+org.apache.commons.io.FileUtils.byteCountToDisplaySize(properties.FapProperties.getLong("fap.file.maxsize"))+")");
				""";
		return out;
	}
	
	public String saveCode() {
		String out = """if(!validation.hasErrors()) {
							if (${subirFactura.name}_documento != null) {
								try {
									services.GestorDocumentalService gestorDocumentalService = config.InjectorConfig.getInjector().getInstance(services.GestorDocumentalService.class);
									gestorDocumentalService.saveDocumentoTemporal(${campo.dbStr()}.documento, ${subirFactura.name}_documento);
								}
								catch(services.GestorDocumentalServiceException e) {
									play.Logger.error(e, "Error al subir el documento al Gestor Documental");
									validation.addError("", "Error al subir el documento al Gestor Documental");
								} catch (Exception e) {
									play.Logger.error(e, "Ex: Error al subir el documento al Gestor Documental");
									validation.addError("", "Error al subir el documento al Gestor Documental");
								}
						}
					}
					
					if (!validation.hasErrors()) {
						try {
							Facturae invoice32${subirFactura.name} = ${campo.dbStr()}.getFacturaeObject();
							FacturasFAP.getDataFromFacturae(invoice32${subirFactura.name}, ${campo.dbStr()});
						} catch (Exception e) {
							play.Logger.error("Error al extraer la información de la factura"+e);
							validation.addError("", "El formato del archivo de factura no es correcto");
						}
					}
					""";
	
		return out;
	}
	
	public String copy() {
		return GSaveCampoElement.copyCamposFiltrados(campo, ["documento.tipo","documento.descripcion"]);
	}
	
	public List<String> extraParams(){
		List<String> extraParams = super.extraParams();
		extraParams.add("java.io.File ${subirFactura.name}_documento");
		return extraParams;
	}
	
}
