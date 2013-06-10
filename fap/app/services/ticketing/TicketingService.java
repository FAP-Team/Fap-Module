package services.ticketing;

import play.libs.WS.HttpResponse;

public interface TicketingService {

	public HttpResponse hazPeticion (String asunto, String ticket) throws TicketingServiceException;
}
