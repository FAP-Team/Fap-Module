package services;

import java.util.HashMap;
import java.util.List;

import models.Documento;
import models.Firmante;
import net.java.dev.jaxb.array.StringArray;
import platino.Firma;
import platino.InfoCert;

public interface FirmaService {

	public boolean hasConnection();

	public String getEndPoint();

	public String getVersion();

	public boolean verificarPKCS7(String texto, String firma);

	public boolean verificarContentSignature(byte[] content, byte[] signature);

	public String firmarPKCS7(String texto);

	public String firmarPKCS7(byte[] bytes);

	public String extraerCertificadoDeFirma(String firma);

	public boolean validarCertificado(String certificado);

	public InfoCert extraerInformacion(String certificado);

	public HashMap<String, String> extraerInfoFromFirma(String firma);

	public List<StringArray> getCertInfo(String certificado) throws Exception;

	/**
	 * Valida la firma y extrae la informacion del firmante
	 * @param contenidoDoc Contenido del documento firmado
	 * @param firma Firma
	 * @return Informacion del firmante
	 */
	public Firmante validateXMLSignature(byte[] contenidoDoc, String firma);

	/**
	 * Valida la firma y la almacena en el AED
	 * @param documento Documento firmado
	 * @param firmantes Lista de firmantes. Se comprueba que la persona no haya firmado ya.
	 * @param firma Firma
	 */
	public void firmar(Documento documento, List<Firmante> firmantes,
			Firma firma);

	/**
	 * Valida la firma y la almacena en el AED
	 * @param documento Documento firmado
	 * @param firmantes Lista de firmantes. Se comprueba que la persona no haya firmado ya.
	 * @param firma Firma
	 * @param valorDocumentofirmanteSolicitado En el caso de que sea != null se comprueba que el certificado del firmante coincida
	 */
	public void firmar(Documento documento, List<Firmante> firmantes,
			Firma firma, String valorDocumentofirmanteSolicitado);

	/**
	 * Permite a un funcionario habilitado firmar, valida la firma y la almacena en el AED
	 * @param documento Documento firmado
	 * @param firmantes Lista de firmantes. Se comprueba que la persona no haya firmado ya.
	 * @param firma Firma
	 * @param valorDocumentofirmanteSolicitado En el caso de que sea != null se comprueba que el certificado del firmante coincida
	 */
	public void firmarFH(Documento documento, Firma firma);

}