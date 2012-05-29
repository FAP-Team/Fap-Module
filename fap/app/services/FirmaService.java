package services;

import java.util.HashMap;
import java.util.List;

import models.Documento;
import models.Firmante;
import models.Solicitante;
import net.java.dev.jaxb.array.StringArray;
import platino.Firma;
import platino.InfoCert;

public interface FirmaService {

    public boolean isConfigured();
    
    public List<String> getFirmaEnClienteJS();
    
    public String firmarTexto(byte[] texto) throws FirmaServiceException;

    public boolean validarFirmaTexto(byte[] texto, String firma) throws FirmaServiceException;

    public String firmarDocumento(byte[] contenidoDocumento) throws FirmaServiceException;

    public boolean validarFirmaDocumento(byte[] contenidoDocumento, String firma) throws FirmaServiceException;

    public InfoCert extraerCertificado(String firma) throws FirmaServiceException;
    
    public Firmante getFirmante(String firma, Documento documento);
    
    public Firmante validateXMLSignature(byte[] contenidoDoc, String firma);
    
    public HashMap<String,String> extraerInfoFromFirma(String firma);
    
    public List<StringArray> getCertInfo(String certificado) throws FirmaServiceException;
    
    public void firmar(Documento documento, List<Firmante> firmantes, String firma, String valorDocumentofirmanteSolicitado);

    /**
     * Verifica si una firma es correcta
     * 
     * @param texto Texto firmado
     * @param firma Firma del texto
     * @return
     * 
     *         public boolean verificarPKCS7(String texto, String firma);
     * 
     *         public boolean verificarContentSignature(byte[] content, byte[]
     *         signature);
     * 
     *         public String extraerCertificadoDeFirma(String firma);
     * 
     *         public boolean validarCertificado(String certificado);
     * 
     * 
     *         public HashMap<String, String> extraerInfoFromFirma(String
     *         firma);
     * 
     *         public List<StringArray> getCertInfo(String certificado) throws
     *         Exception;
     * 
     * 
     * 
     *         /** Valida la firma y la almacena en el AED
     * @param documento Documento firmado
     * @param firmantes Lista de firmantes. Se comprueba que la persona no haya
     *        firmado ya.
     * @param firma Firma
     * 
     *        public void firmar(Documento documento, List<Firmante> firmantes,
     *        Firma firma);
     * 
     *        /** Valida la firma y la almacena en el AED
     * @param documento Documento firmado
     * @param firmantes Lista de firmantes. Se comprueba que la persona no haya
     *        firmado ya.
     * @param firma Firma
     * @param valorDocumentofirmanteSolicitado En el caso de que sea != null se
     *        comprueba que el certificado del firmante coincida
     * 
     *        public void firmar(Documento documento, List<Firmante> firmantes,
     *        Firma firma, String valorDocumentofirmanteSolicitado);
     * 
     *        /** Permite a un funcionario habilitado firmar, valida la firma y
     *        la almacena en el AED
     * @param documento Documento firmado
     * @param firmantes Lista de firmantes. Se comprueba que la persona no haya
     *        firmado ya.
     * @param firma Firma
     * @param valorDocumentofirmanteSolicitado En el caso de que sea != null se
     *        comprueba que el certificado del firmante coincida
     * 
     *        public void firmarFH(Documento documento, Firma firma);
     * 
     * 
     *        /** Comprueba que al menos uno de los firmantes únicos ha firmado
     *        o que hayan firmado todos los firmantes multiples
     * @param firmantes Lista de firmantes
     * @return
     * 
     *         public boolean hanFirmadoTodos(List<Firmante> firmantes);
     * 
     *         /** Borra una lista de firmantes, borrando cada uno de los
     *         firmantes y vaciando la lista
     * @param firmantes
     * 
     *        public void borrarFirmantes(List<Firmante> firmantes);
     * 
     *        /** Dado el solicitante, calcula la lista de persona que pueden
     *        firmar la solicitud
     * 
     * @param solicitante
     * @param firmantes
     * 
     *        public void calcularFirmantes(Solicitante solicitante,
     *        List<Firmante> firmantes);
     */
}