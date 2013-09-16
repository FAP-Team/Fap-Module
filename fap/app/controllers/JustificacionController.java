package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import messages.Messages;
import models.Justificacion;
import models.Documento;
import models.SolicitudGenerica;

import org.joda.time.DateTime;

import play.mvc.Util;
import services.GestorDocumentalService;
import services.async.GestorDocumentalServiceAsync;
import tramitacion.TramiteBase;
import utils.StringUtils;
import controllers.fap.JustificacionFapController;
import controllers.gen.JustificacionControllerGen;

public class JustificacionController extends JustificacionControllerGen {
	@Inject
	static GestorDocumentalServiceAsync gestorDocumentalServiceAsync;

	public static void index(String accion, Long idSolicitud){
		if (accion == null)
			accion = "editar";
		SolicitudGenerica solicitud = null;
		if(accion.equals("crear")){
			solicitud = new SolicitudGenerica();
		}
		else if (!accion.equals("borrado")){
			solicitud = getSolicitudGenerica(idSolicitud);
		}
		if (!permiso(accion)){
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}
		if(solicitud != null){
    		Justificacion justificacion = solicitud.justificaciones.actual;
    		if(StringUtils.in(justificacion.estado, "borrador", "firmada", "registrada", "clasificada")){
    			Messages.warning("Tiene una aportación pendiente de registro");
    			Messages.keep();
    			redirect("JustificacionPresentarController.index", accion, idSolicitud);
    		}
		}
		log.info("Visitando página: " + "gen/Justificacion/Justificacion.html");
		renderTemplate("gen/Justificacion/Justificacion.html", accion, idSolicitud, solicitud);
	}
	
	public static void presentar(Long idSolicitud, SolicitudGenerica solicitud, String botonPresentar) {
		checkAuthenticity();
		if (!permisoPresentarSinRegistrar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(idSolicitud);
		solicitud = dbSolicitud;
		if (!Messages.hasErrors()) {
			
			Justificacion justificacion = solicitud.justificaciones.actual;

			if(justificacion.documentos.isEmpty()){
				Messages.error("Debe aportar al menos un documento");
				
				//Reinicia el estado de la aportación
				justificacion.estado = null;
				justificacion.save();
				try {
					TramiteBase tramite = JustificacionFapController.invoke("getTramiteObject", idSolicitud);
					tramite.deshacer();
				} catch (Throwable e) {
					play.Logger.info("No se ha podido deshacer la aportación de la solicitud tras no haber ningun documento aportado: "+e.getMessage());
				}
			}
			
			if(!Messages.hasErrors() && !justificacion.registro.fasesRegistro.borrador){
				try {
					TramiteBase tramite = JustificacionFapController.invoke("getTramiteObject", idSolicitud);
					tramite.prepararFirmar();
					if (!Messages.hasErrors()){
						if (solicitud.registro.fasesRegistro.expedienteAed){
							solicitud.justificaciones.actual.registro.fasesRegistro.expedienteAed = true;
							solicitud.justificaciones.actual.registro.fasesRegistro.save();
						}
						if ((solicitud.expedientePlatino != null) && (solicitud.expedientePlatino.uri != null) && ((!solicitud.expedientePlatino.uri.isEmpty()))){
							solicitud.justificaciones.actual.registro.fasesRegistro.expedientePlatino = true;
							solicitud.justificaciones.actual.registro.fasesRegistro.save();
						}
						justificacion.estado = "borrador";
						justificacion.save();
					}
				} catch (Throwable e) {
					log.error("Hubo un problema al intentar invocar a los métodos de la clase JustificacionFAPController en prepararPresentar: "+e.getMessage());
					Messages.error("No se pudo preparar para Presentar");
				}
			}
		}
		if(!Messages.hasErrors()){
			Messages.ok("La solicitud de justificación se preparó correctamente");		
		}
		
		presentarRender(idSolicitud);
	}
	
    /**
     * Presenta la aportación de documentación sin registrar los documentos.
     * Deberá realizarlo únicamente un gestor, administrador o revisor.
     */
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void presentarSinRegistrar(Long idSolicitud, SolicitudGenerica solicitud, String aportarSinRegistrar) {
		checkAuthenticity();
		if (!permisoPresentarSinRegistrar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = JustificacionController.getSolicitudGenerica(idSolicitud);

		JustificacionController.presentarSinRegistrarBindReferences(solicitud);

		if (!Messages.hasErrors()) {
			Justificacion justificacion = dbSolicitud.justificaciones.actual;

			if(justificacion.documentos.isEmpty()){
				Messages.error("Debe aportar al menos un documento");
				
				//Reinicia el estado de la aportación
				justificacion.estado = null;
				justificacion.save();
				try {
					TramiteBase tramite = JustificacionFapController.invoke("getTramiteObject", idSolicitud);
					tramite.deshacer();
				} catch (Throwable e) {
					play.Logger.info("No se ha podido deshacer la justificación de la solicitud tras no haber ningun documento aportado: "+e.getMessage());
				}
			}
			
			if(!Messages.hasErrors()) {
				justificacion.estado = "borrador";
				justificacion.save();
				JustificacionController.presentarSinRegistrarValidateCopy("editar", dbSolicitud, solicitud);
				
				validateDateIsAfterNow(justificacion.fechaJustificacionSinRegistro);
				clasificarDocumentosJustificacionSinRegistro(dbSolicitud, justificacion);
				finalizarJustificacion(dbSolicitud, justificacion);
			}

		}

		if (!Messages.hasErrors()) {
			JustificacionController.presentarSinRegistrarValidateRules(dbSolicitud, solicitud);
		}
		if (!Messages.hasErrors()) {
			dbSolicitud.save();
			log.info("Acción Editar de página: " + "gen/Justificacion/Justificacion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/Justificacion/Justificacion.html" + " , intentada sin éxito (Problemas de Validación)");
		JustificacionController.presentarSinRegistrarRender(idSolicitud);
	}
    
    private static void validateDateIsAfterNow(DateTime fecha) {
        if(!Messages.hasErrors()){
            if ((fecha == null) || (fecha.isAfterNow())) {
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date date = new Date();
                Messages.error("La fecha de incorporación debe ser anterior a " + dateFormat.format(date));
            }
        }
    }
    
    private static void clasificarDocumentosJustificacionSinRegistro(SolicitudGenerica solicitud, Justificacion justificacion) {
        if (!Messages.hasErrors() && justificacion.estado.equals("borrador")) {
            // Establecemos la fecha de registro en todos los documentos
            // de la aportación
            for (Documento doc : justificacion.documentos) {
                doc.fechaRegistro = justificacion.fechaJustificacionSinRegistro;
                doc.save();
            }

            // Los documentos temporales se pasan a clasificados, pero sin
            // registrar
            List<Documento> documentos = new ArrayList<Documento>();
            documentos.addAll(justificacion.documentos);
            boolean todosClasificados = true;
            try {
                await(gestorDocumentalServiceAsync.clasificarDocumentos(solicitud, documentos));
            } catch (Exception e) {
                todosClasificados = false;
            }

            if (todosClasificados) {
            	justificacion.estado = "clasificada";
            	justificacion.save();
                play.Logger.info("Se clasificaron (sin registrar) todos los documentos");
            } else {
                Messages.error("Algunos documentos no se pudieron clasificar (sin registrar) correctamente");
            }
        }
    }
    
    /**
     * Mueve la aportación a la lista de justificaciones clasificadas Añade los
     * documentos a la lista de documentos
     * 
     * Cambia el estado de la aportación a finalizada
     * 
     * @param solicitud
     * @param justificacion
     */
    private static void finalizarJustificacion(SolicitudGenerica solicitud, Justificacion justificacion) {
        if (justificacion.estado.equals("clasificada")) {
            solicitud.justificaciones.registradas.add(justificacion);
            solicitud.documentacion.documentos.addAll(justificacion.documentos);
            solicitud.justificaciones.actual = new Justificacion();
            solicitud.save();
            justificacion.estado = "finalizada";
            justificacion.save();

            play.Logger.debug("Los documentos de la justificacion se movieron correctamente");
        }
    }

}
