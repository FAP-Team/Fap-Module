package services.aed;

import java.util.List;


import models.TipoDocumento;
import models.Tramite;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.TiposDocumentosExcepcion;
import es.gobcan.eadmon.procedimientos.ws.ProcedimientosExcepcion;
import es.gobcan.eadmon.procedimientos.ws.dominio.Procedimiento;

public interface ProcedimientosService {

	public String getVersion() throws Exception;


	public List<models.Tramite> obtenerTramites()
			throws TiposDocumentosExcepcion, ProcedimientosExcepcion;

	public List<models.Tramite> obtenerTramites(String uriProcedimiento)
			throws TiposDocumentosExcepcion, ProcedimientosExcepcion;

	public List<models.TipoDocumento> obtenerDocumentosEnTramite(
			String uriProcedimiento, String uriTramite)
			throws TiposDocumentosExcepcion, ProcedimientosExcepcion;


	/**
	 * Actualiza los trámites del procedimiento de las properties
	 * @return
	 */
	public boolean actualizarTramites();

	/**
	 * Actualiza los tipos de documentos en la base de datos.
	 * 
	 * - Consulta los documentos que hay en todos los trámites del procedimiento
	 * - Actualiza la tabla de tablas "tiposDocumentos"
	 * 
	 * @return
	 */
	public boolean actualizarTramites(String uriProcedimiento);
	
	public List<Procedimiento> getProcedimientos();

}