
package controllers;

import javax.inject.Inject;

import messages.Messages;
import play.mvc.Util;
import properties.FapProperties;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.aed.ProcedimientosService;
import controllers.gen.AedControllerGen;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.excepciones.CodigoErrorEnum;
			
public class AedController extends AedControllerGen {
	
	@Inject
	static GestorDocumentalService gestorDocumentalService;
	
	public static void actualizarTramites(){
		checkAuthenticity();
		
		if (!permisoActualizarTramites("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		try {
		    gestorDocumentalService.configure();
		    Messages.ok("Se configuró correctamente el gestor documental");
		}catch(GestorDocumentalServiceException e){
		    play.Logger.error(e, "Error configurando el gestor documental");
		    Messages.error("Se produjo un error configurando el gestor documental");
		}
		
		Messages.keep();
		redirect("AedController.index");
	}
		
}
