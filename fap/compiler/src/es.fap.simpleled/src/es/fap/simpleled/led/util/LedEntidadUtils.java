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

	public static boolean isReferencia(Attribute attr){
		return attr != null && attr.getType().getCompound() != null;
	}
	
	/**
	 * En el caso de que el atributo sea una referencia, devuelve el tipo
	 * de la referencia(OneToOne, OneToMany, ManyToOne, ManyToMany)
	 * El tipo por defecto es OneToOne
	 * @param attr
	 * @return
	 */
	public static String getTipoReferencia(Attribute attr){
		if (!isReferencia(attr)) return null;
		CompoundType c = attr.getType().getCompound();
		if(c.getTipoReferencia() == null){
			return "OneToOne";
		}		
		return c.getTipoReferencia().getType();
	}
	
	/**
	 * Comprueba si el attributo attr es una referencia del tipo refType
	 * @param attr
	 * @param refType
	 * @return
	 */
	public static boolean isRefType(Attribute attr, String refType){
		if(attr == null || refType == null) return false;
		String attrRefType = getTipoReferencia(attr);
		return refType.equals(attrRefType);
	}

	public static boolean isOneToOne(Attribute attr){
		return isRefType(attr, "OneToOne");
	}
	
	public static boolean isOneToMany(Attribute attr){
		return isRefType(attr, "OneToMany");
	}
	
	public static boolean isManyToOne(Attribute attr){
		return isRefType(attr, "ManyToOne");
	}
	
	public static boolean isManyToMany(Attribute attr){
		return isRefType(attr, "ManyToMany");
	}

	/**
	 * Comprueba si un atribute es una referencia OneToOne o ManyToOne
	 * @param attr
	 * @return
	 */
	public static boolean xToOne(Attribute attr) {
		return isOneToOne(attr) || isManyToOne(attr);
	}

	/**
	 * Comprueba si un atribute es una referencia OneToMnay o ManyToMany
	 * @param attr
	 * @return
	 */
	public static boolean xToMany(Attribute attr){
		return isOneToMany(attr) || isManyToMany(attr);
	}
	
	/**
	 * Comprueba si un atribute es una referencia OneToOne o OneToMany
	 * @param attr
	 * @return
	 */
	public static boolean OneToX(Attribute attr) {
		return isOneToOne(attr) || isOneToOne(attr);
	}

	/**
	 * Comprueba si un atribute es una referencia ManyToOne o ManyToMany
	 * @param attr
	 * @return
	 */
	public static boolean ManyToX(Attribute attr){
		return isManyToOne(attr) || isManyToMany(attr);
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
			return attr.getType().getSimple().getType();
		}	
		if (attr.getType().getSpecial() != null){
			return attr.getType().getSpecial().getType();
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
		id.getType().setSimple(new LedFactoryImpl().createSimpleType());
		id.getType().getSimple().setType("Long");
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
