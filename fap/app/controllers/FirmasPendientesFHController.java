package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.SolicitudGenerica;
import controllers.gen.FirmasPendientesFHControllerGen;
import enumerado.fap.gen.EstadosSolicitudEnum;
import enumerado.fap.gen.EstadosVerificacionEnum;

public class FirmasPendientesFHController extends FirmasPendientesFHControllerGen {

	public static void tablalistaPresentacionesPendientesFH() {

		java.util.List<SolicitudGenerica> rows = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudGenerica> rowsFiltered = new ArrayList<SolicitudGenerica>();
		
		for (SolicitudGenerica sol: rows){		
			if ((sol.registro != null) && 
				(sol.registro.habilitaFuncionario) &&
			    (!sol.registro.fasesRegistro.clasificarAed)
			   ){
				rowsFiltered.add(sol);
			}
		}

		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, true, false, false, "esFuncionarioHabilitadoYActivadaProperty", "", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estado", "solicitante.numeroId", "solicitante.nombreCompleto"));
	}
	
	public static void tablalistaAportacionesPendientesFH() {

		java.util.List<SolicitudGenerica> rows = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudGenerica> rowsFiltered = new ArrayList<SolicitudGenerica>();
		
		for (SolicitudGenerica sol: rows){		
			if ((sol.aportaciones != null) && 
				(sol.aportaciones.actual != null) &&
				(sol.aportaciones.actual.habilitaFuncionario) &&
			    (!sol.aportaciones.actual.estado.equals("finalizada"))
			   ){
				rowsFiltered.add(sol);
			}
		}

		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, true, false, false, "esFuncionarioHabilitadoYActivadaProperty", "", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estado", "solicitante.numeroId", "solicitante.nombreCompleto"));
	}
	
}
