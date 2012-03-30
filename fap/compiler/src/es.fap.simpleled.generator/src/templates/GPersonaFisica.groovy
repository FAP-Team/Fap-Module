package templates;

import es.fap.simpleled.led.*
import generator.utils.*

public class GPersonaFisica extends GSaveCampoElement{

	PersonaFisica personaFisica;
	
	public GPersonaFisica(PersonaFisica personaFisica, GElement container){
		super(personaFisica, container);
		this.personaFisica = personaFisica;
		campo = CampoUtils.create(personaFisica.campo);
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		params.putStr("id", personaFisica.name);
		params.putStr "campo", campo.firstLower();
		if(personaFisica.titulo != null)
			params.putStr("titulo", personaFisica.titulo);
		if (personaFisica.requerido)
			params.put("requerido", true);
		
		return """
			#{fap.personaFisica ${params.lista()} /}
		""";
	}
	
	public String copy(){
		return GSaveCampoElement.copyCamposFiltrados(campo, ["nombre","primerApellido","segundoApellido","nip"]);
	}
}
