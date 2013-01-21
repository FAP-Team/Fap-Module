package services.portafirma;

import java.net.URL;

import javax.xml.ws.BindingProvider;

import models.ResolucionFAP;

import es.gobcan.aciisi.portafirma.ws.PortafirmaException;
import es.gobcan.aciisi.portafirma.ws.PortafirmaService;
import es.gobcan.aciisi.portafirma.ws.PortafirmaSoapService;
import es.gobcan.aciisi.portafirma.ws.dominio.CrearSolicitudResponseType;
import es.gobcan.aciisi.portafirma.ws.dominio.CrearSolicitudType;

import platino.PlatinoProxy;
import properties.FapProperties;
import services.PortafirmaFapService;
import services.PortafirmaFapServiceException;
import services.responses.PortafirmaCrearSolicitudResponse;

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
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException {
		CrearSolicitudType solFirma=new CrearSolicitudType();
		solFirma.setTitulo(FapProperties.get("portafirma.resolucion.titulo"));
		solFirma.setDescripcion(FapProperties.get("portafirma.resolucion.descripcion"));
		// TODO: Rellenar los datos necesarios
		
		CrearSolicitudResponseType wsType = new CrearSolicitudResponseType();
		try {
			wsType = portafirmaService.crearSolicitud(solFirma);
		} catch (PortafirmaException e) {
			// TODO Auto-generated catch block
			throw new PortafirmaFapServiceException(e.getMessage(), e);
		}
		PortafirmaCrearSolicitudResponse response = new PortafirmaCrearSolicitudResponse();
		response.setIdSolicitud(wsType.getIdSolicitud());
		response.setComentarios(wsType.getComentario());
		return response;
	}

	@Override
	public void obtenerEstadoFirma() throws PortafirmaFapServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void eliminarSolicitudFirma() throws PortafirmaFapServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public String obtenerVersion() throws PortafirmaFapServiceException {
		// TODO Auto-generated method stub
		return null;
	}

}
