package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Solicitud;
import play.mvc.Util;
import services.VerificarDocumentacionService;
import controllers.gen.SubirArchivoAedTestControllerGen;
			
public class SubirArchivoAedTestController extends SubirArchivoAedTestControllerGen {
	
	@Util
	public static void comprobarDocumentosObligatorios(Long idSolicitud) {
		//Sobreescribir este método para asignar una acción al boton
		Solicitud solicitud = SubirArchivoAedTestController.getSolicitud(idSolicitud);
		VerificarDocumentacionService verificar = new VerificarDocumentacionService("solicitud", solicitud.documentacion.documentos);
		List <String> nada = new ArrayList<String>();
		nada.clear();
		verificar.preparaPresentacionTramite(nada);
	}

}
		