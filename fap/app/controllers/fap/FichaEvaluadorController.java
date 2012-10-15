package controllers.fap;


import java.util.HashMap;

import static play.modules.pdf.PDF.renderPDF;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import messages.Messages;
import messages.Messages.MessageType;
import models.CEconomico;
import models.Criterio;
import models.CriterioListaValores;
import models.Documento;
import models.Evaluacion;
import models.TipoCEconomico;
import models.TipoCriterio;
import models.TipoDocumentoAccesible;
import models.TipoEvaluacion;
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

@With({SecureController.class, AgenteController.class, CheckAccessController.class})
public class FichaEvaluadorController extends Controller {
	
	@Inject
	protected static Secure secure;

	@Finally(only="index")
	public static void end(){
		Messages.deleteFlash();
	}
	
	public static void index(long idEvaluacion){
		
		if(secure.checkGrafico("evaluacion", "visible", "leer", null, null)){
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
			List<Documento> documentos = evaluacion.getDocumentosAccesibles();
			int duracion = tipoEvaluacion.duracion-1;
			BaremacionUtils.calcularTotalesCEconomicosFichaEvaluacion(evaluacion);
			renderTemplate("fap/Baremacion/fichaEvaluador.html", evaluacion, documentos, expedienteUrl, duracion, idEvaluacion);
		}else{
			forbidden();
		}
	}
	
	public static void tabladocumentosAccesiblesEvaluador(Long idSolicitud) {

		java.util.List<Documento> rows = new ArrayList<Documento>();
		if (TipoDocumentoAccesible.count() > 0){
			String completarConsultaTiposDocumentos = "";
			List<TipoDocumentoAccesible> tiposDocumentosAccesibles = TipoDocumentoAccesible.findAll();
			completarConsultaTiposDocumentos =" and (";
			boolean primero=true;
			for (TipoDocumentoAccesible tipo: tiposDocumentosAccesibles){
				if (primero){
					completarConsultaTiposDocumentos += " documento.tipo='"+tipo.uri+"'";
					primero=false;
				} else {
					completarConsultaTiposDocumentos += " or documento.tipo='"+tipo.uri+"'";
				}
			}
			completarConsultaTiposDocumentos +=")";
			rows = Documento.find("select documento from SolicitudGenerica solicitud join solicitud.documentacion.documentos documento where solicitud.id=? and documento.verificado=true "+completarConsultaTiposDocumentos, idSolicitud).fetch();
		}

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Documento> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Documento> response = new tables.TableRenderResponse<Documento>(rowsFiltered, false, false, false, "", "", "", "editar", ids);

		renderJSON(response.toJSON("descripcionVisible", "tipo", "urlDescarga", "id"));
	}

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
	
	public static void generaPDF(Long idEvaluacion, Integer duracion){
		Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
		if(evaluacion == null){
			notFound("Evaluación no encontrada");
		}
		try {
			new Report("reports/baremacion/Borrador.html").header("reports/header.html").footer("reports/footer-borrador.html").renderResponse(evaluacion, duracion);
		} catch (Exception e) {
			play.Logger.error("Error al generar el borrador del documento %s", e.getMessage());
			Messages.error("Error al generar el borrador del documento");
		}
	}

	public static void save(){
		if(secure.checkGrafico("evaluacion", "editable", "editar", null, null)){
			boolean actionSave = params.get("save") != null;
			boolean actionPdf = params.get("pdf") != null;
			boolean actionEnd = params.get("end") != null;
			if(!(actionSave || actionPdf || actionEnd)){
				//No se específico una acción
				notFound("Acción no especificada");
			}
			Evaluacion evaluacion = Evaluacion.findById(params.get("evaluacion.id", Long.class));
			if(evaluacion == null){
				notFound("Evaluación no encontrada");
			}
			
			//Comentarios
			if(evaluacion.tipo.comentariosAdministracion){
				evaluacion.comentariosAdministracion = params.get("evaluacion.comentariosAdministracion");
			}
			
			if(evaluacion.tipo.comentariosSolicitante){
				evaluacion.comentariosSolicitante = params.get("evaluacion.comentariosSolicitante");
			}
				
			//Criterios de evaluacion
			for(Criterio criterio : evaluacion.criterios){
				String param = "criterio[" + criterio.id + "]";
	
				if(criterio.tipo.claseCriterio.equals("manual")){
					String key = param + ".valor";
					Double valor = params.get(key, Double.class);
					
					// Únicamente valida cuando se va a finalizar
					// la verificación
					if(actionEnd){
						validation.required(key, valor);
						//TODO validaciones de tamaño máximo
						if (criterio.tipo.valorMaximo != null && criterio.tipo.valorMaximo.compareTo(valor) > 0) {
							validation.addError(key, "El valor "+valor+" es superior al valor máximo permitido: "+criterio.tipo.valorMaximo);
						}
						if (criterio.tipo.valorMinimo != null && criterio.tipo.valorMinimo.compareTo(valor) < 0) {
							validation.addError(key, "El valor "+valor+" es inferior al valor mínimo permitido: "+criterio.tipo.valorMinimo);
						}
					}
					if(!validation.hasErrors()){
						criterio.valor = valor;
					}
				}else if(criterio.tipo.claseCriterio.equals("automod")){
					//TODO criterio automático modificable
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
				BaremacionService.calcularTotales(evaluacion);
				evaluacion.save();
			} else {
				flash(evaluacion);
			}
			
			if(actionSave || actionEnd){
				if(actionEnd && !validation.hasErrors()){
					evaluacion.estado = "evaluada";
					evaluacion.save();
					Messages.ok("La evaluación del expediente " + evaluacion.solicitud.expedienteAed.idAed + " finalizó correctamente");
					ConsultarEvaluacionesController.index();
				}
				
				if(actionSave && !validation.hasErrors()){
					Messages.ok("La evaluación del expediente " + evaluacion.solicitud.expedienteAed.idAed + " se guardó correctamente");
				}
				
				Messages.keep();
				index(evaluacion.id);
			}
		}else{
			forbidden();
		}
	}
	
	
	private static void flash(Evaluacion evaluacion){
		Messages.setFlash("evaluacion.id", params.get("evaluacion.id", String.class));
		Messages.setFlash("evaluacion.totalCriterios", params.get("evaluacion.totalCriterios", String.class));
		Messages.setFlash("evaluacion.comentariosAdministracion", params.get("evaluacion.comentariosAdministracion", String.class));
		Messages.setFlash("evaluacion.comentariosSolicitante", params.get("evaluacion.comentariosSolicitante", String.class));
		
		for(Criterio c : evaluacion.criterios){
			String param = "criterio[" + c.id + "]";
			Messages.setFlash(param + ".valor", params.get(param + ".valor", String.class));
			Messages.setFlash(param + ".comentariosAdministracion", params.get(param + ".comentariosAdministracion", String.class));
			Messages.setFlash(param + ".comentariosSolicitante", params.get(param + ".comentariosSolicitante", String.class));
		}
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		for(CEconomico ce : evaluacion.ceconomicos){
			String param = "ceconomico[" + ce.id + "]";
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
	
	public static void tablatablaCEconomicos(Long idEvaluacion) {
		
		java.util.List<CEconomico> rows = CEconomico
				.find("select cEconomico from Evaluacion evaluacion join evaluacion.ceconomicos cEconomico where evaluacion.id=?",
						idEvaluacion).fetch();
		
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();

		List<CEconomico> rowsFiltered = rows; //Tabla sin permisos, no filtra
		List <Map<String, String>> columnasCEconomicos = new ArrayList <Map <String, String>>();
		List<Double> totalesConcedidoAnio = new ArrayList<Double>();
		List<Double> totalesSolicitadoAnio = new ArrayList<Double>();
		List<Double> totalesPropuestoAnio = new ArrayList<Double>();
		List<Double> totalesEstimadoAnio = new ArrayList<Double>();
		int indiceTotales=0;
		for (int i=0; i<tipoEvaluacion.duracion; i++){
			totalesConcedidoAnio.add(0.0);
			totalesSolicitadoAnio.add(0.0);
			totalesPropuestoAnio.add(0.0);
			totalesEstimadoAnio.add(0.0);
		}
		for (CEconomico cEconomico : rowsFiltered) {
			 Map<String, String> columna = new HashMap<String, String>();
			 columna.put("id", cEconomico.id.toString());
			 Double totalesConcedido = 0.0;
			 Double totalesSolicitado = 0.0;
			 Double totalesPropuesto = 0.0;
			 Double totalesEstimado = 0.0;
			 for (int i=0; i<tipoEvaluacion.duracion; i++){
				totalesConcedidoAnio.set(i, totalesConcedidoAnio.get(i)+cEconomico.valores.get(i).valorConcedido);
				totalesConcedido += cEconomico.valores.get(i).valorConcedido;
				columna.put("valorConcedido"+i, (new BigDecimal(Double.toString(cEconomico.valores.get(i).valorConcedido)).setScale(2, RoundingMode.FLOOR).toPlainString()));
				totalesSolicitadoAnio.set(i, totalesSolicitadoAnio.get(i)+cEconomico.valores.get(i).valorSolicitado);
				totalesSolicitado += cEconomico.valores.get(i).valorSolicitado;
				columna.put("valorSolicitado"+i, (new BigDecimal(Double.toString(cEconomico.valores.get(i).valorSolicitado)).setScale(2, RoundingMode.FLOOR).toPlainString()));
				totalesPropuestoAnio.set(i, totalesPropuestoAnio.get(i)+cEconomico.valores.get(i).valorPropuesto);
				totalesPropuesto += cEconomico.valores.get(i).valorPropuesto;
				columna.put("valorPropuesto"+i, (new BigDecimal(Double.toString(cEconomico.valores.get(i).valorPropuesto)).setScale(2, RoundingMode.FLOOR).toPlainString()));
				totalesEstimadoAnio.set(i, totalesEstimadoAnio.get(i)+cEconomico.valores.get(i).valorEstimado);
				totalesEstimado += cEconomico.valores.get(i).valorEstimado;
				columna.put("valorEstimado"+i, (new BigDecimal(Double.toString(cEconomico.valores.get(i).valorEstimado)).setScale(2, RoundingMode.FLOOR).toPlainString()));
			 }
		  	 columna.put("nombre", cEconomico.tipo.nombre);
		  	 columna.put("jerarquia", cEconomico.tipo.jerarquia);
		  	 columna.put("permiso", "true");
		  	 columna.put("totalSolicitado", (new BigDecimal(Double.toString(totalesSolicitado)).setScale(2, RoundingMode.FLOOR).toPlainString()));
		  	 columna.put("totalConcedido", (new BigDecimal(Double.toString(totalesConcedido)).setScale(2, RoundingMode.FLOOR).toPlainString()));
		  	 columna.put("totalPropuesto", (new BigDecimal(Double.toString(totalesPropuesto)).setScale(2, RoundingMode.FLOOR).toPlainString()));
		  	 columna.put("totalEstimado", (new BigDecimal(Double.toString(totalesEstimado)).setScale(2, RoundingMode.FLOOR).toPlainString()));
		  	 columnasCEconomicos.add(columna);
		  	 indiceTotales++;
		}
		Map<String, String> columna = new HashMap<String, String>();
		columna.put("id", "0");
		Double totalesConcedido = 0.0;
		Double totalesSolicitado = 0.0;
		Double totalesPropuesto = 0.0;
		Double totalesEstimado = 0.0;
		for (int i=0; i<tipoEvaluacion.duracion; i++){
			columna.put("valorConcedido"+i, (new BigDecimal(Double.toString(totalesConcedidoAnio.get(i))).setScale(2, RoundingMode.FLOOR).toPlainString()));
			columna.put("valorSolicitado"+i, (new BigDecimal(Double.toString(totalesSolicitadoAnio.get(i))).setScale(2, RoundingMode.FLOOR).toPlainString()));
			columna.put("valorPropuesto"+i, (new BigDecimal(Double.toString(totalesPropuestoAnio.get(i))).setScale(2, RoundingMode.FLOOR).toPlainString()));
			columna.put("valorEstimado"+i, (new BigDecimal(Double.toString(totalesEstimadoAnio.get(i))).setScale(2, RoundingMode.FLOOR).toPlainString()));
			totalesConcedido += totalesConcedidoAnio.get(i);
			totalesSolicitado += totalesSolicitadoAnio.get(i);
			totalesPropuesto += totalesPropuestoAnio.get(i);
			totalesEstimado += totalesEstimadoAnio.get(i);
		}
		columna.put("jerarquia", "TOTALES");
	  	columna.put("nombre", "POR AÑOS");
	  	columna.put("permiso", "false");
	  	columna.put("totalSolicitado", (new BigDecimal(Double.toString(totalesSolicitado)).setScale(2, RoundingMode.FLOOR).toPlainString()));
	  	columna.put("totalConcedido", (new BigDecimal(Double.toString(totalesConcedido)).setScale(2, RoundingMode.FLOOR).toPlainString()));
	  	columna.put("totalPropuesto", (new BigDecimal(Double.toString(totalesPropuesto)).setScale(2, RoundingMode.FLOOR).toPlainString()));
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
		
}
