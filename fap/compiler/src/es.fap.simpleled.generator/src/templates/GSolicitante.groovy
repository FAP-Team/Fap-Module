package templates;

import com.sun.media.sound.RealTimeSequencer.PlayThread;

import es.fap.simpleled.led.*
import es.fap.simpleled.led.impl.*
import generator.utils.CampoUtils
import generator.utils.Entidad;
import generator.utils.LedUtils
import generator.utils.TagParameters;
import es.fap.simpleled.led.util.ModelUtils;
import generator.utils.StringUtils
import org.eclipse.emf.ecore.EObject



public class GSolicitante extends GElement{
	
	Solicitante solicitante;
	CampoUtils campo;
		
	public GSolicitante(Solicitante solicitante, GElement container){
		super(solicitante, container);
		this.solicitante = solicitante;
		campo = CampoUtils.create(solicitante.campo);
	}
	
	public void generate(){
		Combo combo;
		Grupo fisica;
		Grupo juridica;
		Direccion direccion;
		Texto telefonoContacto;
		TextoOculto uriTercero;
		Fecha fechaNacimiento;
		Combo sexo;
		Texto email;
		boolean completo = solicitante.elemento == "Solicitante";
		
		if (completo){
			combo = LedFactory.eINSTANCE.createCombo();
			combo.name = "${solicitante.name}Combo";
			combo.titulo = "Tipo";
			combo.campo = campo.addMore("tipo").campo;
			combo.requerido = solicitante.requerido;
		}
		
		if (solicitante.elemento == "SolicitantePersonaFisica" || completo)
			fisica = crearPersonaFisica(combo);
		
		if (solicitante.elemento == "SolicitantePersonaJuridica" || completo)
			juridica = crearPersonaJuridica(combo);
			
		if (solicitante.conDireccion){
			direccion = LedFactory.eINSTANCE.createDireccion();
			direccion.titulo="Domicilio del Solicitante";
			direccion.name="${solicitante.name}Direccion";
			direccion.requerido=solicitante.requerido;
			direccion.elemento="Direccion";
			direccion.campo=CampoUtils.addMore(solicitante.campo, "domicilio").campo;
		}

		telefonoContacto = LedFactory.eINSTANCE.createTexto();
		telefonoContacto.titulo="Teléfono de Contacto";
		telefonoContacto.name="${solicitante.name}_telefonoContacto";
		telefonoContacto.campo=CampoUtils.addMore(solicitante.campo, "telefonoContacto").campo;
		
		uriTercero = LedFactory.eINSTANCE.createTextoOculto();
		uriTercero.name="${solicitante.name}_uriTerceros";
		uriTercero.campo=CampoUtils.addMore(solicitante.campo, "uriTerceros").campo;

		if (!solicitante.noEmail) {
			email = LedFactory.eINSTANCE.createTexto();
			email.titulo="Email";
			email.duplicar=true;
			email.name="${solicitante.name}_email";
			email.campo=CampoUtils.addMore(solicitante.campo, "email").campo;
		}

		Grupo grupoOtrosDatos = LedFactory.eINSTANCE.createGrupo();
		grupoOtrosDatos.borde = false;
		grupoOtrosDatos.elementos.add(telefonoContacto);
		if (!solicitante.noEmail)
			grupoOtrosDatos.elementos.add(email);
		grupoOtrosDatos.elementos.add(uriTercero);

		if (solicitante.titulo){
			Grupo grupo = LedFactory.eINSTANCE.createGrupo();
			grupo.titulo = solicitante.titulo;
			if (combo) grupo.elementos.add(combo);
			if (fisica) grupo.elementos.add(fisica);
			if (juridica) grupo.elementos.add(juridica);
			grupo.elementos.add(grupoOtrosDatos);
			if (direccion) grupo.elementos.add(direccion);
			getGroupContainer().addElementAfter(grupo, solicitante);
		}
		else{
			if (combo) getGroupContainer().addElementAfter(combo, solicitante);
			if (fisica) getGroupContainer().addElementAfter(fisica, combo);
			if (juridica) getGroupContainer().addElementAfter(juridica, fisica);
			getGroupContainer().addElementAfter(grupoOtrosDatos, juridica);
			if (direccion) getGroupContainer().addElementAfter(direccion, grupoOtrosDatos);
		}
	}
	
	public Grupo crearPersonaFisica(Combo combo){
		PersonaFisica fisica = LedFactory.eINSTANCE.createPersonaFisica();
		fisica.name = "${solicitante.name}Fisica";
		fisica.campo = CampoUtils.addMore(solicitante.campo, "fisica").campo;
		fisica.requerido = solicitante.requerido;
		
		Grupo grupo = LedFactory.eINSTANCE.createGrupo();
		grupo.borde = false;
		grupo.elementos.add(fisica);
		if (combo){
			grupo.siCombo = combo;
			grupo.siComboValues = LedFactory.eINSTANCE.createValues();
			grupo.siComboValues.values.add("fisica");
		}
		
		if (!solicitante.noRepresentante){
			Check check = LedFactory.eINSTANCE.createCheck();
			check.name = "checkRepresentante_${solicitante.name}";
			check.titulo = "Representante";
			check.campo = CampoUtils.addMore(solicitante.campo, "representado").campo;

			Grupo grupoRepFisica = LedFactory.eINSTANCE.createGrupo();
			grupoRepFisica.siCheck = check;
			grupoRepFisica.siCheckValues = "true";
			grupoRepFisica.titulo = "Representante"
			Texto textoEmail = LedFactory.eINSTANCE.createTexto();
			textoEmail.titulo="Email";
			textoEmail.requerido=true;
			textoEmail.duplicar=true;
			if (!solicitante.representantePersonaFisica){
				Persona persona = LedFactory.eINSTANCE.createPersona();
				persona.requerido = true;
				persona.name = "representanteDelSolicitante_${solicitante.name}";
				persona.campo = CampoUtils.addMore(solicitante.campo, "representante").campo;
				grupoRepFisica.elementos.add(persona);
				textoEmail.campo = CampoUtils.addMore(persona.campo, "email").campo;
			}
			else {
				PersonaFisica personaFisica = LedFactory.eINSTANCE.createPersonaFisica();
				personaFisica.requerido = true;
				personaFisica.name = "representanteDelSolicitante_${solicitante.name}";
				personaFisica.campo = CampoUtils.addMore(solicitante.campo, "representante.fisica").campo;
				grupoRepFisica.elementos.add(personaFisica);
				textoEmail.campo = CampoUtils.addMore(solicitante.campo, "representante.email").campo;
			}
			grupoRepFisica.elementos.add(textoEmail);
			grupo.getElementos().add(check);
			grupo.getElementos().add(grupoRepFisica);
		}
		return grupo;
	}
	
	public Grupo crearPersonaJuridica(Combo combo){
		PersonaJuridica juridica = LedFactory.eINSTANCE.createPersonaJuridica();
		juridica.name = "${solicitante.name}Juridica";
		juridica.campo = CampoUtils.addMore(solicitante.campo, "juridica").campo;
		juridica.requerido = solicitante.requerido;
		juridica.permiso = solicitante.permiso;
		
		Grupo grupo = LedFactory.eINSTANCE.createGrupo();
		grupo.borde = false;
		grupo.elementos.add(juridica);
		if (combo){
			grupo.siCombo = combo;
			grupo.siComboValues = LedFactory.eINSTANCE.createValues();
			grupo.siComboValues.values.add("juridica");
		}
		
		if (!solicitante.noRepresentante)
			grupo.elementos.add(crearTablaRepresentantes(juridica.name));
		return grupo;
	}
	
	public Tabla crearTablaRepresentantes(String name){
		Tabla tabla = LedFactory.eINSTANCE.createTabla();
		tabla.name = "${name}Tabla";
		tabla.campo = CampoUtils.addMore(solicitante.campo, "representantes").campo;
		tabla.titulo = "Representante";
		tabla.popup = crearPopup(tabla.name);

		Campo campoColumna;
		if (solicitante.representantePersonaFisica)
			campoColumna = CampoUtils.create("RepresentantePersonaJuridica.fisica").campo;
		else{
			campoColumna = CampoUtils.create("RepresentantePersonaJuridica").campo;
			Columna tipo = LedFactory.eINSTANCE.createColumna();
			tipo.campo = CampoUtils.create("RepresentantePersonaJuridica.tipo").campo;
			tipo.titulo = "Tipo";
			tipo.expandir = true;
			tabla.columnas.add(tipo);
		}

		Columna nombre = LedFactory.eINSTANCE.createColumna();
		nombre.campo = CampoUtils.addMore(campoColumna, "nombreCompleto").campo;
		nombre.titulo = "Nombre";
		nombre.expandir = true;

		Columna nip = LedFactory.eINSTANCE.createColumna();
		nip.campo = CampoUtils.addMore(campoColumna, "numeroId").campo;
		nip.titulo = "NIP/CIF";
		nip.expandir = true;

		Columna tipoRep = LedFactory.eINSTANCE.createColumna();
		tipoRep.campo = CampoUtils.create("RepresentantePersonaJuridica.tipoRepresentacion").campo;
		tipoRep.titulo = "Tipo Representación";
		tipoRep.expandir = true;

		tabla.getColumnas().add(nombre);
		tabla.getColumnas().add(nip);
		tabla.getColumnas().add(tipoRep);
		return tabla;
	}

	public Popup crearPopup(String name){
		Popup popup = LedFactory.eINSTANCE.createPopup();
		getPaginaOrPopupContainer().element.eContainer().popups.add(popup);
			
		popup.permiso = solicitante.permiso;
		popup.name = "Popup${StringUtils.firstUpper(name)}";
		popup.titulo = "Representante";
		popup.campo = CampoUtils.addMore(solicitante.campo, "representantes").campo;
		
		Grupo grupo = LedFactory.eINSTANCE.createGrupo();
		grupo.titulo="Representante"
		
		
		Texto textoEmail = LedFactory.eINSTANCE.createTexto();
		textoEmail.titulo="Email";
		textoEmail.requerido=true;
		textoEmail.duplicar=true;
		
		if (!solicitante.representantePersonaFisica){
			Persona persona = LedFactory.eINSTANCE.createPersona();
			persona.campo = CampoUtils.create("RepresentantePersonaJuridica").campo;
			persona.name = "representante_${popup.name}";
			persona.requerido = true;
			grupo.elementos.add(persona);
			textoEmail.campo = CampoUtils.addMore(persona.campo, "email").campo;
		}
		else {
			PersonaFisica personaFisica = LedFactory.eINSTANCE.createPersonaFisica();
			personaFisica.requerido = true;
			personaFisica.setearTipoPadre = true;
			personaFisica.name = "representante_${popup.name}";
			personaFisica.campo = CampoUtils.create("RepresentantePersonaJuridica.fisica").campo;
			grupo.elementos.add(personaFisica);
			textoEmail.campo = CampoUtils.create("RepresentantePersonaJuridica.email").campo;
		}
		grupo.elementos.add(textoEmail);
		Combo tipo = LedFactory.eINSTANCE.createCombo();
		tipo.name = "tipo_${popup.name}";
		tipo.campo = CampoUtils.create("RepresentantePersonaJuridica.tipoRepresentacion").campo;
		tipo.titulo = "Tipo de Representación";
		tipo.requerido = true;
		grupo.elementos.add(tipo);
		popup.elementos.add(grupo);
		return popup;
	}
	
	public String validateCopy(Stack<Set<String>> validatedFields){
		String validation = "";
		if (solicitante.representantePersonaFisica)
			validation += """${campo.dbStr()}.representante.tipo = "fisica";""";
		if (solicitante.elemento.equals("SolicitantePersonaFisica"))
			validation += """${campo.dbStr()}.tipo = "fisica";""";
		else if (solicitante.elemento.equals("SolicitantePersonaJuridica"))
			validation += """${campo.dbStr()}.tipo = "juridica";""";
		return validation + super.validate(validatedFields);
	}
	
}
