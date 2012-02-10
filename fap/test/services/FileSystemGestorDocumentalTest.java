package services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import models.Documento;
import models.ExpedienteAed;
import models.SolicitudGenerica;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.libs.IO;
import play.test.UnitTest;
import services.filesystem.FileSystemGestorDocumentalServiceImpl;
import utils.BinaryResponse;

public class FileSystemGestorDocumentalTest extends UnitTest {
    static GestorDocumentalService gestorDocumentalService;
    static File base;
    
    @BeforeClass
    public static void setup() throws GestorDocumentalServiceException {        
        base = new File(System.getProperty("java.io.tmpdir") + "/fap/test");
        base.delete();
        gestorDocumentalService = new FileSystemGestorDocumentalServiceImpl(base);
        gestorDocumentalService.configure();
        assumeTrue(gestorDocumentalService.isConfigured());
        assertExistsFile("temporal");
        assertExistsFile("clasificado");
    }

    private static void assertExistsFile(String path){
        File f = new File(base, path);
        assertTrue(f.exists());
    }
    
    private static void assertNotExistsFile(String path){
        File f = new File(base, path);
        assertFalse(f.exists());
    }
    
    @AfterClass
    public static void delete(){
        base.delete();
    }

    @Test(expected=NullPointerException.class)
    public void crearExpedienteFailOnNullSolicitud() throws GestorDocumentalServiceException {
        gestorDocumentalService.crearExpediente(null);
    }
    
    @Test
    public void crearExpediente() throws Exception {
        String idExpediente = "exp42";
        SolicitudGenerica solicitud = mockSolicitud(idExpediente);
        String idExpedienteCreado = gestorDocumentalService.crearExpediente(solicitud);
        assertNotNull(idExpedienteCreado);
        assertExistsFile("clasificado/" + idExpediente);
    }

    private SolicitudGenerica mockSolicitud(String idExpediente){
        SolicitudGenerica solicitud = new SolicitudGenerica();
        ExpedienteAed expediente = mock(ExpedienteAed.class);
        solicitud.expedienteAed = expediente;
        when(expediente.asignarIdAed()).thenReturn(idExpediente);
        solicitud.expedienteAed.idAed = idExpediente;
        return solicitud;
    }
    
    @Test
    public void saveDocumentoTemporal() throws Exception {
        String fileContent = "fileContent";
        String fileName = "testfile.txt";
        InputStream is = new ByteArrayInputStream(fileContent.getBytes()); 
        
        Documento documento = new Documento();
        String uri = gestorDocumentalService.saveDocumentoTemporal(documento, is, fileName);
        assertNotNull(uri);
        assertNotNull(documento.uri);
        assertEquals(uri, documento.uri);
        assertTrue(documento.uri.endsWith(fileName));
        assertFalse(documento.clasificado);
        
        String resultPath = "temporal/" + uri;
        assertExistsFile(resultPath);
    }
    
    @Test(expected=GestorDocumentalServiceException.class)
    public void saveDocumentoTemporalFailsIfUri() throws Exception {
        Documento documento = new Documento();
        documento.uri = "uri ya seteada";
        InputStream is = new ByteArrayInputStream("".getBytes());
        gestorDocumentalService.saveDocumentoTemporal(documento, is , "");
    }
    
    @Test
    public void getDocumento() throws Exception {
        String filename = "a.txt";
        String fileContent = "a";
        Documento d = saveTmpDocumento(fileContent, filename);
        
        BinaryResponse response = gestorDocumentalService.getDocumento(d);
        assertEquals(d.uri, response.nombre);
        
        String responseContent = IO.readContentAsString(response.contenido.getInputStream());
        assertEquals(fileContent, responseContent);
    }

    @Test
    public void getDocumentoClasificado() throws Exception {
        String idExpediente = "exp51";
        String fileContent = "a";
        Documento documento = clasificarDocumentoDeTest(idExpediente, fileContent);
        BinaryResponse response = gestorDocumentalService.getDocumento(documento);
        assertEquals(documento.uri, response.nombre);
        assertEquals(fileContent, IO.readContentAsString(response.contenido.getInputStream()));
    }
    
    private Documento saveTmpDocumento(String fileContent, String filename) throws Exception {
        InputStream is = new ByteArrayInputStream(fileContent.getBytes()); 
        Documento documento = new Documento();
        gestorDocumentalService.saveDocumentoTemporal(documento, is, filename);
        return documento;
    }
    
    @Test
    public void clasificarDocumento() throws Exception {
        String idExpediente = "exp51";
        Documento documento = clasificarDocumentoDeTest(idExpediente, "a");
        assertTrue(documento.clasificado);
        assertExistsFile("clasificado/" + documento.uri);
        assertNotExistsFile("temporal/" + documento.uri);
    }
    
    private Documento clasificarDocumentoDeTest(String expediente, String content) throws Exception {
        Documento documento = saveTmpDocumento(content, "a.txt");
        assertExistsFile("temporal/" + documento.uri);

        List<Documento> documentos = new ArrayList<Documento>();
        documentos.add(documento);
        
        SolicitudGenerica solicitud = mockSolicitud(expediente);
        gestorDocumentalService.crearExpediente(solicitud);
        gestorDocumentalService.clasificarDocumentos(solicitud, documentos);
        return documento;
    }
    
    @Test
    public void deleteDocumentoTemporal() throws Exception {
        Documento documento = saveTmpDocumento("a", "a.txt");
        gestorDocumentalService.deleteDocumento(documento);
    }
    
    @Test(expected=GestorDocumentalServiceException.class)
    public void deleteDocumentoClasificado() throws Exception {
        Documento documento = clasificarDocumentoDeTest("exp46", "b");
        gestorDocumentalService.deleteDocumento(documento);
    }
    
}
