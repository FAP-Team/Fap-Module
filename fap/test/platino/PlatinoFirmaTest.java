package platino;

import org.junit.Assert;
import org.junit.Test;

import play.test.UnitTest;

public class PlatinoFirmaTest extends UnitTest {

	private void firmaryvalidar(String texto){
		String firma = FirmaClient.firmarPKCS7(texto);
		Assert.assertNotNull(firma);
		Boolean firmacorrecta = FirmaClient.verificarPKCS7(texto, firma);
		Assert.assertTrue(firmacorrecta);		
	}
	
	@Test
	public void firmaPKCS7(){
		firmaryvalidar("Hola, esto es un texto simple sin tildes");
	}
	
	@Test
	public void firmaPKCS7Tildes(){
		firmaryvalidar("Texto con tildes áéíóúÁÉÍÓÚ");
	}
	
}
