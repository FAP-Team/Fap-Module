package services.filesystem;

import java.util.List;

import es.gobcan.certificados.Areas;
import es.gobcan.certificados.AreasFuncionales;
import es.gobcan.certificados.Certificaciones;
import es.gobcan.certificados.CertificadosFull;
import es.gobcan.certificados.CertificadosResult;
import es.gobcan.certificados.Firmas;
import es.gobcan.certificados.Materias;
import es.gobcan.certificados.Personas;
import es.gobcan.certificados.Tipos;
import models.Documento;
import services.CertificadosService;
import services.CertificadosServiceException;

public class FileSystemCertificadosImpl implements CertificadosService {

	@Override
	public boolean isConfigured() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Libro de Certificados ha sido inyectado con FileSystem y está operativo.");
		else
			play.Logger.info("El servicio de Libro de Certificados ha sido inyectado con FileSystem y NO está operativo.");

	}

	@Override
	public List<AreasFuncionales> getAreasFuncionales()
			throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AreasFuncionales getAreaFuncional()
			throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Firmas> getFirmas() throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Firmas getFirma(Long idFirma) throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Areas> getAreas() throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Areas getArea() throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Personas> insertDestinatario(String nombre, String dni)
			throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean usuarioValido(String username)
			throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Personas> getDestinatario(String dni)
			throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Materias> getMaterias() throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Materias getMateria() throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CertificadosResult saveCertificacion()
			throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CertificadosFull obtenerCertificacion(int numero, int anio)
			throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Tipos> getTipos() throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tipos getTipo() throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CertificadosResult saveCertificacionWithDocumento(Documento documento)
			throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CertificadosResult saveCertificacionWithNde(String NDE)
			throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CertificadosResult saveCertificacionWithURI(String URI)
			throws CertificadosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	

}
