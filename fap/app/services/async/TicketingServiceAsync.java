package services.async;

import play.libs.F.Promise;
import play.libs.WS.HttpResponse;
import services.ticketing.TicketingServiceException;

public interface TicketingServiceAsync {

	public Promise<HttpResponse> hazPeticion (String asunto, String ticket) throws TicketingServiceException;
	
}
