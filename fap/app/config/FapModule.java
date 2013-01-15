package config;

import play.modules.guice.PlayAbstractModule;
import properties.PropertyPlaceholder;
import properties.PropertyPlaceholderImpl;
import security.Secure;
import security.SecureFap;
import security.SecureFapGen;
import services.ConversorService;
import services.FirmaService;
import services.GestorDocumentalService;
import services.RegistroService;
import services.TercerosService;
import services.filesystem.FileSystemConversor;
import services.filesystem.FileSystemFirmaServiceImpl;
import services.filesystem.FileSystemGestorDocumentalServiceImpl;
import services.filesystem.FileSystemNotificacionServiceImpl;
import services.filesystem.FileSystemRegistroService;
import services.filesystem.FileSystemTercerosServiceImpl;
import services.notificacion.NotificacionServiceImpl;
import services.openofice.OpenOfficeConversor;
import services.platino.PlatinoTercerosServiceImpl;
import services.NotificacionService;

public class FapModule extends PlayAbstractModule {

	@Override
	protected void configure() {
		secure();
		propertyPlaceholder();
		gestorDocumental();
		firma();
		registro();
		notificacion();
		terceros();
		conversor();
	}
	
	protected void conversor() {
		bindLazySingletonOnDev(ConversorService.class, OpenOfficeConversor.class);
	}
	
	protected void terceros() {
		bindLazySingletonOnDev(TercerosService.class, FileSystemTercerosServiceImpl.class);
	}
	
	protected void notificacion() {
		bindLazySingletonOnDev(NotificacionService.class, FileSystemNotificacionServiceImpl.class);
	}
	
	protected void gestorDocumental() {
		bindLazySingletonOnDev(GestorDocumentalService.class, FileSystemGestorDocumentalServiceImpl.class);
	}

	protected void firma() {
		bindLazySingletonOnDev(FirmaService.class, FileSystemFirmaServiceImpl.class);
	}
	   
	protected void registro(){
		bindLazySingletonOnDev(RegistroService.class, FileSystemRegistroService.class);
	}
	
	protected void secure() {
		bind(Secure.class).toInstance(new SecureFap(new SecureFapGen(null)));
	}

	protected void propertyPlaceholder() {
		bind(PropertyPlaceholder.class).toInstance(new PropertyPlaceholderImpl());
	}

}
