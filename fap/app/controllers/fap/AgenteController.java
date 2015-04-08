package controllers.fap;

import java.util.List;
import java.util.Map;

import org.apache.log4j.MDC;

import messages.Messages;
import models.Agente;

import org.apache.log4j.Logger;

import play.cache.Cache;
import play.data.validation.Validation;
import play.db.jpa.Transactional;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Scope.Session;
import play.mvc.Util;
import play.mvc.Scope.Flash;
import properties.FapProperties;
import validation.CustomValidation;

public class AgenteController extends Controller {

	private static Logger log = Logger.getLogger(AgenteController.class);

	/**
	 * Recupera el agente conectado y en caso de no encontrarlo devuelve null.
	 * 
	 * Este método está pensado para ser utilizado en cualquier parte del módulo con el fin de recuperar
	 * el agente conectado.
	 */
	@Util
	@Transactional
	public static Agente getAgente(){	
	    Agente currentAgent = null;
		try {
			if (!agenteIsConnected())
				return null;
			
			currentAgent = findAgente();
		} catch (Throwable e) {
			play.Logger.error("Error recuperando agente en getAgente: " + e.getMessage());
		}
		
		return currentAgent;
	}

	/**
	 * Recupera el identificador de agente conectado, lo busca en base de datos y lo devuelve.
	 * 
	 * En el caso que se produzca algún tipo de error se genera una excepción.
	 * 
	 * Este método está pensado para ser utilizado en el checkeo de usuarios mediante la clase CheckAccessController.java
	 * 
	 * @throws Exception 
	 */
	@Util
	@Transactional
	public static Agente findAgente() throws Throwable {
		Agente currentAgent = null;
		try {
			String username =  Session.current().get("username");
			if (FapProperties.getBoolean("fap.debug.session.agente"))
				play.Logger.info("Metodo findAgente: Obteniendo de la sesion: " + Session.current().getId() + " el agente: " + username);
			
			currentAgent = Agente.find("byUsername", username).first();
		}catch(Throwable e){
			throw new Exception("Error buscando agente en findAgente: " + e.getMessage());
		} 
		
		return currentAgent;
	}

	/**
	 * Comprueba si hay un agente conectado
	 * 
	 * @return
	 */
	@Util
	public static boolean agenteIsConnected() {
		return Session.current() != null && Session.current().contains("username");
	}

}
