package services;

import platino.DatosDocumento;
import es.gobcan.platino.servicios.registro.Documentos;
import es.gobcan.platino.servicios.sgrde.DocumentoExpediente;
import models.ExpedientePlatino;

public interface GestorDocumentalService extends WSService {

	public String getVersion();

	public void crearExpediente(ExpedientePlatino exp) throws Exception;

	public DocumentoExpediente guardarDocumento(String expedientePlatinoRuta,
			DatosDocumento documentoRegistrar) throws Exception;

	public Documentos guardarSolicitudEnGestorDocumental(
			String expedienteGestorDocumentalRuta,
			DatosDocumento documentoRegistrar) throws Exception;
}
