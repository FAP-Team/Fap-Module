package aed;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import models.Documento;

import org.junit.Assert;
import org.junit.Test;



import play.Logger;
import play.Play;
import play.test.UnitTest;
import play.vfs.VirtualFile;
import properties.FapProperties;

import es.gobcan.eadmon.aed.ws.*;

public class AedTest extends UnitTest {
	
	static Boolean activo = false;
	static {
		try {
			AedClient.getVersion();
			activo = true; 
			//InetAddress.getByName(FapProperties.get("fap.aed.url")).isReachable(1000);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	public void getVersion() throws Exception {
		if (activo) {
			String version = AedClient.getVersion();
			Assert.assertNotNull(version);
			Logger.info("Version %s", version);
		}
	}

	@Test
	public void subirArchivoTemporal() throws Exception {
		Documento documento = new Documento();
		documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.base");
		documento.descripcion = "prueba";
		
		VirtualFile vf = Play.getVirtualFile("README");
		if (activo) {
			String uri = AedClient.saveDocumentoTemporal(documento, vf.getRealFile());
			Assert.assertNotNull(uri);
		}
	}
	
	@Test
	public void actualizarPropiedades() throws Exception {
		Documento documento = new Documento();
		documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.base");
		documento.descripcion = "prueba";
		VirtualFile vf = Play.getVirtualFile("README");
		if (activo) {
			String uri = AedClient.saveDocumentoTemporal(documento, vf.getRealFile());
		
			documento.descripcion = "nueva descripción que me invento yo";
			AedClient.actualizarTipoDescripcion(documento);
		
			Assert.assertNotNull(uri);
		}
	}
	
}
