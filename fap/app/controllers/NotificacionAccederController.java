package controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import messages.Messages;
import models.Aportacion;
import models.Documento;
import models.JustificanteRegistro;
import models.Notificacion;
import models.SolicitudGenerica;
import play.mvc.Util;
import properties.FapProperties;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.RegistroService;
import controllers.gen.NotificacionAccederControllerGen;
import emails.Mails;
import enumerado.fap.gen.EstadoNotificacionEnum;

public class NotificacionAccederController extends NotificacionAccederControllerGen {
	
	@Inject
	static RegistroService registroService;

	@Inject
	static GestorDocumentalService gestorDocumentalService;
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void btnConfirmar(Long idNotificacion, String firma, String acuseReciboNotificacion) {
		checkAuthenticity();
		if (!permisoBtnConfirmar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		Notificacion dbNotificacion = NotificacionAccederController.getNotificacion(idNotificacion);

		if (acuseReciboNotificacion != null) {
			// Firma
			if (!dbNotificacion.registro.fasesRegistro.firmada)
				NotificacionAccederController.acuseReciboNotificacionBtnConfirmar(idNotificacion, firma);
			// Registra
			registrarNotificacion(dbNotificacion);
			clasificarDocumentosNotificacionConRegistro(dbNotificacion);
			
			if (!Messages.hasErrors()) {
				dbNotificacion.save();
				Messages.clear();
				Messages.ok("Notificacion abierta correctamente.");
			}
			NotificacionAccederController.btnConfirmarRender(idNotificacion);
		}

		if (!Messages.hasErrors()) {
			NotificacionAccederController.btnConfirmarValidateRules(firma);
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/NotificacionAcceder/NotificacionAcceder.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/NotificacionAcceder/NotificacionAcceder.html" + " , intentada sin éxito (Problemas de Validación)");
		NotificacionAccederController.btnConfirmarRender(idNotificacion);
	}

	@Util
	public static void btnConfirmarRender(Long idNotificacion) {
		if (!Messages.hasMessages()) {
			Messages.ok("Notificacion correcta");
			Messages.keep();
			redirect("NotificacionVerController.index", "editar", idNotificacion);
		}
		Messages.keep();
		redirect("NotificacionVerController.index", "editar", idNotificacion);
	}
	
	private static void registrarNotificacion(Notificacion notificacion) {
        // Registro de entrada en platino
        if (notificacion.registro.fasesRegistro.firmada){
            try {  
//            	// Registra la solicitud
//                JustificanteRegistro justificante = registroService.registrarEntrada(solicitud.solicitante,aportacion.oficial, solicitud.expedientePlatino, null);
//                play.Logger.info("Se ha registrado la notificacion %s en platino", notificacion.id);
//
//                // Almacena la información de registro
//                notificacion.registro.informacionRegistro.setDataFromJustificante(justificante);
//                play.Logger.info("Almacenada la información del registro en la base de datos para la notificacion");
//
//                // Guarda el justificante en el AED
//                play.Logger.info("Se procede a guardar el justificante de la notificacion %s en el AED", notificacion.id);
//                guardarJustificanteEnGestorDocumental(notificacion, justificante);
//                
//                notificacion.estado = EstadoNotificacionEnum.accedida.name();
//                notificacion.save();
            } catch (Exception e) {
                Messages.error("Error al registrar de entrada la notificacion");
                return;
            }
        }
    }

	private static void guardarJustificanteEnGestorDocumental(Notificacion notificacion, JustificanteRegistro justificante) throws GestorDocumentalServiceException, IOException {
//	    Documento documento = notificacion.justificante;
//	    documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSolicitud");
//	    documento.descripcion = "Justificante de registro";
//	    documento.save();
//	
//	    InputStream is = justificante.getDocumento().contenido.getInputStream();
//	    gestorDocumentalService.saveDocumentoTemporal(documento, is, "JustificanteNotificacion" + notificacion.id + ".pdf");
//	    play.Logger.info("Justificante almacenado en el AED");
	}
	
	
    private static void clasificarDocumentosNotificacionConRegistro(Notificacion notificacion) {
//        // Clasifica los documentos
//        if (aportacion.estado.equals("registrada")) {
//            boolean todosClasificados = true;
//
//            // Clasifica los documentos sin registro
//            List<Documento> documentos = new ArrayList<Documento>();
//            documentos.addAll(aportacion.documentos);
//            documentos.add(aportacion.justificante);
//
//            try {
//                gestorDocumentalService.clasificarDocumentos(solicitud, documentos);
//            } catch (GestorDocumentalServiceException e) {
//                todosClasificados = false;
//            }
//
//            // Clasifica los documentos con registro de entrada
//            List<Documento> documentosRegistrados = new ArrayList<Documento>();
//            documentosRegistrados.add(aportacion.oficial);
//
//            try {
//                gestorDocumentalService.clasificarDocumentos(solicitud, documentosRegistrados,
//                        aportacion.informacionRegistro);
//            } catch (Exception e) {
//                todosClasificados = false;
//            }
//
//            if (todosClasificados) {
//                aportacion.estado = "clasificada";
//                aportacion.save();
//                play.Logger.info("Se clasificaron todos los documentos");
//            } else {
//                Messages.error("Algunos documentos no se pudieron clasificar correctamente");
//            }
//        } else {
//            play.Logger.debug("Ya están clasificados todos los documentos de la solicitud %s", solicitud.id);
//        }
    }

}
