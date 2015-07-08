package services.verificacionDatos;

import java.util.List;

import models.PeticionSVDFAP;
import models.SolicitudTransmisionSVDFAP;
import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.RespuestaPdf;

public interface SVDService {

	public boolean isConfigured();

	public void mostrarInfoInyeccion();

	public void peticionSincrona(PeticionSVDFAP peticion) throws SVDServiceException;

	public void peticionAsincrona(PeticionSVDFAP peticion) throws SVDServiceException;

	public void solicitudRespuesta(PeticionSVDFAP peticion) throws SVDServiceException;

	public RespuestaPdf peticionPDF(String uidUsuario, String idPeticion, String idTransmision) throws SVDServiceException;

	public Respuesta peticionRecover(PeticionSVDFAP peticion) throws SVDServiceException;
}
