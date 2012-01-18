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

public interface AedService extends WSService {

	public PropertyPlaceholder getPropertyPlaceholder();

	public String getVersion() throws AedExcepcion;

	public AedPortType getPort();

	/**
	 * Sube un documento a la carpeta temporal del AED El documento debe tener
	 * tipo
	 */
	public String saveDocumentoTemporal(models.Documento documento,
			InputStream is, String filename) throws AedExcepcion;

	public String saveDocumentoTemporal(models.Documento documento, File file)
			throws AedExcepcion;

	/**
	 * Comprueba si un documento está clasificado según el estado de la base de
	 * datos
	 */
	public Boolean isClasificado(String uri);

	public BinaryResponse obtenerDoc(String uri) throws AedExcepcion,
			IOException;

	public byte[] obtenerDocBytes(String uri) throws AedExcepcion, IOException;

	/**
	 * Devuelve las propiedades de un documento. El documento debe estar en la
	 * base de datos apra comprobar si está clasificado o no
	 * 
	 * @param uri
	 * @return
	 * @throws AedExcepcion
	 */
	public PropiedadesDocumento obtenerPropiedades(String uri)
			throws AedExcepcion;

	/**
	 * Devuelve las propiedades de un documento.
	 * 
	 * @param uri
	 * @param clasificado
	 * @return
	 * @throws AedExcepcion
	 */
	public PropiedadesDocumento obtenerPropiedades(String uri,
			Boolean clasificado) throws AedExcepcion;

	public PropiedadesAdministrativas obtenerPropiedadesAdministrativas(
			String uri) throws AedExcepcion;

	/**
	 * Actualiza el tipo y las descripción en el aed de un documento
	 * 
	 * @param documento
	 * @throws AedExcepcion
	 */
	public void actualizarTipoDescripcion(models.Documento documento)
			throws AedExcepcion;

	/**
	 * Crea el expediente en el archivo electrónico para la solicitud Si no
	 * tiene asignado ningún ID de expediente, obtiene uno nuevo Asigna como
	 * interesados el solicitante
	 * 
	 * @param solicitud
	 */
	public void crearExpediente(SolicitudGenerica solicitud);

	/**
	 * Rellena lista con los nombres y los nip/cif de los interesados Dos
	 * opciones: - Solicitante persona física: Nombre y NIP del solicitante y
	 * del representante ( si lo hay ) - Solicitante persona jurídica: Nombre y
	 * Cif de la empresa y Nombre y NIP de los representantes
	 * 
	 * @param solicitud
	 * @param documentos
	 *            Lista que se rellenará con los nip/cif
	 * @param nombres
	 *            Lista que se rellenará con los nombres de los interesados
	 */
	public void asignarInteresados(SolicitudGenerica solicitud,
			List<String> documentos, List<String> nombres);

	/**
	 * Clasifica una lista de documentos Si la información de registro es null
	 * los marca como no registrados TODO: Pasar información de notificable
	 * 
	 * @param documentos
	 * @param informacionRegistro
	 * @throws AedExcepcion
	 */
	public boolean clasificarDocumentos(SolicitudGenerica solicitud,
			List<models.Documento> documentos,
			InformacionRegistro informacionRegistro);

	/**
	 * Clasifica una lista de documentos no registrados
	 * 
	 * @param solicitud
	 * @param documentos
	 * @throws AedExcepcion
	 */
	public boolean clasificarDocumentos(SolicitudGenerica solicitud,
			List<models.Documento> documentos);

	/**
	 * Añade una firma al documento
	 * 
	 * @param uri
	 *            Uri del documento
	 * @param firmante
	 *            Persona que firma
	 * @param firmaContenido
	 *            Contenido de la firma
	 * @throws AedExcepcion
	 */
	public void agregarFirma(String uri, Firmante firmante,
			String firmaContenido) throws AedExcepcion;

	/**
	 * Actualiza las propiedades de un documento
	 * @param propiedades. Propiedades que se actualizarán. Debe estar seteado el campo y
	 *                     debe pertenecer a un documento que exista en la base de datos.
	 * @throws AedExcepcion
	 */
	public void actualizarPropiedades(PropiedadesDocumento propiedades)
			throws AedExcepcion;

	/**
	 * Actualiza las propiedades de un documento
	 * @param propiedades
	 * @param clasificado
	 * @throws AedExcepcion
	 */
	public void actualizarPropiedades(PropiedadesDocumento propiedades,
			Boolean clasificado) throws AedExcepcion;

	/**
	 * Borra un documento del Aed.
	 * @param documento. Documento que se va a borrar. Debe tener seteados los campos:
	 *    - uri
	 *    - clasificado
	 * @throws AedExcepcion
	 */
	public void borrarDocumento(models.Documento documento) throws AedExcepcion;

	public List<String> obtenerUrisDocumentosEnExpediente(String expediente)
			throws AedExcepcion;

}