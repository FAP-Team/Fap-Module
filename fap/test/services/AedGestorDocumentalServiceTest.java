package services;

import static org.junit.Assume.assumeTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import models.Documento;
import models.ExpedienteAed;
import models.Persona;
import models.RepresentantePersonaJuridica;
import models.Solicitante;
import models.SolicitudGenerica;
import models.TableKeyValue;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import play.libs.Codec;
import play.libs.IO;
import play.modules.guice.InjectSupport;
import play.test.Fixtures;
import play.test.UnitTest;
import properties.FapProperties;
import properties.MapPropertyPlaceholder;
import properties.PropertyPlaceholder;
import services.aed.AedGestorDocumentalServiceImpl;
import utils.BinaryResponse;
import utils.StringUtils;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.excepciones.CodigoErrorEnum;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;

public class AedGestorDocumentalServiceTest extends org.junit.Assert {
	private static final String TEST_FILENAME = "testfile.txt";
    private static GestorDocumentalService gestorDocumentalService;
	private static boolean isConfigured = false;
	private static final String TEST_FILE_CONTENT = "Contenido del fichero temporal";
	private static final String URI_NOT_IN_DB = "http://uri/notindb";

	@BeforeClass
	public static void configure() throws Exception {
	    PropertyPlaceholder propertyPlaceholder = getPropertyPlaceholder();
	    gestorDocumentalService = new AedGestorDocumentalServiceImpl(propertyPlaceholder);
		isConfigured = gestorDocumentalService.isConfigured();
	}

	private static PropertyPlaceholder getPropertyPlaceholder(){
	    InputStream is = AedGestorDocumentalServiceTest.class.getClassLoader().getResourceAsStream("services/aed.properties");
	    return MapPropertyPlaceholder.load(is);
	}
	
	@Before
	public void before() {
		assumeTrue(isConfigured);
	}

    @Test(expected=NullPointerException.class)
    public void crearExpedienteFailOnNullSolicitud() throws GestorDocumentalServiceException {
        gestorDocumentalService.crearExpediente(null);
    }
	
    @Test
    public void crearExpedientePersonaFisica() throws Exception {
        String idExpediente = "TEST" + Codec.UUID();
        SolicitudGenerica solicitud = mockSolicitud(idExpediente);
        mockPersonaFisica(solicitud.solicitante);
        String idExpedienteCreado = gestorDocumentalService.crearExpediente(solicitud);
        assertNotNull(idExpedienteCreado);
    }
    
    @Test(expected=NullPointerException.class)
    public void crearExpedienteFailsOnNullSolicitante() throws Exception {
        SolicitudGenerica solicitud = mockSolicitud("TEST" + Codec.UUID());
        gestorDocumentalService.crearExpediente(solicitud);
    }
    
    @Test(expected=NullPointerException.class)
    public void crearExpedienteFailsOnNullRepresentante() throws Exception {
        SolicitudGenerica solicitud = mockSolicitud("TEST" + Codec.UUID());
        mockPersonaFisica(solicitud.solicitante);
        solicitud.solicitante.representado = true;
        gestorDocumentalService.crearExpediente(solicitud);        
    }
    
    @Test
    public void crearExpedientePersonaJuridica() throws Exception {
        SolicitudGenerica solicitud = mockSolicitud("TEST" + Codec.UUID());
        mockPersonaJuridica(solicitud.solicitante);
        
        RepresentantePersonaJuridica representante1 = new RepresentantePersonaJuridica();
        mockPersonaFisica(representante1);
        RepresentantePersonaJuridica representante2 = new RepresentantePersonaJuridica();
        mockPersonaJuridica(representante2);

        solicitud.solicitante.representantes.add(representante1);
        solicitud.solicitante.representantes.add(representante2);
        String idExpedienteCreado = gestorDocumentalService.crearExpediente(solicitud); 
        assertNotNull(idExpedienteCreado);
    }

    @Test(expected=NullPointerException.class)
    public void crearExpedientePersonaJuridicaNullRepresentante() throws Exception {
        SolicitudGenerica solicitud = mockSolicitud("TEST" + Codec.UUID());
        mockPersonaJuridica(solicitud.solicitante);
        RepresentantePersonaJuridica representante1 = new RepresentantePersonaJuridica();
        solicitud.solicitante.representantes.add(representante1); 
        gestorDocumentalService.crearExpediente(solicitud);
    }
    
    private SolicitudGenerica mockSolicitud(String idExpediente){
        SolicitudGenerica solicitud = new SolicitudGenerica();
        ExpedienteAed expediente = mock(ExpedienteAed.class);
        solicitud.expedienteAed = expediente;
        when(expediente.asignarIdAed()).thenReturn(idExpediente);
        solicitud.expedienteAed.idAed = idExpediente;
        return solicitud;
    }
    
    private void mockPersonaFisica(Persona personaFisica){
        personaFisica.tipo = "fisica";
        personaFisica.fisica.nombre = "Luke";
        personaFisica.fisica.primerApellido = "Sky";
        personaFisica.fisica.segundoApellido = "Walker";
        personaFisica.fisica.nip.valor = "123456789X";
    }

    private void mockPersonaJuridica(Persona personaJuridica){
        personaJuridica.tipo = "juridica";
        personaJuridica.juridica.entidad = "Imperio";
        personaJuridica.juridica.cif = "X123456789";;
    }
    
    @Test
    public void saveDocumentoTemporal() throws Exception {
        InputStream is = new ByteArrayInputStream(TEST_FILE_CONTENT.getBytes()); 
        
        Documento documento = mock(Documento.class);
        String uri = gestorDocumentalService.saveDocumentoTemporal(documento, is, TEST_FILENAME);
        assertNotNull(uri);
        assertNotNull(documento.uri);
        assertEquals(uri, documento.uri);
        assertFalse(documento.clasificado);
        verify(documento).save();
    }
    
    @Test(expected=GestorDocumentalServiceException.class)
    public void saveDocumentoTemporalFailsIfUri() throws Exception {
        Documento documento = mock(Documento.class);
        documento.uri = "uri ya seteada";
        InputStream is = new ByteArrayInputStream("".getBytes());
        gestorDocumentalService.saveDocumentoTemporal(documento, is , "");
    }
    
    @Test
    public void getDocumento() throws Exception {
        Documento d = saveTmpDocumento(TEST_FILE_CONTENT, TEST_FILENAME);
        
        BinaryResponse response = gestorDocumentalService.getDocumento(d);
        assertEquals(d.uri, response.nombre);
        
        String responseContent = IO.readContentAsString(response.contenido.getInputStream());
        assertEquals(TEST_FILE_CONTENT, responseContent);
    }

    @Test
    public void getDocumentoClasificado() throws Exception {
        Documento documento = clasificarDocumentoDeTest(TEST_FILE_CONTENT);
        BinaryResponse response = gestorDocumentalService.getDocumento(documento);
        assertEquals(documento.uri, response.nombre);
        assertEquals(TEST_FILE_CONTENT, IO.readContentAsString(response.contenido.getInputStream()));
    }
    
    private Documento saveTmpDocumento(String fileContent, String filename) throws Exception {
        InputStream is = new ByteArrayInputStream(fileContent.getBytes()); 
        Documento documento = mock(Documento.class);
        gestorDocumentalService.saveDocumentoTemporal(documento, is, filename);
        return documento;
    }
    
    @Test
    public void clasificarDocumento() throws Exception {
        Documento documento = clasificarDocumentoDeTest(TEST_FILE_CONTENT);
        assertTrue(documento.clasificado);
    }
    
    private Documento clasificarDocumentoDeTest(String content) throws Exception {
        Documento documento = saveTmpDocumento(content, TEST_FILENAME);

        List<Documento> documentos = new ArrayList<Documento>();
        documentos.add(documento);
        
        SolicitudGenerica solicitud = mockSolicitud(Codec.UUID());
        gestorDocumentalService.crearExpediente(solicitud);
        gestorDocumentalService.clasificarDocumentos(solicitud, documentos);
        return documento;
    }
    
    @Test
    public void deleteDocumentoTemporal() throws Exception {
        Documento documento = saveTmpDocumento(TEST_FILE_CONTENT, TEST_FILENAME);
        gestorDocumentalService.deleteDocumento(documento);
    }
    
    @Test(expected=GestorDocumentalServiceException.class)
    public void deleteDocumentoClasificado() throws Exception {
        Documento documento = clasificarDocumentoDeTest(TEST_FILE_CONTENT);
        gestorDocumentalService.deleteDocumento(documento);
    }
        
    
  
    
	/*
	@Test
	public void saveDocumentoTemporal() throws Exception {
		String uriTipoDocumento = FapProperties
				.get("fap.aed.tiposdocumentos.solicitud");
		String descripcion = "prueba";

		Documento documento = new Documento();
		documento.tipo = uriTipoDocumento;
		documento.descripcion = descripcion;

		File tmp = createTmpFile();
		String uri = gestorDocumentalService.saveDocumentoTemporal(documento, tmp);
		assertNotNull(uri);

		PropiedadesDocumento propiedades = gestorDocumentalService.obtenerPropiedades(uri,
				false);
		assertEquals(uriTipoDocumento, propiedades.getUriTipoDocumento());
		assertEquals(descripcion, propiedades.getDescripcion());
	}
	
	
	@Test
	public void saveDocumentoTemporalDescripcionSegunTipo() throws Exception {
		String uriTipoDocumento = FapProperties
				.get("fap.aed.tiposdocumentos.solicitud");
		String descripcion = "descripcion simulada segun tipo";
		TableKeyValue.setValue("tiposDocumentos", uriTipoDocumento,
				descripcion, true);

		Documento documento = new Documento();
		documento.tipo = uriTipoDocumento;

		File tmp = createTmpFile();
		String uri = gestorDocumentalService.saveDocumentoTemporal(documento, tmp);
		assertNotNull(uri);

		PropiedadesDocumento propiedades = gestorDocumentalService.obtenerPropiedades(uri,
				false);
		assertEquals(uriTipoDocumento, propiedades.getUriTipoDocumento());

		String dsc = TableKeyValue
				.getValue("tiposDocumentos", uriTipoDocumento);
		assertEquals(dsc, propiedades.getDescripcion());
	}

	@Test(expected = NullPointerException.class)
	public void saveDocumentoTemporalDebeFallarSiDocumentoNoTieneTipo()
			throws Exception {
		Documento documento = new Documento();
		documento.descripcion = "prueba";

		File tmp = createTmpFile();
		gestorDocumentalService.saveDocumentoTemporal(documento, tmp);
	}

	@Test(expected = NullPointerException.class)
	public void saveDocumentoTemporalDebeFallarSiDocumentoTipoOtrosNoTieneDescripcion()
			throws Exception {
		Documento documento = new Documento();
		documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.otros");

		File tmp = createTmpFile();
		gestorDocumentalService.saveDocumentoTemporal(documento, tmp);
	}

	@Test(expected = NullPointerException.class)
	public void crearCarpetaTemporalDebeFallarSiCarpetaNull() throws Exception {
		gestorDocumentalService.crearCarpetaTemporal(null);
	}

	@Test
	public void crearCarpetaTemporal() throws Exception {
		String firstPath = "faptest";
		String path = firstPath + "/b/c";

		gestorDocumentalService.borrarCarpetaTemporal(firstPath);

		Assert.assertFalse(gestorDocumentalService.existeCarpetaTemporal(path));
		gestorDocumentalService.crearCarpetaTemporal(path);
		Assert.assertTrue(gestorDocumentalService.existeCarpetaTemporal(path));

		gestorDocumentalService.borrarCarpetaTemporal(firstPath);
	}

	@Test
	public void isClasificado() {
		String uri = "http://uri/prueba";
		Documento d = new Documento();
		d.uri = uri;
		d.save();

		// Por defecto los documentos son no clasificado
		assertFalse(gestorDocumentalService.isClasificado(uri));

		d.clasificado = true;
		assertTrue(gestorDocumentalService.isClasificado(uri));

		assertNull(gestorDocumentalService.isClasificado(URI_NOT_IN_DB));
	}

	@Test
	public void obtenerDocBytes() throws Exception {
		Documento d = uploadTestDocumento();
		assertNotNull(d.uri);
		byte[] aedBytes = gestorDocumentalService.obtenerDocBytes(d.uri);
		assertEquals(TMP_FILE_CONTENT, new String(aedBytes));

		assertNull(gestorDocumentalService.obtenerDocBytes(null));
		assertNull(gestorDocumentalService.obtenerDocBytes(URI_NOT_IN_DB));
	}

	@Test
	public void obtenerPropiedades() throws Exception {
		Documento d = uploadTestDocumento();
		PropiedadesDocumento propiedades = gestorDocumentalService.obtenerPropiedades(d.uri);
		assertNotNull(propiedades);
		assertNull(gestorDocumentalService.obtenerPropiedades(URI_NOT_IN_DB));
	}

	@Test(expected = NullPointerException.class)
	public void borrarDocumentoNullDocumento() throws Exception {
		gestorDocumentalService.borrarDocumento(null);
	}

	@Test(expected = NullPointerException.class)
	public void borrarDocumentoNullUri() throws Exception {
		Documento documento = new Documento();
		documento.uri = null;
		gestorDocumentalService.borrarDocumento(null);
	}

	@Test
	public void borrarDocumento() throws Exception {
		Documento documento = uploadTestDocumento();
		gestorDocumentalService.borrarDocumento(documento);
		try {
			gestorDocumentalService.obtenerDoc(documento.uri);
		} catch (AedExcepcion e) {
			assertEquals(CodigoErrorEnum.DOCUMENTO_NO_EXISTE, e.getFaultInfo()
					.getCodigoError());
		}
	}

	@Test(expected = IllegalStateException.class)
	public void borrarDocumentoClasificado() throws Exception {
		Documento documento = uploadTestDocumento();
		documento.clasificado = true;
		gestorDocumentalService.borrarDocumento(documento);
	}

	private Documento uploadTestDocumento() throws Exception {
		Documento d = new Documento();
		d.tipo = FapProperties.get("fap.aed.tiposdocumentos.base");
		d.descripcion = "prueba";

		File tmp = createTmpFile();
		gestorDocumentalService.saveDocumentoTemporal(d, tmp);
		return d;
	}

	private File createTmpFile() throws Exception {
		File tmp = File.createTempFile("tmp", ".txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
		out.write(TMP_FILE_CONTENT);
		out.close();
		return tmp;
	}
*/
}
