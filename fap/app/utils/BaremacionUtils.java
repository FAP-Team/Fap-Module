package utils;

import java.util.List;

import services.BaremacionService;

import models.CEconomico;
import models.SolicitudGenerica;
import models.ValoresCEconomico;

public class BaremacionUtils {

	public static void calcularTotales (SolicitudGenerica solicitud){
		if(solicitud.ceconomicos != null && solicitud.ceconomicos.size() > 0){
			List<List<CEconomico>> sortedCEconomicos = BaremacionService.sortByProfundidad(solicitud.ceconomicos);
			for(int i = sortedCEconomicos.size() -2; i >= 0; i--){
				for(CEconomico ceconomico : sortedCEconomicos.get(i)){
					play.Logger.info("Calculando automático en la solicitud "+ceconomico.tipo.jerarquia);
					if(ceconomico.tipo.clase.equals("auto")){
						List<CEconomico> childs = BaremacionService.getChilds(ceconomico, sortedCEconomicos.get(i + 1));
						BaremacionService.invokeEval(ceconomico.tipo.jerarquia, ceconomico, childs);
					}
				}
			}
		}
		solicitud.save();
	}
	
}
