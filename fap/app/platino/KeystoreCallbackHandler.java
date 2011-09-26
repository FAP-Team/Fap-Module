package platino;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;

import properties.FapProperties;




/**
 * PASSWORD HANDLER 
 */
public class KeystoreCallbackHandler implements CallbackHandler{
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
		pc.setPassword(FapProperties.get("org.apache.ws.security.crypto.merlin.keystore.password"));
	}
}