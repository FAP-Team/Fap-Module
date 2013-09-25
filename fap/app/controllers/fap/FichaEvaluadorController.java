package controllers.fap;


import java.util.HashMap;

import static play.modules.pdf.PDF.renderPDF;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.inject.spi.Message;

import baremacion.BaremacionFAP;

import enumerado.fap.gen.EstadosDocumentoVerificacionEnum;
import enumerado.fap.gen.EstadosEvaluacionEnum;
import format.FapFormat;

import messages.Messages;
import messages.Messages.MessageType;
import models.CEconomico;
import models.CEconomicosManuales;
import models.Criterio;
import models.CriterioListaValores;
import models.Documento;
import models.Evaluacion;
import models.ObligatoriedadDocumentos;
import models.SolicitudGenerica;
import models.TipoCEconomico;
import models.TipoCriterio;
import models.TipoDocumentoAccesible;
import models.TipoEvaluacion;
import models.VerificacionDocumento;
import play.Play;
import play.data.validation.Validation;
import play.db.jpa.JPABase;
import play.modules.pdf.PDF.Options;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Router;
import play.mvc.Util;
import play.mvc.Router.ActionDefinition;
import play.mvc.Scope.Flash;
import play.mvc.Scope.Params;
import play.mvc.With;
import properties.FapProperties;
import reports.Report;
import security.Secure;
import services.BaremacionService;
import tables.TableRecord;
import utils.BaremacionUtils;
import utils.ModelUtils;
import verificacion.ObligatoriedadDocumentosFap;
import validation.CustomValidation;

@With({SecureController.class, AgenteController.class, CheckAccessController.class})
public class FichaEvaluadorController extends Controller {

	@Inject
	protected static Secure secure;

	@Finally(only="index")
	public static void end(){
		Messages.deleteFlash();
	}

	public static void index(Long idEvaluacion, String accion){
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		if(secure.checkGrafico("accesoEvaluacion", "visible", "leer", ids, null)){
			TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
			Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
			if (evaluacion != null) {
				BaremacionUtils.ordenarTiposCEconomicos(evaluacion.tipo.ceconomicos);
				BaremacionUtils.ordenarCEconomicos(evaluacion.ceconomicos);
				BaremacionUtils.ordenarTiposCriterios(evaluacion.tipo.criterios);
				BaremacionUtils.ordenarCriterios(evaluacion.criterios);
			}
			notFoundIfNull(evaluacion);
			String expedienteUrl = redirectToFirstPage(evaluacion.solicitud.id);
			int duracion = tipoEvaluacion.duracion-1;
			// Stupid hack
			boolean admin = "administradorgestor".contains(AgenteController.getAgente().rolActivo);

			if (!EstadosEvaluacionEnum.evaluada.name().equals(evaluacion.estado)) {
				BaremacionService.calcularTotales(evaluacion, admin, true);
			}
			boolean puedoGuardar = secure.checkGrafico("guardarEvaluacion", "editable", accion, ids, null);
			renderTemplate("fap/Baremacion/fichaEvaluador.html", evaluacion, expedienteUrl, duracion, idEvaluacion, accion, puedoGuardar);
		}else{
			play.Logger.error("No se cumple el permiso \"accesoEvaluacion\" con ids: "+ids);
			forbidden();
		}
	}

	public static void tabladocumentosAccesiblesEvaluador(Long idSolicitud, Long idEvaluacion) {

		java.util.List<Documento> rows = new ArrayList<Documento>();
		Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
		if (TipoDocumentoAccesible.count() > 0){
			List<TipoDocumentoAccesible> tiposDocumentosAccesibles = TipoDocumentoAccesible.findAll();
			boolean encontrado;
			SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(idSolicitud);
			List<Documento> documentosAportados = (List<Documento>) ModelUtils.invokeMethodClassStatic(BaremacionFAP.class, "getDocumentosAccesibles", idSolicitud, idEvaluacion);
			for (TipoDocumentoAccesible tipo: tiposDocumentosAccesibles){
				encontrado = false;
				for (int i=dbSolicitud.verificaciones.size()-1; i>=0; i--){
					for (VerificacionDocumento documento: dbSolicitud.verificaciones.get(i).documentos){
						if ((ObligatoriedadDocumentosFap.eliminarVersionUri(documento.uriTipoDocumento).equals(ObligatoriedadDocumentosFap.eliminarVersionUri(tipo.uri))) && (documento.estadoDocumentoVerificacion.equals(EstadosDocumentoVerificacionEnum.valido.name()))){
							if (documentosAportados != null){
								for (Documento doc: documentosAportados){
									if ((doc.uri != null) && (doc.uri.equals(documento.uriDocumento))){
										rows.add(doc);
										encontrado = true;
										//break;
									}	
								}
							} else {
								play.Logger.error("No existe ningun documento en la lista de documentos Accesibles para buscar los del tipo requerido en la Evaluación.");
							}
							//break;
						}
					}
//					if (encontrado)
//						break;
				}
			}
		}
		// Siempre se añade el documento solicitud evaluación
		if (evaluacion.solicitudEnEvaluacion.uri != null)
			rows.add(evaluacion.solicitudEnEvaluacion);

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Documento> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered, false, false, false, "", "", "", "editar", ids);

		renderJSON(response.toJSON("fechaRegistro", "descripcionVisible", "tipo", "urlDescarga", "id"));
	}

	@Util
	private static String redirectToFirstPage(Long idSolicitud) {
		//Url Primera pagina de la solicitud
		String firstPage = FapProperties.get("fap.app.firstPage"); 
		if(firstPage == null){
			play.Logger.error("No está definida la property fap.app.firstPage que identifica la primera página.");
			Messages.fatal("Se ha producido un error");
		}
		String expedienteUrl = Router.reverse(firstPage + "Controller.index").add("idSolicitud", idSolicitud).url;
		return expedienteUrl;
	}

	@Util
	public static void generaPDF(Long idEvaluacion, Integer duracion){
		Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
		if(evaluacion == null){
			Messages.error("Error al recuperar la evaluacion");
		}
		try {
			if (evaluacion != null) {
				BaremacionUtils.ordenarTiposCEconomicos(evaluacion.tipo.ceconomicos);
				BaremacionUtils.ordenarCEconomicos(evaluacion.ceconomicos);
				BaremacionUtils.ordenarTiposCriterios(evaluacion.tipo.criterios);
				BaremacionUtils.ordenarCriterios(evaluacion.criterios);
			}
			// Stupid hack
			boolean admin = "administradorgestor".contains(AgenteController.getAgente().rolActivo);
			if (!EstadosEvaluacionEnum.evaluada.name().equals(evaluacion.estado)) {
				BaremacionService.calcularTotales(evaluacion, admin, true);
			}
			new Report("reports/baremacion/Borrador.html").header("reports/header.html").footer("reports/footer-borrador.html").renderResponse(evaluacion, duracion);
		} catch (Exception e) {
			play.Logger.error("Error al generar el borrador del documento %s", e.getMessage());
			Messages.error("Error al generar el borrador del documento");
		}
	}

	@Util
	public static void save(Long idEvaluacion){
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		if(secure.checkGrafico("guardarEvaluacion", "editable", "editar", ids, null)){
			boolean actionSave = params.get("save") != null;
			boolean actionPdf = params.get("pdf") != null;
			boolean actionEnd = params.get("end") != null;
			if(!(actionSave || actionPdf || actionEnd)){
				//No se específico una acción
				notFound("Acción no especificada");
			}
			Evaluacion evaluacion = Evaluacion.findById(params.get("evaluacion.id", Long.class));
			if(evaluacion == null){
				notFound("Fallo al recuperar la evaluación");
			}
			if (!evaluacion.estado.equals(EstadosEvaluacionEnum.enTramite.name())){
				Messages.error("No se puede guardar porque esta evaluación ya ha sido finalizada");
				Messages.keep();
				index(evaluacion.id, "leer");
			}
			//Comentarios
			if(evaluacion.tipo.comentariosAdministracion){
				evaluacion.comentariosAdministracion = params.get("evaluacion.comentariosAdministracion");
			}

			if(evaluacion.tipo.comentariosSolicitante){
				evaluacion.comentariosSolicitante = params.get("evaluacion.comentariosSolicitante");
			}
			boolean guardarMaximo=false;
			//Criterios de evaluacion
			for(Criterio criterio : evaluacion.criterios){
				String param = "criterio[" + criterio.id + "]";
				String key = param + ".valor";
				Double valor = params.get(key, Double.class);


				if(criterio.tipo.claseCriterio.equals("manual") || criterio.tipo.claseCriterio.equals("automod")){

					// Únicamente valida cuando se va a finalizar
					// la verificación
					if(actionEnd || actionSave){
						if (actionEnd)
							validation.required(key, valor);
						//TODO validaciones de tamaño máximo
						if (criterio.tipo.valorMaximo != null && valor != null && criterio.tipo.valorMaximo.compareTo(valor) < 0) {
							// validation.addError(key, "El valor "+valor+" es superior al valor máximo permitido: "+criterio.tipo.valorMaximo);
							Messages.warning("El valor del criterio manual '"+criterio.tipo.jerarquia+" - "+criterio.tipo.nombre+"' ("+FapFormat.formatMoneda(valor)+") es superior al permitido en ese tipo de criterio: "+FapFormat.formatMoneda(criterio.tipo.valorMaximo));
							if (actionEnd){
								criterio.valor=criterio.tipo.valorMaximo;
								guardarMaximo=true;
							}
						}
						if (criterio.tipo.valorMinimo != null && valor != null && criterio.tipo.valorMinimo.compareTo(valor) > 0) {
							Messages.warning("El criterio manual/automod "+criterio.tipo.jerarquia+" ("+FapFormat.formatMoneda(valor)+") no llega al mínimo valor permitido: "+criterio.tipo.valorMinimo+". Se ha establecido como valor a 0,00");
							if (actionEnd)
								criterio.valor=0.0;
						}
					}
					if(!validation.hasErrors()){
						if (guardarMaximo)
							guardarMaximo=false;
						else
							criterio.valor = valor;
					}
				}

				if(!validation.hasErrors()){
					//Comentarios
					if(criterio.tipo.comentariosAdministracion){				
						criterio.comentariosAdministracion = params.get(param + ".comentariosAdministracion");
					}

					if(criterio.tipo.comentariosSolicitante){
						criterio.comentariosSolicitante = params.get(param + ".comentariosSolicitante");
					}
				}
			}
			if (!validation.hasErrors()){
				boolean admin = "administradorgestor".contains(AgenteController.getAgente().rolActivo);
				BaremacionService.calcularTotales(evaluacion, admin);
				for(Criterio criterio : evaluacion.criterios){
					if (criterio.tipo.claseCriterio.equals("auto")) {
						if(actionEnd || actionSave){
							if (criterio.tipo.valorMaximo != null && criterio.valor != null && criterio.tipo.valorMaximo.compareTo(criterio.valor) < 0) {
								if (actionSave)
									Messages.warning("El criterio automático '"+criterio.tipo.jerarquia+" - "+criterio.tipo.nombre+"' ("+FapFormat.formatMoneda(criterio.valor)+") sobrepasaba el máximo valor permitido. Se ha establecido como valor, su valor máximo posible: "+FapFormat.formatMoneda(criterio.tipo.valorMaximo));
								criterio.valor = criterio.tipo.valorMaximo;
							}
							if (criterio.tipo.valorMinimo != null && criterio.valor != null && criterio.tipo.valorMinimo.compareTo(criterio.valor) > 0) {
								Messages.warning("El criterio automático "+criterio.tipo.jerarquia+" ("+criterio.valor+") no llega al mínimo valor permitido: "+FapFormat.formatMoneda(criterio.tipo.valorMinimo)+". Se ha establecido como valor a 0,00");
								criterio.valor=0.0;
							}
						}
					}
				}
				if (!Messages.hasErrors()) {
					BaremacionService.calcularTotales(evaluacion, true, true);
				}
				evaluacion.save();
			} else {
				flash(evaluacion);
			}

			if(actionSave || actionEnd){
				if(actionEnd && !validation.hasErrors()){
					//Si no hubo errores anteriores, se comprueba si existen validaciones propias de la aplicacion
					botonFinalizar();
					if (!Messages.hasErrors()){
						evaluacion.estado = EstadosEvaluacionEnum.evaluada.name();
						evaluacion.save();
						Messages.ok("La evaluación del expediente " + evaluacion.solicitud.expedienteAed.idAed + " finalizó correctamente");
						ConsultarEvaluacionesController.index();
					}
				}

				if(actionSave && !validation.hasErrors()){
					Messages.ok("La evaluación del expediente " + evaluacion.solicitud.expedienteAed.idAed + " se guardó correctamente");
				}

				Messages.keep();
				redirect("fap.FichaEvaluadorController.index", evaluacion.id, "editar");
			}
		}else{
			play.Logger.error("No se cumple el permiso \"guardarEvaluacion\" con ids: "+ids);
			forbidden();
		}
	}

	@Util
	private static void flash(Evaluacion evaluacion){
		Messages.setFlash("evaluacion.id", params.get("evaluacion.id", String.class));
		Messages.setFlash("evaluacion.totalCriterios", params.get("evaluacion.totalCriterios", String.class));
		Messages.setFlash("evaluacion.comentariosAdministracion", params.get("evaluacion.comentariosAdministracion", String.class));
		Messages.setFlash("evaluacion.comentariosSolicitante", params.get("evaluacion.comentariosSolicitante", String.class));

		for(Criterio c : evaluacion.criterios){
			String param = "criterio[" + c.id + "]";
			if (!c.tipo.noVisibleEvaluador) {
				Messages.setFlash(param + ".valor", params.get(param + ".valor", String.class));
			} else {
				Messages.setFlash(param + ".valor", 0);
			}
			Messages.setFlash(param + ".comentariosAdministracion", params.get(param + ".comentariosAdministracion", String.class));
			Messages.setFlash(param + ".comentariosSolicitante", params.get(param + ".comentariosSolicitante", String.class));
		}
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		for(CEconomico ce : evaluacion.ceconomicos){
			String param = "ceconomico[" + ce.id + "]";
			for (int i = 0; i < tipoEvaluacion.duracion; i++){
				Messages.setFlash(param + ".valores["+i+"].valorEstimado", params.get(param + ".valores["+i+"].valorEstimado", String.class));
			}
			Messages.setFlash(param + ".comentariosAdministracion", params.get(param + ".comentariosAdministracion", String.class));
			Messages.setFlash(param + ".comentariosSolicitante", params.get(param + ".comentariosSolicitante", String.class));
		}
	}

	public static void tablatablaCEconomicos(Long idEvaluacion) {

		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first(); 

		Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
		
		List<CEconomico> rowsFiltered = filtroConceptosEconomicos(evaluacion);

		
		List <Map<String, String>> columnasCEconomicos = new ArrayList <Map <String, String>>();
		List<Double> totalesSolicitadoAnio = new ArrayList<Double>();
		List<Double> totalesEstimadoAnio = new ArrayList<Double>();
		for (int i=0; i<tipoEvaluacion.duracion; i++){
			totalesSolicitadoAnio.add(0.0);
			totalesEstimadoAnio.add(0.0);
		}
		for (CEconomico cEconomico : rowsFiltered) {
			 Map<String, String> columna = new HashMap<String, String>();
			 columna.put("id", cEconomico.id.toString());
			 Double totalesSolicitado = 0.0;
			 Double totalesEstimado = 0.0;
			 
			 Pattern pattern = Pattern.compile ("^[a-zA-Z]$");
			 
			 for (int i=0; i<tipoEvaluacion.duracion; i++){
				 Matcher matcher = pattern.matcher(cEconomico.tipo.jerarquia); 
				 if (matcher.find()){
					 totalesEstimadoAnio.set(i, totalesEstimadoAnio.get(i)+cEconomico.valores.get(i).valorEstimado);
					 totalesSolicitadoAnio.set(i, totalesSolicitadoAnio.get(i)+cEconomico.valores.get(i).valorSolicitado);
				 }

				totalesSolicitado += cEconomico.valores.get(i).valorSolicitado;
				columna.put("valorSolicitado"+i, (new BigDecimal(Double.toString(cEconomico.valores.get(i).valorSolicitado)).setScale(2, RoundingMode.FLOOR).toPlainString()));
				totalesEstimado += cEconomico.valores.get(i).valorEstimado;
				columna.put("valorEstimado"+i, (new BigDecimal(Double.toString(cEconomico.valores.get(i).valorEstimado)).setScale(2, RoundingMode.FLOOR).toPlainString()));
			 }
		  	 columna.put("nombre", cEconomico.tipo.nombre);
		  	 columna.put("jerarquia", cEconomico.tipo.jerarquia);
		  	 if (cEconomico.tipo.clase.equals("auto")){
		  		columna.put("permiso", "false");
		  	 }
		 	 else{
		 		 columna.put("permiso", "true");
		 	 }
		  	 columna.put("totalSolicitado", (new BigDecimal(Double.toString(totalesSolicitado)).setScale(2, RoundingMode.FLOOR).toPlainString()));
		  	 columna.put("totalEstimado", (new BigDecimal(Double.toString(totalesEstimado)).setScale(2, RoundingMode.FLOOR).toPlainString()));
		  	 columnasCEconomicos.add(columna);
		}
		Map<String, String> columna = new HashMap<String, String>();
		columna.put("id", "0");
		Double totalesSolicitado = 0.0;
		Double totalesEstimado = 0.0;
		for (int i=0; i<tipoEvaluacion.duracion; i++){
			columna.put("valorSolicitado"+i, (new BigDecimal(Double.toString(totalesSolicitadoAnio.get(i))).setScale(2, RoundingMode.FLOOR).toPlainString()));
			columna.put("valorEstimado"+i, (new BigDecimal(Double.toString(totalesEstimadoAnio.get(i))).setScale(2, RoundingMode.FLOOR).toPlainString()));
			totalesSolicitado += totalesSolicitadoAnio.get(i);
			totalesEstimado += totalesEstimadoAnio.get(i);
		}
		columna.put("jerarquia", "TOTALES");
	  	columna.put("nombre", "POR AÑOS");
	  	columna.put("permiso", "false");
	  	columna.put("totalSolicitado", (new BigDecimal(Double.toString(totalesSolicitado)).setScale(2, RoundingMode.FLOOR).toPlainString()));
	  	columna.put("totalEstimado", (new BigDecimal(Double.toString(totalesEstimado)).setScale(2, RoundingMode.FLOOR).toPlainString()));
	  	columnasCEconomicos.add(columna);
		renderJSON(columnasCEconomicos);
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
			if (cEconomico.tipo.clase.equals("auto"))
				record.permisoEditar = true;
			else
				record.permisoEditar = false;
			record.permisoBorrar = false;
		}
		return records;
	}

	public static void botonFinalizar() {
		//Buscamos si hay una clase hija de BaremacionFAP que implemente un método de 
		//chequeo de condiciones para finalizar la baremacion individual
		Class invokedClass = null;
		//Busca una clase que herede de BaremacionFAP
        List<Class> assignableClasses = Play.classloader.getAssignableClasses(BaremacionFAP.class);
        if(assignableClasses.size() > 0) {
            invokedClass = assignableClasses.get(0);
        } else {
        	invokedClass = BaremacionFAP.class;
        }
        if (invokedClass != null) {
			Method method = null;
			try {
				method = invokedClass.getDeclaredMethod("checkFinalizarEvaluacion", Evaluacion.class);
			} catch (Exception ex) {
				invokedClass = BaremacionFAP.class;
				if (invokedClass != null) {
					method = null;
					try {
						method = invokedClass.getDeclaredMethod("checkFinalizarEvaluacion", Evaluacion.class);
					} catch (Exception e) {
						play.Logger.error("Error: No se ha podido encontrar el método checkFinalizarEvaluacion de la clase BaremacionApp");
						Messages.error("Error: No se ha podido ejecutar el método checkFinalizarEvaluacion correctamente");
					}
				}
			}

			if (!Messages.hasErrors()) {
				boolean resultado = false;
				if (method != null) {
					try {
						Long idEvaluacion = Long.parseLong(params.get("idEvaluacion"));
						Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
						resultado = (Boolean)method.invoke(ConsultarEvaluacionesController.class, evaluacion);
					} catch (Exception e) {
						play.Logger.error("Error: No se ha podido invocar el método checkFinalizarEvaluacion de la clase BaremacionFAP");
						Messages.error("Error: No se ha podido ejecutar el metodo checkFinalizarEvaluacion correctamente");
					} 
				} else {
					play.Logger.error("Error: No existe el Método apropiado para validar checkFinalizarEvaluacion");
					Messages.error("Error: No se ha podido ejecutar checkFinalizarEvaluacion correctamente");
				}
				if (!resultado){
					play.Logger.error("Error: La evaluación no cumple las condiciones indicadas en checkFinalizarEvaluacion");
					Messages.error("Error: La evaluación no cumple las condiciones indicadas en checkFinalizarEvaluacion");
				}
			}
        }
	}
	
	public static List<CEconomico> filtroConceptosEconomicos (Evaluacion evaluacion){
		SolicitudGenerica solicitud = evaluacion.solicitud;
		List<CEconomico> rowsFiltered = new ArrayList<CEconomico>();
		for(CEconomico ceconomicoS : solicitud.ceconomicos){
			for(CEconomico ceconomicoE : evaluacion.ceconomicos){
				if ((ceconomicoE.tipo.nombre.equals(ceconomicoS.tipo.nombre)) &&
						(ceconomicoE.tipo.jerarquia.equals(ceconomicoS.tipo.jerarquia))){
						rowsFiltered.add(ceconomicoE);
					break;
				}
			}
			
			if (ceconomicoS.tipo.tipoOtro){
				for(CEconomico ceconomicoE : evaluacion.ceconomicos){
					for (CEconomicosManuales ceconomicoManual: ceconomicoS.otros){
						if ((ceconomicoE.tipo.nombre.equals(ceconomicoManual.tipo.nombre)) && 
								(ceconomicoE.tipo.jerarquia.equals(ceconomicoManual.tipo.jerarquia))){ 
							rowsFiltered.add(ceconomicoE);
							break;
						}
					}
				}
			}
		}
		return rowsFiltered;
	}

	
	
}