package templates;

import org.apache.log4j.Logger;

import es.fap.simpleled.led.*
import generator.utils.*

public class GPersonaFisica extends GSaveCampoElement{

	PersonaFisica personaFisica;
	
	private static Logger logger = Logger.getLogger("GPersonaFisica")
	
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
		String ret = "";
		if (personaFisica.setearTipoPadre){
			String var = "db"+StringUtils.firstUpper(campo.sinUltimoAtributo());
			ret+=""" ${var}.tipo = "fisica";
				 """
		}
		return ret + GSaveCampoElement.copyCamposFiltrados(campo, ["nip","nombre","primerApellido","segundoApellido","sexo","fechaNacimiento"]);
	}
}
