package services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;
import es.gobcan.eadmon.procedimientos.ws.dominio.TipoDocumentoEnTramite;

import models.Documento;
import models.ExpedienteAed;
import models.Firma;
import models.Firmante;
import models.InformacionRegistro;
import models.SolicitudGenerica;
import models.Tramite;
import properties.PropertyPlaceholder;
import services.aed.Interesados;
import utils.BinaryResponse;

public interface GestorDocumentalService {

    public void configure() throws GestorDocumentalServiceException;

    public boolean isConfigured();
    
    public void mostrarInfoInyeccion();
    
    public String crearExpediente(SolicitudGenerica solicitud) throws GestorDocumentalServiceException;

    public List<String> getDocumentosEnExpediente(String expediente) throws GestorDocumentalServiceException;
    
    public List<models.Documento> getDocumentosPorTipo(String tipoDocumento) throws AedExcepcion;

    public BinaryResponse getDocumento(Documento documento) throws GestorDocumentalServiceException;
    
    public BinaryResponse getDocumentoConInformeDeFirma(Documento documento) throws GestorDocumentalServiceException;

    public String saveDocumentoTemporal(models.Documento documento, InputStream inputStream, String filename)
            throws GestorDocumentalServiceException;

    public String saveDocumentoTemporal(models.Documento documento, File file) throws GestorDocumentalServiceException;

    public void updateDocumento(Documento documento) throws GestorDocumentalServiceException;

    public void deleteDocumento(Documento documento) throws GestorDocumentalServiceException;
    
    public void clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos,
            InformacionRegistro informacionRegistro, boolean notificable) throws GestorDocumentalServiceException;

    public void clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos,
            InformacionRegistro informacionRegistro) throws GestorDocumentalServiceException;

    public void clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos)
            throws GestorDocumentalServiceException;
    
    public void clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos, boolean notificable)
            throws GestorDocumentalServiceException;

    public void agregarFirma(Documento documento, Firma firma) throws GestorDocumentalServiceException;
    
    public void agregarFirma(Documento documento, String firmaStr) throws GestorDocumentalServiceException;

    
    public Firma getFirma(Documento documento) throws GestorDocumentalServiceException;

    public List<Tramite> getTramites() throws GestorDocumentalServiceException;
    
    public void actualizarCodigosExclusion();
    
    public List<TipoDocumentoEnTramite> getTiposDocumentosAportadosCiudadano (models.Tramite tramite);
    
    // Devuelve la expresión regular que queremos que case con parte de la URI del trámite de la verificación, para componer el nombre de la plantilla que se utilizará como cabecera del PDF del requerimiento.
    public String getExpReg();
    
    public List<TipoDocumento> getListTiposDocumentosAportadosCiudadano (models.Tramite tramite);

	String crearExpediente(ExpedienteAed expedienteAed) throws GestorDocumentalServiceException;

	String modificarInteresados(ExpedienteAed expedienteAed, SolicitudGenerica solicitud) throws GestorDocumentalServiceException;

	BinaryResponse getDocumentoByUri(String uriDocumento) throws GestorDocumentalServiceException;
	
	BinaryResponse getDocumentoConInformeDeFirmaByUri(String uriDocumento) throws GestorDocumentalServiceException;
	
	public void duplicarDocumentoSubido(String uriDocumento) throws AedExcepcion, GestorDocumentalServiceException;
	
	public void duplicarDocumentoSubido(String uriDocumento, String descripcionDocumento, Documento dbDocumento) throws AedExcepcion, GestorDocumentalServiceException;
    
	@Deprecated
	public void duplicarDocumentoSubido(String uriDocumento, SolicitudGenerica solicitud) throws AedExcepcion, GestorDocumentalServiceException;
	
	@Deprecated
	public void duplicarDocumentoSubido(String uriDocumento, String descripcionDocumento, models.Documento dbDocumento, SolicitudGenerica solicitud) throws AedExcepcion, GestorDocumentalServiceException;
    
}