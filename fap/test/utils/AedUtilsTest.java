package utils;

import org.junit.Test;

import play.test.UnitTest;

import junit.framework.Assert;

public class AedUtilsTest extends UnitTest {
	
	@Test
	public void encriptacionUri(){
		String uri = "hola";
		String uriEncriptada = AedUtils.encriptarUri(uri);
		String uriDesencriptada = AedUtils.desencriptarUri(uriEncriptada);
		Assert.assertEquals(uri, uriDesencriptada);
	}
	
}
