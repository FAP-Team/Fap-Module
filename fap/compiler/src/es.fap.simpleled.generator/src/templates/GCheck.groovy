package templates;

import es.fap.simpleled.led.*
import generator.utils.*

public class GCheck extends GSaveCampoElement{

	Check check;
	
	public GCheck(Check check, GElement container){
		super(check, container);
		this.check = check;
		campo = CampoUtils.create(check.campo);
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		params.putStr("campo", campo.firstLower());
		
		if(check.titulo != null)
			params.putStr("titulo", check.titulo)
		
		if(check.name != null)
			params.putStr("id", check.name)	
		
		if (check.ayuda != null) {
			if ((check.tipoAyuda != null) && (check.tipoAyuda.type.equals("propover")))
				params.put "ayuda", "tags.TagAyuda.popover('${check.ayuda}')"
			else
				params.put "ayuda", "tags.TagAyuda.texto('${check.ayuda}')"
		}
			
		if(check.anchoTitulo != null)
			params.putStr("anchoTitulo", check.anchoTitulo)
		
		return """
			#{fap.check ${params.lista()} /}		
		""";
	}	
}
