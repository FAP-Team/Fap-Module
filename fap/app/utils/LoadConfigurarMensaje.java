package utils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.ConfigurarMensaje;

import org.apache.commons.collections.MapUtils;

import play.Play;
import play.data.binding.Binder;
import play.data.binding.ParamNode;
import play.data.binding.RootParamNode;
import play.data.binding.types.DateBinder;
import play.db.Model;
import play.db.jpa.JPAPlugin;
import play.db.jpa.Transactional;
import play.vfs.VirtualFile;

public class LoadConfigurarMensaje {

	
	
	 @SuppressWarnings("unchecked")
	    /**
	     *  Resolve dependencies between objects using their keys. For each referenced objects, it sets the foreign key
	     */
	    static Map<String, String[]> resolveDependencies(Class<Model> type, Map<String, String[]> yml, Map<String, Object> idCache) {

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
	
  static Map<String, String[]> serialize(Map<?, ?> entityProperties, String prefix) {

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
	@Transactional
	public static void loadConfigurarMensaje(String paginasFile) {
		Pattern keyPattern = Pattern.compile("([^(]+)\\(([^)]+)\\)");
		Object o = play.test.Fixtures.loadYaml(paginasFile);

		//Obtengo las paginas que ya están almacenadas
		List<ConfigurarMensaje> paginasMensaje = ConfigurarMensaje.findAll();
		
		if (o instanceof LinkedHashMap<?, ?>) {
			@SuppressWarnings("unchecked")
			LinkedHashMap<Object, Map<?, ?>> objects = (LinkedHashMap<Object, Map<?, ?>>) o;
			for (Object key : objects.keySet()) {
				//System.out.println("key: " + key);
				Matcher matcher = keyPattern.matcher(key.toString().trim());
				if (matcher.matches()) {
					// Type of the object. i.e. models.employee
					String type = matcher.group(1);
					// Id of the entity i.e. nicolas
					String id = matcher.group(2);
					//System.out.println("id: " + id + "   type: " + type);

					if (!type.startsWith("models.")) {
						type = "models." + type;
					}

					// Was the entity already defined?

					// Those are the properties that were parsed from the YML
					// file
					final Map<?, ?> entityValues = objects.get(key);

					// Prefix is object, why is that?
					final Map<String, String[]> fields = serialize(
							entityValues, "object");

					try {

						@SuppressWarnings("unchecked")
						Class<Model> cType = (Class<Model>) Play.classloader
								.loadClass(type);
						final Map<String, String[]> resolvedFields = resolveDependencies(
								cType, fields, (Map<String, Object>) o);

						RootParamNode rootParamNode = ParamNode
								.convert(resolvedFields);
						// This is kind of hacky. This basically says that if we
						// have an embedded class we should ignore it.
						if (Model.class.isAssignableFrom(cType)) {

							Model model = (Model) Binder.bind(rootParamNode,
									"object", cType, cType, null);
							for (Field f : model.getClass().getFields()) {
								if (f.getType().isAssignableFrom(Map.class)) {
									f.set(model,
											objects.get(key).get(f.getName()));
								}
								if (f.getType().equals(byte[].class)) {
									f.set(model,
											objects.get(key).get(f.getName()));
								}
							}

							// En este momento tengo el ConfigurarMensaje
							// completo y compruebo que no esté en BBDD
							// Para guardarlo
							// TODO
							
							ConfigurarMensaje prueba = (ConfigurarMensaje) model;
							boolean encontrado = false;
							for (ConfigurarMensaje configurarMensaje : paginasMensaje) {
								if(configurarMensaje.nombrePagina.equals(prueba.nombrePagina)){
									encontrado = true;
									break; //Encontrado, dejamos de buscar.
								}									
							}
							if (!encontrado)
								model._save();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} 
	}
}
