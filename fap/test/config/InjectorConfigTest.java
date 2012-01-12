package config;

import org.junit.Assert;
import org.junit.Test;

import play.test.UnitTest;
import properties.PropertyPlaceholder;

import security.Secure;
import services.AedService;
import services.AedServiceImpl;
import services.AedServiceImpl;
import services.ProcedimientosService;
import services.TiposDocumentosService;

import com.google.inject.Injector;

public class InjectorConfigTest extends UnitTest {

	@Test
	public void injectOk(){
		assertInjectedSingleton(Secure.class);
		assertInjectedSingleton(AedService.class);
		assertInjectedSingleton(PropertyPlaceholder.class);
		assertInjectedSingleton(ProcedimientosService.class);
		assertInjectedSingleton(TiposDocumentosService.class);
	}
		
	private void assertInjected(Class clazz){
		Injector injector = InjectorConfig.getInjector();
		Object instance = injector.getInstance(clazz);
		assertNotNull(instance);
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
