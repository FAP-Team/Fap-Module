package config;

import play.modules.guice.PlayAbstractModule;
import properties.PropertyPlaceholder;
import properties.PropertyPlaceholderImpl;
import security.Secure;
import security.SecureFap;
import security.SecureFapGen;
import services.ComunicacionesInternasService;
import services.ConversorService;
import services.FirmaService;
import services.GestorDocumentalService;
import services.PortafirmaFapService;
import services.PublicarService;
import services.RegistroLibroResolucionesService;
import services.RegistroService;
import services.TercerosService;
import services.MensajeService;
import services.TiposDocumentosService;
import services.aed.AedTiposDocumentosServiceImpl;
import services.filesystem.FileSystemComunicacionesInternasServiceImpl;
import services.VerificarDatosService;
import services.filesystem.FileSystemConversor;
import services.filesystem.FileSystemFirmaServiceImpl;
import services.filesystem.FileSystemGestorDocumentalServiceImpl;
import services.filesystem.FileSystemMensajeServiceImpl;
import services.filesystem.FileSystemNotificacionServiceImpl;
import services.filesystem.FileSystemPortafirmaImpl;
import services.filesystem.FileSystemPublicarServiceImpl;
import services.filesystem.FileSystemRegistroLibroResolucionesServiceImpl;
import services.filesystem.FileSystemRegistroService;
import services.filesystem.FileSystemTercerosServiceImpl;
import services.filesystem.FileSystemTiposDocumentosServiceImpl;
import services.filesystem.FileSystemVerificarDatosServiceImpl;
import services.filesystem.FilesystemTicketingServiceImpl;
import services.notificacion.NotificacionServiceImpl;
import services.openofice.OpenOfficeConversor;
import services.platino.PlatinoTercerosServiceImpl;
import services.platino.PlatinoMensajeServiceImpl;
import services.ticketing.TicketingService;
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
		portafirma();
		publicar();
		registroLibroResoluciones();
		terceros();
		ticketing();
		mensaje();
		comunicacionesInternas();
		verificarDatos();
//		conversor();
		tiposDocumentos();
	}
	
	protected void portafirma() {
		bindLazySingletonOnDev(PortafirmaFapService.class, FileSystemPortafirmaImpl.class);
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

	protected void firma() {
		bindLazySingletonOnDev(FirmaService.class, FileSystemFirmaServiceImpl.class);
	}
	   
	protected void registro(){
		bindLazySingletonOnDev(RegistroService.class, FileSystemRegistroService.class);
	}
	
	protected void publicar() {
		bindLazySingletonOnDev(PublicarService.class, FileSystemPublicarServiceImpl.class);
	}
	
	protected void registroLibroResoluciones() {
		bindLazySingletonOnDev(RegistroLibroResolucionesService.class, FileSystemRegistroLibroResolucionesServiceImpl.class);
	}
	
	protected void ticketing () {
		bindLazySingletonOnDev(TicketingService.class, FilesystemTicketingServiceImpl.class);
	}
	
	protected void mensaje() {
		bindLazySingletonOnDev(MensajeService.class, FileSystemMensajeServiceImpl.class);
	}
	

	protected void comunicacionesInternas(){
		bindLazySingletonOnDev(ComunicacionesInternasService.class, FileSystemComunicacionesInternasServiceImpl.class);
	}
	
	protected void verificarDatos() {
		bindLazySingletonOnDev(VerificarDatosService.class, FileSystemVerificarDatosServiceImpl.class);
	}
	
	protected void secure() {
		bind(Secure.class).toInstance(new SecureFap(new SecureFapGen(null)));
	}

	protected void propertyPlaceholder() {
		bind(PropertyPlaceholder.class).toInstance(new PropertyPlaceholderImpl());
	}

	protected void tiposDocumentos() {
		bindLazySingletonOnDev(TiposDocumentosService.class, AedTiposDocumentosServiceImpl.class);
	}

}
