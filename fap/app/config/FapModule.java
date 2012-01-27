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
import com.google.inject.Stage;

public class FapModule extends AbstractModule {

	@Override
	protected void configure() {
		secure();
		propertyPlaceholder();
	}

	@Provides
	@Singleton
	final AedService getAedService(PropertyPlaceholder propertyPlaceholder,
			Stage stage) {
		return new AedServiceImpl(propertyPlaceholder,
				stage.equals(Stage.PRODUCTION));
	}

	@Provides
	@Singleton
	final TiposDocumentosService getTiposDocumentoService(
			PropertyPlaceholder propertyPlaceholder, Stage stage) {
		return new TiposDocumentosServiceImpl(propertyPlaceholder,
				stage.equals(Stage.PRODUCTION));
	}

	@Provides
	@Singleton
	final ProcedimientosService getProcedimientosService(
			PropertyPlaceholder propertyPlaceholder,
			TiposDocumentosService tiposDocumentosService, Stage stage) {
		return new ProcedimientosServiceImpl(propertyPlaceholder,
				tiposDocumentosService, stage.equals(Stage.PRODUCTION));
	}

	@Provides
	@Singleton
	final FirmaService getFirmaService(PropertyPlaceholder propertyPlaceholder,
			AedService aedService, Stage stage) {
		return new FirmaServiceImpl(propertyPlaceholder, aedService,
				stage.equals(Stage.PRODUCTION));
	}

	@Provides
	@Singleton
	final RegistroService getRegistroService(
			PropertyPlaceholder propertyPlaceholder, AedService aedService,
			FirmaService firmaService,
			GestorDocumentalService gestorDocumentalService, Stage stage) {
		return new RegistroServiceImpl(propertyPlaceholder, aedService,
				firmaService, gestorDocumentalService,
				stage.equals(Stage.PRODUCTION));
	}

	@Provides
	@Singleton
	final GestorDocumentalService getGestorDocumentalService(
			PropertyPlaceholder propertyPlaceholder, AedService aedService,
			Stage stage) {
		return new GestorDocumentalServiceImpl(propertyPlaceholder,
				stage.equals(Stage.PRODUCTION));
	}

	void secure() {
		bind(Secure.class).toInstance(new SecureFap(new SecureFapGen(null)));
	}

	void propertyPlaceholder() {
		bind(PropertyPlaceholder.class).toInstance(
				new PropertyPlaceholderImpl());
	}

}
