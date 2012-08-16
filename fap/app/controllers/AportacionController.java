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
import utils.StringUtils;
import controllers.gen.AportacionControllerGen;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
			
public class AportacionController extends AportacionControllerGen {

	
	@Inject
	static GestorDocumentalService gestorDocumentalService;

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
	
	public static void presentar(Long idSolicitud) {
		checkAuthenticity();
		if (permisoPresentar("editar") || permisoPresentar("crear")) {
			if (!validation.hasErrors()) {
				SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
				
				Aportacion aportacion = solicitud.aportaciones.actual;

				if(aportacion.documentos.isEmpty()){
					Messages.error("Debe aportar al menos un documento");
					
					//Reinicia el estado de la aportación
					aportacion.estado = null;
					aportacion.save();
				}
				
				if(!Messages.hasErrors() && aportacion.estado == null){
					try {
						String tipoDocumentoSolicitudAportacion = FapProperties.get("fap.aed.tiposdocumentos.aportacion.solicitud");
						
	                    // Borramos los documentos que se pudieron generar en una llamada previa al metodo, para no dejar basura en la BBDD
						if((aportacion.borrador != null) && (aportacion.borrador.uri != null) && (!aportacion.borrador.uri.trim().equals(""))){
						    Documento borradorOld = aportacion.borrador;
						    aportacion.oficial = null;
						    aportacion.save();
						    gestorDocumentalService.deleteDocumento(borradorOld);
						}
						
						if((aportacion.oficial != null) && (aportacion.oficial.uri != null) && (!aportacion.oficial.uri.trim().equals(""))){
						    Documento oficialOld = aportacion.oficial;
						    aportacion.oficial = null;
						    aportacion.save();
						    gestorDocumentalService.deleteDocumento(oficialOld);
						}						
		
						//Genera el borrador
						File borrador = new Report("reports/solicitudAportacion.html").header("reports/header.html").footer("reports/footer-borrador.html").renderTmpFile(solicitud);
						aportacion.borrador = new Documento();
						aportacion.borrador.tipo = tipoDocumentoSolicitudAportacion;
						aportacion.borrador.descripcion = "Borrador solicitud aportación";
						
						gestorDocumentalService.saveDocumentoTemporal(aportacion.borrador, new FileInputStream(borrador), borrador.getName());
												
						//Genera el documento oficial
						File oficial =  new Report("reports/solicitudAportacion.html").header("reports/header.html").registroSize().renderTmpFile(solicitud);
						aportacion.oficial = new Documento();
						aportacion.oficial.tipo = tipoDocumentoSolicitudAportacion;
						aportacion.oficial.descripcion = "Solicitud aportación";
						
						gestorDocumentalService.saveDocumentoTemporal(aportacion.oficial, new FileInputStream(oficial), oficial.getName());
						
						aportacion.estado = "borrador";
						aportacion.save();
					}catch(Exception e){
						Messages.error("Se produjo un error generando el documento de aportación.");
						play.Logger.error(e, "Error al generar el documento de la aportación: " + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		} else {
			Messages.error("En este momento no se permiten aportaciones");
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
                gestorDocumentalService.clasificarDocumentos(solicitud, documentos);
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
