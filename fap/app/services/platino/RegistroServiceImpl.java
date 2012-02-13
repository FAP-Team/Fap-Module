package services.platino;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.activation.DataSource;
import javax.inject.Inject;
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
import services.GestorDocumentalService;
import services.FirmaService;
import services.GestorDocumentalServiceException;
import services.RegistroServiceException;
import services.RegistroService;
import utils.BinaryResponse;
import utils.CharsetUtils;
import utils.WSUtils;
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

/**
 * RegistroServiceImpl
 * 
 * El servicio esta preparado para inicializarse de forma lazy.
 * Por lo tanto siempre que se vaya a consumir el servicio web
 * se deberia acceder a "getRegistroPort" en lugar de acceder directamente
 * a la property
 * 
 */
public class RegistroServiceImpl implements RegistroService {

	private static Logger log = Logger.getLogger(RegistroService.class);

	private final PropertyPlaceholder propertyPlaceholder;

	private final Registro registroPort;

	private final FirmaService firmaService;

	private final GestorDocumentalService gestorDocumentalService;
	
	private final PlatinoGestorDocumentalService platinoGestorDocumentalService;
	
	private final String USERNAME;
	private final String PASSWORD;
	private final String PASSWORD_ENC;
	private final String ALIAS;
	private final String ASUNTO;
	private final long UNIDAD_ORGANICA;
	
	@Inject
	public RegistroServiceImpl(PropertyPlaceholder propertyPlaceholder, FirmaService firmaService, GestorDocumentalService gestorDocumentalService){
	    this.propertyPlaceholder = propertyPlaceholder;
	    this.firmaService = firmaService;
	    this.gestorDocumentalService = gestorDocumentalService;
	    
        URL wsdlURL = Registro_Service.class.getClassLoader().getResource("wsdl/registro.wsdl");
        registroPort = new Registro_Service(wsdlURL).getRegistroPort();
        WSUtils.configureEndPoint(registroPort, getEndPoint());
        WSUtils.configureSecurityHeaders(registroPort, propertyPlaceholder);
        PlatinoProxy.setProxy(registroPort); 
        
        USERNAME = FapProperties.get("fap.platino.registro.username");
        PASSWORD = FapProperties.get("fap.platino.registro.password");
        ALIAS = FapProperties.get("fap.platino.registro.aliasServidor");
        PASSWORD_ENC = encriptarPassword(PASSWORD);
        ASUNTO = FapProperties.get("fap.platino.registro.asunto");
        UNIDAD_ORGANICA = FapProperties.getLong("fap.platino.registro.unidadOrganica");
        
        this.platinoGestorDocumentalService = new PlatinoGestorDocumentalService(propertyPlaceholder);
	}
	
	private String encriptarPassword(String password){
        try {
            return PlatinoSecurityUtils.encriptarPassword(password);
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando la contraseña");
        }	    
	}
	
	public boolean isConfigured(){
	    return hasConnection() && platinoGestorDocumentalService.hasConnection();
	}
	
	public boolean hasConnection() {
		boolean hasConnection = false;
		try {
			hasConnection = getVersion() != null;
		} catch (Exception e) {
			log.info("RegistroServiceImpl no tiene coneccion con " + getEndPoint());
		}
		return hasConnection;
	}

	private String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.registro.url");
	}

	private String getVersion() {
		return registroPort.getVersion();
	}

	@Override
	public models.JustificanteRegistro registrarEntrada(Solicitante solicitante, Documento documento, ExpedientePlatino expediente) throws RegistroServiceException{
	    DatosRegistro datosRegistro = getDatosRegistro(solicitante, documento, expediente);
	    String datos = getDatosRegistroNormalizados(expediente, datosRegistro);
	    String datosFirmados = firmarDatosRegistro(datos);
        JustificanteRegistro justificantePlatino = registroDeEntrada(datos, datosFirmados);
        models.JustificanteRegistro justificante = getJustificanteRegistroModel(justificantePlatino);
        return justificante;
	}
	
    private DatosRegistro getDatosRegistro(Solicitante solicitante, Documento documento, ExpedientePlatino expediente) throws RegistroServiceException {
        XMLGregorianCalendar fechaApertura = WSUtils.getXmlGregorianCalendar(expediente.fechaApertura); 

        // Rellenamos datos expediente
        DatosExpediente datosExp = new DatosExpediente();
        datosExp.setNumero(expediente.numero);
        datosExp.setFechaApertura(fechaApertura);
        datosExp.setCreadoPlatino(expediente.getCreado());
        datosExp.setRuta(expediente.ruta);

        // Rellenamos datos Documento
        DatosDocumento datosDoc = new DatosDocumento();
        datosDoc.setTipoDoc("SOL");
        datosDoc.setTipoMime("application/pdf");

        documento.prepararParaSubir();
        if (documento.descripcion == null)
            throw new NullPointerException();

        datosDoc.setDescripcion(documento.descripcion);
        datosDoc.setFecha(fechaApertura);

        try {
            models.Firma firma = gestorDocumentalService.getFirma(documento);
            if (firma != null){
                play.Logger.info("Poniendo firma");
                datosDoc.setFirma(firma);
            }else{
                play.Logger.info("El documento no está firmado");
            }
        }catch(Exception e){
            throw new RegistroServiceException("Error recuperando la firma del documento", e);
        }

        try {
            BinaryResponse contentResponse = gestorDocumentalService.getDocumento(documento);
            DataSource dataSource = contentResponse.contenido.getDataSource();
            datosDoc.setContenido(dataSource);
        }catch(Exception e){
            throw new RegistroServiceException("Error recuperando el documento del gestor documental", e);
        }
        
        
        log.info("Contenido del documento obtenido");
        DatosRegistro datosRegistro = new DatosRegistro();
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
	
    private String getDatosRegistroNormalizados(ExpedientePlatino expedientePlatino, DatosRegistro datosRegistro) throws RegistroServiceException {
        log.info("Ruta expediente " + datosRegistro.getExpediente().getRuta());
        
        crearExpedienteSiNoExiste(expedientePlatino);
        
        // Documento que se va a registrar
        es.gobcan.platino.servicios.registro.Documento doc = insertarDocumentoGestorDocumentalPlatino(expedientePlatino, datosRegistro.getDocumento());
        Documentos documentosRegistrar = new Documentos();
        documentosRegistrar.getDocumento().add(doc);
              
        // 3) Normalizamos los datos
        // Interesado: IP
        String nombre = datosRegistro.getNombreRemitente();
        String numeroDocumento = datosRegistro.getNumeroDocumento();

        // Se ha de indicar porque si no pone NIF por defecto
        String tipoDocumento = datosRegistro.getTipoDocumento();

        // Poner fecha en la que se produce la solicitud
        XMLGregorianCalendar fecha = WSUtils.getXmlGregorianCalendar(new Date()); 
        Asunto asunto = new Asunto();
        asunto.getContent().add(ASUNTO);

        try {
            String datosAFirmar = registroPort.normalizaDatosFirmados(
                    UNIDAD_ORGANICA, // Organismo
                    asunto, // Asunto
                    nombre, // Nombre remitente
                    null, // TipoTransporte (opcional)
                    tipoDocumento, // TipoDocumento (opcional)
                    numeroDocumento, // NIF remitente
                    fecha, // Fecha en la que se produce la solicitud
                    documentosRegistrar);
            datosAFirmar = CharsetUtils.fromISO2UTF8(datosAFirmar);
            return datosAFirmar;
        } catch (Exception e) {
            log.error("Error normalizando los datos " + e.getMessage());
            log.error("RegistrarEntrada -> EXIT ERROR");
            throw new RegistroServiceException("Error normalizando los datos de registro", e);
        }
    }

    private void crearExpedienteSiNoExiste(ExpedientePlatino expedientePlatino) throws RegistroServiceException {
        if(!expedientePlatino.creado){
            try {
                platinoGestorDocumentalService.crearExpediente(expedientePlatino);
            }catch(Exception e){
                throw new RegistroServiceException("Error al crear el expediente en el gestor documental de platino", e);
            }
        }
    }
    
    private es.gobcan.platino.servicios.registro.Documento insertarDocumentoGestorDocumentalPlatino(
            ExpedientePlatino expedientePlatino, DatosDocumento datosDocumento)
            throws RegistroServiceException {
        try {
            String uri = platinoGestorDocumentalService.guardarDocumento(expedientePlatino.ruta, datosDocumento);
            es.gobcan.platino.servicios.registro.Documento doc = DatosRegistro.documentoSGRDEToRegistro(datosDocumento.getContenido(), uri);
            return doc;
        }catch(Exception e){
            throw new RegistroServiceException("Error al insertar el documento a registrar en el gestor documental de platino", e);
        }
    }
    
    private String firmarDatosRegistro(String datosAFirmar) throws RegistroServiceException {
        try {
            String datosFirmados = firmaService.firmarTexto(datosAFirmar.getBytes("iso-8859-1"));
            return datosFirmados;
        }catch(Exception e){
            throw new RegistroServiceException("Error firmando los datos de registro", e);
        }
    }
    
    private JustificanteRegistro registroDeEntrada(String datos, String datosFirmados) throws RegistroServiceException {
        try {
            JustificanteRegistro justificante = registroPort.registrarEntrada(USERNAME,  PASSWORD_ENC, datos, datosFirmados, ALIAS, null, null);
            return justificante;
        }catch(Exception e){
            throw new RegistroServiceException("Error en la llamada de registro de entrada", e);
        }
    }
    
    private models.JustificanteRegistro getJustificanteRegistroModel(JustificanteRegistro justificantePlatino) {
        DateTime fechaRegistro = getRegistroDateTime(justificantePlatino);
        String numeroRegistro = justificantePlatino.getDatosFirmados().getNúmeroRegistro().getContent().get(0);
        
        BinaryResponse documento = new BinaryResponse();
        documento.contenido = justificantePlatino.getReciboPdf();
        documento.nombre = "Justificante";
        models.JustificanteRegistro result = new models.JustificanteRegistro(numeroRegistro, fechaRegistro, documento);
        return result;
    }
	
    /**
     * Calcula la fecha y hora a partir del justificante de platino
     * @param justificante
     * @return
     */
    private DateTime getRegistroDateTime(JustificanteRegistro justificante) {
        XMLGregorianCalendar fecha = justificante.getDatosFirmados() .getFechaRegistro();
        XMLGregorianCalendar fechaHora = justificante.getDatosFirmados().getHoraRegistro();
        DateTime dateTime = new DateTime(fecha.getYear(), fecha.getMonth(),
                fecha.getDay(), fechaHora.getHour(), fechaHora.getMinute(),
                fechaHora.getSecond(), fechaHora.getMillisecond());
        return dateTime;
    }
	
	
	/**
	 * Registra la solicitud
	 * 
	 * @throws RegistroServiceException

	public void registrarSolicitud(SolicitudGenerica solicitud) throws RegistroServiceException {
		if (!solicitud.registro.fasesRegistro.borrador) {
			Messages.error("Intentando registrar una solicitud que no se ha preparado para firmar");
			throw new RegistroServiceException(
					"Intentando registrar una solicitud que no se ha preparado para firmar");
		}

		if (!solicitud.registro.fasesRegistro.firmada) {
			Messages.error("Intentando registrar una solicitud que no ha sido firmada");
			throw new RegistroServiceException(
					"Intentando registrar una solicitud que no ha sido firmada");
		}

		// mira si se aportaron todos los documentos necesarios
		/* TODO ESTO SE DEBERíA HACER EN OTRO SITIO
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
			throw new RegistroServiceException("Faltan documentos imprescindibles");
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
				throw new RegistroServiceException(
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
				throw new RegistroServiceException(
						"Error al obtener el justificante del registro de entrada");
			}
		} else {
			play.Logger.debug("La solicitud %s ya está registrada",
					solicitud.id);
		}

		// Crea el expediente en el AED
		if (!solicitud.registro.fasesRegistro.expedienteAed) {
			try {
			    aedService.crearExpediente(solicitud);
			    solicitud.registro.fasesRegistro.expedienteAed = true;
			    solicitud.registro.fasesRegistro.save();
			}catch(GestorDocumentalServiceException e){
			    throw new RegistroServiceException("Error creando el expediente", e);
			}
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
			boolean todosClasificados = true;
		    
		    // Clasifica los documentos sin registro
			List<Documento> documentos = new ArrayList<Documento>();
			documentos.addAll(solicitud.documentacion.documentos);
			documentos.add(solicitud.registro.justificante);
			
			try {
			    aedService.clasificarDocumentos(solicitud, documentos);
			}catch(GestorDocumentalServiceException e){
			    todosClasificados = false;
			}

			// Clasifica los documentos con registro de entrada
			List<Documento> documentosRegistrados = new ArrayList<Documento>();
			documentosRegistrados.add(solicitud.registro.oficial);
			
			try {
			    aedService.clasificarDocumentos(solicitud,documentosRegistrados,solicitud.registro.informacionRegistro);
			}catch(GestorDocumentalServiceException e){
			    todosClasificados = false;
			} 

			if (todosClasificados) {
				solicitud.registro.fasesRegistro.clasificarAed = true;
				solicitud.registro.fasesRegistro.save();
			} else {
				Messages.error("Algunos documentos no se pudieron clasificar correctamente");
			}
		} else {
			play.Logger.debug("Ya están clasificados todos los documentos de la solicitud %s",solicitud.id);
		}
	}

	public void registrarAportacionActual(SolicitudGenerica solicitud)
			throws RegistroServiceException {
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
				throw new RegistroServiceException(
						"Error al obtener el justificante del registro de entrada");
			}
		} else {
			play.Logger.debug("La solicitud %s ya está registrada",
					solicitud.id);
		}

		// Clasifica los documentos
		if (aportacion.estado.equals("registrada")) {
			boolean todosClasificados = true;
		    
		    // Clasifica los documentos sin registro
			List<Documento> documentos = new ArrayList<Documento>();
			documentos.addAll(aportacion.documentos);
			documentos.add(aportacion.justificante);
			
			
			try {
			    aedService.clasificarDocumentos(solicitud, documentos);
			}catch(GestorDocumentalServiceException e){
			    todosClasificados = false;
			}

			// Clasifica los documentos con registro de entrada
			List<Documento> documentosRegistrados = new ArrayList<Documento>();
			documentosRegistrados.add(aportacion.oficial);
			
			try {
			    aedService.clasificarDocumentos(solicitud,
                        documentosRegistrados,
                        aportacion.informacionRegistro);
			}catch(Exception e){
			    todosClasificados = false;
			}

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
	 * Aportación sin registro de los documentos
	 * 
	 * @param solicitud
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
				boolean todosClasificados = true;
				try {
				    aedService.clasificarDocumentos(solicitud, documentos);
				}catch(Exception e){
				    todosClasificados = false;
				}

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
    */
}
