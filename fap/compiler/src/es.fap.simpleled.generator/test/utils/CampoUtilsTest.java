package utils;

import junit.framework.Assert;
import generator.utils.CampoUtils;

import org.junit.Test;

public class CampoUtilsTest {

	@Test
	public void campoSinAtributo(){
//		String campoSinAtributo = CampoUtils.campoSinAtributo("solicitud.solicitante.nombre");
//		Assert.assertEquals("solicitud.solicitante", campoSinAtributo);
	}
	
	@Test
	public void campoSinEntidad(){
//		Assert.assertEquals("solicitante.nombre", CampoUtils.campoSinEntidad("solicitud.solicitante.nombre"));
//		Assert.assertEquals("solicitante.nombre", CampoUtils.campoSinEntidad("SoLicitud.solicitante.nombre"));
//		Assert.assertEquals("", CampoUtils.campoSinEntidad("SoLicitud"));
	}
	
	@Test
	public void funcionSinEntidad(){
//		Assert.assertEquals("<a href=\"${urlDescarga}\">Descargar</a>", CampoUtils.funcionSinEntidad("<a href=\"${documento.urlDescarga}\">Descargar</a>"));
//		Assert.assertEquals("asdf ${nombre} asdfasf ${apellido}", CampoUtils.funcionSinEntidad("asdf ${solicitante.nombre} asdfasf ${solicitante.apellido}"));
	}
	
	@Test
	public void testSingleton(){
//		Assert.assertTrue(CampoUtils.isSingleton("Agente"));
//		Assert.assertTrue(CampoUtils.isSingleton("Solicitud.hola"));
//		Assert.assertFalse(CampoUtils.isSingleton("agente.hola"));
	}
}
