
package config;

import security.*;

import com.google.inject.AbstractModule;

/**
 * Configuración de Guice generada.
 *
 * Clase automática, cada vez que se genere la aplicación
 * se sobreescribirá esta clase. Para personalizar
 * la configuración consula la clase config.AppModule. 
 */
public class AppModuleGen extends AbstractModule {
	
	@Override
	protected void configure() {
		secure();
		custom();
	}
	
	protected void secure(){
		bind(Secure.class).toInstance(new SecureApp(new SecureAppGen(new SecureFap(new SecureFapGen(null)))));
	}

	protected void custom(){
	}
	
}
