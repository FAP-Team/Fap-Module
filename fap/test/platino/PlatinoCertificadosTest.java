package platino;

import org.junit.Assert;
import org.junit.Test;

import play.test.UnitTest;

public class PlatinoCertificadosTest extends UnitTest {
	
	@Test
	public void validarCertificado(){
		String texto = "Texto de prueba para firma";
		String firma = FirmaClient.firmarPKCS7(texto);
		Assert.assertNotNull(firma);
		String certificado = FirmaClient.extraerCertificadoDeFirma(firma);
		Assert.assertNotNull(certificado);
		Boolean certificadoValido = FirmaClient.validarCertificado(certificado);
		Assert.assertTrue(certificadoValido);
	}

	@Test
	public void extraerInformacion(){
		String texto = "Texto de prueba para firma";
		String firma = FirmaClient.firmarPKCS7(texto);
		Assert.assertNotNull(firma);
		String certificado = FirmaClient.extraerCertificadoDeFirma(firma);
		Assert.assertNotNull(certificado);
		InfoCert info = FirmaClient.extraerInformacion(certificado);
		Assert.assertNotNull(info);
	}
}
