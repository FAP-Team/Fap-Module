package services.async;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import models.Documento;
import models.ExpedienteAed;
import models.Firma;
import models.Firmante;
import models.InformacionRegistro;
import models.ResolucionFAP;
import models.SolicitudGenerica;
import models.Tramite;
import play.libs.F.Promise;
import properties.PropertyPlaceholder;
import services.GestorDocumentalServiceException;
import services.aed.Interesados;
import services.filesystem.TipoDocumentoEnTramite;
import services.filesystem.TipoDocumentoGestorDocumental;
import utils.BinaryResponse;

public interface GestorDocumentalServiceAsync {

    public Promise<Integer> configure() throws GestorDocumentalServiceException;

    public Promise<Boolean> isConfigured();
    
    public Promise<Integer> mostrarInfoInyeccion();
    
    public Promise<String> crearExpediente(SolicitudGenerica solicitud) throws GestorDocumentalServiceException;

    public Promise<List<String>> getDocumentosEnExpediente(String expediente) throws GestorDocumentalServiceException;
    
    public Promise<List<models.Documento>> getDocumentosPorTipo(String tipoDocumento) throws GestorDocumentalServiceException;

    public Promise<BinaryResponse> getDocumento(Documento documento) throws GestorDocumentalServiceException;
    
    public Promise<BinaryResponse> getDocumentoConInformeDeFirma(Documento documento) throws GestorDocumentalServiceException;

    public Promise<String> getDocumentoFirmaByUri(String uriDocumento) throws GestorDocumentalServiceException;
    
    public Promise<String> saveDocumentoTemporal(models.Documento documento, InputStream inputStream, String filename) throws GestorDocumentalServiceException;

    public Promise<String> saveDocumentoTemporal(models.Documento documento, File file) throws GestorDocumentalServiceException;

    public Promise<Integer> updateDocumento(Documento documento) throws GestorDocumentalServiceException;

    public Promise<Integer> deleteDocumento(Documento documento) throws GestorDocumentalServiceException;
    
    public Promise<Integer> clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos, InformacionRegistro informacionRegistro, boolean notificable) throws GestorDocumentalServiceException;

    public Promise<Integer> clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos, InformacionRegistro informacionRegistro) throws GestorDocumentalServiceException;

    public Promise<Integer> clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos) throws GestorDocumentalServiceException;
    
    public Promise<Integer> clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos, boolean notificable) throws GestorDocumentalServiceException;

    public Promise<Integer> clasificarDocumentoResolucion(ResolucionFAP resolucionFap) throws GestorDocumentalServiceException;
    
    public Promise<String> crearExpedienteConvocatoria() throws GestorDocumentalServiceException;
    
    public Promise<Integer> agregarFirma(Documento documento, Firma firma) throws GestorDocumentalServiceException;
    
    public Promise<Integer> agregarFirma(Documento documento, String firmaStr) throws GestorDocumentalServiceException;

    public Promise<Firma> getFirma(Documento documento) throws GestorDocumentalServiceException;

    public Promise<List<Tramite>> getTramites() throws GestorDocumentalServiceException;
    
    public Promise<Integer> actualizarCodigosExclusion();
    
    public Promise<List<TipoDocumentoEnTramite>> getTiposDocumentosAportadosCiudadano (models.Tramite tramite);
    
    // Devuelve la expresión regular que queremos que case con parte de la URI del trámite de la verificación, para componer el nombre de la plantilla que se utilizará como cabecera del PDF del requerimiento.
    public Promise<String> getExpReg();
    
    public Promise<List<TipoDocumentoGestorDocumental>> getListTiposDocumentosAportadosCiudadano (models.Tramite tramite);

	public Promise<String> crearExpediente(ExpedienteAed expedienteAed) throws GestorDocumentalServiceException;

	public Promise<String> modificarInteresados(ExpedienteAed expedienteAed, SolicitudGenerica solicitud) throws GestorDocumentalServiceException;

	public Promise<BinaryResponse> getDocumentoByUri(String uriDocumento) throws GestorDocumentalServiceException;
	
	public Promise<BinaryResponse> getDocumentoConInformeDeFirmaByUri(String uriDocumento) throws GestorDocumentalServiceException;
	
	public Promise<Integer> duplicarDocumentoSubido(String uriDocumento) throws GestorDocumentalServiceException;
	
	public Promise<Integer> duplicarDocumentoSubido(String uriDocumento, String descripcionDocumento, Documento dbDocumento) throws GestorDocumentalServiceException;
    
	@Deprecated
	public void duplicarDocumentoSubido(String uriDocumento, SolicitudGenerica solicitud) throws GestorDocumentalServiceException;
	
	@Deprecated
	public void duplicarDocumentoSubido(String uriDocumento, String descripcionDocumento, models.Documento dbDocumento, SolicitudGenerica solicitud) throws GestorDocumentalServiceException;

	public Promise<Integer> copiarDocumentoEnExpediente(String uri, List<ExpedienteAed> expedientesAed) throws GestorDocumentalServiceException;

}
