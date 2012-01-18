package platino;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import play.Logger;
import properties.FapProperties;
import properties.PropertyPlaceholder;

public class PlatinoProxy {

	private static final String PROP_ENABLE = "fap.proxy.enable";
	private static final String PROP_SERVER = "fap.proxy.server";
	private static final String PROP_PORT = "fap.proxy.port";

	public static void setProxy(Object service) {
		boolean enable = FapProperties.getBoolean(PROP_ENABLE);
		if (enable) {
			String server = FapProperties.get(PROP_SERVER);
			int port = FapProperties.getInt(PROP_PORT);
			enableProxy(service, server, port);
		}
	}

	public static void setProxy(Object service, PropertyPlaceholder propertyPlaceholder) {
		boolean enable = propertyPlaceholder.getBoolean(PROP_ENABLE);
		if (enable) {
			String server = propertyPlaceholder.get(PROP_SERVER);
			int port = propertyPlaceholder.getInt(PROP_PORT);
			enableProxy(service, server, port);
		}
	}

	private static void enableProxy(Object service, String server, int port) {
		Client client = ClientProxy.getClient(service);
		HTTPConduit http = (HTTPConduit) client.getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setAutoRedirect(false);
		httpClientPolicy.setAllowChunking(false);
		httpClientPolicy.setConnectionTimeout(400000);
		httpClientPolicy.setProxyServer(server);
		httpClientPolicy.setProxyServerPort(port);
		http.setClient(httpClientPolicy);
	}
}
