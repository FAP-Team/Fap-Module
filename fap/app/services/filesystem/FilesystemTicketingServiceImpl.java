package services.filesystem;

import messages.Messages;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import properties.FapProperties;
import services.ticketing.TicketingService;
import services.ticketing.TicketingServiceException;

public class FilesystemTicketingServiceImpl implements TicketingService {

	@Override
	public HttpResponse hazPeticion(String asunto, String ticket)
			throws TicketingServiceException {

		String urlTicketing = FapProperties.get("fap.login.mock.ticketing.url");
		
    	HttpResponse wsResponse = WS.url(urlTicketing + "/" + asunto + "/ticketing?ticketing=" + ticket).get();
    	if (wsResponse.getStatus() != 200) {
    		if (wsResponse.getStatus() == 404) {
    			play.Logger.error("FS: Ticket no encontrado: "+wsResponse);
    			Messages.error("FS: El ticket no ha sido encontrado");
    		} else if (wsResponse.getStatus() == 500) {
    			play.Logger.error("FS: Error interno en el servicio de ticketing.");
    			Messages.error("FS: Error interno en el servicio de ticketing.");
    		} else {
    			Messages.error("FS: Error en la petición al servicio de ticketing: "+wsResponse);
    		}
    	}
    	play.Logger.info("FS: Petición al servicio de ticketing realizada: "+wsResponse);
    	
    	return wsResponse;
	}

}
