package services.async.publicar;

import config.InjectorConfig;
import play.libs.F.Promise;
import services.PublicarService;
import services.async.GenericServiceAsyncImpl;
import services.async.PublicarServiceAsync;

public class PublicarServiceAsyncImpl extends GenericServiceAsyncImpl implements PublicarServiceAsync {
	
	static PublicarService publicarService = InjectorConfig.getInjector().getInstance(PublicarService.class);

	@Override
	public Promise<Integer> mostrarInfoInyeccion() {
		return (Promise<Integer>) execute(publicarService, "mostrarInfoInyeccion");
	}

	@Override
	public Promise<Integer> getInfo() {
		return (Promise<Integer>) execute(publicarService, "getInfo");
	}
	
	public boolean isConfigured() {
		return true;
    }

}
