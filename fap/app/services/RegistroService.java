package services;

import org.joda.time.DateTime;

import es.gobcan.platino.servicios.registro.JustificanteRegistro;
import models.Documento;
import models.ExpedientePlatino;
import models.Solicitante;
import models.SolicitudGenerica;
import platino.DatosRegistro;

public interface RegistroService extends WSService {
	public String getVersion();

	public DatosRegistro getDatosRegistro(Solicitante solicitante,
			Documento documento, ExpedientePlatino expediente) throws Exception;

	public JustificanteRegistro registroDeEntrada(DatosRegistro datosRegistro)
			throws Exception;

	public JustificanteRegistro registroDeEntrada(String datosAFirmar,
			String datosFirmados) throws Exception;
	
	public String obtenerDatosAFirmarRegisto(DatosRegistro datosRegistro)
			throws Exception;
	
	public DateTime getRegistroDateTime(JustificanteRegistro justificante);
	
	public void registrarSolicitud(SolicitudGenerica solicitud)
			throws RegistroException;
	
	public void registrarAportacionActual(SolicitudGenerica solicitud)
			throws RegistroException;
	
	public void noRegistrarAportacionActual(SolicitudGenerica solicitud);
}
