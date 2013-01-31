package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import javax.inject.Inject;

import platino.FirmaUtils;
import play.modules.guice.InjectSupport;
import play.mvc.Util;

import registroresolucion.RegistroResolucion;
import reports.Report;
import resolucion.ResolucionBase;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.PortafirmaFapService;
import services.PortafirmaFapServiceException;
import services.RegistroLibroResolucionesService;
import services.RegistroLibroResolucionesServiceException;
import services.RegistroService;
import services.aed.Interesados;
import services.portafirma.PortafirmaImpl;
import services.responses.PortafirmaCrearSolicitudResponse;
import sun.net.www.content.text.plain;
import tags.ComboItem;
import validation.CustomValidation;

import messages.Messages;
import models.Agente;
import models.JustificanteRegistro;
import models.ResolucionFAP;
import models.SolicitudGenerica;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.EditarResolucionControllerGen;
import emails.Mails;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.TipoCrearExpedienteAedEnum;

@InjectSupport
public class EditarResolucionController extends EditarResolucionControllerGen {

	@Inject
    public static RegistroService registroService;
	
	@Inject
    public static PortafirmaFapService portafirmaService;
	
	@Inject
    public static RegistroLibroResolucionesService registroLibroResolucionesService;
	
	@Inject
	public static GestorDocumentalService gestorDocumentalService;
	
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
				resolBase.prepararResolucion(idResolucionFAP);
			} catch (Throwable e1) {
				play.Logger.error("Error obteniendo tipo de resolución: " + e1.getMessage());
			}
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
	public static void frmCrearResolucion(Long idResolucionFAP, String btnCrearResolucion) {
		checkAuthenticity();
		if (!permisoFrmCrearResolucion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			ResolucionFAP resolucion = EditarResolucionController.getResolucionFAP(idResolucionFAP);
			ResolucionBase resolBase = null;
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
				resolBase.setLineasDeResolucion(idResolucionFAP);
				ResolucionBase.avanzarFase_Borrador(resolucion);
			} catch (Throwable e) {
				play.Logger.error("Error obteniendo tipo de resolución: " + e.getMessage());
			}
		}

		if (!Messages.hasErrors()) {
			EditarResolucionController.frmCrearResolucionValidateRules();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarResolucionController.frmCrearResolucionRender(idResolucionFAP);
	}
	
	public static void crearResolucion(List<Long> idsSeleccionados) {
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		Long idResolucionFAP = ids.get("idResolucionFAP");
		if (idsSeleccionados == null) {
			play.Logger.error("Se debe seleccionar un expediente");
			Messages.error("Se debe seleccionar un expediente");
			Messages.keep();
		} else if (idsSeleccionados.size() > 1) {
			play.Logger.error("Se debe seleccionar solo un expediente");
			Messages.error("Se debe seleccionar solo un expediente");
			Messages.keep();
		} else {
			ResolucionBase resolBase = ResolucionControllerFAP.getResolucionObject(idResolucionFAP);
			resolBase.setLineasDeResolucion(idResolucionFAP);
			ResolucionFAP resolucion = EditarResolucionController.getResolucionFAP(idResolucionFAP);
			ResolucionBase.avanzarFase_Borrador(resolucion);
		}
		index("editar", idResolucionFAP);
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
			dbResolucionFAP.save();
			try {
				PortafirmaCrearSolicitudResponse response = portafirmaService.crearSolicitudFirma(dbResolucionFAP);
				dbResolucionFAP.idSolicitudFirma = response.getIdSolicitud();
			} catch (PortafirmaFapServiceException e) {
				play.Logger.error("Error al enviar los documentos al portafirma", e);
				Messages.error("Error al enviar los documentos al portafirma");
			}
		}
		if (!Messages.hasErrors()) {
			ResolucionBase.avanzarFase_Preparada_Portafirma(dbResolucionFAP);
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
			ResolucionBase.avanzarFase_FirmadaJefeServicio(dbResolucionFAP);
			dbResolucionFAP.save();
			Messages.ok("Se ha enviado correctamente al portafirma la solicitud de la firma del Director");
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarResolucionController.firmaDirectorPortafirmaRender(idResolucionFAP);
	}
	
	@Util
	public static void firFirmarResolucionFrmFirmarJefeServicio(Long idResolucionFAP, String firma) {
		ResolucionFAP resolucionFAP = EditarResolucionController.getResolucionFAP(idResolucionFAP);

		play.Logger.info("Metodo: firFirmarResolucionFrmFirmarJefeServicio");
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		Map<String, Object> vars = new HashMap<String, Object>();
		if (secure.checkAcceso("editarFirma", "editar", ids, vars)) {
			if (resolucionFAP.registro.firmantes.todos == null || resolucionFAP.registro.firmantes.todos.size() == 0) {
				resolucionFAP.registro.firmantes.todos = resolucionFAP.calcularFirmantes();
				resolucionFAP.registro.firmantes.save();
			}
			FirmaUtils.firmar(resolucionFAP.registro.oficial, resolucionFAP.registro.firmantes.todos, firma, null);
		} else {
			//ERROR
			Messages.error("No tiene permisos suficientes para realizar la acción++");
		}
		if (!Messages.hasErrors()) {

			resolucionFAP.save();
			ResolucionBase.avanzarFase_Preparada_FirmaJefeServicio(resolucionFAP);
		}
	}
	
	@Util
	public static void frmComprobarFirmas(Long idResolucionFAP, String comprobarFirmado) {
		checkAuthenticity();
		if (!permisoFrmComprobarFirmas("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			ResolucionFAP dbResolucionFAP = EditarResolucionController.getResolucionFAP(idResolucionFAP);
			try {
				if (portafirmaService.comprobarSiResolucionFirmada(dbResolucionFAP.idSolicitudFirma)) {
					ResolucionBase.avanzarFase_PendienteFirmarDirector(dbResolucionFAP);
					dbResolucionFAP.registro.fasesRegistro.firmada = true;
					dbResolucionFAP.save();
				}
			} catch (PortafirmaFapServiceException e) {
				play.Logger.error("Error al comprobar si ya se ha firmado la resolución en el portafirma.", e);
				Messages.error("Error al comprobar si ya se ha firmado la resolución en el portafirma.");
			}
		}

		if (!Messages.hasErrors()) {
			EditarResolucionController.frmComprobarFirmasValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarResolucionController.frmComprobarFirmasRender(idResolucionFAP);
	}
	
	@Util
	public static void enviarRegistrarResolucion(Long idResolucionFAP, String btnRegistrarResolucion) {
		checkAuthenticity();
		if (!permisoEnviarRegistrarResolucion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		ResolucionFAP dbResolucionFAP = EditarResolucionController.getResolucionFAP(idResolucionFAP);
		RegistroResolucion datosRegistro = null;
		
		/// 1. Crear la resolución
		if (!Messages.hasErrors()) {
			if ((dbResolucionFAP.registro.fasesRegistro.firmada) && (!dbResolucionFAP.registro.fasesRegistro.registro)) {
				try {
					datosRegistro = registroLibroResolucionesService.crearResolucion(dbResolucionFAP);
					dbResolucionFAP.codigoResolucion = Integer.toString(datosRegistro.numero);
					dbResolucionFAP.fechaRegistroResolucion = datosRegistro.fecha;
					dbResolucionFAP.folio_inicio = datosRegistro.primerFolio;
					dbResolucionFAP.folio_final = datosRegistro.ultimoFolio;
					dbResolucionFAP.registro.fasesRegistro.registro = true;
					dbResolucionFAP.save();
				} catch (RegistroLibroResolucionesServiceException e) {
					play.Logger.error("No se puede crear el registro de resolución. "+e);
					Messages.error("No se puede crear el registro de resolución");
				}
			}
		}
		
		if (!Messages.hasErrors()) {
			EditarResolucionController.enviarRegistrarResolucionValidateRules();
		}
		
		/// 2. Crear el expediente de la resolución en el AED
		if (!Messages.hasErrors()) {
			if ((dbResolucionFAP.registro.fasesRegistro.registro) && (!dbResolucionFAP.registro.fasesRegistro.expedienteAed)) {
				// TODO: Crear expediente en el AED
				try {
					gestorDocumentalService.crearExpedienteResolucion(dbResolucionFAP);
					dbResolucionFAP.registro.fasesRegistro.expedienteAed = true;
					dbResolucionFAP.save();
				} catch (GestorDocumentalServiceException e) {
					play.Logger.error("Error. No se ha podido crear el expediente de la resolución el el AED.", e);
					Messages.error("Error. No se ha podido crear el expediente de la resolución el el AED.");
				}
			}
		}

		// 3. Clasificar el documento de resolución
		if (!Messages.hasErrors()) {
			if ((dbResolucionFAP.registro.fasesRegistro.expedienteAed) && (!dbResolucionFAP.registro.fasesRegistro.clasificarAed)) {
				try {
					gestorDocumentalService.clasificarDocumentoResolucion(dbResolucionFAP);
					dbResolucionFAP.registro.fasesRegistro.clasificarAed = true;
					dbResolucionFAP.save();
				} catch (GestorDocumentalServiceException e) {
					play.Logger.error("Error al clasificar el documento de la resolución.", e);
					Messages.error("Error al clasificar el documento de la resolución.");
				}
			}
		}
		
		if (!Messages.hasErrors()) {
			ResolucionBase.avanzarFase_Firmada(dbResolucionFAP);
			
			// Enviar correo al Jefe de Servicio correspondiente
			try {
				Agente agente = Agente.getAgenteByUsername(dbResolucionFAP.jefeDeServicio);
				Mails.enviar("registrarResolucion", agente);
			} catch (Exception e) {
				play.Logger.fatal("No se ha podido enviar el correo al Jefe de Servicio: "+dbResolucionFAP.jefeDeServicio+" de la resolución: "+dbResolucionFAP.id);
				Messages.error("No se ha podido enviar el correo al Jefe de Servicio: "+dbResolucionFAP.jefeDeServicio+" de la resolución: "+dbResolucionFAP.id);
			}
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarResolucionController.enviarRegistrarResolucionRender(idResolucionFAP);
	}
	
}
