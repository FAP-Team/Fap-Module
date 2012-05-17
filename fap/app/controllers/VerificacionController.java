package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import aed.AedClient;

import play.Logger;
import properties.FapProperties;
import reports.Report;
import services.FirmaService;
import services.RegistroException;
import services.RegistroService;

import tags.ComboItem;

import verificacion.VerificacionUtils;

import messages.Messages;
import models.Agente;
import models.Documento;
import models.Firmante;
import models.SolicitudGenerica;
import models.TipoDocumento;
import models.Tramite;
import models.Verificacion;
import models.VerificacionDocumento;
import controllers.fap.AgenteController;
import controllers.fap.VerificacionFapController;
import controllers.gen.VerificacionControllerGen;
import emails.Mails;
import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
import enumerado.fap.gen.EstadosRequerimientoEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;

public class VerificacionController extends VerificacionControllerGen {
	
	public static void index(Long idSolicitud){
		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
        if (solicitud.verificacion.estado != null){
        	renderTemplate( "gen/Verificacion/Verificacion.html" , solicitud);
        } else
        	redirect("AccesoVerificacionesController.index", idSolicitud);
	}
	
//	public static void reiniciarVerificacion(Long idSolicitud){
//		checkAuthenticity();
//		// Save code
//		if (permisoreiniciarVerificacion("update") || permisoreiniciarVerificacion("create")) {
//			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
//			if(!validation.hasErrors()){
//				dbSolicitud.verificacion.verificacionTiposDocumentos.clear();
//				dbSolicitud.verificacion = new Verificacion();
//				dbSolicitud.verificacion.estado = EstadosVerificacionEnum.iniciada.name();
//				dbSolicitud.verificacion.fechaUltimaActualizacion = new DateTime();
//				dbSolicitud.save();
//				Messages.ok("Solicitud reiniciada correctamente");
//			}
//		}
//		else {
//			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
//		}
//		reiniciarVerificacionRender(idSolicitud);
//	}
	
	//Métodos en el controlador manual
	public static List<ComboItem> getTramitesCombo () {
		List<ComboItem> result = new ArrayList<ComboItem>();
		List<Tramite> lTrams = Tramite.findAll();
		if (FapProperties.getBoolean("fap.verificacion.soloSolicitud")) {
			String tramiteSolicitud = FapProperties.get("fap.aed.procedimientos.tramite.uri");
			if (tramiteSolicitud != null && !tramiteSolicitud.trim().equals("")) {
				result.add(new ComboItem(tramiteSolicitud, "Solicitud"));
				return result;
			}
		}
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

			dbSolicitud.verificacion.estado = EstadosVerificacionEnum.verificandoTipos.name();
			dbSolicitud.verificacion.fechaUltimaActualizacion = new DateTime();
			dbSolicitud.verificacion.verificacionTiposDocumentos = VerificacionUtils.existDocumentosNuevos(dbSolicitud.verificacion, idSolicitud);
			dbSolicitud.estado=EstadosSolicitudEnum.enVerificacion.name();
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
		SolicitudGenerica s = getSolicitudGenerica(idSolicitud);
		java.util.List<Documento> rows = Documento
				.find("select documento from SolicitudGenerica solicitud join solicitud.verificacion.verificacionTiposDocumentos documento where solicitud.id=? and (documento.verificado is null or documento.verificado = false)",id).fetch();

		List<Documento> rowsFiltered = rows; // Tabla sin permisos, no filtra

		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered);
		renderJSON(response.toJSON("fechaSubida", "fechaRegistro", "descripcion", "verificado", "urlDescarga", "id"));
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

			try {
				dbSolicitud.verificacion.documentos = VerificacionUtils.getVerificacionDocumentosFromNewDocumentos((List<Documento>)VerificacionFapController.invoke("getNuevosDocumentosVerificar", dbSolicitud.verificacion.id, idSolicitud), dbSolicitud.verificacion.uriTramite, dbSolicitud.verificaciones, idSolicitud);
			} catch (Throwable e) {
				play.Logger.error("Error recuperando los documentos nuevos a verificar", e.getMessage());
			}
			
			if (!validation.hasErrors()) {
				
				dbSolicitud.verificacion.estado = EstadosVerificacionEnum.enVerificacion.name();
				dbSolicitud.verificacion.nuevosDocumentos.clear();
				dbSolicitud.verificacion.verificacionTiposDocumentos.clear();
				dbSolicitud.verificacion.fechaUltimaActualizacion = new DateTime();
				dbSolicitud.save();
				Messages.ok("Finaliza la verificación de tipos");
			}
		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		verificaTiposRender(idSolicitud);
	}
	
	public static void verificaTiposNuevosDoc(Long idSolicitud) {
		checkAuthenticity();
		if (permisoverificaTipos("update") || permisoverificaTipos("create")) {
			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			
			for (Documento doc: dbSolicitud.verificacion.nuevosDocumentos){
				VerificacionDocumento vDoc= new VerificacionDocumento(doc);
				vDoc.existe = true;
				TipoDocumento tipo = TipoDocumento.find("select tipo from TipoDocumento tipo where tipo.tramitePertenece=? and tipo.uri=?", dbSolicitud.verificacion.uriTramite, doc.tipo).first();
				vDoc.identificadorMultiple = tipo.cardinalidad;
				vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noVerificado.name();
				vDoc.save();
				dbSolicitud.verificacion.documentos.add(vDoc);
			}
			
			if (!validation.hasErrors()) {
				dbSolicitud.verificacion.estado = EstadosVerificacionEnum.enVerificacion.name();
				dbSolicitud.verificacion.nuevosDocumentos.clear();
				dbSolicitud.verificacion.verificacionTiposDocumentos.clear();
				dbSolicitud.verificacion.fechaUltimaActualizacion = new DateTime();
				dbSolicitud.save();
				Messages.ok("Finaliza la verificación de tipos");
			}
		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		verificaTiposNuevosDocRender(idSolicitud);

	}
	
//	public static void finalizarVerificacion(Long idSolicitud) {
//		checkAuthenticity();
//
//		if (permisofinalizarVerificacion("update") || permisofinalizarVerificacion("create")) {
//			if (!validation.hasErrors()) {
//				// Obtengo los documentos que el usuario tiene actualmente aportados
//				SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
//				List<Documento> documentosNuevos = VerificacionUtils.existDocumentosNuevos(dbSolicitud.verificacion, dbSolicitud.verificaciones, dbSolicitud.documentacion.documentos);
//				// Compruebo que no haya documentos no verificados, en caso contrario emito el error correspondiente
//				if (VerificacionUtils.existsDocumentoNoVerificado(dbSolicitud.verificacion)){
//					Messages.error("Compruebe que todos los documentos estan Verificados, existe algún documento en estado no Verificado");
//				} 
//				// Compruebo que no existen documentos nuevos aportados por el solicitante y que no esten incluidos en la verificacion actual
//				if (!documentosNuevos.isEmpty()){
//					dbSolicitud.verificacion.nuevosDocumentos.addAll(documentosNuevos);
//					dbSolicitud.verificacion.estado=EstadosVerificacionEnum.enVerificacionNuevosDoc.name();
//					dbSolicitud.save();
//					Messages.error("Existen documentos nuevos aportados por el solicitante que no están incluidos en esta verificación, que han sido aportados durante el propio proceso de verificación. Deberá verificarlos para continuar con la verificación");
//				}
//				if (!Messages.hasErrors()){
//					// Si hay algun documento en estado no valido o no presentado
//					if (VerificacionUtils.documentosIncorrectos(dbSolicitud.verificacion)){
//						dbSolicitud.verificacion.estado=EstadosVerificacionEnum.enRequerimiento.name();
//						dbSolicitud.verificacion.fechaUltimaActualizacion = new DateTime();
//						dbSolicitud.save();
//						//Crear requerimiento
//						// Si es la primera verificacion, se crea si o si
//						// Si no, aparecen dos botones
//						//           CREAR REQUERIMIENTO f.i)
//						//           FINALIZAR VERIFICACION NEGATIVAMENTE f.ii)
//						// Boton FINALIZAR requerimiento encargado de finalizar verificacion
//						// --------------------------------------------------
//						try {
//							// Generar Borrador 
//							File borrador;
//							borrador = new Report("reports/borradorRequerimiento.html").header("reports/header.html").footer("reports/footer-borrador.html").renderTmpFile(dbSolicitud);
//							dbSolicitud.verificacion.requerimiento.borrador = new Documento();
//							dbSolicitud.verificacion.requerimiento.borrador.tipo = FapProperties.get("fap.aed.tiposdocumentos.solicitud");
//							AedClient.saveDocumentoTemporal(dbSolicitud.verificacion.requerimiento.borrador, borrador);
//							// Generar el Documento Oficial
//							File oficial;
//							oficial = new Report("reports/borradorRequerimiento.html").header("reports/header.html").renderTmpFile(dbSolicitud);
//							dbSolicitud.verificacion.requerimiento.oficial = new Documento();
//							dbSolicitud.verificacion.requerimiento.oficial.tipo = FapProperties.get("fap.aed.tiposdocumentos.solicitud");
//							AedClient.saveDocumentoTemporal(dbSolicitud.verificacion.requerimiento.oficial, oficial);
//						} catch (Exception e) {
//							play.Logger.error("Error generando el borrador del requerimiento", e);
//							Messages.error("Error generando el borrador o el documento oficial del requerimiento.");
//						}
//						// ------------------------------------------------------------------
//					} else if (VerificacionUtils.documentosValidos(dbSolicitud.verificacion)){ // Si todos los documentos estan en estado valido o no procede, todo ha ido correcto, cambiamos el estado de la verificacion
//						dbSolicitud.verificacion.estado=EstadosVerificacionEnum.verificacionPositiva.name();
//						dbSolicitud.verificacion.fechaUltimaActualizacion = new DateTime();
//						// Pasamos la verificacion Actual a la lista de historicos de la verficaciones y dejamos todo listo para que se pueda iniciar otra, si asi lo requiere el gestor o revisor
//						dbSolicitud.verificaciones.add(dbSolicitud.verificacion);
//						dbSolicitud.verificacion = new Verificacion();
//						dbSolicitud.save();
//						redirect("AccesoVerificacionesController.index", idSolicitud);
//					}
//				}
//			}
//		} else {
//			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
//		}
//		finalizarVerificacionRender(idSolicitud);
//	}


	/**
	 * Sobreescribimos el combo para mostrar los Gestores que podrán ser solicitados para firmar el
	 * requerimiento.
	 * @return Lista de gestores (DNI - Nombre)
	 */
	public static List<ComboItem> requerimientoCrearFirmante() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		
		List<Agente> lGestores = Agente.findAll();
		for (Agente a: lGestores) {
			if (a.roles.contains("gestor")) {
				String name = a.name;
				if (a.name == null || a.name.trim().equals("")) {
					name = a.username;
				}
				result.add(new ComboItem(a.username, name));
			}
		}
		return result;
	}
	
	/**
	 * Solicitar la firma del requerimiento a un Gestor en concreto
	 * @param idSolicitud
	 * @param solicitud
	 */
	public static void frequerimientoSolicitaFirma(Long idSolicitud, SolicitudGenerica solicitud){
		checkAuthenticity();

		if (permisofrequerimientoSolicitaFirma("update") || permisofrequerimientoSolicitaFirma("create")) {
			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			frequerimientoSolicitaFirmaValidateCopy(dbSolicitud, solicitud);
			if(!validation.hasErrors()){
				frequerimientoSolicitaFirmaValidateRules(dbSolicitud, solicitud);
			}
			if(!validation.hasErrors()){
				// Enviar el correo al agente
				String emailAgente = Agente.find("select agente from Agente agente where agente.username=?" , solicitud.verificacion.requerimiento.firmante).first();
				if (emailAgente == null || emailAgente.length() == 0) {
					Messages.error("El agente al que se le solicita la firma no tiene email");
				} else {
					try {
						Mails.enviar("solicitarFirmaRequerimiento", emailAgente);
					} catch (Exception e) {
						play.Logger.error("El mail de Solicitar Firma de requerimiento de la verificación "+solicitud.verificacion.id+" no ha podido ser enviado. "+e.getMessage());
					}
				
					dbSolicitud.verificacion.estado = EstadosVerificacionEnum.enRequerimientoFirmaSolicitada.name();
					dbSolicitud.save();
					Logger.info("Firma de requerimiento solicitada a " + dbSolicitud.verificacion.requerimiento.firmante);
				}
			}
		}
		else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		frequerimientoSolicitaFirmaRender(idSolicitud);
	}
	
	/**
	 * Cuando pulsemos sobre Firmar, Registrar y Notificar el Requerimiento
	 * 
	 * @param idSolicitud
	 * @param firma
	 */
	public static void grequerimientoFirmarRequerimiento(Long idSolicitud, platino.Firma firma){
		checkAuthenticity();

		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
		
		if (permisogrequerimientoFirmarRequerimiento("update") || permisogrequerimientoFirmarRequerimiento("create")) {
			if(!validation.hasErrors()){
				
				/// 1. Firmar el requerimiento
				if (solicitud.verificacion.requerimiento.estado.equals(EstadosRequerimientoEnum.creado.name())) {
					List<Firmante> firmantes = FirmaService.calcularFirmantesRequerimiento();
					play.Logger.info("Firmantes " + firmantes);
					FirmaService.firmar(solicitud.verificacion.requerimiento.oficial, firmantes, firma);
				
					play.Logger.info("Firmado el requerimiento");
				
					if(!Messages.hasErrors()){
						solicitud.verificacion.requerimiento.estado = "firmada";
						solicitud.save();
					}
				}
				
				/// 2. Registrar el requerimiento
				if(!Messages.hasErrors() && solicitud.verificacion.requerimiento.estado.equals(EstadosRequerimientoEnum.firmado.name())) {
					try {
						RegistroService.registrarSalidaRequerimientoActual(solicitud);
					} catch (RegistroException e) {
						e.printStackTrace();
						Messages.error("Se produjo un error al intentar registrar la aportación, inténtelo de nuevo.");
					}
				}
				
				/// 3. Notificar el requerimiento
				
			}
		}
		else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		grequerimientoFirmarRequerimientoRender(idSolicitud);
	}

	public static void nuevosDocumentos(Long idSolicitud) {
		checkAuthenticity();

		// Save code
		if (permisonuevosDocumentos("update") || permisonuevosDocumentos("create")) {

			if (!validation.hasErrors()) {
				SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
				List<Documento> documentosNuevos = VerificacionUtils.existDocumentosNuevos(dbSolicitud.verificacion, idSolicitud);
				// Compruebo que no existen documentos nuevos aportados por el solicitante y que no esten incluidos en la verificacion actual
				if (!documentosNuevos.isEmpty()){
					dbSolicitud.verificacion.nuevosDocumentos.addAll(documentosNuevos);
					dbSolicitud.verificacion.estado=EstadosVerificacionEnum.enVerificacionNuevosDoc.name();
					dbSolicitud.verificacion.verificacionTiposDocumentos = VerificacionUtils.existDocumentosNuevos(dbSolicitud.verificacion, idSolicitud);
					dbSolicitud.save();
					Messages.info("Nuevos documentos aportados por el solicitante añadidos a la verificación actual. Verifique los tipos de estos documentos para proseguir con la verificación en curso.");
				}
			}
			
		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		nuevosDocumentosRender(idSolicitud);

	}
	
	/**
	 * Se sobreescribe para ordenar la tabla por descripcion, uriTipo y Fecha Presentacion
	 * @param idSolicitud
	 * @param idEntidad
	 */
	public static void tablaverificacionDocumentos(Long idSolicitud, Long idEntidad){
		
		Long id = idSolicitud != null? idSolicitud : idEntidad;

		//java.util.List<VerificacionDocumento> rows = VerificacionDocumento.find("select verificacionDocumento from SolicitudGenerica solicitud join solicitud.verificacion.documentos verificacionDocumento where solicitud.id=? order by verificacionDocumento.descripcion, verificacionDocumento.fechaPresentacion", id ).fetch();
		java.util.List<VerificacionDocumento> rows = VerificacionDocumento.find("select verificacionDocumento from TipoDocumento tipoDoc, SolicitudGenerica solicitud join solicitud.verificacion.documentos verificacionDocumento where solicitud.id=? and tipoDoc.uri=verificacionDocumento.uriTipoDocumento order by tipoDoc.nombre, verificacionDocumento.descripcion, verificacionDocumento.fechaPresentacion", id ).fetch();
		
		List<VerificacionDocumento> rowsFiltered = rows; //Tabla sin permisos, no filtra
		
		tables.TableRenderResponse<VerificacionDocumento> response = new tables.TableRenderResponse<VerificacionDocumento>(rowsFiltered);
		renderJSON(response.toJSON("fechaPresentacion", "descripcion", "nombreTipoDocumento", "estadoDocumentoVerificacion", "identificadorMultiple", "linkUrlDescarga", "id"));
			
	}
	
	public static void gnuevoRequerimientoBorradorPreliminar(Long idSolicitud) {
		checkAuthenticity();

		// Save code
		if (permisognuevoRequerimientoBorradorPreliminar("update") || permisognuevoRequerimientoBorradorPreliminar("create")) {

			if (!validation.hasErrors()) {
				// Generar el borrador en PDF del requerimiento
				try {
					SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(idSolicitud);
					new Report("reports/borradorRequerimiento.html").header("reports/header.html").footer("reports/footer-borrador.html").renderResponse(dbSolicitud);
				} catch (Exception e) {
					play.Logger.error("Error generando el borrador", e.getMessage());
					Messages.error("Error generando el borrador");
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		gnuevoRequerimientoBorradorPreliminarRender(idSolicitud);
	}

	// BORRRRRRRRRRRRRRRRRRRRRRRRAAAAAAAAAAAAAAAAAAAARRRRRRRRRRRRRRRRRRRRRRR, para pruebas sólo
//	public static void todosNoProcede(Long idSolicitud) {
//		checkAuthenticity();
//
//		// Save code
//		if (permisotodosNoProcede("update") || permisotodosNoProcede("create")) {
//
//			if (!validation.hasErrors()) {
//				SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
//				for (VerificacionDocumento vd: dbSolicitud.verificacion.documentos){
//					vd.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noProcede.name();
//				}
//				dbSolicitud.save();
//			}
//
//		} else {
//			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
//		}
//
//		todosNoProcedeRender(idSolicitud);
//
//
//	}

}
