package utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import messages.Messages;

import exceptions.ModelAccessException;

import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.db.jpa.Model;
import play.mvc.Scope;
import play.mvc.Scope.Params;
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

}
