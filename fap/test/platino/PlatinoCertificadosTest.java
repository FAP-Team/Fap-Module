package platino;

import org.junit.Assert;
import org.junit.Test;

import aed.AedClient;

import play.test.UnitTest;

public class PlatinoCertificadosTest extends UnitTest {
	
	@Test
	public void validarCertificado(){
		if (!PlatinoActivo.activo)
			return;
		String texto = "Texto de prueba para firma";
		String firma = FirmaClient.firmarPKCS7(texto);
		String certificado = FirmaClient.extraerCertificadoDeFirma(firma);
		Boolean certificadoValido = FirmaClient.validarCertificado(certificado);
		Assert.assertNotNull(firma);
		Assert.assertNotNull(certificado);
		Assert.assertTrue(certificadoValido);
	}

	@Test
	public void extraerInformacion(){
		if (!PlatinoActivo.activo)
			return;
		String texto = "Texto de prueba para firma";
		String firma = FirmaClient.firmarPKCS7(texto);
		Assert.assertNotNull(firma);
		String certificado = FirmaClient.extraerCertificadoDeFirma(firma);
		Assert.assertNotNull(certificado);
		InfoCert info = FirmaClient.extraerInformacion(certificado);
		Assert.assertNotNull(info);
	}
}
