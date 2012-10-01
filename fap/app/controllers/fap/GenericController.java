package controllers.fap;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;

import models.Agente;
import models.Participacion;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import play.Play;
import play.cache.Cache;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.exceptions.PlayException;
import play.exceptions.TemplateNotFoundException;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Scope;
import play.mvc.With;
import play.mvc.results.RenderTemplate;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.utils.Java;
import properties.FapProperties;
import utils.BinaryResponse;
import validation.CustomValidation;

import security.Secure;
import javax.inject.Inject;


@With({PropertiesFap.class, MessagesController.class, AgenteController.class})
public class GenericController extends Controller {

	@Inject
	protected static Secure secure;
	
	@Before
	protected static void packageParams() throws Exception {
		//Añade a la pila todos los parámetros que empiezan por id
		//Se utiliza en la propagacion de identificadores
		//tags.TagMapStack.clear();
		Map<String, String> allSimple = params.allSimple();
		Map<String, Long> idParams = (Map<String, Long>) tags.TagMapStack.pop("idParams");
		if (idParams == null || idParams.size() == 0)
			idParams = new HashMap<String, Long>();
		for(Map.Entry<String, String> entry : allSimple.entrySet()){
			if(entry.getKey().startsWith("id")){
				try {
					idParams.put(entry.getKey(), Long.parseLong(entry.getValue()));
				}catch(Exception e){
					//El parámetro no era un long
				}
			}
		}
		tags.TagMapStack.push("idParams", idParams);
	}
	
	protected static void renderBinary(BinaryResponse response){
		try {
			renderBinary(response.contenido.getInputStream(), response.nombre);
		} catch (IOException e) {
			play.Logger.error(e, "Error en renderBinary, no puedo obtener el inputStream");
			error();
		}
	}
	
	private static List<String> getIdParams() {
		Set<String> setParams = params.allSimple().keySet();
		List<String> listIds = new ArrayList<String>();
		Iterator it = setParams.iterator();
		Pattern p = Pattern.compile("^id");
	    Matcher m;
		while (it.hasNext()) {
			String key = (String) it.next();
			m = p.matcher(key);
			if (m.find()) {
				listIds.add(key+"="+params.get(key));
			}
		}
		return listIds;
	}
	
	/**
	 * Establece en la cache las entidades que se están procesando actualmente en la petición actual
	 */
	protected static void setEntidadesProcesando () {
		String threadName = Thread.currentThread().getName();
		if (!FapProperties.getBoolean("fap.cache"))
			return;
		
		HashMap<String, String> idsEntidades = (HashMap<String, String>) Cache.get("entidadesProcesando");
		if (idsEntidades == null) {
			idsEntidades = new HashMap<String, String>();
			Cache.safeSet("entidadesProcesando", idsEntidades, FapProperties.get("fap.cache.time"));
		}
		List<String> paramsId = getIdParams();
		boolean cambio = false;
		for (String par: paramsId) {
			if (isEntidadProcesando(par)) {
				play.Logger.error(threadName+" La entidad "+par+" está siendo procesada");
				CustomValidation.error(" La entidad está siendo procesada, vuelva a intentarlo en unos "+FapProperties.get("fap.cache.time")+" minutos.", "", null);
			} else {
				idsEntidades.put(par, threadName);
				cambio = true;
			}
		}
		if (cambio)
			Cache.safeReplace("entidadesProcesando", idsEntidades, FapProperties.get("fap.cache.time"));
	}
	
	protected static void unsetEntidadesProcesando () {
		if (!FapProperties.getBoolean("fap.cache"))
			return;
		String threadName = Thread.currentThread().getName();
		HashMap<String, String> idsEntidades = (HashMap<String, String>) Cache.get("entidadesProcesando");
		List<String> paramsId = getIdParams();
		boolean cambio = false;
		
		for (String par: paramsId) {
			if ((idsEntidades != null) && (idsEntidades.containsKey(par)) && idsEntidades.get(par).equals(threadName)) {
				idsEntidades.remove(par);
				cambio = true;
			}
		}
		if (cambio)
			Cache.safeReplace("entidadesProcesando", idsEntidades, FapProperties.get("fap.cache.time"));
	}
	
	/**
	 * Comprueba si la entidad está siendo procesada en estos momentos
	 * @param param de la forma "idSolicitud=3"
	 * @return
	 */
	private static boolean isEntidadProcesando (String param) {
		int indice = param.indexOf('=');
		if (indice != -1) {
			String paramName = param.substring(0, indice);
			String identificador = param.substring(indice+1, param.length());
			play.Logger.info("Nombre del parámetro: "+paramName+ " ---> "+identificador);
			if (params.get(paramName) != null) {
				HashMap<String, String> idsEnt = (HashMap<String, String>) Cache.get("entidadesProcesando");
				if ((idsEnt != null) && idsEnt.containsKey(param)) {
					return true;
				}
			}
		}
		return false;
	}
}
