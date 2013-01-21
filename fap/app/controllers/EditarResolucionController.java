package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import play.mvc.Util;

import reports.Report;
import resolucion.ResolucionBase;
import tags.ComboItem;
import validation.CustomValidation;

import messages.Messages;
import models.Agente;
import models.ResolucionFAP;
import models.SolicitudGenerica;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.EditarResolucionControllerGen;
import enumerado.fap.gen.EstadoResolucionEnum;

public class EditarResolucionController extends EditarResolucionControllerGen {

	/**
	 * Expedientes que se muestran en la tabla para poder seleccionar
	 */
	public static void tablatablaExpedientes() {
		
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		
		// Obtenemos el objeto "ResolucionBase"
		ResolucionBase resolBase = ResolucionControllerFAP.getResolucionObject(ids.get("idResolucionFAP"));
		
		java.util.List<SolicitudGenerica> rows = resolBase.getSolicitudesAResolver();
		
		List<SolicitudGenerica> rowsFiltered = rows; //Tabla sin permisos, no filtra
		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estadoValue", "estado", "estadoUsuario", "solicitante.id", "solicitante.nombreCompleto"));
	}
	
	public static void tablatablaExpedientesUnico() {

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		
		// Obtenemos el objeto "ResolucionBase"
		ResolucionBase resolBase = ResolucionControllerFAP.getResolucionObject(ids.get("idResolucionFAP"));
		
		java.util.List<SolicitudGenerica> rows = resolBase.getSolicitudesAResolver();
		
		List<SolicitudGenerica> rowsFiltered = rows; //Tabla sin permisos, no filtra
		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estadoValue", "estado", "estadoUsuario", "solicitante.id", "solicitante.nombreCompleto"));
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void prepararResolucion(Long idResolucionFAP, String btnPrepararResolucion) {
		checkAuthenticity();
		if (!permisoPrepararResolucion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			ResolucionFAP resolucion = EditarResolucionController.getResolucionFAP(idResolucionFAP);
			ResolucionBase resolBase = null;
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			} catch (Throwable e1) {
				play.Logger.error("Error obteniendo tipo de resolución: " + e1.getMessage());
			}
			resolBase.prepararResolucion(idResolucionFAP);
		}

		if (!Messages.hasErrors()) {
			EditarResolucionController.prepararResolucionValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarResolucionController.prepararResolucionRender(idResolucionFAP);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void crearResolucion(Long idResolucionFAP, String btnCrearResolucion) {
		checkAuthenticity();
		if (!permisoCrearResolucion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			ResolucionFAP resolucion = EditarResolucionController.getResolucionFAP(idResolucionFAP);
			resolucion.estado = EstadoResolucionEnum.creada.name();
			resolucion.save();
			ResolucionBase resolBase = null;
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
				resolBase.setLineasDeResolucion(idResolucionFAP);
			} catch (Throwable e) {
				play.Logger.error("Error obteniendo tipo de resolución: " + e.getMessage());
			}
		}

		if (!Messages.hasErrors()) {
			EditarResolucionController.crearResolucionValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarResolucionController.crearResolucionRender(idResolucionFAP);
	}
	
	public static List<ComboItem> selectJefeServicio() {
		List<Agente> listaJefesServicio = new ArrayList<Agente>();
		List<ComboItem> listaCombo = new ArrayList<ComboItem>();
		try {
			listaJefesServicio = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getJefesServicio");
		} catch (Throwable e) {
			Messages.error("No se pudieron obtener los jefes de servicio");
			play.Logger.error("No se pudieron obtener los jefes de servicio"+e.getMessage());
		}
		for (Agente agente: listaJefesServicio) {
			listaCombo.add(new ComboItem(agente.username, agente.username+" - "+agente.name));
		}
		return listaCombo;
	}
	
	public static List<ComboItem> selectPrioridadFirma() {
		List<ComboItem> listaPrioridades = new ArrayList<ComboItem>();
		try {
			listaPrioridades = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getPrioridadesFirma");
		} catch (Throwable e) {
			Messages.error("No se pudieron obtener las prioridades de la firma");
			play.Logger.error("No se pudieron obtener las prioridades posibles en la firma"+e.getMessage());
		}
		return listaPrioridades;
	}
	
	@Util
	public static void formSelectJefeServicio(Long idResolucionFAP, ResolucionFAP resolucionFAP, String enviarFirmaJSPortafirma) {
		checkAuthenticity();
		if (!permisoFormSelectJefeServicio("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		ResolucionFAP dbResolucionFAP = EditarResolucionController.getResolucionFAP(idResolucionFAP);

		EditarResolucionController.formSelectJefeServicioBindReferences(resolucionFAP);

		if (!Messages.hasErrors()) {
			EditarResolucionController.formSelectJefeServicioValidateCopy("editar", dbResolucionFAP, resolucionFAP);
		}

		if (!Messages.hasErrors()) {
			EditarResolucionController.formSelectJefeServicioValidateRules(dbResolucionFAP, resolucionFAP);
			
			// TODO: Enviar al portafirma los documentos, indicando qué jefe de Servicio lo debe Firmar
		}
		if (!Messages.hasErrors()) {
			dbResolucionFAP.estado = EstadoResolucionEnum.pendienteFirmaJefeServicio.name();
			dbResolucionFAP.save();
			Messages.ok("Se ha enviado correctamente al portafirma la solicitud de la firma del Jefe de Servicio");
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarResolucionController.formSelectJefeServicioRender(idResolucionFAP);
	}

	@Util
	public static void formSelectJefeServicioValidateCopy(String accion, ResolucionFAP dbResolucionFAP, ResolucionFAP resolucionFAP) {
		CustomValidation.clearValidadas();
		CustomValidation.valid("resolucionFAP", resolucionFAP);
		CustomValidation.required("resolucionFAP.jefeDeServicio", resolucionFAP.jefeDeServicio);
		CustomValidation.validValueFromTable("resolucionFAP.jefeDeServicio", resolucionFAP.jefeDeServicio);
		dbResolucionFAP.jefeDeServicio = resolucionFAP.jefeDeServicio;
		CustomValidation.required("resolucionFAP.prioridadFirma", resolucionFAP.prioridadFirma);
		CustomValidation.validValueFromTable("resolucionFAP.prioridadFirma", resolucionFAP.prioridadFirma);
		dbResolucionFAP.prioridadFirma = resolucionFAP.prioridadFirma;
		CustomValidation.required("resolucionFAP.fechaTopeFirma", resolucionFAP.fechaTopeFirma);
		dbResolucionFAP.fechaTopeFirma = resolucionFAP.fechaTopeFirma;

		if (dbResolucionFAP.fechaTopeFirma != null) {
			if (dbResolucionFAP.fechaTopeFirma.isBeforeNow()) {
				play.Logger.error("La fecha tope de firma no puede ser anterior a hoy.");
				CustomValidation.error("La fecha tope de firma no puede ser anterior a hoy.","resolucionFAP.fechaTopeFirma", resolucionFAP.fechaTopeFirma);
			}
			int dias = 0;
			// Comprobar la fecha de tope de firma con el ResolucionBase
			try {
				dias = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getDiasLimiteFirma", dbResolucionFAP.id);
				DateTime diaLimite = new DateTime();
				diaLimite = diaLimite.plusDays(dias);
				if (diaLimite.isBefore(dbResolucionFAP.fechaTopeFirma)) {
					play.Logger.error("La fecha tope de firma no puede ser posterior a "+diaLimite+".");
					CustomValidation.error("La fecha tope de firma no puede ser posterior a "+diaLimite+".", "resolucionFAP.fechaTopeFirma", resolucionFAP.fechaTopeFirma);					
				}
			} catch (Throwable e) {
				e.printStackTrace();
				play.Logger.error("No se ha podido calcular el límite de fecha para la firma."+e);
				CustomValidation.error("No se ha podido calcular el límite de fecha para la firma", "resolucionFAP.fechaTopeFirma", resolucionFAP.fechaTopeFirma);
			}
			
		}
		

	}
	
	@Util
	public static void firmaDirectorPortafirma(Long idResolucionFAP, String enviarFirmaDirectorPortafirma) {
		checkAuthenticity();
		if (!permisoFirmaDirectorPortafirma("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		ResolucionFAP dbResolucionFAP = EditarResolucionController.getResolucionFAP(idResolucionFAP);

		if (!Messages.hasErrors()) {
		}

		if (!Messages.hasErrors()) {
			EditarResolucionController.firmaDirectorPortafirmaValidateRules();
		}
		if (!Messages.hasErrors()) {
			dbResolucionFAP.estado = EstadoResolucionEnum.pendienteFirmaDirector.name();
			dbResolucionFAP.save();
			Messages.ok("Se ha enviado correctamente al portafirma la solicitud de la firma del Director");
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarResolucionController.firmaDirectorPortafirmaRender(idResolucionFAP);
	}
}
