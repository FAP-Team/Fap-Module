package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;

import messages.Messages;
import models.Aportacion;
import models.Documento;
import models.SolicitudGenerica;
import play.mvc.Util;
import properties.FapProperties;
import reports.Report;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.async.GestorDocumentalServiceAsync;
import tramitacion.TramiteBase;
import utils.StringUtils;
import controllers.fap.AportacionFapController;
import controllers.fap.PresentacionFapController;
import controllers.gen.AportacionControllerGen;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
			
public class AportacionController extends AportacionControllerGen {

	
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
    		Aportacion aportacion = solicitud.aportaciones.actual;
    		if(StringUtils.in(aportacion.estado, "borrador", "firmada", "registrada", "clasificada")){
    			Messages.warning("Tiene una aportación pendiente de registro");
    			Messages.keep();
    			redirect("AportacionPresentarController.index", accion, idSolicitud);
    		}
		}
		log.info("Visitando página: " + "gen/Aportacion/Aportacion.html");
		renderTemplate("gen/Aportacion/Aportacion.html", accion, idSolicitud, solicitud);
	}
	
	public static void presentar(Long idSolicitud, SolicitudGenerica solicitud, String botonPresentar) {
		checkAuthenticity();
		if (!permisoPresentarSinRegistrar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(idSolicitud);
		solicitud = dbSolicitud;
		if (!Messages.hasErrors()) {
			
			Aportacion aportacion = solicitud.aportaciones.actual;

			if(aportacion.documentos.isEmpty()){
				Messages.error("Debe aportar al menos un documento");
				
				//Reinicia el estado de la aportación
				aportacion.estado = null;
				aportacion.save();
				try {
					TramiteBase tramite = AportacionFapController.invoke("getTramiteObject", idSolicitud);
					tramite.deshacer();
				} catch (Throwable e) {
					play.Logger.info("No se ha podido deshacer la aportación de la solicitud tras no haber ningun documento aportado: "+e.getMessage());
				}
			}
			
			if(!Messages.hasErrors() && !aportacion.registro.fasesRegistro.borrador){
				try {
					TramiteBase tramite = AportacionFapController.invoke("getTramiteObject", idSolicitud);
					tramite.prepararFirmar();
					if (!Messages.hasErrors()){
						if (solicitud.registro.fasesRegistro.expedienteAed){
							solicitud.aportaciones.actual.registro.fasesRegistro.expedienteAed = true;
							solicitud.aportaciones.actual.registro.fasesRegistro.save();
						}
						if ((solicitud.expedientePlatino != null) && (solicitud.expedientePlatino.uri != null) && ((!solicitud.expedientePlatino.uri.isEmpty()))){
							solicitud.aportaciones.actual.registro.fasesRegistro.expedientePlatino = true;
							solicitud.aportaciones.actual.registro.fasesRegistro.save();
						}
						aportacion.estado = "borrador";
						aportacion.save();
					}
				} catch (Throwable e) {
					log.error("Hubo un problema al intentar invocar a los métodos de la clase AportacionFAPController en prepararPresentar: "+e.getMessage());
					Messages.error("No se pudo preparar para Presentar");
				}
			}
		}
		if(!Messages.hasErrors()){
			Messages.ok("La solicitud de aportación se preparó correctamente");		
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
		SolicitudGenerica dbSolicitud = AportacionController.getSolicitudGenerica(idSolicitud);

		AportacionController.presentarSinRegistrarBindReferences(solicitud);

		if (!Messages.hasErrors()) {
			Aportacion aportacion = dbSolicitud.aportaciones.actual;

			if(aportacion.documentos.isEmpty()){
				Messages.error("Debe aportar al menos un documento");
				
				//Reinicia el estado de la aportación
				aportacion.estado = null;
				aportacion.save();
				try {
					TramiteBase tramite = AportacionFapController.invoke("getTramiteObject", idSolicitud);
					tramite.deshacer();
				} catch (Throwable e) {
					play.Logger.info("No se ha podido deshacer la aportación de la solicitud tras no haber ningun documento aportado: "+e.getMessage());
				}
			}
			
			if(!Messages.hasErrors()) {
				aportacion.estado = "borrador";
				aportacion.save();
				AportacionController.presentarSinRegistrarValidateCopy("editar", dbSolicitud, solicitud);
				
				validateDateIsAfterNow(aportacion.fechaAportacionSinRegistro);
				clasificarDocumentosAportacionSinRegistro(dbSolicitud, aportacion);
				finalizarAportacion(dbSolicitud, aportacion);
			}

		}

		if (!Messages.hasErrors()) {
			AportacionController.presentarSinRegistrarValidateRules(dbSolicitud, solicitud);
		}
		if (!Messages.hasErrors()) {
			dbSolicitud.save();
			log.info("Acción Editar de página: " + "gen/Aportacion/Aportacion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/Aportacion/Aportacion.html" + " , intentada sin éxito (Problemas de Validación)");
		AportacionController.presentarSinRegistrarRender(idSolicitud);
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
    
    private static void clasificarDocumentosAportacionSinRegistro(SolicitudGenerica solicitud, Aportacion aportacion) {
        if (!Messages.hasErrors() && aportacion.estado.equals("borrador")) {
            // Establecemos la fecha de registro en todos los documentos
            // de la aportación
            for (Documento doc : aportacion.documentos) {
                doc.fechaRegistro = aportacion.fechaAportacionSinRegistro;
                doc.save();
            }

            // Los documentos temporales se pasan a clasificados, pero sin
            // registrar
            List<Documento> documentos = new ArrayList<Documento>();
            documentos.addAll(aportacion.documentos);
            boolean todosClasificados = true;
            try {
                await(gestorDocumentalServiceAsync.clasificarDocumentos(solicitud, documentos));
            } catch (Exception e) {
                todosClasificados = false;
            }

            if (todosClasificados) {
                aportacion.estado = "clasificada";
                aportacion.save();
                play.Logger.info("Se clasificaron (sin registrar) todos los documentos");
            } else {
                Messages.error("Algunos documentos no se pudieron clasificar (sin registrar) correctamente");
            }
        }
    }
    
    /**
     * Mueve la aportación a la lista de aportaciones clasificadas Añade los
     * documentos a la lista de documentos
     * 
     * Cambia el estado de la aportación a finalizada
     * 
     * @param solicitud
     * @param aportacion
     */
    private static void finalizarAportacion(SolicitudGenerica solicitud, Aportacion aportacion) {
        if (aportacion.estado.equals("clasificada")) {
            solicitud.aportaciones.registradas.add(aportacion);
            solicitud.documentacion.documentos.addAll(aportacion.documentos);
            solicitud.aportaciones.actual = new Aportacion();
            solicitud.save();
            aportacion.estado = "finalizada";
            aportacion.save();

            play.Logger.debug("Los documentos de la aportacion se movieron correctamente");
        }
    }

}
