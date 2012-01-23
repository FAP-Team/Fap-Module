
package controllers;

import javax.inject.Inject;

import messages.Messages;
import play.mvc.Util;
import properties.FapProperties;
import services.AedService;
import services.ProcedimientosService;
import controllers.gen.AedControllerGen;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.excepciones.CodigoErrorEnum;
			
public class AedController extends AedControllerGen {

	@Inject
	static ProcedimientosService procedimientosService;
	
	@Inject
	static AedService aedService;
	
	
	public static void actualizarTramites(){
		checkAuthenticity();
		if (!permisoActualizarTramites("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		boolean result = procedimientosService.actualizarTramites();
		if(result){
			Messages.ok("Se actualizaron correctamente los tipos de documentos desde el AED");
		}else{
			Messages.error("Se produjo un error actualizando los tipos de documentos desde el AED");
		}
		Messages.keep();
		redirect("AedController.index");
	}
	

	public static void crearCarpetaTemporal(){
		checkAuthenticity();
		if(!permisoCrearCarpetaTemporal("editar")){
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		String carpeta = FapProperties.get("fap.aed.temporales");
		try {
			
			aedService.crearCarpetaTemporal();
			Messages.ok("La carpeta temporal "+ carpeta + " se creo correctamente");
		}catch(AedExcepcion e){
			if(e.getFaultInfo().getCodigoError() == CodigoErrorEnum.CARPETA_YA_EXISTE){
				Messages.warning("La carpeta " + carpeta + " ya existe");
			}else{
				Messages.error("Se produjo un error creando la carpeta temporal " + carpeta);
				play.Logger.error(e, "Error al crear carpeta temporal");
			}
		}
		crearCarpetaTemporalRender();
	}
	
}
