package services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.inject.Inject;

import models.Documento;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import play.Play;
import play.modules.guice.InjectSupport;
import play.test.Fixtures;
import play.test.UnitTest;
import play.vfs.VirtualFile;
import properties.FapProperties;
import properties.MapPropertyPlaceholder;
import properties.PropertyPlaceholder;
import utils.FileUtils;

import aed.AedClient;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import config.InjectorConfig;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;

import static org.junit.Assume.*;

@InjectSupport
public class AedServiceTest extends UnitTest {
	
	@Inject
	private static AedService aedService;
	
	private static boolean hasConnection = false;
	
	private static Logger logger = Logger.getLogger(AedServiceTest.class); 
	
	private static final String TMP_FILE_CONTENT = "Contenido del fichero temporal";
	
	private static final String URI_NOT_IN_DB = "http://uri/notindb";
	
	@BeforeClass
	public static void configure() throws Exception {
		//Comprueba si tiene conección con el AED para poder realizar las pruebas
		try {
			hasConnection = aedService.getVersion() != null;
		}catch(Exception e){
			logger.warn("No hay conexión a "+ aedService.getEndPoint() + " , saltando los tests de AedServiceTest");
		}
	}
	
	@Before
	public void before(){
		Fixtures.delete(Documento.class);
	}
	
	@Test
	public void initOk(){
		assertNotNull(aedService);
		assertNotNull(aedService.getEndPoint());
		assertNotNull(aedService.getPort());
	}
	
	@Test
	public void getVersion() throws Exception {
		assumeTrue(hasConnection);
		assertNotNull(aedService.getVersion());
	}

	/**
	 * saveDocumentoTemporal
	 */
	
	@Test
	public void saveDocumentoTemporal() throws Exception{
		assumeTrue(hasConnection);
		
		Documento documento = new Documento();
		documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.base");
		documento.descripcion = "prueba";
		
		File tmp = createTmpFile();
		String uri = aedService.saveDocumentoTemporal(documento, tmp);
		assertNotNull(uri);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void saveDocumentoTemporalDebeFallarSiDocumentoNoTieneTipo() throws Exception {
		assumeTrue(hasConnection);
		
		Documento documento = new Documento();
		documento.descripcion = "prueba";

		File tmp = createTmpFile();
		aedService.saveDocumentoTemporal(documento, tmp);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void saveDocumentoTemporalDebeFallarSiDocumentoTipoOtrosNoTieneDescripcion() throws Exception {
		assumeTrue(hasConnection);
		
		Documento documento = new Documento();
		documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.otros");

		File tmp = createTmpFile();
		aedService.saveDocumentoTemporal(documento, tmp);
	}

	@Test
	public void isClasificado(){
		assumeTrue(hasConnection);
		
		String uri = "http://uri/prueba"; 
		Documento d = new Documento();
		d.uri = uri;
		d.save();
		
		//Por defecto los documentos son no clasificado
		assertFalse(aedService.isClasificado(uri));
		
		d.clasificado = true;
		assertTrue(aedService.isClasificado(uri));
		
		assertNull(aedService.isClasificado(URI_NOT_IN_DB));
	}
	
	@Test
	public void obtenerDocBytes() throws Exception {
		assumeTrue(hasConnection);
		
		Documento d = uploadTestDocumento();
		assertNotNull(d.uri);
		byte[] aedBytes = aedService.obtenerDocBytes(d.uri);
		assertEquals(TMP_FILE_CONTENT, new String(aedBytes));
		
		assertNull(aedService.obtenerDocBytes(null));
		assertNull(aedService.obtenerDocBytes(URI_NOT_IN_DB));
	}
	
	
	@Test
	public void obtenerPropiedades() throws Exception {
		assumeTrue(hasConnection);
		
		Documento d = uploadTestDocumento();
		PropiedadesDocumento propiedades = aedService.obtenerPropiedades(d.uri);
		assertNotNull(propiedades);
		assertNull(aedService.obtenerPropiedades(URI_NOT_IN_DB));
	}
	
	
	private Documento uploadTestDocumento() throws Exception {
		Documento d = new Documento();
		d.tipo = FapProperties.get("fap.aed.tiposdocumentos.base");
		d.descripcion = "prueba";
		
		File tmp = createTmpFile();
		aedService.saveDocumentoTemporal(d, tmp);
		return d;
	}
	
	private File createTmpFile() throws Exception {
		File tmp = File.createTempFile("tmp", ".txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
		out.write(TMP_FILE_CONTENT);
		out.close();
		return tmp;
	}
	
	
}
