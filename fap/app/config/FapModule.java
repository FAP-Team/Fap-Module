package config;

import javax.inject.Singleton;

import play.modules.guice.PlayAbstractModule;
import properties.PropertyPlaceholder;
import properties.PropertyPlaceholderImpl;
import security.Secure;
import security.SecureFap;
import security.SecureFapGen;
import services.GestorDocumentalService;
import services.FirmaService;
import services.RegistroService;
import services.aed.AedGestorDocumentalServiceImpl;
import services.aed.ProcedimientosService;
import services.aed.ProcedimientosServiceImpl;
import services.aed.TiposDocumentosService;
import services.aed.TiposDocumentosServiceImpl;
import services.platino.FirmaServiceImpl;
import services.platino.GestorDocumentalPlatinoService;
import services.platino.GestorDocumentalServiceImpl;
import services.platino.RegistroServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Stage;

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
	    bindLazySingletonOnDev(FirmaService.class, FirmaServiceImpl.class);
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
