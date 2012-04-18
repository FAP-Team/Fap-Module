package aed;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

public class ClasificarDocumentos extends UnitTest {
	
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

	/**
	 * Setea el boolean "clasificado" cuando es necesario.
	 * Clasifica los documentos que quedaron sin clasificar (por algún error). Comprueba que teniendo el boolean de
	 * "clasificado" a false, de un error al obtener las propiedades del documento no clasificado y que todo
	 * vaya bien  al obtener las propiedades del documentoClasificado
	 * @throws Exception
	 */
	@Test
	public void clasificarDocumentos() throws Exception {
		List<Documento> lDocs = Documento.findAll();
		play.Logger.info("Existen "+lDocs.size()+" documentos en BBDD");
		for (Documento doc: lDocs) {
			if ((!doc.clasificado) && (doc.uri != null && !(doc.uri.trim().equals("")))) {
				boolean noClasificado = false;
				try {
					AedClient.obtenerPropiedades(doc.uri, false);
				} catch (AedExcepcion e) {
					play.Logger.info("El documento "+doc.id+" con uri <"+doc.uri+"> no se pudo obtener con \"noClasificado\"");
					noClasificado = true;
				}
				if (noClasificado) {
					try {
						AedClient.obtenerPropiedades(doc.uri, true);
						play.Logger.info("Debemos poner true clasificado ya que el documento "+doc.id+" con uri <"+doc.uri+"> no se pudo obtener con \"noClasificado\"");
						doc.clasificado = true;
						doc.save();
					} catch (AedExcepcion e) {
						play.Logger.error("El documento "+doc.id+" con uri <"+doc.uri+"> no se pudo obtener con \"Clasificado\"");
					}
				}
			}
			play.Logger.info("Done clasificar");
		}
	}
	
	
}
