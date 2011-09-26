package platino;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import play.Logger;
import properties.FapProperties;


public class PlatinoProxy {

	public static void setProxy(Object service){
		if(FapProperties.getBoolean("fap.proxy.enable")){
			Client client = ClientProxy.getClient(service);
			HTTPConduit http = (HTTPConduit) client.getConduit();
			HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
			httpClientPolicy.setAutoRedirect(false);
			httpClientPolicy.setAllowChunking(false);
			httpClientPolicy.setConnectionTimeout(400000);
			httpClientPolicy.setProxyServer(FapProperties.get("fap.proxy.server"));
			httpClientPolicy.setProxyServerPort(FapProperties.getInt("fap.proxy.port"));
			http.setClient(httpClientPolicy);
		}
	}
	
}
