package services;

import java.util.List;

import models.PeticionSVDFAP;
import models.SolicitudTransmisionSVDFAP;
import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.RespuestaPdf;

public interface SVDService {

	public boolean isConfigured();

	public void mostrarInfoInyeccion();

	public void crearPeticion(PeticionSVDFAP peticion, List<SolicitudTransmisionSVDFAP> solicitudes);

	public Respuesta enviarPeticionSincrona(PeticionSVDFAP peticion) throws SVDServiceException;

	public Respuesta enviarPeticionAsincrona(PeticionSVDFAP peticion) throws SVDServiceException;

	public Respuesta solicitarRespuestaAsincrona(String idRespuesta) throws SVDServiceException;

	public RespuestaPdf generarPDFRespuesta() throws SVDServiceException;

	public Respuesta peticionRecover() throws SVDServiceException;
}
