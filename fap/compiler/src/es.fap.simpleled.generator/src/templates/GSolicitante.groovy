
package templates;

import com.sun.media.sound.RealTimeSequencer.PlayThread;

import es.fap.simpleled.led.*
import es.fap.simpleled.led.impl.*
import generator.utils.CampoUtils
import generator.utils.LedUtils
import es.fap.simpleled.led.util.ModelUtils;
import generator.utils.HashStack
import generator.utils.StringUtils
import generator.utils.TagParameters
import generator.utils.EntidadUtils
import generator.utils.HashStack.HashStackName
import org.eclipse.emf.ecore.EObject

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
		else
			titulo = "play.i18n.Messages.get('fap.tags.persona.grupo.titulo')"
			
		if(solicitante.requerido)
			requerido = true;
		
		fisica = crearPersonaFisica(combo, solicitante.isNoRepresentante())
		juridica = crearPersonaJuridica(combo, solicitante.permiso, solicitante.isNoRepresentante())
		
		def out = """
	#{fap.grupo titulo:${titulo}}
		#{fap.combo id:'${combo}', titulo:play.i18n.Messages.get('fap.tags.persona.tipo'), campo:'${campo.firstLower()}.tipo', requerido:${requerido} /}
		${fisica}
		${juridica}
	#{/fap.grupo}
"""
		return out;	
	}
	
	public String crearPersonaFisica(String combo, boolean noRepresentante) {
		String fisicaStr = "";
		PersonaFisica fisica = new PersonaFisicaImpl();
		fisica.setName(solicitante.name+"Fisica");
		fisica.setCampo(CampoUtils.addMore(solicitante.campo, "fisica"));
		fisica.setRequerido(solicitante.requerido? true:false);
		fisicaStr = Expand.expand(fisica);
		
		String params = ", siCombo:'${combo}', siComboValue:['fisica'], grupoVisible:false";
		
		fisicaStr = fisicaStr.replaceAll(/(\#\{fap\.personaFisica.+?)(\/\})/, '$1' + params + '$2')
		
		if (!noRepresentante) {
			Grupo grupoCheckRepFisica = new GrupoImpl();
			Values values = new ValuesImpl();
			values.values.add("fisica");
			grupoCheckRepFisica.setSiComboValues(values);
			
			Combo c = new ComboImpl();
			c.setName("${combo}");
			
			grupoCheckRepFisica.setSiCombo(c);
			grupoCheckRepFisica.setBorde("false");
			
			Check check = new CheckImpl();
			check.setName("checkRepresentante");
			check.setTitulo("Representante");
			check.setCampo(CampoUtils.addMore(solicitante.campo, "representado"));
			
			Grupo grupoRepFisica = new GrupoImpl();
			grupoRepFisica.setSiCheck(check);
			grupoRepFisica.setSiCheckValues("true");
			grupoRepFisica.setBorde("false");
			
			Persona persona = new PersonaImpl();
			persona.setTitulo("Representante");
			persona.setName("representanteDelSolicitante")
			persona.setCampo(CampoUtils.addMore(solicitante.campo, "representante"));
			grupoRepFisica.getElementos().add(persona);
			
			
			grupoCheckRepFisica.getElementos().add(check);
			grupoCheckRepFisica.getElementos().add(grupoRepFisica);
			
			fisicaStr += Expand.expand(grupoCheckRepFisica);
		}
		
		params = ", grupoVisible:false";
		fisicaStr = fisicaStr.replaceAll(/(\#\{fap\.personaJuridica.+?)(\/\})/, '$1' + params + '$2')
		
		return fisicaStr;
	}
	
	public String crearPersonaJuridica(String combo, PermisoGrafico permiso, boolean noRepresentante) {
		String juridicaStr = "";
		
		PersonaJuridica juridica = new PersonaJuridicaImpl();
		juridica.setName (solicitante.name+"Juridica");
		juridica.setCampo(CampoUtils.addMore(solicitante.campo, "juridica"));
		juridica.setRequerido(solicitante.requerido ? true:false);
		juridica.permiso = permiso;
		
		juridicaStr = Expand.expand(juridica);

		if (!noRepresentante) {
			juridicaStr += crearTablaRepresentantes(juridica.name, solicitante.campo, permiso, combo, noRepresentante);
		}
		
		String params = ", siCombo:'${combo}', siComboValue:['juridica'], visible:false, grupoVisible:false";
		juridicaStr = juridicaStr.replaceAll(/(\#\{fap\.personaJuridica.+?)(\/\})/, '$1' + params + '$2')
		juridicaStr = juridicaStr.replaceAll(/(.*\#\{fap\.grupo.+?)(\})/, '$1' + params + '$2')
		
		return juridicaStr;
	}
	
	public String crearTablaRepresentantes (String name, Campo campo, PermisoGrafico permiso, String combo, boolean noRepresentante) {
		String tablaStr = "";

		if (!noRepresentante) {
			Grupo grupoCheckRepFisica = new GrupoImpl();
			Values values = new ValuesImpl();
			values.values.add("juridica");
			grupoCheckRepFisica.setSiComboValues(values);
			
			Combo c = new ComboImpl();
			c.setName("${combo}");
			
			grupoCheckRepFisica.setSiCombo(c);
			grupoCheckRepFisica.setBorde("false");
		
			Tabla tabla = new TablaImpl();
			
			tabla.setPermiso(permiso);
			tabla.setName(name+"Tabla");
			tabla.setCampo(CampoUtils.addMore(campo, "representantes"));
			tabla.setTitulo("Representante");
			tabla.setPopup(crearPopup(tabla.name, campo, permiso));
	
			Columna tipo = new ColumnaImpl();
			tipo.setCampo(CampoUtils.create("RepresentantePersonaJuridica.tipo").campo);
			tipo.setTitulo("Tipo");
			tipo.setExpandir(true);
		
			Columna nombre = new ColumnaImpl();
			nombre.setCampo(CampoUtils.create("RepresentantePersonaJuridica.nombreCompleto").campo);
			nombre.setTitulo("Nombre");
			nombre.setExpandir(true);

			Columna nip = new ColumnaImpl();
			nip.setCampo(CampoUtils.create("RepresentantePersonaJuridica.numeroId").campo);
			nip.setTitulo("NIP/CIF");
			nip.setExpandir(true);
		
			Columna tipoRep = new ColumnaImpl();
			tipoRep.setCampo(CampoUtils.create("RepresentantePersonaJuridica.tipoRepresentacion").campo);
			tipoRep.setTitulo("Tipo Representación");
			tipoRep.setExpandir(true);
		
			tabla.getColumnas().add(tipo);
			tabla.getColumnas().add(nombre);
			tabla.getColumnas().add(nip);
			tabla.getColumnas().add(tipoRep);
		
			grupoCheckRepFisica.getElementos().add(tabla);
			tablaStr = Expand.expand(grupoCheckRepFisica);
		}
		return tablaStr;
	}

	public Popup crearPopup (String name, Campo campo, PermisoGrafico permiso) {
		Popup popup = new PopupImpl();

		EObject container = campo;
		while (!(container instanceof Formulario))
			container = container.eContainer();
		
		popup.eContainer = container;
		popup.permiso = permiso;
		
		popup.setName "Popup" + StringUtils.firstUpper(name);
		popup.setTitulo "Representante";
		popup.setCampo(CampoUtils.create("Solicitud.solicitante.representantes").campo);
		
		// Debemos crear las acciones para el popup, ya que no son referenciados en la tabla
		// y se lo debemos indicar de esta forma.
		Accion action = new AccionImpl();
		action.setName("crear");
		action.setCrearSiempre(true);
		popup.getElementos().add(action);
		action = new AccionImpl();
		action.setName("editar");
		action.setCrearSiempre(true);
		popup.getElementos().add(action);
		action = new AccionImpl();
		action.setName("borrar");
		action.setCrearSiempre(true);
		popup.getElementos().add(action);
			
		Persona person = new PersonaImpl();
		
		person.setCampo(CampoUtils.create("RepresentantePersonaJuridica").campo);
		person.setTitulo("Representante");
		person.setName("representante_" + popup.name);
		person.setRequerido(true);
		//person.setNoRepresentante(true);
		popup.getElementos().add(person);

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
