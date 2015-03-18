package tags;

import static play.templates.JavaExtensions.capFirst;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Nip;
import models.PersonaFisica;
import models.Solicitante;
import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.templates.JavaExtensions;
import validation.ValueFromTable;

public class ReflectionUtils {

	public static Field getFieldByName(String fieldname) {
		String[] pieces = fieldname.split("\\.");

		String baseClazz = "models." + capFirst(pieces[0]);
		try {
			Class<?> clazz = Class.forName(baseClazz);
			if (clazz != null) {
				if (pieces.length > 1) {
					for (int i = 1; i < pieces.length; i++) {
						try {
							Field f = clazz.getField(pieces[i]);

							if (i == (pieces.length - 1)) {
								return f;
							} else {
								clazz = f.getType();
							}
						} catch (Exception e) {
							// if there is a problem reading the field we dont
							// set any value
							e.printStackTrace();
						}
					}
				} else {
					// field.put("value", obj);
				}
			}
		} catch (ClassNotFoundException e) {
			Logger.error("No existe la clase %s", baseClazz);
		}
		return null;
	}

	public static Class<?> getClassByName(String fieldname) {
		String[] pieces = fieldname.split("\\.");

		String baseClazz = "models." + capFirst(pieces[0]);
		Class<?> clazz = null;
		try {
			clazz = Class.forName(baseClazz);
		}
		catch (ClassNotFoundException e) {
			Logger.error("No existe la clase %s", baseClazz);
		}
		return clazz;
	}
	
	/**
	 * Busca la entidad cuyo ID es el pasado por parametro, y la devuelve en una instancia de play.db.jpa.Model
	 * @param fieldname por ejemplo "solicitud.documentacion.doc". La entidad que busca es Solicitud
	 * @param id ID a buscar
	 * @return
	 */
	public static Model findById(String fieldname, Long id) {
		Model model = null;
		try {
			Class<Model> modelClass = (Class<Model>) getClassByName(fieldname);
			model = (Model) modelClass.getMethod("findById", Object.class).invoke(null, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}
	
	
	
	public static Class getClassFromGenericField(Field field) {
		ParameterizedType type = (ParameterizedType) field.getGenericType();
		Type[] typeArguments = type.getActualTypeArguments();
		Class clazz = (Class) typeArguments[0];
		return clazz;
	}

	public static List<String> getFieldsNamesForClass(Class clazz) {
		List<String> names = new ArrayList<String>();
		Field[] fields = clazz.getFields();
		for (int i = 0; i < fields.length; i++) {
			names.add(fields[i].getName());
		}
		return names;
	}
	
	/**
	 * Dada una clase, y un acceso a parametros deveuelve el Field 
	 * Ejemplo:
	 *    getFieldRecusively(Solicitud.class, "solicitud.solicitante.fisica.nombre")
	 * 
	 * @param clazz
	 * @param field
	 * @return
	 */
	public static Field getFieldRecursively(Class clazz, String field) {
		String[] pieces = field.split("\\.");
		Class clazzActual = clazz;
		
		if(clazz == null) throw new IllegalArgumentException("clazz no puede ser nulo");
		
		if (pieces.length > 1) {
			for (int i = 1; i < pieces.length; i++) {
				try {
					String campoActual = pieces[i];
					Field f = clazzActual.getField(campoActual);
					if (i == (pieces.length - 1)) {
						return f;
					} else {
						clazzActual = f.getType();
					}
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
		} else {
			// TODO posible problema, cuando se renderice un campo simple
			return null;
		}
		return null;
	}
	
	/**
	 * Dada una clase, y un acceso a parametros deveuelve el Field 
	 * Ejemplo:
	 *    getFieldRecusivelyFromClass(Solicitud.class, "solicitante.fisica.nombre")
	 * 
	 * @param clazz
	 * @param field
	 * @return
	 */
	public static Field getFieldRecursivelyFromClass(Class clazz, String field) {
		String[] pieces = field.split("\\.");
		Class clazzActual = clazz;
		
		if(clazz == null) throw new IllegalArgumentException("clazz no puede ser nulo");
		
		if (pieces.length > 0) {
			for (int i = 0; i < pieces.length; i++) {
				try {
					String campoActual = pieces[i];
					Field f = clazzActual.getField(campoActual);
					if (i == (pieces.length - 1)) {
						return f;
					} else {
						clazzActual = f.getType();
					}
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
		} else {
			// TODO posible problema, cuando se renderice un campo simple
			return null;
		}
		return null;
	}

	/**
	 * Dada una clase, y un acceso a parametros deveuelve el valor del campo accedido
	 * Ejemplo:
	 *    getValueRecusively(solicitud, "solicitud.solicitante.fisica.nombre")
	 * 
	 * @param o
	 * @param field
	 * @return
	 */
	public static Object getValueRecursively(Object o, String field) {
		Object obj = o;
		String[] pieces = field.split("\\.");
		if (obj != null) {
			if (pieces.length > 1) {
				for (int i = 1; i < pieces.length; i++) {
					try {
						Field f = obj.getClass().getField(pieces[i]);
						try {
							Method getter = obj.getClass().getMethod("get" + JavaExtensions.capFirst(f.getName()));
							obj = getter.invoke(obj, new Object[0]);
						} catch (NoSuchMethodException e) {
							obj = f.get(obj);
						}
						
						if (i == (pieces.length - 1)) {
							return obj;
						}
					} catch (Exception e) {
						// if there is a problem reading the field we dont set
						// any value
						//Logger.warn("" + e);
						return null;
					}
				}
			} else {
				return obj;
			}
		} else {
			return getValueFromMethod(field);
		}

		return null;
	}
	
	private static Object getValueFromMethod (String routeToMethod) {
		// Debemos realizar la llamada al método que contiene el string
		// defino el nombre de clase
		String sClass = routeToMethod.substring(0, routeToMethod.lastIndexOf("."));
		String _method = "";
		try {
			Class clazz = Class.forName(sClass);
			Object object = clazz.newInstance();
			_method = routeToMethod.substring(routeToMethod.lastIndexOf(".")+1, routeToMethod.lastIndexOf("("));
			Method m1 = clazz.getMethod(_method);
			return m1.invoke(object);
		} catch (Exception e) {
			System.out.println("El método no se pudo ejecutar"+routeToMethod+" class=\""+sClass+"\" method=\""+_method+"\"");
		}
		return null;
	}
	
	public static Object getValueFromMethodFromClass (Object object, String method) {
		try {
			Method m1 = object.getClass().getMethod(method);
			return m1.invoke(object);
		} catch (Exception e) {
			System.out.println("El método no se pudo ejecutar "+method+" class= "+object.getClass());
		}
		return null;
	}
	
	public static List<Field> getFieldsOfType(Class clazz, String field){
		Field f = getFieldRecursively(clazz, field);
		if(f == null) return null;
		
		Class typeClass = f.getType();
		List<Field> fields =  new ArrayList<Field>(Arrays.asList(typeClass.getDeclaredFields()));
		if(typeClass.getSuperclass() != play.db.Model.class){
			fields.addAll(Arrays.asList(typeClass.getSuperclass().getDeclaredFields()));
		}
		
		return fields;
	}
	
	public static Class getListClass(Class clazz, String field){
		Field f = getFieldRecursively(clazz, field);
		return getListClass(f);
	}
	
	public static Class getListClass(Field f){
		if(f == null) return null;
		
		Type genericFieldType = f.getGenericType();
	    
		if(genericFieldType instanceof ParameterizedType){
		    ParameterizedType aType = (ParameterizedType) genericFieldType;
		    Type[] fieldArgTypes = aType.getActualTypeArguments();
		    Class ret = (Class)fieldArgTypes[0];
		    return ret;
		}
		return null;
	}
	
	public static String getNameClass (Object o){
		String clase = o.getClass().getName();
		String[] parser = clase.split("\\.");
		if (parser.length > 0)
			return parser[parser.length-1];
		return null;
	}

    /**
     * Llama al método de un controlador Manual si existe
     * El método debe ser estático, accesible
     * @param method
     * @return
     */
    public static Object callControllerMethodIfExists(final String method) {
        return callControllerMethodIfExists(method, null);
    }
    
    /**
     * Llama al método de un controlador Manual si existe
     * El método debe ser estático, accesible, con o sin parámetros
     * @param method
     * @param args
     * @return
     */
    public static Object callControllerMethodIfExists(final String method, final Map<String, Object> args) {
        Method m = getControllerMethod(method, args);
        
        // Se realiza un segundo intento sin parámetros por mantener la compatibilidad en las aplicaciones antiguas
        boolean noArgs = false;
        if (m == null && args != null) {
            m = getControllerMethod(method, null);
            noArgs = true;
            Logger.warn("Definición del método %s obsoleto. La nueva definición \"public static List<ComboItem> %s(Map<String, Object> args)\"", method, method);
        }

        Object o = null;
        if (m != null) {
            try {
                // Se realiza la llamada al método dependiendo de si está definido con o sin parámetros
                o = (noArgs) ? m.invoke(null, (Object []) null) : m.invoke(null, args);
            } catch (Exception e) {
                // No se ha podido invocar al método
            }
        }
        
        return o;
    }
    
    /**
     * Comprueba si existe el método 'method' en el controlador
     * @param method Nombre del metodo
     * @return true en caso de que existe
     */
    public static boolean existsControllerMethod(final String method) {
    	return existsControllerMethod(method, null);
    }

    /**
     * Comprueba si existe el método 'method' en el controlador con o sin parámetros
     * @param method
     * @param args
     * @return
     */
    public static boolean existsControllerMethod(final String method, final Map<String, Object> args) {
        boolean ret = getControllerMethod(method, args) != null;
        
        // Se realiza un segundo intento sin parámetros por mantener la compatibilidad en las aplicaciones antiguas
        return (!ret && args != null) ? getControllerMethod(method, null) != null : ret;
    }
	
    /**
     * Devuelve el método que está definido en el controlador
     * @param method
     * @return El método o null si no lo se encuentra
     */
    private static Method getControllerMethod(final String method) {
        return getControllerMethod(method, null);
    }
    
    /**
     * Devuelve el método que está definido en el controlador
     * @param method
     * @param args
     * @return
     */
    private static Method getControllerMethod(final String method, final Map<String, Object> args) {
        String controller = (String) play.mvc.Scope.RenderArgs.current().get("controllerName");
        
        //Elimina el sufijo Gen del nombre del controlador, para llamar al controlador manual
        if (controller.endsWith("Gen"))
            controller = controller.substring(0, controller.length() - 3);
    
        //Busca la clase del contorllador, puede ser un controlador de página o de popup
        String pageController = "controllers." + controller;
        String popupController = "controllers.popups." + controller;
        Class clazz = null;
        clazz = Play.classloader.getClassIgnoreCase(pageController);
        if (clazz == null) {
            clazz = Play.classloader.getClassIgnoreCase(popupController);
        }
    
        // Si encuentra la clase, invoca el método si existe en la clase
        Method ret = null;
        if (clazz != null) {
            try {
                /* TODO: realizar un análisis de los parámetros pasados al método a través del parámetro args
                 * por lo pronto se simplifica invocando al método con el map
                 */
                Class<?> parameters [] = { Map.class };
                // Se intenta tomar una instancia del método
                ret = clazz.getMethod(method, (args != null) ? parameters : null);
            } catch (Exception e) {
                // Method not found
            }
        }
    
        return ret;
    }
	
}
