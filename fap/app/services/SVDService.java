package services;

import java.util.List;

import models.PeticionSVDFAP;
import models.SolicitudTransmisionSVDFAP;
import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.RespuestaPdf;

public interface SVDService {

	public boolean isConfigured();

	public void mostrarInfoInyeccion();

	public void crearPeticion(PeticionSVDFAP peticion, List<SolicitudTransmisionSVDFAP> solicitudes, String tipoServicio);

	public Respuesta enviarPeticionSincrona(PeticionSVDFAP peticion) throws SVDServiceException;

	public Respuesta enviarPeticionAsincrona(PeticionSVDFAP peticion) throws SVDServiceException;

	public Respuesta solicitarRespuestaAsincrona(String idRespuesta) throws SVDServiceException;

	public RespuestaPdf peticionPDF(String uidUsuario, String idPeticion, String idTransmision) throws SVDServiceException;

	public Respuesta peticionRecover() throws SVDServiceException;
}
