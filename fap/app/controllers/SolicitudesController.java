
package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import messages.Messages;
import models.Agente;
import models.Busqueda;
import models.RepresentantePersonaJuridica;
import models.Solicitante;
import models.SolicitudGenerica;

import org.apache.log4j.Logger;

import play.mvc.Util;
import properties.FapProperties;

import controllers.fap.AgenteController;
import controllers.gen.SolicitudesControllerGen;
import enumerado.fap.gen.RolesEnum;
			
public class SolicitudesController extends SolicitudesControllerGen {
	
	protected static Logger log = Logger.getLogger("Paginas");

	public static void tablalistaSolicitudesBuscadas() {
		Busqueda busqueda = SolicitudesController.getBusqueda(); 
		java.util.List<SolicitudGenerica> rows, rowsRepresentantes = null;

		if ( (busqueda.solicitud == null || busqueda.solicitud.isEmpty()) && 
			 (busqueda.interesado == null || busqueda.interesado.isEmpty()) && 
			 (busqueda.estadoSolicitud == null || busqueda.estadoSolicitud.isEmpty()) ) {
			rows = SolicitudGenerica.findAll();
		}
		else {
			String consulta = "select solicitud from SolicitudGenerica solicitud join solicitud.autorizacion s where"; //  
			Boolean andWhere = false;		// separar las condiciones de la cláusula where con 'and' (cuando corresponda)
				
			if (busqueda.solicitud != null && !busqueda.solicitud.isEmpty()) {
				String[] listaExped = busqueda.solicitud.trim().split("\\s+");
				String exp = listaExped[0];
				consulta += "( (solicitud.expedienteAed.idAed like '%" + exp + "%') ";
				for (int i = 1; i < listaExped.length; i++)
					consulta += "or (solicitud.expedienteAed.idAed like '%" + listaExped[i] + "%') ";
				consulta += ")";
				andWhere = true;
			}
			if (busqueda.interesado != null && !busqueda.interesado.isEmpty()) {
				//String interesado = "('" + busqueda.interesado.trim().replaceAll("\\s+", "','") + "')";
				String[] listaInteresado = busqueda.interesado.trim().split("\\s+");				
				if (andWhere) 
					consulta += " and ";
				String interesado = listaInteresado[0];

				consulta += " ( (solicitud.solicitante.fisica.nombre like '%" + interesado + "%')";
				consulta += " or (solicitud.solicitante.fisica.primerApellido like '%" + interesado + "%')";
				consulta += " or (solicitud.solicitante.fisica.segundoApellido like '%" + interesado + "%')";
				consulta += " or (solicitud.solicitante.fisica.nip.valor like '%" + interesado + "%')";
				consulta += " or (solicitud.solicitante.juridica.entidad like '%" + interesado + "%')";
				consulta += " or (solicitud.solicitante.juridica.cif like '%" + interesado + "%')";
				consulta += " or (solicitud.solicitante.representante.fisica.nombre like '%" + interesado + "%')";
				consulta += " or (solicitud.solicitante.representante.fisica.primerApellido like '%" + interesado + "%')";
				consulta += " or (solicitud.solicitante.representante.fisica.segundoApellido like '%" + interesado + "%')";
				consulta += " or (solicitud.solicitante.representante.fisica.nip.valor like '%" + interesado + "%')";
				consulta += " or (s.nip.valor like '%" + interesado + "%'))"; //Los usuarios tienes id = cif
				
				String consultaRepresentantes =  "select solicitud from SolicitudGenerica solicitud " +
						  "where solicitud.solicitante.id in " +
						  "(select solicitante.id from Solicitante solicitante " +
								  "where solicitante.representante.id in " +
								  "(select representante.id from RepresentantePersonaJuridica representante " +
					                      "where (representante.juridica.entidad like '%" + interesado + "%')" +
					                             " or (representante.juridica.cif like '%" + interesado + "%')";
				
				for (int i = 1; i < listaInteresado.length; i++) {
					consulta += " or (solicitud.solicitante.fisica.nombre like '%" + listaInteresado[i] + "%')";
					consulta += " or (solicitud.solicitante.fisica.primerApellido like '%" + listaInteresado[i] + "%')";
					consulta += " or (solicitud.solicitante.fisica.segundoApellido like '%" + listaInteresado[i] + "%')";
					consulta += " or (solicitud.solicitante.fisica.nip.valor like '%" + listaInteresado[i] + "%')";
					consulta += " or (solicitud.solicitante.juridica.entidad like '%" + listaInteresado[i] + "%')";
					consulta += " or (solicitud.solicitante.juridica.cif like '%" + listaInteresado[i] + "%')";
					consulta += " or (solicitud.solicitante.representante.fisica.nombre like '%" + listaInteresado[i] + "%')";
					consulta += " or (solicitud.solicitante.representante.fisica.primerApellido like '%" + listaInteresado[i] + "%')";
					consulta += " or (solicitud.solicitante.representante.fisica.segundoApellido like '%" + listaInteresado[i] + "%')";
					consulta += " or (solicitud.solicitante.representante.fisica.nip.valor like '%" + listaInteresado[i] + "%')";
					consultaRepresentantes += " or (representante.juridica.entidad like '%" + listaInteresado[i] + "%')";
					consultaRepresentantes += " or (representante.juridica.cif like '%" + listaInteresado[i] + "%')";
				}
					
				consultaRepresentantes += " ))";
				rowsRepresentantes = SolicitudGenerica.find(consultaRepresentantes).fetch();
				andWhere = true;	
			}
			if (busqueda.estadoSolicitud != null && !busqueda.estadoSolicitud.isEmpty()) {
				String listaEstados = busqueda.estadoSolicitud.toString().replace("[", "('").replace("]", "')").replace(", ", "', '");
				if (andWhere) 
					consulta += " and ";
				consulta += " ( solicitud.estado in " +  listaEstados + " )";	
			}
			
			rows = SolicitudGenerica.find(consulta).fetch();
			if (rowsRepresentantes != null) {
				for (SolicitudGenerica sol : rowsRepresentantes) 
					if (!rows.contains(sol)) 
						rows.add(sol);
			}
		}
		
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<SolicitudGenerica> rowsFiltered = rows; //Tabla sin permisos, no filtra
		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, true, false, false, "editarSolicitud", "", "", getAccion(), ids);
	
		// "Reseteamos" la tabla de resultados de la búsqueda para la próxima vez que se utilice
		busqueda.mostrarTabla = false; 
		busqueda.solicitud = "";
		busqueda.interesado = "";
		busqueda.estadoSolicitud = null;	
		busqueda.save();
	
		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estado", "estadoValue", "estadoUsuario", "solicitante.numeroId", "solicitante.nombreCompleto"));	
	}
	
	
	@Util
	public static void busquedaFormRender() {
		Busqueda busqueda = SolicitudesController.getBusqueda();
		busqueda.mostrarTabla = true; 				// damos permiso para que se muestre la tabla de resultados
		busqueda.save();
	
		if (!Messages.hasMessages()) {
			Messages.keep();
			redirect("SolicitudesController.index", "editar");
		}		
		Messages.keep();
		redirect("SolicitudesController.index", "editar");
	}
	
	public static void tablalistaSolicitudes() {

		java.util.List<SolicitudGenerica> rows = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud").fetch();
		String consulta = "select solicitud from SolicitudGenerica solicitud join solicitud.autorizacion s where (s.nip.valor like '%" + AgenteController.getAgente().username + "%') or (solicitud.solicitante.fisica.nip.valor like '%" + AgenteController.getAgente().username + "%')";
		rows = SolicitudGenerica.find(consulta).fetch();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rows, true, false, false, "editarSolicitud", "", "", getAccion(), ids);
		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estado", "estadoValue", "estadoUsuario", "solicitante.numeroId", "solicitante.nombreCompleto"));
	
	}
}
		