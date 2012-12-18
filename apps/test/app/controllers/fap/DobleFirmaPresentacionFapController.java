package controllers.fap;

import models.SolicitudGenerica;
import tramitacion.TramiteFirmaDoble;
import tramitacion.TramiteBase;

public class DobleFirmaPresentacionFapController extends PresentacionFapController{
	
	public static TramiteBase getTramiteObject (Long idSolicitud){
		System.out.println("Devolviendo la solicitud");
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		return new TramiteFirmaDoble(solicitud);
	}

}
