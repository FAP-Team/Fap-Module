package utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

import javax.persistence.EntityTransaction;

import org.apache.commons.collections.MapUtils;
import org.h2.constant.SysProperties;
import org.hibernate.collection.PersistentBag;
import org.joda.time.DateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import controllers.PresentarModificacionFAPController;

import messages.Messages;
import models.CodigoRequerimiento;
import models.Direccion;
import models.JsonPeticionModificacion;
import models.Nip;
import models.Participacion;
import models.Persona;
import models.RegistroModificacion;
import models.RepresentantePersonaFisica;
import models.SolicitudGenerica;
import models.TiposCodigoRequerimiento;
import models.Tramite;
import models.TramitesVerificables;
import models.VerificacionTramites;

import enumerado.fap.gen.EstadosSolicitudEnum;
import exceptions.ModelAccessException;

import play.Play;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.data.binding.types.DateBinder;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.db.jpa.Model;
import play.mvc.Scope;
import play.mvc.Scope.Params;
import play.vfs.VirtualFile;
import tags.ReflectionUtils;
import utils.PeticionModificacion.ValorCampoModificado;
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
	
	public static void restaurarSolicitud(Long idRegistroModificacion, Long idSolicitud, boolean consolidarValoresNuevos){
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		PeticionModificacion peticionModificacion;
		Gson gson = new Gson();
		for (JsonPeticionModificacion json: Lists.reverse(registroModificacion.jsonPeticionesModificacion)){
			peticionModificacion = gson.fromJson(json.jsonPeticion, PeticionModificacion.class);
			if (!peticionModificacion.valoresModificado.isEmpty()){
				aplicarCambios(idSolicitud, peticionModificacion, consolidarValoresNuevos);
				//Añadiendo fechaRestauracion y boolean restaurado
				json.fechaRestauracion = new DateTime().now();
				json.restaurado = true;
			}
		}
	}
	
	public static void restaurarBorrados(Long idRegistroModificacion, Long idSolicitud){
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		PeticionModificacion peticionModificacion;
		Gson gson = new Gson();
		
		for (JsonPeticionModificacion json: registroModificacion.jsonPeticionesModificacion){
			peticionModificacion = gson.fromJson(json.jsonPeticion, PeticionModificacion.class);
			if (!peticionModificacion.valoresBorrados.isEmpty()){
				aplicarRestauracion (idSolicitud, peticionModificacion);
				//Añadiendo fechaRestauracion y boolean restaurado
				json.fechaRestauracion = new DateTime().now();
				json.restaurado = true;
			}
		}
	}
	
	public static void eliminarCreados(Long idRegistroModificacion, Long idSolicitud){
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		PeticionModificacion peticionModificacion;
		Gson gson = new Gson();
		
		for (JsonPeticionModificacion json: registroModificacion.jsonPeticionesModificacion){
			peticionModificacion = gson.fromJson(json.jsonPeticion, PeticionModificacion.class);
			if (!peticionModificacion.valoresCreados.isEmpty())
				aplicarEliminacion (idSolicitud, peticionModificacion);
				//Añadiendo fechaRestauracion y boolean restaurado
				json.fechaRestauracion = new DateTime().now();
				json.restaurado = true;
		}
	}
	
	public static void aplicarRestauracion (Long idSolicitud, PeticionModificacion peticionModificacion){
		EntityTransaction tx = JPA.em().getTransaction();
		String IdSimpleString = peticionModificacion.idSimples.keySet().toString(); //Paso a string los valores
		Pattern pattern = Pattern.compile("(id([^,\\]])*)");
		Matcher matcher = pattern.matcher(IdSimpleString);
		String idAux = "";
		Long idRestaurar = null;
		Boolean restaurado = false;
		while ((matcher.find()) &&(!restaurado)) {
		    idAux = matcher.group(1);
		    idRestaurar = peticionModificacion.idSimples.get(idAux);

			int numeroCampos = peticionModificacion.campoPagina.split("\\.").length; //Num campos de campoPagina
			Model modeloEntidad = null;
			Model modeloEntidadPrimera = null;
			Method metodo = null;
			Class claseEntidad = null;
			String entidad = "";
			int camposRecorridos=1;
			if (idRestaurar != null)
				for (String campo : peticionModificacion.campoPagina.split("\\.")){ //Para cada elemento 
					if (camposRecorridos == 1){
						entidad = tags.StringUtils.firstUpper(campo);
						Long idEntidad = peticionModificacion.idSimples.get("id"+entidad);
						try {
							if (idEntidad != null){
								claseEntidad = Class.forName("models."+entidad);				
								Method findById = claseEntidad.getDeclaredMethod("findById", Object.class);
								modeloEntidad = (Model)findById.invoke(claseEntidad.newInstance(), idEntidad);
								modeloEntidadPrimera = (Model)findById.invoke(claseEntidad.newInstance(), idEntidad);
							}
						} catch (Exception e) {
							play.Logger.error("Error recuperando por reflection la entidad "+entidad+" - "+e.getMessage());
							Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
							Messages.keep();
							break;
						}
					} else {
						if (camposRecorridos == numeroCampos){ // LLEGAMOS AL SETTER
							try { 
								Type tipoO2M = null;
								Class claseEntidadBorrar = null;
								String claseEntidadBorrarString = null;
								entidad = tags.StringUtils.firstUpper(campo);
								metodo = claseEntidad.getMethod("get"+entidad);
								PersistentBag aux = (PersistentBag) metodo.invoke(modeloEntidad);
								Field field = extraerField(claseEntidad, campo);
								Type type = field.getGenericType();  
							    if (type instanceof ParameterizedType) {  
							    	ParameterizedType pt = (ParameterizedType) type;  
							        for (Type t : pt.getActualTypeArguments()) {   
							           Pattern patternClase = Pattern.compile("class (.*)");
							           Matcher matcherClase = patternClase.matcher(t.toString());
							           tipoO2M = t;
							           if(matcherClase.find()) {
							        	   claseEntidadBorrarString = matcherClase.group(1).toString();
							        	   claseEntidadBorrar = Class.forName(claseEntidadBorrarString);
							           }
							        }  
							    }  
							       
							    Method findById = claseEntidadBorrar.getDeclaredMethod("findById", Object.class);
							    Pattern patternModel = Pattern.compile("models\\.(.*)");
						        Matcher matcherModel = patternModel.matcher(claseEntidadBorrarString.toString());
							    if (matcherModel.find())
							    	idRestaurar = peticionModificacion.idSimples.get("id"+matcherModel.group(1));
								
								Object restaurar = findById.invoke(claseEntidadBorrar.newInstance(), idRestaurar);
								aux.add(restaurar);
								modeloEntidad.save();
								restaurado = true;
								break;
							} catch (Exception e) {
								play.Logger.error("Error recuperando por reflection el campo "+entidad+" - "+e.getMessage());
								Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
								Messages.keep();
								break;
							}
						} else { // VAMOS RECUPERANDO GETTERS
							try {
								String claseO2MString = "";
								Type tipoO2M = null;
								entidad = tags.StringUtils.firstUpper(campo);
								metodo = claseEntidad.getMethod("get"+entidad);
//								//CODIGO o2m
								if (metodo.invoke(modeloEntidad) instanceof PersistentBag){					
									Field field = extraerField(claseEntidad, campo);
									Type type = field.getGenericType();  
								    if (type instanceof ParameterizedType) {  
								    	ParameterizedType pt = (ParameterizedType) type;  
								        for (Type t : pt.getActualTypeArguments()) {   
								           Pattern patternClase = Pattern.compile("class models\\.(.*)");
								           Matcher matcherClase = patternClase.matcher(t.toString());
								           tipoO2M = t;
								           if(matcherClase.find()) {
								        	   claseO2MString=matcherClase.group(1).toString();
								           }
								        }  
								    }  
								    Class claseO2M = Class.forName("models."+claseO2MString);
									Method findById = claseO2M.getDeclaredMethod("findById", Object.class);
									Long idEntidad = peticionModificacion.idSimples.get("id"+claseO2MString);
									modeloEntidad = (Model)findById.invoke(claseO2M.newInstance(), idEntidad); //Model
									claseEntidad = Class.forName(modeloEntidad.getClass().getName());
								}else{
									modeloEntidad = (Model) metodo.invoke(modeloEntidad);
									
									//Quitar nombres raros
							        Pattern patternModelo = Pattern.compile("(.*?)\\_\\$\\$\\_.*");
							        Matcher matcherModelo = patternModelo.matcher(modeloEntidad.getClass().getName());
							        
							        if (matcherModelo.find()){
							        	claseEntidad = Class.forName(matcherModelo.group(1));
							        }
							        else{
							        	claseEntidad = Class.forName(modeloEntidad.getClass().getName());	
							        }
								}
							} catch (Exception e) {
								play.Logger.error("Error recuperando por reflection la entidad "+entidad+" - "+e.getMessage());
								Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
								Messages.keep();
								break;
							}
						}
					}
					camposRecorridos++;
				}//While
			}
		if (Messages.hasErrors()){ // Si hubo fallos se recupera todo lo anterior
			tx.rollback();
		}
	}
	
	public static void aplicarCambios(Long idSolicitud, PeticionModificacion peticionModificacion){
		aplicarCambios(idSolicitud, peticionModificacion, false);
	}
	
	public static void aplicarEliminacion (Long idSolicitud, PeticionModificacion peticionModificacion){
		EntityTransaction tx = JPA.em().getTransaction();
		String IdSimpleString = peticionModificacion.idSimples.keySet().toString(); //Paso a string los valores
		Pattern pattern = Pattern.compile("(id([^,\\]])*)");
		Matcher matcher = pattern.matcher(IdSimpleString);
		String idAux = "";
		Long idBorrar = null;
		while(matcher.find()) {
		    idAux = matcher.group(1);
		    idBorrar = peticionModificacion.idSimples.get(idAux);

			int numeroCampos = peticionModificacion.campoPagina.split("\\.").length; //Num campos de campoPagina
			Model modeloEntidad = null;
			Model modeloEntidadPrimera = null;
			Method metodo = null;
			Class claseEntidad = null;
			String entidad = "";
			int camposRecorridos=1;
			if (idBorrar != null)
				if (numeroCampos == 1){
					String campo = peticionModificacion.campoPagina;
					entidad = tags.StringUtils.firstUpper(campo);
					Long idEntidad = peticionModificacion.idSimples.get("id"+entidad);
					try {
						if ((idEntidad != null) && (entidad.equals("Participacion"))){ //CASO PARTICIPACIÓN
							claseEntidad = Class.forName("models."+entidad);				
							Method findById = claseEntidad.getDeclaredMethod("findById", Object.class);
							modeloEntidad = (Model)findById.invoke(claseEntidad.newInstance(), idEntidad);
							modeloEntidad.delete();
							break;
						}
					} catch (Exception e) {
						play.Logger.error("Error recuperando por reflection la entidad "+entidad+" - "+e.getMessage());
						Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
						Messages.keep();
						break;
					}
				} // FINAL DEL ELEMENTO CON UN SOLO ID SIMPLE
				for (String campo : peticionModificacion.campoPagina.split("\\.")){ //Para cada elemento 
					if (camposRecorridos == 1){
						entidad = tags.StringUtils.firstUpper(campo);
						Long idEntidad = peticionModificacion.idSimples.get("id"+entidad);
						try {
							if (idEntidad != null){
								claseEntidad = Class.forName("models."+entidad);				
								Method findById = claseEntidad.getDeclaredMethod("findById", Object.class);
								modeloEntidad = (Model)findById.invoke(claseEntidad.newInstance(), idEntidad);
								modeloEntidadPrimera = (Model)findById.invoke(claseEntidad.newInstance(), idEntidad);
							}
						} catch (Exception e) {
							play.Logger.error("Error recuperando por reflection la entidad "+entidad+" - "+e.getMessage());
							Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
							Messages.keep();
							break;
						}
					} else {
						if (camposRecorridos == numeroCampos){ // LLEGAMOS AL SETTER
							try {
								Class claseEntidadBorrar = null;
								Type tipoO2M = null;
								entidad = tags.StringUtils.firstUpper(campo);
								metodo = claseEntidad.getMethod("get"+entidad);
  								PersistentBag aux = (PersistentBag) metodo.invoke(modeloEntidad);								
								Field field = extraerField(claseEntidad, campo);
								Type type = field.getGenericType();  
							    if (type instanceof ParameterizedType) {  
							    	ParameterizedType pt = (ParameterizedType) type;  
							        for (Type t : pt.getActualTypeArguments()) {   
							           Pattern patternClase = Pattern.compile("class (.*)");
							           Matcher matcherClase = patternClase.matcher(t.toString());
							           tipoO2M = t;
							           if(matcherClase.find()) {
							        	   claseEntidadBorrar = Class.forName(matcherClase.group(1).toString());
							           }
							        }  
							    }  
								Method findById = claseEntidadBorrar.getDeclaredMethod("findById", Object.class);
								Object borrar = findById.invoke(claseEntidadBorrar.newInstance(), idBorrar);
								aux.remove(borrar);
								modeloEntidad.save();
								break;
							} catch (Exception e) {
								play.Logger.error("Error recuperando por reflection el campo "+entidad+" - "+e.getMessage());
								Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
								Messages.keep();
								break;
							}
						} else { // VAMOS RECUPERANDO GETTERS
							try { 
								String claseO2MString = "";
								Type tipoO2M = null;
								entidad = tags.StringUtils.firstUpper(campo);
								metodo = claseEntidad.getMethod("get"+entidad);
								//CODIGO o2m
								if (metodo.invoke(modeloEntidad) instanceof PersistentBag){									
									Field field = extraerField(claseEntidad, campo);
									Type type = field.getGenericType();  
								    if (type instanceof ParameterizedType) {  
								    	ParameterizedType pt = (ParameterizedType) type;  
								        for (Type t : pt.getActualTypeArguments()) {   
								        	Pattern patternClase = Pattern.compile("class models\\.(.*)");
								        	Matcher matcherClase = patternClase.matcher(t.toString());
								        	tipoO2M = t;
								        	if(matcherClase.find()) {
								        	   claseO2MString=matcherClase.group(1).toString();
								        	}
								        }  
								    }  
								    Class claseO2M = Class.forName("models."+claseO2MString);
									Method findById = claseO2M.getDeclaredMethod("findById", Object.class);
									Long idEntidad = peticionModificacion.idSimples.get("id"+claseO2MString);
									modeloEntidad = (Model)findById.invoke(claseO2M.newInstance(), idEntidad); //Model
									claseEntidad = Class.forName(modeloEntidad.getClass().getName());
								}else{
									modeloEntidad = (Model) metodo.invoke(modeloEntidad);
									//Quitar nombres raros
							        Pattern patternModelo = Pattern.compile("(.*?)\\_\\$\\$\\_.*");
							        Matcher matcherModelo = patternModelo.matcher(modeloEntidad.getClass().getName());
							        
							        if (matcherModelo.find()){
							        	claseEntidad = Class.forName(matcherModelo.group(1));
							        }
							        else{
							        	claseEntidad = Class.forName(modeloEntidad.getClass().getName());	
							        }
								}
							} catch (Exception e) {
								play.Logger.error("Error recuperando por reflection la entidad "+entidad+" - "+e.getMessage());
								Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
								Messages.keep();
								break;
							}
						}
					}
					camposRecorridos++;
				}//While--- creo que es end for
			}
		if (Messages.hasErrors()){ // Si hubo fallos se recupera todo lo anterior
			tx.rollback();
		}
	}
	
	// consolidarValoresNuevos, para que los valores que se seteen sean los Nuevos, si no se setearan  los Antiguos (los que había antes de modificar)
	public static void aplicarCambios(Long idSolicitud, PeticionModificacion peticionModificacion, boolean consolidarValoresNuevos){
		EntityTransaction tx = JPA.em().getTransaction();
		for (ValorCampoModificado valor: peticionModificacion.valoresModificado){
			if (Messages.hasErrors())
				break;
			int numeroCampos = valor.nombreCampo.split("\\.").length;
			Model modeloEntidad = null;
			Model modeloEntidadPrimera = null;
			Method metodo = null;
			Class claseEntidad = null;
			String entidad = "";
			int camposRecorridos=1;
			Direccion direccionModel = null;
			boolean direccionBoolean = false;
			Nip nipModel = null;
			boolean nipBoolean = false;
			for (String campo : valor.nombreCampo.split("\\.")){
				if (camposRecorridos == 1){
					entidad = tags.StringUtils.firstUpper(campo);
					Long idEntidad = peticionModificacion.idSimples.get("id"+entidad);
					try {
						if (idEntidad != null){
							claseEntidad = Class.forName("models."+entidad);				
							Method findById = claseEntidad.getDeclaredMethod("findById", Object.class);
							modeloEntidad = (Model)findById.invoke(claseEntidad.newInstance(), idEntidad);
							modeloEntidadPrimera = (Model)findById.invoke(claseEntidad.newInstance(), idEntidad);
						}
					} catch (Exception e) {
						play.Logger.error("Error recuperando por reflection la entidad "+entidad+" - "+e.getMessage());
						Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
						Messages.keep();
						break;
					}
				} else {
					if (camposRecorridos == numeroCampos){ // LLEGAMOS AL SETTER
						try {
							if (entidad.equals("Direccion")){
								direccionBoolean = true;
							}
							if (entidad.equals("Nip")){
								nipBoolean = true;
							}
							entidad = tags.StringUtils.firstUpper(campo);
							Field field = extraerField(claseEntidad, campo);
							if (consolidarValoresNuevos){
								if(direccionBoolean)
									setValueFromTypeAttributeDireccion(claseEntidad, direccionModel, modeloEntidadPrimera, entidad, field, valor.valoresNuevos);
								else if (nipBoolean)
									setValueFromTypeAttributeNip(claseEntidad, nipModel, modeloEntidadPrimera, entidad, field, valor.valoresNuevos);
								else
									setValueFromTypeAttribute(claseEntidad, modeloEntidad, modeloEntidadPrimera, entidad, field, valor.valoresNuevos);
							}else{
								if(direccionBoolean)
									setValueFromTypeAttributeDireccion(claseEntidad, direccionModel, modeloEntidadPrimera, entidad, field, valor.valoresAntiguos);
								else if (nipBoolean)
									setValueFromTypeAttributeNip(claseEntidad, nipModel, modeloEntidadPrimera, entidad, field, valor.valoresAntiguos);
								else
									setValueFromTypeAttribute(claseEntidad, modeloEntidad, modeloEntidadPrimera, entidad, field, valor.valoresAntiguos);
							}
							modeloEntidad.save();
							break;
						} catch (Exception e) {
							play.Logger.error("Error recuperando por reflection el campo "+entidad+" - "+e.getMessage());
							e.printStackTrace();
							Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
							Messages.keep();
							break;
						}
					} else { // VAMOS RECUPERANDO GETTERS
						try { 
							String claseO2MString = "";
							Type tipoO2M = null;
							entidad = tags.StringUtils.firstUpper(campo);
							metodo = claseEntidad.getMethod("get"+entidad);
							//CODIGO o2m
							if (metodo.invoke(modeloEntidad) instanceof PersistentBag){
								Field field = extraerField(claseEntidad, campo);
								Type type = field.getGenericType();  
							    if (type instanceof ParameterizedType) {  
							    	ParameterizedType pt = (ParameterizedType) type;  
							        for (Type t : pt.getActualTypeArguments()) {   
							        	Pattern patternClase = Pattern.compile("class models\\.(.*)");
							        	Matcher matcherClase = patternClase.matcher(t.toString());
							        	tipoO2M = t;
							        	if(matcherClase.find()) {
							        	   claseO2MString=matcherClase.group(1).toString();
							        	}
							        }  
							    }  
							    
							    Class claseO2M = Class.forName("models."+claseO2MString);
								Method findById = claseO2M.getDeclaredMethod("findById", Object.class);
								Long idEntidad = peticionModificacion.idSimples.get("id"+claseO2MString);
								modeloEntidad = (Model)findById.invoke(claseO2M.newInstance(), idEntidad); //Model
								claseEntidad = Class.forName(modeloEntidad.getClass().getName());
							}else{
								if (metodo.invoke(modeloEntidad).getClass().getSimpleName().equals("Direccion")){ //Excepcion  
									direccionModel = (Direccion)metodo.invoke(modeloEntidad);
									direccionBoolean = true;
									//Asignacion
									 Pattern patternModelo = Pattern.compile("(.*?)\\_\\$\\$\\_.*");
								        Matcher matcherModelo = patternModelo.matcher(direccionModel.getClass().getName());
								        if (matcherModelo.find()){
								        	claseEntidad = Class.forName(matcherModelo.group(1));
								        }
								        else{
								        	claseEntidad = Class.forName(direccionModel.getClass().getName());	
								        }
								}
								else if (metodo.invoke(modeloEntidad).getClass().getSimpleName().equals("Nip")){ //Excepcion  
									nipModel = (Nip)metodo.invoke(modeloEntidad);
									nipBoolean = true;
									//Asignacion
									 Pattern patternModelo = Pattern.compile("(.*?)\\_\\$\\$\\_.*");
								        Matcher matcherModelo = patternModelo.matcher(nipModel.getClass().getName());
								        if (matcherModelo.find()){
								        	claseEntidad = Class.forName(matcherModelo.group(1));
								        }
								        else{
								        	claseEntidad = Class.forName(nipModel.getClass().getName());	
								        }
								}
								else{
								//Codigo normal
									modeloEntidad = (Model) metodo.invoke(modeloEntidad);
							        Pattern patternModelo = Pattern.compile("(.*?)\\_\\$\\$\\_.*");
							        Matcher matcherModelo = patternModelo.matcher(modeloEntidad.getClass().getName());
							        if (matcherModelo.find()){
							        	claseEntidad = Class.forName(matcherModelo.group(1));
							        }
							        else{
							        	claseEntidad = Class.forName(modeloEntidad.getClass().getName());	
							        }
								}
							}							
						} catch (Exception e) {
							play.Logger.error("Error recuperando por reflection la entidad "+entidad+" - "+e.getMessage());
							Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
							Messages.keep();
							break;
						}
					}
				}
				camposRecorridos++;
			}
		}
		if (Messages.hasErrors()){ // Si hubo fallos se recupera todo lo anterior
			tx.rollback();
		}
	}

	private static Field extraerField(Class claseEntidad, String campo) throws NoSuchFieldException {
		Field field = null;
		try{
			field = claseEntidad.getDeclaredField(campo); //Solo si campo propio no vale campo padre
		} catch (Exception e) {
			// Caso de que campo sea de padre
			//play.Logger.info ("La execpción es = " +  e);
		}
		
		while (field == null){ //Campo en padre
			Class clasePadre = claseEntidad.getSuperclass();
			try {
			field = clasePadre.getDeclaredField(campo);
			} catch (Exception e){
				// Caso de que campo sea de padre	
			}
			if (field == null){
				claseEntidad = clasePadre;
			}
		}
		return field;
	}
	

	public static void finalizarDeshacerModificacion(Long idSolicitud) {
		if (!Messages.hasErrors()){
			SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
			RegistroModificacion ultimoRegistro = solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1);
			if (ultimoRegistro.fechaCancelacion == null){
				ultimoRegistro.fechaCancelacion=new DateTime();
				ultimoRegistro.save();
			}
			solicitud.estado = solicitud.estadoAntesModificacion;
			solicitud.save();
		}
	}
	
	public static void setValueFromTypeAttribute(Class claseEntidad, Model modeloEntidad, Model entidadAGuardar, String nombreMetodo, Field field, List<String> values){
		//Aqui en donde se vuelven a asignar los valores cambiados
		if (field.getType().equals(String.class)){			
			String value = "";
			if (!values.isEmpty())
				value = values.get(0);
			try {
				Method metodo = claseEntidad.getMethod("set"+nombreMetodo, String.class);
				metodo.invoke(modeloEntidad, value);
				entidadAGuardar.save();
			} catch (Exception e) {
				play.Logger.error("Error al intentar setear el valor "+value+" a través de la función "+nombreMetodo+" - "+e.getMessage());
				Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
				Messages.keep();
				return;
			} 
		} else if (field.getType().equals(Long.class)){
			Long value = null;
			if (!values.isEmpty())
				value = Long.valueOf(values.get(0));
			try {
				Method metodo = claseEntidad.getMethod("set"+nombreMetodo, Long.class);
				metodo.invoke(modeloEntidad, value);
				entidadAGuardar.save();
			} catch (Exception e) {
				play.Logger.error("Error al intentar setear el valor "+value+" a través de la función "+nombreMetodo+" - "+e.getMessage());
				Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
				Messages.keep();
				return;
			}
		} else if (field.getType().equals(Boolean.class)) {
			Boolean value = null;
			if (!values.isEmpty())
				value = Boolean.valueOf(values.get(0));
			try {
				Method metodo = claseEntidad.getMethod("set"+nombreMetodo, Boolean.class);
				metodo.invoke(modeloEntidad, value);
				entidadAGuardar.save();
			} catch (Exception e) {
				play.Logger.error("Error al intentar setear el valor "+value+" a través de la función "+nombreMetodo+" - "+e.getMessage());
				Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
				Messages.keep();
				return;
			}
		} else if (field.getType().equals(boolean.class)) {
			boolean value = false;
			if (!values.isEmpty())
				value = Boolean.valueOf(values.get(0)).booleanValue();
			try {
				Method metodo = claseEntidad.getMethod("set"+nombreMetodo, boolean.class);
				metodo.invoke(modeloEntidad, value);
				entidadAGuardar.save();
			} catch (Exception e) {
				play.Logger.error("Error al intentar setear el valor "+value+" a través de la función "+nombreMetodo+" - "+e.getMessage());
				Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
				Messages.keep();
				return;
			}
		} else if (field.getType().equals(Double.class)){
			Double value = null;
			if (!values.isEmpty())
				value = Double.valueOf(values.get(0));
			try {
				Method metodo = claseEntidad.getMethod("set"+nombreMetodo, Double.class);
				metodo.invoke(modeloEntidad, value);
				entidadAGuardar.save();
			} catch (Exception e) {
				play.Logger.error("Error al intentar setear el valor "+value+" a través de la función "+nombreMetodo+" - "+e.getMessage());
				Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
				Messages.keep();
				return;
			}
		} else if (field.getType().equals(DateTime.class)){
			DateTime value = null;
			if (!values.isEmpty())
				value = DateTime.parse(values.get(0));
			try {
				Method metodo = claseEntidad.getMethod("set"+nombreMetodo, DateTime.class);
				metodo.invoke(modeloEntidad, value);
				entidadAGuardar.save();
			} catch (Exception e) {
				play.Logger.error("Error al intentar setear el valor "+value+" a través de la función "+nombreMetodo+" - "+e.getMessage());
				Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
				Messages.keep();
				return;
			}
		} else if (field.getType().equals(Integer.class)){
			Integer value = null;
			if (!values.isEmpty())
				value = Integer.parseInt(values.get(0));
			try {
				Method metodo = claseEntidad.getMethod("set"+nombreMetodo, Integer.class);
				metodo.invoke(modeloEntidad, value);
				entidadAGuardar.save();
			} catch (Exception e) {
				play.Logger.error("Error al intentar setear el valor "+value+" a través de la función "+nombreMetodo+" - "+e.getMessage());
				Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
				Messages.keep();
				return;
			}
		} else if (field.getType().equals(Set.class)){
			try {
				Method metodo = field.getType().getMethod("clear");
				Method mi = claseEntidad.getMethod("get"+nombreMetodo);
				Object m = mi.invoke(modeloEntidad);
				metodo.invoke(m);
				metodo = field.getType().getMethod("add", Object.class);
				Class tipoSet = ReflectionUtils.getListClass(field);
				for (String str : values){
					setValueSimple(tipoSet, m, metodo, str);
				}
				entidadAGuardar.save();
			} catch (Exception e) {
				play.Logger.error("Error al intentar setear los valores de un Set "+e.getMessage());
				Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
				Messages.keep();
				return;
			}
		} else if (field.getType().equals(List.class)){
			try {
				Method metodo = field.getType().getMethod("clear");
				Class tipoList = ReflectionUtils.getListClass(field);

				Method mi = claseEntidad.getMethod("get"+nombreMetodo);
				Object m = mi.invoke(modeloEntidad);
				metodo.invoke(m);
				metodo = field.getType().getMethod("add", Object.class);
				for (String str : values){
					if (!setValueSimple(tipoList, m, metodo, str)){ // Si es de tipo especial (Entidades)
						Class claseTipo = Class.forName(tipoList.getName());						
						Long idEntidad = getIdEntidad(str);
//						String valor = getValorEntidad(str);
						if (idEntidad == null){
							play.Logger.error("Error al intentar setear los valores de una List. Id no encontrado de la entidad en "+str);
							Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
							Messages.keep();
							return;
						}
						Method findById = claseTipo.getDeclaredMethod("findById", Object.class);
//						Method get = claseTipo.getDeclaredMethod("get", Object.class);
//						Model modelGet = (Model)get.invoke(claseTipo.newInstance(), idEntidad);
						Model instancia = (Model)findById.invoke(claseTipo.newInstance(), idEntidad);
//						Object obj = metodo.invoke(m, modelGet);
//						setValueSimple(instancia.getClass(), m, metodo, valor);
						
						metodo.invoke(m, instancia);
					}
				}
				entidadAGuardar.save();
			} catch (Exception e) {
				play.Logger.error("Error al intentar setear los valores de una List "+e.getMessage());
				Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
				Messages.keep();
				return;
			}
		}else { //entidad nuestra
			String value = null;
			if (!values.isEmpty())
				value = values.get(0);
			try {
				Class clase = Class.forName("models."+nombreMetodo);
				Method metodo = claseEntidad.getMethod("set"+nombreMetodo, clase);
				
				//Obtengo el objeto
				Method metodoObjeto = clase.getDeclaredMethod("findById", Object.class);
				//Obtener el id de value (value = entidad[numero]
				Pattern p = Pattern.compile(".*?\\[([0-9]+)\\]");
			    Matcher m = p.matcher(value);
				Long id = new Long(0);
			    if(m.find()){
			    	id = Long.parseLong(m.group(1));
				    Object objetoNuevo = metodoObjeto.invoke(clase.newInstance(), id);
					metodo.invoke(modeloEntidad, objetoNuevo);
					entidadAGuardar.save();
					modeloEntidad.save();
			    } else {
			    	Messages.error("Se produjo un error recuperando datos para deshacer la modificacion");
			    }

			} catch (Exception e) {
				play.Logger.error("Error al intentar setear el valor "+value+" a través de la función "+nombreMetodo+" - "+e);
				Messages.error("(MIO)Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
				Messages.keep();
				return;
			}
		}
	}
	
	public static boolean setValueSimple(Class tipoSimple, Object instanciaAtributo, Method metodo, String value){
		try {
			if (tipoSimple.equals(String.class)){
				metodo.invoke(instanciaAtributo, value);
				return true;
			} else if (tipoSimple.equals(Long.class)) {
				metodo.invoke(instanciaAtributo, Long.parseLong(value));
				return true;
			} else if (tipoSimple.equals(Boolean.class)) {
				metodo.invoke(instanciaAtributo, Boolean.valueOf(value));
				return true;
			} else if (tipoSimple.equals(boolean.class)) {
				metodo.invoke(instanciaAtributo, Boolean.valueOf(value).booleanValue());
				return true;
			} else if (tipoSimple.equals(Double.class)) {
				metodo.invoke(instanciaAtributo, Double.valueOf(value));
				return true;
			} else if (tipoSimple.equals(DateTime.class)) {
				metodo.invoke(instanciaAtributo, DateTime.parse(value));
				return true;
			} else if (tipoSimple.equals(Integer.class)) {
				metodo.invoke(instanciaAtributo, Integer.parseInt(value));
				return true;
			} else
				return false;
		} catch (Exception e) {
			play.Logger.error("Error al intentar setear los valores de "+instanciaAtributo.getClass()+" - "+e.getMessage());
			Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
			Messages.keep();
		}
		return false;
	}
	
	private static Long getIdEntidad(String entidadToString){
		Pattern p = Pattern.compile(".+?\\[(\\d+)\\](.)+");
	    Matcher m = p.matcher(entidadToString);
	    m.matches();
	    if (m.groupCount() != 0)
	    	return Long.parseLong(m.group(1));
	    return null;
	}
	
	private static String getValorEntidad(String entidadToString){
		Pattern p = Pattern.compile(".+?\\[(\\d+)\\](.)+");
	    Matcher m = p.matcher(entidadToString);
	    m.matches();
	    if (m.groupCount() != 0)
	    	return m.group(2);
	    return null;
	}
	
	public static String getValorSimpleAntiguoModificado (List<ValorCampoModificado> valoresModificado, String nombreCampo){
		for (ValorCampoModificado valor: valoresModificado){
			if (valor.nombreCampo.equals(nombreCampo)){
				return valor.valoresAntiguos.get(0);
			}
		}
		return null;
	}
	
	public static List<String> getValoresListaAntiguoModificado (List<ValorCampoModificado> valoresModificado, String nombreCampo){
		for (ValorCampoModificado valor: valoresModificado){
			if (valor.nombreCampo.equals(nombreCampo)){
				return valor.valoresAntiguos;
			}
		}
		return null;
	}


	public static void setValueFromTypeAttributeDireccion(Class claseEntidad, Direccion modeloEntidad, Model entidadAGuardar, String nombreMetodo, Field field, List<String> values){
		if (field.getType().equals(String.class)){			
			String value = "";
			if (!values.isEmpty())
				value = values.get(0);
			try {
				Method metodo = claseEntidad.getMethod("set"+nombreMetodo, String.class);
				metodo.invoke(modeloEntidad, value);
				entidadAGuardar.save();
			} catch (Exception e) {
				play.Logger.error("Error al intentar setear el valor "+value+" a través de la función "+nombreMetodo+" - "+e.getMessage());
				Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
				Messages.keep();
				return;
			} 
		}
	}
	
	public static void setValueFromTypeAttributeNip(Class claseEntidad, Nip modeloEntidad, Model entidadAGuardar, String nombreMetodo, Field field, List<String> values){
		if (field.getType().equals(String.class)){			
			String value = "";
			if (!values.isEmpty())
				value = values.get(0);
			try {
				Method metodo = claseEntidad.getMethod("set"+nombreMetodo, String.class);
				metodo.invoke(modeloEntidad, value);
				entidadAGuardar.save();
			} catch (Exception e) {
				play.Logger.error("Error al intentar setear el valor "+value+" a través de la función "+nombreMetodo+" - "+e.getMessage());
				Messages.error("Hubo un problema al intentar recuperar un determinado valor. La recuperación no ha finalizado con éxito. Consulte los Logs o vuelva a intentar la acción");
				Messages.keep();
				return;
			} 
		}
	}
}
