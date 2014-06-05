package services.certificados;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.ws.BindingProvider;

import org.joda.time.DateTime;

import config.InjectorConfig;

import platino.PlatinoProxy;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import es.gobcan.certificados.AreaFuncionalResult;
import es.gobcan.certificados.AreaResult;
import es.gobcan.certificados.Areas;
import es.gobcan.certificados.AreasFuncionales;
import es.gobcan.certificados.AreasFuncionalesResult;
import es.gobcan.certificados.AreasResult;
import es.gobcan.certificados.Certificaciones;
import es.gobcan.certificados.CertificadosFull;
import es.gobcan.certificados.CertificadosResult;
import es.gobcan.certificados.CertificadosWeb;
import es.gobcan.certificados.CertificadosWeb_Service;
import es.gobcan.certificados.FirmaResult;
import es.gobcan.certificados.Firmas;
import es.gobcan.certificados.FirmasResult;
import es.gobcan.certificados.MateriaResult;
import es.gobcan.certificados.Materias;
import es.gobcan.certificados.MateriasResult;
import es.gobcan.certificados.Personas;
import es.gobcan.certificados.PersonasResult;
import es.gobcan.certificados.TipoResult;
import es.gobcan.certificados.Tipos;
import es.gobcan.certificados.TiposResult;
import models.Documento;

import services.CertificadosService;
import services.CertificadosServiceException;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import utils.BinaryResponse;

@InjectSupport
public class CertificadosServiceImpl implements CertificadosService {
	
	private static CertificadosWeb_Service certficadosWS;
	private static CertificadosWeb port = null;
	
	private static String usuario = FapProperties.get("fap.certificados.usuario");
	private static Long idAreaFuncional = FapProperties.getLong("fap.certificados.idAreaFuncional");
	
	static{
		URL wsdlURL = CertificadosWeb_Service.class.getClassLoader().getResource("wsdl/certificadosWeb.wsdl");
		certficadosWS = new CertificadosWeb_Service(wsdlURL);
		port = certficadosWS.getCertificadosWebHttpPort();
		
		BindingProvider bp = (BindingProvider)port;
		bp.getRequestContext().put(
					BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
					FapProperties.get("fap.certificados.url"));
		
		PlatinoProxy.setProxy(port);
	}
	
	@Override
	public boolean isConfigured() {
		try {
			return port.getAreas(usuario, idAreaFuncional) != null;
		} catch (Exception e) {
			play.Logger.error("Error al comprobar el servicio de certificados "+e);
		}
		return false;
	}

	@Override
	public void mostrarInfoInyeccion(){
		if(isConfigured())
			play.Logger.info("El servicio de Libro de Certificados ha sido inyectado con Certificados y está operativo");
		else
			play.Logger.info("El servicio de Libro de Certificados Certificados ha sido inyectado con Certificados y NO está operativo");
	}

	@Override
	public List<Areas> getAreas() throws CertificadosServiceException{
		List<Areas> areas = new ArrayList<Areas>();
		
		try {
			AreasResult response = port.getAreas(usuario, idAreaFuncional);
			List<Areas> wsAreas = response.getAreasList().getAreas();
			// Probablemente no haga falta el for
			for(Areas wsArea : wsAreas) {
				if(!wsArea.getAreaFuncional().isBaja())
					areas.add(wsArea);
			}
		} catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio Certificados."+ e);
		}
		return areas;
	}
	
	@Override
	public Areas getArea() throws CertificadosServiceException{
		Areas area = null;
		try {
			AreaResult response = port.getArea(usuario, idAreaFuncional);
			area = response.getArea();
		} catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio Certificados."+ e);
		}
		return area;
	}
	
	
	@Override
	public List<AreasFuncionales> getAreasFuncionales() throws CertificadosServiceException{
		List<AreasFuncionales> areas = new ArrayList<AreasFuncionales>();
		try {
			AreasFuncionalesResult response = port.getAreasFuncionales(usuario, usuario);
			List<AreasFuncionales> wsAreas = response.getAreasFuncionalesList().getAreasFuncionales();
			for(AreasFuncionales wsArea: wsAreas) {
				if(!wsArea.isBaja())
					areas.add(wsArea);

			}
		} catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		return areas;
		
	}
	
	@Override
	public AreasFuncionales getAreaFuncional() throws CertificadosServiceException{
		AreasFuncionales area = null;
		try {
			AreaFuncionalResult response = port.getAreaFuncional(usuario, idAreaFuncional);
			area = response.getAreaFuncional();
		} catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio de Certificados. "+e);
		}
		return area;
	}
	
	@Override
	public List<Firmas> getFirmas() throws CertificadosServiceException{
		List<Firmas> firmas = new ArrayList<Firmas>();
		
		try {
			FirmasResult response = port.getFirmas(usuario, idAreaFuncional);
			List<Firmas> wsFirmas = response.getFirmasList().getFirmas();
			for(Firmas wsFirma : wsFirmas) {
				if(!wsFirma.isBaja())
					firmas.add(wsFirma);
			}
		} catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		
		return firmas;
	}
	
	@Override
	public Firmas getFirma(Long idFirma) throws CertificadosServiceException{
		Firmas firma = null;
		try {
			FirmaResult response = port.getFirma(usuario, idFirma);
			firma = response.getFirma();
		} catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		return firma;
	}
	
	@Override
	public List<Materias> getMaterias() throws CertificadosServiceException{
		List<Materias> materias = new ArrayList<Materias>();
		
		try {
			MateriasResult response = port.getMaterias(usuario, idAreaFuncional);
			List<Materias> wsMaterias = response.getMateriasList().getMaterias();
			for(Materias wsMateria : wsMaterias){
				if(!wsMateria.isBaja())
					materias.add(wsMateria);
			}
		} catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		return materias;
		
	}
	
	@Override
	public Materias getMateria() throws CertificadosServiceException{
		Materias materia = null;
		try {
			MateriaResult response = port.getMateria(usuario, idAreaFuncional);
			materia = response.getMateria();
		} catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		return materia;
	}
	
	@Override
	public List<Tipos> getTipos() throws CertificadosServiceException{
		List<Tipos> tipos = new ArrayList<Tipos>();
		
		try {
			TiposResult response = port.getTipos(usuario, idAreaFuncional);
			List<Tipos> wsTipos = response.getTiposList().getTipos();
			for(Tipos wsTipo : wsTipos){
				if(!wsTipo.isBaja())
					tipos.add(wsTipo);
			}
		} catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		return tipos;
	}
	
	@Override
	public Tipos getTipo() throws CertificadosServiceException{
		Tipos tipo = null;
		try {
			TipoResult response = port.getTipo(usuario, idAreaFuncional);
			tipo = response.getTipo();
		} catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		return tipo;
	}
	
	@Override
	public List<Personas> insertDestinatario(String nombre, String dni) throws CertificadosServiceException{
		List<Personas> persona = new ArrayList<Personas>();
		
		if(nombre.equals("") || dni.equals("")) {
			throw new CertificadosServiceException("Error, los campos dni y nombre no pueden ser vacios");
		}
		try {
			PersonasResult response = port.insertDestinatario(usuario, dni, nombre);
			persona = response.getPersonasList().getPersonas();
		} catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		return persona;
	}
	
	@Override
	public List<Personas> getDestinatario(String dni) throws CertificadosServiceException{
		List<Personas> destinatarios = new ArrayList<Personas>();
		try {
			PersonasResult response = port.getDestinatario(usuario, dni);
			destinatarios = response.getPersonasList().getPersonas();
		} catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		return destinatarios;
	}
	
	@Override
	public boolean usuarioValido(String username) throws CertificadosServiceException{
		boolean isValid = false;
	    try {
	    	isValid = port.usuarioValido(usuario);
	    } catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
	    return isValid;
	}
	
	@Override
	public CertificadosFull obtenerCertificacion(int numero, int anio) throws CertificadosServiceException{
		CertificadosFull cert = null;
		try {
			cert = port.obtenerCertificacion(usuario, numero, anio, idAreaFuncional);
		} catch (Exception e) {
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		return cert;	
	}

	@Override
	public CertificadosResult saveCertificacion() throws CertificadosServiceException{
		Certificaciones cert = new Certificaciones();
		CertificadosResult nuevoCertificado = null;
	
		try {
			cert.setAnio(new GregorianCalendar().get(GregorianCalendar.YEAR));
			cert.setAnulada(false);
			
			AreasFuncionales areaFuncional = new AreasFuncionales();
			areaFuncional.setId(idAreaFuncional);
			cert.setAreaFuncional(areaFuncional);
					
			AreasResult area = port.getAreas(usuario, idAreaFuncional);
			cert.setArea(area.getAreasList().getAreas().get(0));
						
			FirmasResult firmas = port.getFirmas(usuario, idAreaFuncional);
			cert.setFirma(firmas.getFirmasList().getFirmas().get(0));
			
			MateriasResult materias = port.getMaterias(usuario, idAreaFuncional);
			cert.setMateria(materias.getMateriasList().getMaterias().get(0));
			
			TiposResult tipos = port.getTipos(usuario, idAreaFuncional);
			cert.setTipo(tipos.getTiposList().getTipos().get(0));
			
			PersonasResult persona = port.getDestinatario(usuario, "11111111H");
			cert.setPersona(persona.getPersonasList().getPersonas().get(0));
			
			cert.setExtracto("PRUEBA");
			try {
				GregorianCalendar cal = new GregorianCalendar();
				DateTime fecha = new DateTime().now();
				cal.setTimeInMillis(fecha.getMillis());
				cert.setFechaCertificacion(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
				cert.setFechaRec(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
				cert.setFechaSalida(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
			} catch (Exception e) {
				throw new CertificadosServiceException("El formato de fecha no es válido");
			}
			
			cert.setPorSustitucion(false);
			nuevoCertificado = port.saveCertificacion(usuario, cert, usuario);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		
		return nuevoCertificado;
	}
	
	// TODO:
	//Falta implementar la carga del documento al metodo
	//
	@Override
	public CertificadosResult saveCertificacionWithDocumento(Documento documento) throws CertificadosServiceException{
		Certificaciones cert = new Certificaciones();
		CertificadosResult nuevoCertificado = null;
		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);

		BinaryResponse response;
		byte[] contenidoDocumento = null;
		String nombreDocumento = null;
		try {
			response = gestorDocumentalService.getDocumento(documento);
			contenidoDocumento = response.getBytes();
			nombreDocumento = response.nombre;
		} catch (GestorDocumentalServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		try {
			cert.setAnio(new GregorianCalendar().get(GregorianCalendar.YEAR));
			cert.setAnulada(false);
			
			AreasFuncionales areaFuncional = new AreasFuncionales();
			areaFuncional.setId(idAreaFuncional);
			cert.setAreaFuncional(areaFuncional);
					
			AreasResult area = port.getAreas(usuario, idAreaFuncional);
			cert.setArea(area.getAreasList().getAreas().get(0));
			
			FirmasResult firmas = port.getFirmas(usuario, idAreaFuncional);
			cert.setFirma(firmas.getFirmasList().getFirmas().get(0));
			
			MateriasResult materias = port.getMaterias(usuario, idAreaFuncional);
			cert.setMateria(materias.getMateriasList().getMaterias().get(0));
			
			TiposResult tipos = port.getTipos(usuario, idAreaFuncional);
			cert.setTipo(tipos.getTiposList().getTipos().get(0));
			
			PersonasResult persona = port.getDestinatario(usuario, "11111111H");
			cert.setPersona(persona.getPersonasList().getPersonas().get(0));
			
			cert.setExtracto("PRUEBA");
			try {
				GregorianCalendar cal = new GregorianCalendar();
				DateTime fecha = new DateTime().now();
				cal.setTimeInMillis(fecha.getMillis());
				cert.setFechaCertificacion(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
				cert.setFechaRec(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
				cert.setFechaSalida(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
			} catch (Exception e) {
				throw new CertificadosServiceException("El formato de fecha no es válido");
			}
			
			cert.setPorSustitucion(false);
			nuevoCertificado = port.saveCertificacionWithDocumento(usuario, cert, usuario, contenidoDocumento, nombreDocumento);
			} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		
		return nuevoCertificado;
	}
	
	@Override
	public CertificadosResult saveCertificacionWithURI(String URI) throws CertificadosServiceException{
		Certificaciones cert = new Certificaciones();
		CertificadosResult nuevoCertificado = null;
		
		
		try {
			cert.setAnio(new GregorianCalendar().get(GregorianCalendar.YEAR));
			cert.setAnulada(false);
			
			AreasFuncionales areaFuncional = new AreasFuncionales();
			areaFuncional.setId(idAreaFuncional);
			cert.setAreaFuncional(areaFuncional);
					
			AreasResult area = port.getAreas(usuario, idAreaFuncional);
			cert.setArea(area.getAreasList().getAreas().get(0));
			
			FirmasResult firmas = port.getFirmas(usuario, idAreaFuncional);
			cert.setFirma(firmas.getFirmasList().getFirmas().get(0));
			
			MateriasResult materias = port.getMaterias(usuario, idAreaFuncional);
			cert.setMateria(materias.getMateriasList().getMaterias().get(0));
			
			TiposResult tipos = port.getTipos(usuario, idAreaFuncional);
			cert.setTipo(tipos.getTiposList().getTipos().get(0));
			
			PersonasResult persona = port.getDestinatario(usuario, "11111111H");
			cert.setPersona(persona.getPersonasList().getPersonas().get(0));
			
			cert.setExtracto("PRUEBA");
			try {
				GregorianCalendar cal = new GregorianCalendar();
				DateTime fecha = new DateTime().now();
				cal.setTimeInMillis(fecha.getMillis());
				cert.setFechaCertificacion(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
				cert.setFechaRec(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
				cert.setFechaSalida(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
			} catch (Exception e) {
				throw new CertificadosServiceException("El formato de fecha no es válido");
			}
			
			cert.setPorSustitucion(false);
			nuevoCertificado = port.saveCertificacionWithUrl(usuario, cert, usuario, URI);
			} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		
		return nuevoCertificado;
	}
	
	@Override
	public CertificadosResult saveCertificacionWithNde(String NDE) throws CertificadosServiceException{
	
		Certificaciones cert = new Certificaciones();
		CertificadosResult nuevoCertificado = null;
		
		
		try {
			cert.setAnio(new GregorianCalendar().get(GregorianCalendar.YEAR));
			cert.setAnulada(false);
			
			AreasFuncionales areaFuncional = new AreasFuncionales();
			areaFuncional.setId(idAreaFuncional);
			cert.setAreaFuncional(areaFuncional);
					
			AreasResult area = port.getAreas(usuario, idAreaFuncional);
			cert.setArea(area.getAreasList().getAreas().get(0));
			
			FirmasResult firmas = port.getFirmas(usuario, idAreaFuncional);
			cert.setFirma(firmas.getFirmasList().getFirmas().get(0));
			
			MateriasResult materias = port.getMaterias(usuario, idAreaFuncional);
			cert.setMateria(materias.getMateriasList().getMaterias().get(0));
			
			TiposResult tipos = port.getTipos(usuario, idAreaFuncional);
			cert.setTipo(tipos.getTiposList().getTipos().get(0));
			
			PersonasResult persona = port.getDestinatario(usuario, "11111111H");
			cert.setPersona(persona.getPersonasList().getPersonas().get(0));
			
			cert.setExtracto("PRUEBA");
			try {
				GregorianCalendar cal = new GregorianCalendar();
				DateTime fecha = new DateTime().now();
				cal.setTimeInMillis(fecha.getMillis());
				cert.setFechaCertificacion(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
				cert.setFechaRec(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
				cert.setFechaSalida(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
			} catch (Exception e) {
				throw new CertificadosServiceException("El formato de fecha no es válido");
			}
			
			cert.setPorSustitucion(false);
			nuevoCertificado = port.saveCertificacionWithNde(usuario, cert, usuario, NDE);
			} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new CertificadosServiceException("Error al acceder al servicio Certificados. "+e);
		}
		
		return nuevoCertificado;
	}
}
