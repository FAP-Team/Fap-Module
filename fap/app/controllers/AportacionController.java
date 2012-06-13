
package controllers;

import java.io.File;
import java.io.FileInputStream;

import javax.inject.Inject;

import messages.Messages;
import models.Aportacion;
import models.Documento;
import models.SolicitudGenerica;
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
		renderTemplate("fap/Admin/Aportacion.html", accion, idSolicitud, solicitud);
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
						if(aportacion.borrador != null){
						    Documento borradorOld = aportacion.borrador;
						    aportacion.oficial = null;
						    aportacion.save();
						    gestorDocumentalService.deleteDocumento(borradorOld);
						}
						
						if(aportacion.oficial != null){
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

}
