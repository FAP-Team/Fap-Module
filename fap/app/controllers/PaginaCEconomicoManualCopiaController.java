package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import messages.Messages;
import models.CEconomico;
import models.CEconomicosManuales;
import models.JsonPeticionModificacion;
import models.SolicitudGenerica;
import models.TipoCEconomico;
import models.TipoEvaluacion;
import models.ValoresCEconomico;
import play.mvc.Util;
import utils.ModelUtils;
import utils.PeticionModificacion;
import validation.CustomValidation;
import controllers.gen.PaginaCEconomicoManualCopiaControllerGen;

public class PaginaCEconomicoManualCopiaController extends PaginaCEconomicoManualCopiaControllerGen {
	public static void index(String accion, Long idSolicitud, Long idCEconomico, Long idCEconomicosManuales) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/PaginaCEconomicoManualCopia/PaginaCEconomicoManualCopia.html");
		}

		SolicitudGenerica solicitud = PaginaCEconomicoManualCopiaController.getSolicitudGenerica(idSolicitud);
		CEconomico cEconomico = PaginaCEconomicoManualCopiaController.getCEconomico(idSolicitud, idCEconomico);

		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		Integer duracion = tipoEvaluacion.duracion-1;

		CEconomicosManuales cEconomicosManuales = null;
		if ("crear".equals(accion)) {
			cEconomicosManuales = PaginaCEconomicoManualCopiaController.getCEconomicosManuales();
			cEconomicosManuales.valores = new ArrayList<ValoresCEconomico>();
			for (int i=0; i <= duracion; i++) {
				ValoresCEconomico vCE = new ValoresCEconomico(i);
				cEconomicosManuales.valores.add(vCE);
			}
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				cEconomicosManuales.save();
				idCEconomicosManuales = cEconomicosManuales.id;
				cEconomico.otros.add(cEconomicosManuales);
				cEconomico.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion)){
				cEconomicosManuales = PaginaCEconomicoManualCopiaController.getCEconomicosManuales(idCEconomico, idCEconomicosManuales);
				if (cEconomicosManuales == null){
					cEconomicosManuales = getFlashCEconomicosManuales();
					accion= "crear";
					Messages.clear();
				}
		}

		log.info("Visitando página: " + "fap/PaginaCEconomicoManualCopia/PaginaCEconomicoManualCopia.html");
		renderTemplate("fap/PaginaCEconomicoManualCopia/PaginaCEconomicoManualCopia.html", accion, idSolicitud, idCEconomico, idCEconomicosManuales, solicitud, cEconomico, cEconomicosManuales, duracion);

	}

	@Util
	public static void guardarPCEValidateCopy(String accion, CEconomicosManuales dbCEconomicosManuales, CEconomicosManuales cEconomicosManuales) {
		CustomValidation.clearValidadas();
		if (secure.checkGrafico("modificacionTrasPresentacionDeSolicitud", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("cEconomicosManuales", cEconomicosManuales);

			// Código de Copia
			// (1) Caso de modificacion (Existe y lo cambio) (2) Caso de Creacion (Lo creo desde 0)

			String idSolicitud = params.get("idSolicitud");
			SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(Long.parseLong(idSolicitud));

			PeticionModificacion peticionModificacion = new PeticionModificacion();
			peticionModificacion.campoPagina = "Solicitud.ceconomicos.otros"; 

			Map<String, String> allSimpleTabla = params.allSimple();
			for (Map.Entry<String, String> entry : allSimpleTabla.entrySet()) {
				if (entry.getKey().startsWith("id")) {
					try {
						peticionModificacion.idSimples.put(entry.getKey(), Long.parseLong(entry.getValue()));
					} catch (Exception e) {
						//El parámetro no era un long
					}
				}
			}
			peticionModificacion.idSimples.put("idCEconomicosManuales",dbCEconomicosManuales.id);
			List<String> valoresAntiguos = new ArrayList<String>();
			List<String> valoresNuevos = new ArrayList<String>();
			Boolean hayModificaciones = false;

			//Validando el tipo.nombre 
			CustomValidation.valid("cEconomicosManuales.tipo", cEconomicosManuales.tipo);
			CustomValidation.required("cEconomicosManuales.tipo.nombre", cEconomicosManuales.tipo.nombre);

			//Código de Copia del tipo.nombre
			if (((dbCEconomicosManuales.tipo.nombre != null) && (cEconomicosManuales.tipo.nombre.toString() != null) && (!dbCEconomicosManuales.tipo.nombre.toString().equals(cEconomicosManuales.tipo.nombre.toString()))) || ((cEconomicosManuales.tipo.nombre == null) && (!cEconomicosManuales.tipo.nombre.equals(dbCEconomicosManuales.tipo.nombre)))) {
				valoresAntiguos = new ArrayList<String>();
				if (dbCEconomicosManuales.tipo.nombre != null)
					valoresAntiguos.add(dbCEconomicosManuales.tipo.nombre.toString());
				valoresNuevos = new ArrayList<String>();
				valoresNuevos.add(cEconomicosManuales.tipo.nombre.toString());
				peticionModificacion.setValorModificado("Solicitud.ceconomicos.otros.tipo.nombre", valoresAntiguos, valoresNuevos);
				hayModificaciones = true;
			}
			dbCEconomicosManuales.tipo.nombre = cEconomicosManuales.tipo.nombre;

			//Código de Copia del tipo.descripcion
			if (((dbCEconomicosManuales.tipo.descripcion != null) && (cEconomicosManuales.tipo.descripcion.toString() != null) && (!dbCEconomicosManuales.tipo.descripcion.toString().equals(cEconomicosManuales.tipo.descripcion.toString()))) || ((cEconomicosManuales.tipo.descripcion == null) && (!cEconomicosManuales.tipo.descripcion.equals(dbCEconomicosManuales.tipo.descripcion)))) {
				valoresAntiguos = new ArrayList<String>();
				if (dbCEconomicosManuales.tipo.descripcion != null)
					valoresAntiguos.add(dbCEconomicosManuales.tipo.descripcion.toString());
				valoresNuevos = new ArrayList<String>();
				valoresNuevos.add(cEconomicosManuales.tipo.descripcion.toString());
				peticionModificacion.setValorModificado("Solicitud.ceconomicos.otros.tipo.descripcion", valoresAntiguos, valoresNuevos);
				hayModificaciones = true;
			}
			dbCEconomicosManuales.tipo.descripcion = cEconomicosManuales.tipo.descripcion;

			//Código de Copia del comentarioAdministracion
			if (((dbCEconomicosManuales.comentariosAdministracion != null) && (cEconomicosManuales.comentariosAdministracion.toString() != null) && (!dbCEconomicosManuales.comentariosAdministracion.toString().equals(cEconomicosManuales.comentariosAdministracion.toString()))) || ((cEconomicosManuales.comentariosAdministracion == null) && (!cEconomicosManuales.comentariosAdministracion.equals(dbCEconomicosManuales.comentariosAdministracion)))) {
				valoresAntiguos = new ArrayList<String>();
				if (dbCEconomicosManuales.comentariosAdministracion != null)
					valoresAntiguos.add(dbCEconomicosManuales.comentariosAdministracion.toString());
				valoresNuevos = new ArrayList<String>();
				valoresNuevos.add(cEconomicosManuales.comentariosAdministracion.toString());
				peticionModificacion.setValorModificado("Solicitud.ceconomicos.otros.comentariosAdministracion", valoresAntiguos, valoresNuevos);
				hayModificaciones = true;
			}
			dbCEconomicosManuales.comentariosAdministracion = cEconomicosManuales.comentariosAdministracion;

			//Código de Copia del comentarioSolicitante
			if (((dbCEconomicosManuales.comentariosSolicitante != null) && (cEconomicosManuales.comentariosSolicitante.toString() != null) && (!dbCEconomicosManuales.comentariosSolicitante.toString().equals(cEconomicosManuales.comentariosSolicitante.toString()))) || ((cEconomicosManuales.comentariosSolicitante == null) && (!cEconomicosManuales.comentariosSolicitante.equals(dbCEconomicosManuales.comentariosSolicitante)))) {
				valoresAntiguos = new ArrayList<String>();
				if (dbCEconomicosManuales.comentariosSolicitante != null)
					valoresAntiguos.add(dbCEconomicosManuales.comentariosSolicitante.toString());
				valoresNuevos = new ArrayList<String>();
				valoresNuevos.add(cEconomicosManuales.comentariosSolicitante.toString());
				peticionModificacion.setValorModificado("Solicitud.ceconomicos.otros.comentariosSolicitante", valoresAntiguos, valoresNuevos);
				hayModificaciones = true;
			}
			dbCEconomicosManuales.comentariosSolicitante = cEconomicosManuales.comentariosSolicitante;

			//Guardo en el json los valores simples:
			// Json por todos los valores
			if (hayModificaciones){
				//peticionModificacion.setValorModificado("Solicitud.ceconomicos.otros", valoresAntiguos, valoresNuevos);
				peticionModificacion.idSimples.put("idCEconomicosManuales",dbCEconomicosManuales.id);
				Gson gson = new Gson();
				String jsonPM = gson.toJson(peticionModificacion);
				JsonPeticionModificacion jsonPeticionModificacion = new JsonPeticionModificacion();
				jsonPeticionModificacion.jsonPeticion = jsonPM;
				dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size() - 1).jsonPeticionesModificacion.add(jsonPeticionModificacion);
				dbSolicitud.save();
			}

			//Copia de los valores: O2M Un json por cada valor

			//Logica de copia a BBDD
			List<String> valoresAntiguosTabla = new ArrayList<String>();
			List<String> valoresNuevosTabla = new ArrayList<String>();

			if ((dbCEconomicosManuales.valores == null) || (dbCEconomicosManuales.valores.size() == 0)) { //Dead code?
				//peticionModificacion.idSimples.put("idCEconomicosManuales",dbCEconomicosManuales.id); //Viernes añadido
				//Guardo todos los nuevos valores
				for (int i=0; i < cEconomicosManuales.valores.size(); i++) { //Modificando
					valoresNuevosTabla = new ArrayList<String>();
					valoresNuevosTabla.add(cEconomicosManuales.valores.get(i).valorSolicitado.toString());

					// Json por todos los valores
					PeticionModificacion peticionModificacionTabla = new PeticionModificacion();
					peticionModificacionTabla.campoPagina = "Solicitud.ceconomicos.otros.valores.valorSolicitado";
					peticionModificacionTabla.idSimples = peticionModificacion.idSimples;
					peticionModificacionTabla.idSimples.put("idValoresCEconomicoManuales",cEconomicosManuales.valores.get(i).id);

					peticionModificacionTabla.setValorCreado("Solicitud.ceconomicos.otros.valores.valorSolicitado", new ArrayList<String>(), valoresNuevosTabla); // PRUEBA
					Gson gsonTabla = new Gson();
					String jsonPMTabla = gsonTabla.toJson(peticionModificacionTabla);
					JsonPeticionModificacion jsonPeticionModificaciontabla = new JsonPeticionModificacion();
					jsonPeticionModificaciontabla.jsonPeticion = jsonPMTabla;
					dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size() - 1).jsonPeticionesModificacion.add(jsonPeticionModificaciontabla);
					dbSolicitud.save();
				}
				dbCEconomicosManuales.valores = cEconomicosManuales.valores;

			}
			else {
				for (int i=0; i < cEconomicosManuales.valores.size(); i++) { //Modificando
					valoresAntiguosTabla = new ArrayList<String>();
					valoresNuevosTabla = new ArrayList<String>();
					hayModificaciones = false;

					PeticionModificacion peticionModificacionTabla = new PeticionModificacion();
					peticionModificacionTabla.campoPagina = "Solicitud.ceconomicos.otros.valores.valorSolicitado";
					peticionModificacionTabla.idSimples = peticionModificacion.idSimples;


					if ((cEconomicosManuales.valores.get(i).valorSolicitado != null) &&(cEconomicosManuales.valores.get(i).valorSolicitado.toString() != dbCEconomicosManuales.valores.get(i).valorSolicitado.toString())){
						valoresAntiguosTabla.add(dbCEconomicosManuales.valores.get(i).valorSolicitado.toString());
						dbCEconomicosManuales.valores.get(i).valorSolicitado = cEconomicosManuales.valores.get(i).valorSolicitado; //Logica 
						valoresNuevosTabla.add(cEconomicosManuales.valores.get(i).valorSolicitado.toString());
						peticionModificacionTabla.idSimples.put("idValoresCEconomico",dbCEconomicosManuales.valores.get(i).id); //AQUI idValoresCEconomo

						//Json
						peticionModificacionTabla.setValorModificado("Solicitud.ceconomicos.otros.valores.valorSolicitado", valoresAntiguosTabla, valoresNuevosTabla); // PRUEBA
						Gson gsonTabla = new Gson();
						String jsonPMTabla = gsonTabla.toJson(peticionModificacionTabla);
						JsonPeticionModificacion jsonPeticionModificaciontabla = new JsonPeticionModificacion();
						jsonPeticionModificaciontabla.jsonPeticion = jsonPMTabla;
						dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size() - 1).jsonPeticionesModificacion.add(jsonPeticionModificaciontabla);
						dbSolicitud.save();
					}
				}
			}
			//dbCEconomicosManuales.valores=cEconomicosManuales.valores;
		}

	}



	@Util
	public static void guardarPCERender(Long idSolicitud, Long idCEconomico, Long idCEconomicosManuales) {
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		Integer duracion = tipoEvaluacion.duracion-1;
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("PaginaCEconomicosCopiaController.index", controllers.PaginaCEconomicosCopiaController.getAccion(), idSolicitud, idCEconomico, duracion);
		}
		Messages.keep();
		redirect("PaginaCEconomicoManualCopiaController.index", "editar", idSolicitud, idCEconomico, idCEconomicosManuales);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void guardarPCE(Long idSolicitud, Long idCEconomico, Long idCEconomicosManuales, CEconomicosManuales cEconomicosManuales, String bGuardarPCE) {
		checkAuthenticity();
		if (!permisoGuardarPCE("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		CEconomico dbCEconomico = PaginaCEconomicoManualCopiaController.getCEconomico(idSolicitud, idCEconomico);
		CEconomicosManuales dbCEconomicosManuales;
		if (idCEconomicosManuales == null) {			
			dbCEconomicosManuales = PaginaCEconomicoManualCopiaController.getCEconomicosManuales();
			CustomValidation.required("cEconomicosManuales.tipo.nombre", cEconomicosManuales.tipo.nombre);
			if (!Messages.hasErrors()){
				// Creamos el Tipo ConceptoEconomico por defecto (el solicitante solo podrá cambiar el atributo nombre)
				TipoCEconomico tipoCEconomico = new TipoCEconomico();
				tipoCEconomico.clase="manual";
				tipoCEconomico.comentariosAdministracion=true;
				tipoCEconomico.comentariosSolicitante=true;
				tipoCEconomico.descripcion=cEconomicosManuales.tipo.descripcion;
				tipoCEconomico.nombre=cEconomicosManuales.tipo.nombre;
				tipoCEconomico.instrucciones=null; //"Instrucciones";
				tipoCEconomico.tipoOtro=false;
				tipoCEconomico.creadoUsuario=true;
				tipoCEconomico.jerarquia=dbCEconomico.tipo.jerarquia+"."+(dbCEconomico.otros.size()+1);
				tipoCEconomico.save();
				TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
				tipoEvaluacion.ceconomicos.add(tipoCEconomico);
				tipoEvaluacion.save();

				dbCEconomicosManuales.tipo = tipoCEconomico;
				dbCEconomicosManuales.save();

				CreandoEnModificacionSolicitudes("editar", dbCEconomicosManuales, cEconomicosManuales);

				idCEconomicosManuales = dbCEconomicosManuales.id;
				dbCEconomico.otros.add(dbCEconomicosManuales);
			}
		} else {
			dbCEconomicosManuales = PaginaCEconomicoManualCopiaController.getCEconomicosManuales(idCEconomico, idCEconomicosManuales);
		}
		PaginaCEconomicoManualCopiaController.guardarPCEBindReferences(cEconomicosManuales);

		if (!Messages.hasErrors()) {
			PaginaCEconomicoManualCopiaController.guardarPCEValidateCopy("editar", dbCEconomicosManuales, cEconomicosManuales);
		}

		if (!Messages.hasErrors()) {
			PaginaCEconomicoManualCopiaController.guardarPCEValidateRules(dbCEconomicosManuales, cEconomicosManuales);
		}
		if (!Messages.hasErrors()) {
			SolicitudGenerica solicitud = PaginaCEconomicosController.getSolicitudGenerica(idSolicitud);
			Object miSavePages = ModelUtils.invokeMethodClass(SolicitudGenerica.class, solicitud, "getSavePagesCopy");
			ModelUtils.invokeMethodClass(miSavePages.getClass(), miSavePages, "setPaginaPCEconomicosCopia", false);
			ModelUtils.invokeMethodClass(miSavePages.getClass(), miSavePages, "save");
			dbCEconomico.save();
			dbCEconomicosManuales.save();
			idCEconomicosManuales = dbCEconomicosManuales.id;
			log.info("Acción Editar de página: " + "gen/PaginaCEconomicoManualCopia/PaginaCEconomicoManualCopia.html" + " , intentada con éxito");
		} else{
			flash(cEconomicosManuales);
			log.info("Acción Editar de página: " + "gen/PaginaCEconomicoManualCopia/PaginaCEconomicoManualCopia.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		PaginaCEconomicoManualCopiaController.guardarPCERender(idSolicitud, idCEconomico, idCEconomicosManuales);
	}


	public static void CreandoEnModificacionSolicitudes(String accion, CEconomicosManuales dbCEconomicosManuales, CEconomicosManuales cEconomicosManuales) {

		String idSolicitud = params.get("idSolicitud");
		SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(Long.parseLong(idSolicitud));

		PeticionModificacion peticionModificacion = new PeticionModificacion();
		peticionModificacion.campoPagina = "Solicitud.ceconomicos.otros"; //OJO

		Map<String, String> allSimpleTabla = params.allSimple();
		for (Map.Entry<String, String> entry : allSimpleTabla.entrySet()) {
			if (entry.getKey().startsWith("id")) {
				try {
					peticionModificacion.idSimples.put(entry.getKey(), Long.parseLong(entry.getValue()));
				} catch (Exception e) {
					//El parámetro no era un long
				}
			}
		}
		List<String> valoresNuevos = new ArrayList<String>();

			if (cEconomicosManuales.tipo.nombre != null) {
				valoresNuevos = new ArrayList<String>();
				valoresNuevos.add(cEconomicosManuales.tipo.nombre.toString());
				peticionModificacion.setValorCreado("Solicitud.ceconomicos.otros.tipo.nombre", new ArrayList<String>(), valoresNuevos);
				dbCEconomicosManuales.tipo.nombre = cEconomicosManuales.tipo.nombre;
			}

			if (cEconomicosManuales.tipo.descripcion!= null) {
				valoresNuevos = new ArrayList<String>();
				valoresNuevos.add(cEconomicosManuales.tipo.descripcion.toString());
				peticionModificacion.setValorCreado("Solicitud.ceconomicos.otros.tipo.descripcion", new ArrayList<String>(), valoresNuevos);
				dbCEconomicosManuales.tipo.descripcion = cEconomicosManuales.tipo.descripcion;
			}

			if (cEconomicosManuales.comentariosAdministracion!= null) {
				valoresNuevos = new ArrayList<String>();
				valoresNuevos.add(cEconomicosManuales.tipo.comentariosAdministracion.toString());
				peticionModificacion.setValorCreado("Solicitud.ceconomicos.otros.comentariosAdministracion", new ArrayList<String>(), valoresNuevos);
				dbCEconomicosManuales.comentariosAdministracion = cEconomicosManuales.comentariosAdministracion;
			}

			if (cEconomicosManuales.comentariosSolicitante!= null) {
				valoresNuevos = new ArrayList<String>();
				valoresNuevos.add(cEconomicosManuales.tipo.comentariosSolicitante.toString());
				peticionModificacion.setValorCreado("Solicitud.ceconomicos.otros.comentariosSolicitante", new ArrayList<String>(), valoresNuevos);
				dbCEconomicosManuales.comentariosSolicitante = cEconomicosManuales.comentariosSolicitante;
			}

			peticionModificacion.idSimples.put("idCEconomicosManuales",dbCEconomicosManuales.id);
			Gson gson = new Gson();
			String jsonPM = gson.toJson(peticionModificacion);
			JsonPeticionModificacion jsonPeticionModificacion = new JsonPeticionModificacion();
			jsonPeticionModificacion.jsonPeticion = jsonPM;
			dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size() - 1).jsonPeticionesModificacion.add(jsonPeticionModificacion);
			dbSolicitud.save();
	}

	@Util
	public static CEconomicosManuales getCEconomicosManuales() {
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		return new CEconomicosManuales(tipoEvaluacion.duracion);
	}

	@Util
	private static void flash(CEconomicosManuales dbCEconomicoManual){
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		String param = "cEconomicosManuales";
		if (dbCEconomicoManual.tipo.tipoOtro){
			for (int i = 0; i < tipoEvaluacion.duracion; i++){
				Messages.setFlash(param + ".valores["+i+"].valorEstimado", dbCEconomicoManual.valores.get(i).valorEstimado);
				Messages.setFlash(param + ".valores["+i+"].valorSolicitado", dbCEconomicoManual.valores.get(i).valorSolicitado);
				Messages.setFlash(param + ".valores["+i+"].valorPropuesto", dbCEconomicoManual.valores.get(i).valorPropuesto);
				Messages.setFlash(param + ".valores["+i+"].valorConcedido", dbCEconomicoManual.valores.get(i).valorConcedido);
			}
			Messages.setFlash(param + ".comentariosAdministracion", dbCEconomicoManual.comentariosAdministracion);
			Messages.setFlash(param + ".comentariosSolicitante", dbCEconomicoManual.comentariosSolicitante);
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

	private static CEconomicosManuales getFlashCEconomicosManuales (){
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		CEconomicosManuales cEconomicosManuales = PaginaCEconomicoManualCopiaController.getCEconomicosManuales();
		cEconomicosManuales.valores = new ArrayList<ValoresCEconomico>();
		String param = "cEconomicosManuales";
		for (int i=0; i <= tipoEvaluacion.duracion; i++) {
			ValoresCEconomico vCE = new ValoresCEconomico(i);
			//vCE.valorConcedido = Double.parseDouble(Messages.getFlash(param + ".valores["+i+"].valorConcedido").toString());
			//vCE.valorPropuesto = Double.parseDouble(Messages.getFlash(param + ".valores["+i+"].valorPropuesto").toString());
			// Recupera un 'Y' por ejemplo, y no la puede parsear a DOUBLE!!!!!!!
			try {
				vCE.valorSolicitado = Double.parseDouble(Messages.getFlash(param + ".valores["+i+"].valorSolicitado").toString());
			} catch (Exception e){
				vCE.valorSolicitado = null;
			}
			//vCE.valorEstimado = Double.parseDouble(Messages.getFlash(param + ".valores["+i+"].valorEstimado").toString());
			cEconomicosManuales.valores.add(vCE);
		}
		//cEconomicosManuales.comentariosAdministracion=Messages.getFlash(param + ".comentariosAdministracion").toString();
	    //cEconomicosManuales.comentariosSolicitante=Messages.getFlash(param + ".comentariosSolicitante").toString();
	    return cEconomicosManuales;
	}
}