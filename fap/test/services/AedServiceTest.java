package services;

import static org.junit.Assume.assumeTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.inject.Inject;

import models.Documento;
import models.TableKeyValue;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import play.modules.guice.InjectSupport;
import play.test.Fixtures;
import play.test.UnitTest;
import properties.FapProperties;
import utils.BinaryResponse;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.excepciones.CodigoErrorEnum;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;

@InjectSupport
public class AedServiceTest extends UnitTest {
	
	@Inject
	private static AedService aedService;
	
	private static boolean hasConnection = false; 
	
	private static final String TMP_FILE_CONTENT = "Contenido del fichero temporal";
	
	private static final String URI_NOT_IN_DB = "http://uri/notindb";
	
	@BeforeClass
	public static void configure() throws Exception {
		hasConnection = aedService.hasConnection();
	}
	
	@Before
	public void before(){
		assumeTrue(hasConnection);
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
		assertNotNull(aedService.getVersion());
	}

	/**
	 * saveDocumentoTemporal
	 */
	
	@Test
	public void saveDocumentoTemporal() throws Exception{		
		String uriTipoDocumento = FapProperties.get("fap.aed.tiposdocumentos.solicitud"); 
		String descripcion = "prueba";
		
		Documento documento = new Documento();
		documento.tipo = uriTipoDocumento;
		documento.descripcion = descripcion;
		
		File tmp = createTmpFile();
		String uri = aedService.saveDocumentoTemporal(documento, tmp);
		assertNotNull(uri);
		
		PropiedadesDocumento propiedades = aedService.obtenerPropiedades(uri, false);
		assertEquals(uriTipoDocumento, propiedades.getUriTipoDocumento());
		assertEquals(descripcion, propiedades.getDescripcion());
	}
	
	@Test
	public void saveDocumentoTemporalDescripcionSegunTipo() throws Exception {
		String uriTipoDocumento = FapProperties.get("fap.aed.tiposdocumentos.solicitud"); 
		String descripcion = "descripcion simulada segun tipo";
		TableKeyValue.setValue("tiposDocumentos", uriTipoDocumento, descripcion, true);

		Documento documento = new Documento();
		documento.tipo = uriTipoDocumento;
		
		File tmp = createTmpFile();
		String uri = aedService.saveDocumentoTemporal(documento, tmp);
		assertNotNull(uri);
		
		PropiedadesDocumento propiedades = aedService.obtenerPropiedades(uri, false);
		assertEquals(uriTipoDocumento, propiedades.getUriTipoDocumento());
		
		String dsc= TableKeyValue.getValue("tiposDocumentos", uriTipoDocumento);
		assertEquals(dsc, propiedades.getDescripcion());		
	}
	
	@Test(expected=NullPointerException.class)
	public void saveDocumentoTemporalDebeFallarSiDocumentoNoTieneTipo() throws Exception {	
		Documento documento = new Documento();
		documento.descripcion = "prueba";

		File tmp = createTmpFile();
		aedService.saveDocumentoTemporal(documento, tmp);
	}
	
	@Test(expected=NullPointerException.class)
	public void saveDocumentoTemporalDebeFallarSiDocumentoTipoOtrosNoTieneDescripcion() throws Exception {		
		Documento documento = new Documento();
		documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.otros");

		File tmp = createTmpFile();
		aedService.saveDocumentoTemporal(documento, tmp);
	}

	@Test
	public void isClasificado(){
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
		Documento d = uploadTestDocumento();
		assertNotNull(d.uri);
		byte[] aedBytes = aedService.obtenerDocBytes(d.uri);
		assertEquals(TMP_FILE_CONTENT, new String(aedBytes));
		
		assertNull(aedService.obtenerDocBytes(null));
		assertNull(aedService.obtenerDocBytes(URI_NOT_IN_DB));
	}
	
	@Test
	public void obtenerPropiedades() throws Exception {
		Documento d = uploadTestDocumento();
		PropiedadesDocumento propiedades = aedService.obtenerPropiedades(d.uri);
		assertNotNull(propiedades);
		assertNull(aedService.obtenerPropiedades(URI_NOT_IN_DB));
	}
	
	
	@Test(expected=NullPointerException.class)
	public void borrarDocumentoNullDocumento() throws Exception {
		aedService.borrarDocumento(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void borrarDocumentoNullUri() throws Exception {
		Documento documento = new Documento();
		documento.uri = null;
		aedService.borrarDocumento(null);
	}
	
	@Test
	public void borrarDocumento() throws Exception {
		Documento documento = uploadTestDocumento();
		aedService.borrarDocumento(documento);
		try {
			aedService.obtenerDoc(documento.uri);
		}catch(AedExcepcion e){
			assertEquals(CodigoErrorEnum.DOCUMENTO_NO_EXISTE, e.getFaultInfo().getCodigoError());
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void borrarDocumentoClasificado() throws Exception {
		Documento documento = uploadTestDocumento();
		documento.clasificado = true;
		aedService.borrarDocumento(documento);
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
