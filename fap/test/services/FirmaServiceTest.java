package services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import messages.Messages;
import models.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import platino.InfoCert;
import play.test.UnitTest;

import static org.junit.Assume.assumeTrue;

public abstract class FirmaServiceTest extends UnitTest {

    private static final String TEST_FIRMA_INVALIDA = "esta no es una firma valida";
    protected static final String TEST_TEXT = "test text";
    protected static FirmaService firmaService;
    protected static boolean hasConnection = false;

    @Before
    public void before() {
        assumeTrue(hasConnection);
        Messages.clear();
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
        assumeTrue(hasConnection);
        String texto = "Texto de prueba para firma";
        String firma = firmaService.firmarTexto(texto.getBytes());
        Assert.assertNotNull(firma);
        InfoCert certificado = firmaService.extraerCertificado(firma);
        Assert.assertNotNull(certificado);
        asssertValidCertificado(certificado);
    }

    public abstract void asssertValidCertificado(InfoCert certificado);



    @Test
    public void firmarFirmanteNoEntreLosFirmantes() {
        Documento documento = new Documento();
        Firmante firmante1 = new Firmante();
        firmante1.idvalor = "111a";
        Firmante firmante2 = new Firmante();
        firmante2.idvalor = "222b";
        List<Firmante> firmantes = Arrays.asList(firmante1, firmante2);
        String firma = "Firmante No Entre Firmantes";
        String valorDocumento = "";
        setMocksImplFirmanteNoEntreFirmantes(firma);
        firmaService.firmar(documento,firmantes, firma, valorDocumento);
        assertFirmaFirmanteNoEntreFirmantes();
    }

    protected abstract void assertFirmaFirmanteNoEntreFirmantes();
    protected abstract void setMocksImplFirmanteNoEntreFirmantes(String firma);


    @Test
    public void firmarFirmanteYaHaFirmado() {
        Documento documento = new Documento();
        Firmante firmante = new Firmante();
        firmante.idvalor = "111a";
        List<Firmante> firmantes = Arrays.asList(firmante);
        String firma = "Firma de firmante que ya ha firmado";
        String valorDocumento = "";
        setMocksImplFirmanteYaHaFirmado(firma, firmante);
        firmaService.firmar(documento, firmantes, firma, valorDocumento);
        assertFirmanteYaHaFirmado();
    }

    protected abstract void setMocksImplFirmanteYaHaFirmado(String firma, Firmante firmante);
    protected abstract void assertFirmanteYaHaFirmado();


    @Test
    public void firmarFirmanteDocumentoNoSolicitado() {
        Documento documento = new Documento();
        Firmante firmante = new Firmante();
        firmante.idvalor = "111a";
        List<Firmante> firmantes = Arrays.asList(firmante);
        String firma = "Firma de documento no solicitado";
        String valorDocumento = "999a";
        setMocksImplFirmanteDocumentoNoSolicitado(firma, firmante, valorDocumento);
        firmaService.firmar(documento,firmantes, firma, valorDocumento);
        assertFirmanteDocumentoNoSolicitado();
    }

    protected abstract void assertFirmanteDocumentoNoSolicitado();
    protected abstract void setMocksImplFirmanteDocumentoNoSolicitado(String firma, Firmante firmante, String valorDocumento);



    @Test
    public void firmarValido() {
        Documento documento = new Documento();
        Firmante firmante = new Firmante();
        firmante.idvalor = "111a";
        List<Firmante> firmantes = Arrays.asList(firmante);
        String firma = "Firma valida";
        String valorDocumento = firmante.idvalor;
        setMocksImplFirmarFirmaValida(documento, firma, firmante, valorDocumento);
        firmaService.firmar(documento, firmantes, firma, valorDocumento);
        assertFirmarFirmaValida(firma, firmante, documento);
    }

    protected abstract void assertFirmarFirmaValida(String firma, Firmante firmante, Documento documento);
    protected abstract void setMocksImplFirmarFirmaValida(Documento documento, String firma, Firmante firmante, String valorDocumento);





}
