package utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;

import messages.Messages;
import models.CodigoRequerimiento;
import models.TiposCodigoRequerimiento;

import exceptions.ModelAccessException;

import play.Play;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.data.binding.types.DateBinder;
import play.db.jpa.JPAPlugin;
import play.db.jpa.Model;
import play.mvc.Scope;
import play.mvc.Scope.Params;
import play.vfs.VirtualFile;
import tags.ReflectionUtils;
import validation.ValueFromTable;

public class ModelUtils {

	public static <T> T copyComboValue(T from, String[] ids, Class parentClass, String field) throws ModelAccessException {
		try {
			Field f = parentClass.getField(field);
			Class fromClass = f.getType();
			if (Model.class.isAssignableFrom(fromClass)) {
				Object result = findById(fromClass, ((Model) from).id);
				return (T) result;
			} else if (Collection.class.isAssignableFrom(fromClass)) {
				// Clase de los elementos de la lista
				Class clazz = ReflectionUtils.getListClass(parentClass.getField(field));
				
				if (!Model.class.isAssignableFrom(clazz)) {
					return from;
				} else {
					//Colleccion de referencias, la lista de la entidad está vacía
					List<Long> idsL = new ArrayList<Long>();
					// Analiza los parámetros que se pasaron por el request,
					// todos
					// deben ser de tipo long
					if (ids != null) {
						for (String s : ids) {
							if (s != null) {
								long parseLong = Long.parseLong(s);
								idsL.add(parseLong);
							}
						}
					}

					// Consulta cada uno de los elementos de la lista
					// TODO: Se podría optimizar para hacer una única consulta?
					Collection<Model> result;
					if(List.class.isAssignableFrom(fromClass)){
						//LIST
						result = new ArrayList<Model>();	
					}else{
						//SET
						result = new HashSet<Model>();
					}
					
					for (Long id : idsL) {
						result.add((Model) findById(clazz, id));
					}
					return (T) result;
				}
			} else {
				return from;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ModelAccessException();
		}
	}

	private static Object findById(Class clazz, Long id)
			throws ModelAccessException {
		if (id != null) {
			try {
				Method method = clazz.getMethod("findById", Object.class);
				return method.invoke(null, id);
			} catch (Exception e) {
				throw new ModelAccessException();
			}
		}
		return null;

	}
	
	public static List<CodigoRequerimiento> getListCodigoRequerimientoFromTiposCodigoRequerimiento(List<TiposCodigoRequerimiento> tipoCodReq){
		List<CodigoRequerimiento> codReq = new ArrayList<CodigoRequerimiento>();
		for (TiposCodigoRequerimiento tcr: tipoCodReq){
			CodigoRequerimiento cr = new CodigoRequerimiento();
			cr.codigo=tcr.codigo;
			cr.descripcion=tcr.descripcion;
			cr.descripcionCorta=tcr.descripcionCorta;
			codReq.add(cr);
		}
		return codReq;
	}

	
	 @SuppressWarnings("unchecked")
	    /**
	     *  Resolve dependencies between objects using their keys. For each referenced objects, it sets the foreign key
	     */
	    public static Map<String, String[]> resolveDependencies(Class<play.db.Model> type, Map<String, String[]> yml, Map<String, Object> idCache) {

	        // Contains all the fields (object properties) we should look up
	        final Set<Field> fields = new HashSet<Field>();
	        final Map<String, String[]> resolvedYml = new HashMap<String, String[]>();
	        resolvedYml.putAll(yml);

	        // Look up the super classes
	        Class<?> clazz = type;
	        while (!clazz.equals(Object.class)) {
	            Collections.addAll(fields, clazz.getDeclaredFields());
	            clazz = clazz.getSuperclass();
	        }


	        // Iterate through the Entity property list
	        // @Embedded are not managed by the JPA plugin
	        // This is not the nicest way of doing things.
	         //modelFields =  Model.Manager.factoryFor(type).listProperties();
	        final List<Model.Property> modelFields =  new JPAPlugin.JPAModelLoader(type).listProperties();

	        for (Model.Property field : modelFields) {
	            // If we have a relation, get the matching object
	            if (field.isRelation) {
	                // These are the Ids that were set in the yml file (i.e person(nicolas)-> nicolas is the id)
	                final String[] ids = resolvedYml.get("object." + field.name);
	                if (ids != null) {
	                    final String[] resolvedIds = new String[ids.length];
	                    for (int i = 0; i < ids.length; i++) {
	                        final String id = field.relationType.getName() + "-" + ids[i];
	                        if (!idCache.containsKey(id)) {
	                            throw new RuntimeException("No previous reference found for object of type " + field.name + " with key " + ids[i]);
	                        }
	                        // We now get the primary key
	                        resolvedIds[i] = idCache.get(id).toString();
	                    }
	                    // Set the primary keys instead of the object itself.
	                    // Model.Manager.factoryFor((Class<? extends Model>)field.relationType).keyName() returns the primary key label.
	                    if (Model.class.isAssignableFrom(field.relationType )) {
	                        resolvedYml.put("object." + field.name + "." + Model.Manager.factoryFor((Class<? extends Model>)field.relationType).keyName(), resolvedIds);
	                    } else {
	                        // Might be an embedded object
	                        final String id = field.relationType.getName() + "-" + ids[0];
	                        Object o = idCache.get(id);
	                        // This can be a composite key
	                        if (o.getClass().isArray()) {
	                            for (Object a : (Object[])o) {
	                                for (Field f : field.relationType.getDeclaredFields()) {
	                                    try {
	                                        resolvedYml.put("object." + field.name + "." + f.getName(), new String[] {f.get(a).toString()});
	                                    } catch(Exception e) {
	                                        // Ignores
	                                    }
	                                }
	                            }
	                        } else {
	                            for (Field f : field.relationType.getDeclaredFields()) {
	                                try {
	                                    resolvedYml.put("object." + field.name + "." + f.getName(), new String[] {f.get(o).toString()});
	                                } catch(Exception e) {
	                                    // Ignores
	                                }
	                            }
	                        }
	                    }
	                }

	                resolvedYml.remove("object." + field.name);
	            }
	        }
	        // Returns the map containing the ids to load for this object's relation.
	        return resolvedYml;
	    }
	
 public static Map<String, String[]> serialize(Map<?, ?> entityProperties, String prefix) {

     if (entityProperties == null) {
         return MapUtils.EMPTY_MAP;
     }

     final Map<String, String[]> serialized = new HashMap<String, String[]>();

     for (Object key : entityProperties.keySet()) {

         Object value = entityProperties.get(key);
         if (value == null) {
             continue;
         }
         if (value instanceof Map<?, ?>) {
             serialized.putAll(serialize((Map<?, ?>) value, prefix + "." + key));
         } else if (value instanceof Date) {
             serialized.put(prefix + "." + key.toString(), new String[]{new SimpleDateFormat(DateBinder.ISO8601).format(((Date) value))});
         } else if (Collection.class.isAssignableFrom(value.getClass())) {
             Collection<?> l = (Collection<?>) value;
             String[] r = new String[l.size()];
             int i = 0;
             for (Object el : l) {
                 r[i++] = el.toString();
             }
             serialized.put(prefix + "." + key.toString(), r);
         } else if (value instanceof String && value.toString().matches("<<<\\s*\\{[^}]+}\\s*")) {
             Matcher m = Pattern.compile("<<<\\s*\\{([^}]+)}\\s*").matcher(value.toString());
             m.find();
             String file = m.group(1);
             VirtualFile f = Play.getVirtualFile(file);
             if (f != null && f.exists()) {
                 serialized.put(prefix + "." + key.toString(), new String[]{f.contentAsString()});
             }
         } else {
             serialized.put(prefix + "." + key.toString(), new String[]{value.toString()});
         }
     }

     return serialized;
 }
	
}
