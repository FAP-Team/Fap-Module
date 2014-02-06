package controllers;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.soap.SOAPFaultException;

import org.joda.time.DateTime;

import messages.Messages;
import models.Agente;
import models.Documento;
import models.LineaResolucionFAP;
import models.ResolucionFAP;
import models.SolicitudGenerica;
import play.mvc.Util;
import properties.FapProperties;
import resolucion.ResolucionBase;
import services.GestorDocumentalService;
import services.PortafirmaFapService;
import services.PortafirmaFapServiceException;
import services.RegistroService;
import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;
import validation.CustomValidation;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.PaginaNotificarResolucionControllerGen;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadoResolucionNotificacionEnum;
import enumerado.fap.gen.EstadoResolucionPublicacionEnum;
import es.gobcan.aciisi.portafirma.ws.dominio.DocumentoAedType;
import es.gobcan.aciisi.portafirma.ws.dominio.ListaDocumentosAedType;
import es.gobcan.aciisi.portafirma.ws.dominio.ListaDocumentosType;
import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudResponseType;
import es.gobcan.aciisi.portafirma.ws.dominio.PrioridadEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.TipoDocumentoEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.TipoSolicitudEnumType;

public class PaginaNotificarResolucionController extends PaginaNotificarResolucionControllerGen {
	
	public static void index(String accion, Long idResolucionFAP) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html");
		}

		ResolucionFAP resolucionFAP = null;
		if ("crear".equals(accion)) {
			resolucionFAP = PaginaNotificarResolucionController.getResolucionFAP();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				resolucionFAP.save();
				idResolucionFAP = resolucionFAP.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			resolucionFAP = PaginaNotificarResolucionController.getResolucionFAP(idResolucionFAP);

		log.info("Visitando página: " + "fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		renderTemplate("fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html", accion, idResolucionFAP, resolucionFAP);
	}

	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formGenerarOficioRemision(Long idResolucionFAP, ResolucionFAP resolucionFAP, String botonGenerarOficioRemision) {
		checkAuthenticity();
		if (!permisoFormGenerarOficioRemision("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		ResolucionFAP dbResolucionFAP = PaginaNotificarResolucionController.getResolucionFAP(idResolucionFAP);

		PaginaNotificarResolucionController.formGenerarOficioRemisionBindReferences(resolucionFAP);

		if (!Messages.hasErrors()) {
			PaginaNotificarResolucionController.formGenerarOficioRemisionValidateCopy("editar", dbResolucionFAP, resolucionFAP);
		}
		ResolucionBase resolBase = null;
		
		if (!Messages.hasErrors()) {
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
				resolBase.generarOficioRemision(idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		} else {
			play.Logger.info("No se genero el documento de oficio de remision para la resolucion "+idResolucionFAP);
		}

		if (!Messages.hasErrors()) {
			PaginaNotificarResolucionController.formGenerarOficioRemisionValidateRules(dbResolucionFAP, resolucionFAP);
		}
		if (!Messages.hasErrors()) {
			dbResolucionFAP.save();
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaNotificarResolucionController.formGenerarOficioRemisionRender(idResolucionFAP);
	}
	
//	@Util
//	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
//	public static void formGenerarOficioRemision(Long idResolucionFAP, String botonGenerarOficioRemision) {
//		checkAuthenticity();
//		if (!permisoFormGenerarOficioRemision("editar")) {
//			Messages.error("No tiene permisos suficientes para realizar la acción");
//		}
//
//		if (!Messages.hasErrors()) {
//
//		}
//
//		if (!Messages.hasErrors()) {
//			PaginaNotificarResolucionController.formGenerarOficioRemisionValidateRules(dbResolucionFAP, resolucionFAP);//formGenerarOficioRemisionValidateRules();
//		}
//		
//		ResolucionBase resolBase = null;
//		if (!Messages.hasErrors()) {
//			try {
//				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
//				resolBase.generarOficioRemision(idResolucionFAP);
//			} catch (Throwable e) {
//				new Exception ("No se ha podido obtener el objeto resolución", e);
//			}
//		} else {
//			play.Logger.info("No se genero el documento de oficio de remision para la resolucion "+idResolucionFAP);
//		}
//
//		if (!Messages.hasErrors()) {
//
//			log.info("Acción Editar de página: " + "fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito");
//		} else
//			log.info("Acción Editar de página: " + "fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
//		PaginaNotificarResolucionController.formGenerarOficioRemisionRender(idResolucionFAP);
//	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static boolean notificar(Long idResolucionFAP, int fapNotificacionPlazoacceso, int fapNotificacionFrecuenciarecordatorioacceso, int fapNotificacionPlazorespuesta, int fapNotificacionFrecuenciarecordatoriorespuesta) {

		ResolucionBase resolBase = null;
		boolean notificada = false;
		try {
			resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			notificada = resolBase.notificarCopiarEnExpedientes(idResolucionFAP, fapNotificacionPlazoacceso, fapNotificacionFrecuenciarecordatorioacceso, fapNotificacionPlazorespuesta, fapNotificacionFrecuenciarecordatoriorespuesta);
			resolBase.resolucion.estadoNotificacion = EstadoResolucionEnum.notificada.name();
			resolBase.resolucion.save();
		} catch (Throwable e) {
			new Exception ("No se ha podido obtener el objeto resolución", e);
		}

		return notificada;
	}

	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formVolver(Long idResolucionFAP) {
		checkAuthenticity();
		if (!permisoFormVolver("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {

		}

		if (!Messages.hasErrors()) {
			PaginaNotificarResolucionController.formVolverValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		} else
			log.info("Acción Editar de página: " + "fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaNotificarResolucionController.formVolverRender(idResolucionFAP);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formEnviarOficiosRemisionPortaFirma(Long idResolucionFAP, ResolucionFAP resolucionFAP, String botonEnviarOficiosRemisionPortaFirma) {
		checkAuthenticity();
		if (!permisoFormEnviarOficiosRemisionPortaFirma("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		ResolucionFAP dbResolucionFAP = PaginaNotificarResolucionController.getResolucionFAP(idResolucionFAP);

		PaginaNotificarResolucionController.formEnviarOficiosRemisionPortaFirmaBindReferences(resolucionFAP);

		if (!Messages.hasErrors()) {

			PaginaNotificarResolucionController.formEnviarOficiosRemisionPortaFirmaValidateCopy("editar", dbResolucionFAP, resolucionFAP);

		}

		if (!Messages.hasErrors()) {
			PaginaNotificarResolucionController.formEnviarOficiosRemisionPortaFirmaValidateRules(dbResolucionFAP, resolucionFAP);
		}
		
		if (!Messages.hasErrors()) {
			
			Agente agenteActual = AgenteController.getAgente();
			
			String titulo = "Oficios de remisión"; // TODO ¿Añadir property?
			String descripcion = "Se firmarán los oficios de remisión de la resolución "+idResolucionFAP; // TODO ¿Añadir property?
			TipoSolicitudEnumType tipoSolicitud = TipoSolicitudEnumType.OTROS;
			PrioridadEnumType prioridad = PrioridadEnumType.NORMAL;
			GregorianCalendar gcal = new GregorianCalendar();
			gcal.setTime(dbResolucionFAP.fechaTopeFirmaOficiosRemision.toDate());
			gcal.setTimeInMillis(dbResolucionFAP.fechaTopeFirmaOficiosRemision.getMillis());
			gcal.set(dbResolucionFAP.fechaTopeFirmaOficiosRemision.getYear(), dbResolucionFAP.fechaTopeFirmaOficiosRemision.getMonthOfYear(), dbResolucionFAP.fechaTopeFirmaOficiosRemision.getDayOfMonth(), 23, 59, 59);
			XMLGregorianCalendar fechaTopeFirma = null;
			try {
				fechaTopeFirma = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
			} catch (DatatypeConfigurationException e) {
				Messages.error("Error al setear la fecha tope de firma.");
				e.printStackTrace();
				play.Logger.error("Error al setear la fecha tope de firma."+ e);
			}
			String idSolicitante = FapProperties.get("portafirma.usuario");
			String idDestinatario = dbResolucionFAP.destinatarioOficioRemisionPortafirma;
			String emailNotificacion = agenteActual.email;
	
			try {
				PortafirmaFapService portafirmaService = InjectorConfig.getInjector().getInstance(PortafirmaFapService.class);
				PortafirmaCrearSolicitudResponse response = portafirmaService.crearSolicitudFirma(titulo, descripcion, tipoSolicitud.name(), prioridad.name(), fechaTopeFirma, idSolicitante, idDestinatario, emailNotificacion, resolucionFAP);
				dbResolucionFAP.idSolicitudFirmaOficiosRemision = response.getIdSolicitud();
				dbResolucionFAP.hacePeticionPortafirmaOficiosRemision = AgenteController.getAgente();
				dbResolucionFAP.estadoNotificacion = EstadoResolucionNotificacionEnum.oficiosRemisionPendientesPortafirma.name();
				dbResolucionFAP.save();
				Messages.ok("Se ha enviado correctamente al portafirma la solicitud de la firma");
			} catch (PortafirmaFapServiceException e) {
				play.Logger.error("Error al crear la solicitud de firma: " + e);
				Messages.error("Error al crear la solicitud de firma");
			} catch (SOAPFaultException e) {
				play.Logger.error("Error al crear la solicitud de firma: " + e);
				Messages.error("Error al crear la solicitud de firma");
			}
		}
		
		if (!Messages.hasErrors()) {
			dbResolucionFAP.save();
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		} else
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaNotificarResolucionController.formEnviarOficiosRemisionPortaFirmaRender(idResolucionFAP);
	}
	
	@Util
	public static void formEnviarOficiosRemisionPortaFirmaValidateCopy(String accion, ResolucionFAP dbResolucionFAP, ResolucionFAP resolucionFAP) {
		CustomValidation.clearValidadas();

		if (secure.checkGrafico("permisoEnviarOficiosRemisionPortafirma", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("resolucionFAP", resolucionFAP);
			CustomValidation.required("resolucionFAP.destinatarioOficioRemisionPortafirma", resolucionFAP.destinatarioOficioRemisionPortafirma);
			CustomValidation.validValueFromTable("resolucionFAP.destinatarioOficioRemisionPortafirma", resolucionFAP.destinatarioOficioRemisionPortafirma);
			dbResolucionFAP.destinatarioOficioRemisionPortafirma = resolucionFAP.destinatarioOficioRemisionPortafirma;
			CustomValidation.required("resolucionFAP.fechaTopeFirmaOficiosRemision", resolucionFAP.fechaTopeFirmaOficiosRemision);
			dbResolucionFAP.fechaTopeFirmaOficiosRemision = resolucionFAP.fechaTopeFirmaOficiosRemision;

		}

		if (dbResolucionFAP.fechaTopeFirmaOficiosRemision != null) {
			
			DateTime today = new DateTime().withTimeAtStartOfDay();
			if (dbResolucionFAP.fechaTopeFirmaOficiosRemision.isBefore(today)) {
				play.Logger.error("La fecha tope de firma no puede ser anterior a hoy.");
				CustomValidation.error("La fecha tope de firma no puede ser anterior a hoy.","resolucionFAP.fechaTopeFirmaOficiosRemision", resolucionFAP.fechaTopeFirmaOficiosRemision);
			}
			int dias = 0;
			// Comprobar la fecha de tope de firma con el ResolucionBase
			try {
				//dias = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getDiasLimiteFirma", dbResolucionFAP.id);
				dias = 1;
				DateTime diaLimite = new DateTime();
				diaLimite = diaLimite.plusDays(dias);
				if (diaLimite.isBefore(dbResolucionFAP.fechaTopeFirmaOficiosRemision)) {
					play.Logger.error("La fecha tope de firma no puede ser posterior a "+diaLimite+".");
					CustomValidation.error("La fecha tope de firma no puede ser posterior a "+diaLimite+".", "resolucionFAP.fechaTopeFirmaOficiosRemision", resolucionFAP.fechaTopeFirmaOficiosRemision);					
				}
			} catch (Throwable e) {
				e.printStackTrace();
				play.Logger.error("No se ha podido calcular el límite de fecha para la firma."+e);
				CustomValidation.error("No se ha podido calcular el límite de fecha para la firma", "resolucionFAP.fechaTopeFirmaOficiosRemision", resolucionFAP.fechaTopeFirmaOficiosRemision);
			}
		}
		
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formComprobarFirmasOficiosRemisionPortaFirma(Long idResolucionFAP, String botonComprobarFirmasOficiosRemisionPortaFirma) {
		checkAuthenticity();
		if (!permisoFormComprobarFirmasOficiosRemisionPortaFirma("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			ResolucionFAP dbResolucionFAP = PaginaNotificarResolucionController.getResolucionFAP(idResolucionFAP);
			
			try {
				PortafirmaFapService portafirmaService = InjectorConfig.getInjector().getInstance(PortafirmaFapService.class);
				if (portafirmaService.comprobarSiResolucionFirmada(dbResolucionFAP, dbResolucionFAP.idSolicitudFirmaOficiosRemision)) {
					dbResolucionFAP.estadoNotificacion = EstadoResolucionNotificacionEnum.oficiosRemisionFirmados.name();
					for (LineaResolucionFAP linea: dbResolucionFAP.lineasResolucion) {
						if (!linea.registro.fasesRegistro.firmada) {
							linea.registro.fasesRegistro.firmada = true;
						}
					}
					dbResolucionFAP.save();
					Messages.ok("Los oficios de remisión de la resolución asociados a la solicitud de firma se han firmado y finalizado correctamente.");
					play.Logger.info("Los oficios de remisión de la resolución [" + idResolucionFAP + "] asociados a la solicitud de firma se han firmado y finalizado correctamente.");
				} else {
					play.Logger.warn("Los oficios de remisión de la resolución ["+dbResolucionFAP.id+"] asociados a la solicitud de firma no han sido firmados y finalizados.");
					Messages.warning("Los oficios de remisión de la resolución asociados a la solicitud de firma no han sido firmados y finalizados.");
					
					String response = portafirmaService.obtenerEstadoFirma(dbResolucionFAP, dbResolucionFAP.idSolicitudFirmaOficiosRemision, FapProperties.get("portafirma.usuario"));
					if (response == null) {
						throw new PortafirmaFapServiceException("No se pudo obtener el estado de la firma: Response null.");
					}
					//TODO: Recuperar comentario del response (que ahora no existe)
					//play.Logger.info("El estado de la solicitud de firma en el portafirma es: "+response+ ": "+response.getComentario());
					play.Logger.info("El estado de la solicitud de firma en el portafirma es: "+response);
					Messages.warning("El estado de la solicitud de firma en el portafirma es: "+response);
					if (response.equalsIgnoreCase("Rechazada")) {
						dbResolucionFAP.estadoNotificacion = EstadoResolucionNotificacionEnum.noNotificada.name();
						dbResolucionFAP.save();
					}
				}
			} catch (PortafirmaFapServiceException e) {
				play.Logger.error("Error al comprobar si ya se han firmado los oficios de remisión en el portafirma: " + e);
				Messages.error("Error al comprobar si ya se han firmado los oficios de remisión en el portafirma.");
			} catch (SOAPFaultException e) {
				play.Logger.error("Error al comprobar si ya se han firmado los oficios de remisión en el portafirma: " + e);
				Messages.error("Error al comprobar si ya se han firmado los oficios de remisión en el portafirma.");
			}
		}

		if (!Messages.hasErrors()) {
			PaginaNotificarResolucionController.formComprobarFirmasOficiosRemisionPortaFirmaValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		} else
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaNotificarResolucionController.formComprobarFirmasOficiosRemisionPortaFirmaRender(idResolucionFAP);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formRegistrarOficiosRemision(Long idResolucionFAP, String botonRegistrarOficiosRemision) {
		checkAuthenticity();
		if (!permisoFormRegistrarOficiosRemision("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			
			ResolucionFAP dbResolucionFAP = PaginaNotificarResolucionController.getResolucionFAP(idResolucionFAP);
			for (LineaResolucionFAP lineaResolucionFAP: dbResolucionFAP.lineasResolucion) {
	
				SolicitudGenerica solicitud = SolicitudGenerica.findById(lineaResolucionFAP.solicitud.id);
				RegistroService registroService = InjectorConfig.getInjector().getInstance(RegistroService.class);
				GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
				
				try  {
					
					if (!lineaResolucionFAP.registro.fasesRegistro.registro) {
						play.Logger.info("Se inicia el proceso de Registro");
						// Se obtiene el justificante de registro de salida del oficio de remisión
						models.JustificanteRegistro justificanteSalida = registroService.registroDeSalida(solicitud.solicitante, lineaResolucionFAP.registro.oficial, solicitud.expedientePlatino, "Oficio de remisión");				
						lineaResolucionFAP.registro.informacionRegistro.setDataFromJustificante(justificanteSalida);
						Documento documento = lineaResolucionFAP.registro.justificante;
						documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSalida");
						documento.descripcion = "Justificante de registro de salida del oficio de remisión";
						documento.save();
						play.Logger.info("Creado el documento de justificante en local, se procede a almacenar en el AED");
						InputStream is = justificanteSalida.getDocumento().contenido.getInputStream();
						gestorDocumentalService.saveDocumentoTemporal(documento, is, "JustificanteOficioRemision" + ".pdf");
						play.Logger.info("Justificante del documento oficio de remisión almacenado en el AED");
						lineaResolucionFAP.registro.fasesRegistro.registro = true;
	
						List<Documento> documentos = new ArrayList<Documento>();
						documentos.add(lineaResolucionFAP.registro.oficial);
						documentos.add(lineaResolucionFAP.registro.justificante);
						
						// Se pone la fecha de registro a los documentos
						for (Documento doc: documentos) {
							if (doc.fechaRegistro == null) {
								doc.fechaRegistro = lineaResolucionFAP.registro.informacionRegistro.fechaRegistro;
								doc.save();
							}
						}
						Messages.ok("Se realizó el registro correctamente");
					}
					if (!lineaResolucionFAP.registro.fasesRegistro.clasificarAed) {
						List<Documento> documentos = new ArrayList<Documento>();
						documentos.add(lineaResolucionFAP.registro.oficial);
						documentos.add(lineaResolucionFAP.registro.justificante);
						play.Logger.info("Se procede a clasificar los documentos oficio de remisión y justificante de la línea: "+lineaResolucionFAP.id);
						// Se clasifican los documentos
						gestorDocumentalService.clasificarDocumentos(solicitud, documentos, false);
						lineaResolucionFAP.registro.fasesRegistro.clasificarAed = true;
						play.Logger.info("Documentos clasificados");
						lineaResolucionFAP.save();
						solicitud.save();
						Messages.ok("Se realizó la clasificación correctamente");
					}
					
				} catch (Throwable e)   {
					Messages.error("Error almacenando el justificante del documento de oficio de remisión en el AED");
					play.Logger.info("Error almacenando el justificante del documento de oficio de remisión en el AED");
				}
			}
		}

		if (!Messages.hasErrors()) {
			PaginaNotificarResolucionController.formRegistrarOficiosRemisionValidateRules();
		}
		
		
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		} else
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaNotificarResolucionController.formRegistrarOficiosRemisionRender(idResolucionFAP);
	}
	
	public static List<ComboItem> selectJefeServicio() {
		List<ComboItem> listaCombo = new ArrayList<ComboItem>();
		try {
			listaCombo = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getJefesServicio");
		} catch (Throwable e) {
			Messages.error("No se pudieron obtener los jefes de servicio");
			play.Logger.error("No se pudieron obtener los jefes de servicio"+e.getMessage());
		}
		return listaCombo;
	}

}
