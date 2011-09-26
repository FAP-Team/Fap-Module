
package templates;

import es.fap.simpleled.led.*
import es.fap.simpleled.led.impl.*;
import generator.utils.*
import generator.utils.HashStack.HashStackName

public class GEntidadAutomatica {

	def EntidadAutomatica entidadAutomatica;
	
	public static String generate(EntidadAutomatica entidadAutomatica){
		GEntidadAutomatica g = new GEntidadAutomatica();
		entidadAutomatica.metaClass.elementos = new ArrayList<Elemento>()
		g.entidadAutomatica = entidadAutomatica;
		g.view();
	}
	
	public String view(){
		String out = ""
		Entity entidad = CampoUtils.create(entidadAutomatica.campo).getLastEntity();
				
		if (entidad.getExtends()?.name.equals("Persona")) {
			Persona persona = new PersonaImpl()
			persona.name = entidadAutomatica.name
			persona.campo = entidadAutomatica.campo
			persona.requerido = true
			out = GPersona.generate(persona);
			entidadAutomatica.elementos.add(persona)			
		}
		else if (entidad.getExtends()?.name.equals("PersonaFisica")) {
			PersonaFisica persona = new PersonaFisicaImpl()
			persona.name = entidadAutomatica.name
			persona.campo = CampoUtils.addMore(entidadAutomatica.campo, "fisica");
			persona.requerido = true
			out = GPersonaFisica.generate(persona);
			entidadAutomatica.elementos.add(persona)			
		}
		else if (entidad.getExtends()?.name.equals("PersonaJuridica")){
			PersonaJuridica persona = new PersonaJuridicaImpl()
			persona.name = entidadAutomatica.name
			persona.campo = CampoUtils.addMore(entidadAutomatica.campo, "juridica");
			persona.requerido = true
			out = GPersonaJuridica.generate(persona);
			entidadAutomatica.elementos.add(persona)			
		}
		
		for (Attribute attr : entidad.attributes) {
			if (!attr.name.equals("id")){
				out += generateAttr(attr);	
			}		
		}
		
		return out;
	}
				
	private String generateAttr(Attribute attr) {
		String out = "" 
		if ( attr.type.simple != null)
			out = generateAttrSimple(attr);
		else if ( attr.type.special != null)
			out = generateAttrSpecial(attr);
		else
			out = generateAttrCompound(attr);
		return out;
	}
	
	private String generateAttrSimple(Attribute attr) {
		String out = ""
		String type = attr.type.simple
		if (type.equals("LongText")) {
			out = generateAreaTexto(attr);
		}
		else{
			out = generateTexto(attr);
		}
		return out
	}

	private String generateAttrSpecial(Attribute attr) {
		String out = ""
		String type = attr.type.simple
		if (type.equals("DateTime")) {
			out = generateFecha(attr);
		}
		else{
			out = generateTexto(attr);
		}
		return out
	}

	private String generateAttrCompound(Attribute attr) {
		String out = "";
		CompoundType compound = attr.type.compound;
		if (compound.entidad != null){
			out = generateEntidad(attr);
		}
		else {
			out = generateLista(attr);
		}		
		return out;
	}

/*  Atributos Simples */
		
	private String generateTexto(Attribute attr) {
		String out = "";
		Texto texto = new TextoImpl();
		texto.campo = CampoUtils.addAttribute(entidadAutomatica.campo, attr);
		texto.requerido = esRequerido(attr);
		out = GTexto.generate(texto);
		entidadAutomatica.elementos.add(texto);
		return out;
	}
	
	private String generateFecha(Attribute attr) {
		String out = "";
		Fecha fecha = new FechaImpl();
		fecha.campo = CampoUtils.addAttribute(entidadAutomatica.campo, attr);
		fecha.requerido = esRequerido(attr);
		out = GFecha.generate(fecha);
		entidadAutomatica.elementos.add(fecha);
		return out;
	}

	private String generateAreaTexto(Attribute attr) {
		String out = "";
		AreaTexto texto = new AreaTextoImpl();
		texto.campo = CampoUtils.addAttribute(entidadAutomatica.campo, attr);
		texto.requerido = esRequerido(attr);
		out = GAreaTexto.generate(texto);
		entidadAutomatica.elementos.add(texto);
		return out;
	}
	
	private String generateNip(Attribute attr) {
		String out = "";
		Nip nip = new NipImpl();
		nip.campo = CampoUtils.addAttribute(entidadAutomatica.campo, attr);
		nip.requerido = esRequerido(attr);
		out = GNip.generate(nip);
		entidadAutomatica.elementos.add(nip);
		return out;
	}
	
	/*
	 * Atributo simple de direccion
	 * no se muestra ni el país ni la provincia
	 */
	private String generateDireccion(Attribute attr) {
		String out = "";
		Direccion direccion = new DireccionImpl();
		direccion.campo = CampoUtils.addAttribute(entidadAutomatica.campo, attr);
		direccion.requerido = esRequerido(attr);
		out = GDireccion.generate(direccion);
		entidadAutomatica.elementos.add(direccion);
		return out;
	}

	private boolean esRequerido (Attribute attr) {
		boolean requerido = false;
		for (AttributeAnotations anotacion : attr.anotaciones) {
			if (anotacion.required) {
				requerido = true;
				break;
			}
		}
		return requerido;
	}
	
	/* Atributos Compuestos */
	
	private String generateLista(Attribute attr) {
		String out = ""
		Combo combo = new ComboImpl();
		combo.campo = CampoUtils.addAttribute(entidadAutomatica.campo, attr);
		combo.requerido = esRequerido(attr);
		if (attr.type.getCompound()?.isMultiple()){
			combo.multiple = true;
		}
		out = GCombo.generate(combo);
		entidadAutomatica.elementos.add(combo)
		return out;
	}

	private String generatePersona(Attribute attr) {
		String out = ""
		Persona persona = new PersonaImpl();
		persona.name = entidadAutomatica.name + "_" + attr.name;
		persona.campo = CampoUtils.addAttribute(entidadAutomatica.campo, attr);
		persona.requerido = esRequerido(attr);
		out = GPersona.generate(persona);
		entidadAutomatica.elementos.add(persona);
		return out;
	}

	private String generatePersonaFisica(Attribute attr) {
		String out = "";
		PersonaFisica persona = new PersonaFisicaImpl();
		persona.name = entidadAutomatica.name + "_" + attr.name;
		persona.campo = CampoUtils.addAttribute(entidadAutomatica.campo, attr);
		persona.requerido = esRequerido(attr);
		out = GPersonaFisica.generate(persona);
		entidadAutomatica.elementos.add(persona);
		return out;
	}

	private String generatePersonaJuridica(Attribute attr) {
		String out = "";
		PersonaJuridica persona = new PersonaJuridicaImpl();
		persona.name = entidadAutomatica.name + "_" + attr.name;
		persona.campo = CampoUtils.addAttribute(entidadAutomatica.campo, attr);
		persona.requerido = esRequerido(attr);
		out = GPersonaJuridica.generate(persona);
		entidadAutomatica.elementos.add(persona);
		return out;
	}

	private String generateEntidad (Attribute attr) {
		String out = ""
		Entity entidad = attr.type.compound.entidad;
		if (entidad.name.equals("Persona")){
			out = generatePersona(attr);
		}
		else if (entidad.name.equals("PersonaFisica")){
			out = generatePersonaFisica(attr)
		}
		else if (entidad.name.equals("PersonaFisica")){
			out = generatePersonaJuridica(attr);
		}
		else if (entidad.name.equals("Direccion")){
			out = generateDireccion(attr);
		}
		else if (entidad.name.equals("Nip")){
			out = generateNip(attr);
		}
		else{
			if (EntidadUtils.xToOne(attr)){ 
				out = generateEntidadAutomatica(attr);
			}
			else if (EntidadUtils.xToMany(attr)){
				out = generateTabla(attr);
			}
		}
		return out;
	}
	
	private String generateEntidadAutomatica(Attribute attr) {
		String out = "";
		Entity entidad = attr.type.compound.entidad;
		EntidadAutomatica nuevaAutomatica = new EntidadAutomaticaImpl();
		nuevaAutomatica.name = entidadAutomatica.name + "_" + attr.name;	
		nuevaAutomatica.campo = CampoUtils.addAttribute(entidadAutomatica.campo, attr);
		nuevaAutomatica.permiso = entidadAutomatica.permiso;
		out = GEntidadAutomatica.generate(nuevaAutomatica);
		entidadAutomatica.elementos.addAll(nuevaAutomatica.elementos);
		return out;
	}

	private String generateTabla(Attribute attr) {
		String out = "";
		Tabla tabla = new TablaImpl();
		tabla.name = attr.name;
		tabla.campo = CampoUtils.addAttribute(entidadAutomatica.campo, attr);
		tabla.popup = generatePopup(attr);
		Entity entidad = attr.type.compound.entidad;
		tabla.columnasAutomaticas = true;
		out = GTabla.generate(tabla);
		return out;
	}
	
	private Popup generatePopup(Attribute attr) {
		Entity entidad = attr.type.compound.entidad;
		Popup popup = new PopupImpl();
		popup.name = "Popup" + attr.name;
		popup.campo = CampoUtils.addAttribute(entidadAutomatica.campo, attr);
		
		EntidadAutomatica nuevaAutomatica = new EntidadAutomaticaImpl();
		nuevaAutomatica.campo = CampoUtils.create(entidad).campo;
		nuevaAutomatica.permiso=entidadAutomatica.permiso;
		popup.elementos.add(nuevaAutomatica);
		GPopup.generate(popup);
		return popup;
	}

}
