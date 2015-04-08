package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.hibernate.dialect.function.NoArgSQLFunction;

import controllers.gen.LogOutSedeControllerGen;
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
	 * Comprueba que el usuario está logueado en páginas que requieran login, en el caso de que no
	 * exista un usuario conectado o se produzca un error en el checkeo del usuario,  
	 * se redirije a una página fuera de la aplicación que indica un error de sesión.
	 * 
	 * Se utiliza un hash de sesión en el modelo Agente a modo de firma para verificar que la sesión actual
	 * es del agente que inició la misma. Esto es necesario porque aparecieron problemas de vulnerabilidad de las 
	 * sesiones en la versión 1.2.5 de Play Framework.
	 * 
	 * @throws Throwable
	 */
    @Before
    static void checkAccess() throws Throwable {
        // Authent
        if(!AgenteController.agenteIsConnected()) {
        	String httpPath = Play.configuration.getProperty("http.path", "/");
        	if(request.method == "GET" && !request.url.equals(httpPath)){
        		flash.put("url", request.method == "GET" ? request.url : "/"); // seems a good default
        	}
            SecureController.loginFap();
        }
        
        try {
	        Agente currentAgent = AgenteController.findAgente();
			if (currentAgent != null && currentAgent.sessionHash.equals(Crypto.passwordHash(Session.current().getId()))) {
				if (FapProperties.getBoolean("fap.debug.session.agente"))
					play.Logger.info("Agente encontrado: " + currentAgent.username + " con hash de sesion: " + currentAgent.sessionHash);
			} else {
				throw new Exception("Error verificando agente en checkAccess, agente es null");
			}
        }catch(Throwable e){
        	play.Logger.error("Opss! Hubo un error en la sesión de usuario: " + e.getMessage());
        	flash.put("unAuthorized", "Opss! Hubo un error en la sesión de usuario.");
        	SecureController.logoutFap();
        }
    }
}