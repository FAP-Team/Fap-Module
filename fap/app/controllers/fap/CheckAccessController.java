package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;


import messages.Messages;
import models.*;

import platino.FirmaClient;
import platino.InfoCert;
import play.Play;
import play.mvc.*;
import play.mvc.Http.Request;
import play.mvc.Scope.Params;
import play.mvc.Scope.Session;
import play.cache.Cache;
import play.data.validation.*;
import play.db.jpa.JPA;
import play.libs.*;
import play.utils.*;
import properties.FapProperties;
import ugot.recaptcha.Recaptcha;
import ugot.recaptcha.RecaptchaCheck;
import ugot.recaptcha.RecaptchaValidator;


public class CheckAccessController extends Controller{

	/**
	 * Comprueba que el usuario está logueado en páginas que requieran login
	 * @throws Throwable
	 */
    @Before
    static void checkAccess() throws Throwable {
        // Authent
        if(!AgenteController.agenteIsConnected()) {
            flash.put("url", request.method == "GET" ? request.url : "/"); // seems a good default
            SecureController.login();
        }
        AgenteController.findAgente(); //Recupera el agente de base de datos
    }

}