package templates;

import es.fap.simpleled.led.*
import es.fap.simpleled.led.util.LedCampoUtils
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.led.impl.*;
import generator.utils.*

public class GEntidadAutomatica extends GElement{

	EntidadAutomatica entidadAutomatica;
	
	public GEntidadAutomatica(EntidadAutomatica entidadAutomatica, GElement container){
		super(entidadAutomatica, container);
		this.entidadAutomatica = entidadAutomatica;
	}
	
	public void generate(){
		CampoUtils campo = CampoUtils.create(entidadAutomatica.campo);
		List<Elemento> elementos = generateEntidad(campo);
		Elemento prev = entidadAutomatica;
		GGroupElement group = getGroupContainer();
		for (Elemento e: elementos){
			group.addElementAfter(e, prev);
			prev = e;
		}
		group.removeElement(entidadAutomatica);
	}
	
	private List<Elemento> generateEntidad(CampoUtils campo) {
		Entity entidad = CampoUtils.create(entidadAutomatica.campo).getUltimaEntidad();
		
		if (LedEntidadUtils.extend(entidad, "Solicitante"))
			return [generateSolicitante(campo)];
		
		if (LedEntidadUtils.extend(entidad, "PersonaFisica"))
			return [generatePersonaFisica(campo)];
	
		if (LedEntidadUtils.extend(entidad, "PersonaJuridica"))
			return [generatePersonaJuridica(campo)];
		
		if (LedEntidadUtils.extend(entidad, "Persona"))
			return [generatePersona(campo)];
			
		if (LedEntidadUtils.extend(entidad, "Direccion"))
			return [generateDireccion(campo)];
			
		if (LedEntidadUtils.extend(entidad, "DireccionMapa"))
			return [generateDireccionMapa(campo)];
			
		if (LedEntidadUtils.extend(entidad, "Nip"))
			return [generateNip(campo)];
			
		List<Elemento> elementos = new ArrayList<Elemento>();
		for (Attribute attr: entidad.attributes){
			if (!attr.name.equals("id"))
				elementos.addAll(generateAtributo(campo.addAttribute(attr)));
		}
		return elementos;
	}
				
	private List<Elemento> generateAtributo(CampoUtils campo){
		Attribute attr = campo.getUltimoAtributo();
		if (attr.type.simple)
			return [generateAttrSimple(campo)];
		if (attr.type.special)
			return [generateAttrSpecial(campo)];
		else
			return generateAttrCompound(campo);
	}
	
	private Elemento generateAttrSimple(CampoUtils campo){
		String type = campo.getUltimoAtributo().type.simple?.type;
		if ("LongText".equals(type))
			return generateAreaTexto(campo);
		if ("Boolean".equals(type))
			return generateCheck(campo);
		else
			return generateTexto(campo);
	}

	private Elemento generateAttrSpecial(CampoUtils campo){
		String type = campo.getUltimoAtributo().type.special?.type;
		if ("DateTime".equals(type))
			return generateFecha(campo);
		else
			return generateTexto(campo);
	}

	private List<Elemento> generateAttrCompound(CampoUtils campo){
		if (LedCampoUtils.xToOne(campo.campo))
			return generateEntidad(campo);
		if (LedCampoUtils.xToMany(campo.campo))
			return [generateTabla(campo)];
		return [generateLista(campo)];
	}

	private Elemento generateTexto(CampoUtils campo){
		Texto texto = LedFactory.eINSTANCE.createTexto();
		texto.campo = campo.newCampo();
		texto.requerido = esRequerido(campo);
		return texto;
	}
	
	private Elemento generateCheck(CampoUtils campo){
		Check check = LedFactory.eINSTANCE.createCheck();
		check.campo = campo.newCampo();
		return check;
	}
	
	private Elemento generateAreaTexto(CampoUtils campo){
		AreaTexto areaTexto = LedFactory.eINSTANCE.createAreaTexto();
		areaTexto.campo = campo.newCampo();
		areaTexto.requerido = esRequerido(campo);
		return areaTexto;
	}
	
	private Elemento generateFecha(CampoUtils campo){
		Fecha fecha = LedFactory.eINSTANCE.createFecha();
		fecha.campo = campo.newCampo();
		fecha.requerido = esRequerido(campo);
		return fecha;
	}
	
	private Elemento generateNip(CampoUtils campo){
		Nip nip = LedFactory.eINSTANCE.createNip();
		nip.campo = campo.newCampo();
		nip.requerido = esRequerido(campo);
		return nip;
	}
	
	private Elemento generateDireccion(CampoUtils campo){
		Direccion direccion = LedFactory.eINSTANCE.createDireccion();
		direccion.campo = campo.newCampo();
		direccion.requerido = esRequerido(campo);
		return direccion;
	}
	
	private Elemento generateDireccionMapa(CampoUtils campo){
		DireccionMapa direccionMapa = LedFactory.eINSTANCE.createDireccionMapa();
		direccionMapa.campo = campo.newCampo();
		direccionMapa.requerido = esRequerido(campo);
		return direccionMapa;
	}
	
	private Elemento generateLista(CampoUtils campo){
		Combo combo = LedFactory.eINSTANCE.createCombo();
		combo.campo = campo.newCampo();
		combo.requerido = esRequerido(campo);
		return combo;
	}

	private Elemento generatePersona(CampoUtils campo){
		Persona persona = LedFactory.eINSTANCE.createPersona();
		persona.name = entidadAutomatica.name + "_" + campo.str.replaceAll("\\.", "_");
		persona.campo = campo.newCampo();
		persona.requerido = esRequerido(campo);
		return persona;
	}

	private Elemento generatePersonaFisica(CampoUtils campo){
		PersonaFisica persona = LedFactory.eINSTANCE.createPersonaFisica();
		persona.name = entidadAutomatica.name + "_" + campo.str.replaceAll("\\.", "_");
		persona.campo = campo.newCampo();
		persona.requerido = esRequerido(campo);
		return persona;
	}

	private Elemento generatePersonaJuridica(CampoUtils campo){
		PersonaJuridica persona = LedFactory.eINSTANCE.createPersonaJuridica();
		persona.name = entidadAutomatica.name + "_" + campo.str.replaceAll("\\.", "_");
		persona.titulo = "Persona juridica";
		persona.campo = campo.newCampo();
		persona.requerido = esRequerido(campo);
		return persona;
	}

	private Elemento generateSolicitante(CampoUtils campo){
		Solicitante solicitante = LedFactory.eINSTANCE.createSolicitante();
		solicitante.name = entidadAutomatica.name + "_" + campo.str.replaceAll("\\.", "_");
		solicitante.elemento = "Solicitante";
		solicitante.campo = campo.newCampo();
		solicitante.requerido = esRequerido(campo);
		return solicitante;
	}
	
	private Elemento generateTabla(CampoUtils campo){
		Tabla tabla = LedFactory.eINSTANCE.createTabla();
		tabla.name = campo.str.replaceAll("\\.", "_");
		tabla.campo = campo.newCampo();
		tabla.columnasAutomaticas = true;
		tabla.popup = generatePopup(campo);
		getPaginaOrPopupContainer().element.eContainer().popups.add(tabla.popup);
		return tabla;
	}
	
	private Popup generatePopup(CampoUtils campo){
		Popup popup = LedFactory.eINSTANCE.createPopup();
		popup.name = "Popup_" + campo.str.replaceAll("\\.", "_");
		Campo c = getPaginaOrPopupContainer().campo?.campo;
		Campo concatenado = LedCampoUtils.concatena(c, campo.campo);
		if (concatenado != null)
			popup.campo = concatenado;
		else
			popup.campo = campo.newCampo();
		
		EntidadAutomatica nuevaAutomatica = LedFactory.eINSTANCE.createEntidadAutomatica();
		nuevaAutomatica.campo = CampoUtils.create(campo.getUltimaEntidad()).campo;
		nuevaAutomatica.name = entidadAutomatica.name + "_" + campo.str.replaceAll("\\.", "_");
		nuevaAutomatica.permiso = entidadAutomatica.permiso;
		popup.elementos.add(nuevaAutomatica);
		return popup;
	}
	
	private boolean esRequerido(CampoUtils campo){
		return campo?.getUltimoAtributo()?.required;
	}

}
