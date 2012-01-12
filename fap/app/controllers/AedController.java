
package controllers;

import javax.inject.Inject;

import messages.Messages;
import play.mvc.Util;
import services.ProcedimientosService;
import controllers.gen.AedControllerGen;
			
public class AedController extends AedControllerGen {

	@Inject
	static ProcedimientosService procedimientosService;
	
	public static void actualizarTramites(){
		boolean result = procedimientosService.actualizarTramites();
		if(result){
			Messages.ok("Se actualizaron correctamente los tipos de documentos desde el AED");
		}else{
			Messages.error("Se produjo un error actualizando los tipos de documentos desde el AED");
		}
		Messages.keep();
		redirect("AedController.index");
	}
	
}
