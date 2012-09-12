package config;

import security.*;

/**
 * Configuración de Guice.
 * 
 * En esta clase puedes personalizar la configuración de Guice.
 * 
 * La configuración por defecto personaliza el método secure para
 * configurar correctamente los permisos.
 * 
 * Si quieres añadir nueva configuración de guice puede
 * sobreescribir el metodo <config> (recuerda llamar al super)
 * 
 * Si quieres descartar la configuración del módulo y únicamente
 * utilizar la tuya elimina el "extends FapModule".
 */
public class AppModule extends FapModule {

	@Override
	protected void secure() {
		bind(Secure.class).toInstance(new SecureApp(new SecureAppGen(new SecureFap(new SecureFapGen(null)))));
	}

}
