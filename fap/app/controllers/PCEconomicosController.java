package controllers;

import play.*;
import play.mvc.*;
import play.db.jpa.Model;
import controllers.fap.*;
import controllers.gen.PCEconomicosControllerGen;
import validation.*;
import messages.Messages;
import messages.Messages.MessageType;
import tables.TableRecord;
import models.*;
import tags.ReflectionUtils;
import security.Accion;
import services.BaremacionService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.h2.constant.SysProperties;

@With(CheckAccessController.class)
public class PCEconomicosController extends PCEconomicosControllerGen {

	private static Logger log = Logger.getLogger("Paginas");

	public static void index(String accion, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/PCEconomicos/PCEconomicos.html");
		}

		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion)) {
			solicitud = new SolicitudGenerica();

			solicitud.save();
			idSolicitud = solicitud.id;
			((Map<String, Long>) tags.TagMapStack.top("idParams")).put(
					"idSolicitud", idSolicitud);

		} else if (!"borrado".equals(accion)) {
			solicitud = PCEconomicosController
					.getSolicitudGenerica(idSolicitud);
		}
		
		//Inicializa los conceptos economicos con los Tipos de Conceptos Economicos
		//que están definidos en la base de datos
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		if(solicitud != null && solicitud.ceconomicos.isEmpty()){
			List<TipoCEconomico> tipos = TipoCEconomico.findAll();
			int c=0;
			for(TipoCEconomico tipo : tipos){
				CEconomico ceconomico = new CEconomico();
				ceconomico.tipo = tipo;
				for (int i = 0; i < tipoEvaluacion.duracion; i++){
					ValoresCEconomico vCEconomico = new ValoresCEconomico();
					vCEconomico.initValues();
					ceconomico.valores.add(vCEconomico);
				}
				solicitud.ceconomicos.add(ceconomico);
			}
			solicitud.save();
		}
		int duracion = tipoEvaluacion.duracion;
		log.info("Visitando página: " + "fap/PCEconomicos/PCEconomicos.html");		
		renderTemplate("fap/PCEconomicos/PCEconomicos.html", accion, idSolicitud, solicitud, duracion);
	}

	public static void tablatablaCEconomicos(Long idSolicitud) {

		java.util.List<CEconomico> rows = CEconomico
				.find("select cEconomico from SolicitudGenerica solicitud join solicitud.ceconomicos cEconomico where solicitud.id=?",
						idSolicitud).fetch();

		List<CEconomico> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<CEconomico> response = new tables.TableRenderResponse<CEconomico>(rowsFiltered);
		// Para no permitir editar en la tabla los conceptos economicos que sean automaticos
		for (TableRecord<CEconomico> filaCEconomico: response.rows){
			if (filaCEconomico.objeto.tipo.clase.equals("auto"))
				filaCEconomico.permisoEditar = false;
		}
		response.mensajes.error = Messages.messages(MessageType.ERROR);
		response.mensajes.warning = Messages.messages(MessageType.WARNING);
		response.mensajes.fatal = Messages.messages(MessageType.FATAL);
		response.mensajes.ok = Messages.messages(MessageType.OK);
		response.mensajes.info = Messages.messages(MessageType.INFO);
		renderJSON(response.toJSON("tipo.nombre", "valores.valorSolicitado", "id"));
	}

	@Util
	public static List<TableRecord<CEconomico>> tablatablaCEconomicosPermisos(
			List<CEconomico> rowsFiltered) {
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack
				.top("idParams");
		List<TableRecord<CEconomico>> records = new ArrayList<TableRecord<CEconomico>>();
		Map<String, Object> vars = new HashMap<String, Object>();
		for (CEconomico cEconomico : rowsFiltered) {
			TableRecord<CEconomico> record = new TableRecord<CEconomico>();
			records.add(record);
			record.objeto = cEconomico;
			vars.put("cEconomico", cEconomico);
			record.permisoLeer = false;
			record.permisoEditar = true;
			record.permisoBorrar = false;
		}
		return records;
	}
	
	public static void editar(Long idSolicitud) {
		checkAuthenticity();
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		if (!permiso("editar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		if (!Messages.hasErrors()) {
			// CALCULAR TOTALES

			log.info("Acción Editar de página: " + "gen/PaginaDocumentoVerificacionEditar/PaginaDocumentoVerificacionEditar.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaDocumentoVerificacionEditar/PaginaDocumentoVerificacionEditar.html" + " , intentada sin éxito (Problemas de Validación)");
		PCEconomicosController.editarRender(idSolicitud);
	}
	
	@Util
	public static void editarRender(Long idSolicitud) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("PCEconomicosController.index", "editar", idSolicitud);
		}
		Messages.keep();
		redirect("PCEconomicosController.index", "editar", idSolicitud);
	}

}

		