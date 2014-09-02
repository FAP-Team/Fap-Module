package services;

import es.gobcan.platino.servicios.portafirmas.wsdl.solicitudfirma.SolicitudFirmaExcepcion;
import org.junit.Test;
import play.test.UnitTest;
import properties.MapPropertyPlaceholder;
import properties.PropertyPlaceholder;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public abstract class PortafirmaFapServiceTest extends UnitTest {
    protected static final String idValida = "idSolicitudFirmaValida";
    protected static final String comentarioValido = "Comentario valido para la solicitud de firma";
    protected static final String idNoValida = "idNoValido";

    protected static PortafirmaFapService portafirmaService;
    protected static PropertyPlaceholder propertyPlaceholder;

    protected abstract void configuraMockEntregaSolicitudFirmaValida();
    protected abstract void configuraMockEntregaSolicitudFirmaNoValida() throws SolicitudFirmaExcepcion;

    @Test
    public void entregaUnaSolicitudDeFirmaValida() {
        configuraMockEntregaSolicitudFirmaValida();
        boolean resultado = portafirmaService.entregarSolicitudFirma(idValida,comentarioValido);
        assertThat(resultado, is(equalTo(true)));
    }

    @Test
    public void noEntregaSolicitudDeFirmaConIDNoValida() {
        try {
            configuraMockEntregaSolicitudFirmaNoValida();
            boolean resultado = portafirmaService.entregarSolicitudFirma(idNoValida,comentarioValido);
            assertThat(resultado, is(equalTo(false)));
        } catch (SolicitudFirmaExcepcion e) {
            e.printStackTrace();
        }
    }

    protected static PropertyPlaceholder getPropertyPlaceholder(String path) {
        InputStream is = AedGestorDocumentalServiceTest.class
                .getClassLoader().getResourceAsStream(path);
        return MapPropertyPlaceholder.load(is);
    }
}
