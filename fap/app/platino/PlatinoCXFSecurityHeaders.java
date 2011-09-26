package platino;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingOutInterceptor;

import es.gobcan.platino.wss.headers.UsernameCallbackHandler;
import es.gobcan.platino.wss.interceptor.WSSPlatinoHeaderOutInterceptor;
import es.gobcan.platino.wss.interceptor.WSSPlatinoOutInterceptor;

public class PlatinoCXFSecurityHeaders {
	public static final String SOAP_11 = "SOAP11";
	public static final String SOAP_12 = "SOAP12";
	private static Log log = LogFactory.getLog(PlatinoCXFSecurityHeaders.class);

	public static void addSoapWSSHeader(Object service, String soapVersion,
			String username, String alias, String keystoreCallbackHandler,
			Map<String, String> headers) {
		
		Client client = ClientProxy.getClient(service);
		Endpoint cxfEndpoint = client.getEndpoint();

		if ((!soapVersion.equals("SOAP11")) && (!soapVersion.equals("SOAP12"))) {
			log.debug("No se ha asignado un valor correcto al parámetro soapVersion. Se usará el valor por defecto SOAP 1.1");
			soapVersion = "SOAP11";
		}

		String soap = soapVersion == "SOAP11" ? "http://schemas.xmlsoap.org/soap/envelope/"
				: "http://www.w3.org/2003/05/soap-envelope";

		Map outUsernameProps = new HashMap();
		outUsernameProps.put("action", "UsernameToken");
		outUsernameProps.put("passwordType", "PasswordText");
		outUsernameProps.put("user", username);
		outUsernameProps.put("passwordCallbackClass", UsernameCallbackHandler.class.getName());
		outUsernameProps.put("addUTElements", "Nonce");
		outUsernameProps.put("actor", "http://www.gobiernodecanarias.org/Platino/Authentication/1.0");

		WSSPlatinoOutInterceptor wssUsernameInterceptor = new WSSPlatinoOutInterceptor(outUsernameProps);
		wssUsernameInterceptor.setId("es.gobcan.platino.wss.UsernameToken");

		Map outSignatureProps = new HashMap();
		outSignatureProps.put("action", "Signature");
		outSignatureProps.put("user", alias);
		outSignatureProps.put("passwordType", "PasswordText");
		outSignatureProps.put("passwordCallbackClass", keystoreCallbackHandler);
		outSignatureProps.put("signaturePropFile", "platino/client_sign.properties");
		outSignatureProps.put("signatureKeyIdentifier", "DirectReference");
		outSignatureProps.put("actor", "http://www.gobiernodecanarias.org/Platino/Authentication/1.0");
		outSignatureProps.put("signatureParts",
						"{Element}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd}UsernameToken;{Element}{"
								+ soap
								+ "}Body;{Element}{http://platino.gobcan.es}platinoHeaders");

		WSSPlatinoOutInterceptor wssSignatureInterceptor = new WSSPlatinoOutInterceptor(outSignatureProps);
		wssSignatureInterceptor.setId("es.gobcan.platino.wss.Signature");

		List outInterceptors = cxfEndpoint.getOutInterceptors();

		log.debug("Interceptor SAAJ añadido");
		outInterceptors.add(new SAAJOutInterceptor());

		WSSPlatinoHeaderOutInterceptor headerInterceptor = new WSSPlatinoHeaderOutInterceptor(
				headers);
		log.debug("Interceptor Cabeceras añadido.");
		outInterceptors.add(headerInterceptor);

		outInterceptors.add(wssUsernameInterceptor);
		log.debug("Interceptor UsernameToken añadido.");
		outInterceptors.add(wssSignatureInterceptor);
		log.debug("Interceptor Signature añadido.");
		if (log.isDebugEnabled()) {
			cxfEndpoint.getOutInterceptors().add(new LoggingOutInterceptor());
			log.debug("Interceptor Logging añadido");
		}
	}
}