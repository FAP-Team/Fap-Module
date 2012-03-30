package templates;

import utils.StringUtils;
import es.fap.simpleled.led.*;
import generator.utils.CampoUtils
import generator.utils.Entidad;
import generator.utils.StringUtils;
import generator.utils.TagParameters;

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
		String checkFile = "";
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
				check += """!extension.equals("${extension}")""";
			}
			checkFile = """
				String extension = GestorDocumentalUtils.getExtension(${subirArchivo.name});
				if (${check})
					validation.addError("${subirArchivo.name}", "La extensión \\"" + extension + "\\" del documento a incorporar, no es válida. Compruebe los formatos de documentos aceptados.");
			""";
		}
		else{
			checkFile = """
				String extension = GestorDocumentalUtils.getExtension(${subirArchivo.name});
				String mimeType = play.libs.MimeTypes.getMimeType(${subirArchivo.name}.getAbsolutePath());
				if (!utils.GestorDocumentalUtils.acceptExtension(extension))
					validation.addError("${subirArchivo.name}", "La extensión \\"" + extension + "\\" del documento a incorporar, no es válida. Compruebe los formatos de documentos aceptados.");
				if (!utils.GestorDocumentalUtils.acceptMime(mimeType))
					validation.addError("${subirArchivo.name}", "El tipo mime \\"" + mimeType + "\\" del documento a incorporar, no es válido. Compruebe los formatos de documentos aceptados.");
			""";
		}
		return """
			if(${subirArchivo.name} == null)
				validation.addError("${subirArchivo.name}", "Archivo requerido");
			else{
				${checkFile}
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
	
	public List<String> extraParams(){
		List<String> extraParams = super.extraParams();
		extraParams.add("java.io.File ${subirArchivo.name}");
		return extraParams;
	}
	
	public String copy(){
		return GSaveCampoElement.copyCamposFiltrados(campo, ["tipo","descripcion"]);
	}
	
}
