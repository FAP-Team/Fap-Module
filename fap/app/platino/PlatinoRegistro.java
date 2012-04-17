package platino;

import java.io.IOException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.activation.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;

import org.apache.commons.lang.CharSetUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import properties.FapProperties;


import services.RegistroException;
import utils.BinaryResponse;
import utils.CharsetUtils;

import es.gobcan.eadmon.aed.ws.excepciones.AedExcepcion;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Contenido;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firma;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firmante;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesAdministrativas;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;
import es.gobcan.platino.servicios.registro.Asunto;
import es.gobcan.platino.servicios.registro.Documentos;
import es.gobcan.platino.servicios.registro.JustificanteRegistro;
import es.gobcan.platino.servicios.registro.Registro;
import es.gobcan.platino.servicios.registro.Registro_Service;
import es.gobcan.platino.servicios.sgrde.SGRDEServicePortType;
import es.gobcan.platino.servicios.sgrde.SGRDEServiceProxy;

import aed.AedClient;

import models.Documento;
import models.ExpedientePlatino;
import models.PersonaFisica;
import models.PersonaJuridica;
import models.Solicitante;
import models.SolicitudGenerica;

import java.util.*;

public class PlatinoRegistro {
	
	private static Logger log = Logger.getLogger(PlatinoRegistro.class);
	
	private static Registro registro;

	static {
		URL wsdlURL = Registro_Service.class.getClassLoader().getResource("wsdl/registro.wsdl");
		registro = new Registro_Service(wsdlURL).getRegistroPort();

		BindingProvider bp = (BindingProvider)registro;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				FapProperties.get("fap.platino.registro.url"));

		PlatinoCXFSecurityHeaders.addSoapWSSHeader(
				registro,
				PlatinoCXFSecurityHeaders.SOAP_11,
				FapProperties.get(
						"fap.platino.security.backoffice.uri"),
						FapProperties.get(
						"fap.platino.security.certificado.alias"),
				KeystoreCallbackHandler.class.getName(), null);

		
		PlatinoProxy.setProxy(registro);
		
		//Depuración
//		Client client = ClientProxy.getClient(registro);
//		client.getInInterceptors().add(new LoggingInInterceptor());
//		client.getOutInterceptors().add(new LoggingOutInterceptor());
		
//		HTTPConduit http = (HTTPConduit) client.getConduit();
//		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
//		httpClientPolicy.setConnectionTimeout(36000);
//		httpClientPolicy.setAllowChunking(false);
//		httpClientPolicy.setContentType("text/xml; charset=ISO-8859-1;");
//		http.setClient(httpClientPolicy);
	}
	
	public static String getVersion () {
		return registro.getVersion();
	}
	
	/**
	 * Obtiene los datos del registro de un documento.
	 * @see #getDatosRegistro(Solicitante solicitante, Documento documento, ExpedientePlatino expediente, String descripcion)
	 * @param solicitante
	 * @param documento
	 * @param expediente
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public static DatosRegistro getDatosRegistro(Solicitante solicitante, Documento documento, ExpedientePlatino expediente) throws Exception {
		return getDatosRegistro(solicitante, documento, expediente, null);
	}
	
	
	/**
	 * Obtiene los datos del registro de un documento.
	 * @param solicitante
	 * @param documento
	 * @param expediente
	 * @param descripcion Si descripción = null, se utilizará: "Solicitud " + FapProperties.get("application.name")
	 * @return
	 * @throws Exception
	 */
	public static DatosRegistro getDatosRegistro(Solicitante solicitante, Documento documento, ExpedientePlatino expediente, String descripcion) throws Exception {
			log.debug("Obteniendo los datos de registro del documento " + documento.uri);
			
			DatosRegistro datosRegistro = new DatosRegistro();

			XMLGregorianCalendar fecha = DatatypeFactory.newInstance().newXMLGregorianCalendar(expediente.fechaApertura.toGregorianCalendar());
			
			// Rellenamos datos expediente
			DatosExpediente datosExp = new DatosExpediente();
			datosExp.setNumero(expediente.numero);
			datosExp.setFechaApertura(fecha);
			datosExp.setCreadoPlatino(expediente.getCreado());
			datosExp.setRuta(expediente.ruta);

			// Rellenamos datos Documento
			DatosDocumento datosDoc = new DatosDocumento();
			datosDoc.setTipoDoc("SOL");
			datosDoc.setTipoMime("application/pdf");
			if (descripcion == null)
				descripcion = "Solicitud " + FapProperties.get("application.name");
			datosDoc.setDescripcion(descripcion);
			datosDoc.setFecha(fecha);

			
			String uriSolicitud = documento.uri;
			PropiedadesDocumento propsDoc = AedClient.obtenerPropiedades(uriSolicitud);
			PropiedadesAdministrativas propsAdmin = ((PropiedadesAdministrativas)propsDoc.getPropiedadesAvanzadas());
			Firma firmaAed = propsAdmin.getFirma();
			
			if (firmaAed != null) {
				log.info("El documento a registrar está firmado");
				String firmaDoc = firmaAed.getContenido();
				datosDoc.setFirmaXml(firmaDoc);
				datosDoc.setFirmantes(PlatinoRegistro.getDatosFirmantesAED(firmaAed));
			} else {
				log.info("No se registrará informacion sobre la firma ya que no tiene firma asociada");
			}

			
			play.Logger.info("================Size firmantes en AED", datosDoc.getFirmantes().size());
			
			BinaryResponse contentResponse = AedClient.obtenerDoc(uriSolicitud);
			DataSource dataSource = contentResponse.contenido.getDataSource(); 
			datosDoc.setContenido(dataSource);

			log.info("Contenido del documento obtenido");
			datosRegistro.setExpediente(datosExp);
			datosRegistro.setDocumento(datosDoc);
					
			if (solicitante.isPersonaFisica()) {
				PersonaFisica solFisica = solicitante.fisica;
				datosRegistro.setNombreRemitente(solFisica.getNombreCompleto());
				datosRegistro.setNumeroDocumento(solFisica.nip.valor);
				datosRegistro.setTipoDocumento(solFisica.nip.getPlatinoTipoDocumento());
			} else if (solicitante.isPersonaJuridica()) {
				PersonaJuridica solJuridica = solicitante.juridica;
				datosRegistro.setNombreRemitente(solJuridica.entidad);
				datosRegistro.setNumeroDocumento(solJuridica.cif);
				datosRegistro.setTipoDocumento("C");// CIF
			}
			return datosRegistro;
	}


	
	/**
	 * A partir de la firma del AED rellena una lista de firmantes que se utilizan en lso procesos de registro
	 * @param firmaAed
	 * @return Listt<DatosFirmante>
	 * @throws DatatypeConfigurationException
	 */
	private static List<DatosFirmante> getDatosFirmantesAED(Firma firmaAed) throws DatatypeConfigurationException {
		List<DatosFirmante> listFirmantes = new ArrayList<DatosFirmante>(); 
		for (Firmante firmante : firmaAed.getFirmantes()) {
			DatosFirmante datFirm = new DatosFirmante();
			datFirm.setIdFirmante(firmante.getFirmanteNif());
			datFirm.setDescFirmante(firmante.getFirmanteNombre());
		
			GregorianCalendar gregCal = new GregorianCalendar();
			gregCal.setTime(firmante.getFecha());
			datFirm.setFechaFirma(DatatypeFactory.newInstance().newXMLGregorianCalendar(gregCal));
		
			//TODO: Cambiar cuando se use BD de terceros platino
			datFirm.setCargoFirmante("Solicitante");
			datFirm.setUriFirmante("URITest");
			listFirmantes.add(datFirm);
		}
		return listFirmantes;
	}
	
	public static JustificanteRegistro registroDeEntrada(DatosRegistro datosRegistro) throws Exception {
		log.info("Preparando registro de entrada");
		
		String datosAFirmar = obtenerDatosAFirmarRegisto(datosRegistro);
		log.info(datosAFirmar);
		
		String datosFirmados = FirmaClient.firmarPKCS7(datosAFirmar.getBytes("iso-8859-1"));
		log.info("Datos normalizados firmados");
		
		// 6) Registrar
		try {	
			JustificanteRegistro justificante = registroDeEntrada(datosAFirmar, datosFirmados);
			log.info("Registro de entrada realizado con justificante con NDE " + justificante.getNDE() + " Numero Registro General: " + justificante.getDatosFirmados().getNúmeroRegistro().getContent().get(0)+" Nº Registro Oficina: "+justificante.getDatosFirmados().getNúmeroRegistro().getOficina()+" / "+justificante.getDatosFirmados().getNúmeroRegistro().getNumOficina());
			log.info("RegistrarEntrada -> EXIT OK");
			return justificante;
		} catch (Exception e) {
			log.error("Error al obtener el justificante y EXIT "+e);
			log.error("RegistrarEntrada -> EXIT ERROR");
			throw e;
		}		
	}
	
	
	public static JustificanteRegistro registroDeEntrada(String datosAFirmar, String datosFirmados) throws Exception{
		// Se realiza el registro de Entrada, obteniendo el justificante
		String username = FapProperties.get("fap.platino.registro.username");
		String password = FapProperties.get("fap.platino.registro.password");
		String aliasServidor = FapProperties.get("fap.platino.registro.aliasServidor");
		
		String passwdEncripted = PlatinoSecurityUtils.encriptarPassword(password);
		return registro.registrarEntrada (username, passwdEncripted, datosAFirmar, datosFirmados, aliasServidor, null, null);	
	}
	
	/**
	 * Se almacena el documento en el gestor documental.
	 * Se normalizan los datos de registro.
	 * 
	 * *** LOS DATOS DE REGISTRO SE DEVUELVEN EN codificación iso-8859-1
	 * 
	 * @param datosRegistro
	 * @return
	 * @throws Exception
	 */
	public static String obtenerDatosAFirmarRegisto(DatosRegistro datosRegistro) throws Exception {
		log.info("Ruta expediente "+datosRegistro.getExpediente().getRuta());

		// 2) Guardar documentos en Gestor Documental de Platino. El documento que se guarda es el informe con pie de firma reducido
		Documentos documentosRegistrar = PlatinoGestorDocumentalClient.guardarSolicitudEnGestorDocumental(datosRegistro.getExpediente().getRuta(), datosRegistro.getDocumento());
		log.info("Documento guardado en Gestor Documental Platino");
		
		// 3) Normalizamos los datos
		// Interesado: IP
		
		String nombre = datosRegistro.getNombreRemitente();
		String numeroDocumento = datosRegistro.getNumeroDocumento();
		
		 // Se ha de indicar porque si no pone NIF por defecto
		String tipoDocumento = datosRegistro.getTipoDocumento();
		
		
		
		//Poner fecha en la que se produce la solicitud
		XMLGregorianCalendar fecha = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
		
		Asunto asunto = new Asunto();
		
		String asuntoProperty = FapProperties.get("fap.platino.registro.asunto");
		
		asunto.getContent().add(asuntoProperty);
		
		Long organismo = FapProperties.getLong("fap.platino.registro.unidadOrganica");

		String datosAFirmar = null;	
		try {								
			datosAFirmar = registro.normalizaDatosFirmados(Long.valueOf(organismo),  // Organismo 
																  asunto,				   		   // Asunto 
																  nombre,					       // Nombre remitente
																  null,						       // TipoTransporte (opcional)
																  tipoDocumento,			       // TipoDocumento (opcional)
																  numeroDocumento,			       // NIF remitente
																  fecha, 				           // Fecha en la que se produce la solicitud
																  documentosRegistrar);
			datosAFirmar = CharsetUtils.fromISO2UTF8(datosAFirmar);		
		} catch (Exception e) {
			log.error("Error normalizando los datos "+e.getMessage());
			log.error("RegistrarEntrada -> EXIT ERROR");
			throw e;
		}
		
		log.info("Datos normalizados");
		return datosAFirmar;
	}
	
	/**
	 * Devuelve la fecha de registro
	 * @param justificante
	 * @return
	 */
	public static DateTime getRegistroDateTime(JustificanteRegistro justificante) {
		XMLGregorianCalendar fecha = justificante.getDatosFirmados().getFechaRegistro();
		XMLGregorianCalendar fechaHora = justificante.getDatosFirmados().getHoraRegistro();
		DateTime dateTime = new DateTime(fecha.getYear(),fecha.getMonth(),fecha.getDay(),fechaHora.getHour(),fechaHora.getMinute(),fechaHora.getSecond(),fechaHora.getMillisecond());
		return dateTime;
	}
	
	/**
	 * TODO: Registrar de Salida los datos
	 * @param datosRegistro
	 * @return
	 * @throws Exception
	 */
	public static JustificanteRegistro registroDeSalida(DatosRegistro datosRegistro) throws Exception {
//		log.info("Preparando registro de salida");
//		String datosAFirmar = obtenerDatosAFirmarRegisto(datosRegistro);
//		log.info(datosAFirmar);
//		
//		String datosFirmados = FirmaClient.firmarPKCS7(datosAFirmar.getBytes("iso-8859-1"));
//		log.info("Datos normalizados firmados");
//		
//		// 6) Registrar
//		try {	
//			JustificanteRegistro justificante = registroDeSalida(datosAFirmar, datosFirmados);
//			log.info("Registro de salida realizado con justificante con NDE " + justificante.getNDE() + " Numero Registro General: " + justificante.getDatosFirmados().getNúmeroRegistro().getContent().get(0)+" Nº Registro Oficina: "+justificante.getDatosFirmados().getNúmeroRegistro().getOficina()+" / "+justificante.getDatosFirmados().getNúmeroRegistro().getNumOficina());
//			log.info("RegistrarSalida -> EXIT OK");
//			return justificante;
//		} catch (Exception e) {
//			log.error("Error al obtener el justificante y EXIT "+e);
//			log.error("RegistrarSalida -> EXIT ERROR");
//			throw e;
//		}
		return null;
	}
	
	/**
	 * TODO
	 * @param datosAFirmar
	 * @param datosFirmados
	 * @return
	 * @throws Exception
	 */
	public static JustificanteRegistro registroDeSalida(String datosAFirmar, String datosFirmados) throws Exception{
//		// Se realiza el registro de Salida, obteniendo el justificante
//		String username = FapProperties.get("fap.platino.registro.username");
//		String password = FapProperties.get("fap.platino.registro.password");
//		String aliasServidor = FapProperties.get("fap.platino.registro.aliasServidor");
//		
//		String passwdEncripted = PlatinoSecurityUtils.encriptarPassword(password);
//		return registro.registrarSalida(username, passwdEncripted, datosAFirmar, datosFirmados, aliasServidor, null);
		return null;
	}
}