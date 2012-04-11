package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import play.Logger;
import reports.Report;

import tags.ComboItem;
import verificacion.VerificacionUtils;

import messages.Messages;
import models.Documento;
import models.SolicitudGenerica;
import models.Tramite;
import models.Verificacion;
import models.VerificacionDocumento;
import controllers.gen.VerificacionControllerGen;
import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
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
	
	//Métodos en el controlador manual
	public static List<ComboItem> getTramitesCombo () {
		List<ComboItem> result = new ArrayList<ComboItem>();
		List<Tramite> lTrams = Tramite.findAll();
		for (Tramite t: lTrams) {
			result.add(new ComboItem(t.uri, t.nombre));
		}
		return result;
	}

	public static void iniciarVerificacion(Long idSolicitud, SolicitudGenerica solicitud) {
		checkAuthenticity();

		// Save code
		if (permisoiniciarVerificacion("update")
				|| permisoiniciarVerificacion("create")) {

			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);

			iniciarVerificacionValidateCopy(dbSolicitud, solicitud);

			if (!validation.hasErrors()) {
				dbSolicitud.verificacion.uriTramite = solicitud.verificacion.tramiteNombre.uri;
				iniciarVerificacionValidateRules(dbSolicitud, solicitud);
			}

			// Debemos recuperar todos los documentos aportados, del trámite seleccionado que no hayan sido verificados
			List<Documento> sinVerificar = solicitud.documentacion.getDocumentosNoVerificados();
			dbSolicitud.verificacion.estado = EstadosVerificacionEnum.verificandoTipos.name();
			if (!validation.hasErrors()) {
				
				dbSolicitud.save();
				Messages.ok("Verificación de Tipos de Documentos para el trámite iniciada correctamente.");
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
			
			dbSolicitud.verificacion.documentos = VerificacionUtils.getVerificacionDocumentosFromNewDocumentos(dbSolicitud.documentacion.documentos, dbSolicitud.verificacion.uriTramite, dbSolicitud.verificaciones);
			
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
	
	public static void finalizarVerificacion(Long idSolicitud) {
		checkAuthenticity();

		if (permisofinalizarVerificacion("update") || permisofinalizarVerificacion("create")) {
			if (!validation.hasErrors()) {
				// Obtengo los documentos que el usuario tiene actualmente aportados
				SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
				List<VerificacionDocumento> documentos = VerificacionUtils.existDocumentosNuevos(dbSolicitud.verificacion, dbSolicitud.verificaciones, dbSolicitud.documentacion.documentos);
				for(VerificacionDocumento d: documentos)
					System.out.println("Hola: "+d.descripcion);
				// Compruebo que no haya documentos no verificados, en caso contrario emito el error correspondiente
				if (VerificacionUtils.existsDocumentoNoVerificado(dbSolicitud.verificacion)){
					Messages.error("Compruebe que todos los documentos estan Verificados, existe algún documento en estado no Verificado");
				} 
				// Compruebo que no existen documentos nuevos aportados por el solicitante y que no esten incluidos en la verificacion actual
				if (!documentos.isEmpty()){
					Messages.error("Existen documentos nuevos aportados por el solicitante que no están incluidos en esta verificación. Pulse el botón 'Reiniciar la verificación' para incluirlos");
				}
				if (!Messages.hasErrors()){
					// Si hay algun documento en estado no valido o no presentado
					if (VerificacionUtils.documentosIncorrectos(dbSolicitud.verificacion)){
						dbSolicitud.verificacion.estado=EstadosVerificacionEnum.enRequerimiento.name();
						//Crear requerimiento
						// Si es la primera verificacion, se crea si o si
						// Si no, aparecen dos botones
						//           CREAR REQUERIMIENTO f.i)
						//           FINALIZAR VERIFICACION NEGATIVAMENTE f.ii)
						// Boton FINALIZAR requerimiento encargado de finalizar verificacion
					} else if (VerificacionUtils.documentosValidos(dbSolicitud.verificacion)){ // Si todos los documentos estan en estado valido o no procede, todo ha ido correcto, cambiamos el estado de la verificacion
						dbSolicitud.verificacion.estado=EstadosVerificacionEnum.verificacionPositiva.name();
						// Pasamos la verificacion Actual a la lista de historicos de la verficaciones y dejamos todo listo para que se pueda iniciar otra, si asi lo requiere el gestor o revisor
						dbSolicitud.verificaciones.add(dbSolicitud.verificacion);
						dbSolicitud.verificacion = new Verificacion();
						dbSolicitud.save();
					}
				}
			}
		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		finalizarVerificacionRender(idSolicitud);
	}
	
	public static void requerimientoBorrador(Long idSolicitud) {
		checkAuthenticity();

		if (permisorequerimientoBorrador("update") || permisorequerimientoBorrador("create")) {
			if (!validation.hasErrors()) {
				// Generar el borrador en PDF del requerimiento
				try {
					SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
					new Report("reports/borradorRequerimiento.html").header("reports/header.html").footer("reports/footer-borrador.html").renderResponse(solicitud);
				} catch (Exception e) {
					play.Logger.error("Error generando el borrador", e);
					Messages.error("Error generando el borrador");
				}
			}

		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		requerimientoBorradorRender(idSolicitud);

	}


}
