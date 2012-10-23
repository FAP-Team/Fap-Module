package templates;

import es.fap.simpleled.led.*
import generator.utils.*

public class GCCC extends GSaveCampoElement{
	
	CCC ccc;
	
	public GCCC(CCC ccc, GElement container){
		super(ccc, container);
		this.ccc = ccc;
		campo = CampoUtils.create(ccc.campo);
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		if(ccc.name != null)
			params.putStr "id", ccc.name
			
		if(ccc.titulo != null)
			params.putStr("titulo", ccc.titulo)
		
		if(ccc.requerido)
			params.put "requerido", true
			
		if (ccc.ayuda != null) {
			if ((ccc.tipoAyuda != null) && ((ccc.tipoAyuda.type.equals("propover")) || (ccc.tipoAyuda.type.equals("popover"))))
				params.put "ayuda", "tags.TagAyuda.popover('${ccc.ayuda}')"
			else
				params.put "ayuda", "tags.TagAyuda.texto('${ccc.ayuda}')"
		}
				
		params.putStr "campo", campo.firstLower();
		
		return """
			#{fap.ccc ${params.lista()} /}
		""";
	}
}
