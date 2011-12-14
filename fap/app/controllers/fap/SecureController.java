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

public class SecureController extends Controller {

	private static Logger log = Logger.getLogger(SecureController.class);
	
	

    // ~~~ Login
    public static void login() throws Throwable {
    	Http.Cookie remember = request.cookies.get("rememberme");
        if(remember != null && remember.value.indexOf("-") > 0) {
            String sign = remember.value.substring(0, remember.value.indexOf("-"));
            String username = remember.value.substring(remember.value.indexOf("-") + 1);
            if(Crypto.sign(username).equals(sign)) {
                session.put("username", username);
                redirectToOriginalURL();
            }
        }
        flash.keep("url");
        
        
        //Token para firmar y acceder por certificado
        if(FapProperties.getBoolean("fap.login.type.cert")){
        	String sessionid = Session.current().getId();
        	String token = Codec.UUID();
        	Cache.delete(sessionid + "login.cert.token");
        	Cache.add(sessionid + "login.cert.token",token, "5mn");
        	renderArgs.put("token", token);
        }
        //Messages.keep();
        renderTemplate("fap/Secure/login.html");        
    }
   
    /**
     * Login con certificado electronico
     * @param certificado
     * @throws Throwable
     */
    public static void authenticateCertificate(String certificado, String token, String firma) throws Throwable {
    	checkAuthenticity(); //Comprueba token de autenticidad
    	
    	if(!FapProperties.getBoolean("fap.login.type.cert")){
            flash.keep("url");
            Messages.error("El acceso a la aplicación mediante certificado electrónico está desactivado");
            Messages.keep();
            login();   		
    	}
    	
    	String sessionid = Session.current().getId();
    	String serverToken = (String)Cache.get(sessionid + "login.cert.token");
    	
    	//Comprueba que el token firmado sea el correcto
    	log.debug("Server token " + serverToken + " token " + token);
    	if(!token.equals(serverToken)) validation.addError("login-certificado", "El token firmado no es correcto");
    	
    	//Comprueba que la firma es correcta
    	if(!validation.hasErrors()){
    		log.debug("Validando firma");
    		
    		Boolean firmaCorrecta = FirmaClient.verificarPKCS7(token, firma);
    		if(!firmaCorrecta){
    			validation.addError("login-certificado", "La firma no es correcta");
    			log.debug("Firma validada");
    		}
    		
    	}
    	
    	//Valida el certificado
    	String certificadoExtraido = null;
    	if(!validation.hasErrors()){
    		log.debug("Validando certificado");
    		certificadoExtraido = FirmaClient.extraerCertificadoDeFirma(firma);
    		Boolean certificadoValido = FirmaClient.validarCertificado(certificadoExtraido);
    		if(!certificadoValido) validation.addError("login-certificado", "El certificado no es válido");
    		log.debug("Certificado validado");
    	}
    	
    	//Extrae la información del certificado
    	String username = null;
    	String name = null;
    	if(!validation.hasErrors()){
    		log.debug("Extrayendo información del certificado");
    		InfoCert info = FirmaClient.extraerInformacion(certificadoExtraido);
    		if(info == null) {
    			validation.addError("login-certificado", "No se pudo extraer la información del certificado");
    		}else{
    			username = info.getId();
    			name = info.getNombreCompleto();
    			if(username == null) validation.addError("login-certificado", "No se pudo extraer la información del certificado");
    			
    			log.debug("Información del certificado" + info);
    		}
    		
    	}
    	
    	//Si hay errores redirige a la página de login
    	if(validation.hasErrors()){
            flash.keep("url");
            Messages.keep();
            login();
    	}
    	
    	//Busca el agente en la base de datos, si no existe lo crea
		Agente agente = Agente.find("byUsername", username).first();
		if(agente == null){
			log.debug("El agente no existe en la base de datos");
			//El agente no existe, hay que crear uno nuevo
			agente = new Agente();
			agente.username = username;
			agente.roles.add("usuario");
			agente.rolActivo = "usuario";
			agente.name = name;
			
		}else{
			if(agente.name == null){
				agente.name = name;
			}
		}
		
		//Almacena el modo de acceso del agente
		agente.acceso = "certificado";
		agente.save();

		//Almacena el usuario en la sesion
		session.put("username", agente.username);
		
		redirectToOriginalURL();
    }    

    /**
     * Login con usuario y contraseña
     * @param username
     * @param password
     * @param remember
     * @throws Throwable
     */
    public static void authenticate(@Required String username, String password, boolean remember) throws Throwable {
    	checkAuthenticity();

        int accesosFallidos = 0;
        if (session.get("accesoFallido") != null) {
        	accesosFallidos = new Integer(session.get("accesoFallido"));
        }
        
        if (accesosFallidos > 2) {
        	boolean valido = RecaptchaValidator.checkAnswer(Request.current(), Params.current());
        	if (valido == false){
        		flash.keep("url");
        		Messages.error(play.i18n.Messages.get("validation.recaptcha"));
        		Messages.keep();
        		login();
        	}
    	}

        if(!FapProperties.getBoolean("fap.login.type.user")){
            flash.keep("url");
            Messages.error("El acceso a la aplicación mediante usuario y contraseña está desactivado");
            Messages.keep();
            login();   		
    	}
    	    	
    	String cryptoPassword = Crypto.passwordHash(password);
    	log.info("Login con usuario y contraseña. User: " +  username + ", Pass: " + cryptoPassword);
    	// Check tokens
        Boolean allowed = false;

    	Agente agente = null;
    	if(username.contains("@")){
    		//Correo
    		agente = Agente.find("byEmail", username).first();
    	}else{
    		//Nip
    		agente = Agente.find("byUsername", username).first();
    	}
    	
    	log.debug("Agente encontrado " + agente);
    	
    	if(agente != null){
    		if(Play.mode.isDev()){
    			//En modo desarollo se permite hacer login a cualquier usuario
    			allowed = true;
    		}else {
    	        /** Si uno de los passwords es vacío */
    			if (agente.password == null) {
    				allowed = false;
    	        	log.info("No se permite hacer password, porque en BBDD es vacío");
    			} else if ((password.trim().length() == 0)  || (agente.password.trim().length() == 0)) {
    	        	allowed = false;
    	        	log.info("Uno de los Passwords es vacío");
    	        } else {
    	        	log.info("Agente encontrado " + agente);
    				allowed = agente.password.equals(cryptoPassword);
    	        }
    		}
    	}else{
    		log.info("Agente no encontrado");
    	}
    	
    	log.debug("Allowed " + allowed);
    	
    	if(!allowed){
    		//Usuario no encontrado
    		log.warn("Intento de login fallido, user:"+ username+ ", pass:"+cryptoPassword+", IP:"+request.remoteAddress+", URL:"+request.url);
            accesosFallidos = 0;
            if (session.get("accesoFallido") != null) {
            	accesosFallidos = new Integer(session.get("accesoFallido"));
            }
            session.put("accesoFallido", accesosFallidos+1);

            flash.keep("url");
            Messages.error(play.i18n.Messages.get("fap.login.error.user"));
            Messages.keep();
            login();
    	}
        
		//Almacena el modo de acceso del agente
		agente.acceso = "usuario";
		agente.save();

        session.put("accesoFallido", 0);

        // Mark user as connected
        session.put("username", agente.username);
        // Remember if needed
        if(remember) {
            response.setCookie("rememberme", Crypto.sign(agente.username) + "-" + username, "30d");
        }
        
        // Redirect to the original URL (or /)
        redirectToOriginalURL();
    }

    public static void logout() throws Throwable {
    	Cache.delete(session.getId());
        session.clear();
        response.removeCookie("rememberme");
        Messages.warning(play.i18n.Messages.get("fap.logout.ok"));
        Messages.keep();
        redirect("fap.SecureController.login");
    }
    
    @Util
    static void redirectToOriginalURL() throws Throwable {
        String url = flash.get("url");
        if(url == null) {
            url = "SolicitudesController.index";
        }
        redirect(url);
    }
    
	/**
	 * 1) Comprueba que el usuario está logueado en páginas que requieran login
	 * 2) Si es una petición de una solicitud (Ej /solicitud/{id}/...)
	 *    Comprueba que el usuario tenga permisos para ver la solicitud
	 * 3) Comprueba los permisos según las anotaciones del método   
	 * @throws Throwable
	 */
    @Before(unless={"login", "authenticate", "logout", "authenticateCertificate"})
    static void checkAccess() throws Throwable {
        // Authent
        if(!AgenteController.agenteIsConnected()) {
            flash.put("url", request.method == "GET" ? request.url : "/"); // seems a good default
            SecureController.login();
        }
       
        AgenteController.findAgente(); //Recupera el agente de base de datos
        Long idSolicitud = null;

        if(params._contains("idSolicitud")){
        	idSolicitud = Long.parseLong(params.get("idSolicitud"));
        	Map<String, Long> ids = new HashMap<String, Long>();
        	ids.put("idSolicitud", idSolicitud);
			if (!secure.PermissionFap.listaSolicitudes("read", ids, null)) {
        		Messages.fatal("No tiene permisos para acceder a la solicitud");
        		//Messages.keep();
			}
        	renderArgs.put("idSolicitud", idSolicitud);
        	renderArgs.put("idEntidad", idSolicitud);
        }
        
    }

    /**
     * Cambia el rol del usuario
     * Se comprueba que el usuario conectado tenga el rol que se quiera cambiar
     * @param url Dirección a la que redirigir
     * @param rol Rol nuevo
     */
    @Util
    public static void changeRol(String url, String rol){
    	log.debug("Cambiando rol a :" + rol);
    	AgenteController.getAgente().cambiarRolActivo(rol);
    	log.debug("Redirigiendo a :" + url);
    	redirect(url);
    }
        
}