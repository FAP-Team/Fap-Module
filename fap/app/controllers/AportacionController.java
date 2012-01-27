
package controllers;

import java.io.File;

import javax.inject.Inject;

import messages.Messages;
import models.Aportacion;
import models.Documento;
import models.SolicitudGenerica;
import properties.FapProperties;
import reports.Report;
import services.AedService;
import utils.StringUtils;
import controllers.gen.AportacionControllerGen;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
			
public class AportacionController extends AportacionControllerGen {

	
	@Inject
	static AedService aedService;

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
		Aportacion aportacion = solicitud.aportaciones.actual;
		if(StringUtils.in(aportacion.estado, "borrador", "firmada", "registrada", "clasificada")){
			Messages.warning("Tiene una aportación pendiente de registro");
			Messages.keep();
			redirect("AportacionPresentarController.index", accion, idSolicitud);
		}
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
						
						//Si los documentos ya estaban creados los borra
						Documento borradorOld = aportacion.borrador;
						Documento oficialOld = aportacion.oficial;						
						aportacion.borrador = null;
						aportacion.oficial = null;
						aportacion.save();
						
						try {
							aedService.borrarDocumento(borradorOld);
							aedService.borrarDocumento(oficialOld);
						}catch(AedExcepcion e){
							//Error? no importa, son temporales...
							play.Logger.info("Error borrando los documento temporales desde el aed");
						}
						
						// Borramos los documentos que se pudieron generar en una llamada previa al metodo, para no dejar basura en la BBDD
						if ((borradorOld != null) && (borradorOld.delete() == null))
							play.Logger.info("Error borrando los documento temporales generados para el borrador");
						if ((oficialOld != null) && (oficialOld.delete() == null))
							play.Logger.info("Error borrando los documento temporales generados para el documento oficial");
						
						//Genera el borrador
						File borrador = new Report("reports/solicitudAportacion.html").header("reports/header.html").footer("reports/footer-borrador.html").renderTmpFile(solicitud);
						aportacion.borrador = new Documento();
						aportacion.borrador.tipo = tipoDocumentoSolicitudAportacion;
						aedService.saveDocumentoTemporal(aportacion.borrador, borrador);
												
						//Genera el documento oficial
						File oficial =  new Report("reports/solicitudAportacion.html").header("reports/header.html").registroSize().renderTmpFile(solicitud);
						aportacion.oficial = new Documento();
						aportacion.oficial.tipo = tipoDocumentoSolicitudAportacion;
						aedService.saveDocumentoTemporal(aportacion.oficial, oficial);
						
						
						aportacion.estado = "borrador";
						aportacion.save();
					}catch(Exception e){
						Messages.error("Se produjo un error generando el documento de aportación.");
						play.Logger.error("Error al generar el documento de la aportación: " + e.getLocalizedMessage());
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
