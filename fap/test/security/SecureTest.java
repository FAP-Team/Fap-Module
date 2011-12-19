package security;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import config.InjectorConfig;

import play.test.UnitTest;
import security.Secure;

public class SecureTest extends UnitTest {

	private static Secure secure;
	
	@BeforeClass
	public static void setup(){
		secure = InjectorConfig.getInjector().getInstance(Secure.class);
	}
	
	@Test
	public void secureIsConfigured(){
		assertNotNull(secure);
	}
	
}
