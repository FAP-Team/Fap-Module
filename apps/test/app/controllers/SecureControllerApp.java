package controllers;

import controllers.fap.AgenteController;
import controllers.fap.SecureController;
import enumerado.fap.gen.AccesoAgenteEnum;
import messages.Messages;
import play.cache.Cache;
import play.mvc.Util;


public class SecureControllerApp extends SecureController{
	
	@Util
	public static String logoutRedireccion(){
	        String redireccion="fap.SecureController.loginFap";
	        if(AgenteController.getAgente().acceso.equals(AccesoAgenteEnum.ticketing.name())){
	            redireccion=FapProperties.get("fap.logout.ticketing.url");
	        }
	        return redireccion;
	}

}
