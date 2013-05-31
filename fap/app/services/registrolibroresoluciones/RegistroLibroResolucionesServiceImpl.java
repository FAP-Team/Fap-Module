package services.registrolibroresoluciones;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.naming.ldap.HasControls;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.ws.BindingProvider;

import org.joda.time.DateTime;

import config.InjectorConfig;

import models.Interesado;
import models.Persona;
import models.ResolucionFAP;

import es.gobcan.resoluciones.AreaResult;
import es.gobcan.resoluciones.Areas;
import es.gobcan.resoluciones.AreasFuncionales;
import es.gobcan.resoluciones.AreasResult;
import es.gobcan.resoluciones.ArrayOfPersonas;
import es.gobcan.resoluciones.Personas;
import es.gobcan.resoluciones.PersonasResult;
import es.gobcan.resoluciones.Recursos;
import es.gobcan.resoluciones.RecursosResult;
import es.gobcan.resoluciones.Resoluciones;
import es.gobcan.resoluciones.ResolucionesResult;
import es.gobcan.resoluciones.ResolucionesWeb;
import es.gobcan.resoluciones.ResolucionesWeb_Service;
import es.gobcan.resoluciones.TipoResult;
import es.gobcan.resoluciones.Tipos;
import es.gobcan.resoluciones.TiposResult;

import play.libs.IO;
import properties.FapProperties;

import registroresolucion.AreaResolucion;
import registroresolucion.RecursoResolucion;
import registroresolucion.RegistroResolucion;
import registroresolucion.TipoResolucion;
import services.RegistroLibroResolucionesService;
import services.RegistroLibroResolucionesServiceException;
import services.aed.AedGestorDocumentalServiceImpl;
import utils.BinaryResponse;
import services.GestorDocumentalService;

import utils.AedUtils;

public class RegistroLibroResolucionesServiceImpl implements RegistroLibroResolucionesService {

	// TODO: Revisar imports.
	private static ResolucionesWeb_Service ss;
	private static ResolucionesWeb port = null;
	 
	private static String usuario = FapProperties.get("fap.resoluciones.usuario");
	private static Long idAreaFuncional = FapProperties.getLong("fap.resoluciones.idAreaFuncional");
	
	static {
		URL wsdlURL = ResolucionesWeb_Service.class.getClassLoader().getResource("wsdl/resolucionesWeb.wsdl");
		ss = new ResolucionesWeb_Service(wsdlURL);
		port = ss.getResolucionesWebHttpPort();
	    
	    BindingProvider bp = (BindingProvider)port;
	    
	    bp.getRequestContext().put(
	    		BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
	    		FapProperties.get("fap.resoluciones.url"));
	}

	public boolean isConfigured() {
		try {
			return port.getAreas(usuario, idAreaFuncional) != null;
		} catch (Exception e) {
			play.Logger.error("Error al comprobar el servicio de Registro" + e);
		}
		return false;
	}
	
	@Override
	public void mostrarInfoInyeccion() {
		if (isConfigured())
			// TODO: inyectado con...
			play.Logger.info("El servicio de Registro de Libro de Resoluciones ha sido inyectado con RegistroLibroResoluciones y está operativo.");
		else
			play.Logger.info("El servicio de Registro de Libro de Resoluciones ha sido inyectado con RegistroLibroResoluciones y NO está operativo.");
	}

	@Override
	public List<TipoResolucion> leerTipos() throws RegistroLibroResolucionesServiceException {
		List<TipoResolucion> tipos = new ArrayList<TipoResolucion>();
		try {
			TiposResult response = port.getTipos(usuario, idAreaFuncional);
			List<Tipos> wsTipos = response.getTiposList().getTipos();
			for (Tipos wsTipo: wsTipos) {
				if (!wsTipo.isBaja()) {
					TipoResolucion tipo = new TipoResolucion(wsTipo.getId(), wsTipo.getCodigo(), wsTipo.getDescripcion());
					tipos.add(tipo);
				}
			}
		} catch (Exception e) {
			throw new RegistroLibroResolucionesServiceException("Error al acceder al servicio de Resoluciones.", e);
		}
		return tipos;
	}

	@Override
	public List<AreaResolucion> leerAreas() throws RegistroLibroResolucionesServiceException {
		List<AreaResolucion> areas = new ArrayList<AreaResolucion>();
		try {
			AreasResult response = port.getAreas(usuario, idAreaFuncional);
			List<Areas> wsAreas = response.getAreasList().getAreas();
			for (Areas wsArea: wsAreas) {
				if (!wsArea.isBaja()) {
					AreaResolucion area = new AreaResolucion(wsArea.getId(), wsArea.getCodigo(), wsArea.getDescripcion());
					areas.add(area);
				}
			}
		} catch (Exception e) {
			throw new RegistroLibroResolucionesServiceException("Error al acceder al servicio de Resoluciones.", e);
		}
		return areas;
	}

	@Override
	public List<RecursoResolucion> leerRecursos() throws RegistroLibroResolucionesServiceException {
		List<RecursoResolucion> recursos = new ArrayList<RecursoResolucion>();
		try {
			RecursosResult response = port.getRecursos(usuario);
			List<Recursos> serviceRecursos = response.getRecursosList().getRecursos();
			for (Recursos wsRecurso: serviceRecursos) {
				if (!wsRecurso.isBaja()) {
					RecursoResolucion recurso = new RecursoResolucion(wsRecurso.getId(), wsRecurso.getCodigoAntiguo() == null ? "": wsRecurso.getCodigoAntiguo(), wsRecurso.getDescripcion());
					recursos.add(recurso);
				}
			}
		} catch (Exception e) {
			throw new RegistroLibroResolucionesServiceException("Error al acceder al servicio de Resoluciones.", e);
		}
		return recursos;
	}

	@Override
	public RegistroResolucion crearResolucion(ResolucionFAP resolucionFAP) throws RegistroLibroResolucionesServiceException {
		
		// Se rellenan los datos del objeto Resolución.
		Resoluciones resolucion = new Resoluciones();
		AreasFuncionales areaFuncional = new AreasFuncionales();
		areaFuncional.setId(idAreaFuncional);
		resolucion.setAreaFuncional(areaFuncional);
		resolucion.setSintesis(resolucionFAP.sintesis);
		resolucion.setObservaciones(resolucionFAP.observaciones);
		resolucion.setPaginas(resolucionFAP.numero_folios);
		
		AreaResult areaResponse = port.getArea(usuario, Long.parseLong(resolucionFAP.areasResolucion, 10));
		resolucion.setArea(areaResponse.getArea());
		play.Logger.info("Area resolucion" + areaResponse.getArea());
		
		TipoResult tipoResponse = port.getTipo(usuario, Long.parseLong(resolucionFAP.tiposResolucion, 10));
		resolucion.setTipo(tipoResponse.getTipo());
		play.Logger.info("Tipo resolucion" + tipoResponse.getTipo());
		
		resolucion.setAnulada(false);
		resolucion.setVigenciaIndefinida(true);
		resolucion.setTomo(1);
		resolucion.setAnio(new GregorianCalendar().get(GregorianCalendar.YEAR));
		
		try {
			GregorianCalendar cal = new GregorianCalendar();
			resolucionFAP.fechaRegistroResolucion = new DateTime().now(); //Asignamos fecha actual
			cal.setTimeInMillis(resolucionFAP.fechaRegistroResolucion.getMillis());
			//cal.setTimeInMillis(System.currentTimeMillis());
			resolucion.setFecha(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
		} catch (Exception e) {
			throw new RegistroLibroResolucionesServiceException("El formato de la fecha no es válido.", e);
		}
		
		// Asociar destinatarios.
		List<Personas> personas = new ArrayList<Personas>();
		
		ArrayOfPersonas arrayPersonas = new ArrayOfPersonas();
		
		
		try {
			for (Interesado interesado: resolucionFAP.getInteresados(resolucionFAP.id)) {
				PersonasResult wsPersonas = port.getDestinatario(usuario, interesado.persona.fisica.nip.valor);
				
				if (wsPersonas.getPersonasList().getPersonas().size() == 0) {
					// El destinatario no existe en el servicio. Es necesario crearlo.
					wsPersonas = port.insertDestinatario(usuario, 
							interesado.persona.fisica.nip.valor,
							interesado.persona.fisica.getNombreCompleto());
				}
				
				// Debo comprobar que no exista ya
				if (personas.size() == 0) {
					personas.add(wsPersonas.getPersonasList().getPersonas().get(0));
				} else {
					boolean encontrado = false;
					for (Personas p: personas) {
						if ((p.getId().equals(wsPersonas.getPersonasList().getPersonas().get(0).getId()))) {
							encontrado = true;
							break;
						}
					}
					if (!encontrado)
						personas.add(wsPersonas.getPersonasList().getPersonas().get(0));
				}
				
			}
			arrayPersonas.getPersonas().addAll(personas);
			resolucion.setPersonas(arrayPersonas);

		} catch (Exception e) {
			throw new RegistroLibroResolucionesServiceException("Error al acceder al servicio de Resoluciones al intentar asociar los Destinatarios.", e);
		}
		
		// Registrar la resolución en el sistema.
		ResolucionesResult  response = null;
		try {
//			String urlExterna = AedUtils.crearFullConInformeDeFirma(resolucionFAP.registro.oficial.uri);
//			if (properties.FapProperties.get("fap.proxy.preserve.host").equals("off")) {
//				urlExterna = utils.AedUtils.crearExternalFirmadoFullUrl(resolucionFAP.registro.oficial.uri);
//			}
//			play.Logger.info("La url del documento para la resolución es: "+urlExterna);
//			response = port.saveResolucionWithUrl(usuario, 
//					resolucion,
//					usuario, 
//					urlExterna);
			
			GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
			BinaryResponse br = gestorDocumentalService.getDocumentoConInformeDeFirmaByUri(resolucionFAP.registro.oficial.uri);
			if (br == null)
				play.Logger.error("El documento no tiene contenido");
			response = port.saveResolucionWithDocumento(usuario,
					resolucion,
					usuario,
					br.getBytes(),
					resolucionFAP.tituloInterno);
			/*
			InputStream istream = 
					getDocumentoConInformeDeFirma(resolucionFAP.registro.oficial);
					//AedClientExtended.obtenerDocumentoConInformeFirma(resolucionFAP.registro.oficial.uri);
			byte[] pdfContent = IOUtils.toByteArray(istream);
			response = port.saveResolucionWithDocumento(
					usuario,
					resolucion,
					usuario,
					pdfContent,
					resolucionFAP.registro.oficial.descripcion);
			
			*/
		} catch (Exception e) {
			throw new RegistroLibroResolucionesServiceException("Error al acceder al servicio de Resoluciones.", e);
		}
		
		if (!response.getErrores().equals("")) {
			throw new RegistroLibroResolucionesServiceException("Los datos de entrada han sido rechazados por el servicio de Resoluciones. "+response.getErrores(), new IllegalArgumentException());
		}
		
		Resoluciones responseData = response.getResolucion();
		RegistroResolucion datosRegistro = new RegistroResolucion(responseData.getNumero(), 
				responseData.getFolioInicio(), 
				responseData.getFolioFinal(), 
				new DateTime(responseData.getFechaRec().toGregorianCalendar().getTimeInMillis()));
		
		return datosRegistro;
	}


}
