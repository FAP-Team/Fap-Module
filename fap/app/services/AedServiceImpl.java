package services;

import java.net.URL;

import javax.inject.Inject;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.MTOMFeature;

import platino.PlatinoProxy;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import es.gobcan.eadmon.aed.ws.Aed;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.AedPortType;

public class AedServiceImpl implements AedService {

	private AedPortType aed;
	
	private PropertyPlaceholder propertyPlaceholder;
	
	@Inject
	public AedServiceImpl(PropertyPlaceholder propertyPlaceholder){
		this.propertyPlaceholder = propertyPlaceholder;
		
		URL wsdlURL = Aed.class.getClassLoader().getResource("aed/aed.wsdl");
		aed = new Aed(wsdlURL).getAed(new MTOMFeature());
		
		BindingProvider bp = (BindingProvider) aed;
		String endPoint = this.propertyPlaceholder.get("fap.aed.url");
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);
		
		PlatinoProxy.setProxy(aed);
	}
	
	public String getVersion() throws AedExcepcion {
		Holder<String> version = new Holder<String>();
		Holder<String> revision = new Holder<String>();
		aed.obtenerVersionServicio(version, revision);
		return version.value;
	}
	
}
