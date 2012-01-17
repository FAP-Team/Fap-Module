package config;

import javax.inject.Singleton;

import properties.PropertyPlaceholder;
import properties.PropertyPlaceholderImpl;
import security.Secure;
import security.SecureFap;
import security.SecureFapGen;
import services.AedService;
import services.AedServiceImpl;
import services.FirmaService;
import services.FirmaServiceImpl;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceImpl;
import services.ProcedimientosService;
import services.ProcedimientosServiceImpl;
import services.RegistroService;
import services.RegistroServiceImpl;
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

	@Provides
	@Singleton
	final AedService getAedService(PropertyPlaceholder propertyPlaceholder) {
		return new AedServiceImpl(propertyPlaceholder);
	}

	@Provides
	@Singleton
	final TiposDocumentosService getTiposDocumentoService(
			PropertyPlaceholder propertyPlaceholder) {
		return new TiposDocumentosServiceImpl(propertyPlaceholder);
	}

	@Provides
	@Singleton
	final ProcedimientosService getProcedimientosService(
			PropertyPlaceholder propertyPlaceholder,
			TiposDocumentosService tiposDocumentosService) {
		return new ProcedimientosServiceImpl(propertyPlaceholder,
				tiposDocumentosService);
	}

	@Provides
	@Singleton
	final FirmaService getFirmaService(PropertyPlaceholder propertyPlaceholder,
			AedService aedService) {
		return new FirmaServiceImpl(propertyPlaceholder, aedService);
	}

	@Provides
	@Singleton
	final RegistroService getRegistroService(
			PropertyPlaceholder propertyPlaceholder, AedService aedService,
			FirmaService firmaService, GestorDocumentalService gestorDocumentalService) {
		return new RegistroServiceImpl(propertyPlaceholder, aedService,
				firmaService, gestorDocumentalService);
	}

	@Provides
	@Singleton
	final GestorDocumentalService getGestorDocumentalService(
			PropertyPlaceholder propertyPlaceholder, AedService aedService) {
		return new GestorDocumentalServiceImpl(propertyPlaceholder);
	}

	void secure() {
		bind(Secure.class).toInstance(new SecureFap(new SecureFapGen(null)));
	}

	void propertyPlaceholder() {
		bind(PropertyPlaceholder.class).toInstance(
				new PropertyPlaceholderImpl());
	}

}
