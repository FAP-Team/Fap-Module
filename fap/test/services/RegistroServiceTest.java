package services;

import static org.junit.Assume.assumeTrue;

import javax.inject.Inject;

import models.SolicitudGenerica;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import platino.DatosRegistro;
import play.modules.guice.InjectSupport;


@InjectSupport
public class RegistroServiceTest {

	@Inject
	static RegistroService registroService;

	@Inject
	static FirmaService firmaService;
	
	@Before
	public void before(){
		assumeTrue(registroService.hasConnection());
	}
	
	@Test
	public void normalizarDatos() throws Exception {
		SolicitudGenerica solicitud = SolicitudGenerica.findById(2L);
		DatosRegistro datosRegistro = registroService.getDatosRegistro(solicitud.solicitante, solicitud.registro.oficial, solicitud.expedientePlatino);
		String datosAFirmar = registroService.obtenerDatosAFirmarRegisto(datosRegistro);
		String firma = firmaService.firmarPKCS7(datosAFirmar.getBytes("iso-8859-1"));
		Boolean valida = firmaService.verificarPKCS7(datosAFirmar, firma);
		registroService.registroDeEntrada(datosAFirmar, firma);
		Assert.assertNotNull(datosAFirmar);
		Assert.assertTrue(valida);
	}
	
}
