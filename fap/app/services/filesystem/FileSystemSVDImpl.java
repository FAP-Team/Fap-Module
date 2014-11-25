package services.filesystem;

import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.RespuestaPdf;
import services.SVDService;
import services.VerificarDatosServiceException;

public class FileSystemSVDImpl implements SVDService {

	@Override
	public boolean isConfigured() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void mostrarInfoInyeccion() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void crearPeticionSincrona() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void crearPeticionAsincrona() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Respuesta enviarPeticion() throws VerificarDatosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Respuesta solicitarRespuestaAsincrona(String idRespuesta)
			throws VerificarDatosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RespuestaPdf generarPDFRespuesta()
			throws VerificarDatosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Respuesta peticionRecover() throws VerificarDatosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

}
