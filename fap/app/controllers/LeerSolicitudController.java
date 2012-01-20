
package controllers;

import java.util.Map;

import properties.FapProperties;

import controllers.gen.LeerSolicitudControllerGen;
			
public class LeerSolicitudController extends LeerSolicitudControllerGen {
	
	public static void index(String accion, Long idSolicitud){
		redirect(FapProperties.get("fap.app.firstPage") + "Controller.index", "leer", idSolicitud);
	}
	
}
		