package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.SolicitudGenerica;
import controllers.gen.VerificacionesSinFinalizarControllerGen;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;

public class VerificacionesSinFinalizarController extends VerificacionesSinFinalizarControllerGen {

	public static void tablalistaVerificacionesSinFinalizar(Long idSolicitud) {

		java.util.List<SolicitudGenerica> rows = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		
		List<SolicitudGenerica> rowsFiltered = new ArrayList<SolicitudGenerica>();
		
		for (SolicitudGenerica sol: rows){		
			if ((sol.verificacion != null) && 
				(sol.estado != null) &&
				(sol.verificacion.estado != null) &&
			    (!sol.verificacion.estado.equals(EstadosVerificacionEnum.enRequerido.name())) &&
			    (!sol.verificacion.estado.equals(EstadosVerificacionEnum.plazoVencido.name())) &&
			    (!sol.verificacion.estado.equals(EstadosVerificacionEnum.verificacionNegativa.name())) &&
			    (!sol.verificacion.estado.equals(EstadosVerificacionEnum.verificacionPositiva.name())) &&
			    (!sol.estado.equals(EstadosSolicitudEnum.excluido.name())) &&
			    (!sol.estado.equals(EstadosSolicitudEnum.excluidoRSLDEF.name())) &&
			    (!sol.estado.equals(EstadosSolicitudEnum.desistido.name()))
			   ){
				rowsFiltered.add(sol);
			}
		}

		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, true, false, false, "noUsuario", "", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estado", "verificacion.id", "verificacion.estado", "solicitante.numeroId", "solicitante.nombreCompleto"));
	}
	
}
