package services;

import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.RespuestaPdf;
import es.gobcan.platino.servicios.svd.Solicitudes;
import es.gobcan.platino.servicios.svd.SolicitudesIdResi;
import platino.*;

public interface VerificarDatosService {
	
	public boolean isConfigured();
	
	public void mostrarInfoInyeccion();
	
	public Respuesta peticionRecover(String uidUsuario, String IdPeticion) throws VerificarDatosServiceException;
	
	public RespuestaPdf peticionPDF (String IdPeticion, String IdTransmision, String uidUsuario) throws VerificarDatosServiceException;
	
	public Respuesta solicitudRespuesta (String uidUsuario, String idPeticion, String codigoCertificado, Integer numElementos) throws VerificarDatosServiceException;
	
	public Respuesta peticionSincronaIdentidad(String codigoCertificado, String uidUsuario, String idSolicitante, String nombreSolicitante, 
			String finalidad, String idExpediente, String unidadTramitadora, String codigoProc, String nombreProc, String nombreCompletoFuncionario, String nif, 
			String valorConsentimiento, String documentacion, String nombreCompleto, String nombre, String apellido1, String apellido2, String tipoDoc) throws VerificarDatosServiceException;
	
	public Respuesta peticionAsincronaIdentidad(SolicitudesIdResi solicitud, String codigoCertificado, String uidUsuario, String idSolicitante, String nombreSolicitante,
			String finalidad, String idExpediente, String unidadTramitadora, String codigoProc, String nombreProc, String nombreCompletoFuncionario, String nif, 
			String valorConsentimiento, String documentacion, String nombreCompleto, String nombre, String apellido1, String apellido2, String tipoDoc) throws VerificarDatosServiceException;
}