package services.verificacionDatos;

import java.util.List;
import models.PeticionSVDFAP;
import models.SolicitudTransmisionSVDFAP;
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
	public void peticionSincrona(PeticionSVDFAP peticion) throws SVDServiceException {
		// TODO Auto-generated method stub
	}

	@Override
	public void peticionAsincrona(PeticionSVDFAP peticion) throws SVDServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void solicitudRespuesta(PeticionSVDFAP peticion) throws SVDServiceException {
		// TODO Auto-generated method stub
	}

	@Override
	public RespuestaPdf peticionPDF(String uidUsuario, String idPeticion, String idTransmision) throws SVDServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Respuesta peticionRecover(PeticionSVDFAP peticion) throws SVDServiceException {
		// TODO Auto-generated method stub
		return null;
	}


}
