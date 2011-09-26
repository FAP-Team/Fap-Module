package model;

import org.junit.Assert;
import org.junit.Test;

import models.SemillaExpediente;
import play.test.UnitTest;

public class SemillaExpedienteTest extends UnitTest {

	@Test
	public void semilla(){
		Long id1 = SemillaExpediente.obtenerId();
		Assert.assertNotNull(id1);
		Long id2 = SemillaExpediente.obtenerId();
		Assert.assertNotNull(id2);
		Assert.assertFalse(id1.equals(id2));
	}
	
}
