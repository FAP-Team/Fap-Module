package controllers;

import java.util.ArrayList;
import java.util.List;

import play.Logger;

import tags.ComboItem;
import verificacion.VerificacionUtils;

import messages.Messages;
import models.Documento;
import models.SolicitudGenerica;
import models.Verificacion;
import models.VerificacionDocumento;
import controllers.gen.VerificacionControllerGen;
import enumerado.fap.gen.EstadosVerificacionEnum;

public class VerificacionController extends VerificacionControllerGen {
	
	public static void reiniciarVerificacion(Long idSolicitud){
		checkAuthenticity();
		// Save code
		if (permisoreiniciarVerificacion("update") || permisoreiniciarVerificacion("create")) {
			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			if(!validation.hasErrors()){
				dbSolicitud.verificacion = new Verificacion();
				dbSolicitud.verificacion.estado = EstadosVerificacionEnum.iniciada.name();
				dbSolicitud.save();
				Messages.ok("Solicitud reiniciada correctamente");
			}
		}
		else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		reiniciarVerificacionRender(idSolicitud);
	}

	public static void iniciarVerificacion(Long idSolicitud, SolicitudGenerica solicitud) {
		checkAuthenticity();

		// Save code
		if (permisoiniciarVerificacion("update")
				|| permisoiniciarVerificacion("create")) {

			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);

			iniciarVerificacionValidateCopy(dbSolicitud, solicitud);

			if (!validation.hasErrors()) {
				iniciarVerificacionValidateRules(dbSolicitud, solicitud);
			}

			// Debemos recuperar todos los documentos aportados, del trámite seleccionado que no hayan sido verificados
			List<Documento> sinVerificar = solicitud.documentacion.getDocumentosNoVerificados();
			dbSolicitud.verificacion.estado = EstadosVerificacionEnum.verificandoTipos.name();
			if (!validation.hasErrors()) {
				
				dbSolicitud.save();
				Messages.ok("Varificación de Tipos de Documentos para el trámite iniciada correctamente.");
			}
		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		iniciarVerificacionRender(idSolicitud);

	}
	
	/**
	 * Sobreescribimos el método para mostrar sólo los documentos que no han sido verificados aún
	 */
	public static void tablaverificacionTipos(Long idSolicitud, Long idEntidad) {
		Long id = idSolicitud != null ? idSolicitud : idEntidad;
		java.util.List<Documento> rows = Documento
				.find("select documento from SolicitudGenerica solicitud join solicitud.documentacion.documentos documento where solicitud.id=? and (documento.verificado is null or documento.verificado = false)",id).fetch();

		List<Documento> rowsFiltered = rows; // Tabla sin permisos, no filtra

		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered);
		renderJSON(response.toJSON("fechaSubida", "descripcion", "verificado", "urlDescarga", "id"));
	}
	
	/**
	 * Finaliza la verificación de tipos
	 * 
	 * TODO:
	 * 		1. Pasa los documentos a la verificación, al tipo VerificacionDocumento
	 * 		2. Pone la verificación en el estado "En Verificacion"
	 * 
	 * @param idSolicitud
	 */
	public static void verificaTipos(Long idSolicitud) {
		checkAuthenticity();
		if (permisoverificaTipos("update") || permisoverificaTipos("create")) {
			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			
			dbSolicitud.verificacion.documentos = VerificacionUtils.getVerificacionDocumentosFromNewDocumentos(dbSolicitud.documentacion.documentos, dbSolicitud.verificacion.tramiteNombre.uri, dbSolicitud.verificaciones);
			
			if (!validation.hasErrors()) {
				
				dbSolicitud.verificacion.estado = EstadosVerificacionEnum.enVerificacion.name();
				dbSolicitud.save();
				Messages.ok("Finaliza la verificación de tipos");
			}
		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		verificaTiposRender(idSolicitud);
	}


}
