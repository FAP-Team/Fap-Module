package controllers;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.spi.Message;

import messages.Messages;
import models.ComunicacionInterna;
import models.Documento;
import models.ListaUris;
import controllers.gen.PaginaNuevaComunicacionInternaDocumentosControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;

public class PaginaNuevaComunicacionInternaDocumentosController extends PaginaNuevaComunicacionInternaDocumentosControllerGen {

	public static void seleccionar(Long id, List<Long> idsSeleccionados) {
		Long idComunicacionInterna = id;
		ComunicacionInterna comunicacionInterna = ComunicacionInterna.findById(idComunicacionInterna);
		
		if (comunicacionInterna != null) {
			List<ListaUris> lstUri = new ArrayList<ListaUris>();
			if (idsSeleccionados != null && !idsSeleccionados.isEmpty()){
				for (Long idDoc : idsSeleccionados) {
					Documento doc = Documento.findById(idDoc);
					ListaUris uri = new ListaUris();
					if (doc != null){
						if (doc.uriPlatino != null && !doc.uriPlatino.isEmpty()){
							uri.uri = doc.uriPlatino;
							lstUri.add(uri);
						} else
							if (doc.uri != null && !doc.uri.isEmpty()){
								uri.uri = doc.uriPlatino;
								lstUri.add(uri);
							}
					} else {
						log.error("No se encuentra el documento con identificador: " + idDoc);
						Messages.error("No se encuentra el documento con identificador: " + idDoc);
					}
				}
			}
			
			comunicacionInterna.estado = EstadosComunicacionInternaEnum.docAdjuntos.name();
			comunicacionInterna.save(); 
		} else {
			log.error("No se encuentra la comunicacion interna con identificador: " + idComunicacionInterna);
			Messages.error("No se encuentra la comunicacion interna con identificador: " + idComunicacionInterna);
		}
			
		
		if (!Messages.hasErrors()){
//			SolicitudGenerica solicitud = SolicitudGenerica.find("Select solicitud from Solicitud solicitud join solicitud.comunicacionesInternas comunicacionesInternas where comunicacionesInternas.id = ?", idComunicacionInterna).first();
//			Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
//			ids.put("idSolicitud", solicitud.id);
//			redirect("PaginaNuevaComunicacionDatosController.index", "editar", solicitud.id, idComunicacionInterna);
		}

	}
}
