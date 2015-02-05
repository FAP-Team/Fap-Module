package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.Documento;
import models.ListaUris;
import controllers.gen.PaginaDatosLeerComunicacionInternaControllerGen;

public class PaginaDatosLeerComunicacionInternaController extends PaginaDatosLeerComunicacionInternaControllerGen {

	public static void tabladocumentosCI(Long idComunicacionInterna) {

		java.util.List<ListaUris> rows = ListaUris.find("select listaUris from ComunicacionInterna comunicacionInterna join comunicacionInterna.asiento.uris listaUris where comunicacionInterna.id=?", idComunicacionInterna).fetch();
		
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Documento> rowsFiltered = new ArrayList<Documento>();
		
		for (ListaUris lsturi : rows){
			Documento doc = null;
			if (lsturi != null && lsturi.uri != null && !lsturi.uri.isEmpty()){
				doc = Documento.findByUri(lsturi.uri);
				
				if (doc == null){
					doc = Documento.findByUriPlatino(lsturi.uri);
				}
				
				if (doc != null)
					rowsFiltered.add(doc);
			}
		}

		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("descripcionVisible", "enlaceDescarga", "enlaceDescargaFirmado", "id"));
	}
}
