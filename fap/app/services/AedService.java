package services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import models.Documento;
import models.Firmante;
import models.InformacionRegistro;
import models.SolicitudGenerica;
import properties.PropertyPlaceholder;
import utils.BinaryResponse;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.AedPortType;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesAdministrativas;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;

public interface AedService {

	public abstract PropertyPlaceholder getPropertyPlaceholder();

	public abstract String getEndPoint();

	public abstract String getVersion() throws AedExcepcion;

	public abstract AedPortType getPort();

	/**
	 * Sube un documento a la carpeta temporal del AED
	 * El documento debe tener tipo
	 */
	public abstract String saveDocumentoTemporal(models.Documento documento,
			InputStream is, String filename) throws AedExcepcion;

	public abstract String saveDocumentoTemporal(models.Documento documento,
			File file) throws AedExcepcion;

	/**
	 * Comprueba si un documento está clasificado según
	 * el estado de la base de datos
	 */
	public abstract Boolean isClasificado(String uri);

	public abstract BinaryResponse obtenerDoc(String uri) throws AedExcepcion,
			IOException;

	public abstract byte[] obtenerDocBytes(String uri) throws AedExcepcion,
			IOException;

	/**
	 * Devuelve las propiedades de un documento. El documento debe estar en la base de datos apra comprobar
	 * si está clasificado o no
	 * @param uri
	 * @return
	 * @throws AedExcepcion
	 */
	public abstract PropiedadesDocumento obtenerPropiedades(String uri)
			throws AedExcepcion;

	/**
	 * Devuelve las propiedades de un documento.
	 * @param uri
	 * @param clasificado
	 * @return
	 * @throws AedExcepcion
	 */
	public abstract PropiedadesDocumento obtenerPropiedades(String uri,
			Boolean clasificado) throws AedExcepcion;

	public abstract PropiedadesAdministrativas obtenerPropiedadesAdministrativas(
			String uri) throws AedExcepcion;

	/**
	 * Actualiza el tipo y las descripción en el aed de un documento
	 * @param documento
	 * @throws AedExcepcion
	 */
	public abstract void actualizarTipoDescripcion(models.Documento documento)
			throws AedExcepcion;

	/**
	 * Crea el expediente en el archivo electrónico para la solicitud
	 * Si no tiene asignado ningún ID de expediente, obtiene uno nuevo
	 * Asigna como interesados el solicitante
	 * @param solicitud
	 */
	public abstract void crearExpediente(SolicitudGenerica solicitud);

	/**
	 * Rellena lista con los nombres y los nip/cif de los interesados
	 * Dos opciones:
	 * 		- Solicitante persona física: Nombre y NIP del solicitante y del representante ( si lo hay )
	 *      - Solicitante persona jurídica: Nombre y Cif de la empresa y Nombre y NIP de los representantes
	 * @param solicitud
	 * @param documentos Lista que se rellenará con los nip/cif
	 * @param nombres    Lista que se rellenará con los nombres de los interesados
	 */
	public abstract void asignarInteresados(SolicitudGenerica solicitud,
			List<String> documentos, List<String> nombres);

	/**
	 * Clasifica una lista de documentos
	 * Si la información de registro es null los marca como no registrados
	 * TODO: Pasar información de notificable
	 * @param documentos
	 * @param informacionRegistro
	 * @throws AedExcepcion 
	 */
	public abstract boolean clasificarDocumentos(SolicitudGenerica solicitud,
			List<models.Documento> documentos,
			InformacionRegistro informacionRegistro);

	/**
	 * Clasifica una lista de documentos no registrados
	 * @param solicitud
	 * @param documentos
	 * @throws AedExcepcion
	 */
	public abstract boolean clasificarDocumentos(SolicitudGenerica solicitud,
			List<models.Documento> documentos);

	/**
	 * Añade una firma al documento
	 * 
	 * @param uri            Uri del documento
	 * @param firmante       Persona que firma
	 * @param firmaContenido Contenido de la firma
	 * @throws AedExcepcion
	 */
	public abstract void agregarFirma(String uri, Firmante firmante,
			String firmaContenido) throws AedExcepcion;

	public abstract void actualizarPropiedades(PropiedadesDocumento propiedades)
			throws AedExcepcion;

	public abstract void actualizarPropiedades(
			PropiedadesDocumento propiedades, Boolean clasificado)
			throws AedExcepcion;

	public abstract void borrarDocumento(models.Documento documento)
			throws AedExcepcion;

	public abstract List<String> obtenerUrisDocumentosEnExpediente(
			String expediente) throws AedExcepcion;

}