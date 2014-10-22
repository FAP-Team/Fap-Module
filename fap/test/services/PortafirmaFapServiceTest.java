package services;

import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;
import es.gobcan.platino.servicios.portafirmas.wsdl.solicitudfirma.SolicitudFirmaExcepcion;

import org.junit.Test;

import play.test.UnitTest;
import properties.MapPropertyPlaceholder;
import properties.PropertyPlaceholder;

import java.io.InputStream;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public abstract class PortafirmaFapServiceTest extends UnitTest {
    protected static final String idValida = "idSolicitudFirmaValida";
    protected static final String comentarioValido = "Comentario valido para la solicitud de firma";
    protected static final String idNoValida = "idNoValido";
    protected static final String idSolicitante = "dgonmor";

    protected static PortafirmaFapService portafirmaService;
    protected static PropertyPlaceholder propertyPlaceholder;

    protected abstract void configuraMockEntregaSolicitudFirmaValida();
    protected abstract void configuraMockEntregaSolicitudFirmaNoValida() throws SolicitudFirmaExcepcion, DBOrganizacionException_Exception;

    @Test
    public void entregaUnaSolicitudDeFirmaValida() {
        configuraMockEntregaSolicitudFirmaValida();
        boolean resultado = portafirmaService.entregarSolicitudFirma(idSolicitante, idValida,comentarioValido);
        assertThat(resultado, is(equalTo(true)));
    }

    @Test
    public void noEntregaSolicitudDeFirmaConIDNoValida() {
        try {
            configuraMockEntregaSolicitudFirmaNoValida();
            boolean resultado = portafirmaService.entregarSolicitudFirma(idSolicitante, idNoValida,comentarioValido);
            assertThat(resultado, is(equalTo(false)));
        } catch (SolicitudFirmaExcepcion e) {
            e.printStackTrace();
        } catch (DBOrganizacionException_Exception e) {
			e.printStackTrace();
		}
    }

    protected static PropertyPlaceholder getPropertyPlaceholder(String path) {
        InputStream is = AedGestorDocumentalServiceTest.class
                .getClassLoader().getResourceAsStream(path);
        return MapPropertyPlaceholder.load(is);
    }
}
