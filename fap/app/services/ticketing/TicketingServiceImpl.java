package services.ticketing;

import messages.Messages;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import properties.FapProperties;

public class TicketingServiceImpl implements TicketingService {

	@Override
	public HttpResponse hazPeticion (String asunto, String ticket) throws TicketingServiceException {
		
    	String urlTicketing = FapProperties.get("fap.login.ticketing.url");
    	
    	HttpResponse wsResponse = WS.url(urlTicketing + "/" + asunto + "/" + ticket).get();
    	if (wsResponse.getStatus() != 200) {
    		if (wsResponse.getStatus() == 404) {
    			play.Logger.error("Ticket no encontrado: "+wsResponse);
    			Messages.error("El ticket no ha sido encontrado");
    		} else if (wsResponse.getStatus() == 500) {
    			play.Logger.error("Error interno en el servicio de ticketing.");
    			Messages.error("Error interno en el servicio de ticketing.");
    		} else {
    			Messages.error("Error en la petición al servicio de ticketing: "+wsResponse);
    		}
    	}
    	play.Logger.info("Petición al servicio de ticketing realizada: "+wsResponse);
    	
    	return wsResponse;
	}

}
