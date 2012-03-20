package services;

import javax.inject.Inject;

import org.junit.BeforeClass;

import platino.InfoCert;
import play.modules.guice.InjectSupport;
import properties.PropertyPlaceholder;

import services.filesystem.FileSystemFirmaServiceImpl;
import services.platino.PlatinoFirmaServiceImpl;

@InjectSupport
public class PlatinoFirmaServiceTest extends FirmaServiceTest {

    @Inject
    static PropertyPlaceholder propertyPlaceholder;
    
    @BeforeClass
    public static void beforeClass(){
        //firmaService = new PlatinoFirmaServiceImpl(propertyPlaceholder);
        //hasConnection = firmaService.isConfigured();
    }

    @Override
    public void asssertValidCertificado(InfoCert certificado) {
        // TODO comprobar la información del certificado de la ACIISI
    }
    
}
