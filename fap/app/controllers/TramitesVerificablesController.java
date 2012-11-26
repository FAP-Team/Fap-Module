package controllers;

import java.util.ArrayList;
import java.util.List;

import models.TramitesVerificables;

import tags.ComboItem;
import controllers.gen.TramitesVerificablesControllerGen;

public class TramitesVerificablesController extends TramitesVerificablesControllerGen {

	public static List<ComboItem> tramitePorDefecto() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		List <TramitesVerificables> tramitesVerificables = TramitesVerificables.findAll();
		for (TramitesVerificables tramite: tramitesVerificables){
			if (tramite.verificable)
				result.add(new ComboItem(tramite.uriTramite, tramite.nombre));
		}
		return result;
	}
	
}
