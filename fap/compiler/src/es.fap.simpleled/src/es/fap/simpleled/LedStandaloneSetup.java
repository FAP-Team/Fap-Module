
package es.fap.simpleled;


/**
 * Initialization support for running Xtext languages 
 * without equinox extension registry
 */
public class LedStandaloneSetup extends LedStandaloneSetupGenerated{

	public static void doSetup() {		
		new LedStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}

