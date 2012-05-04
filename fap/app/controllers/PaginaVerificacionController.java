package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import tags.ComboItem;
import verificacion.VerificacionUtils;

import messages.Messages;
import models.Documento;
import models.SolicitudGenerica;
import models.Tramite;
import models.Verificacion;
import models.VerificacionDocumento;

import controllers.fap.VerificacionFapController;
import controllers.gen.PaginaVerificacionControllerGen;
import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;

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
			dbVerificacion.verificacionTiposDocumentos = VerificacionUtils.existDocumentosNuevos(dbVerificacion);
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
			
			dbSolicitud.verificacionEnCurso.documentos = VerificacionUtils.getVerificacionDocumentosFromNewDocumentos(VerificacionFapController.getNuevosDocumentosVerificar(dbSolicitud.verificacionEnCurso.id), dbSolicitud.verificacionEnCurso.uriTramite, dbSolicitud.verificaciones);

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
			List<Documento> documentosNuevos = VerificacionUtils.existDocumentosNuevos(dbSolicitud.verificacionEnCurso);
			// Compruebo que no existen documentos nuevos aportados por el solicitante y que no esten incluidos en la verificacion actual
			if (!documentosNuevos.isEmpty()){
				dbSolicitud.verificacionEnCurso.nuevosDocumentos.addAll(documentosNuevos);
				dbSolicitud.verificacionEnCurso.estado=EstadosVerificacionEnum.enVerificacionNuevosDoc.name();
				dbSolicitud.verificacionEnCurso.verificacionTiposDocumentos = VerificacionUtils.existDocumentosNuevos(dbSolicitud.verificacionEnCurso);
				dbSolicitud.save();
				Messages.info("Nuevos documentos aportados por el solicitante añadidos a la verificación actual. Verifique los tipos de estos documentos para proseguir con la verificación en curso.");
			}
			log.info("Acción sobre adjuntar Nuevos Documentos en página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción sobre adjuntar Nuevos Documentos en página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.nuevosDocumentosRender(idSolicitud, idVerificacion);
	}
	

}
