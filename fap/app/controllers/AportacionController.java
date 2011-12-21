
package controllers;

import java.io.File;

import aed.AedClient;

import messages.Messages;
import models.Aportacion;
import models.Documento;
import models.SolicitudGenerica;

import play.Logger;
import play.mvc.Util;
import properties.FapProperties;
import reports.Report;
import utils.StringUtils;
import controllers.gen.AportacionControllerGen;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
			
public class AportacionController extends AportacionControllerGen {

	public static void index(Long idSolicitud){
		Documento documento = getDocumento();
		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);

		Aportacion aportacion = solicitud.aportaciones.actual;
		if(StringUtils.in(aportacion.estado, "borrador", "firmada", "registrada", "clasificada")){
			Messages.warning("Tiene una aportación pendiente de registro");
			Messages.keep();
			redirect("AportacionPresentarController.index", idSolicitud);
		}else{
			renderTemplate( "gen/Aportacion/Aportacion.html" , documento, solicitud);	
		}
	}
	
	public static void presentar(Long idSolicitud) {
		checkAuthenticity();
		if (permisopresentar("update") || permisopresentar("create")) {
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
							AedClient.borrarDocumento(borradorOld);
							AedClient.borrarDocumento(oficialOld);
						}catch(AedExcepcion e){
							//Error? no importa, son temporales...
							play.Logger.info("Error borrando los documento temporales desde el aed");
						}
						
						//Genera el borrador
						File borrador = new Report("reports/solicitudAportacion.html").header("reports/header.html").footer("reports/footer-borrador.html").renderTmpFile(solicitud);
						aportacion.borrador = new Documento();
						aportacion.borrador.tipo = tipoDocumentoSolicitudAportacion;
						AedClient.saveDocumentoTemporal(aportacion.borrador, borrador);
												
						//Genera el documento oficial
						File oficial =  new Report("reports/solicitudAportacion.html").header("reports/header.html").registroSize().renderTmpFile(solicitud);
						aportacion.oficial = new Documento();
						aportacion.oficial.tipo = tipoDocumentoSolicitudAportacion;
						AedClient.saveDocumentoTemporal(aportacion.oficial, oficial);
						
						
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
