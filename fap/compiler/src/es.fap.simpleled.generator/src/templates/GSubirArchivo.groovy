package templates;

import java.util.Map;
import java.util.Set;
import java.util.Stack;

import es.fap.simpleled.led.*;
import generator.utils.*

public class GSubirArchivo extends GSaveCampoElement{

	SubirArchivo subirArchivo;
	
	public GSubirArchivo(SubirArchivo subirArchivo, GElement container){
		super(subirArchivo, container);
		this.subirArchivo = subirArchivo;
		campo = CampoUtils.create(subirArchivo.campo);
	}
	
	public String view(){
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
		if(subirArchivo.listarDocumentosSubidos != null) 
			params.put("listarDocumentosSubidos", subirArchivo.listarDocumentosSubidos)
		if(subirArchivo.conMetadatos != null)
			params.put("conMetadatos", subirArchivo.conMetadatos)
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
	
	public String validateCopy(Stack<Set<String>> validatedFields) {
		String checkFile = "";
		String validationSaveCampo = validate(validatedFields) + copy();
		if (subirArchivo.mimes.size > 0){
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
				String mimeType = play.libs.MimeTypes.getMimeType(${subirArchivo.name}.getAbsolutePath());
				String type = mimeType.split("/")[0];
				if (${check})
					validation.addError("${subirArchivo.name}", "El tipo mime \\"" + mimeType + "\\" del documento a incorporar, no es válido. Compruebe los formatos de documentos aceptados.");
			""";
		}
		else if (subirArchivo.extensiones.size > 0){
			String check = "";
			for (String extension: subirArchivo.extensiones){
				if (!check.equals(""))
					check += " && ";
				check += """!extension.equalsIgnoreCase("${extension}")""";
			}
			checkFile = """
				String extension = GestorDocumentalUtils.getExtension(${subirArchivo.name});
				if (${check})
					validation.addError("${subirArchivo.name}", "La extensión \\"" + extension + "\\" del documento a incorporar, no es válida. Compruebe los formatos de documentos aceptados.");
			""";
		}
		else{
			checkFile = """String extension = GestorDocumentalUtils.getExtension(${subirArchivo.name});
				String mimeType = play.libs.MimeTypes.getMimeType(${subirArchivo.name}.getAbsolutePath());
				if (!utils.GestorDocumentalUtils.acceptExtension(extension))
					validation.addError("${subirArchivo.name}", "La extensión \\"" + extension + "\\" del documento a incorporar, no es válida. Compruebe los formatos de documentos aceptados.");
				if (!utils.GestorDocumentalUtils.acceptMime(mimeType))
					validation.addError("${subirArchivo.name}", "El tipo mime \\"" + mimeType + "\\" del documento a incorporar, no es válido. Compruebe los formatos de documentos aceptados.");""";
		}

		String out ="""${validationSaveCampo}
					""";
		if((subirArchivo.listarDocumentosSubidos != null) && subirArchivo.listarDocumentosSubidos) {
			out += """if((${campo.firstLower()}.uri != null) && (!${campo.firstLower()}.uri.isEmpty())) {
							services.GestorDocumentalService gestorDocumentalService = config.InjectorConfig.getInjector().getInstance(services.GestorDocumentalService.class);
							try {
								gestorDocumentalService.duplicarDocumentoSubido(${campo.firstLower()}.uri, ${campo.firstLower()}.descripcion, ${campo.dbStr()});
							} catch (Exception e) {
								log.error("Ha habido un error al subir el documento "+e.getMessage());
								Messages.error("Ha habido un error al subir el documento");
								Messages.keep();
							}
						} else {""";
		}
		out += """
				int DESCMAXIMA = 255;
				if (${campo.firstLower()}.descripcion.length() > DESCMAXIMA){
					validation.addError("${campo.firstLower()}.descripcion", "La descripción excede el tamaño máximo permitido de "+DESCMAXIMA+" caracteres");
				}
				if(${subirArchivo.name} == null) validation.addError("${subirArchivo.name}", "Archivo requerido");
				else if (${subirArchivo.name}.length() > properties.FapProperties.getLong("fap.file.maxsize")) validation.addError("${subirArchivo.name}", "Tamaño del archivo superior al máximo permitido ("+org.apache.commons.io.FileUtils.byteCountToDisplaySize(properties.FapProperties.getLong("fap.file.maxsize"))+")");
				else{
					${checkFile}
				}""";
        if (subirArchivo.conMetadatos) {
            out += "if ((metadatos != null) && (metadatos.size() >= 0)) {for (Metadato md : metadatos) { if" +
                    "(!md.esValido()) { validation.addError(\"${campo.firstLower()}.metadatos\"," +
                    " \"El metadato \" + md.nombre + \" no tiene un " +
                    "valor válido\");}}}"
        }
		
		if((subirArchivo.listarDocumentosSubidos != null) && subirArchivo.listarDocumentosSubidos)
			out += """}""";
				
		return out;
	}
	
	public String saveCode(){
		String out = """if(!validation.hasErrors()) {""";
		if((subirArchivo.listarDocumentosSubidos != null) && subirArchivo.listarDocumentosSubidos)
			out += """if (${subirArchivo.name} != null && ${campo.firstLower()}.uri != null && ${campo.firstLower()}.uri.isEmpty()) {""";
		else
			out += """if (${subirArchivo.name} != null) {""";
		
		out += """
					try {
						services.GestorDocumentalService gestorDocumentalService = config.InjectorConfig.getInjector().getInstance(services.GestorDocumentalService.class);
						gestorDocumentalService.saveDocumentoTemporal(${campo.dbStr()}, ${subirArchivo.name});
					}
					catch(services.GestorDocumentalServiceException e) {
						play.Logger.error(e, "Error al subir el documento al Gestor Documental");
						validation.addError("", "Error al subir el documento al Gestor Documental");
					} catch (Exception e) {
						play.Logger.error(e, "Ex: Error al subir el documento al Gestor Documental");
						validation.addError("", "Error al subir el documento al Gestor Documental");
					}
				}
			}""";
			
		return out;
	}
	
	public List<String> extraParams(){
		List<String> extraParams = super.extraParams();
		extraParams.add("java.io.File ${subirArchivo.name}");
		if (subirArchivo.conMetadatos == true) {
			extraParams.add("List<Metadato> metadatos");
		}
		return extraParams;
	}
	
	public String copy(){
		return GSaveCampoElement.copyCamposFiltrados(campo, ["tipo","descripcion"]);
	}
	
}
