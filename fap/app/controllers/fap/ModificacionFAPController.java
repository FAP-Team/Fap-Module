package controllers.fap;

import java.util.List;

import play.mvc.Util;

import controllers.PCEconomicosController;
import controllers.PaginaCEconomicosController;

import models.SolicitudGenerica;

import utils.BaremacionUtils;

import models.CEconomico;
import models.CEconomicosManuales;
import models.ValoresCEconomico;


public class ModificacionFAPController extends InvokeClassController {

	public static void postRestaurarSolicitud (Long idSolicitud){
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		List<CEconomico> cEconomico = solicitud.ceconomicos;
		for (CEconomico ceconomico : solicitud.ceconomicos){
		//	play.Logger.info("El tipo otro es = " + ceconomico.tipo.tipoOtro);
			if (ceconomico.tipo.tipoOtro){
				calcularValoresAuto(ceconomico);
			}
		}
	}
	
	@Util
	public static void calcularValoresAuto(CEconomico cEconomico){
		for (ValoresCEconomico valor: cEconomico.valores){
			valor.valorSolicitado = sumarValoresHijosOtro(cEconomico.otros, valor.anio);
		}
		cEconomico.save();
	}
	
	@Util
	private static Double sumarValoresHijosOtro(List<CEconomicosManuales> listaHijosOtro, Integer anio){
		Double suma=0.0;
		for (CEconomicosManuales cEconomicoManual: listaHijosOtro){
			if (!cEconomicoManual.valores.isEmpty())
				if (cEconomicoManual.valores.get(anio).valorSolicitado != null)
					suma += cEconomicoManual.valores.get(anio).valorSolicitado;
		}
		return suma;
	}
	
	
	
}

