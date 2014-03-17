package services;

import java.util.List;

import es.gobcan.certificados.Certificaciones;
import es.gobcan.certificados.CertificadosFull;
import es.gobcan.certificados.CertificadosResult;
import es.gobcan.certificados.Firmas;
import es.gobcan.certificados.Materias;
import es.gobcan.certificados.Personas;
import es.gobcan.certificados.Areas;
import es.gobcan.certificados.AreasFuncionales;
import es.gobcan.certificados.Tipos;
import models.Documento;

public interface CertificadosService {

	boolean isConfigured();
	
	void mostrarInfoInyeccion();

	public List<AreasFuncionales> getAreasFuncionales() throws CertificadosServiceException;
	
	public AreasFuncionales getAreaFuncional() throws CertificadosServiceException;
	
	public List<Firmas> getFirmas() throws CertificadosServiceException;
	
	public Firmas getFirma() throws CertificadosServiceException;
	
	public List<Areas> getAreas() throws CertificadosServiceException;
	
	public Areas getArea() throws CertificadosServiceException;

	public List<Personas> insertDestinatario(String nombre, String dni) throws CertificadosServiceException;
	
	public boolean usuarioValido(String username) throws CertificadosServiceException;
	
	public List<Personas> getDestinatario(String dni) throws CertificadosServiceException; 
		
	public List<Materias> getMaterias() throws CertificadosServiceException;
	
	public Materias getMateria() throws CertificadosServiceException;
	
	public List<Tipos> getTipos() throws CertificadosServiceException;
	
	public Tipos getTipo() throws CertificadosServiceException;
	
	public CertificadosResult saveCertificacion() throws CertificadosServiceException;
	
	// Falta implementar la carga del documento al metodo
	public CertificadosResult saveCertificacionWithDocumento(Documento documento) throws CertificadosServiceException;
	
	public CertificadosResult saveCertificacionWithURI(String URI) throws CertificadosServiceException;
	
	public CertificadosResult saveCertificacionWithNde(String NDE) throws CertificadosServiceException;
	
	public CertificadosFull obtenerCertificacion(int numero, int anio) throws CertificadosServiceException;
	
}
