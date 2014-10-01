package services.platino;

import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;
import es.gobcan.platino.servicios.sfst.*;
import messages.Messages;
import models.Documento;
import models.Firma;
import models.Firmante;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.mockito.ArgumentMatcher;
import platino.InfoCert;
import properties.PropertyPlaceholder;
import properties.PropertyPlaceholderImpl;
import services.FirmaServiceTest;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;

import javax.inject.Inject;
import javax.xml.ws.BindingProvider;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class PlatinoMiniAppletFirmaServiceTest extends FirmaServiceTest {

    protected static final String TEST_TEXT_FIRMADO = simularFirmaValida(TEST_TEXT);
    protected static final String CERTIFICADO_VALIDO = "CERTIFICADO_VALIDO";
    protected static final String NIF_VALIDO = "1111111H";
    protected static final String NOMBRE_VALIDO = "Nombre valido";

    @Inject
    static PropertyPlaceholder propertyPlaceholder;
    private static PlatinoSignatureServerBean firmaPort;
    protected GestorDocumentalService mockGestorDocumental;

    @BeforeClass
    public static void beforeClass(){
        firmaPort = mock(PlatinoSignatureServerBean.class, withSettings().extraInterfaces(BindingProvider.class));
        firmaService = spy(new PlatinoFirmaServiceImpl(new PropertyPlaceholderImpl(), firmaPort));
        configurarMockFirmaPort();
        hasConnection = true; //Simulamos el servicio de firma
    }

    private static void configurarMockFirmaPort() {
        try {
            //Tests excepciones
            doThrow(NullPointerException.class).when(firmaPort).signPKCS7((byte[]) isNull(), anyString(), anyString());
            doThrow(NullPointerException.class).when(firmaPort).verifyContentSignature((byte[]) isNull(),any(byte[].class), anyString());


            //Test ExtraerInformacion
            configurarMockFirmaPort("Texto de prueba para firma");
            doReturn(CERTIFICADO_VALIDO).when((PlatinoFirmaServiceImpl)firmaService)
                    .extraerCertificadoDeFirma(simularFirmaValida("Texto de prueba para firma"));
            doReturn(stubInfoCert()).when((PlatinoFirmaServiceImpl)firmaService)
                    .getInformacion(CERTIFICADO_VALIDO);
            doReturn(stubValidateCertResult()).when(firmaPort).validateCert(eq(CERTIFICADO_VALIDO), anyString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static ValidateCertResult stubValidateCertResult() {
        ValidateCertResult validateCertResult = new ValidateCertResult();
        validateCertResult.setCode(6);
        return validateCertResult;
    }

    private static InfoCert stubInfoCert() {
        InfoCert infoCert = new InfoCert();
        infoCert.nif = NIF_VALIDO;
        infoCert.nombre = NOMBRE_VALIDO;
        return infoCert;
    }

    private static void configurarMockFirmaPort(String texto) {
        try {
            String textoFirmado = simularFirmaValida(texto);
            doReturn(simularFirmaValida(texto)).when(firmaPort).signPKCS7(eq(texto.getBytes()), anyString(), anyString());
            doReturn(true).when(firmaPort).verifyContentSignature(eq(texto.getBytes()), eq(textoFirmado.getBytes()), anyString());
        } catch (SignatureServiceException_Exception e) {
            e.printStackTrace();
        }
    }

    private static FirmaInfoResult stubFirmaInfoResult() {
        FirmaInfoResult firmaInfoResult = new FirmaInfoResult();
        firmaInfoResult.setNodosFirma(new NodosFirmaInfo());
        NodoInfoResult nodoInfoResult = new NodoInfoResult();
        nodoInfoResult.getCertificado().add(CERTIFICADO_VALIDO);
        firmaInfoResult.getNodosFirma().getNodoFirma().add(nodoInfoResult);
        return firmaInfoResult;
    }


    @Override
    protected void assertFirmaFirmanteNoEntreFirmantes() {
        assertTrue(Messages.messages(Messages.MessageType.ERROR)
                .contains("El certificado no se corresponde con uno que debe firmar la solicitud."));

    }


    @Override
    protected void setMocksImplFirmanteNoEntreFirmantes(String firma) {
        Firmante firmante = new Firmante();
        firmante.idvalor = "no valido";
        doReturn(firmante).when(firmaService).getFirmante(eq(firma), any(Documento.class));
    }

    @Override
    protected void setMocksImplFirmanteYaHaFirmado(String firma, Firmante firmante) {
        firmante.fechaFirma = DateTime.now();
        doReturn(firmante).when(firmaService).getFirmante(eq(firma), any(Documento.class));
    }

    @Override
    protected void assertFirmanteYaHaFirmado() {
    	boolean contieneElError = false;
        for(String mensaje : Messages.messages(Messages.MessageType.ERROR)) {
            if (mensaje.matches("Este certificado ya ha firmado el documento .*")) {
                contieneElError = true;
                break;
            }
        }
        assertTrue(contieneElError);
    }

    @Override
    protected void assertFirmanteDocumentoNoSolicitado() {
        boolean contieneElError = false;
        for(String mensaje : Messages.messages(Messages.MessageType.ERROR)) {
            if (mensaje.matches("Se esperaba la firma de .*")) {
                contieneElError = true;
                break;
            }
        }
        assertTrue(contieneElError);
    }

    @Override
    protected void setMocksImplFirmanteDocumentoNoSolicitado(String firma, Firmante firmante, String valorDocumento) {
        doReturn(firmante).when(firmaService).getFirmante(eq(firma), any(Documento.class));
    }

    @Override
    protected void assertFirmarFirmaValida(String firma, Firmante firmante, Documento documento) {
        assertThat(firmante.fechaFirma, is(notNullValue()));
        try {
            verify(mockGestorDocumental, times(1)).agregarFirma(eq(documento), argThat(new FirmaCorrecta(firma)));
        } catch (GestorDocumentalServiceException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setMocksImplFirmarFirmaValida(Documento documento, String firma, Firmante firmante, String valorDocumento) {
        doReturn(firmante).when(firmaService).getFirmante(eq(firma), any(Documento.class));
        mockGestorDocumental = mock(GestorDocumentalService.class);
        ((PlatinoFirmaServiceImpl)firmaService).gestorDocumentalService = mockGestorDocumental;
    }

    @Override
    protected void assertCorrectFirmarValidarTexto(String texto) throws Exception {
        configurarMockFirmaPort(texto);
        super.assertCorrectFirmarValidarTexto(texto);
    }

    private static String simularFirmaValida(String testText) {
        return new StringBuilder(testText).append("FIRMADO").reverse().toString();
    }

    @Override
    public void asssertValidCertificado(InfoCert certificado) {
        try {
            verify(firmaPort, times(1)).validateCert(anyString(), anyString());
            assertTrue(NIF_VALIDO.equals(certificado.nif));
            assertTrue(NOMBRE_VALIDO.equals(certificado.nombre));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class FirmaCorrecta extends ArgumentMatcher<Firma> {
        private final String firma;

        FirmaCorrecta(String firma) {
            this.firma = firma;
        }

        @Override
        public boolean matches(Object o) {
            if (o != null) {
                String firma = ((Firma)o).getContenido();
                if (firma.equals(this.firma)) {
                    return true;
                }
            }
            return false;
        }
    }
}
