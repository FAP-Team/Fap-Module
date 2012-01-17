package services;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.activation.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

import emails.Mails;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firma;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firmante;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesAdministrativas;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;
import es.gobcan.platino.servicios.registro.Asunto;
import es.gobcan.platino.servicios.registro.Documentos;
import es.gobcan.platino.servicios.registro.JustificanteRegistro;
import es.gobcan.platino.servicios.registro.Registro;
import es.gobcan.platino.servicios.registro.Registro_Service;
import platino.DatosDocumento;
import platino.DatosExpediente;
import platino.DatosFirmante;
import platino.DatosRegistro;

import platino.KeystoreCallbackHandler;
import platino.PlatinoCXFSecurityHeaders;
import platino.PlatinoProxy;
import platino.PlatinoSecurityUtils;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import utils.BinaryResponse;
import utils.CharsetUtils;
import utils.WSUtils;
import messages.Messages;
import models.Aportacion;
import models.Documento;
import models.ExpedientePlatino;
import models.ObligatoriedadDocumentos;
import models.Persona;
import models.PersonaFisica;
import models.PersonaJuridica;
import models.SemillaExpediente;
import models.Singleton;
import models.Solicitante;
import models.SolicitudGenerica;
import models.TableKeyValue;

public class RegistroServiceImpl implements RegistroService {

	private static Logger log = Logger.getLogger(RegistroService.class);

	private final PropertyPlaceholder propertyPlaceholder;

	private final AedService aedService;

	private final Registro registro;

	private final FirmaService firmaService;

	private final GestorDocumentalService gestorDocumentalService;
	
	public RegistroServiceImpl(PropertyPlaceholder propertyPlaceholder,
			AedService aedService, FirmaService firmaService,
			GestorDocumentalService gestorDocumentalService) {

		if (propertyPlaceholder == null || aedService == null
				|| firmaService == null || gestorDocumentalService == null)
			throw new NullPointerException();

		this.propertyPlaceholder = propertyPlaceholder;
		this.aedService = aedService;
		this.firmaService = firmaService;
		this.gestorDocumentalService = gestorDocumentalService;
		
		URL wsdlURL = Registro_Service.class.getClassLoader().getResource(
				"wsdl/registro.wsdl");
		registro = new Registro_Service(wsdlURL).getRegistroPort();

		WSUtils.configureEndPoint(registro, getEndPoint());
		WSUtils.configureSecurityHeaders(registro, propertyPlaceholder);
		PlatinoProxy.setProxy(registro);
	}

	@Override
	public boolean hasConnection() {
		boolean hasConnection = false;
		try {
			hasConnection = getVersion() != null;
		} catch (Exception e) {
			log.info("RegistroServiceImpl no tiene coneccion con "
					+ getEndPoint());
		}
		return hasConnection;
	}

	@Override
	public String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.registro.url");
	}

	public String getVersion() {
		return registro.getVersion();
	}

	private static XMLGregorianCalendar toXmlGregorian(DateTime time)
			throws Exception {
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(
				time.toGregorianCalendar());
	}

	public DatosRegistro getDatosRegistro(Solicitante solicitante,
			Documento documento, ExpedientePlatino expediente) throws Exception {
		log.debug("Obteniendo los datos de registro del documento "
				+ documento.uri);

		DatosRegistro datosRegistro = new DatosRegistro();

		XMLGregorianCalendar fecha = toXmlGregorian(expediente.fechaApertura);

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
		datosDoc.setDescripcion("Solicitud "
				+ propertyPlaceholder.get("application.name"));
		datosDoc.setFecha(fecha);

		String uriSolicitud = documento.uri;
		PropiedadesDocumento propsDoc = aedService
				.obtenerPropiedades(uriSolicitud);
		PropiedadesAdministrativas propsAdmin = ((PropiedadesAdministrativas) propsDoc
				.getPropiedadesAvanzadas());
		Firma firmaAed = propsAdmin.getFirma();

		if (firmaAed != null) {
			log.info("El documento a registrar está firmado");
			String firmaDoc = firmaAed.getContenido();
			datosDoc.setFirmaXml(firmaDoc);
			datosDoc.setFirmantes(getDatosFirmantesAED(firmaAed));
		} else {
			log.info("No se registrará informacion sobre la firma ya que no tiene firma asociada");
		}

		log.debug("================Size firmantes en AED "
				+ datosDoc.getFirmantes().size());

		BinaryResponse contentResponse = aedService.obtenerDoc(uriSolicitud);
		DataSource dataSource = contentResponse.contenido.getDataSource();
		datosDoc.setContenido(dataSource);

		log.info("Contenido del documento obtenido");
		datosRegistro.setExpediente(datosExp);
		datosRegistro.setDocumento(datosDoc);

		if (solicitante.isPersonaFisica()) {
			PersonaFisica solFisica = solicitante.fisica;
			datosRegistro.setNombreRemitente(solFisica.getNombreCompleto());
			datosRegistro.setNumeroDocumento(solFisica.nip.valor);
			datosRegistro.setTipoDocumento(solFisica.nip
					.getPlatinoTipoDocumento());
		} else if (solicitante.isPersonaJuridica()) {
			PersonaJuridica solJuridica = solicitante.juridica;
			datosRegistro.setNombreRemitente(solJuridica.entidad);
			datosRegistro.setNumeroDocumento(solJuridica.cif);
			datosRegistro.setTipoDocumento("C");// CIF
		}
		return datosRegistro;
	}

	/**
	 * A partir de la firma del AED rellena una lista de firmantes que se
	 * utilizan en lso procesos de registro
	 * 
	 * @param firmaAed
	 * @return Listt<DatosFirmante>
	 * @throws DatatypeConfigurationException
	 */
	private List<DatosFirmante> getDatosFirmantesAED(Firma firmaAed)
			throws DatatypeConfigurationException {
		List<DatosFirmante> listFirmantes = new ArrayList<DatosFirmante>();
		for (Firmante firmante : firmaAed.getFirmantes()) {
			DatosFirmante datFirm = new DatosFirmante();
			datFirm.setIdFirmante(firmante.getFirmanteNif());
			datFirm.setDescFirmante(firmante.getFirmanteNombre());

			GregorianCalendar gregCal = new GregorianCalendar();
			gregCal.setTime(firmante.getFecha());
			datFirm.setFechaFirma(DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gregCal));

			// TODO: Cambiar cuando se use BD de terceros platino
			datFirm.setCargoFirmante("Solicitante");
			datFirm.setUriFirmante("URITest");
			listFirmantes.add(datFirm);
		}
		return listFirmantes;
	}

	public JustificanteRegistro registroDeEntrada(DatosRegistro datosRegistro)
			throws Exception {
		log.info("Preparando registro de entrada");

		String datosAFirmar = obtenerDatosAFirmarRegisto(datosRegistro);
		log.info(datosAFirmar);

		String datosFirmados = firmaService.firmarPKCS7(datosAFirmar
				.getBytes("iso-8859-1"));
		log.info("Datos normalizados firmados");

		// 6) Registrar
		try {
			JustificanteRegistro justificante = registroDeEntrada(datosAFirmar,
					datosFirmados);
			log.info("Registro de entrada realizado con justificante con NDE "
					+ justificante.getNDE()
					+ " Numero Registro General: "
					+ justificante.getDatosFirmados().getNúmeroRegistro()
							.getContent().get(0)
					+ " Nº Registro Oficina: "
					+ justificante.getDatosFirmados().getNúmeroRegistro()
							.getOficina()
					+ " / "
					+ justificante.getDatosFirmados().getNúmeroRegistro()
							.getNumOficina());
			log.info("RegistrarEntrada -> EXIT OK");
			return justificante;
		} catch (Exception e) {
			log.error("Error al obtener el justificante y EXIT " + e);
			log.error("RegistrarEntrada -> EXIT ERROR");
			throw e;
		}
	}

	public JustificanteRegistro registroDeEntrada(String datosAFirmar,
			String datosFirmados) throws Exception {
		// Se realiza el registro de Entrada, obteniendo el justificante
		String username = FapProperties.get("fap.platino.registro.username");
		String password = FapProperties.get("fap.platino.registro.password");
		String aliasServidor = FapProperties
				.get("fap.platino.registro.aliasServidor");

		String passwdEncripted = PlatinoSecurityUtils
				.encriptarPassword(password);
		return registro.registrarEntrada(username, passwdEncripted,
				datosAFirmar, datosFirmados, aliasServidor, null, null);
	}

	/**
	 * Se almacena el documento en el gestor documental. Se normalizan los datos
	 * de registro.
	 * 
	 * *** LOS DATOS DE REGISTRO SE DEVUELVEN EN codificación iso-8859-1
	 * 
	 * @param datosRegistro
	 * @return
	 * @throws Exception
	 */
	public String obtenerDatosAFirmarRegisto(DatosRegistro datosRegistro)
			throws Exception {
		log.info("Ruta expediente " + datosRegistro.getExpediente().getRuta());

		// 2) Guardar documentos en Gestor Documental de Platino. El documento
		// que se guarda es el informe con pie de firma reducido
		Documentos documentosRegistrar = gestorDocumentalService
				.guardarSolicitudEnGestorDocumental(datosRegistro
						.getExpediente().getRuta(), datosRegistro
						.getDocumento());
		log.info("Documento guardado en Gestor Documental Platino");

		// 3) Normalizamos los datos
		// Interesado: IP

		String nombre = datosRegistro.getNombreRemitente();
		String numeroDocumento = datosRegistro.getNumeroDocumento();

		// Se ha de indicar porque si no pone NIF por defecto
		String tipoDocumento = datosRegistro.getTipoDocumento();

		// Poner fecha en la que se produce la solicitud
		XMLGregorianCalendar fecha = DatatypeFactory.newInstance()
				.newXMLGregorianCalendar(new GregorianCalendar());

		Asunto asunto = new Asunto();

		String asuntoProperty = FapProperties
				.get("fap.platino.registro.asunto");

		asunto.getContent().add(asuntoProperty);

		Long organismo = FapProperties
				.getLong("fap.platino.registro.unidadOrganica");

		String datosAFirmar = null;
		try {
			datosAFirmar = registro.normalizaDatosFirmados(
					Long.valueOf(organismo), // Organismo
					asunto, // Asunto
					nombre, // Nombre remitente
					null, // TipoTransporte (opcional)
					tipoDocumento, // TipoDocumento (opcional)
					numeroDocumento, // NIF remitente
					fecha, // Fecha en la que se produce la solicitud
					documentosRegistrar);
			datosAFirmar = CharsetUtils.fromISO2UTF8(datosAFirmar);
		} catch (Exception e) {
			log.error("Error normalizando los datos " + e.getMessage());
			log.error("RegistrarEntrada -> EXIT ERROR");
			throw e;
		}

		log.info("Datos normalizados");
		return datosAFirmar;
	}

	/**
	 * Devuelve la fecha de registro
	 * 
	 * @param justificante
	 * @return
	 */
	public DateTime getRegistroDateTime(JustificanteRegistro justificante) {
		XMLGregorianCalendar fecha = justificante.getDatosFirmados()
				.getFechaRegistro();
		XMLGregorianCalendar fechaHora = justificante.getDatosFirmados()
				.getHoraRegistro();
		DateTime dateTime = new DateTime(fecha.getYear(), fecha.getMonth(),
				fecha.getDay(), fechaHora.getHour(), fechaHora.getMinute(),
				fechaHora.getSecond(), fechaHora.getMillisecond());
		return dateTime;
	}

	/**
	 * Registra la solicitud
	 * 
	 * @throws RegistroException
	 */
	public void registrarSolicitud(SolicitudGenerica solicitud)
			throws RegistroException {
		if (!solicitud.registro.fasesRegistro.borrador) {
			Messages.error("Intentando registrar una solicitud que no se ha preparado para firmar");
			throw new RegistroException(
					"Intentando registrar una solicitud que no se ha preparado para firmar");
		}

		if (!solicitud.registro.fasesRegistro.firmada) {
			Messages.error("Intentando registrar una solicitud que no ha sido firmada");
			throw new RegistroException(
					"Intentando registrar una solicitud que no ha sido firmada");
		}

		// mira si se aportaron todos los documentos necesarios
		ObligatoriedadDocumentos docObli = ObligatoriedadDocumentos
				.get(ObligatoriedadDocumentos.class);
		for (Documento doc : solicitud.documentacion.documentos) {
			if (doc.tipoCiudadano != null) {
				String tipo = doc.tipoCiudadano;
				if (docObli.imprescindibles.remove(tipo))
					continue;
				else if (docObli.obligatorias.remove(tipo))
					continue;
				else if (docObli.automaticas.remove(tipo))
					continue;
			}
		}

		if (!docObli.imprescindibles.isEmpty()) {
			for (String uri : docObli.imprescindibles) {
				String descripcion = TableKeyValue.getValue(
						"tipoDocumentosCiudadanos", uri);
				Messages.error("Documento \"" + descripcion
						+ "\" es imprescindible");
			}
			throw new RegistroException("Faltan documentos imprescindibles");
		}
		if (!docObli.obligatorias.isEmpty()) {
			for (String uri : docObli.obligatorias) {
				String descripcion = TableKeyValue.getValue(
						"tipoDocumentosCiudadanos", uri);
				Messages.warning("Documento \"" + descripcion
						+ "\" pendiente de aportación 1");
			}
		}
		if (!docObli.automaticas.isEmpty()) {
			for (String uri : docObli.automaticas) {
				if (solicitud.documentoEsObligatorio(uri)) {
					String descripcion = TableKeyValue.getValue(
							"tipoDocumentosCiudadanos", uri);
					Messages.warning("Documento \"" + descripcion
							+ "\" pendiente de aportación 2");
				}
			}
		}

		// Crea el expediente en el archivo electrónico de platino
		if (!solicitud.registro.fasesRegistro.expedientePlatino) {
			try {
				gestorDocumentalService.crearExpediente(solicitud.expedientePlatino);

				solicitud.registro.fasesRegistro.expedientePlatino = true;
				solicitud.registro.fasesRegistro.save();
			} catch (Exception e) {
				Messages.error("Error creando expediente en el gestor documental de platino");
				throw new RegistroException(
						"Error creando expediente en el gestor documental de platino");
			}
		} else {
			play.Logger
					.debug("El expediente de platino para la solicitud %s ya está creado",
							solicitud.id);
		}

		// Registra la solicitud
		if (!solicitud.registro.fasesRegistro.registro) {
			try {
				DatosRegistro datos = getDatosRegistro(solicitud.solicitante,
						solicitud.registro.oficial, solicitud.expedientePlatino);
				// Registra la solicitud
				JustificanteRegistro justificante = registroDeEntrada(datos);
				play.Logger
						.info("Se ha registrado la solicitud %s en platino, solicitud.id");

				// Almacena la información de registro
				solicitud.registro.informacionRegistro
						.setDataFromJustificante(justificante);
				play.Logger
						.info("Almacenada la información del registro en la base de datos");

				// Guarda el justificante en el AED
				play.Logger
						.info("Se procede a guardar el justificante de la solicitud %s en el AED",
								solicitud.id);
				Documento documento = solicitud.registro.justificante;
				documento.tipo = FapProperties
						.get("fap.aed.tiposdocumentos.justificanteRegistroSolicitud");
				documento.descripcion = "Justificante de registro de la solicitud";
				documento.save();
				aedService.saveDocumentoTemporal(documento, justificante
						.getReciboPdf().getInputStream(),
						"JustificanteSolicitudPDF" + solicitud.id + ".pdf");

				solicitud.registro.fasesRegistro.registro = true;
				solicitud.registro.fasesRegistro.save();

				play.Logger.info("Justificante almacenado en el AED");

				// Establecemos las fechas de registro para todos los documentos
				// de la solicitud
				List<Documento> documentos = new ArrayList<Documento>();
				documentos.addAll(solicitud.documentacion.documentos);
				documentos.add(solicitud.registro.justificante);
				documentos.add(solicitud.registro.oficial);
				for (Documento doc : documentos) {
					if (doc.fechaRegistro == null) {
						doc.fechaRegistro = solicitud.registro.informacionRegistro.fechaRegistro;
					}
				}
				play.Logger.info("Fechas de registro establecidas a  "
						+ solicitud.registro.informacionRegistro.fechaRegistro);

			} catch (Exception e) {
				Messages.error("Error al registrar de entrada la solicitud");
				throw new RegistroException(
						"Error al obtener el justificante del registro de entrada");
			}
		} else {
			play.Logger.debug("La solicitud %s ya está registrada",
					solicitud.id);
		}

		// Crea el expediente en el AED
		if (!solicitud.registro.fasesRegistro.expedienteAed) {
			aedService.crearExpediente(solicitud);
			solicitud.registro.fasesRegistro.expedienteAed = true;
			solicitud.registro.fasesRegistro.save();
		} else {
			play.Logger
					.debug("El expediente del aed para la solicitud %s ya está creado",
							solicitud.id);
		}

		// Cambiamos el estado de la solicitud
		if (!solicitud.estado.equals("iniciada")) {
			solicitud.estado = "iniciada";
			solicitud.save();
			Mails.enviar("solicitudIniciada", solicitud);
		}

		// Clasifica los documentos en el AED
		if (!solicitud.registro.fasesRegistro.clasificarAed) {
			// Clasifica los documentos sin registro
			List<Documento> documentos = new ArrayList<Documento>();
			documentos.addAll(solicitud.documentacion.documentos);
			documentos.add(solicitud.registro.justificante);
			boolean todosClasificados = aedService.clasificarDocumentos(
					solicitud, documentos);

			// Clasifica los documentos con registro de entrada
			List<Documento> documentosRegistrados = new ArrayList<Documento>();
			documentosRegistrados.add(solicitud.registro.oficial);
			todosClasificados = todosClasificados
					&& aedService.clasificarDocumentos(solicitud,
							documentosRegistrados,
							solicitud.registro.informacionRegistro);

			if (todosClasificados) {
				solicitud.registro.fasesRegistro.clasificarAed = true;
				solicitud.registro.fasesRegistro.save();
			} else {
				Messages.error("Algunos documentos no se pudieron clasificar correctamente");
			}
		} else {
			play.Logger
					.debug("Ya están clasificados todos los documentos de la solicitud %s",
							solicitud.id);
		}
	}

	public void registrarAportacionActual(SolicitudGenerica solicitud)
			throws RegistroException {
		// Registra la solicitud

		Aportacion aportacion = solicitud.aportaciones.actual;

		if (aportacion.estado == null) {
			Messages.error("La solicitud no está firmada");
		}

		// Registro de entrada en platino
		if (aportacion.estado.equals("firmada")) {
			try {
				DatosRegistro datos = getDatosRegistro(solicitud.solicitante,
						aportacion.oficial, solicitud.expedientePlatino);
				// Registra la solicitud
				JustificanteRegistro justificante = registroDeEntrada(datos);
				play.Logger
						.info("Se ha registrado la solicitud de aportacion de la solicitud %s en platino",
								solicitud.id);

				// Almacena la información de registro
				aportacion.informacionRegistro
						.setDataFromJustificante(justificante);
				play.Logger
						.info("Almacenada la información del registro en la base de datos");

				// Guarda el justificante en el AED
				play.Logger
						.info("Se procede a guardar el justificante de la solicitud %s en el AED",
								solicitud.id);
				Documento documento = aportacion.justificante;
				documento.tipo = FapProperties
						.get("fap.aed.tiposdocumentos.justificanteRegistroSolicitud");
				documento.descripcion = "Justificante de registro de la solicitud";
				documento.save();
				aedService.saveDocumentoTemporal(documento, justificante
						.getReciboPdf().getInputStream(),
						"JustificanteSolicitudPDF" + solicitud.id + ".pdf");

				aportacion.estado = "registrada";
				aportacion.save();
				Mails.enviar("aportacionRealizada", solicitud);

				play.Logger.info("Justificante almacenado en el AED");
			} catch (Exception e) {
				e.printStackTrace();
				Messages.error("Error al registrar de entrada la solicitud");
				throw new RegistroException(
						"Error al obtener el justificante del registro de entrada");
			}
		} else {
			play.Logger.debug("La solicitud %s ya está registrada",
					solicitud.id);
		}

		// Clasifica los documentos
		if (aportacion.estado.equals("registrada")) {
			// Clasifica los documentos sin registro
			List<Documento> documentos = new ArrayList<Documento>();
			documentos.addAll(aportacion.documentos);
			documentos.add(aportacion.justificante);
			boolean todosClasificados = aedService.clasificarDocumentos(
					solicitud, documentos);

			// Clasifica los documentos con registro de entrada
			List<Documento> documentosRegistrados = new ArrayList<Documento>();
			documentosRegistrados.add(aportacion.oficial);
			todosClasificados = todosClasificados
					&& aedService.clasificarDocumentos(solicitud,
							documentosRegistrados,
							aportacion.informacionRegistro);

			if (todosClasificados) {
				aportacion.estado = "clasificada";
				aportacion.save();
				play.Logger.info("Se clasificaron todos los documentos");
			} else {
				Messages.error("Algunos documentos no se pudieron clasificar correctamente");
			}
		} else {
			play.Logger
					.debug("Ya están clasificados todos los documentos de la solicitud %s",
							solicitud.id);
		}

		// Mueve la aportación a la lista de aportaciones clasificadas
		// Añade los documentos a la lista de documentos
		if (aportacion.estado.equals("clasificada")) {
			solicitud.aportaciones.registradas.add(aportacion);
			solicitud.documentacion.documentos.addAll(aportacion.documentos);
			solicitud.aportaciones.actual = new Aportacion();
			solicitud.save();
			aportacion.estado = "finalizada";
			aportacion.save();

			play.Logger
					.debug("Los documentos de la aportacion se movieron correctamente");
		}

	}

	/**
	 * Aportación sin registro de los documentos
	 * 
	 * @param solicitud
	 */
	public void noRegistrarAportacionActual(SolicitudGenerica solicitud) {
		Aportacion aportacion = solicitud.aportaciones.actual;

		if ((aportacion.fechaAportacionSinRegistro == null)
				|| (aportacion.fechaAportacionSinRegistro.isAfterNow())) {
			System.out.println("-> " + aportacion.fechaAportacionSinRegistro);
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			Date date = new Date();
			Messages.error("La fecha de incorporación debe ser anterior a "
					+ dateFormat.format(date));
		}

		if (aportacion.estado.equals("borrador")) {
			if (!Messages.hasErrors()) {
				play.Logger
						.info("Se procede a aportar sin registrar en la solicitud: "
								+ solicitud.id);
				play.Logger.info("El estado es " + solicitud.estado);

				// / Establecemos la fecha de registro en todos los documentos
				// de la aportación
				for (Documento doc : aportacion.documentos) {
					doc.fechaRegistro = aportacion.fechaAportacionSinRegistro;
					doc.save();
				}

				// / Los documentos temporales se pasan a clasificados, pero sin
				// registrar
				List<Documento> documentos = new ArrayList<Documento>();
				documentos.addAll(aportacion.documentos);
				boolean todosClasificados = aedService.clasificarDocumentos(
						solicitud, documentos);

				if (todosClasificados) {
					aportacion.estado = "clasificada";
					aportacion.save();
					play.Logger
							.info("Se clasificaron (sin registrar) todos los documentos");
				} else {
					Messages.error("Algunos documentos no se pudieron clasificar (sin registrar) correctamente");
				}

			}
		}

		// Mueve la aportación a la lista de aportaciones clasificadas
		// Añade los documentos a la lista de documentos
		if (aportacion.estado.equals("clasificada")) {
			solicitud.aportaciones.registradas.add(aportacion);
			solicitud.documentacion.documentos.addAll(aportacion.documentos);
			solicitud.aportaciones.actual = new Aportacion();
			solicitud.save();
			// Reseteamos la fecha de aportación sin registro
			aportacion.fechaAportacionSinRegistro = null;

			aportacion.estado = "finalizada";
			aportacion.save();

			play.Logger
					.debug("Los documentos de la aportacion se movieron correctamente");
		}
	}

}
