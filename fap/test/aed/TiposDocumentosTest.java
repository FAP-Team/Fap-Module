package aed;

import junit.framework.Assert;

import org.junit.Test;

import play.test.UnitTest;

public class TiposDocumentosTest extends UnitTest {

	@Test
	public void getVersion() throws Exception {
		String version = TiposDocumentosClient.getVersion();
		Assert.assertNotNull(version);
	}

	@Test
	public void actualizarDB(){
		boolean ok = TiposDocumentosClient.actualizarTiposDocumentoDB();
		Assert.assertTrue(ok);
	}
	
}
