package controllers.fap;

import java.util.List;
import java.util.Map;

import org.apache.log4j.MDC;

import models.Agente;
import org.apache.log4j.Logger;

import play.cache.Cache;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Util;
import play.mvc.Scope.Flash;
import validation.CustomValidation;

public class AgenteController extends Controller {

	private static ThreadLocal<Agente> agente = new ThreadLocal<Agente>();

	private static Logger log = Logger.getLogger(AgenteController.class);

	@Util
	public static Agente getAgente() {
		if (!agenteIsConnected()) {
			return null;
		}
		Agente a = agente.get();
		if (a == null || !a.isPersistent()) {
			findAgente();
		}
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
	public static void findAgente() {
		String username = session.get("username");
		Agente a = Agente.find("byUsername", username).first();
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
		return session.contains("username");
	}

	@Finally
	static void removeAgente() {
		agente.remove();
		MDC.remove("username");
	}

}
