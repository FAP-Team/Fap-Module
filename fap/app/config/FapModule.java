package config;

import play.modules.guice.PlayAbstractModule;
import properties.PropertyPlaceholder;
import properties.PropertyPlaceholderImpl;
import security.Secure;
import security.SecureFap;
import security.SecureFapGen;
import services.FirmaService;
import services.GestorDocumentalService;
import services.RegistroService;
import services.aed.AedGestorDocumentalServiceImpl;
import services.platino.PlatinoFirmaServiceImpl;
import services.platino.RegistroServiceImpl;

public class FapModule extends PlayAbstractModule {

	@Override
	protected void configure() {
		secure();
		propertyPlaceholder();
		gestorDocumental();
		firma();
		registro();
	}
	
	protected void gestorDocumental(){
	    bindLazySingletonOnDev(GestorDocumentalService.class, AedGestorDocumentalServiceImpl.class);
	}
	
	protected void firma(){
	    bindLazySingletonOnDev(FirmaService.class, PlatinoFirmaServiceImpl.class);
	}

	protected void registro(){
	    bindLazySingletonOnDev(RegistroService.class, RegistroServiceImpl.class);
	}
	
	protected void secure() {
		bind(Secure.class).toInstance(new SecureFap(new SecureFapGen(null)));
	}

	protected void propertyPlaceholder() {
		bind(PropertyPlaceholder.class).toInstance(new PropertyPlaceholderImpl());
	}

}
