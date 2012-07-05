package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import reports.Report;
import org.joda.time.DateTime;

import play.mvc.Util;

import tags.ComboItem;
import verificacion.VerificacionUtils;

import messages.Messages;
import models.Documento;
import models.SolicitudGenerica;
import models.TipoDocumento;
import models.Tramite;
import models.Verificacion;
import models.VerificacionDocumento;

import controllers.fap.VerificacionFapController;
import controllers.gen.PaginaVerificacionControllerGen;
import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;
import es.gobcan.eadmon.verificacion.ws.dominio.DocumentoVerificacion;

public class PaginaVerificacionController extends PaginaVerificacionControllerGen {
	
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
					log.info("Hay que requerir y notificar, existe un motivo general de requerimiento o documentos en estado noValidos o noPresentados");
								
					// Firma requerimiento por el Gestor
					// Crear notificación
					// Enviar Notificacion
		
					// Actualizamos los datos de la verificacion para verificaciones posteriores, en este caso el estado.
					dbSolicitud.verificacionEnCurso.estado = EstadosVerificacionEnum.verificacionNegativa.name();
					Messages.ok("Se ha creado una notificación, verificación finalizada y notificación enviada");
				} else { // Si la verificación ha ido correcta, no hay ninguna causa subsanable
					log.info("La verificación se ha podido finalizar con éxito, todo es correcto");
					Messages.ok("La verificación no tiene ningun requerimiento, finalizada correctamente y con éxito");
					// Actualizamos los datos de la verificacion para verificaciones posteriores, en este caso el estado.
					dbSolicitud.verificacionEnCurso.estado = EstadosVerificacionEnum.verificacionPositiva.name();
				}
				// Ponemos todos los documentos de la verificacion como verificados, para que no se incluyan en sucesivas verificaciones
				for (VerificacionDocumento docV: dbSolicitud.verificacionEnCurso.documentos){
					for (Documento docu: dbSolicitud.documentacion.documentos){
						if ((docu.uri != null) && (docV.uriDocumento != null) && (docu.uri.equals(docV.uriDocumento))){
							docu.verificado=true;
							break;
						}
					}
				}
				// Actualizamos los datos de la verificacion para verificaciones posteriores. Copiamos la verificacionActual a las verificaciones Anteriores para poder empezar una nueva verificación.
				dbSolicitud.verificaciones.add(dbSolicitud.verificacionEnCurso);
				dbSolicitud.estado = EstadosSolicitudEnum.verificado.name();
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

}
