package services;

import java.util.List;

import models.PeticionSVDFAP;
import models.SolicitudGenerica;
import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.RespuestaPdf;

public interface SVDService {

	public boolean isConfigured();
	
	public void mostrarInfoInyeccion();
	
	public void crearPeticion(PeticionSVDFAP peticion, List<SolicitudGenerica> solicitudes);
	
	public Respuesta enviarPeticionSincrona(PeticionSVDFAP peticion) throws VerificarDatosServiceException;
	
	public Respuesta enviarPeticionAsincrona(PeticionSVDFAP peticion) throws VerificarDatosServiceException;
	
	public Respuesta solicitarRespuestaAsincrona(String idRespuesta) throws VerificarDatosServiceException;
	
	public RespuestaPdf generarPDFRespuesta() throws VerificarDatosServiceException;
	
	public Respuesta peticionRecover() throws VerificarDatosServiceException;
}
