package config;

import org.junit.Test;

import play.test.UnitTest;
import properties.PropertyPlaceholder;
import security.Secure;
import services.GestorDocumentalService;
import services.FirmaService;
import services.RegistroService;
import services.aed.ProcedimientosService;
import services.aed.TiposDocumentosService;

import com.google.inject.Injector;

public class InjectorConfigTest extends UnitTest {

	@Test
	public void injectOk(){
		assertInjectedSingleton(Secure.class);
		assertInjectedSingleton(GestorDocumentalService.class);
		assertInjectedSingleton(PropertyPlaceholder.class);
		assertInjectedSingleton(FirmaService.class);
		assertInjectedSingleton(RegistroService.class);
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
