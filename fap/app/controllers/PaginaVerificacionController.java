package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityTransaction;

import reports.Report;
import services.FirmaService;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.NotificacionService;
import services.RegistroService;
import services.filesystem.TipoDocumentoEnTramite;

import org.joda.time.DateTime;

import platino.FirmaUtils;
import play.Play;
import play.db.jpa.JPA;
import play.mvc.Util;
import properties.FapProperties;
import tags.ComboItem;
import utils.CalcularFirmantes;
import utils.ComboUtils;
import utils.NotificacionUtils;
import validation.CustomValidation;
import verificacion.VerificacionUtils;
import messages.Messages;
import models.Agente;
import models.Documento;
import models.DocumentoNotificacion;
import models.Firma;
import models.Firmante;
import models.Firmantes;
import models.Notificacion;
import models.Requerimiento;
import models.SolicitudGenerica;
import models.TableKeyValue;
import models.TipoDocumento;
import models.Tramite;
import models.TramitesVerificables;
import models.Verificacion;
import models.VerificacionDocumento;
import models.VerificacionTramites;
import controllers.fap.AgenteController;
import controllers.fap.VerificacionFapController;
import controllers.gen.PaginaVerificacionControllerGen;
import emails.Mails;
import enumerado.fap.gen.EstadoNotificacionEnum;
import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;
import es.gobcan.eadmon.verificacion.ws.dominio.DocumentoVerificacion;
import es.gobcan.aciisi.servicios.enotificacion.notificacion.NotificacionException;
import es.gobcan.platino.servicios.registro.Documentos;

public class PaginaVerificacionController extends PaginaVerificacionControllerGen {
	
    @Inject
    static FirmaService firmaService;

    @Inject
    static RegistroService registroService;
    
    @Inject
    static NotificacionService notificacionService;
    
	@Inject
	static GestorDocumentalService gestorDocumentalService;
	
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

        if ((solicitud != null) && (solicitud.verificacion != null) && (solicitud.verificacion.estado != null)){
        	log.info("Visitando página: " + "gen/PaginaVerificacion/PaginaVerificacion.html");
        	if ((verificacion.uriTramite == null) || (verificacion.uriTramite.isEmpty())){
        		VerificacionTramites vTramites = VerificacionTramites.get(VerificacionTramites.class);
        		if ((vTramites.uriTramitePorDefecto != null) && (!vTramites.uriTramitePorDefecto.isEmpty()))
        			verificacion.uriTramite=vTramites.uriTramitePorDefecto;
        	}
        	renderTemplate("gen/PaginaVerificacion/PaginaVerificacion.html", accion, idSolicitud, idVerificacion, solicitud, verificacion);
        } else
        	redirect("AccesoVerificacionesController.index", accion, idSolicitud);
		
	}
	
	//Métodos en el controlador manual
	public static List<ComboItem> getTramitesCombo () {
		List<ComboItem> result = new ArrayList<ComboItem>();
		VerificacionTramites vTramites = VerificacionTramites.get(VerificacionTramites.class);
		for (TramitesVerificables t: vTramites.tramites) {
			if (t.verificable)
				result.add(new ComboItem(t.uriTramite, t.nombre));
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
			//dbVerificacion.uriTramite = verificacion.tramiteNombre.uri;
			PaginaVerificacionController.iniciarVerificacionValidateCopy("editar", dbVerificacion, verificacion);
		}

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.iniciarVerificacionValidateRules(dbVerificacion, verificacion);
		}
		if (!Messages.hasErrors()) {
			dbVerificacion.estado = EstadosVerificacionEnum.verificandoTipos.name();
			dbVerificacion.fechaUltimaActualizacion = new DateTime();
			dbVerificacion.verificacionTiposDocumentos = VerificacionUtils.existDocumentosNuevos(dbVerificacion, idSolicitud);
			//SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			//dbSolicitud.estado = EstadosSolicitudEnum.enVerificacion.name();
			dbVerificacion.save();
			//dbSolicitud.save();
			log.info("Acción sobre iniciar verificacion de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción sobre iniciar verificacion de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.iniciarVerificacionRender(idSolicitud, idVerificacion);
	}
	
	public static void tablaverificacionTipos(Long idSolicitud) {

		java.util.List<Documento> rows = Documento.find("select documento from SolicitudGenerica solicitud join solicitud.verificacion.verificacionTiposDocumentos documento where solicitud.id=? and (documento.verificado is null or documento.verificado = false)",idSolicitud).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Documento> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("fechaSubida", "fechaRegistro", "tipo", "descripcionVisible", "verificado", "urlDescarga", "id"));
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
				dbSolicitud.verificacion.documentos = VerificacionUtils.getVerificacionDocumentosFromNewDocumentos((List<Documento>)VerificacionFapController.invoke(VerificacionFapController.class, "getNuevosDocumentosVerificar", dbSolicitud.verificacion.id, idSolicitud), dbSolicitud.verificacion.uriTramite, dbSolicitud.verificaciones, idSolicitud);
			} catch (Throwable e) {
				play.Logger.error("Error recuperando los documentos nuevos a verificar", e.getMessage());
			}

			dbSolicitud.verificacion.estado = EstadosVerificacionEnum.obtenerNoProcede.name();
			dbSolicitud.verificacion.nuevosDocumentos.clear();
			dbSolicitud.verificacion.verificacionTiposDocumentos.clear();
			dbSolicitud.verificacion.fechaUltimaActualizacion = new DateTime();
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
			for (Documento doc: dbSolicitud.verificacion.nuevosDocumentos){
				VerificacionDocumento vDoc= new VerificacionDocumento(doc);
				TipoDocumento tipo = TipoDocumento.find("select tipo from TipoDocumento tipo where tipo.tramitePertenece=? and tipo.uri=?", dbSolicitud.verificacion.uriTramite, doc.tipo).first();
				if (tipo != null)
					vDoc.identificadorMultiple = tipo.cardinalidad;
				else
					log.error("Tipo no encontrado al verificar los tipos de documentos nuevos: "+doc.tipo);
				vDoc.existe = true;
				vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noVerificado.name();
				vDoc.save();
				dbSolicitud.verificacion.documentos.add(vDoc);
			}
			
			/// Si existen verificaciones anteriores el estado al que pasa es "obtenerNoProcede",
			/// en caso contrario a "enVerificacion"
			if (dbSolicitud.verificaciones.size() > 0)
				dbSolicitud.verificacion.estado = EstadosVerificacionEnum.obtenerNoProcede.name();
			else
				dbSolicitud.verificacion.estado = EstadosVerificacionEnum.enVerificacion.name();
			dbSolicitud.verificacion.nuevosDocumentos.clear();
			dbSolicitud.verificacion.verificacionTiposDocumentos.clear();
			dbSolicitud.verificacion.fechaUltimaActualizacion = new DateTime();
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
			List<Documento> documentosNuevos = VerificacionUtils.existDocumentosNuevos(dbSolicitud.verificacion, idSolicitud);
			// Compruebo que no existen documentos nuevos aportados por el solicitante y que no esten incluidos en la verificacion actual
			if (!documentosNuevos.isEmpty()){
				dbSolicitud.verificacion.nuevosDocumentos.addAll(documentosNuevos);
				dbSolicitud.verificacion.estado=EstadosVerificacionEnum.enVerificacionNuevosDoc.name();
				dbSolicitud.verificacion.verificacionTiposDocumentos = VerificacionUtils.existDocumentosNuevos(dbSolicitud.verificacion, idSolicitud);
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
				new Report("reports/requerimiento.html").header("reports/header.html").footer("reports/footer-borrador.html").renderResponse(dbSolicitud);
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
	public static void gnuevoRequerimientoBorradorPreliminargR(Long idSolicitud, Long idVerificacion, String obtenerBorradorPreliminar) {
		checkAuthenticity();
		if (!permisoGnuevoRequerimientoBorradorPreliminargR("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		if (!Messages.hasErrors()) {
			PaginaVerificacionController.gnuevoRequerimientoBorradorPreliminargRValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			try {
				SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(idSolicitud);
				new Report("reports/requerimiento.html").header("reports/header.html").footer("reports/footer-borrador.html").renderResponse(dbSolicitud);
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
		PaginaVerificacionController.gnuevoRequerimientoBorradorPreliminargRRender(idSolicitud, idVerificacion);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void guardarMotivoRequerimientoGeneral(Long idSolicitud, Long idVerificacion, SolicitudGenerica solicitud, String guardarRequerimientoMotivoRequerimientoGeneralbtn, String btnFinalizarVerificacion) {
		checkAuthenticity();
		if (!permisoGuardarMotivoRequerimientoGeneral("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = PaginaVerificacionController.getSolicitudGenerica(idSolicitud);

		PaginaVerificacionController.guardarMotivoRequerimientoGeneralBindReferences(solicitud);

		PaginaVerificacionController.guardarRequerimientoMotivoRequerimientoGeneralbtnGuardarMotivoRequerimientoGeneral(idSolicitud, idVerificacion, solicitud);
		if (!Messages.hasErrors()) {

			PaginaVerificacionController.guardarMotivoRequerimientoGeneralValidateCopy("editar", dbSolicitud, solicitud);

		}

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.guardarMotivoRequerimientoGeneralValidateRules(dbSolicitud, solicitud);
		}
		
		Agente logAgente = AgenteController.getAgente();
		
		if (!Messages.hasErrors()) {
			dbSolicitud.save();
		}
		if (guardarRequerimientoMotivoRequerimientoGeneralbtn != null) {
				PaginaVerificacionController.guardarMotivoRequerimientoGeneralRender(idSolicitud, idVerificacion);
		}

		if (btnFinalizarVerificacion != null) {
			PaginaVerificacionController.btnFinalizarVerificacionGuardarMotivoRequerimientoGeneral(idSolicitud, idVerificacion, solicitud);
			PaginaVerificacionController.guardarMotivoRequerimientoGeneralRender(idSolicitud, idVerificacion);
		}

		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		PaginaVerificacionController.guardarMotivoRequerimientoGeneralRender(idSolicitud, idVerificacion);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void btnFinalizarVerificacionGuardarMotivoRequerimientoGeneral(Long idSolicitud, Long idVerificacion, SolicitudGenerica solicitud) {
	//public static void finalizarVerificacion(Long idSolicitud, Long idVerificacion, String btnFinalizarVerificacion) {	
		checkAuthenticity();
		if (!permisoFinalizarVerificacion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			SolicitudGenerica dbSolicitud = getSolicitudGenerica(idSolicitud);
			// Comprobamos que esten todos los documentos verificados
			if (!VerificacionUtils.existsDocumentoNoVerificado(dbSolicitud.verificacion)){
				// Si hay cosas que requerir, la verificación tiene causas subsanables
				if (((dbSolicitud.verificacion.requerimiento.motivo != null) && (!dbSolicitud.verificacion.requerimiento.motivo.trim().isEmpty()))
                        || (VerificacionUtils.documentosIncorrectos(dbSolicitud.verificacion))){
					log.info("Hay que requerir y notificar, existe un motivo general de requerimiento o documentos en estado noValidos o noPresentados (Solicitud "+dbSolicitud.id+")");
					CustomValidation.required("solicitud.verificacion.requerimiento.firmante", dbSolicitud.verificacion.requerimiento.firmante);
					Requerimiento requerimiento = dbSolicitud.verificacion.requerimiento;
					if(!Messages.hasErrors()){
						try {
							String tipoDocumentoRequerimiento = FapProperties.get("fap.aed.tiposdocumentos.requerimiento");
													
							if((requerimiento.oficial != null) && (requerimiento.oficial.uri != null) && (!requerimiento.oficial.uri.trim().equals(""))){
							    Documento oficialOld = requerimiento.oficial;
							    requerimiento.oficial = null;
							    requerimiento.save();
							    gestorDocumentalService.deleteDocumento(oficialOld);
							}						

							//Genera el documento oficial
							SolicitudGenerica solicitudb = dbSolicitud;
							File oficial =  new Report("reports/requerimiento.html").header("reports/header.html").registroSize().renderTmpFile(solicitudb);
							requerimiento.oficial = new Documento();
							requerimiento.oficial.tipo = tipoDocumentoRequerimiento;
							requerimiento.oficial.descripcion = "Requerimiento";
							requerimiento.oficial.clasificado=false;
							
							gestorDocumentalService.saveDocumentoTemporal(requerimiento.oficial, new FileInputStream(oficial), oficial.getName());
							
							requerimiento.estado = "borrador";
							requerimiento.save();
							
							
							// Actualizamos los datos de la verificacion para verificaciones posteriores, en este caso el estado.
							dbSolicitud.verificacion.estado = EstadosVerificacionEnum.enRequerimiento.name();
							Messages.ok("Se deberá realizar un Requerimiento");
						}catch(Exception e){
							Messages.error("Se produjo un error generando el documento de requerimiento.");
							play.Logger.error(e, "Error al generar el documento de requerimiento: " + e.getMessage());
							e.printStackTrace();
						}
					}

				} else { // Si la verificación ha ido correcta, no hay ninguna causa subsanable
					log.info("La verificación se ha podido finalizar con éxito, todo es correcto");
					Messages.ok("La verificación no tiene ningun requerimiento, finalizada correctamente y con éxito");
					
					// Ponemos todos los documentos de la verificacion como verificados, para que no se incluyan en sucesivas verificaciones
					VerificacionUtils.setVerificadoDocumentos(dbSolicitud.verificacion.documentos, dbSolicitud.documentacion.documentos);
					if (dbSolicitud.verificacion.tramiteNombre.nombre.equalsIgnoreCase("Solicitud")){
						if ((dbSolicitud.registro.oficial != null) && (!dbSolicitud.registro.oficial.uri.isEmpty()) && ((dbSolicitud.registro.oficial.verificado == null) || (!dbSolicitud.registro.oficial.verificado)))
							VerificacionUtils.setVerificadoDocumento(dbSolicitud.verificacion.documentos, dbSolicitud.registro.oficial);
					}
					// Actualizamos los datos de la verificacion para verificaciones posteriores, en este caso el estado.
					dbSolicitud.verificacion.estado = EstadosVerificacionEnum.verificacionPositiva.name();
					
					// Actualizamos los datos de la verificacion para verificaciones posteriores. Copiamos la verificacionActual a las verificaciones Anteriores para poder empezar una nueva verificación.
					dbSolicitud.verificaciones.add(dbSolicitud.verificacion);
					// Si el trámite es distinto de "Solicitud" se vuelve al estado anterior de la verificación
					if (!dbSolicitud.verificacion.tramiteNombre.nombre.equalsIgnoreCase("Solicitud")) {
						dbSolicitud.estado = dbSolicitud.estadoAntesVerificacion;
					}
					else {
						// Según el estado anterior de la Solicitud cambiamos a nuevo estado
						if (dbSolicitud.estado.equals(EstadosSolicitudEnum.enVerifAceptadoRSLPROV.name()))
							dbSolicitud.estado = EstadosSolicitudEnum.concedidoRSLPROV.name();
						else if (dbSolicitud.estado.equals(EstadosSolicitudEnum.enVerifAceptadoRSLDEF.name()))
							dbSolicitud.estado = EstadosSolicitudEnum.concedidoRSLDEF.name();
						else
							dbSolicitud.estado = EstadosSolicitudEnum.verificado.name();
					}
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

    @Util
    public static boolean permisoFinalizarVerificacion(String accion) {
        //Sobreescribir para incorporar permisos a mano
        return true;
    }

    @Util
    public static void finalizarVerificacionValidateRules() {
        //Sobreescribir para validar las reglas de negocio
    }

    @Util
    public static void finalizarVerificacionRender(Long idSolicitud, Long idVerificacion) {
        if (!Messages.hasMessages()) {
            Messages.ok("Página editada correctamente");
            Messages.keep();
            redirect("PaginaVerificacionController.index", "editar", idSolicitud, idVerificacion);
        }
        Messages.keep();
        redirect("PaginaVerificacionController.index", "editar", idSolicitud, idVerificacion);
    }

	/**
	 * Lista los gestores que pueden firmar el requerimiento
	 * @return
	 */
	public static List<ComboItem> gestorAFirmar() {
		return ComboUtils.gestorAFirmar();
	}

	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void gRequerirFirmaRequerimiento(Long idSolicitud, Long idVerificacion, SolicitudGenerica solicitud, String requerirFirma) {
		checkAuthenticity();
		if (!permisoGRequerirFirmaRequerimiento("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = PaginaVerificacionController.getSolicitudGenerica(idSolicitud);

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.gRequerirFirmaRequerimientoValidateCopy("editar", dbSolicitud, solicitud);
		}

		if (!Messages.hasErrors()) {
			// Se debe enviar el mail de "solicitarFirmaRequerimiento"
			envioMailFirmaRequerimiento(dbSolicitud);
		}
		if (!Messages.hasErrors()) {
			dbSolicitud.save();
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.gRequerirFirmaRequerimientoRender(idSolicitud, idVerificacion);
	}

	private static void envioMailFirmaRequerimiento(SolicitudGenerica solicitud) {
		String mailRevisor = null;
		String mailGestor = null;
		try {
			Agente revisor = AgenteController.getAgente();
			mailRevisor = revisor.email;
			mailGestor = ((Agente) Agente.find("select agente from Agente agente where agente.username=?", solicitud.verificacion.requerimiento.firmante).first()).email;
			play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", solicitud);
			play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("mailGestor", mailGestor);
			play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("mailRevisor", mailRevisor);
			
			Mails.enviar("solicitarFirmaRequerimiento", solicitud, mailGestor, mailRevisor);
            Messages.ok("Se envió requerimiento al gestor firmante solicitado.");
            solicitud.verificacion.estado = EstadosVerificacionEnum.enRequerimientoFirmaSolicitada.name();
            solicitud.save();
		} catch (Exception e) {
			play.Logger.error("No se pudo enviar el mail solicitarFirmaRequerimiento a los mails: "+mailGestor+", "+mailRevisor+". Error: "+e.getMessage());
		}
	}
	
	@Util
	public static void gRequerirFirmaRequerimientoValidateCopy(String accion, SolicitudGenerica dbSolicitud, SolicitudGenerica solicitud) {
		CustomValidation.clearValidadas();
		if (secure.checkGrafico("requerimientoRequerirFirma", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("solicitud.verificacion.requerimiento", dbSolicitud.verificacion.requerimiento);
			CustomValidation.valid("solicitud.verificacion", dbSolicitud.verificacion);
			CustomValidation.valid("solicitud", dbSolicitud);
			CustomValidation.required("solicitud.verificacion.requerimiento.firmante", dbSolicitud.verificacion.requerimiento.firmante);
			CustomValidation.validValueFromTable("solicitud.verificacion.requerimiento.firmante", solicitud.verificacion.requerimiento.firmante);
			dbSolicitud.verificacion.requerimiento.registro.firmantes.todos = CalcularFirmantes.getGestorComoFirmante(solicitud.verificacion.requerimiento.firmante);
			dbSolicitud.verificacion.requerimiento.registro.firmantes.save();
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
			
			// Si no ha sido firmada
			if (!dbSolicitud.verificacion.requerimiento.registro.fasesRegistro.firmada) {
				PaginaVerificacionController.firmaRequerimientoGFirmarRequerimiento(idSolicitud, idVerificacion, firma);
			}
			
			// Si ya fue firmada y no ha sido registrada
			if (dbSolicitud.verificacion.requerimiento.registro.fasesRegistro.firmada
					&& !dbSolicitud.verificacion.requerimiento.registro.fasesRegistro.registro) {
				try {

					models.JustificanteRegistro justificanteSalida = registroService.registroDeSalida(dbSolicitud.solicitante, dbSolicitud.verificacion.requerimiento.oficial, dbSolicitud.expedientePlatino, "Requerimiento");
					
					// ----- Hecho por Paco ------------------------
					
					dbSolicitud.verificacion.requerimiento.registro.informacionRegistro.setDataFromJustificante(justificanteSalida);
					
					Documento documento = dbSolicitud.verificacion.requerimiento.justificante;
			        documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSalida");
			        String aplicacionJ = "la aplicación";
			        if ((FapProperties.get("fap.app.name.requerimiento.justificante.descripcion") != null) && (!"undefined".equals(FapProperties.get("fap.app.name.requerimiento.justificante.descripcion"))))
			        	aplicacionJ = FapProperties.get("fap.app.name.requerimiento.justificante.descripcion");
			        documento.descripcion = "Justificante de registro de requerimientos de la solicitud de "+aplicacionJ;
			        documento.save();

			        InputStream is = justificanteSalida.getDocumento().contenido.getInputStream();
			        gestorDocumentalService.saveDocumentoTemporal(documento, is, "JustificanteRequerimiento" + dbSolicitud.verificacion.requerimiento.id + ".pdf");
			        play.Logger.info("Justificante del Requerimiento almacenado en el AED");
			        
			        List<Documento> documentos = new ArrayList<Documento>();
			        
			        documentos.add(dbSolicitud.verificacion.requerimiento.justificante);
			        documentos.add(dbSolicitud.verificacion.requerimiento.oficial);
			        
			        // TODO: OJO, Descomentar esto, que el el correcto funcionamiento, pero pro problemas de la sede electronica de mis notificaciones
					//       No se puede ni poner a notificable tanto el requerimiento como su justificante de registro
					//       Ni poder los datos de registro del requerimiento
			        
			        try { // Sin registro
		                gestorDocumentalService.clasificarDocumentos(dbSolicitud, documentos, false /*true*/);
		            } catch (Exception e) {
		                play.Logger.error("No se ha podido clasificar el justificante del requerimiento: "+e.getMessage());
		            }
			        
//			        documentos.clear();
//			        documentos.add(dbSolicitud.verificacion.requerimiento.oficial);
//			        
//			        try { // Con registro
//		                gestorDocumentalService.clasificarDocumentos(dbSolicitud, documentos, dbSolicitud.verificacion.requerimiento.registro.informacionRegistro, true);
//		            } catch (Exception e) {
//		            	play.Logger.error("No se ha podido clasificar el requerimiento oficial: "+e.getMessage());
//		            }
					
			        // ------------------------------------------
			        
					play.Logger.info("Se ha registrado de Salida el documento del requerimiento de la solicitud "+dbSolicitud.id);
					Messages.ok("Se ha registrado el Requerimiento correctamente.");
					dbSolicitud.verificacion.requerimiento.registro.fasesRegistro.registro = true;
					
					//Si todo fue correcto, cambio el estado de la verificacion a firmada y registrada
					//para diferenciarla de las que aún esperan la firma.
					dbSolicitud.verificacion.estado=EstadosVerificacionEnum.enRequerimientoFirmadaRegistrada.name();
					
					dbSolicitud.save();
					
				} catch (Exception e) {
					Messages.error("No se ha podido registrar el requerimiento de la solicitud "+dbSolicitud.id);
					play.Logger.error("No se ha podido registrar el requerimiento de la solicitud "+dbSolicitud.id+": "+e.getMessage());
				}
			}
			
			// Si ya fue registrada
			if (dbSolicitud.verificacion.requerimiento.registro.fasesRegistro.registro) {
				Notificacion notificacion = dbSolicitud.verificacion.requerimiento.notificacion;
				if (notificacion.estado == null || notificacion.estado.isEmpty()) {
					//La notificación no ha sido creada
					DocumentoNotificacion docANotificar = new DocumentoNotificacion(dbSolicitud.verificacion.requerimiento.justificante.uri);
					notificacion.documentosANotificar.add(docANotificar);
					notificacion.interesados.addAll(dbSolicitud.solicitante.getAllInteresados());
					notificacion.descripcion = "Notificación";
					notificacion.plazoAcceso = FapProperties.getInt("fap.notificacion.plazoacceso");
					notificacion.plazoRespuesta = FapProperties.getInt("fap.notificacion.plazorespuesta");
					notificacion.frecuenciaRecordatorioAcceso = FapProperties.getInt("fap.notificacion.frecuenciarecordatorioacceso");
					notificacion.frecuenciaRecordatorioRespuesta = FapProperties.getInt("fap.notificacion.frecuenciarecordatoriorespuesta");
					notificacion.estado = EstadoNotificacionEnum.creada.name();
					notificacion.idExpedienteAed = dbSolicitud.expedienteAed.idAed;
					notificacion.asunto = "Notificación por Requerimiento";
					notificacion.save();
					dbSolicitud.save();
				}

			}
			
			PaginaVerificacionController.gFirmarRequerimientoRender(idSolicitud, idVerificacion);
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

		if (solicitud.verificacion.requerimiento.registro.firmantes.todos == null || solicitud.verificacion.requerimiento.registro.firmantes.todos.size() == 0) {
			solicitud.verificacion.requerimiento.registro.firmantes.todos = CalcularFirmantes.getGestoresComoFirmantes();
			solicitud.verificacion.requerimiento.registro.firmantes.save();
		}
		FirmaUtils.firmar(solicitud.verificacion.requerimiento.oficial, solicitud.verificacion.requerimiento.registro.firmantes.todos, firma, null);
		
		if (!Messages.hasErrors()) {
			Messages.ok("El requerimiento se ha firmado correctamente");
			
			solicitud.verificacion.requerimiento.registro.fasesRegistro.firmada = true;
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
			Notificacion notificacion = solicitud.verificacion.requerimiento.notificacion;
		
			EntityTransaction tx = JPA.em().getTransaction();
			if (notificacion.estado.equals(EstadoNotificacionEnum.creada.name())) {
				// TODO: Está en estado creada, debo notificarla
				try {
					if (tx.isActive())
				    	tx.commit();
					tx.begin();
					notificacionService.enviarNotificaciones(notificacion, AgenteController.getAgente());
					
					play.Logger.info("Se ha puesto a disposición la notificación "+notificacion.id);
					notificacion.fechaPuestaADisposicion = new DateTime();
					notificacion.save();
					tx.commit();
				} catch (Exception e) {
					if ( tx != null && tx.isActive() ) tx.rollback();
					Messages.error("Ha ocurrido un error en el proceso de notificación");
					play.Logger.error("Ha ocurrido un error en el proceso de notificación para la notificacion: "+notificacion.id+" error: "+e.getMessage());
					e.printStackTrace();
				}
				tx.begin();
			}
			
			// Si fue puesta a disposición
			if (notificacion.estado.equals(EstadoNotificacionEnum.puestaadisposicion.name())) {
				try{
					if (tx.isActive())
				    	tx.commit();
					tx.begin();
					
					if (!solicitud.verificacion.estado.equals(EstadosVerificacionEnum.enRequerido.name())) {
						solicitud.verificacion.estado = EstadosVerificacionEnum.enRequerido.name();
						// Ponemos todos los documentos de la verificacion como verificados, para que no se incluyan en sucesivas verificaciones
						VerificacionUtils.setVerificadoDocumentos(solicitud.verificacion.documentos, solicitud.documentacion.documentos);
						
						//Se marca la solicitud como verificada
						if (solicitud.verificacion.tramiteNombre.nombre.equalsIgnoreCase("Solicitud")) { 
							if ((solicitud.registro.oficial != null) && (!solicitud.registro.oficial.uri.isEmpty()) && ((solicitud.registro.oficial.verificado == null) || (!solicitud.registro.oficial.verificado))) 
								VerificacionUtils.setVerificadoDocumento(solicitud.verificacion.documentos, solicitud.registro.oficial); 
						}
						
						// Actualizamos los datos de la verificacion para verificaciones posteriores. Copiamos la verificacionActual a las verificaciones Anteriores para poder empezar una nueva verificación.
						solicitud.verificaciones.add(solicitud.verificacion);
						solicitud.save();
					}
					notificacion.estado = EstadoNotificacionEnum.enviada.name();
					notificacion.save();
					
					play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", solicitud);
					Mails.enviar("emitirRequerimiento", solicitud);
					tx.commit();
				} catch (Exception e) {
					if ( tx != null && tx.isActive() ) tx.rollback();
					play.Logger.error("No se pudo enviar el mail emitirRequerimiento: "+e.getMessage());
				}
				tx.begin();
			}
		}

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.gPonerADisposicionValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
			Messages.ok("El proceso de notificación se ha realizado satisfactoriamente");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.gPonerADisposicionRender(idSolicitud, idVerificacion);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void anularVerificacion(Long idSolicitud, Long idVerificacion, String botonAnularVerificacion) {
		checkAuthenticity();
		if (!permisoAnularVerificacion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
		long idVerificacionActualyMal=0;
		if (!Messages.hasErrors()) {
			// Cambiamos la verificación actual a null para 'resetear' la verificación y simular el anulamiento de la misma
			solicitud.verificacion.setNullOneToMany();
			solicitud.save();
			idVerificacionActualyMal = solicitud.verificacion.id;
			solicitud.verificacion = null;
			solicitud.estado = solicitud.estadoAntesVerificacion;
		}

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.anularVerificacionValidateRules();
		}
		if (!Messages.hasErrors()) {
			solicitud.save();
			log.info("Anulada Verificación Correctamente por el Agente: "+AgenteController.getAgente().username+". Deshechada la Verificación con ID: "+idVerificacionActualyMal);
			Messages.warning("Verificación Anulada Correctamente, puede iniciar una Nueva Verificación");
			Messages.keep();
			redirect("AccesoVerificacionesController.index", getAccion(), idSolicitud);
		} else
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.anularVerificacionRender(idSolicitud, idVerificacion);
	}

    @Util
    public static void reabrirVerificacion(Long idSolicitud, Long idVerificacion, String botonReabrirVerificacion) {
        checkAuthenticity();
        if (!permisoReabrirVerificacion("editar")) {
            Messages.error("No tiene permisos suficientes para realizar la acción");
        }

        if(!Messages.hasErrors()) {
            Verificacion verificacion = Verificacion.findById(idVerificacion);
            if (verificacion != null) {
                verificacion.estado = EstadosVerificacionEnum.enVerificacion.name();
                verificacion.save();
                Messages.ok("Ahora puede modificar la verificación");
            }
        }

        Agente logAgente = AgenteController.getAgente();
        if (!Messages.hasErrors()) {
            log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito " + " Agente: " + logAgente);
        } else
            log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
        PaginaVerificacionController.reabrirVerificacionRender(idSolicitud, idVerificacion);
    }

	
	@Util
	public static void obtenerNoProcede(Long idSolicitud, Long idVerificacion){
		//Debe extraer los documentos clasificados como no procede en verificaciones anteriores
		//todas las verificaciones de la solicitud
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		List<Verificacion> verificaciones = solicitud.verificaciones;
		Map<String, VerificacionDocumento> docsnp = new HashMap<String, VerificacionDocumento>();		
		
		for (Verificacion verif : verificaciones) {
			List<VerificacionDocumento> documentos = verif.documentos;
			
			for (VerificacionDocumento doc : documentos) {
				//Si estado es noProcede, existe y no está ya en la lista -> Añadir
				if ((doc.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noProcede.name())) && (doc.existe) && (!docsnp.containsKey(doc.uriDocumento))){
					docsnp.put(doc.uriDocumento, new VerificacionDocumento(doc));
				}
				//Si está en lista, existe, el estado es distinto de noProcede y fecha posterior al almacenado
				else if ((!doc.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.noProcede.name())) && (doc.existe) && (docsnp.containsKey(doc.uriDocumento))){
					docsnp.remove(doc.uriDocumento);
				}
			}
		}
		Verificacion verificacion = Verificacion.findById(idVerificacion);
		verificacion.documentos.addAll(docsnp.values());
		verificacion.estado = EstadosVerificacionEnum.enVerificacion.name();
		verificacion.save();
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmtiposProceso(Long idSolicitud, Long idVerificacion, String obtenerNoProcede) {
		checkAuthenticity();
		if (!permisoFrmtiposProceso("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			obtenerNoProcede(idSolicitud, idVerificacion);
		}

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.frmtiposProcesoValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.frmtiposProcesoRender(idSolicitud, idVerificacion);
	}
	

	public static void tablaverificacionDocumentos(Long idSolicitud) {
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		java.util.List<VerificacionDocumento> rows = solicitud.verificacion.documentos;
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<VerificacionDocumento> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<VerificacionDocumento> response = new tables.TableRenderResponse<VerificacionDocumento>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("fechaPresentacion", "descripcion", "nombreTipoDocumento", "estadoDocumentoVerificacion", "identificadorMultiple", "linkUrlDescarga", "id"));
	}
	
	@Util
	public static void incluirNoProcede(SolicitudGenerica solicitud, Long idVerificacion) {
		Verificacion verificacion = Verificacion.findById(idVerificacion);
		TipoDocumento tDoc = TipoDocumento.find("select tDoc from TipoDocumento tDoc where uri=? and tramitePertenece=?", verificacion.incluirFichMultiple, verificacion.uriTramite).first();
		if (tDoc == null) {
			play.Logger.error("No existe el tipo de documento "+verificacion.incluirFichMultiple+"en el trámite "+verificacion.uriTramite+" de la verificación actual: "+verificacion.id);
			Messages.error("No existe el tipo de documento en el trámite de la verificación actual");
			return;
		}
		VerificacionDocumento vDoc = new VerificacionDocumento();
		vDoc.existe = false;
		vDoc.uriTipoDocumento = tDoc.uri;
		vDoc.identificadorMultiple = tDoc.cardinalidad;
		vDoc.descripcion = TableKeyValue.getValue("tiposDocumentos", tDoc.uri);
		vDoc.estadoDocumentoVerificacion = EstadosDocumentoVerificacionEnum.noProcede.name();
		vDoc.save();
		verificacion.documentos.add(vDoc);
		verificacion.save();
	}
	
	/**
	 * A partir del trámite de la verificación, devuelve todos los tipos de documentos
	 * que cumplan:
	 * 		- Aportado por CIUDADANO
	 * 
	 * @return
	 */
	public static List<ComboItem> comboMultiples() {
		Long idSolicitud = Long.parseLong(params.get("idSolicitud"));
		Long idVerificacion = Long.parseLong(params.get("idVerificacion"));
		Verificacion verificacion = Verificacion.findById(idVerificacion);
		// Obtenemos el trámite actual
		Tramite tramite = verificacion.tramiteNombre;
		List<ComboItem> result = new ArrayList<ComboItem>();
		for (TipoDocumento tDoc : tramite.documentos) {
			if (tDoc.aportadoPor.equalsIgnoreCase("CIUDADANO")) {
				result.add(new ComboItem(tDoc.uri, tDoc.nombre));
			}
		}

		return result;
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void incluirNoProcedeMultiples(Long idSolicitud, Long idVerificacion, SolicitudGenerica solicitud, String incluirNoProcede) {
		checkAuthenticity();
		
		if (!permisoIncluirNoProcedeMultiples("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = PaginaVerificacionController.getSolicitudGenerica(idSolicitud);

		PaginaVerificacionController.incluirNoProcedeMultiplesBindReferences(solicitud);

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.incluirNoProcedeMultiplesValidateCopy("editar", dbSolicitud, solicitud);
		}
		
		if ((!Messages.hasErrors()) && (!incluirNoProcede.isEmpty())){
			incluirNoProcede(solicitud, idVerificacion);
		}

		if (!Messages.hasErrors()) {
			PaginaVerificacionController.incluirNoProcedeMultiplesValidateRules(dbSolicitud, solicitud);
		}
		if (!Messages.hasErrors()) {
			dbSolicitud.save();
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaVerificacion/PaginaVerificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaVerificacionController.incluirNoProcedeMultiplesRender(idSolicitud, idVerificacion);
	}
	
}
