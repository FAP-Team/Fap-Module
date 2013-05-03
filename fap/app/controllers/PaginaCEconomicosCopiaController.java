package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.h2.message.DbException;

import messages.Messages;
import models.CEconomico;
import models.CEconomicosManuales;
import models.JsonPeticionModificacion;
import models.SolicitudGenerica;
import models.TipoEvaluacion;
import models.ValoresCEconomico;
import play.mvc.Finally;
import play.mvc.Util;
import security.Secure;
import tables.TableRecord;
import utils.BaremacionUtils;
import utils.PeticionModificacion;
import validation.CustomValidation;

import com.google.gson.Gson;
import com.google.inject.Inject;

import controllers.gen.PaginaCEconomicosCopiaControllerGen;

public class PaginaCEconomicosCopiaController extends PaginaCEconomicosCopiaControllerGen {

	@Inject
	protected static Secure secure;
	
	@Finally(only="index")
	public static void end(){
		Messages.deleteFlash();
	}
	
	public static void index(String accion, Long idSolicitud, Long idCEconomico, Integer duracion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/PaginaCEconomicosCopia/PaginaCEconomicosCopia.html");
		}
		
		SolicitudGenerica solicitud = PaginaCEconomicosCopiaController.getSolicitudGenerica(idSolicitud);

		CEconomico cEconomico = null;
		if ("crear".equals(accion))
			cEconomico = PaginaCEconomicosCopiaController.getCEconomico();
		else if (!"borrado".equals(accion))
			cEconomico = PaginaCEconomicosCopiaController.getCEconomico(idSolicitud, idCEconomico);

		if (cEconomico.tipo.tipoOtro)
			calcularValoresAuto(cEconomico);
		
		log.info("Visitando página: " + "fap/PaginaCEconomicosCopia/PaginaCEconomicosCopia.html");
		
		renderTemplate("fap/PaginaCEconomicosCopia/PaginaCEconomicosCopia.html", accion, idSolicitud, idCEconomico, solicitud, cEconomico, duracion);
	}
	
	private static void calcularValoresAuto(CEconomico cEconomico){
		for (ValoresCEconomico valor: cEconomico.valores){
			valor.valorSolicitado = sumarValoresHijosOtro(cEconomico.otros, valor.anio);
		}
		cEconomico.save();
	}
	
	private static Double sumarValoresHijosOtro(List<CEconomicosManuales> listaHijosOtro, Integer anio){
		Double suma=0.0;
		for (CEconomicosManuales cEconomicoManual: listaHijosOtro){
			if (!cEconomicoManual.valores.isEmpty())
				if (cEconomicoManual.valores.get(anio).valorSolicitado != null)
					suma += cEconomicoManual.valores.get(anio).valorSolicitado;
		}
		return suma;
	}
	
	@Util
	public static void guardarValidateCopy(String accion, CEconomico dbCEconomico, CEconomico cEconomico) {
		CustomValidation.clearValidadas();
		
		// Añadiendo código de ModificacionSolicitudes
		String idSolicitud = params.get("idSolicitud");
		SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(Long.parseLong(idSolicitud));


		if (dbCEconomico.id == null) {
			dbCEconomico.save();
			params.put("idCEconomicos", dbCEconomico.id.toString());
		}

		//"Tabla dentro de tabla"
		PeticionModificacion peticionModificacionTablaGen = new PeticionModificacion();
		peticionModificacionTablaGen.campoPagina = "SolicitudGenerica.ceconomicos.valores.valorSolicitado";
		
		Map<String, String> allSimpleTabla = params.allSimple();
		for (Map.Entry<String, String> entry : allSimpleTabla.entrySet()) {
			if (entry.getKey().startsWith("id")) {
				try {
					peticionModificacionTablaGen.idSimples.put(entry.getKey(), Long.parseLong(entry.getValue()));
				} catch (Exception e) {
					//El parámetro no era un long
				}
			}
		}
		List<String> valoresAntiguosTabla = new ArrayList<String>();
		List<String> valoresNuevosTabla = new ArrayList<String>();
		
		
		if (!dbCEconomico.tipo.tipoOtro){
			valoresAntiguosTabla = new ArrayList<String>();
			int anios=0;
			if (dbCEconomico.valores != null){
				//Almacenar en valores antiguos, todos los valores que habian en el o2m valores
				for (ValoresCEconomico valor: dbCEconomico.valores){
					PeticionModificacion peticionModificacionTabla = new PeticionModificacion();
					peticionModificacionTabla.campoPagina = "SolicitudGenerica.ceconomicos.valores.valorSolicitado";
					peticionModificacionTabla.idSimples = peticionModificacionTablaGen.idSimples;
					valoresAntiguosTabla = new ArrayList<String>();
					valoresNuevosTabla = new ArrayList<String>();
					if (valor.valorSolicitado != cEconomico.valores.get(anios).valorSolicitado){
						valoresAntiguosTabla.add(valor.valorSolicitado.toString());
						valor.valorSolicitado = cEconomico.valores.get(anios).valorSolicitado;
						valoresNuevosTabla.add(valor.valorSolicitado.toString());
						peticionModificacionTabla.idSimples.put("idValoresCEconomico",valor.id);
					}
					anios++;
					// Json por todos los valores
					peticionModificacionTabla.setValorModificado("Solicitud.ceconomicos.valores.valorSolicitado", valoresAntiguosTabla, valoresNuevosTabla); // PRUEBA
					Gson gson = new Gson();
					String jsonPM = gson.toJson(peticionModificacionTabla);
					JsonPeticionModificacion jsonPeticionModificaciontabla = new JsonPeticionModificacion();
					jsonPeticionModificaciontabla.jsonPeticion = jsonPM;
					System.out.println("jsonPeticionModificaciontabla: "+jsonPeticionModificaciontabla.jsonPeticion);
					dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size() - 1).jsonPeticionesModificacion.add(jsonPeticionModificaciontabla);
					dbSolicitud.save();
				}
				
			}
		}
		
		if (!Messages.hasErrors())
			dbCEconomico.save();
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void guardar(Long idSolicitud, Long idCEconomico, CEconomico cEconomico, String botonGuardar, Integer duracion) {
		checkAuthenticity();
		if (!permisoGuardar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		CEconomico dbCEconomico = PaginaCEconomicosCopiaController.getCEconomico(idSolicitud, idCEconomico);

		PaginaCEconomicosCopiaController.guardarBindReferences(cEconomico);

		if (!Messages.hasErrors()) {
			PaginaCEconomicosCopiaController.guardarValidateCopy("editar", dbCEconomico, cEconomico);
			SolicitudGenerica solicitud = PaginaCEconomicosCopiaController.getSolicitudGenerica(idSolicitud);
			BaremacionUtils.calcularTotales(solicitud);
		}

		if (!Messages.hasErrors()) {
			PaginaCEconomicosCopiaController.guardarValidateRules(dbCEconomico, cEconomico);
		}
		if (!Messages.hasErrors()) {		
			dbCEconomico.save();
			log.info("Acción Editar de página: " + "fap/PaginaCEconomicosCopia/PaginaCEconomicosCopia.html" + " , intentada con éxito");
		} else {
			flash(cEconomico);
			log.info("Acción Editar de página: " + "fap/PaginaCEconomicosCopia/PaginaCEconomicosCopia.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		PaginaCEconomicosCopiaController.guardarRender(idSolicitud, idCEconomico, duracion);
	}
	
	@Util
	public static void guardarRender(Long idSolicitud, Long idCEconomico, Integer duracion) {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("PCEconomicosCopiaController.index", "editar", idSolicitud);
		}
		Messages.keep();
		redirect("PaginaCEconomicosCopiaController.index", "editar", idSolicitud, idCEconomico, duracion);
	}
	
	@Util
	private static void flash(CEconomico dbCEconomico){
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		String param = "cEconomico";
		if (dbCEconomico.tipo.tipoOtro){
			for (int i = 0; i < tipoEvaluacion.duracion; i++){
				Messages.setFlash(param + ".valores["+i+"].valorEstimado", dbCEconomico.valores.get(i).valorEstimado);
				Messages.setFlash(param + ".valores["+i+"].valorSolicitado", dbCEconomico.valores.get(i).valorSolicitado);
				Messages.setFlash(param + ".valores["+i+"].valorPropuesto", dbCEconomico.valores.get(i).valorPropuesto);
				Messages.setFlash(param + ".valores["+i+"].valorConcedido", dbCEconomico.valores.get(i).valorConcedido);
			}
			Messages.setFlash(param + ".comentariosAdministracion", dbCEconomico.comentariosAdministracion);
			Messages.setFlash(param + ".comentariosSolicitante", dbCEconomico.comentariosSolicitante);
		} else {
			for (int i = 0; i < tipoEvaluacion.duracion; i++){
				Messages.setFlash(param + ".valores["+i+"].valorEstimado", params.get(param + ".valores["+i+"].valorEstimado", String.class));
				Messages.setFlash(param + ".valores["+i+"].valorSolicitado", params.get(param + ".valores["+i+"].valorSolicitado", String.class));
				Messages.setFlash(param + ".valores["+i+"].valorPropuesto", params.get(param + ".valores["+i+"].valorPropuesto", String.class));
				Messages.setFlash(param + ".valores["+i+"].valorConcedido", params.get(param + ".valores["+i+"].valorConcedido", String.class));
			}
			Messages.setFlash(param + ".comentariosAdministracion", params.get(param + ".comentariosAdministracion", String.class));
			Messages.setFlash(param + ".comentariosSolicitante", params.get(param + ".comentariosSolicitante", String.class));
		}
	}
	
	public static void tablaceconomicosManuales(Long idCEconomico) {

		java.util.List<CEconomicosManuales> rows = CEconomicosManuales.find("select cEconomicosManuales from CEconomico cEconomico join cEconomico.otros cEconomicosManuales where cEconomico.id=?", idCEconomico).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		
		List<CEconomicosManuales> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<CEconomicosManuales> response = new tables.TableRenderResponse<CEconomicosManuales>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		
		for (TableRecord<CEconomicosManuales> filaCEconomico: response.rows){
			if (tipoEvaluacion.estado.equals("iniciada"))
				filaCEconomico.permisoEditar = false;
			else if (!permiso("editar")){
				filaCEconomico.permisoEditar = false;
				filaCEconomico.permisoLeer = true;
			}
		}
		
		renderJSON(response.toJSON("tipo.jerarquia", "tipo.nombre", "id", "valores.valorSolicitado_formatFapTabla"));
	}
	
}
