package controllers;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import baremacion.BaremacionFAP;

import play.Play;
import play.mvc.Util;
import tables.TableRecord;
import messages.Messages;
import messages.Messages.MessageType;
import models.CEconomico;
import models.Evaluacion;
import models.SolicitudGenerica;
import models.TipoEvaluacion;
import controllers.gen.PaginaCEconomicosEvaluadosControllerGen;
import enumerado.fap.gen.EstadosEvaluacionEnum;

public class PaginaCEconomicosEvaluadosController extends PaginaCEconomicosEvaluadosControllerGen {
	
	public static void index(String accion, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/PaginaCEconomicosEvaluados/PaginaCEconomicosEvaluados.html");
		}

		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion)) {
			solicitud = PaginaCEconomicosEvaluadosController.getSolicitudGenerica();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitud.save();
				idSolicitud = solicitud.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			solicitud = PaginaCEconomicosEvaluadosController.getSolicitudGenerica(idSolicitud);

		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		int duracion = 0;
		if (tipoEvaluacion != null)
			duracion=tipoEvaluacion.duracion-1;
		log.info("Visitando página: " + "fap/PaginaCEconomicosEvaluados/PaginaCEconomicosEvaluados.html");

		boolean noPuedeEditar = true;
		Evaluacion evaluacion = Evaluacion.find("select evaluacion from Evaluacion evaluacion where evaluacion.solicitud.id=?", idSolicitud).first();
		if (evaluacion != null){
			if ((evaluacion.tipo.estado.equals("evaluada")) && (evaluacion.estado.equals(EstadosEvaluacionEnum.evaluada.name()))){
				noPuedeEditar = false;
			}
		}
		renderTemplate("fap/PaginaCEconomicosEvaluados/PaginaCEconomicosEvaluados.html", accion, duracion, idSolicitud, solicitud, noPuedeEditar);
	}
	
	public static void tablatablaCEconomicosEvaluados(Long idSolicitud) {
		
		java.util.List<CEconomico> rows = CEconomico.find("select cEconomico from Solicitud solicitud join solicitud.ceconomicos cEconomico where solicitud.id=?",idSolicitud).fetch();
		
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();

		List<CEconomico> rowsFiltered = rows; //Tabla sin permisos, no filtra
		List <Map<String, String>> columnasCEconomicos = new ArrayList <Map <String, String>>();
		List<Double> totalesConcedidoAnio = new ArrayList<Double>();
		List<Double> totalesSolicitadoAnio = new ArrayList<Double>();
		List<Double> totalesPropuestoAnio = new ArrayList<Double>();
		List<Double> totalesEstimadoAnio = new ArrayList<Double>();
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
	        if(assignableClasses.size() > 0) {
	            invokedClass = assignableClasses.get(0);
	        } else {
	        	invokedClass = BaremacionFAP.class;
	        }
	        if (invokedClass != null) {
				Method method = null;
				try {
					method = invokedClass.getDeclaredMethod("validarCEconomicosEvaluados", long.class, List.class);
				} catch (Exception ex) {
					invokedClass = BaremacionFAP.class;
					if (invokedClass != null) {
						method = null;
						try {
							method = invokedClass.getDeclaredMethod("validarCEconomicosEvaluados", long.class, List.class);
						} catch (Exception e) {
							play.Logger.error("Error g001: No se ha podido encontrar el método validarCEconomicosEvaluados de la clase BaremacionFAP");
							Messages.error("Error interno g001. No se ha podido Guardar correctamente");
						}
					}
				}
				if (!Messages.hasErrors()) {
					if (method != null) {
						try {
							List<CEconomico> ceconomicos = solicitud.ceconomicos;
							method.invoke(PaginaCEconomicosEvaluadosController.class, idSolicitud, ceconomicos);
						} catch (Exception e) {
							play.Logger.error("Error g002: No se ha podido invocar el método validarCEconomicosEvaluados de la clase BaremacionFAP");
							Messages.error("Error interno g002. No se ha podido Guardar correctamente");
						} 
					} else {
						play.Logger.error("Error g003: No existe el Método apropiado para validar los CEconomicos. El método debe llamarse 'validarCEconomicosEvaluados()'");
						Messages.error("Error interno g003. No se ha podido Guardar correctamente");
					}
				}
			} else {
				play.Logger.error("Error g004: No existe la Clase apropiada para iniciar la Baremacion. La clase debe extender de 'BaremacionFAP'");
				Messages.error("Error interno g004. No se ha podido Guardar correctamente");
			}
		}

		if (!Messages.hasErrors()) {
			PaginaCEconomicosEvaluadosController.guardarValidateRules();
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaCEconomicosEvaluados/PaginaCEconomicosEvaluados.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaCEconomicosEvaluados/PaginaCEconomicosEvaluados.html" + " , intentada sin éxito (Problemas de Validación)");
		PaginaCEconomicosEvaluadosController.guardarRender(idSolicitud);
	}
	
}
