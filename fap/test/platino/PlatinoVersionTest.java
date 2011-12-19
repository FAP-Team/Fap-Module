package platino;

import org.junit.Assert;
import org.junit.Test;

import play.Logger;
import play.test.UnitTest;

public class PlatinoVersionTest extends UnitTest {

	@Test
	public void getVersion() {
		if (!PlatinoActivo.activo)
			return;
		String version = FirmaClient.getVersion();
		Assert.assertNotNull(version);
		Logger.info("Platino version %s", version);
	}

}
