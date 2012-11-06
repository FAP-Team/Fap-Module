package controllers;

import controllers.fap.PresentacionFapController;
import models.Solicitud;
import tramitacion.TramiteBase;
import tramitacion.TramiteSolicitudApp;

public class PresentarAppController extends PresentacionFapController {
	
	public static TramiteBase getTramiteObject (Long idSolicitud){
        Solicitud solicitud = Solicitud.findById(idSolicitud);
        return new TramiteSolicitudApp(solicitud);
	}
	
}