
package es.fap.simpleled;

import com.google.inject.Injector;

import es.fap.simpleled.led.impl.LedPackageImpl;


/**
 * Initialization support for running Xtext languages 
 * without equinox extension registry
 */
public class LedStandaloneSetup extends LedStandaloneSetupGenerated{

	public static void doSetup() {
		new LedStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
	
	@Override
	public Injector createInjectorAndDoEMFRegistration() {
		//LedPackageImpl.init();
		return super.createInjectorAndDoEMFRegistration();
	}
}

