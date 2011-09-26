package aed;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import models.Documento;

import org.junit.Assert;
import org.junit.Test;



import play.Logger;
import play.test.UnitTest;
import properties.FapProperties;

import es.gobcan.eadmon.aed.ws.*;

public class AedTest extends UnitTest {

	@Test
	public void getVersion() throws Exception {	
		String version = AedClient.getVersion();
		Assert.assertNotNull(version);
		Logger.info("Version %s", version);
	}

	@Test
	public void subirArchivoTemporal() throws Exception {
		Documento documento = new Documento();
		documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.base");
		documento.descripcion = "prueba";
		File file = new File("C:/report.pdf");
		
		String uri = AedClient.saveDocumentoTemporal(documento, file);
		Assert.assertNotNull(uri);
	}
	
	@Test
	public void actualizarPropiedades() throws Exception {
		Documento documento = new Documento();
		documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.base");
		documento.descripcion = "prueba";
		File file = new File("C:/report.pdf");
		
		String uri = AedClient.saveDocumentoTemporal(documento, file);
		
		documento.descripcion = "nueva descripción que me invento yo";
		AedClient.actualizarTipoDescripcion(documento);
		
		Assert.assertNotNull(uri);
	}
	
}
