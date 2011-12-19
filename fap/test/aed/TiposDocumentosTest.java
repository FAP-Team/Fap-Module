package aed;

import java.net.InetAddress;

import junit.framework.Assert;

import org.junit.Test;

import play.test.UnitTest;
import properties.FapProperties;

public class TiposDocumentosTest extends UnitTest {

	static Boolean activo = false;
	static {
		try {
			TiposDocumentosClient.getVersion();
			activo = true; // InetAddress.getByName(FapProperties.get("fap.aed.tiposdocumentos.url")).isReachable(1000000);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Test
	public void getVersion() throws Exception {
		if (activo) {
			String version = TiposDocumentosClient.getVersion();
			Assert.assertNotNull(version);
		}
	}

	@Test
	public void actualizarDB(){
		if (activo) {
			boolean ok = TiposDocumentosClient.actualizarTiposDocumentoDB();
			Assert.assertTrue(ok);
		}
	}
	
}
