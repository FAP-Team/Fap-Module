package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import reports.Report;
import services.FirmaService;
import services.NotificacionService;
import services.RegistroService;

import org.joda.time.DateTime;

import platino.FirmaUtils;
import play.mvc.Util;
import properties.FapProperties;

import tags.ComboItem;
import utils.CalcularFirmantes;
import validation.CustomValidation;
import verificacion.VerificacionUtils;

import messages.Messages;
import models.Agente;
import models.Documento;
import models.DocumentoNotificacion;
import models.Notificacion;
import models.SolicitudGenerica;
import models.TipoDocumento;
import models.Tramite;
import models.Verificacion;
import models.VerificacionDocumento;

import controllers.fap.AgenteController;
import controllers.fap.VerificacionFapController;
import controllers.gen.PaginaVerificacionControllerGen;
import emails.Mails;
import enumerado.fap.gen.EstadoNotificacionEnum;
import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;
import es.gobcan.eadmon.verificacion.ws.dominio.DocumentoVerificacion;
import es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionException;

public class PaginaVerificacionController extends PaginaVerificacionControllerGen {
	
    @Inject
    static FirmaService firmaService;

    @Inject
    static RegistroService registroService;
    
    @Inject
    static NotificacionService notificacionService;
	
	public static void index(String accion, Long idSolicitud, Long idVerificacion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("gen/PaginaVerificacion/PaginaVerificacion.html");
		}
		
		SolicitudGenerica solicitud = PaginaVerificacionController.getSolicitudGenerica(idSolicitud);

		Verificacion verificacion = null;
		if ("crear".equals(accion))
			verificacion = PaginaVerificacionController.getVerificacion();
		else if (!"borrado".equals(accion))
			verificacion = PaginaVerificacionController.getVerificacion(idSolicitud, idVerificacion);

        if ((solicitud != null) && (solicitud.verificacionEnCurso != null) && (solicitud.verificacionEnCurso.estado != null)){
        	log.info("Visitando página: " + "gen/PaginaVerificacion/PaginaVerificacion.html");
        	renderTemplate("gen/PaginaVerificacion/PaginaVerificacion.html", accion, idSolicitud, idVerificacion, solicitud, verificacion);
        } else
        	redirect("AccesoVerificacionesController.index", accion, idSolicitud);
		
	}
	
	//Métodos en el controlador manual
	public static List<ComboItem> getTramitesCombo () {
		List<ComboItem> result = new ArrayList<ComboItem>();
		List<Tramite> lTrams = Tramite.findAll();
		for (Tramite t: lTrams) {
			result.add(new ComboItem(t.uri, t.nombre));
		}
		return result;
	}
	
	public static void iniciarVerificacion(Long idSolicitud, Long idVerificacion, Verificacion verificacion, String botonIniciarVerificacion) {
		checkAuthenticity();
		if (!permisoIniciarVerificacion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		Verificacion dbVerificacion = PaginaVerificacionController.getVerificacion(idSolicitud, idVerificacion);

		PaginaVerificacionController.iniciarVerificacionBindReferences(verificacion);

		if (!Messages.hasErrors()) {
			dbVerificacion.uriTramite = verificacion.tramiteNombre.uri;
			PaginaVerificacionController.iniciarVerificacionValidateCopy("editar", dbVerificacion, verificacion);
		}

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.iniciarVerificacionValidateRules(dbVerificacion, verificacion);
		}
		if (!Messages.hasErrors()) {
			dbVerificacion.estado = EstadosVerificacionEnum.verificandoTipos.name();
			dbVerificacion.fechaUltimaActualizacion = new DateTime();
			dbVerificacion.verificacionTiposDocumentos = VerificacionUtils.existDocumentosNuevos(dbVerificacion, idSolicitud);
			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			dbSolicitud.estado=EstadosSolicitudEnum.enVerificacion.name();
			dbVerificacion.save();
			dbSolicitud.save();
			log.info("Acción sobre iniciar verificacion de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción sobre iniciar verificacion de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.iniciarVerificacionRender(idSolicitud, idVerificacion);
	}
	
	public static void tablaverificacionTipos(Long idSolicitud) {

		java.util.List<Documento> rows = Documento.find("select documento from SolicitudGenerica solicitud join solicitud.verificacionEnCurso.verificacionTiposDocumentos documento where solicitud.id=? and (documento.verificado is null or documento.verificado = false)",idSolicitud).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Documento> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("fechaSubida", "fechaRegistro", "descripcion", "verificado", "urlDescarga", "id"));
	}
	
	public static void verificaTipos(Long idSolicitud, Long idVerificacion, String finalizaVerificarTipos) {
		checkAuthenticity();
		if (!permisoVerificaTipos("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.verificaTiposValidateRules();
		}
		if (!Messages.hasErrors()) {
			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			
			try {
				dbSolicitud.verificacionEnCurso.documentos = VerificacionUtils.getVerificacionDocumentosFromNewDocumentos((List<Documento>)VerificacionFapController.invoke("getNuevosDocumentosVerificar", dbSolicitud.verificacionEnCurso.id, idSolicitud), dbSolicitud.verificacionEnCurso.uriTramite, dbSolicitud.verificaciones, idSolicitud);
			} catch (Throwable e) {
				play.Logger.error("Error recuperando los documentos nuevos a verificar", e.getMessage());
			}

			dbSolicitud.verificacionEnCurso.estado = EstadosVerificacionEnum.enVerificacion.name();
			dbSolicitud.verificacionEnCurso.nuevosDocumentos.clear();
			dbSolicitud.verificacionEnCurso.verificacionTiposDocumentos.clear();
			dbSolicitud.verificacionEnCurso.fechaUltimaActualizacion = new DateTime();
			dbSolicitud.save();
			Messages.ok("Finaliza la verificación de tipos");
			log.info("Acción verificacion de tipos de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción verificacion de tipos de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.verificaTiposRender(idSolicitud, idVerificacion);
	}
	
	public static void verificaTiposNuevosDoc(Long idSolicitud, Long idVerificacion, String finalizaVerificarTipos) {
		checkAuthenticity();
		if (!permisoVerificaTiposNuevosDoc("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		if (!Messages.hasErrors()) {
			PaginaVerificacionController.verificaTiposNuevosDocValidateRules();
		}
		if (!Messages.hasErrors()) {
			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			for (Documento doc: dbSolicitud.verificacionEnCurso.nuevosDocumentos){
				VerificacionDocumento vDoc= new VerificacionDocumento(doc);
				TipoDocumento tipo = TipoDocumento.find("select tipo from TipoDocumento tipo where tipo.tramitePertenece=? and tipo.uri=?", dbSolicitud.verificacionEnCurso.uriTramite, doc.tipo).first();
				vDoc.identificadorMultiple = tipo.cardinalidad;
				vDoc.existe = true;
				vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noVerificado.name();
				vDoc.save();
				dbSolicitud.verificacionEnCurso.documentos.add(vDoc);
			}
			dbSolicitud.verificacionEnCurso.estado = EstadosVerificacionEnum.enVerificacion.name();
			dbSolicitud.verificacionEnCurso.nuevosDocumentos.clear();
			dbSolicitud.verificacionEnCurso.verificacionTiposDocumentos.clear();
			dbSolicitud.verificacionEnCurso.fechaUltimaActualizacion = new DateTime();
			dbSolicitud.save();
			log.info("Acción sobre verificacion de tipos de nuevos documentos de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción sobre verificacion de tipos de nuevos documentos de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.verificaTiposNuevosDocRender(idSolicitud, idVerificacion);
	}
	
	public static void nuevosDocumentos(Long idSolicitud, Long idVerificacion, String adjuntarNuevosDocumentos) {
		checkAuthenticity();
		if (!permisoNuevosDocumentos("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		if (!Messages.hasErrors()) {
			PaginaVerificacionController.nuevosDocumentosValidateRules();
		}
		if (!Messages.hasErrors()) {
			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			List<Documento> documentosNuevos = VerificacionUtils.existDocumentosNuevos(dbSolicitud.verificacionEnCurso, idSolicitud);
			// Compruebo que no existen documentos nuevos aportados por el solicitante y que no esten incluidos en la verificacion actual
			if (!documentosNuevos.isEmpty()){
				dbSolicitud.verificacionEnCurso.nuevosDocumentos.addAll(documentosNuevos);
				dbSolicitud.verificacionEnCurso.estado=EstadosVerificacionEnum.enVerificacionNuevosDoc.name();
				dbSolicitud.verificacionEnCurso.verificacionTiposDocumentos = VerificacionUtils.existDocumentosNuevos(dbSolicitud.verificacionEnCurso, idSolicitud);
				dbSolicitud.save();
				Messages.info("Nuevos documentos aportados por el solicitante añadidos a la verificación actual. Verifique los tipos de estos documentos para proseguir con la verificación en curso.");
			}
			log.info("Acción sobre adjuntar Nuevos Documentos en página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción sobre adjuntar Nuevos Documentos en página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.nuevosDocumentosRender(idSolicitud, idVerificacion);
	}
	
	public static void gnuevoRequerimientoBorradorPreliminar(Long idSolicitud, Long idVerificacion, String obtenerBorradorPreliminar) {
		checkAuthenticity();
		if (!permisoGnuevoRequerimientoBorradorPreliminar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		if (!Messages.hasErrors()) {
			PaginaVerificacionController.gnuevoRequerimientoBorradorPreliminarValidateRules();
		}
		if (!Messages.hasErrors()) {
			try {
				SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(idSolicitud);
				new Report("reports/borradorRequerimiento.html").header("reports/header.html").footer("reports/footer-borrador.html").renderResponse(dbSolicitud);
			} catch (Exception e) {
				play.Logger.error("Error generando el borrador", e.getMessage());
				Messages.error("Error generando el borrador");
			} catch (Throwable e) {
				play.Logger.error("Error generando el borrador", e.getMessage());
				Messages.error("Error generando el borrador");
			}
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.gnuevoRequerimientoBorradorPreliminarRender(idSolicitud, idVerificacion);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void finalizarVerificacion(Long idSolicitud, Long idVerificacion, String btnFinalizarVerificacion) {
		checkAuthenticity();
		if (!permisoFinalizarVerificacion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			// Comprobamos que esten todos los documentos verificados
			if (!VerificacionUtils.existsDocumentoNoVerificado(dbSolicitud.verificacionEnCurso)){
				// Si hay cosas que requerir, la verificación tiene causas subsanables
				if (((dbSolicitud.verificacionEnCurso.requerimiento.motivo != null) && (!dbSolicitud.verificacionEnCurso.requerimiento.motivo.trim().isEmpty())) || (VerificacionUtils.documentosIncorrectos(dbSolicitud.verificacionEnCurso))){
					log.info("Hay que requerir y notificar, existe un motivo general de requerimiento o documentos en estado noValidos o noPresentados (Solicitud "+dbSolicitud.id+")");
								
					// Firma requerimiento por el Gestor
					// Crear notificación
					// Enviar Notificacion
		
					// Actualizamos los datos de la verificacion para verificaciones posteriores, en este caso el estado.
					dbSolicitud.verificacionEnCurso.estado = EstadosVerificacionEnum.enRequerimiento.name();
					Messages.ok("Se deberá realizar un Requerimiento");
				} else { // Si la verificación ha ido correcta, no hay ninguna causa subsanable
					log.info("La verificación se ha podido finalizar con éxito, todo es correcto");
					Messages.ok("La verificación no tiene ningun requerimiento, finalizada correctamente y con éxito");
					
					// Ponemos todos los documentos de la verificacion como verificados, para que no se incluyan en sucesivas verificaciones
					VerificacionUtils.setVerificadoDocumentos(dbSolicitud.verificacionEnCurso.documentos, dbSolicitud.documentacion.documentos);
					// Actualizamos los datos de la verificacion para verificaciones posteriores, en este caso el estado.
					dbSolicitud.verificacionEnCurso.estado = EstadosVerificacionEnum.verificacionPositiva.name();
					
					// Actualizamos los datos de la verificacion para verificaciones posteriores. Copiamos la verificacionActual a las verificaciones Anteriores para poder empezar una nueva verificación.
					dbSolicitud.verificaciones.add(dbSolicitud.verificacionEnCurso);
					dbSolicitud.estado = EstadosSolicitudEnum.verificado.name();
					dbSolicitud.save();
					
					//redirect("PaginaVerificacionController.index", "editar", idSolicitud, idVerificacion);
				}

				dbSolicitud.save();
			} else {
				Messages.error("Existen documentos aún por verificar, compruebe y verifiquelos para finalizar la Verificación Actual");
			}
		}

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.finalizarVerificacionValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.finalizarVerificacionRender(idSolicitud, idVerificacion);
	}
	
	/**
	 * Lista los gestores que pueden firmar el requerimiento
	 * @return
	 */
	public static List<ComboItem> gestorAFirmar() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		List<Agente> listaAgentes = Agente.findAll(); //Agente.find("select agente from Agente agente where agente.roles").fetch();
		for (Agente ag : listaAgentes) {
			result.add(new ComboItem(ag.username, ag.username +" - "+ag.name));
		}
		return result;
	}

	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void gRequerirFirmaRequerimiento(Long idSolicitud, Long idVerificacion, SolicitudGenerica solicitud, String requerirFirma) {
		checkAuthenticity();
		if (!permisoGRequerirFirmaRequerimiento("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = PaginaVerificacionController.getSolicitudGenerica(idSolicitud);
		PaginaVerificacionController.gRequerirFirmaRequerimientoBindReferences(solicitud);

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.gRequerirFirmaRequerimientoValidateCopy("editar", dbSolicitud, solicitud);
		}

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.gRequerirFirmaRequerimientoValidateRules(dbSolicitud, solicitud);
			Messages.ok("Se estableció correctamente el firmante del Requerimiento");
			dbSolicitud.verificacionEnCurso.estado = EstadosVerificacionEnum.enRequerimientoFirmaSolicitada.name();
			
			// Se debe enviar el mail de "solicitarFirmaRequerimiento"
			String mailRevisor = null;
			String mailGestor = null;
			try {
				Agente revisor = AgenteController.getAgente();
				mailRevisor = revisor.email;
				mailGestor = ((Agente) Agente.find("select agente from Agente agente where agente.username=?", solicitud.verificacionEnCurso.requerimiento.firmante).first()).username;
				
				Mails.enviar("solicitarFirmaRequerimiento", solicitud, mailGestor, mailRevisor);
			} catch (Exception e) {
				play.Logger.error("No se pudo enviar el mail solicitarFirmaRequerimiento a los mails: "+mailGestor+", "+mailRevisor);
			}
		}
		if (!Messages.hasErrors()) {
			dbSolicitud.save();
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.gRequerirFirmaRequerimientoRender(idSolicitud, idVerificacion);
	}
	
	@Util
	public static void gRequerirFirmaRequerimientoValidateCopy(String accion, SolicitudGenerica dbSolicitud, SolicitudGenerica solicitud) {
		CustomValidation.clearValidadas();
		if (secure.checkGrafico("requerimientoRequerirFirma", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("solicitud.verificacionEnCurso.requerimiento", solicitud.verificacionEnCurso.requerimiento);
			CustomValidation.valid("solicitud.verificacionEnCurso", solicitud.verificacionEnCurso);
			CustomValidation.valid("solicitud", solicitud);
			CustomValidation.validValueFromTable("solicitud.verificacionEnCurso.requerimiento.firmante", solicitud.verificacionEnCurso.requerimiento.firmante);
			dbSolicitud.verificacionEnCurso.requerimiento.firmante = solicitud.verificacionEnCurso.requerimiento.firmante;
			
			dbSolicitud.verificacionEnCurso.requerimiento.registro.firmantes.todos = CalcularFirmantes.getGestorComoFirmante(solicitud.verificacionEnCurso.requerimiento.firmante);
			dbSolicitud.verificacionEnCurso.requerimiento.registro.firmantes.save();
			dbSolicitud.save();
		}
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void gFirmarRequerimiento(Long idSolicitud, Long idVerificacion, String firma, String firmaRequerimiento) {
		checkAuthenticity();
		if (!permisoGFirmarRequerimiento("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = PaginaVerificacionController.getSolicitudGenerica(idSolicitud);

		if (firmaRequerimiento != null) {
			PaginaVerificacionController.firmaRequerimientoGFirmarRequerimiento(idSolicitud, idVerificacion, firma);
			PaginaVerificacionController.gFirmarRequerimientoRender(idSolicitud, idVerificacion);
		}
		
		// Si ya fue firmada y no ha sido registrada
		if (dbSolicitud.verificacionEnCurso.requerimiento.registro.fasesRegistro.firmada
				&& !dbSolicitud.verificacionEnCurso.requerimiento.registro.fasesRegistro.registro) {
			try {
				registroService.registroDeSalida(dbSolicitud.solicitante, dbSolicitud.verificacionEnCurso.requerimiento.oficial, dbSolicitud.expedientePlatino, "Requerimiento");
				play.Logger.info("Se ha registrado de Salida el documento del requerimiento de la solicitud "+dbSolicitud.id);
				Messages.ok("Se ha registrado el Requerimiento correctamente.");
				dbSolicitud.verificacionEnCurso.requerimiento.registro.fasesRegistro.registro = true;
				dbSolicitud.save();
			} catch (Exception e) {
				Messages.error("No se ha podido registrar el requerimiento de la solicitud "+dbSolicitud.id);
				play.Logger.error("No se ha podido registrar el requerimiento de la solicitud "+dbSolicitud.id+": "+e.getMessage());
			}
		}
		
		// Si ya fue registrada
		if (dbSolicitud.verificacionEnCurso.requerimiento.registro.fasesRegistro.registro) {
			Notificacion notificacion = dbSolicitud.verificacionEnCurso.requerimiento.notificacion;
			if (notificacion.estado == null || notificacion.estado.isEmpty()) {
				//La notificación no ha sido creada
				DocumentoNotificacion docANotificar = new DocumentoNotificacion(dbSolicitud.verificacionEnCurso.requerimiento.oficial.uri);
				notificacion.documentosANotificar.add(docANotificar);
				notificacion.interesados.addAll(dbSolicitud.solicitante.getInteresados());
				notificacion.descripcion = "Notificación";
				notificacion.plazoAcceso = FapProperties.getInt("fap.notificacion.plazoacceso");
				notificacion.plazoRespuesta = FapProperties.getInt("fap.notificacion.plazorespuesta");
				notificacion.frecuenciaRecordatorioAcceso = FapProperties.getInt("fap.notificacion.frecuenciarecordatorioacceso");
				notificacion.frecuenciaRecordatorioRespuesta = FapProperties.getInt("fap.notificacion.frecuenciarecordatoriorespuesta");
				
				dbSolicitud.save();
			}
			if (notificacion.estado.equals(EstadoNotificacionEnum.creada.name())) {
				// TODO: Está en estado creada, debo notificarla
				try {
					notificacionService.enviarNotificaciones(notificacion, AgenteController.getAgente());
					play.Logger.info("Se ha enviado correctamente la notificación "+notificacion.id);
					// Los demás cambios en la notificación los hace el Servicio
					notificacion.save();
				} catch (NotificacionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					play.Logger.error("No se ha podido enviar la notificación "+notificacion.id+": "+e.getMessage());
				}
			}
		}

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.gFirmarRequerimientoValidateRules(firma);
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.gFirmarRequerimientoRender(idSolicitud, idVerificacion);
	}
	
	@Util
	public static void firmaRequerimientoGFirmarRequerimiento(Long idSolicitud, Long idVerificacion, String firma) {
		SolicitudGenerica solicitud = PaginaVerificacionController.getSolicitudGenerica(idSolicitud);

		if (solicitud.verificacionEnCurso.requerimiento.registro.firmantes.todos == null || solicitud.verificacionEnCurso.requerimiento.registro.firmantes.todos.size() == 0) {
			solicitud.verificacionEnCurso.requerimiento.registro.firmantes.todos = CalcularFirmantes.getGestoresComoFirmantes();
			solicitud.verificacionEnCurso.requerimiento.registro.firmantes.save();
		}
		FirmaUtils.firmar(solicitud.verificacionEnCurso.requerimiento.oficial, solicitud.verificacionEnCurso.requerimiento.registro.firmantes.todos, firma, solicitud.verificacionEnCurso.requerimiento.firmante);
		if (!Messages.hasErrors()) {
			Messages.ok("El requerimiento se ha firmado correctamente");
			solicitud.verificacionEnCurso.estado = EstadosVerificacionEnum.enRequerido.name();
			solicitud.verificacionEnCurso.requerimiento.registro.fasesRegistro.firmada = true;
			solicitud.save();
		}
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void gPonerADisposicion(Long idSolicitud, Long idVerificacion, String ponerADisposicion) {
		checkAuthenticity();
		if (!permisoGPonerADisposicion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			Agente gestor = AgenteController.getAgente();
			SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);

			
			try {
				//enviarNotificaciones(solicitud.verificacionEnCurso.requerimiento.notificacion, gestor);
			} catch (Exception e) {
				Messages.error("No se ha podido enviar la notificación");
			}
		}

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.gPonerADisposicionValidateRules();
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.gPonerADisposicionRender(idSolicitud, idVerificacion);
	}
	
}
