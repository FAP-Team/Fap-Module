package services.aed;

import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.AedPortType;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesAdministrativas;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesAvanzadas;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;

import models.Documento;
import models.Firma;
import org.junit.BeforeClass;
import org.mockito.ArgumentMatcher;
import properties.MapPropertyPlaceholder;
import properties.PropertyPlaceholder;
import services.AedGestorDocumentalServiceTest;
import services.GestorDocumentalServiceFirmaTest;

import javax.xml.ws.BindingProvider;
import java.io.InputStream;
import java.util.List;

import static org.mockito.Mockito.*;

public class AedGestorDocumentalServiceFirmaTest extends GestorDocumentalServiceFirmaTest {
    public static final String TEXTO_FIRMA_VALIDA = "Texto de firma CADES valida";
    public static final String FIRMA_CADES_VALIDA = "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";
    private static PropertyPlaceholder propertyPlaceholder;
    private static AedPortType aedPortType;

    @BeforeClass
    public static void configure() throws Exception {
        propertyPlaceholder = getPropertyPlaceholder();
        aedPortType = mock(AedPortType.class, withSettings().extraInterfaces(BindingProvider.class));
        gestorDocumentalService = spy(new AedGestorDocumentalServiceImpl(propertyPlaceholder, aedPortType));
        isConfigured = gestorDocumentalService.isConfigured();
    }

    private static PropertyPlaceholder getPropertyPlaceholder() {
        InputStream is = AedGestorDocumentalServiceTest.class.getClassLoader().getResourceAsStream(
                "services/aed.properties");
        return MapPropertyPlaceholder.load(is);
    }

    private PropiedadesDocumento stubPropiedadesAdministrativas(Firma firmaFAP) {
        PropiedadesDocumento propDoc = new PropiedadesDocumento();
        PropiedadesAdministrativas propAdm = new PropiedadesAdministrativas();
        if (firmaFAP != null) {
            es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firma firma =
                    new es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firma();
            firma.setContenido(firmaFAP.getContenido());
            propAdm.setFirma(firma);
        }
        propDoc.setPropiedadesAvanzadas(propAdm);
        return propDoc;
    }

    @Override
    protected String getTipoDocumentoValido() {
        return propertyPlaceholder.get("fap.aed.tiposdocumentos.base");
    }

    @Override
    protected String getFirmaValida() {
        return FIRMA_CADES_VALIDA;
    }

    @Override
    protected void assertFirmaGuardada(Firma firmaRecibida) {
        try {
            verify(aedPortType, times(1))
                    .actualizarDocumentoPropiedadesNoClasificado((PropiedadesDocumento) argThat(
                            new PropiedadesDocumentoContieneFirma(firmaRecibida.getContenido())));
            verify((AedGestorDocumentalServiceImpl)gestorDocumentalService, never())
                    .concatenarFirma((es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firma) anyObject(), (models.Firmante) anyObject(), (String) anyObject());
        } catch (AedExcepcion aedExcepcion) {
            aedExcepcion.printStackTrace();
        }
    }

    @Override
    protected void assertContraFirmaGuardada(Firma firmaRecibida) {
        try {
            verify(aedPortType, times(1)).
                    actualizarDocumentoPropiedades((PropiedadesDocumento) argThat(
                            new PropiedadesDocumentoContieneFirma(firmaRecibida.getContenido())), (List) anyObject());
            verify((AedGestorDocumentalServiceImpl)gestorDocumentalService, times(1))
                    .contieneTodosFirmantes(eq(firmaRecibida.getContenido()), eq(firmaRecibida.getContenido()));
            verify((AedGestorDocumentalServiceImpl)gestorDocumentalService, times(1))
                    .firmaParalela((es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firma)anyObject(), eq(firmaRecibida.getContenido()));
        } catch (AedExcepcion aedExcepcion) {
            aedExcepcion.printStackTrace();
        }
    }

    @Override
    protected Firma contrafirma(Firma firmaRecibida) {
        return stubFirma();
    }

    @Override
    protected void setPropiedadesDocumento(Documento documento, Firma firma) throws AedExcepcion {
        if (documento.clasificado) {
            doReturn(stubPropiedadesAdministrativas(firma)).when(aedPortType).obtenerDocumentoPropiedades((String)anyObject());
        } else {
            doReturn(stubPropiedadesAdministrativas(null)).when(aedPortType).obtenerDocumentoPropiedadesNoClasificado(documento.uri);
        }
    }

    private class PropiedadesDocumentoContieneFirma extends ArgumentMatcher<PropiedadesDocumento> {
        private final String firma;

        PropiedadesDocumentoContieneFirma(String firma) {
            this.firma = firma;
        }

        @Override
        public boolean matches(Object o) {
            if (o != null) {
                PropiedadesAvanzadas avanzadas = ((PropiedadesDocumento)o).getPropiedadesAvanzadas();
                String firma = ((PropiedadesAdministrativas)avanzadas).getFirma().getContenido();
                if (firma.equals(this.firma)) {
                    return true;
                }
            }
            return false;
        }
    }
}
