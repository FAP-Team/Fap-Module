
package templates;

import es.fap.simpleled.led.*
import es.fap.simpleled.led.impl.*;
import generator.utils.*

public class GPersonaJuridica extends GSaveCampoElement{
	
	PersonaJuridica personaJuridica;
	
	public GPersonaJuridica(PersonaJuridica personaJuridica, GElement container){
		super(personaJuridica, container);
		this.personaJuridica = personaJuridica;
		campo = CampoUtils.create(personaJuridica.campo);
	}
	
	public String view(){
		String tabla = "";
		String permiso = "";
		
		TagParameters params = new TagParameters();
		params.putStr("id", personaJuridica.name);
		params.putStr "campo", campo.firstLower()
		if(personaJuridica.titulo != null)
			params.putStr("titulo", personaJuridica.titulo);
		if(personaJuridica.requerido)
			params.put("requerido", true);
		if (personaJuridica.permiso != null)
			permiso = personaJuridica.permiso.name;
		
		return """
			#{fap.personaJuridica ${params.lista()} /}
		""";
	}
	
	public String copy(){
		return GSaveCampoElement.copyCamposFiltrados(campo, ["cif","entidad"]);
	}
	
}
