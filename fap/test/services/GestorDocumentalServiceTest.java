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
import models.Tramite;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import play.Play;
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

public abstract class GestorDocumentalServiceTest extends UnitTest {
    
    protected static GestorDocumentalService gestorDocumentalService;
    protected static boolean isConfigured = false;
    
	private static final String TEST_FILENAME = "testfile.txt";
	private static final String TEST_FILE_CONTENT = "Contenido del fichero temporal";
	private static final String URI_NOT_IN_DB = "http://uri/notindb";

    protected abstract String getTipoDocumentoValido(); 
	
	@Before
	public void before() {
		if(!isConfigured){
		    play.Logger.error("El servicio de gestor documental no está configurado");
		}
	    assumeTrue(isConfigured);
	}

    @Test(expected=NullPointerException.class)
    public void crearExpedienteFailOnNullSolicitud() throws GestorDocumentalServiceException {
    	SolicitudGenerica sol = null;
        gestorDocumentalService.crearExpediente(sol);
    }
	
    @Test
    public void crearExpedientePersonaFisica() throws Exception {
        String idExpediente = "TEST" + Codec.UUID();
        SolicitudGenerica solicitud = stubSolicitud(idExpediente);
        mockPersonaFisica(solicitud.solicitante);
        String idExpedienteCreado = gestorDocumentalService.crearExpediente(solicitud);
        assertNotNull(idExpedienteCreado);
        play.Logger.info("Expediente persona fisica creado");
    }
    
    @Test(expected=NullPointerException.class)
    public void crearExpedienteFailsOnNullSolicitante() throws Exception {
        SolicitudGenerica solicitud = stubSolicitud("TEST" + Codec.UUID());
        solicitud.solicitante = null;
        gestorDocumentalService.crearExpediente(solicitud);
    }
    
    @Test(expected=NullPointerException.class)
    public void crearExpedienteFailsOnNullRepresentante() throws Exception {
        SolicitudGenerica solicitud = stubSolicitud("TEST" + Codec.UUID());
        mockPersonaFisica(solicitud.solicitante);
        solicitud.solicitante.representado = true;
        solicitud.solicitante.representante = null;
        gestorDocumentalService.crearExpediente(solicitud);        
    }
    
    @Test
    public void crearExpedientePersonaJuridica() throws Exception {
        SolicitudGenerica solicitud = stubSolicitud("TEST" + Codec.UUID());
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
        SolicitudGenerica solicitud = stubSolicitud("TEST" + Codec.UUID());
        mockPersonaJuridica(solicitud.solicitante);
        solicitud.solicitante.representantes = null;
        gestorDocumentalService.crearExpediente(solicitud);
    }
    
    protected SolicitudGenerica stubSolicitud(String idExpediente){
        SolicitudGenerica solicitud = new SolicitudGenerica();
        ExpedienteAed expediente = mock(ExpedienteAed.class);
        solicitud.expedienteAed = expediente;
        when(expediente.asignarIdAed()).thenReturn(idExpediente);
        solicitud.expedienteAed.idAed = idExpediente;
        return solicitud;
    }
    
    protected ExpedienteAed stubExpediente(String idExpediente){
        ExpedienteAed expedienteAed = new ExpedienteAed();
        expedienteAed.idAed = idExpediente;
        return expedienteAed;
    }
    
    protected void mockPersonaFisica(Persona personaFisica){
        personaFisica.tipo = "fisica";
        personaFisica.fisica.nombre = "Luke";
        personaFisica.fisica.primerApellido = "Sky";
        personaFisica.fisica.segundoApellido = "Walker";
        personaFisica.fisica.nip.valor = "123456789X";
    }

    private void mockPersonaJuridica(Persona personaJuridica){
        personaJuridica.tipo = "juridica";
        personaJuridica.juridica.entidad = "Imperio";
        personaJuridica.juridica.cif = "X123456789";
    }
    
    @Test
    public void saveDocumentoTemporal() throws Exception {
        InputStream is = new ByteArrayInputStream(TEST_FILE_CONTENT.getBytes()); 
        
        Documento documento = stubDocumento();

        String uri = gestorDocumentalService.saveDocumentoTemporal(documento, is, TEST_FILENAME);
        assertSubidaDocumentoCorrecta(documento, uri);
    }
    
    @Test
    public void saveDocumentoTemporalByFile() throws Exception {
        Documento documento = stubDocumento();
        File f = Play.getVirtualFile("/test/services/aedTest.txt").getRealFile();
        String uri = gestorDocumentalService.saveDocumentoTemporal(documento, f);
        assertSubidaDocumentoCorrecta(documento, uri);
    }
    
    @Test
    public void saveDocumentoTemporalFechaSubidaNow() throws Exception {
        DateTime t1 = new DateTime(); 
        Thread.sleep(1000);
        Documento documento = saveTmpDocumento(TEST_FILE_CONTENT, TEST_FILENAME);
        Thread.sleep(1000);
        DateTime t2 = new DateTime();
        assertTrue(documento.fechaSubida.isAfter(t1));
        assertTrue(documento.fechaSubida.isBefore(t2));
    }
    
    private Documento stubDocumento(){
        Documento documento = new Documento();
        documento.tipo = getTipoDocumentoValido(); 
        documento.descripcion = "descripcion";
        return documento;
    }
    
    private void assertSubidaDocumentoCorrecta(Documento documento, String uri){
        assertNotNull(uri);
        assertNotNull(documento.uri);
        assertEquals(uri, documento.uri);
        assertFalse(documento.clasificado);  
        assertNotNull(documento.hash);
        assertNotNull(documento.fechaSubida);
    }
    
    @Test(expected=GestorDocumentalServiceException.class)
    public void saveDocumentoTemporalFailsIfUri() throws Exception {
        Documento documento = stubDocumento();
        documento.uri = "uri ya seteada";
        InputStream is = new ByteArrayInputStream("contenido".getBytes());
        gestorDocumentalService.saveDocumentoTemporal(documento, is , TEST_FILENAME);
    }
    
    @Test(expected=NullPointerException.class)
    public void saveDocumentoTemporalFailsIfNotTipo() throws Exception {
        Documento documento = stubDocumento();
        documento.tipo = null;
        InputStream is = new ByteArrayInputStream("".getBytes());
        gestorDocumentalService.saveDocumentoTemporal(documento, is , TEST_FILENAME);
    }
    
    
    
    @Test(expected=NullPointerException.class)
    public void saveDocumentoTemporalFailsIfOtrosAndNullDescription() throws Exception {
        Documento documento = new Documento();
        documento.tipo = getTipoDocumentoValido();
        documento.descripcion = null;
        InputStream is = new ByteArrayInputStream("".getBytes());
        gestorDocumentalService.saveDocumentoTemporal(documento, is , TEST_FILENAME);
    }

    @Test(expected=IllegalArgumentException.class)
    public void saveDocumentoTemporalFailsIEmptyTipo() throws Exception {
        Documento documento = new Documento();
        documento.tipo = "";
        documento.descripcion = "despcripcion";
        InputStream is = new ByteArrayInputStream("".getBytes());
        gestorDocumentalService.saveDocumentoTemporal(documento, is , TEST_FILENAME);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void saveDocumentoTemporalFailsIfOtrosAndEmptyDescripcion() throws Exception {
        Documento documento = new Documento();
        documento.tipo = getTipoDocumentoValido();
        documento.descripcion = "";
        InputStream is = new ByteArrayInputStream("".getBytes());
        gestorDocumentalService.saveDocumentoTemporal(documento, is , TEST_FILENAME);
    }
    
    @Test(expected=GestorDocumentalServiceException.class)
    public void saveDocumentoTemporalFailsIfEmptyStream() throws Exception {
        Documento documento = stubDocumento();
        InputStream is = new ByteArrayInputStream("".getBytes());
        gestorDocumentalService.saveDocumentoTemporal(documento, is , TEST_FILENAME);
    }
    
    @Test(expected=NullPointerException.class)
    public void saveDocumentoTemporalFailsIfNullStream() throws Exception {
        Documento documento = stubDocumento();
        gestorDocumentalService.saveDocumentoTemporal(documento, null , TEST_FILENAME);
    }
    
    @Test
    public void getDocumento() throws Exception {
        Documento d = saveTmpDocumento(TEST_FILE_CONTENT, TEST_FILENAME);
        
        BinaryResponse response = gestorDocumentalService.getDocumento(d);
        assertEquals(TEST_FILENAME, response.nombre);
        
        String responseContent = IO.readContentAsString(response.contenido.getInputStream());
        assertEquals(TEST_FILE_CONTENT, responseContent);
    }

    @Test
    public void getDocumentoClasificado() throws Exception {
        Documento documento = clasificarDocumentoDeTest(TEST_FILE_CONTENT);
        BinaryResponse response = gestorDocumentalService.getDocumento(documento);
        assertEquals(TEST_FILENAME, response.nombre);
        assertEquals(TEST_FILE_CONTENT, IO.readContentAsString(response.contenido.getInputStream()));
    }
    
    private Documento saveTmpDocumento(String fileContent, String filename) throws Exception {
        InputStream is = new ByteArrayInputStream(fileContent.getBytes()); 
        Documento documento = stubDocumento();
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
        
        SolicitudGenerica solicitud = new SolicitudGenerica();
        solicitud.expedienteAed.idAed = "TEST" + Codec.UUID();
        mockPersonaJuridica(solicitud.solicitante);
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
        
    @Test
    public void getTramites() throws Exception {
        List<Tramite> tramites = gestorDocumentalService.getTramites();
        assertNotNull(tramites);
        assertTrue(tramites.size() > 0);
        for(Tramite tramite : tramites){
            assertNotNull(tramite.nombre);
            assertNotNull(tramite.uri);
            assertNotNull(tramite.documentos);
            assertTrue(tramite.documentos.size() > 0);
        }
    }
    
    @Test
    public void modificarExpedientePersonaFisica() throws Exception {
        String idExpediente = "TEST" + Codec.UUID();
        ExpedienteAed exp = stubExpediente(idExpediente);
        String idExpedienteCreado = gestorDocumentalService.crearExpediente(exp);
        assertNotNull(idExpedienteCreado);
        play.Logger.info("Expediente creado");
        
        SolicitudGenerica solicitud = stubSolicitud(idExpediente);
        mockPersonaFisica(solicitud.solicitante);
        String idExpedienteCreado2 = gestorDocumentalService.modificarInteresados(exp, solicitud);
        assertEquals(idExpedienteCreado, idExpedienteCreado2);
    }
    
}
