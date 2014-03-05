package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.IdClass;

import messages.Messages;
import messages.Messages.MessageType;
import models.Agente;
import models.AsientoCIFap;
import models.ComunicacionInterna;
import models.Documento;
import models.SolicitudGenerica;
import play.mvc.Util;
import tags.ComboItem;
import controllers.fap.AgenteController;
import controllers.gen.PaginaNuevaComunicacionDocumentosControllerGen;
import enumerado.fap.gen.EstadosComunicacionInternaEnum;

public class PaginaNuevaComunicacionDocumentosController extends PaginaNuevaComunicacionDocumentosControllerGen {
	@Util
	public static ComunicacionInterna getComunicacionInterna(Long idSolicitud, Long idComunicacionInterna) {
		ComunicacionInterna comunicacionInterna = null;

		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		}

		if (idComunicacionInterna == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idComunicacionInterna"))
				Messages.fatal("Falta parámetro idComunicacionInterna");
		}
		if (idSolicitud != null && idComunicacionInterna != null) {
			//No se asocia la entidad a la solicitud hasta que se rellena el asiento
			comunicacionInterna = ComunicacionInterna.findById(idComunicacionInterna);
			if (comunicacionInterna == null)
				Messages.fatal("Error al recuperar ComunicacionInterna");
		}
		return comunicacionInterna;
	}
	

	public static void index(String accion, Long idSolicitud, Long idComunicacionInterna) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/PaginaNuevaComunicacionDocumentos/PaginaNuevaComunicacionDocumentos.html");
		}

		SolicitudGenerica solicitud = PaginaNuevaComunicacionDocumentosController.getSolicitudGenerica(idSolicitud);

		ComunicacionInterna comunicacionInterna = null;
		if ("crear".equals(accion)) {
			comunicacionInterna = PaginaNuevaComunicacionDocumentosController.getComunicacionInterna();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				comunicacionInterna.save();
				idComunicacionInterna = comunicacionInterna.id;
				solicitud.comunicacionesInternas.add(comunicacionInterna);
				solicitud.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			comunicacionInterna = PaginaNuevaComunicacionDocumentosController.getComunicacionInterna(idSolicitud, idComunicacionInterna);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "fap/PaginaNuevaComunicacionDocumentos/PaginaNuevaComunicacionDocumentos.html" + " Agente: " + logAgente);
		renderTemplate("fap/PaginaNuevaComunicacionDocumentos/PaginaNuevaComunicacionDocumentos.html", accion, idSolicitud, idComunicacionInterna, solicitud, comunicacionInterna);
	}

	@Util
	//En id, tiene que llegarle el idComunicacionInterna -> Sobreescribir vista
	public static void seleccionar(Long id, List<Long> idsSeleccionados) {
		//Este método solo "almacena los documentos" creando el asiento
		//El asiento se asigna a la comunicacion interna en guardar
		//AsientoCIFap asiento = new AsientoCIFap();
		Long idComunicacionInterna = id;
		ComunicacionInterna comunicacionInterna = ComunicacionInterna.findById(idComunicacionInterna);
		if (idsSeleccionados != null && !idsSeleccionados.isEmpty()){
			for (Long idFila : idsSeleccionados) {
				// El documento puede no estar subido a Platino -> ¿uri en platino?
				Documento doc = Documento.findById(idFila);
	//			if (doc.uriPlatino != null){ // Existe en Platino
	//				
	//			} else { //Puede que esté o no
	//				
	//			}
				System.out.println("Documento seleccionado: "+doc.descripcionVisible);
				//TODO Queda adjuntar los documentos, por ahora se prueba sin envio de docs 
			}
		}
		else{
			System.out.println("No hay documentos asociados");
		}
		comunicacionInterna.estado = EstadosComunicacionInternaEnum.docAdjuntos.name();
		comunicacionInterna.save(); //esto es para que se guarden las uris del asiento
		SolicitudGenerica solicitud = SolicitudGenerica.find("Select solicitud from Solicitud solicitud join solicitud.comunicacionesInternas comunicacionesInternas where comunicacionesInternas.id = ?", idComunicacionInterna).first();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		ids.put("idSolicitud", solicitud.id);
		redirect("PaginaNuevaComunicacionDatosController.index", "editar", solicitud.id, idComunicacionInterna);
		
	}
}
