package services.portafirma;

import java.net.URL;

import javax.xml.ws.BindingProvider;

import es.gobcan.aciisi.portafirma.ws.PortafirmaService;
import es.gobcan.aciisi.portafirma.ws.PortafirmaSoapService;

import platino.PlatinoProxy;
import properties.FapProperties;
import services.PortafirmaFapService;

public class PortafirmaImpl implements PortafirmaFapService {
	
	private static PortafirmaService portafirmaService;
	
	static {
		URL wsdlURL =PortafirmaFapService.class.getClassLoader()
				.getResource("wsdl/PortafirmaServiceImpl.wsdl");

		portafirmaService= new PortafirmaSoapService(wsdlURL).getPortafirmaSoapService();
		BindingProvider bp = (BindingProvider) portafirmaService;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				FapProperties.get("portafirma.webservice.wsdlURL"));
		PlatinoProxy.setProxy(portafirmaService);
	}

	@Override
	public void crearSolicitudFirma() {
		// TODO Auto-generated method stub

	}

	@Override
	public void obtenerEstadoFirma() {
		// TODO Auto-generated method stub

	}

	@Override
	public void eliminarSolicitudFirma() {
		// TODO Auto-generated method stub

	}

}
