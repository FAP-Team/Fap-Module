
package templates;

import com.sun.media.sound.RealTimeSequencer.PlayThread;

import es.fap.simpleled.led.*
import es.fap.simpleled.led.impl.*
import generator.utils.CampoUtils
import generator.utils.HashStack
import generator.utils.StringUtils
import generator.utils.TagParameters
import generator.utils.EntidadUtils
import generator.utils.HashStack.HashStackName

public class GPersona {
	def Persona persona
		
	public static String generate(Persona persona){
		
		GPersona g = new GPersona();
		g.persona = persona;
		g.view();
	}
	
	public String view(){
		String fisica = "";
		String juridica = "";
		String combo = "";
		String titulo = "";
		boolean requerido = false;
		
		// Añado la entidad que lo engloba a los parametros del Save
		CampoUtils campo = CampoUtils.create(persona.campo);
		EntidadUtils.addToSaveEntity(campo);
		
		combo = persona.name + "Combo"
			
		if(persona.titulo != null)
			titulo = "'${persona.titulo}'";
			
		if(persona.requerido)
			requerido = true;
		
		fisica = crearPersonaFisica(combo)
		
		juridica = crearPersonaJuridica(combo, persona.permiso)
		
		def out;
		if (persona.titulo != null) {
			out = """
	#{fap.grupo titulo:${titulo}}
		#{fap.combo id:'${combo}', titulo:play.i18n.Messages.get('fap.tags.persona.tipo'), campo:'${campo.firstLower()}.tipo', requerido:${requerido} /}
		${fisica}
		${juridica}
	#{/fap.grupo}
"""
		} else {
			out = """
		#{fap.combo id:'${combo}', titulo:play.i18n.Messages.get('fap.tags.persona.tipo'), campo:'${campo.firstLower()}.tipo', requerido:${requerido} /}
		${fisica}
		${juridica}
		"""
		}
		return out;	
	}
	
	
	public String crearPersonaFisica(String combo) {
		String fisicaStr = "";
		
		PersonaFisica fisica = new PersonaFisicaImpl();
		
		fisica.setName(persona.name + "Fisica");
		
		fisica.campo = CampoUtils.addMore(persona.campo, "fisica");
		
		fisica.setRequerido(persona.requerido? true:false);
		//fisica.setNoRepresentante(persona.isNoRepresentante());
		
		fisicaStr = Expand.expand(fisica);
		
		String params = ", siCombo:'${combo}', siComboValue:['fisica'], grupoVisible:false";
		
		fisicaStr = fisicaStr.replaceAll(/(\#\{fap\.personaFisica.+?)(\/\})/, '$1' + params + '$2')

		return fisicaStr;
	}
	
	public String crearPersonaJuridica(String combo, Permiso permiso) {
		String juridicaStr = "";
		
		PersonaJuridica juridica = new PersonaJuridicaImpl();
		
		juridica.setName (persona.name+"Juridica");
		juridica.campo = CampoUtils.addMore(persona.campo, "juridica");
		juridica.setRequerido(persona.requerido ? true:false);
		
		juridica.permiso = permiso;
		
		juridicaStr = Expand.expand(juridica);
		
		String params = ", siCombo:'${combo}', siComboValue:['juridica'], grupoVisible:false";
		juridicaStr = juridicaStr.replaceAll(/(.*\#\{fap\.grupo.+?)(\})/, '$1' + params + '$2')
		juridicaStr = juridicaStr.replaceAll(/(\#\{fap\.personaJuridica.+?)(\/\})/, '$1' + params + '$2')

		return juridicaStr;
	}

}
