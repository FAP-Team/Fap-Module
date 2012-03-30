package templates

import es.fap.simpleled.led.*
import generator.utils.*

public class GFecha extends GSaveCampoElement{

	Fecha fecha;
	
	public GFecha(Fecha fecha, GElement container){
		super(fecha, container);
		this.fecha = fecha;
		campo = CampoUtils.create(fecha.campo);
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		params.putStr("campo", campo.firstLower())
		if(fecha.titulo != null)
			params.putStr("titulo", fecha.titulo)
		
		if(fecha.requerido)
			params.put "requerido", true
		
		if (fecha.ayuda != null) {
			if ((fecha.tipoAyuda != null) && (fecha.tipoAyuda.type.equals("propover")))
				params.put "ayuda", "tags.TagAyuda.popover('${fecha.ayuda}')"
			else
				params.put "ayuda", "tags.TagAyuda.texto('${fecha.ayuda}')"
		}
			
		return """
			#{fap.fecha ${params.lista()} /}		
		""";
	}
	
}
