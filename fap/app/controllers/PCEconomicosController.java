package controllers;

import play.*;
import play.mvc.*;
import play.db.jpa.Model;
import baremacion.BaremacionFAP;
import controllers.fap.*;
import controllers.gen.PCEconomicosControllerGen;
import utils.BaremacionUtils;
import utils.ModelUtils;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.ivy.util.Message;
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
		if (tipoEvaluacion != null){
			if(solicitud != null && solicitud.ceconomicos.isEmpty()){
				List<TipoCEconomico> tipos = TipoCEconomico.findAll();
				for(TipoCEconomico tipo : tipos){
					if (tipo.creadoUsuario == null || !tipo.creadoUsuario) {
						CEconomico ceconomico = new CEconomico();
						ceconomico.tipo = tipo;
						for (int i = 0; i < tipoEvaluacion.duracion; i++){
							ValoresCEconomico vCEconomico = new ValoresCEconomico(i);
							vCEconomico.initValues(i);
							ceconomico.valores.add(vCEconomico);
						}
						solicitud.ceconomicos.add(ceconomico);
					}
				}
				solicitud.save();
			}
			BaremacionUtils.calcularTotales(solicitud);
			int duracion = tipoEvaluacion.duracion;
			log.info("Visitando página: " + "fap/PCEconomicos/PCEconomicos.html");		
			renderTemplate("fap/PCEconomicos/PCEconomicos.html", accion, idSolicitud, solicitud, duracion);
		} else {
			Messages.fatal("Los Conceptos Económicos todavía no están disponibles en la aplicación");
			int duracion=1; // Para que no reviente el HTML, al intentar buscar una variable duracion dentro.
			renderTemplate("fap/PCEconomicos/PCEconomicos.html", accion, idSolicitud, solicitud, duracion);
		}
	}

	public static void tablatablaCEconomicos(Long idSolicitud) {

		java.util.List<CEconomico> rows = CEconomico
				.find("select cEconomico from SolicitudGenerica solicitud join solicitud.ceconomicos cEconomico where solicitud.id=?",
						idSolicitud).fetch();

		List<CEconomico> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<CEconomico> response = new tables.TableRenderResponse<CEconomico>(rowsFiltered);
		// Para no permitir editar en la tabla los conceptos economicos que sean automaticos
		for (TableRecord<CEconomico> filaCEconomico: response.rows){
			if ((filaCEconomico.objeto.tipo.clase!=null) && (filaCEconomico.objeto.tipo.clase.equals("auto")) && (!filaCEconomico.objeto.tipo.tipoOtro)){
				filaCEconomico.permisoEditar = false;
				filaCEconomico.permisoLeer = false;
		    } else if (!permiso("editar")){
				filaCEconomico.permisoEditar = false;
				filaCEconomico.permisoLeer = true;
			}
		}
		response.mensajes.error = Messages.messages(MessageType.ERROR);
		response.mensajes.warning = Messages.messages(MessageType.WARNING);
		response.mensajes.fatal = Messages.messages(MessageType.FATAL);
		response.mensajes.ok = Messages.messages(MessageType.OK);
		response.mensajes.info = Messages.messages(MessageType.INFO);
		renderJSON(response.toJSON("tipo.nombre", "tipo.jerarquia", "total_formatFapTabla","valores.valorSolicitado_formatFapTabla", "id"));
	}

	@Util
	public static List<TableRecord<CEconomico>> tablatablaCEconomicosPermisos(List<CEconomico> rowsFiltered) {
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
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void guardar(Long idSolicitud, String botonGuardar) {
		checkAuthenticity();
		if (!permisoGuardar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
		if (!Messages.hasErrors()) {
			Class invokedClass = null;
			//Busca una clase que herede de BaremacionFAP
	        List<Class> assignableClasses = Play.classloader.getAssignableClasses(BaremacionFAP.class);
	        if(assignableClasses.size() > 0){
	            invokedClass = assignableClasses.get(0);
	        } else {
	        	invokedClass = BaremacionFAP.class;
	        }
	        if (invokedClass != null){
				Method method = null;
				try {
					method = invokedClass.getDeclaredMethod("validarCEconomicos", long.class, List.class);
				} catch (Exception e) {
					play.Logger.error("Error g001: No se ha podido encontrar el método validarCEconomicos de la clase BaremacionFAP");
					Messages.error("Error interno g001. No se ha podido Guardar correctamente");
				}
				if (!Messages.hasErrors()) {
					if (method != null){
						try {
							List<CEconomico> ceconomicos = solicitud.ceconomicos;
							method.invoke(PCEconomicosController.class, idSolicitud, ceconomicos);
						} catch (Exception e) {
							play.Logger.error("Error g002: No se ha podido invocar el método validarCEconomicos de la clase BaremacionFAP");
							Messages.error("Error interno g002. No se ha podido Guardar correctamente");
						} 
					} else{
						play.Logger.error("Error g003: No existe el Método apropiado para validar los CEconomicos. El método debe llamarse 'validarCEconomicos()'");
						Messages.error("Error interno g003. No se ha podido Guardar correctamente");
					}
				}
			} else{
				play.Logger.error("Error g004: No existe la Clase apropiada para iniciar la Baremacion. La clase debe extender de 'BaremacionFAP'");
				Messages.error("Error interno g004. No se ha podido Guardar correctamente");
			}
		}

		if (!Messages.hasErrors()) {
			PCEconomicosController.guardarValidateRules();
		}
		if (!Messages.hasErrors()) {
			Object miSavePages = ModelUtils.invokeMethodClass(SolicitudGenerica.class, solicitud, "getSavePages");
			ModelUtils.invokeMethodClass(miSavePages.getClass(), miSavePages, "setPaginaPCEconomicos", true);
			ModelUtils.invokeMethodClass(miSavePages.getClass(), miSavePages, "save");
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PCEconomicos/PCEconomicos.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PCEconomicos/PCEconomicos.html" + " , intentada sin éxito (Problemas de Validación)");
		PCEconomicosController.guardarRender(idSolicitud);
	}

}

		