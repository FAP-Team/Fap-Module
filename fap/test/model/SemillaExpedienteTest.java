package model;

import org.junit.Assert;
import org.junit.Test;

import models.ExpedienteAed;
import models.SemillaExpediente;
import play.test.UnitTest;

public class SemillaExpedienteTest extends UnitTest {

	@Test
	public void semilla(){
		ExpedienteAed eAed1 = new ExpedienteAed();
		eAed1.asignarIdAed();
		ExpedienteAed eAed2 = new ExpedienteAed();
		eAed2.asignarIdAed();
		Assert.assertNotNull(eAed1.idAed);
		Assert.assertNotNull(eAed2.idAed);
		Assert.assertFalse(eAed1.idAed.equals(eAed2.idAed));
	}
	
}
