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
import services.PortafirmaFapService;
import services.PublicarService;
import services.RegistroLibroResolucionesService;
import services.RegistroService;
import services.TercerosService;
import services.filesystem.FileSystemConversor;
import services.filesystem.FileSystemFirmaServiceImpl;
import services.filesystem.FileSystemGestorDocumentalServiceImpl;
import services.filesystem.FileSystemNotificacionServiceImpl;
import services.filesystem.FileSystemPortafirmaImpl;
import services.filesystem.FileSystemPublicarServiceImpl;
import services.filesystem.FileSystemRegistroLibroResolucionesServiceImpl;
import services.filesystem.FileSystemRegistroService;
import services.filesystem.FileSystemTercerosServiceImpl;
import services.filesystem.FilesystemTicketingServiceImpl;
import services.async.*;
import services.async.portafirma.PortafirmaServiceAsyncImpl;
import services.async.publicar.PublicarServiceAsyncImpl;
import services.async.ticketing.TicketingServiceAsyncImpl;
import services.async.aed.*;
import services.async.filesystem.FileSystemPublicarServiceAsyncImpl;
import services.notificacion.NotificacionServiceImpl;
import services.openofice.OpenOfficeConversor;
import services.platino.PlatinoTercerosServiceImpl;
import services.ticketing.TicketingService;
import services.NotificacionService;

public class FapModule extends PlayAbstractModule {

	@Override
	protected void configure() {
		secure();
		propertyPlaceholder();
		gestorDocumental();
		gestorDocumentalAsync();
		firma();
		registro();
		notificacion();
		portafirma();
		portafirmaAsync();
		publicar();
		publicarAsync();
		registroLibroResoluciones();
		terceros();
		ticketing();
		ticketingAsync();
//		conversor();
	}
	
	protected void portafirma() {
		bindLazySingletonOnDev(PortafirmaFapService.class, FileSystemPortafirmaImpl.class);
	}
	
	protected void portafirmaAsync() {
		bindLazySingletonOnDev(PortafirmaServiceAsync.class, PortafirmaServiceAsyncImpl.class);
	}
	
//	protected void conversor() {
//		bindLazySingletonOnDev(ConversorService.class, OpenOfficeConversor.class);
//	}
	
	protected void terceros() {
		bindLazySingletonOnDev(TercerosService.class, FileSystemTercerosServiceImpl.class);
	}
	
	protected void notificacion() {
		bindLazySingletonOnDev(NotificacionService.class, FileSystemNotificacionServiceImpl.class);
	}
	
	protected void gestorDocumental() {
		bindLazySingletonOnDev(GestorDocumentalService.class, FileSystemGestorDocumentalServiceImpl.class);
	}
	
	protected void gestorDocumentalAsync() {
		bindLazySingletonOnDev(GestorDocumentalServiceAsync.class, GestorDocumentalServiceAsyncImpl.class);
	}

	protected void firma() {
		bindLazySingletonOnDev(FirmaService.class, FileSystemFirmaServiceImpl.class);
	}
	   
	protected void registro(){
		bindLazySingletonOnDev(RegistroService.class, FileSystemRegistroService.class);
	}
	
	protected void publicar() {
		bindLazySingletonOnDev(PublicarService.class, FileSystemPublicarServiceImpl.class);
	}
	
	protected void publicarAsync() {
		bindLazySingletonOnDev(PublicarServiceAsync.class, PublicarServiceAsyncImpl.class);
	}
	
	protected void registroLibroResoluciones() {
		bindLazySingletonOnDev(RegistroLibroResolucionesService.class, FileSystemRegistroLibroResolucionesServiceImpl.class);
	}
	
	protected void ticketing () {
		bindLazySingletonOnDev(TicketingService.class, FilesystemTicketingServiceImpl.class);
	}
	
	protected void ticketingAsync () {
		bindLazySingletonOnDev(TicketingServiceAsync.class, TicketingServiceAsyncImpl.class);
	}
	
	protected void secure() {
		bind(Secure.class).toInstance(new SecureFap(new SecureFapGen(null)));
	}

	protected void propertyPlaceholder() {
		bind(PropertyPlaceholder.class).toInstance(new PropertyPlaceholderImpl());
	}

}
