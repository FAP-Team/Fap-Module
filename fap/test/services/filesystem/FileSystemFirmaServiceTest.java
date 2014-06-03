package services.filesystem;

import models.Documento;
import models.Firmante;
import org.junit.Before;
import org.junit.BeforeClass;

import platino.InfoCert;

import services.FirmaServiceTest;
import services.GestorDocumentalService;
import services.filesystem.FileSystemFirma;
import services.filesystem.FileSystemFirmaServiceImpl;
import services.platino.PlatinoFirmaServiceImpl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class FileSystemFirmaServiceTest extends FirmaServiceTest {

    protected static GestorDocumentalService mockGestorDocumental;


    @BeforeClass
    public static void beforeClass(){
        firmaService = spy(new FileSystemFirmaServiceImpl());
        hasConnection = firmaService.isConfigured();
        mockGestorDocumental = mock(GestorDocumentalService.class);
        FileSystemFirmaServiceImpl.gestorDocumentalService = mockGestorDocumental;
        setMockFileSystem();
    }

    private static void setMockFileSystem() {
        Firmante firmante = new Firmante();
        firmante.idvalor = "111a";
        doReturn(firmante).when(firmaService).getFirmante(anyString(), any(Documento.class));
    }

    @Override
    protected void assertFirmaFirmanteNoEntreFirmantes() {

    }

    @Override
    protected void setMocksImplFirmanteNoEntreFirmantes(String firma) {

    }

    @Override
    protected void setMocksImplFirmanteYaHaFirmado(String firma, Firmante firmante) {

    }

    @Override
    protected void assertFirmanteYaHaFirmado() {

    }

    @Override
    protected void assertFirmanteDocumentoNoSolicitado() {

    }

    @Override
    protected void setMocksImplFirmanteDocumentoNoSolicitado(String firma, Firmante firmante, String valorDocumento) {

    }

    @Override
    protected void assertFirmarFirmaValida(String firma, Firmante firmante, Documento documento) {

    }

    @Override
    protected void setMocksImplFirmarFirmaValida(Documento documento, String firma, Firmante firmante, String valorDocumento) {

    }



    @Override
    public void asssertValidCertificado(InfoCert certificado) {
        assertEquals("APP", certificado.getNombreCompleto());
        assertEquals("APPNIF", certificado.getId());
    }
    
}
