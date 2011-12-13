package config;

import org.junit.Assert;
import org.junit.Test;

import play.test.UnitTest;

import secure.Secure;

import com.google.inject.Injector;

public class InjectorConfigTest extends UnitTest {

	@Test
	public void injectOk(){
		Injector injector = InjectorConfig.getInjector();
		Assert.assertNotNull(injector);
		Secure secure = injector.getInstance(Secure.class);
		Assert.assertNotNull(secure);
	}
	
}
