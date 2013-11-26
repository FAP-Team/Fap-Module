package controllers;

import java.util.HashMap;
import java.util.Map;

import models.Documento;
import play.mvc.Router;
import utils.AedUtils;
import controllers.gen.paginaFirmaMultiplesDocumentosControllerGen;

public class paginaFirmaMultiplesDocumentosController extends paginaFirmaMultiplesDocumentosControllerGen {

	public static String crearFullUrlId(Long idDocumento){
		Documento documento = Documento.find("select documento from Documento documento where documento.id=?", idDocumento).first();
		play.Logger.info("El documento " + documento.id + " tiene la uri " + documento.uri);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("k", AedUtils.encriptarUri(documento.uri));
		return Router.getFullUrl("fap.DescargasAedController.descargar", params).toString();		
	}
	
}
