package model;

import org.junit.Assert;
import org.junit.Test;

import models.ExpedienteAed;
import models.SemillaExpediente;
import play.test.UnitTest;

public class SemillaExpedienteTest extends UnitTest {

	@Test
	public void semilla(){
		ExpedienteAed expediente1 = new ExpedienteAed();
		String id1 = expediente1.asignarIdAed();
		Assert.assertNotNull(id1);
		ExpedienteAed expediente2 = new ExpedienteAed();
		String id2 = expediente2.asignarIdAed();
		Assert.assertNotNull(id2);
		Assert.assertFalse(id1.equals(id2));
	}
	
}
