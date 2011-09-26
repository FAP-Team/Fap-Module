
package templates;

import es.fap.simpleled.led.*
import es.fap.simpleled.led.impl.*;
import generator.utils.*
import generator.utils.HashStack.HashStackName;

public class GPersonaJuridica {
	def PersonaJuridica personaJuridica;
	
	public static String generate(PersonaJuridica personaJuridica){
		GPersonaJuridica g = new GPersonaJuridica();
		g.personaJuridica = personaJuridica;
		g.view();
	}
	
	public String view(){
		String tabla = "";
		String permiso = "";
		// Añado la entidad que lo engloba a los parametros del Save
		CampoUtils campo = CampoUtils.create(personaJuridica.campo);
		EntidadUtils.addToSaveEntity(campo);
		
		TagParameters params = new TagParameters();
		
		params.putStr("id", personaJuridica.name);
		
		params.putStr "campo", campo.firstLower()
		
		if(personaJuridica.titulo != null)
			params.putStr("titulo", personaJuridica.titulo);

		if(personaJuridica.requerido)
			params.put("requerido", true);
		
		if (personaJuridica.permiso != null)
			permiso = personaJuridica.permiso.name;
		
		def out = """
#{fap.personaJuridica ${params.lista()} /}
"""
	
		return out;	
	}
	
}
