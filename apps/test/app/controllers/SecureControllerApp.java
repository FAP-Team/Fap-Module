package controllers;

import controllers.fap.AgenteController;
import controllers.fap.SecureController;
import enumerado.fap.gen.AccesoAgenteEnum;
import messages.Messages;
import play.cache.Cache;
import play.mvc.Util;
import properties.FapProperties;

public class SecureControllerApp extends SecureController{
	
	@Util
	public void logoutOverwrite(){
	        String redireccion="fap.SecureController.loginFap";
	        if(AgenteController.getAgente().acceso.equals(AccesoAgenteEnum.ticketing.name())){
	            redireccion=FapProperties.get("fap.logout.ticketing.url");          
	        }
	        Cache.delete(session.getId());
	        session.clear();
	        response.removeCookie("rememberme");
	        Messages.info(play.i18n.Messages.get("fap.logout.ok"));
	        Messages.keep();
	        redirect(redireccion);
	}



}
