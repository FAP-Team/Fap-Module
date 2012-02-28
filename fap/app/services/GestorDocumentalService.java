package services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import models.Documento;
import models.Firma;
import models.Firmante;
import models.InformacionRegistro;
import models.SolicitudGenerica;
import models.Tramite;
import properties.PropertyPlaceholder;
import utils.BinaryResponse;

public interface GestorDocumentalService {

    public void configure() throws GestorDocumentalServiceException;

    public boolean isConfigured();
    
    public String crearExpediente(SolicitudGenerica solicitud) throws GestorDocumentalServiceException;

    public List<String> getDocumentosEnExpediente(String expediente) throws GestorDocumentalServiceException;

    public BinaryResponse getDocumento(Documento documento) throws GestorDocumentalServiceException;

    public String saveDocumentoTemporal(models.Documento documento, InputStream inputStream, String filename)
            throws GestorDocumentalServiceException;

    public String saveDocumentoTemporal(models.Documento documento, File file) throws GestorDocumentalServiceException;

    public void updateDocumento(Documento documento) throws GestorDocumentalServiceException;

    public void deleteDocumento(Documento documento) throws GestorDocumentalServiceException;

    public void clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos,
            InformacionRegistro informacionRegistro) throws GestorDocumentalServiceException;

    public void clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos)
            throws GestorDocumentalServiceException;

    public void agregarFirma(Documento documento, Firma firma) throws GestorDocumentalServiceException;
    
    public Firma getFirma(Documento documento) throws GestorDocumentalServiceException;

    public List<Tramite> getTramites() throws GestorDocumentalServiceException;
    
}