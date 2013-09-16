package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityTransaction;
import javax.xml.ws.soap.SOAPFaultException;

import messages.Messages;
import models.Agente;
import models.ResolucionFAP;
import models.SolicitudGenerica;

import org.joda.time.DateTime;

import com.google.inject.Inject;

import platino.FirmaUtils;
import play.db.jpa.JPA;
import play.modules.guice.InjectSupport;
import play.mvc.Util;
import registroresolucion.RegistroResolucion;
import reports.Report;
import resolucion.ResolucionBase;
import services.FirmaServiceException;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.PortafirmaFapService;
import services.PortafirmaFapServiceException;
import services.RegistroLibroResolucionesService;
import services.RegistroLibroResolucionesServiceException;
import services.RegistroService;
import services.async.GestorDocumentalServiceAsync;
import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;
import validation.CustomValidation;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.EditarResolucionControllerGen;
import emails.Mails;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadoResolucionPublicacionEnum;
import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudResponseType;

@InjectSupport
public class EditarResolucionController extends EditarResolucionControllerGen {
	
	@Util
	private static ResolucionBase getResolucionObject (Long idResolucionFAP) throws Throwable {
		return ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
	}
	
	/**
	 * Expedientes que se muestran en la tabla para poder seleccionar
	 */
	public static void tablatablaExpedientes() {
		
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		
		// Obtenemos el objeto "ResolucionBase"
		ResolucionBase resolBase = null;
		try {
			resolBase = getResolucionObject(ids.get("idResolucionFAP"));
		} catch (Throwable e) {
			play.Logger.error("No se ha podido obtener el objeto resolución: "+ids.get("idResolucionFAP"));
		}
		
		java.util.List<SolicitudGenerica> rows = (List<SolicitudGenerica>) resolBase.getSolicitudesAResolver(ids.get("idResolucionFAP"));
		
		List<SolicitudGenerica> rowsFiltered = rows; //Tabla sin permisos, no filtra
		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estadoValue", "estado", "estadoUsuario", "solicitante.id", "solicitante.nombreCompleto"));
	}
	
	public static void tablatablaExpedientesUnico() {

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		
		// Obtenemos el objeto "ResolucionBase"
		ResolucionBase resolBase = null;
		try {
			resolBase = getResolucionObject(ids.get("idResolucionFAP"));
		} catch (Throwable e) {
			play.Logger.error("No se ha podido obtener el objeto resolución: "+ids.get("idResolucionFAP"));
		}
		java.util.List<SolicitudGenerica> rows = (List<SolicitudGenerica>) resolBase.getSolicitudesAResolver(ids.get("idResolucionFAP"));
		
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
				resolBase = getResolucionObject (idResolucionFAP);
				resolBase.prepararLineasResolucion(idResolucionFAP);
			} catch (Throwable e) {
				play.Logger.error("Error antes de obtener las líneas de resolución: " + e.getMessage());
				Messages.error("Error antes de obtener las líneas de resolución");
			}
		}

		if (!Messages.hasErrors()) {
			ResolucionFAP resolucion = EditarResolucionController.getResolucionFAP(idResolucionFAP);
			ResolucionBase resolBase = null;
			try {
				resolBase = getResolucionObject (idResolucionFAP);
				resolBase.setLineasDeResolucion(idResolucionFAP);
				resolBase.avanzarFase_Borrador(resolucion);
			} catch (Throwable e) {
				play.Logger.error("Error obteniendo tipo de resolución: " + e.getMessage());
				Messages.error("Error obteniendo el tipo de resolución");
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
			ResolucionBase resolBase = null;
			try {
				resolBase = getResolucionObject(idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
			resolBase.setLineasDeResolucion(idResolucionFAP);
			ResolucionFAP resolucion = EditarResolucionController.getResolucionFAP(idResolucionFAP);
			resolBase.avanzarFase_Borrador(resolucion);
		}
		index("editar", idResolucionFAP);
	}
	
	public static List<ComboItem> selectJefeServicio() {
		List<ComboItem> listaCombo = new ArrayList<ComboItem>();
		try {
			listaCombo = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getJefesServicio");
		} catch (Throwable e) {
			Messages.error("No se pudieron obtener los jefes de servicio");
			play.Logger.error("No se pudieron obtener los jefes de servicio"+e.getMessage());
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
				PortafirmaFapService portafirmaService = InjectorConfig.getInjector().getInstance(PortafirmaFapService.class);
				//String version = portafirmaService.obtenerVersion();
				//play.Logger.error("La versión del portafirma es: "+version);
				//Messages.error("La versión del portafirma es: "+version);
				PortafirmaCrearSolicitudResponse response = portafirmaService.crearSolicitudFirma(dbResolucionFAP);
				dbResolucionFAP.idSolicitudFirma = response.getIdSolicitud();
				dbResolucionFAP.hacePeticionPortafirma = AgenteController.getAgente();
				dbResolucionFAP.save();
			} catch (PortafirmaFapServiceException e) {
				play.Logger.error("Error al crear la solicitud de firma: " + e);
				Messages.error("Error al crear la solicitud de firma");
			} catch (SOAPFaultException e) {
				play.Logger.error("Error al crear la solicitud de firma: " + e);
				Messages.error("Error al crear la solicitud de firma");
			}
		}
		if (!Messages.hasErrors()) {
			ResolucionBase resolBase = null;
			try {
				resolBase = getResolucionObject(dbResolucionFAP.id);
				resolBase.avanzarFase_Preparada_Portafirma(dbResolucionFAP);
				dbResolucionFAP.save();
				Messages.ok("Se ha enviado correctamente al portafirma la solicitud de la firma del Jefe de Servicio");
			} catch (Throwable e) {
				play.Logger.error("No se ha enviado correctamente al portafirma la solicitud de firma: "+e);
				Messages.error("No se ha enviado correctamente al portafirma la solicitud de firma. ");
			}
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
		CustomValidation.required("resolucionFAP.numero_folios", resolucionFAP.numero_folios);
		dbResolucionFAP.numero_folios = resolucionFAP.numero_folios;

		if (dbResolucionFAP.fechaTopeFirma != null) {
			DateTime today = new DateTime().withTimeAtStartOfDay();
			if (dbResolucionFAP.fechaTopeFirma.isBefore(today)) {
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
			ResolucionBase resolBase = null;
			try {
				resolBase = getResolucionObject(idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
			resolBase.avanzarFase_FirmadaJefeServicio(dbResolucionFAP);
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
			ResolucionBase resolBase = null;
			try {
				resolBase = getResolucionObject(idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
			resolBase.avanzarFase_Preparada_FirmaJefeServicio(resolucionFAP);
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
				PortafirmaFapService portafirmaService = InjectorConfig.getInjector().getInstance(PortafirmaFapService.class);
				if (portafirmaService.comprobarSiResolucionFirmada(dbResolucionFAP.idSolicitudFirma)) {
					ResolucionBase resolBase = null;
					try {
						resolBase = getResolucionObject(idResolucionFAP);
					} catch (Throwable e) {
						new Exception ("No se ha podido obtener el objeto resolución", e);
					}
					Messages.ok("La solicitud de firma asociada a la resolución se ha firmado y finalizado correctamente.");
					resolBase.avanzarFase_PendienteFirmarDirector(dbResolucionFAP);
					dbResolucionFAP.registro.fasesRegistro.firmada = true;
					dbResolucionFAP.save();
				} else {
					play.Logger.warn("La resolución ["+dbResolucionFAP.id+"] no ha sido firmada y finalizada ");
					Messages.warning("El documento de resolución no ha sido firmado y finalizado");
					
					ObtenerEstadoSolicitudResponseType response = portafirmaService.obtenerEstadoFirma(dbResolucionFAP);
					if (response == null) {
						throw new PortafirmaFapServiceException("No se pudo obtener el estado de la firma: Response null. ");
					}
					play.Logger.info("El estado de la solicitud en el portafirma es: "+response.getEstado());
					Messages.warning("El estado de la solicitud en el portafirma es: "+response.getEstado());
					if (response.getEstado().equalsIgnoreCase("Rechazada")) {
						// TODO: Volver a estado anterior
						ResolucionBase resolBase = null;
						try {
							resolBase = getResolucionObject(idResolucionFAP);
						} catch (Throwable e) {
							new Exception ("No se ha podido obtener el objeto resolución", e);
						}
						resolBase.retrocederFase_Modificacion(dbResolucionFAP);
					} else {
						play.Logger.warn("La Solicitud está en el estado: "+response.getEstado()+ ": "+response.getComentario());
						Messages.warning("La Solicitud está en el estado: "+response.getEstado());
					}
				}
			} catch (PortafirmaFapServiceException e) {
				play.Logger.error("Error al comprobar si ya se ha firmado la resolución en el portafirma: " + e);
				Messages.error("Error al comprobar si ya se ha firmado la resolución en el portafirma.");
			} catch (SOAPFaultException e) {
				play.Logger.error("Error al comprobar si ya se ha firmado la resolución en el portafirma: " + e);
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
	public static void enviarRegistrarResolucion(Long idResolucionFAP, ResolucionFAP resolucionFAP, String btnRegistrarResolucion) {
		checkAuthenticity();
		if (!permisoEnviarRegistrarResolucion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		ResolucionFAP dbResolucionFAP = EditarResolucionController.getResolucionFAP(idResolucionFAP);
		RegistroResolucion datosRegistro = null;
		
		EntityTransaction tx = JPA.em().getTransaction();
		tx.commit();
		
		/// 1. Crear la resolución
		if (!Messages.hasErrors()) {
			if ((dbResolucionFAP.registro.fasesRegistro.firmada) && (!dbResolucionFAP.registro.fasesRegistro.registro)) {
				tx.begin();
				try {
					RegistroLibroResolucionesService registroLibroResolucionesService = InjectorConfig.getInjector().getInstance(RegistroLibroResolucionesService.class);
					datosRegistro = registroLibroResolucionesService.crearResolucion(dbResolucionFAP);
					dbResolucionFAP.codigoResolucion = Integer.toString(datosRegistro.numero);
					dbResolucionFAP.fechaRegistroResolucion = datosRegistro.fecha;
					dbResolucionFAP.folio_inicio = datosRegistro.primerFolio;
					dbResolucionFAP.folio_final = datosRegistro.ultimoFolio;
					dbResolucionFAP.numero = datosRegistro.numero;
					dbResolucionFAP.registro.fasesRegistro.registro = true;
					dbResolucionFAP.save();
				} catch (RegistroLibroResolucionesServiceException e) {
					play.Logger.error("No se puede crear el registro de resolución. "+e);
					Messages.error("No se puede crear el registro de resolución");
				}
				tx.commit();
			}
		}
		
		if (!Messages.hasErrors()) {
			EditarResolucionController.enviarRegistrarResolucionValidateRules();
		}

		GestorDocumentalServiceAsync gestorDocumentalServiceAsync = InjectorConfig.getInjector().getInstance(GestorDocumentalServiceAsync.class);
		
		/// 2. Crear el expediente de la convocatoria en el AED por si no existe
		if (!Messages.hasErrors()) {
			if ((dbResolucionFAP.registro.fasesRegistro.registro) && (!dbResolucionFAP.registro.fasesRegistro.expedienteAed)) {
				// TODO: Crear expediente en el AED
				tx.begin();
				try {
					await(gestorDocumentalServiceAsync.crearExpedienteConvocatoria());
					dbResolucionFAP.registro.fasesRegistro.expedienteAed = true;
					dbResolucionFAP.save();
				} catch (GestorDocumentalServiceException e) {
					play.Logger.error("Error. No se ha podido crear el expediente de la resolución el el AED.", e);
					Messages.error("Error. No se ha podido crear el expediente de la resolución el el AED.");
				}
				tx.commit();
			}
		}
		
		// 3. Clasificar el documento de resolución
		if (!Messages.hasErrors()) {
			if ((dbResolucionFAP.registro.fasesRegistro.expedienteAed) && (!dbResolucionFAP.registro.fasesRegistro.clasificarAed)) {
				tx.begin();
				try {
					await(gestorDocumentalServiceAsync.clasificarDocumentoResolucion(dbResolucionFAP));
					dbResolucionFAP.registro.fasesRegistro.clasificarAed = true;
					dbResolucionFAP.save();
				} catch (GestorDocumentalServiceException e) {
					play.Logger.error("Error al clasificar el documento de la resolución.", e);
					Messages.error("Error al clasificar el documento de la resolución.");
				}
				tx.commit();
			}
		}
		
		if (!Messages.hasErrors()) {
			ResolucionBase resolBase = null;
			tx.begin();
			try {
				resolBase = getResolucionObject(idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
			resolBase.avanzarFase_Firmada(dbResolucionFAP);
			
			// Enviar correo al Jefe de Servicio correspondiente
			try {
				Agente agente = dbResolucionFAP.hacePeticionPortafirma;
				ResolucionFAP resolucionfap = dbResolucionFAP;
				play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("resolucionfap", resolucionfap);
				play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("agente", agente);
				Mails.enviar("registrarResolucion", resolucionfap, agente);
			} catch (Exception e) {
				play.Logger.fatal("No se ha podido enviar el correo al Jefe de Servicio: "+dbResolucionFAP.jefeDeServicio+" de la resolución: "+dbResolucionFAP.id);
				Messages.error("No se ha podido enviar el correo al Jefe de Servicio: "+dbResolucionFAP.jefeDeServicio+" de la resolución: "+dbResolucionFAP.id);
			}
			tx.commit();
		}
		
		tx.begin();
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarResolucionController.enviarRegistrarResolucionRender(idResolucionFAP);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void publicarResolucion(Long idResolucionFAP, ResolucionFAP resolucionFAP, String btnPublicarResolucion) {
		checkAuthenticity();
		
		EntityTransaction tx = JPA.em().getTransaction();
		tx.commit();
		
		if (!permisoPublicarResolucion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		ResolucionFAP dbResolucionFAP = EditarResolucionController.getResolucionFAP(idResolucionFAP);

		EditarResolucionController.publicarResolucionBindReferences(resolucionFAP);

		if (!Messages.hasErrors()) {
			EditarResolucionController.publicarResolucionValidateCopy("editar", dbResolucionFAP, resolucionFAP);
		}

		ResolucionBase resolBase = null;
		
		if (!Messages.hasErrors()) {
			try {
				resolBase = getResolucionObject(idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		}
		
		
		if (!Messages.hasErrors() && ((dbResolucionFAP.estadoPublicacion == null) || (dbResolucionFAP.estadoPublicacion.isEmpty()))) {
			tx.begin();
			resolBase.publicarResolucion(idResolucionFAP);
			dbResolucionFAP.estadoPublicacion = EstadoResolucionPublicacionEnum.publicada.name();
			dbResolucionFAP.save();
			tx.commit();
		}
		
		
		
		if (!Messages.hasErrors() && (EstadoResolucionPublicacionEnum.publicada.name().equals(dbResolucionFAP.estadoPublicacion))) {
			tx.begin();
			resolBase.generarDocumentosResolucion(idResolucionFAP);
			dbResolucionFAP.estadoPublicacion = EstadoResolucionPublicacionEnum.documentoGenerado.name();
			dbResolucionFAP.save();
			tx.commit();
		}
		
		
		 if (!Messages.hasErrors() && (EstadoResolucionPublicacionEnum.documentoGenerado.name().equals(dbResolucionFAP.estadoPublicacion))
                 && (!EstadoResolucionEnum.publicada.name().equals(dbResolucionFAP.estado))) {
			tx.begin();
			resolBase.avanzarFase_Registrada(dbResolucionFAP);
			tx.commit();
		}
		
		tx.begin();
		
		if (!Messages.hasErrors()) {
			dbResolucionFAP.save();
			Messages.info("Se ha publicado la resolución");
			play.Logger.info("Se ha publicado la Resolución: "+dbResolucionFAP.id);
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarResolucionController.publicarResolucionRender(idResolucionFAP);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void firmarBaremacion(Long idResolucionFAP, String btnFirmarBaremacion) {
		checkAuthenticity();
		if (!permisoFirmarBaremacion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			EditarResolucionController.firmarBaremacionValidateRules();
		}
		
		if (!Messages.hasErrors()) {
			//Obtener tipo de Resolucion
			ResolucionBase resolucion = null;
			try {
				resolucion = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(resolucion.resolucion.conBaremacion){
				resolucion.firmarDocumentosBaremacionEnResolucion (resolucion);
			}
		}
		
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarResolucionController.firmarBaremacionRender(idResolucionFAP);
	}

	
}
