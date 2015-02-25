package controllers.fap;

import java.util.List;
import java.util.Map;

import org.apache.log4j.MDC;

import models.Agente;

import org.apache.log4j.Logger;

import play.cache.Cache;
import play.data.validation.Validation;
import play.db.jpa.Transactional;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Scope.Session;
import play.mvc.Util;
import play.mvc.Scope.Flash;
import validation.CustomValidation;

public class AgenteController extends Controller {

	private static final ThreadLocal<Agente> agente = new ThreadLocal<Agente>();

	private static Logger log = Logger.getLogger(AgenteController.class);

	@Util
	@Transactional
	public static Agente getAgente() {
		if (!agenteIsConnected()) {
			return null;
		}
	
		Agente a = agente.get();
		if (a == null || !a.isPersistent()) {
			findAgente();
		} 
		
		if (a != null)
			play.Logger.info("Recuperando agente local, Método getAgente: Agente: " + a.username);
		
		return agente.get();
	}

	@Util
	public static void setAgente(Agente newAgente) {
		agente.set(newAgente);
	}

	/**
	 * Recupera el agente conectado de base de datos y lo pone disponible en
	 * agenteLocal
	 */
	@Util
	@Transactional
	public static void findAgente() {
		play.Logger.info("Metodo findAgente: Obteniendo de la sesion: " + Session.current().getId() + " el agente: " + Session.current().get("username"));
		String username = Session.current().get("username");
		Agente a = Agente.find("byUsername", username).first();
		play.Logger.info("Agente encontrado: " + a.username);
		agente.set(a);
		MDC.put("username", a.username);
	}

	/**
	 * Comprueba si hay un agente conectado
	 * 
	 * @return
	 */
	@Util
	public static boolean agenteIsConnected() {
		return Session.current().contains("username");
	}

	@Finally
	static void removeAgente() {
		agente.remove();
		MDC.remove("username");
	}

}
