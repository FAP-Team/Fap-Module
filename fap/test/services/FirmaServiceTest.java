package services;

import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import models.Firmante;
import models.Persona;
import models.PersonaFisica;
import models.PersonaJuridica;
import models.RepresentantePersonaJuridica;
import models.Solicitante;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import platino.InfoCert;
import play.modules.guice.InjectSupport;
import play.test.UnitTest;

public abstract class FirmaServiceTest extends UnitTest {

    private static final String TEST_FIRMA_INVALIDA = "esta no es una firma valida";
    protected static final String TEST_TEXT = "test text";
    protected static FirmaService firmaService;
    protected static boolean hasConnection = false;

    @Before
    public void before() {
       // assumeTrue(hasConnection);
    }

    @Test
    public void firmarTexto() throws Exception {
        assertCorrectFirmarValidarTexto(TEST_TEXT);
    }

    @Test
    public void firmarTextoConTildes() throws Exception {
        assertCorrectFirmarValidarTexto("Texto con tildes áéíóúÁÉÍÓÚ");
    }

    protected void assertCorrectFirmarValidarTexto(String texto) throws Exception {
        String firma = firmaService.firmarTexto(texto.getBytes());
        assertNotNull(firma);
        boolean firmaValida = firmaService.validarFirmaTexto(texto.getBytes(), firma);
        assertTrue(firmaValida);
    }

    @Test
    public void invalidFirma() throws Exception {
        boolean firmaValida = firmaService.validarFirmaTexto(TEST_TEXT.getBytes(), TEST_FIRMA_INVALIDA);
        assertFalse(firmaValida);
    }
    
    @Test(expected = NullPointerException.class)
    public void firmarTextoNull() throws Exception {
        firmaService.firmarTexto(null);
    }

    @Test(expected = NullPointerException.class)
    public void validarFirmaTextoTextoNull() throws Exception {
        firmaService.validarFirmaTexto(null, "firma");
    }

    @Test(expected = NullPointerException.class)
    public void validarFirmaTextoFirmaNull() throws Exception {
        firmaService.validarFirmaTexto("".getBytes(), null);
    }

    @Test
    public void extraerInformacion() throws Exception {
        //assumeTrue(hasConnection);
        String texto = "Texto de prueba para firma";
        String firma = firmaService.firmarTexto(texto.getBytes());
        Assert.assertNotNull(firma);
        InfoCert certificado = firmaService.extraerCertificado(firma);
        Assert.assertNotNull(certificado);
        asssertValidCertificado(certificado);
    }

    public abstract void asssertValidCertificado(InfoCert certificado);

}
