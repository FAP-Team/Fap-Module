package controllers.fap;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ivy.util.Message;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import config.InjectorConfig;
import messages.Messages;
import models.Agente;

import org.apache.log4j.Logger;

import enumerado.fap.gen.AccesoAgenteEnum;
import platino.InfoCert;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.libs.Codec;
import play.libs.Crypto;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Scope;
import play.mvc.Http.Request;
import play.mvc.Scope.Params;
import play.mvc.Scope.Session;
import play.mvc.Util;
import play.mvc.With;
import properties.FapProperties;
import reports.Report;
import security.Secure;
import services.FirmaService;
import services.FirmaServiceException;
import services.TercerosService;
import services.TercerosServiceException;
import services.ticketing.TicketingService;
import services.ticketing.TicketingServiceException;
import ugot.recaptcha.Recaptcha;
import ugot.recaptcha.RecaptchaCheck;
import ugot.recaptcha.RecaptchaValidator;
import utils.RoutesUtils;


public class SecureController extends GenericController{

	@Inject
	private static FirmaService firmaService;
	
	private static Logger log = Logger.getLogger(SecureController.class);
	
    // ~~~ Login
    public static void loginFap() {
    	//if (!buscarLoginOverwrite())
    		loginPorDefecto();
    }
    
//    private static boolean buscarLoginOverwrite(){
//    	Class invokedClass = getSecureClass();
//    	Object object=null;
//    	
//    	if (invokedClass != null){
//			Method method = null;
//			try {
//    			object = invokedClass.newInstance();
//    			method = invokedClass.getDeclaredMethod("login");
//    			if (method != null){
//    				method.invoke(object);
//    				return true;
//    			}
//    			else{
//    				log.info("No existe el método login() en la clase "+invokedClass.getName());
//    				return false;
//    			}
//			} catch (Exception e) {
//				log.info("No se puede instanciar la clase propia de la aplicación que se encargará del login, por defecto se usará la autenticación de FAP");
//				return false;
//			}
//    	} else {
//    		log.info("No existe una clase en la aplicación que extienda de SecureController, por defecto se usará la autenticación de FAP");
//    		return false;
//    	}
//    }
    
    @Util
    private static void loginPorDefecto(){
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
    
    public static void authenticateCertificateFap(String certificado, String token, String firma){
    	checkAuthenticity();
    	if (!buscarAuthenticateCertificateOverwrite(certificado, token, firma))
    		authenticateCertificatePorDefecto(certificado, token, firma);
    }
    
    @Util
    private static boolean buscarAuthenticateCertificateOverwrite(String certificado, String token, String firma){
    	Class invokedClass = getSecureClass();
    	Object object=null;
    	
    	if (invokedClass != null){
			Method method = null;
			try {
    			object = invokedClass.newInstance();
    			method = invokedClass.getDeclaredMethod("authenticateCertificate", String.class, String.class, String.class);
    			if (method != null){
    				method.invoke(object, certificado, token, firma);
    				return true;
    			}
    			else{
    				log.info("No existe el método authenticateCertificate() en la clase "+invokedClass.getName());
    				return false;
    			}
			} catch (Exception e) {
				log.info("No se puede instanciar la clase propia de la aplicación que se encargará del authenticateCertificate, por defecto se usará la autenticación de FAP");
				return false;
			}
    	} else {
    		log.info("No existe una clase en la aplicación que extienda de SecureController, por defecto se usará la autenticación de FAP");
    		return false;
    	}
    }
   
    /**
     *  Buscar Logout Sobreescrito
     */
    @Util
    private static String buscarRedireccionLogout(){
    	Class invokedClass = getSecureClass();
    	Object object=null;
    	
    	if (invokedClass != null){
			Method method = null;
			try {
    			object = invokedClass.newInstance();
    			method = invokedClass.getDeclaredMethod("logoutRedireccion");
    			if (method != null){
    				return (String)method.invoke(object);
    			}
    			else{
    				log.info("No existe el método logout() en la clase "+invokedClass.getName());
    				return null;
    			}
			} catch (Exception e) {
				log.info("No se puede instanciar la clase propia de la aplicación que se encargará del logout, por defecto se usará el logout de FAP");
				e.printStackTrace();
				System.out.println("Causa: "+e.getCause());
				e.getLocalizedMessage();
				return null;
			}
    	} else {
    		log.info("No existe una clase en la aplicación que extienda de SecureController, por defecto se usará el logout de FAP");
    		return null;
    	}
    }
    
    /**
     * Login con certificado electronico
     * @param certificado
     * @throws Throwable
     */
    @Util
    public static void authenticateCertificatePorDefecto(String certificado, String token, String firma) {
    	
    	if(!FapProperties.getBoolean("fap.login.type.cert")){
            flash.keep("url");
            Messages.error("El acceso a la aplicación mediante certificado electrónico está desactivado");
            Messages.keep();
            loginFap();   		
    	}
    	
    	String sessionid = Session.current().getId();
    	String serverToken = (String)Cache.get(sessionid + "login.cert.token");
    	
    	//Comprueba que el token firmado sea el correcto
    	if(!token.equals(serverToken)) validation.addError("login-certificado", "El token firmado no es correcto");
    	 
    	//Valida la firma
    	if(!validation.hasErrors()){
    	    try {
    	        boolean firmaCorrecta = firmaService.validarFirmaTexto(token.getBytes(), firma);
    	        if(!firmaCorrecta){
    	            validation.addError("login-certificado", "La firma no es válida");
    	        }
    	    }catch(Exception e){
    	        validation.addError("login-certificado", "Error validando la firma");
    	        play.Logger.error("Error validando la firma: "+e.getMessage());
    	    }
    	}
    	
    	//Obtiene información del certificado
        String username = null;
        String name = null;
        if (!validation.hasErrors()) {
            try {
                InfoCert cert = firmaService.extraerCertificado(firma);
                username = cert.getId();
                name = cert.getNombreCompleto();
            } catch (FirmaServiceException e) {
                log.error(e);
                validation.addError("login-certificado", "El certificado no es válido");
            }
        }

    	
    	//Si hay errores redirige a la página de login
    	if(validation.hasErrors()){
            flash.keep("url");
            Messages.keep();
            loginFap();
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
			if(agente.name == null || agente.acceso == null || !agente.acceso.equals(AccesoAgenteEnum.certificado.name())){
				agente.name = name;
			}
		}
		
		//Almacena el modo de acceso del agente
		agente.acceso = AccesoAgenteEnum.certificado.name();
		agente.save();

		//Almacena el usuario en la sesion
		session.put("username", agente.username);
		
		redirectToOriginalURL();
    }    

    public static void authenticateFap(@Required String username, String password, boolean remember) throws Throwable {
    	checkAuthenticity();
    	if (!buscarAuthenticateOverwrite(username, password, remember))
    		authenticatePorDefecto(username, password, remember);
    }
    
    @Util
    private static boolean buscarAuthenticateOverwrite(String username, String password, boolean remember){
    	Class invokedClass = getSecureClass();
    	Object object=null;
    	
    	if (invokedClass != null){
			Method method = null;
			try {
    			object = invokedClass.newInstance();
    			method = invokedClass.getDeclaredMethod("authenticate", String.class, String.class, boolean.class);
    			if (method != null){
    				System.out.println("Invocando el autenticate");
    				method.invoke(object, username, password, remember);
    				return true;
    			}
    			else{
    				log.info("No existe el método authenticate() en la clase "+invokedClass.getName());
    				return false;
    			}
			} catch (Exception e) {
				log.info("No se puede instanciar la clase propia de la aplicación que se encargará del authenticate, por defecto se usará la autenticación de FAP");
				return false;
			}
    	} else {
    		log.info("No existe una clase en la aplicación que extienda de SecureController, por defecto se usará la autenticación de FAP");
    		return false;
    	}
    }
    
    /**
     * Login con usuario y contraseña
     * @param username
     * @param password
     * @param remember
     * @throws Throwable
     */
    @Util
    public static void authenticatePorDefecto(String username, String password, boolean remember){

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
        		loginFap();
        	}
    	}

        if(!FapProperties.getBoolean("fap.login.type.user")){
            flash.keep("url");
            Messages.error("El acceso a la aplicación mediante usuario y contraseña está desactivado");
            Messages.keep();
            loginFap();   		
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
    			//En modo desarrollo se permite hacer login a cualquier usuario
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
            loginFap();
    	}
        
		//Almacena el modo de acceso del agente
		agente.acceso = AccesoAgenteEnum.usuario.name();
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
    
    private static boolean logoutPorTicketing() {
        return (flash.contains("error_ticketing") 
                || AgenteController.getAgente().acceso.equals(AccesoAgenteEnum.ticketing.name()));
    }

    @Util
    public static void logoutFap() throws Throwable{
    	String redireccion = buscarRedireccionLogout();
    	boolean logoutPorTicketing = logoutPorTicketing();
    	String redireccionTicketing = FapProperties.get("fap.logout.ticketing.url");
    	Cache.delete(session.getId());
        session.clear();
        response.removeCookie("rememberme");
        Messages.info(play.i18n.Messages.get("fap.logout.ok"));
        Messages.keep();
    	if (redireccion != null){
	        redirect(redireccion);
        } else if(redireccionTicketing != null && logoutPorTicketing){
            redirect(redireccionTicketing);
        } else {
        	redirect("fap.SecureController.loginFap");
        }
    }
    
    @Util
    static void redirectToOriginalURL() {
        String url = flash.get("url");
        redirectToUrlOrOriginal(url);
        redirect(url);
    }
    
    static void redirectToUrlOrOriginal(String url) {
        if(url == null) {
            url = RoutesUtils.getDefaultRoute(); 
        }
        redirect(url);
    }
    
    private static Class getSecureClass() {
		Class invokedClass = null;
		//Busca una clase que herede del SecureController
        List<Class> assignableClasses = Play.classloader.getAssignableClasses(SecureController.class);
        if(assignableClasses.size() > 0){
            invokedClass = assignableClasses.get(0);
        }
		return invokedClass;
	}

    /**
     * Cambia el rol del usuario
     * Se comprueba que el usuario conectado tenga el rol que se quiera cambiar
     * @param url Dirección a la que redirigir
     * @param rol Rol nuevo
     */
    @Util
    public static void changeRol(String url, String rol){
    	checkAuthenticity();
    	AgenteController.getAgente().cambiarRolActivo(rol);
    	redirectToUrlOrOriginal(url);
    }
    
    
    public static void authenticateTicketingFap(@Required String idTicket) throws Throwable {
    	//checkAuthenticity();
    	if (!buscarAuthenticateTicketingOverwrite(idTicket))
    		authenticateTicketingPorDefecto(idTicket);
    }
    
    @Util
    private static boolean buscarAuthenticateTicketingOverwrite(String ticket){
    	Class invokedClass = getSecureClass();
    	Object object=null;
    	
    	if (invokedClass != null){
			Method method = null;
			try {
    			object = invokedClass.newInstance();
    			method = invokedClass.getDeclaredMethod("authenticateTicketing", String.class, String.class, String.class);
    			if (method != null){
    				method.invoke(object, ticket);
    				return true;
    			}
    			else{
    				log.info("No existe el método authenticateTicketing() en la clase "+invokedClass.getName());
    				return false;
    			}
			} catch (Exception e) {
				log.info("No se puede instanciar la clase propia de la aplicación que se encargará del authenticateCertificate, por defecto se usará la autenticación de FAP");
				return false;
			}
    	} else {
    		log.info("No existe una clase en la aplicación que extienda de SecureController, por defecto se usará la autenticación de FAP");
    		return false;
    	}
    }
    
    @Util
    public static void authenticateTicketingPorDefecto(String ticket) {
    	
    	if(!FapProperties.getBoolean("fap.login.type.ticketing")){
            flash.keep("url");
            play.Logger.error("Se ha intentado un login con ticketing y está desactivada esta opción. Ticket["+ticket+"]");
            Messages.error("El acceso a la aplicación mediante ticketing electrónico está desactivado");
            Messages.keep();
            loginFap();   		
    	}
    	// Asunto
    	String asunto = FapProperties.get("fap.login.ticketing.sede.asunto");
    	if (asunto == null || asunto.isEmpty()) {
    		play.Logger.error("Se ha intentado un login con ticketing y el asunto no es correcto. Asunto["+asunto+"]  Ticket["+ticket+"]");
    		Messages.error("El acceso a la aplicación mediante ticketing ha fallado");
            Messages.keep();
    		loginFap();
    	}
    
		TicketingService ticketingService = InjectorConfig.getInjector().getInstance(TicketingService.class);
		HttpResponse wsResponse = null;
		try {
			wsResponse = ticketingService.hazPeticion(asunto, ticket);
		} catch (TicketingServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	if (wsResponse == null || wsResponse.getStatus() != 200) {
			try {
				flash.put("error_ticketing","Error obteniendo los datos de terceros");
				flash.keep("error_ticketing");
				logoutFap();
			} catch (Throwable e) {
				log.error("No se ha podido recuperar los datos de terceros: " + e.getMessage());
				return;
			}
    	}
    
		String numDocumento = wsResponse.getJson().getAsJsonObject().get("numDoc").getAsString();
		String tipoDocumento = wsResponse.getJson().getAsJsonObject().get("tipoDoc").getAsString();
		String uriTercero = wsResponse.getJson().getAsJsonObject().get("uri").getAsString();

		TercerosService tercerosService = InjectorConfig.getInjector().getInstance(TercerosService.class);
		tercerosService.mostrarInfoInyeccion();
		Agente agente = new Agente();
		try {
			agente = tercerosService.buscarTercerosAgenteByNumeroIdentificacion (numDocumento,tipoDocumento);
		} catch (TercerosServiceException e) {
			play.Logger.error("No se ha podido recuperar el tercero con numDoc = " + numDocumento + " y uri = " + uriTercero);
			Messages.error("No se ha podido recuperar el tercero con numDoc = " + numDocumento);
			e.printStackTrace();
			
            flash.keep("url");
            Messages.error(play.i18n.Messages.get("fap.login.error.user"));
            Messages.keep();
            loginFap();
		}
		
		if (agente == null) {
			play.Logger.error("El agente recuperado por TercerosService es NULL (numDoc = "+ numDocumento + " y uri = " + uriTercero + ")");
			Messages.error("El agente recuperado por TercerosService es NULL");
			
            flash.keep("url");
            Messages.error(play.i18n.Messages.get("fap.login.error.user"));
            Messages.keep();
            loginFap();
			// TODO: Los demás datos... no se pueden obtener
		} else
			if (session.contains("username") && agente.username != null && agente.username.compareTo(session.get("username")) != 0){
				try {
					log.info("Intentando inicio de sesión mediante ticketig (ticket = "+ ticket + " y agente = " + agente.username + ")");
					Messages.error("Error en la sesión de usuario.");
					flash.put("error_ticketing","Error de autentificación por ticketing");
					flash.keep("error_ticketing");
					logoutFap();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
    	
    	
     	log.info("Login con ticketing. User: "+agente.username);
     	// Check tokens
        Boolean allowed = false;
     	
     	log.debug("Agente encontrado " + agente);
     	
     	if(agente != null){
     		if(Play.mode.isDev()){
     			//En modo desarrollo se permite hacer login a cualquier usuario
     			allowed = true;
     		}else {
     	        // TODO: Comprobar algo?
     		}
     	}
		log.debug("Allowed " + allowed);

		// Almacena el modo de acceso del agente
		agente.acceso = AccesoAgenteEnum.ticketing.name();
		if (agente.getSortRoles().isEmpty()) {
			log.info("Usuario carece de roles, se modificara el campo para permitir el rol de usuario");
			agente.roles.add("usuario");
			agente.cambiarRolActivo("usuario");
		}
		agente.save();

		// Mark user as connected
		session.put("username", agente.username);

		// Redirect to the original URL (or /)
		redirectToOriginalURL();
    }    
        
}