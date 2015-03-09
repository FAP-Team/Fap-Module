package services.filesystem;

import java.util.List;

import models.PeticionSVDFAP;
import models.SolicitudTransmisionSVDFAP;
import services.SVDService;
import services.SVDServiceException;
import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.RespuestaPdf;

public class FileSystemSVDServiceImpl implements SVDService {

	@Override
	public boolean isConfigured() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de SVD ha sido inyectado con FileSystem y está operativo.");
		else
			play.Logger.info("El servicio de SVD ha sido inyectado con FileSystem y NO está operativo.");
	}
	@Override
	public void crearPeticion(PeticionSVDFAP peticion, List<SolicitudTransmisionSVDFAP> solicitudes, String tipoServicio) {
		// TODO Auto-generated method stub

	}

	@Override
	public Respuesta enviarPeticionSincrona(PeticionSVDFAP peticion) throws SVDServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Respuesta enviarPeticionAsincrona(PeticionSVDFAP peticion) throws SVDServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Respuesta solicitarRespuestaAsincrona(String idRespuesta) throws SVDServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RespuestaPdf peticionPDF(String uidUsuario, String idPeticion, String idTransmision) throws SVDServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Respuesta peticionRecover() throws SVDServiceException {
		// TODO Auto-generated method stub
		return null;
	}


}
