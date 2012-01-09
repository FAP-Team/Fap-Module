package controllers;

import java.util.List;

import controllers.gen.PCEconomicosControllerGen;

import models.CEconomico;
import models.SolicitudGenerica;
import models.TipoCEconomico;

public class CEconomicosController extends PCEconomicosControllerGen {

	public static void index(Long idSolicitud){
		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
		
		//Inicializa los conceptos economicos con los Tipos de Conceptos Economicos
		//que están definidos en la base de datos
		if(solicitud != null && solicitud.ceconomicos.isEmpty()){
			List<TipoCEconomico> tipos = TipoCEconomico.findAll();
			for(TipoCEconomico tipo : tipos){
				CEconomico ceconomico = new CEconomico();
				ceconomico.tipo = tipo;
				solicitud.ceconomicos.add(ceconomico);
			}
			solicitud.save();
		}
		
		renderTemplate( "gen/CEconomicos/CEconomicos.html" , solicitud);
	}
	
}
