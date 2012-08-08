
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

public class GSolicitante {
	def Solicitante solicitante
		
	public static String generate(Solicitante solicitante){
		
		GSolicitante g = new GSolicitante();
		g.solicitante = solicitante;
		g.view();
	}
	
	public String view(){
		String fisica = "";
		String juridica = "";
		String fisicaRepresentantes = "";
		String juridicaRepresentantes = "";
		String combo = "";
		String titulo = "";
		boolean requerido = false;
		
		// Añado la entidad que lo engloba a los parametros del Save
		CampoUtils campo = CampoUtils.create(solicitante.campo);
		EntidadUtils.addToSaveEntity(campo);
		
		combo = solicitante.name + "Combo"
			
		if(solicitante.titulo != null)
			titulo = "'${solicitante.titulo}'";
		else {// No tendrá grupo
			//titulo = "play.i18n.Messages.get('fap.tags.persona.grupo.titulo')"
		}	
		if(solicitante.requerido)
			requerido = true;

		def out = "";
		if (solicitante.elemento == "SolicitantePersonaFisica") {
			fisica = crearPersonaFisica(combo, solicitante.isNoRepresentante(), solicitante.representantePersonaFisica, true)
			if (solicitante.titulo != null) {
				out = """
					#{fap.grupo titulo:${titulo}}
						${fisica}
					#{/fap.grupo}
				"""
			} else {
				out = """
					${fisica}
				"""
			}
			return out;
		} else if (solicitante.elemento == "SolicitantePersonaJuridica") {
			juridica = crearPersonaJuridica(combo, solicitante.permiso, solicitante.isNoRepresentante(), solicitante.representantePersonaFisica, true)
			if (solicitante.titulo != null) {
				out = """
					#{fap.grupo titulo:${titulo}}
						${juridica}
					#{/fap.grupo}
				"""
			} else {
				out = """
					${juridica}
				"""
			}
			return out;
		} else{
			fisica = crearPersonaFisica(combo, solicitante.isNoRepresentante(), solicitante.representantePersonaFisica, false)
			juridica = crearPersonaJuridica(combo, solicitante.permiso, solicitante.isNoRepresentante(), solicitante.representantePersonaFisica, false)
			if (solicitante.titulo != null) {
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
		}
		return out;	
	}
	
	public String crearPersonaFisica(String combo, boolean noRepresentante, boolean soloRepresentantePersonaFisica, boolean soloFisica) {
		String fisicaStr = "";
		PersonaFisica fisica = new PersonaFisicaImpl();
		fisica.setName(solicitante.name+"Fisica");
		fisica.setCampo(CampoUtils.addMore(solicitante.campo, "fisica"));
		fisica.setRequerido(solicitante.requerido? true:false);
		fisicaStr = Expand.expand(fisica);
		String params = "";
		if (!soloFisica)
			params = ", siCombo:'${combo}', siComboValue:['fisica'], grupoVisible:false";
		
		fisicaStr = fisicaStr.replaceAll(/(\#\{fap\.personaFisica.+?)(\/\})/, '$1' + params + '$2')
		
		if (!noRepresentante) {
			Grupo grupoCheckRepFisica = new GrupoImpl();
			if (!soloFisica){
				Values values = new ValuesImpl();
				values.values.add("fisica");
				grupoCheckRepFisica.setSiComboValues(values);
			
				Combo c = new ComboImpl();
				c.setName("${combo}");
				grupoCheckRepFisica.setSiCombo(c);
			}
			
			grupoCheckRepFisica.setVisible("false");
			
			Check check = new CheckImpl();
			check.setName("checkRepresentante_"+solicitante.name);
			check.setTitulo("Representante");
			check.setCampo(CampoUtils.addMore(solicitante.campo, "representado"));
			
			Grupo grupoRepFisica = new GrupoImpl();
			grupoRepFisica.setSiCheck(check);
			grupoRepFisica.setSiCheckValues("true");
			grupoRepFisica.setVisible("false");
			
			Texto textoEmail = LedFactory.eINSTANCE.createTexto();
			textoEmail.titulo="Email";
	  		textoEmail.requerido=true;
	  		textoEmail.duplicar=true;
			
			if (!soloRepresentantePersonaFisica){
				Persona persona = new PersonaImpl();
				persona.setTitulo("Representante");
				persona.setRequerido(true);
				persona.setName("representanteDelSolicitante_"+solicitante.name);
				persona.setCampo(CampoUtils.addMore(solicitante.campo, "representante"));
				grupoRepFisica.getElementos().add(persona);
				textoEmail.setCampo(CampoUtils.addMore(persona.campo, "email"));
			} else {
				PersonaFisica personaFisica = new PersonaFisicaImpl();
				personaFisica.setTitulo("Representante");
				personaFisica.setRequerido(true);
				personaFisica.setName("representanteDelSolicitante_"+solicitante.name);
				personaFisica.setCampo(CampoUtils.addMore(solicitante.campo, "representante.fisica"));
				grupoRepFisica.getElementos().add(personaFisica);
				textoEmail.setCampo(CampoUtils.addMore(solicitante.campo, "representante.email"));
			}
			grupoRepFisica.elementos.add(textoEmail);
			grupoCheckRepFisica.getElementos().add(check);
			grupoCheckRepFisica.getElementos().add(grupoRepFisica);
			
			fisicaStr += Expand.expand(grupoCheckRepFisica);
		}

		params = ", grupoVisible:false";
		fisicaStr = fisicaStr.replaceAll(/(\#\{fap\.personaJuridica.+?)(\/\})/, '$1' + params + '$2');
		return fisicaStr;
	}
	
	public String crearPersonaJuridica(String combo, Permiso permiso, boolean noRepresentante, boolean soloRepresentantePersonaFisica, boolean soloJuridica) {
		String juridicaStr = "";
		
		PersonaJuridica juridica = new PersonaJuridicaImpl();
		juridica.setName (solicitante.name+"Juridica");
		juridica.setCampo(CampoUtils.addMore(solicitante.campo, "juridica"));
		juridica.setRequerido(solicitante.requerido ? true:false);
		juridica.permiso = permiso;
		juridicaStr = Expand.expand(juridica);

		String params = "";
		if (!soloJuridica)
			params = ", siCombo:'${combo}', siComboValue:['juridica'], visible:false, grupoVisible:false";
		if (!noRepresentante) {
			juridicaStr += crearTablaRepresentantes(juridica.name, solicitante.campo, permiso, combo, noRepresentante, soloJuridica, soloRepresentantePersonaFisica);
		}
		juridicaStr = juridicaStr.replaceAll(/(\#\{fap\.personaJuridica.+?)(\/\})/, '$1' + params + '$2');
		
		return juridicaStr;
	}
	
	public String crearTablaRepresentantes (String name, Campo campo, Permiso permiso, String combo, boolean noRepresentante, boolean soloJuridica, boolean soloRepresentantePersonaFisica) {
		String tablaStr = "";

		if (!noRepresentante) {
			Grupo grupoCheckRepFisica = new GrupoImpl();
			if (!soloJuridica){
				Values values = new ValuesImpl();
				values.values.add("juridica");
				grupoCheckRepFisica.setSiComboValues(values);
			
				Combo c = new ComboImpl();
				c.setName("${combo}");
			
				grupoCheckRepFisica.setSiCombo(c);
			}
			grupoCheckRepFisica.setVisible("false");
		
			Tabla tabla = new TablaImpl();
			
			tabla.setName(name+"Tabla");
			tabla.setCampo(CampoUtils.addMore(campo, "representantes"));
			tabla.setTitulo("Representante");
			tabla.setPopup(crearPopup(tabla.name, campo, permiso, soloRepresentantePersonaFisica));
			
			String baseRepresentante = "";
			Campo campoColumna = new CampoImpl();
			if (soloRepresentantePersonaFisica)
				campoColumna = CampoUtils.create("RepresentantePersonaJuridica.fisica").campo;
			else{
				campoColumna = CampoUtils.create("RepresentantePersonaJuridica").campo;
				Columna tipo = new ColumnaImpl();
				tipo.setCampo(CampoUtils.create("RepresentantePersonaJuridica.tipo").campo);
				tipo.setTitulo("Tipo");
				tipo.setExpandir(true);
				tabla.getColumnas().add(tipo);
			}
				
		
			Columna nombre = new ColumnaImpl();
			nombre.setCampo(CampoUtils.addMore(campoColumna, "nombreCompleto"));
			nombre.setTitulo("Nombre");
			nombre.setExpandir(true);

			Columna nip = new ColumnaImpl();
			nip.setCampo(CampoUtils.addMore(campoColumna, "numeroId"));
			nip.setTitulo("NIP/CIF");
			nip.setExpandir(true);
		
			Columna tipoRep = new ColumnaImpl();
			tipoRep.setCampo(CampoUtils.create("RepresentantePersonaJuridica.tipoRepresentacion").campo);
			tipoRep.setTitulo("Tipo Representación");
			tipoRep.setExpandir(true);
		
			tabla.getColumnas().add(nombre);
			tabla.getColumnas().add(nip);
			tabla.getColumnas().add(tipoRep);
		
			grupoCheckRepFisica.getElementos().add(tabla);
			tablaStr = Expand.expand(grupoCheckRepFisica);
		}
		return tablaStr;
	}

	public Popup crearPopup (String name, Campo campo, Permiso permiso, boolean soloRepresentantePersonaFisica) {
		Popup popup = new PopupImpl();

		popup.permiso = permiso
		
		popup.setName "Popup" + StringUtils.firstUpper(name);
		popup.setTitulo "Representante";
		popup.setCampo(CampoUtils.addMore(campo, "representantes"));
		
		Texto textoEmail = LedFactory.eINSTANCE.createTexto();
		textoEmail.titulo="Email";
        textoEmail.requerido=true;
  		textoEmail.duplicar=true;

		if (!soloRepresentantePersonaFisica){
			Persona person = new PersonaImpl();
			person.setCampo(CampoUtils.create("RepresentantePersonaJuridica").campo);
			person.setTitulo("Representante");
			person.setName("representante_"+popup.name);
			person.setRequerido(true);
			popup.getElementos().add(person);
			textoEmail.setCampo(CampoUtils.addMore(person.campo, "email"));
		} else {
			PersonaFisica personaFisica = new PersonaFisicaImpl();
			personaFisica.setTitulo("Representante");
			personaFisica.setRequerido(true);
			personaFisica.setName("representante_"+popup.name);
			personaFisica.setCampo(CampoUtils.create("RepresentantePersonaJuridica.fisica").campo);
			popup.getElementos().add(personaFisica);
			textoEmail.setCampo(CampoUtils.create("RepresentantePersonaJuridica.email").campo);
		}
		
		popup.elementos.add(textoEmail);
		Combo tipo = new ComboImpl();
		tipo.setName "tipo_"+popup.name;
		
		tipo.setCampo(CampoUtils.create("RepresentantePersonaJuridica.tipoRepresentacion").campo);
		tipo.setTitulo "Tipo de Representación";
		tipo.setRequerido(true)
		
		popup.getElementos().add(tipo);
		
		Expand.expand(popup);
		
		return popup;
	}

}
