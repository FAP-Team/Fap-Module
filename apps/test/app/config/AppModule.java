package config;

import security.*;
import services.FirmaService;
import services.GestorDocumentalService;
import services.RegistroService;
import services.aed.AedGestorDocumentalServiceImpl;
import services.filesystem.FileSystemFirmaServiceImpl;
import services.filesystem.FileSystemGestorDocumentalServiceImpl;
import services.filesystem.FileSystemRegistroService;
import services.platino.PlatinoFirmaServiceImpl;

/**
 * Configuración de Guice.
 * 
 * En esta clase puedes personalizar la configuración de Guice.
 * 
 * La configuración por defecto personaliza el método secure para configurar
 * correctamente los permisos.
 * 
 * Si quieres añadir nueva configuración de guice puede sobreescribir el metodo
 * <config> (recuerda llamar al super)
 * 
 * Si quieres descartar la configuración del módulo y únicamente utilizar la
 * tuya elimina el "extends FapModule".
 */
public class AppModule extends FapModule {

    @Override
    protected void secure() {
        bind(Secure.class).toInstance(new SecureApp(new SecureAppGen(new SecureFap(new SecureFapGen(null)))));
    }

    @Override
    protected void gestorDocumental() {
        bindLazySingletonOnDev(GestorDocumentalService.class, FileSystemGestorDocumentalServiceImpl.class);
    }

    @Override
    protected void firma() {
        bindLazySingletonOnDev(FirmaService.class, FileSystemFirmaServiceImpl.class);
    }
    
    @Override
    protected void registro(){
        bindLazySingletonOnDev(RegistroService.class, FileSystemRegistroService.class);
    }

}
