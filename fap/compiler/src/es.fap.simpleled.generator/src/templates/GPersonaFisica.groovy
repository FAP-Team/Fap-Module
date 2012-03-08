package templates;

import es.fap.simpleled.led.*
import generator.utils.*
import generator.utils.HashStack.HashStackName

public class GPersonaFisica {
	def PersonaFisica personaFisica;
	
	public static String generate(PersonaFisica personaFisica){
		GPersonaFisica g = new GPersonaFisica();
		g.personaFisica = personaFisica;
		g.view();
	}
	
	public String view(){
		// Añado la entidad que lo engloba a los parametros del Save
		CampoUtils campo = CampoUtils.create(personaFisica.campo);
//		System.out.println("CAMPO1 ${personaFisica.campo}");
//		System.out.println("CAMPO2 ${campo}");
		EntidadUtils.addToSaveEntity(campo);
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
}
