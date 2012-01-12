package config;

import javax.inject.Singleton;

import properties.PropertyPlaceholder;
import properties.PropertyPlaceholderImpl;
import security.Secure;
import security.SecureFap;
import security.SecureFapGen;
import services.AedService;
import services.AedServiceImpl;
import services.AedServiceImpl;
import services.ProcedimientosService;
import services.ProcedimientosServiceImpl;
import services.TiposDocumentosService;
import services.TiposDocumentosServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class FapModule extends AbstractModule {

	@Override
	protected void configure() {
		secure();
		propertyPlaceholder();
	}

	@Provides @Singleton
	final AedService getAedService(PropertyPlaceholder propertyPlaceholder) {
		return new AedServiceImpl(propertyPlaceholder);
	}
	
	@Provides @Singleton
	final TiposDocumentosService getTiposDocumentoService(PropertyPlaceholder propertyPlaceholder) {
		return new TiposDocumentosServiceImpl(propertyPlaceholder);
	}
	
	@Provides @Singleton
	final ProcedimientosService getProcedimientosService(PropertyPlaceholder propertyPlaceholder, TiposDocumentosService tiposDocumentosService){
		return new ProcedimientosServiceImpl(propertyPlaceholder, tiposDocumentosService);
	}
	
	void secure(){
		bind(Secure.class).toInstance(new SecureFap(new SecureFapGen(null)));
	}
	
	void propertyPlaceholder(){
		bind(PropertyPlaceholder.class).toInstance(new PropertyPlaceholderImpl());
	}
	
}

