package config;

import org.junit.Test;

import play.test.UnitTest;
import properties.PropertyPlaceholder;
import security.Secure;
import services.*;
import services.aed.ProcedimientosService;
import services.aed.TiposDocumentosService;
import services.comunicacionesInternas.ComunicacionesInternasService;

import com.google.inject.Injector;
import services.ticketing.TicketingService;

public class InjectorConfigTest extends UnitTest {

	@Test
	public void injectOk(){
		assertInjectedSingleton(Secure.class);
		assertInjectedSingleton(GestorDocumentalService.class);
		assertInjectedSingleton(PropertyPlaceholder.class);
		assertInjectedSingleton(FirmaService.class);
		assertInjectedSingleton(RegistroService.class);
		assertInjectedSingleton(MensajeService.class);
        assertInjectedSingleton(NotificacionService.class);
        assertInjectedSingleton(PortafirmaFapService.class);
        assertInjectedSingleton(PublicarService.class);
        assertInjectedSingleton(RegistroLibroResolucionesService.class);
        assertInjectedSingleton(TercerosService.class);
        assertInjectedSingleton(TicketingService.class);
        assertInjectedSingleton(ComunicacionesInternasService.class);
        assertInjectedSingleton(VerificarDatosService.class);
	}
		
	private void assertInjectedSingleton(Class clazz){
		Injector injector = InjectorConfig.getInjector();
		Object instance = injector.getInstance(clazz);
		assertNotNull(instance);
		Object instance2 = injector.getInstance(clazz);
		assertNotNull(instance2);
		assertSame(instance, instance2);
	}
	
}
