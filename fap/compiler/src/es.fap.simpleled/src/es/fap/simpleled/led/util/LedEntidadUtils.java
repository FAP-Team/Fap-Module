package es.fap.simpleled.led.util;

import java.util.ArrayList;
import java.util.List;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.CompoundType;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.LedFactory;
import es.fap.simpleled.led.impl.LedFactoryImpl;

public class LedEntidadUtils {
	
	public static List<Attribute> getAllDirectAttributes(Entity entidad) {
		List<Attribute> attrs = new ArrayList<Attribute>();
		while (entidad != null) {
			for (Attribute attr : entidad.getAttributes()) {
				attrs.add(attr);
			}
			entidad = entidad.getExtends();
		}
		return attrs;
	}
	
	public static Entity getEntidad(Attribute attr) {
		if (attr == null || attr.getType().getCompound() == null) {
			return null;
		}
		return attr.getType().getCompound().getEntidad();
	}
	
	// OneToOne o ManyToOne
	public static boolean xToOne(Attribute attr) {
		if (getEntidad(attr) == null) {
			return false;
		}
		CompoundType c = attr.getType().getCompound();
		return c.getTipoReferencia() == null || (!c.getTipoReferencia().equals("OneToMany") && !c.getTipoReferencia().equals("ManyToMany"));
	}

	// OneToMany o ManyToMany
	public static boolean xToMany(Attribute attr){
		if (getEntidad(attr) == null){
			return false;
		}
		CompoundType c = attr.getType().getCompound();
		return c.getTipoReferencia() != null && (c.getTipoReferencia().equals("OneToMany") || c.getTipoReferencia().equals("ManyToMany"));
	}
	
	public static boolean esLista(Attribute attr){
		if (attr.getType().getCompound() == null){
			return false;
		}
		if (attr.getType().getCompound().getLista() != null){
			return true;
		}
		return false;
	}
	
	public static boolean esColeccion(Attribute attr){
		if (attr.getType().getCompound() == null){
			return false;
		}
		if (attr.getType().getCompound().getCollectionType() != null){
			return true;
		}
		return false;
	}
	
	public static boolean esSimple(Attribute attr){
		return attr.getType().getSimple() != null || attr.getType().getSpecial() != null;
	}
	
	public static String getSimpleTipo(Attribute attr){
		if (attr.getType().getSimple() != null){
			return attr.getType().getSimple();
		}	
		if (attr.getType().getSpecial() != null){
			return attr.getType().getSpecial();
		}
		return null;
	}
		
	public static Attribute getAttribute(Entity entidad, String atributo){
		for (Attribute attr: getAllDirectAttributes(entidad)){
			if (attr.getName().equals(atributo)){
				return attr;
			}
		}
		return null;
	}
	
	public static void addId(Entity entidad){
		if (entidad.getExtends() != null){
			return;
		}
		for (Attribute attr: entidad.getAttributes()){
			if (attr.getName().equals("id")){
				return;
			}
		}
		LedFactory factory = new LedFactoryImpl();
		Attribute id = factory.createAttribute();
		id.setName("id");
		id.setType(factory.createType());
		id.getType().setSimple("Long");
		entidad.getAttributes().add(id);
	}
	
	public static List<Attribute> getAllDirectAttributesExceptId(Entity entidad){
		List<Attribute> attrs = getAllDirectAttributes(entidad);
		for (int i = 0; i < attrs.size(); i++){
			if (attrs.get(i).getName().equals("id")){
				attrs.remove(i);
				break;
			}
		}
		return attrs;
	}
	
}
