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
import models.SolicitudGenerica;
import models.TiposCodigoRequerimiento;
import models.Tramite;
import models.TramitesVerificables;
import models.VerificacionTramites;

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
	
	/*
	 * Clase que permite hacer llamadas a los métodos de una clase que extienda de otra, conociendo unicamente el nombre del método que se quiere llamar, los argumentos que se le pasa
	 * y la clase de la que hereda la clase que tiene el método a llamar.
	 * Para ello será necesario que la clase en cuestión (la que no conocemos a priori), sea la única que herede de la clase que conocemos, ya que sino habrá conflicots GRAVES y no
	 * se asegurará el correcto funcionamiento.
	 * PUEDE HABER PROBLEMA CON ALGUNOS METODOS QUE RECIBAN PARAMETROS NO SIMPLES, POR LOS TYPECAST (LISTAS, O TIPOS COMPLEJOS SIMILARES)
	 */
	public static Object invokeMethodClass (Class miClaseExtensionDeClaseHija, Object miObjetoClaseExtension, String metodoMiClaseHija, Object... parametrosMetodoMiClaseHija){
		Class invokedClass = null;
		Object ret = null;
		
		if ((miClaseExtensionDeClaseHija == null) || (miObjetoClaseExtension == null)){
			play.Logger.error("Error 101: Fallo en los parametros de la llamada ModelUtils.invokeMethodClass. Parametros 'miClaseExtensionDeClaseHija': "+miClaseExtensionDeClaseHija+", 'miObjetoClaseExtension': "+miObjetoClaseExtension);
			Messages.error("Error interno 101. No se ha podido Guardar correctamente");
			return null;
		}

        List<Class> assignableClasses = Play.classloader.getAssignableClasses(miClaseExtensionDeClaseHija);
        if(assignableClasses.size() > 0){
        	invokedClass = assignableClasses.get(0);
        	if (assignableClasses.size() > 1)
        		play.Logger.warn("Cuidado!!!: Existen varias clases ("+assignableClasses.size()+") que heredan de "+miClaseExtensionDeClaseHija.getName()+" se usará la clase: "+invokedClass.getName()+" por defecto");
        } else{
        	invokedClass = miClaseExtensionDeClaseHija;
        	play.Logger.warn("Cuidado!!!: No existe una clase que herede de "+miClaseExtensionDeClaseHija.getName()+" se usará esta clase por defecto");
        }

    	Object claseBuscada = invokedClass.cast(miObjetoClaseExtension);
		Method method = null;
		try {
			Class[] clasesDeParametrosMetodoMiClaseHija = new Class[parametrosMetodoMiClaseHija.length];
			int iterador = 0;
			for (Object o: parametrosMetodoMiClaseHija) 
				clasesDeParametrosMetodoMiClaseHija[iterador++] = o.getClass();
			method = invokedClass.getDeclaredMethod(metodoMiClaseHija, clasesDeParametrosMetodoMiClaseHija);
		} catch (Exception ex) {
			play.Logger.warn("Cuidado!!!: No existe una clase que herede de "+miClaseExtensionDeClaseHija.getName()+" y que tenga un método que se llame "+metodoMiClaseHija+" se usará esta clase por defecto");
			invokedClass = miClaseExtensionDeClaseHija;
			claseBuscada = null;
			try {
				Class[] clasesDeParametrosMetodoMiClaseHija = new Class[parametrosMetodoMiClaseHija.length];
				int iterador = 0;
				for (Object o: parametrosMetodoMiClaseHija) 
					clasesDeParametrosMetodoMiClaseHija[iterador++] = o.getClass();
				method = invokedClass.getDeclaredMethod(metodoMiClaseHija, clasesDeParametrosMetodoMiClaseHija);
			} catch (Exception e) {
				play.Logger.error("Error 102b: No se ha podido encontrar el método "+metodoMiClaseHija+" de la clase "+invokedClass.getName());
				Messages.error("Error interno 102. No se ha podido Guardar correctamente");
			}
		}
		if (!Messages.hasErrors()){
			if (method != null){
				try {
					ret = method.invoke(claseBuscada, parametrosMetodoMiClaseHija);
				} catch (Exception e) {
					play.Logger.error("Error 103: No se ha podido invocar el método "+metodoMiClaseHija+" de la clase "+claseBuscada.getClass().getName());
					Messages.error("Error interno 103. No se ha podido Guardar correctamente");
				} 
			} else{
				play.Logger.error("Error 104: No existe el Método apropiado "+metodoMiClaseHija+" de la clase "+claseBuscada.getClass().getName());
				Messages.error("Error interno 104. No se ha podido Guardar correctamente");
			}
		}
		
        return ret;
	}
	
	/*
	 * Clase que permite hacer llamadas a los métodos de una clase estática que extienda de otra, conociendo unicamente el nombre del método que se quiere llamar y los argumentos que se le pasa
	 * Para ello será necesario que la clase estática en cuestión (la que no conocemos a priori), sea la única que herede de la clase que conocemos, ya que sino habrá conflicots GRAVES y no
	 * se asegurará el correcto funcionamiento.
	 * PUEDE HABER PROBLEMA CON ALGUNOS METODOS QUE RECIBAN PARAMETROS NO SIMPLES, POR LOS TYPECAST
	 * CUIDADO, TIENE PROBLEMAS AL PASARLE COMO UNO DE LOS ARGUMENTOS A PASAR AL METODO QUE BUSCAMOS, UNA LISTA, PORQUE EL GETCLASS DEL TIPO LISTA LO RECONOCE COMO UN OBJETO HIBERNATE O ALGO DE ESO
	 */
	public static Object invokeMethodClassStatic (Class miClaseExtensionDeClaseHija, String metodoMiClaseHija, Object... parametrosMetodoMiClaseHija){
		Class invokedClass = null;
		Object ret = null;
		
		if (miClaseExtensionDeClaseHija == null){
			play.Logger.error("Error 101: Fallo en los parametros de la llamada ModelUtils.invokeMethodClass. Parametros 'miClaseExtensionDeClaseHija': "+miClaseExtensionDeClaseHija);
			Messages.error("Error interno 101. No se ha podido Guardar correctamente");
			return null;
		}

        List<Class> assignableClasses = Play.classloader.getAssignableClasses(miClaseExtensionDeClaseHija);
        if(assignableClasses.size() > 0){
        	invokedClass = assignableClasses.get(0);
        	if (assignableClasses.size() > 1)
        		play.Logger.warn("Cuidado!!!: Existen varias clases ("+assignableClasses.size()+") que heredan de "+miClaseExtensionDeClaseHija.getName()+", se usará la clase: "+invokedClass.getName()+" por defecto");
        } else{
        	invokedClass = miClaseExtensionDeClaseHija;
        	play.Logger.warn("Cuidado!!!: No existe una clase que herede de "+miClaseExtensionDeClaseHija.getName()+" se usará esta clase por defecto");
        }

		Method method = null;
		try {
			Class[] clasesDeParametrosMetodoMiClaseHija = new Class[parametrosMetodoMiClaseHija.length];
			int iterador = 0;
			for (Object o: parametrosMetodoMiClaseHija) 
				clasesDeParametrosMetodoMiClaseHija[iterador++] = o.getClass();
			method = invokedClass.getDeclaredMethod(metodoMiClaseHija, clasesDeParametrosMetodoMiClaseHija);
		} catch (Exception ex) {
			invokedClass = miClaseExtensionDeClaseHija;
        	play.Logger.warn("Cuidado!!!: No existe una clase que herede de "+miClaseExtensionDeClaseHija.getName()+" y que contenga un método que se llame "+metodoMiClaseHija+", se usará esta clase por defecto");
			try {
				Class[] clasesDeParametrosMetodoMiClaseHija = new Class[parametrosMetodoMiClaseHija.length];
				int iterador = 0;
				for (Object o: parametrosMetodoMiClaseHija) 
					clasesDeParametrosMetodoMiClaseHija[iterador++] = o.getClass();
				method = invokedClass.getDeclaredMethod(metodoMiClaseHija, clasesDeParametrosMetodoMiClaseHija);
			} catch (Exception e) {
				play.Logger.error("Error 102: No se ha podido encontrar el método "+metodoMiClaseHija+" de la clase "+invokedClass.getName());
				Messages.error("Error interno 102. No se ha podido Guardar correctamente");
			}
		}
		if (!Messages.hasErrors()){
			if (method != null){
				try {
					ret = method.invoke(invokedClass, parametrosMetodoMiClaseHija);
				} catch (Exception e) {
					play.Logger.error("Error 103: No se ha podido invocar el método "+metodoMiClaseHija+" de la clase "+invokedClass.getName());
					Messages.error("Error interno 103. No se ha podido Guardar correctamente");
				} 
			} else{
				play.Logger.error("Error 104: No existe el Método apropiado "+metodoMiClaseHija+" de la clase "+invokedClass.getName());
				Messages.error("Error interno 104. No se ha podido Guardar correctamente");
			}
		}
		
        return ret;
	}
	
	public static void actualizarTramitesVerificables(List<Tramite> tramites){
    	VerificacionTramites vTramites = VerificacionTramites.get(VerificacionTramites.class);
    	boolean encontrado=false;
    	int tamLista = vTramites.tramites.size();
    	// Primero eliminamos los obsoletos
    	for (int i=tamLista-1; i>=0; i--){
    		encontrado = false;
    		for (Tramite t : tramites) {
            	if ((t.existTipoDocumentoAportadoPorCiudadano()) && (t.uri.equals(vTramites.tramites.get(i).uriTramite))){
            		encontrado = true;
            		break;
            	}
            }
            if (!encontrado){ // Borramos
            	TramitesVerificables tvABorrar = vTramites.tramites.get(i);
            	vTramites.tramites.remove(i);
            	vTramites.save();
            	tvABorrar.delete();
            }
        }
    	
    	// Despues creamos los nuevos
    	for (Tramite t : tramites) {
    		encontrado = false;
    		for (TramitesVerificables tv: vTramites.tramites){
            	if (t.uri.equals(tv.uriTramite)){
            		encontrado = true;
            		break;
            	}
            }
            if ((!encontrado) && (t.existTipoDocumentoAportadoPorCiudadano())){ // Creamos
            	TramitesVerificables tvNuevo = new TramitesVerificables();
            	tvNuevo.uriTramite = t.uri;
            	tvNuevo.verificable = true;
            	tvNuevo.save();
            	vTramites.tramites.add(tvNuevo);
            	vTramites.save();
            }
        }
    }
}
