package controllers.popups;

import com.google.inject.spi.Message;

import messages.Messages;
import messages.Messages.MessageType;
import models.Agente;
import models.ComunicacionInterna;
import models.Documento;
import models.ListaUris;
import models.SolicitudGenerica;
import play.mvc.Util;
import controllers.fap.AgenteController;
import controllers.gen.popups.PopupBorrarDocumentosAsociadosAltaCIControllerGen;

public class PopupBorrarDocumentosAsociadosAltaCIController extends PopupBorrarDocumentosAsociadosAltaCIControllerGen {

	@Util
	public static ListaUris getListaUris(Long idComunicacionInterna, Long idListaUris) {
		ListaUris listaUris = null;

		if (idComunicacionInterna == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idComunicacionInterna"))
				Messages.fatal("Falta parámetro idComunicacionInterna");
		}

		if (idListaUris == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idListaUris"))
				Messages.fatal("Falta parámetro idListaUris");
		}
		
		if (idComunicacionInterna != null && idListaUris != null) {
			listaUris = ListaUris.find("select listaUris from ComunicacionInterna comunicacionInterna join comunicacionInterna.asiento.uris listaUris where comunicacionInterna.id=? and listaUris.id=?", idComunicacionInterna, idListaUris).first();
		}
		
		if (idComunicacionInterna != null && idListaUris != null && listaUris == null) {
			java.util.List<ListaUris> lstUris = ListaUris.find("select listaUris from ComunicacionInterna comunicacionInterna join comunicacionInterna.asiento.uris listaUris where comunicacionInterna.id=?", idComunicacionInterna).fetch();
			Documento doc = Documento.findById(idListaUris);
			
			if (doc != null){
				Long idListaUrisDoc = null;
				for (ListaUris uri: lstUris){
					if (doc.uriPlatino != null && !doc.uriPlatino.isEmpty() && doc.uriPlatino.equals(uri.uri)){
						idListaUrisDoc = uri.id;
					} else
						if (doc.uri != null && !doc.uri.isEmpty() && doc.uri.equals(uri.uri)){
							idListaUrisDoc = uri.id;
						}
				}
				
				listaUris = ListaUris.find("select listaUris from ComunicacionInterna comunicacionInterna join comunicacionInterna.asiento.uris listaUris where comunicacionInterna.id=? and listaUris.id=?", idComunicacionInterna, idListaUrisDoc).first();
			} 
		}
		
		if (listaUris == null)
			Messages.fatal("Error al recuperar ListaUris");
		
		return listaUris;
	}
	
	public static void borrar(Long idSolicitud, Long idComunicacionInterna, Long idListaUris) {
		checkAuthenticity();
		if (!permiso("borrar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		
		ListaUris dbListaUris = PopupBorrarDocumentosAsociadosAltaCIController.getListaUris(idComunicacionInterna, idListaUris);
		ComunicacionInterna dbComunicacionInterna = PopupBorrarDocumentosAsociadosAltaCIController.getComunicacionInterna(idSolicitud, idComunicacionInterna);
		if (!Messages.hasErrors()) {
			PopupBorrarDocumentosAsociadosAltaCIController.borrarValidateRules(dbListaUris);
		}
		
		if (!Messages.hasErrors()) {
			if (dbComunicacionInterna.asiento.uris.indexOf(dbListaUris) == 0)
				Messages.error("No se puede excluir el documento principal de la comunicación en este fase");
		}
		
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			dbComunicacionInterna.asiento.uris.remove(dbListaUris);
			dbComunicacionInterna.save();
			dbListaUris.delete();

			log.info("Acción Borrar de página: " + "gen/popups/PopupBorrarDocumentosAsociadosAltaCI.html" + " , intentada con éxito" + " Agente: " + logAgente);
		} else {
			log.info("Acción Borrar de página: " + "gen/popups/PopupBorrarDocumentosAsociadosAltaCI.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		}
		PopupBorrarDocumentosAsociadosAltaCIController.borrarRender(idSolicitud, idComunicacionInterna, idListaUris);
	}

}
