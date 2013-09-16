package services.async.ticketing;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Inject;

import config.InjectorConfig;
import controllers.JobsController;

import messages.Messages;
import play.libs.F.Promise;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.jobs.*;
import properties.FapProperties;
import services.GestorDocumentalService;
import services.async.GenericServiceAsyncImpl;
import services.async.TicketingServiceAsync;
import services.ticketing.TicketingService;
import services.ticketing.TicketingServiceException;

public class TicketingServiceAsyncImpl extends GenericServiceAsyncImpl implements TicketingServiceAsync {
	
    static TicketingService ticketingService = InjectorConfig.getInjector().getInstance(TicketingService.class);

	@Override
	public Promise<HttpResponse> hazPeticion (String asunto, String ticket) throws TicketingServiceException {
		Object[] params = {asunto, ticket};
		Class[] types = {String.class, String.class};
		return (Promise<HttpResponse>) execute(ticketingService, "hazPeticion", params, types);
	}

}