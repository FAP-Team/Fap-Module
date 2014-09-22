package services.platino;

import es.gobcan.platino.servicios.portafirmas.wsdl.solicitudfirma.SolicitudFirmaExcepcion;
import es.gobcan.platino.servicios.portafirmas.wsdl.solicitudfirma.SolicitudFirmaInterface;
import org.junit.Before;
import org.junit.BeforeClass;
import services.PortafirmaFapServiceTest;

import javax.xml.ws.BindingProvider;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class PlatinoPortafirmaServiceTest extends PortafirmaFapServiceTest {

    private static SolicitudFirmaInterface mockPortaFirmaPort;

    @BeforeClass
    public static void setUp() {
        mockPortaFirmaPort = mock(SolicitudFirmaInterface.class, withSettings().extraInterfaces(BindingProvider.class));
        propertyPlaceholder = getPropertyPlaceholder("services/platino/portafirma.properties");
        portafirmaService = new PlatinoPortafirmaServiceImpl(propertyPlaceholder, mockPortaFirmaPort);
    }

    protected void configuraMockEntregaSolicitudFirmaValida() {
    }

    protected void configuraMockEntregaSolicitudFirmaNoValida() throws SolicitudFirmaExcepcion {
        String mensajeError = "Testeando solicitud de firma no válida";
        SolicitudFirmaExcepcion solicitudFirmaExcepcion = new SolicitudFirmaExcepcion(mensajeError);
        doThrow(solicitudFirmaExcepcion).when(mockPortaFirmaPort).entregarSolicitudFirma(eq(idNoValida), anyString());
    }

}
