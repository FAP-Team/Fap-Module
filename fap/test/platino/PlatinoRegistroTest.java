package platino;

import models.SolicitudGenerica;

import org.junit.Assert;
import org.junit.Test;

import es.gobcan.platino.servicios.registro.JustificanteRegistro;

import play.db.jpa.JPABase;
import play.test.UnitTest;

public class PlatinoRegistroTest extends UnitTest {

	@Test
	public void normalizarDatos() throws Exception{
		SolicitudGenerica solicitud = SolicitudGenerica.findById(2L);
		DatosRegistro datosRegistro = PlatinoRegistro.getDatosRegistro(solicitud.solicitante, solicitud.registro.oficial, solicitud.expedientePlatino);
		String datosAFirmar = PlatinoRegistro.obtenerDatosAFirmarRegisto(datosRegistro);
		System.out.println(datosAFirmar);
		Assert.assertNotNull(datosAFirmar);
		
		String firma = FirmaClient.firmarPKCS7(datosAFirmar.getBytes("iso-8859-1"));
		//Boolean valida = FirmaClient.verificarPKCS7(datosAFirmar, firma);
		//Assert.assertTrue(valida);
		
		PlatinoRegistro.registroDeEntrada(datosAFirmar, firma);
	}
	
	
	
}
