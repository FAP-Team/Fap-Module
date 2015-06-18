package controllers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityTransaction;
import javax.xml.ws.soap.SOAPFaultException;

import messages.Messages;
import models.Agente;
import models.Documento;
import models.Firma;
import models.LineaResolucionFAP;
import models.ResolucionFAP;
import models.SolicitudGenerica;
import models.ResolucionFAP;


import org.joda.time.DateTime;

import play.db.jpa.JPA;
import play.mvc.Util;
import properties.FapProperties;
import resolucion.ResolucionBase;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.PortafirmaFapService;
import services.PortafirmaFapServiceException;
import services.RegistroService;
import services.BDOrganizacion.PlatinoBDOrganizacionServiceImpl;
import services.platino.PlatinoGestorDocumentalService;
import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;
import utils.AedUtils;
import utils.ComboUtils;
import utils.ResolucionUtils;
import validation.CustomValidation;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.PaginaNotificarResolucionControllerGen;
import enumerado.fap.gen.EstadoResolucionNotificacionEnum;
import es.gobcan.aciisi.portafirma.ws.dominio.PrioridadEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.TipoSolicitudEnumType;
import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;

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

		log.info("Visitando página: " + "fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + ", usuario: " + AgenteController.getAgente().name + " Resolución: " + idResolucionFAP);
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
				Messages.error("Error generando los oficios de remisión");
				play.Logger.error("Error generando los oficios de remisión: "+e.getMessage());
				new Exception ("Error generando los de oficios de remisión", e);
			}
		} 

		if (!Messages.hasErrors()) {
			PaginaNotificarResolucionController.formGenerarOficioRemisionValidateRules(dbResolucionFAP, resolucionFAP);
		}
		if (!Messages.hasErrors()) {
			dbResolucionFAP.save();
			Messages.ok("Se ha generado el documento de oficios de remisón satisfactoriamente");
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaNotificarResolucionController.formGenerarOficioRemisionRender(idResolucionFAP);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static boolean notificar(Long idResolucionFAP, int fapNotificacionPlazoacceso, int fapNotificacionFrecuenciarecordatorioacceso, int fapNotificacionPlazorespuesta, int fapNotificacionFrecuenciarecordatoriorespuesta) {

		ResolucionBase resolBase = null;
		boolean notificada = false;
		try {
			resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			notificada = resolBase.notificarCopiarEnExpedientes(idResolucionFAP, fapNotificacionPlazoacceso, fapNotificacionFrecuenciarecordatorioacceso, fapNotificacionPlazorespuesta, fapNotificacionFrecuenciarecordatoriorespuesta);
		} catch (Throwable e) {
			Messages.error("Ha ocurrido un error en el proceso de notificación");
			play.Logger.error("Ha ocurrido un error en el proceso de notificación: "+e.getMessage());
			new Exception ("Ha ocurrido un error en el proceso de notificación", e);
		}
		
		if (!Messages.hasErrors())
			Messages.ok("El proceso de notificación se ha realizado satisfactoriamente");
		Messages.keep();

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

			log.info("Acción Editar de página: " + "fap/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Resolución: " + idResolucionFAP);
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
		ResolucionUtils.actualizarSolicitudesFirmaPortafirmaAntiguasResolucion(dbResolucionFAP);
		
		PaginaNotificarResolucionController.formEnviarOficiosRemisionPortaFirmaBindReferences(resolucionFAP);

		Agente agenteActual = AgenteController.getAgente();
		
		if (!Messages.hasErrors()) {
			resolucionFAP.solicitudFirmaPortafirmaOficioRemision.idSolicitante = agenteActual.usuarioldap;
			PaginaNotificarResolucionController.formEnviarOficiosRemisionPortaFirmaValidateCopy("editar", dbResolucionFAP, resolucionFAP);

			if (!Messages.hasErrors()) {
				if (properties.FapProperties.getBoolean("fap.platino.portafirma")) {
					PlatinoBDOrganizacionServiceImpl platinoDBOrgPort = InjectorConfig.getInjector().getInstance(PlatinoBDOrganizacionServiceImpl.class);
					try {
						dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.uriFuncionarioSolicitante = platinoDBOrgPort.recuperarURIPersona(dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.idSolicitante);
					} catch (DBOrganizacionException_Exception e) {
						play.Logger.error("Error al obtener la uri del funcionario solicitante en la Base de Datos de Organización: " + e.getMessage());
						Messages.error("Error al obtener la uri del funcionario solicitante en la Base de Datos de Organización.");
					}
					if ((dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.uriFuncionarioSolicitante == null) || (dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.uriFuncionarioSolicitante.isEmpty()))
						Messages.error("El usuario "+dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.idSolicitante+" especificado no se encuentra en la Base de Datos de Organización.");
				}
			}
		}

		if (!Messages.hasErrors()) {
			PaginaNotificarResolucionController.formEnviarOficiosRemisionPortaFirmaValidateRules(dbResolucionFAP, resolucionFAP);
		}
		
		if (!Messages.hasErrors()) {
			
			dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.tema = "Oficios de remisión";
			dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.materia = "Se firmarán los oficios de remisión de la resolución "+idResolucionFAP;
			dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.tipoSolicitud = TipoSolicitudEnumType.OTROS.value();
			dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.prioridad = PrioridadEnumType.NORMAL.value();
			dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.tipoDocumento = "SOL";
			dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.emailNotificacion = agenteActual.email;
			if (dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.agenteHaceSolicitud == null)
				dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.agenteHaceSolicitud = new Agente();
			dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.agenteHaceSolicitud = agenteActual;
			for (LineaResolucionFAP linea: dbResolucionFAP.lineasResolucion) {
				if (!linea.registro.fasesRegistro.firmada) {
					Documento documento = new Documento();
					documento.uri = linea.registro.oficial.uri;
					documento.descripcion = linea.registro.oficial.descripcionVisible;
					dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.documentosFirma.add(documento);
				}
			}
			dbResolucionFAP.save();
			try {
				PortafirmaFapService portafirmaService = InjectorConfig.getInjector().getInstance(PortafirmaFapService.class);
				PortafirmaCrearSolicitudResponse portafirmaCrearSolicitudResponse = portafirmaService.crearSolicitudFirma(dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision);
                portafirmaService.entregarSolicitudFirma(dbResolucionFAP.solicitudFirmaPortafirma.idSolicitante, portafirmaCrearSolicitudResponse.getIdSolicitud(), portafirmaCrearSolicitudResponse.getComentarios());
				dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.uriSolicitud = portafirmaCrearSolicitudResponse.getIdSolicitud();
				dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.solicitudEstadoComentario = portafirmaCrearSolicitudResponse.getComentarios();
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
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Resolución: " + idResolucionFAP);
		} else
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaNotificarResolucionController.formEnviarOficiosRemisionPortaFirmaRender(idResolucionFAP);
	}
	
	@Util
	public static void formEnviarOficiosRemisionPortaFirmaValidateCopy(String accion, ResolucionFAP dbResolucionFAP, ResolucionFAP resolucionFAP) {
		CustomValidation.clearValidadas();

		if (secure.checkGrafico("permisoEnviarOficiosRemisionPortafirma", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("resolucionFAP.solicitudFirmaPortafirmaOficioRemision", resolucionFAP.solicitudFirmaPortafirmaOficioRemision);
			CustomValidation.valid("resolucionFAP", resolucionFAP);
			CustomValidation.required("resolucionFAP.solicitudFirmaPortafirmaOficioRemision.idDestinatario", resolucionFAP.solicitudFirmaPortafirmaOficioRemision.idDestinatario);
			CustomValidation.validValueFromTable("resolucionFAP.solicitudFirmaPortafirmaOficioRemision.idDestinatario", resolucionFAP.solicitudFirmaPortafirmaOficioRemision.idDestinatario);
			dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision = resolucionFAP.solicitudFirmaPortafirmaOficioRemision;
			dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.idDestinatario = resolucionFAP.solicitudFirmaPortafirmaOficioRemision.idDestinatario;
			
			if (properties.FapProperties.getBoolean("fap.platino.portafirma")) {
				if (resolucionFAP.solicitudFirmaPortafirmaOficioRemision.idSolicitante == null){
					Messages.error("El usuario no tiene asociado un identificador único en el ldap del gobierno.");
					play.Logger.error("El usuario no tiene asociado un identificador único en el ldap del gobierno.");
				} else
					dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.idSolicitante = resolucionFAP.solicitudFirmaPortafirmaOficioRemision.idSolicitante;
			}
			else {
				dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.idSolicitante = FapProperties.get("portafirma.usuario");
			}
		}

		if (dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.plazoMaximo != null) {
			DateTime today = new DateTime().withTimeAtStartOfDay();
			if (dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.plazoMaximo.isBefore(today)) {
				play.Logger.error("La fecha tope de firma no puede ser anterior a hoy.");
				CustomValidation.error("La fecha tope de firma no puede ser anterior a hoy.","resolucionFAP.solicitudFirmaPortafirmaOficioRemision.plazoMaximo", resolucionFAP.solicitudFirmaPortafirmaOficioRemision.plazoMaximo);
			}
			int dias = 0;
			try {
				dias = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getDiasLimiteFirma", dbResolucionFAP.id, true);
				//Si hay un plazo límite, se calcula si la fecha seleccionada está dentro del rango 
				if (dias != -1) {
					//Es requerido sólo si existe la property y tiene valor
					CustomValidation.required("resolucionFAP.solicitudFirmaPortafirmaOficioRemision.plazoMaximo", resolucionFAP.solicitudFirmaPortafirmaOficioRemision.plazoMaximo);
					dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.plazoMaximo = resolucionFAP.solicitudFirmaPortafirmaOficioRemision.plazoMaximo;
					
					DateTime diaLimite = new DateTime();
					diaLimite = diaLimite.plusDays(1);	// Por defecto, sólo se permite un día de plazo máximo
					if (diaLimite.isBefore(dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision.plazoMaximo)) {
						play.Logger.error("La fecha tope de firma no puede ser posterior a "+diaLimite+".");
						CustomValidation.error("La fecha tope de firma no puede ser posterior a "+diaLimite+".", "resolucionFAP.solicitudFirmaPortafirmaOficioRemision.plazoMaximo", resolucionFAP.solicitudFirmaPortafirmaOficioRemision.plazoMaximo);					
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
				play.Logger.error("No se ha podido calcular el límite de fecha para la firma."+e);
				CustomValidation.error("No se ha podido calcular el límite de fecha para la firma", "resolucionFAP.solicitudFirmaPortafirmaOficioRemision.plazoMaximo", resolucionFAP.solicitudFirmaPortafirmaOficioRemision.plazoMaximo);
			}
		}
		
		dbResolucionFAP.save();
		
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
			ResolucionUtils.actualizarSolicitudesFirmaPortafirmaAntiguasResolucion(dbResolucionFAP);
			try {
				PortafirmaFapService portafirmaService = InjectorConfig.getInjector().getInstance(PortafirmaFapService.class);
				if (portafirmaService.comprobarSiSolicitudFirmada(dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision)) {
					
					//Se agrega la firma de los oficios de remisión del portafirma del gobierno al AED de la ACIISI
					for (LineaResolucionFAP linea: dbResolucionFAP.lineasResolucion) {
						if (!linea.registro.fasesRegistro.firmada) {
							linea.registro.fasesRegistro.firmada = true;
							AedUtils.agregarFirma(linea.registro.oficial);
						}
					}
					dbResolucionFAP.estadoNotificacion = EstadoResolucionNotificacionEnum.oficiosRemisionFirmados.name();
					dbResolucionFAP.save();
					Messages.ok("Los oficios de remisión de la resolución asociados a la solicitud de firma se han firmado y finalizado correctamente.");
					play.Logger.info("Los oficios de remisión de la resolución [" + idResolucionFAP + "] asociados a la solicitud de firma se han firmado y finalizado correctamente.");
				} else {
					play.Logger.warn("Los oficios de remisión de la resolución ["+dbResolucionFAP.id+"] asociados a la solicitud de firma no han sido firmados y finalizados.");
					Messages.warning("Los oficios de remisión de la resolución asociados a la solicitud de firma no han sido firmados y finalizados.");
					String response = portafirmaService.obtenerEstadoFirma(dbResolucionFAP.solicitudFirmaPortafirmaOficioRemision);
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

			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Resolución: " + idResolucionFAP);
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

		RegistroService registroService = InjectorConfig.getInjector().getInstance(RegistroService.class);
		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		
		try {
			if (!registroService.isConfigured()){
				Messages.error("No se tiene acceso al servicio web de registro");
				play.Logger.info("No se tiene acceso al servicio web de registro, registrando oficios de remision");
			}
		} catch (Exception e) {
			Messages.error("Se ha producido un error de acceso al servicio web de registro");
			play.Logger.info("Se ha producido un error de acceso al servicio web de registro: " + e.getMessage());
		}
		
		try {
			if (!gestorDocumentalService.isConfigured()){
				Messages.error("No se tiene acceso al servicio de archivo electrónico de documentos (AED)");
				play.Logger.info("No se tiene acceso al servicio de archivo electrónico de documentos (AED)");
			}
		} catch (Exception e) {
			Messages.error("Se ha producido un error de acceso al servico de archivo electrónico de documentos (AED)");
			play.Logger.info("Se ha producido un error de acceso al servicio de archivo electrónico de documentos (AED): " + e.getMessage());
		}

		if (!Messages.hasErrors()) {
			EntityTransaction tx = JPA.em().getTransaction();
			try  {
				if (tx.isActive())
					tx.commit();
				tx.begin();
				ResolucionFAP dbResolucionFAP = PaginaNotificarResolucionController.getResolucionFAP(idResolucionFAP);
				for (LineaResolucionFAP lineaResolucionFAP: dbResolucionFAP.lineasResolucion) {
					SolicitudGenerica solicitud = SolicitudGenerica.findById(lineaResolucionFAP.solicitud.id);
					if (!lineaResolucionFAP.registro.fasesRegistro.registro) {
						play.Logger.info("Se inicia el proceso de registro de salida del oficio de remisión "+lineaResolucionFAP.registro.oficial.uri);
						// Se obtiene el justificante de registro de salida del oficio de remisión
						models.JustificanteRegistro justificanteSalida = registroService.registroDeSalida(solicitud.solicitante, lineaResolucionFAP.registro.oficial, solicitud.expedientePlatino, "Oficio de remisión");				
						lineaResolucionFAP.registro.informacionRegistro.setDataFromJustificante(justificanteSalida);
						Documento documento = lineaResolucionFAP.registro.justificante;
						documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSalida");
						documento.descripcion = "Justificante de registro de salida del oficio de remisión";
						documento.save();
						play.Logger.info("Creado el documento "+documento.descripcion+" en local, se procede a almacenar en el AED");
						InputStream is = justificanteSalida.getDocumento().contenido.getInputStream();
						gestorDocumentalService.saveDocumentoTemporal(documento, is, "JustificanteOficioRemision" + ".pdf");
						play.Logger.info(documento.descripcion+" almacenado en el AED");
						play.Logger.info(documento.uriPlatino);
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
						play.Logger.info("Se realizó el registro de salida del oficio de remisión correctamente");
					}
					if (!lineaResolucionFAP.registro.fasesRegistro.clasificarAed) {
						List<Documento> documentos = new ArrayList<Documento>();
						documentos.add(lineaResolucionFAP.registro.oficial);
						documentos.add(lineaResolucionFAP.registro.justificante);
						play.Logger.info("Se procede a clasificar los documentos oficio de remisión y justificante de registro de salida del oficio de remisión de la línea: "+lineaResolucionFAP.id);
						// Se clasifican los documentos
						gestorDocumentalService.clasificarDocumentos(solicitud, documentos, false);
						lineaResolucionFAP.registro.fasesRegistro.clasificarAed = true;
						play.Logger.info("Documentos clasificados");
						lineaResolucionFAP.save();
						solicitud.save();
						play.Logger.info("Se realizó la clasificación correctamente");
					}
				}
				tx.commit();
			} catch (Throwable e)   {
				 if ( tx != null && tx.isActive() ) tx.rollback();
				Messages.error("Se ha producido un error realizando el registro de los oficios de remisión");
				play.Logger.info("Se ha producido un error realizando el registro de los oficios de remisión: " + e.getMessage());
			}
			tx.begin();
		}
		
		if (!Messages.hasErrors()) {
			PaginaNotificarResolucionController.formRegistrarOficiosRemisionValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			Messages.ok("El registro de los oficios de remisión se ha realizado satisfactoriamente");
			log.info("Acción Editar de página: " + "gen/PaginaNotificarResolucion/PaginaNotificarResolucion.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Resolución: " + idResolucionFAP);
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


    public static List<ComboItem> gestorAFirmar() {
        return ComboUtils.gestorAFirmar();
    }

}
