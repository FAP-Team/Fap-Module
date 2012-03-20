package services;

import org.junit.BeforeClass;

import platino.InfoCert;

import services.filesystem.FileSystemFirma;
import services.filesystem.FileSystemFirmaServiceImpl;

public class FileSystemFirmaServiceTest extends FirmaServiceTest {

    @BeforeClass
    public static void beforeClass(){
        firmaService = new FileSystemFirmaServiceImpl();
        hasConnection = firmaService.isConfigured();
    }

    @Override
    public void asssertValidCertificado(InfoCert certificado) {
        assertEquals("APP", certificado.getNombreCompleto());
        assertEquals("APPNIF", certificado.getId());
    }
    
}
