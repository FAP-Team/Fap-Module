package services.platino;

import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;
import es.gobcan.platino.servicios.organizacion.DBOrganizacionServiceBean;
import es.gobcan.platino.servicios.portafirmas.wsdl.solicitudfirma.SolicitudFirmaExcepcion;
import es.gobcan.platino.servicios.portafirmas.wsdl.solicitudfirma.SolicitudFirmaInterface;

import org.junit.Before;
import org.junit.BeforeClass;

import config.InjectorConfig;
import services.GestorDocumentalService;
import services.PortafirmaFapServiceTest;

import javax.inject.Inject;
import javax.xml.ws.BindingProvider;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class PlatinoPortafirmaServiceTest extends PortafirmaFapServiceTest {

    private static SolicitudFirmaInterface mockPortaFirmaPort;
   	private static PlatinoBDOrganizacionServiceImpl platinoDBOrgImplementation;
   	private static DBOrganizacionServiceBean mockplatinoDBOrgPort;

    @BeforeClass
    public static void setUp() {
        mockPortaFirmaPort = mock(SolicitudFirmaInterface.class, withSettings().extraInterfaces(BindingProvider.class));
        mockplatinoDBOrgPort = mock(DBOrganizacionServiceBean.class,withSettings().extraInterfaces(BindingProvider.class));
        propertyPlaceholder = getPropertyPlaceholder("services/platino/portafirma.properties");
        platinoDBOrgImplementation = new PlatinoBDOrganizacionServiceImpl(propertyPlaceholder, mockplatinoDBOrgPort);
        portafirmaService = new PlatinoPortafirmaServiceImpl(propertyPlaceholder, mockPortaFirmaPort, platinoDBOrgImplementation);
    }

    protected void configuraMockEntregaSolicitudFirmaValida() {
    }

    protected void configuraMockEntregaSolicitudFirmaNoValida() throws SolicitudFirmaExcepcion, DBOrganizacionException_Exception {
        String mensajeError = "Testeando solicitud de firma no válida";
        SolicitudFirmaExcepcion solicitudFirmaExcepcion = new SolicitudFirmaExcepcion(mensajeError);
        doThrow(solicitudFirmaExcepcion).when(mockPortaFirmaPort).entregarSolicitudFirma(eq(idNoValida), anyString());
    }

}
