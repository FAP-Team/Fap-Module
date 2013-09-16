package services.filesystem;

import services.PublicarService;
import services.async.publicar.PublicarServiceAsyncImpl;

public class FileSystemPublicarServiceImpl implements PublicarService {

	@Override
	public void getInfo() {
		// TODO Auto-generated method stub
	}
	
    public boolean isConfigured() {
        //No necesita configuración
        return true;
    }

	@Override
	public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Publicar ha sido inyectado con FileSystem y está operativo.");
		else
			play.Logger.info("El servicio de Publicar ha sido inyectado con FileSystem y NO está operativo.");
	}

}
