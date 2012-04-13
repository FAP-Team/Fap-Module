package services;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

import aed.AedClient;

import emails.Mails;
import enumerado.fap.gen.EstadosRequerimientoEnum;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firma;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesAdministrativas;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;
import es.gobcan.platino.servicios.registro.JustificanteRegistro;
import platino.DatosDocumento;
import platino.DatosExpediente;
import platino.DatosRegistro;
import platino.PlatinoGestorDocumentalClient;
import platino.PlatinoRegistro;
import properties.FapProperties;
import utils.BinaryResponse;
import messages.Messages;
import models.Aportacion;
import models.Documento;
import models.ExpedientePlatino;
import models.ObligatoriedadDocumentos;
import models.Persona;
import models.PersonaFisica;
import models.PersonaJuridica;
import models.Requerimiento;
import models.SemillaExpediente;
import models.Singleton;
import models.Solicitante;
import models.SolicitudGenerica;
import models.TableKeyValue;

public class RegistroService {
	
	private static Logger log = Logger.getLogger(RegistroService.class);
	
	/**
	 * Registra la solicitud, pasandole como descripcion null
	 * @see #registrarSolicitud(SolicitudGenerica solicitud, String descripcion)
	 * @param solicitud
	 * @throws RegistroException
	 */
	@Deprecated
	public static void registrarSolicitud(SolicitudGenerica solicitud) throws RegistroException {
		registrarSolicitud(solicitud, null);
	}
	
	/**
	 * Registra la solicitud
	 * @param solicitud
	 * @param descripcion
	 * @throws RegistroException
	 */
	public static void registrarSolicitud(SolicitudGenerica solicitud, String descripcion) throws RegistroException {
		if(!solicitud.registro.fasesRegistro.borrador){
			Messages.error("Intentando registrar una solicitud que no se ha preparado para firmar");
			throw new RegistroException("Intentando registrar una solicitud que no se ha preparado para firmar");
		}
		
		if(!solicitud.registro.fasesRegistro.firmada){
			Messages.error("Intentando registrar una solicitud que no ha sido firmada");
			throw new RegistroException("Intentando registrar una solicitud que no ha sido firmada");
		}

		//mira si se aportaron todos los documentos necesarios
		ObligatoriedadDocumentos docObli = ObligatoriedadDocumentos.get(ObligatoriedadDocumentos.class);
		for (Documento doc : solicitud.documentacion.documentos) {
			if (doc.tipoCiudadano != null) {
				String tipo = doc.tipoCiudadano;
				if(docObli.imprescindibles.remove(tipo)) continue;
				else if(docObli.obligatorias.remove(tipo)) continue;
				else if(docObli.automaticas.remove(tipo)) continue;
			}
		}
		
		if (!docObli.imprescindibles.isEmpty()) {
			for (String uri : docObli.imprescindibles) {
				String _descripcion = TableKeyValue.getValue("tipoDocumentosCiudadanos", uri);
				Messages.error("Documento \""+ _descripcion + "\" es imprescindible");
			}
			throw new RegistroException("Faltan documentos imprescindibles");
		}
		if (!docObli.obligatorias.isEmpty()) {
			for (String uri : docObli.obligatorias) {
				String _descripcion = TableKeyValue.getValue("tipoDocumentosCiudadanos", uri);
				Messages.warning("Documento \""+ _descripcion + "\" pendiente de aportación 1");
			}
		}
		if (!docObli.automaticas.isEmpty()) {
			for (String uri : docObli.automaticas) {
				if (solicitud.documentoEsObligatorio(uri)) {
					String _descripcion = TableKeyValue.getValue("tipoDocumentosCiudadanos", uri);
					Messages.warning("Documento \""+ _descripcion + "\" pendiente de aportación 2");
				}
			}
		}
		
		//Crea el expediente en el archivo electrónico de platino
		if(!solicitud.registro.fasesRegistro.expedientePlatino){
			try {
				PlatinoGestorDocumentalClient.crearExpediente(solicitud.expedientePlatino);
			
				solicitud.registro.fasesRegistro.expedientePlatino = true;
				solicitud.registro.fasesRegistro.save();
			} catch (Exception e) {
				Messages.error("Error creando expediente en el gestor documental de platino");
				throw new RegistroException("Error creando expediente en el gestor documental de platino");
			}
		}else{
			play.Logger.debug("El expediente de platino para la solicitud %s ya está creado", solicitud.id);
		}
		
		
		//Registra la solicitud
		if(!solicitud.registro.fasesRegistro.registro){
			try {
				DatosRegistro datos = PlatinoRegistro.getDatosRegistro(solicitud.solicitante, solicitud.registro.oficial, solicitud.expedientePlatino, descripcion);
				//Registra la solicitud
				JustificanteRegistro justificante = PlatinoRegistro.registroDeEntrada(datos);
				play.Logger.info("Se ha registrado la solicitud %s en platino, solicitud.id");
				
				//Almacena la información de registro
				solicitud.registro.informacionRegistro.setDataFromJustificante(justificante);
				play.Logger.info("Almacenada la información del registro en la base de datos");

				//Guarda el justificante en el AED
				play.Logger.info("Se procede a guardar el justificante de la solicitud %s en el AED", solicitud.id);
				Documento documento = solicitud.registro.justificante;
				documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSolicitud");
				documento.descripcion = "Justificante de registro de la solicitud";
				documento.save();
				AedClient.saveDocumentoTemporal(documento, justificante.getReciboPdf().getInputStream(), "JustificanteSolicitudPDF" + solicitud.id + ".pdf");
				
				solicitud.registro.fasesRegistro.registro = true;
				solicitud.registro.fasesRegistro.save();
				
				play.Logger.info("Justificante almacenado en el AED");
				
				
				// Establecemos las fechas de registro para todos los documentos de la solicitud
				List<Documento> documentos = new ArrayList<Documento>();
				documentos.addAll(solicitud.documentacion.documentos);
				documentos.add(solicitud.registro.justificante);
				documentos.add(solicitud.registro.oficial);
				for (Documento doc: documentos) {
					if (doc.fechaRegistro == null) {
						doc.fechaRegistro = solicitud.registro.informacionRegistro.fechaRegistro;
					}
				}
				play.Logger.info("Fechas de registro establecidas a  "+solicitud.registro.informacionRegistro.fechaRegistro);
				
			} catch (Exception e) {
				Messages.error("Error al registrar de entrada la solicitud");
				throw new RegistroException("Error al obtener el justificante del registro de entrada");
			}
		}else{
			play.Logger.debug("La solicitud %s ya está registrada", solicitud.id);
		}
		
		
		
		//Crea el expediente en el AED
		if(!solicitud.registro.fasesRegistro.expedienteAed){
			AedClient.crearExpediente(solicitud);
			solicitud.registro.fasesRegistro.expedienteAed = true;
			solicitud.registro.fasesRegistro.save();
		}else{
			play.Logger.debug("El expediente del aed para la solicitud %s ya está creado", solicitud.id);
		}

		//Cambiamos el estado de la solicitud
		if (!solicitud.estado.equals("iniciada")) {
			solicitud.estado = "iniciada";
			solicitud.save();
			Mails.enviar("solicitudIniciada", solicitud);
		}
	
		//Clasifica los documentos en el AED
		if(!solicitud.registro.fasesRegistro.clasificarAed){
			//Clasifica los documentos sin registro
			List<Documento> documentos = new ArrayList<Documento>();
			documentos.addAll(solicitud.documentacion.documentos);
			documentos.add(solicitud.registro.justificante);
			boolean todosClasificados = AedClient.clasificarDocumentos(solicitud, documentos);
			
			//Clasifica los documentos con registro de entrada
			List<Documento> documentosRegistrados = new ArrayList<Documento>();
			documentosRegistrados.add(solicitud.registro.oficial);
			todosClasificados = todosClasificados && AedClient.clasificarDocumentos(solicitud, documentosRegistrados, solicitud.registro.informacionRegistro);
			
			if(todosClasificados){
				solicitud.registro.fasesRegistro.clasificarAed = true;
				solicitud.registro.fasesRegistro.save();
			}else{
				Messages.error("Algunos documentos no se pudieron clasificar correctamente");
			}
		}else{
			play.Logger.debug("Ya están clasificados todos los documentos de la solicitud %s", solicitud.id);
		}
	}	 
	
	/**
	 * Registra la aportación actual a partir de la solicitud.
	 * 
	 * @see #registrarAportacionActual(SolicitudGenerica solicitud, String descripcion)
	 * @param solicitud
	 * @throws RegistroException
	 */
	@Deprecated
	public static void registrarAportacionActual(SolicitudGenerica solicitud) throws RegistroException {
		registrarAportacionActual(solicitud, null);
	}
	

	/**
	 * Registra la aportación actual a partir de la solicitud.
	 * @param solicitud
	 * @param descripcion Si descripción = null, se utilizará: "Solicitud de Aportación de Documentación " + FapProperties.get("application.name");
	 * @throws RegistroException
	 */
	public static void registrarAportacionActual(SolicitudGenerica solicitud, String descripcion) throws RegistroException {
		//Registra la solicitud
		
		Aportacion aportacion = solicitud.aportaciones.actual; 
		
		if(aportacion.estado == null){
			Messages.error("La solicitud no está firmada");
		}
		
		//Registro de entrada en platino
		if(aportacion.estado.equals("firmada")){
			try {
				String _descripcion = descripcion;
				if (_descripcion == null)
					_descripcion = "Solicitud de Aportación de Documentación " + FapProperties.get("application.name");
				DatosRegistro datos = PlatinoRegistro.getDatosRegistro(solicitud.solicitante, aportacion.oficial, solicitud.expedientePlatino, _descripcion);
				//Registra la solicitud
				JustificanteRegistro justificante = PlatinoRegistro.registroDeEntrada(datos);
				play.Logger.info("Se ha registrado la solicitud de aportacion de la solicitud %s en platino", solicitud.id);
				
				//Almacena la información de registro
				aportacion.informacionRegistro.setDataFromJustificante(justificante);
				play.Logger.info("Almacenada la información del registro en la base de datos");

				//Guarda el justificante en el AED
				play.Logger.info("Se procede a guardar el justificante de la solicitud %s en el AED", solicitud.id);
				Documento documento = aportacion.justificante;
				documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSolicitud");
				documento.descripcion = "Justificante de registro de la solicitud";
				documento.save();
				AedClient.saveDocumentoTemporal(documento, justificante.getReciboPdf().getInputStream(), "JustificanteSolicitudPDF" + solicitud.id + ".pdf");
				
				aportacion.estado = "registrada";
				aportacion.save();
				Mails.enviar("aportacionRealizada", solicitud);
				
				play.Logger.info("Justificante almacenado en el AED");
			} catch (IllegalArgumentException e){
				play.Logger.warn("No se encontró el ID del mail en la base de datos");
				play.Logger.info("Justificante almacenado en el AED");
			} catch (Exception e) {
				e.printStackTrace();
				Messages.error("Error al registrar de entrada la solicitud");
				throw new RegistroException("Error al obtener el justificante del registro de entrada");
			}
		}else{
			play.Logger.debug("La solicitud %s ya está registrada", solicitud.id);
		}
		
		//Clasifica los documentos
		if(aportacion.estado.equals("registrada")){
			//Clasifica los documentos sin registro
			List<Documento> documentos = new ArrayList<Documento>();
			documentos.addAll(aportacion.documentos);
			documentos.add(aportacion.justificante);
			boolean todosClasificados = AedClient.clasificarDocumentos(solicitud, documentos);
			
			//Clasifica los documentos con registro de entrada
			List<Documento> documentosRegistrados = new ArrayList<Documento>();
			documentosRegistrados.add(aportacion.oficial);
			todosClasificados = todosClasificados && AedClient.clasificarDocumentos(solicitud, documentosRegistrados, aportacion.informacionRegistro);
			
			if(todosClasificados){
				aportacion.estado = "clasificada";
				aportacion.save();
				play.Logger.info("Se clasificaron todos los documentos");
			}else{
				Messages.error("Algunos documentos no se pudieron clasificar correctamente");
			}
		}else{
			play.Logger.debug("Ya están clasificados todos los documentos de la solicitud %s", solicitud.id);
		}
		
		//Mueve la aportación a la lista de aportaciones clasificadas
		//Añade los documentos a la lista de documentos
		if(aportacion.estado.equals("clasificada")){
			solicitud.aportaciones.registradas.add(aportacion);
			solicitud.documentacion.documentos.addAll(aportacion.documentos);
			solicitud.aportaciones.actual = new Aportacion();
			solicitud.save();
			aportacion.estado = "finalizada";
			aportacion.save();
			
			play.Logger.debug("Los documentos de la aportacion se movieron correctamente");
		}
		
		
	}
	
	
	/** Aportación sin registro de los documentos 
	 * 
	 * @param solicitud
	 */
	public static void noRegistrarAportacionActual (SolicitudGenerica solicitud) {
		Aportacion aportacion = solicitud.aportaciones.actual; 
		
		if ((aportacion.fechaAportacionSinRegistro == null) || (aportacion.fechaAportacionSinRegistro.isAfterNow())) {
	        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	        Date date = new Date();
			Messages.error("La fecha de incorporación debe ser anterior a "+dateFormat.format(date));
		}
		
		if (aportacion.estado.equals("borrador")) {
			if (!Messages.hasErrors()){
				play.Logger.info("Se procede a aportar sin registrar en la solicitud: "+solicitud.id);
				play.Logger.info("El estado es "+solicitud.estado);
			
				/// Establecemos la fecha de registro en todos los documentos de la aportación
				for (Documento doc: aportacion.documentos) {
					doc.fechaRegistro = aportacion.fechaAportacionSinRegistro;
					doc.save();
				}
			
				/// Los documentos temporales se pasan a clasificados, pero sin registrar
				List<Documento> documentos = new ArrayList<Documento>();
				documentos.addAll(aportacion.documentos);
				boolean todosClasificados = AedClient.clasificarDocumentos(solicitud, documentos);
			
				if (todosClasificados) {
					aportacion.estado = "clasificada";
					aportacion.save();
					play.Logger.info("Se clasificaron (sin registrar) todos los documentos");
				}else{
					Messages.error("Algunos documentos no se pudieron clasificar (sin registrar) correctamente");
				}
			
			}
		}
		
		//Mueve la aportación a la lista de aportaciones clasificadas
		//Añade los documentos a la lista de documentos
		if(aportacion.estado.equals("clasificada")){
			solicitud.aportaciones.registradas.add(aportacion);
			solicitud.documentacion.documentos.addAll(aportacion.documentos);
			solicitud.aportaciones.actual = new Aportacion();
			solicitud.save();
			// Reseteamos la fecha de aportación sin registro
			aportacion.fechaAportacionSinRegistro = null;
			
			aportacion.estado = "finalizada";
			aportacion.save();
			
			play.Logger.debug("Los documentos de la aportacion se movieron correctamente");
		}
	}
	
	/**
	 * Registra el requerimiento actual
	 * @param solicitud
	 */
	public static void registrarSalidaRequerimientoActual (SolicitudGenerica solicitud) throws RegistroException {
		Requerimiento requerimiento = solicitud.verificacion.requerimiento;
		
//		if(requerimiento.estado.equals(EstadosRequerimientoEnum.creado)){
//			try {
//				DatosRegistro datos = PlatinoRegistro.getDatosRegistro(solicitud.solicitante, aportacion.oficial, solicitud.expedientePlatino);
//				//Registra la solicitud
//				JustificanteRegistro justificante = PlatinoRegistro.registroDeEntrada(datos);
//				play.Logger.info("Se ha registrado la solicitud de aportacion de la solicitud %s en platino", solicitud.id);
						
				//Almacena la información de registro
				//aportacion.informacionRegistro.setDataFromJustificante(justificante);
				//play.Logger.info("Almacenada la información del registro en la base de datos");

				//Guarda el justificante en el AED
				//play.Logger.info("Se procede a guardar el justificante de la solicitud %s en el AED", solicitud.id);
				//Documento documento = aportacion.justificante;
				//documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSolicitud");
				//documento.descripcion = "Justificante de registro de la solicitud";
				//documento.save();
				//AedClient.saveDocumentoTemporal(documento, justificante.getReciboPdf().getInputStream(), "JustificanteSolicitudPDF" + solicitud.id + ".pdf");
						
//				aportacion.estado = "registrada";
//				aportacion.save();
//				Mails.enviar("aportacionRealizada", solicitud);
//						
//				play.Logger.info("Justificante almacenado en el AED");
//			} catch (IllegalArgumentException e){
//				play.Logger.warn("No se encontró el ID del mail en la base de datos");
//				play.Logger.info("Justificante almacenado en el AED");
//			} catch (Exception e) {
//				e.printStackTrace();
//				Messages.error("Error al registrar de entrada la solicitud");
//				throw new RegistroException("Error al obtener el justificante del registro de entrada");
//			}
//		} else {
//			Messages.error("El Requerimiento no ha sido creado");
//		}
		
	}
	
}
