package controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityTransaction;
import javax.xml.ws.soap.SOAPFaultException;

import messages.Messages;
import models.Agente;
import models.Documento;
import models.LineaResolucionFAP;
import models.Registro;
import models.ResolucionFAP;
import models.SolicitudFirmaPortafirma;
import models.SolicitudGenerica;

import org.joda.time.DateTime;

import com.google.inject.Inject;

import platino.FirmaUtils;
import play.db.jpa.JPA;
import play.modules.guice.InjectSupport;
import play.mvc.Util;
import properties.FapProperties;
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
import services.platino.PlatinoBDOrganizacionServiceImpl;
import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;
import utils.ResolucionUtils;
import validation.CustomValidation;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.EditarResolucionControllerGen;
import emails.Mails;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadoResolucionPublicacionEnum;
import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudResponseType;
import es.gobcan.platino.servicios.organizacion.DBOrganizacionException_Exception;

@InjectSupport
public class EditarResolucionController extends EditarResolucionControllerGen {
	
	@Util
	private static ResolucionBase getResolucionObject (Long idResolucionFAP) throws Throwable {
		return ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
	}
	
	public static void index(String accion, Long idResolucionFAP) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("fap/EditarResolucion/EditarResolucion.html");
		}

		ResolucionFAP resolucionFAP = null;
		if ("crear".equals(accion)) {
			resolucionFAP = EditarResolucionController.getResolucionFAP();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				resolucionFAP.save();
				idResolucionFAP = resolucionFAP.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			resolucionFAP = EditarResolucionController.getResolucionFAP(idResolucionFAP);
		
		//play.mvc.Router.ActionDefinition ad = play.mvc.Router.reverse("EditarResolucion.index");
		//ad.absolute();
		//System.out.println("url = " + ad.toString());
		
		
		log.info("Visitando página: " + "gen/EditarResolucion/EditarResolucion.html" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		renderTemplate("fap/EditarResolucion/EditarResolucion.html", accion, idResolucionFAP, resolucionFAP);		
	}
	
	/**
	 * Expedientes que se muestran en la tabla para poder seleccionar
	 */
	public static void tablatablaExpedientes() {
		
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		// Obtenemos el objeto "ResolucionBase"
		ResolucionBase resolBase = null;
		Long idResolucionFAP = ids.get("idResolucionFAP");
		try {
			resolBase = getResolucionObject(idResolucionFAP);
		} catch (Throwable e) {
			play.Logger.error("No se ha podido obtener el objeto resolución: "+idResolucionFAP);
		}
		
		java.util.List<SolicitudGenerica> rows = (List<SolicitudGenerica>) resolBase.getSolicitudesAResolver(idResolucionFAP);
		
		List<SolicitudGenerica> rowsFiltered = rows; //Tabla sin permisos, no filtra
		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estadoValue", "estado", "estadoUsuario", "solicitante.id", "solicitante.nombreCompleto"));
	}
	
	public static void tablatablaExpedientesUnico() {

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");

		// Obtenemos el usuario conectado al sistema y el rol activo
		Agente usuario = AgenteController.getAgente();
		String rolActivo = usuario.getRolActivo();
		// Obtenemos el objeto "ResolucionBase"
		ResolucionBase resolBase = null;
		try {
			resolBase = getResolucionObject(ids.get("idResolucionFAP"));
		} catch (Throwable e) {
			play.Logger.error("No se ha podido obtener el objeto resolución: "+ids.get("idResolucionFAP"));
		}
		
		java.util.List<SolicitudGenerica> rows;
		// Listamos los expedientes segun la provincia a la que esta asignado el gestor o todos
		if(rolActivo.equals("gestorTenerife")){
			rows = (List<SolicitudGenerica>) resolBase.getSolicitudesAResolverSC(ids.get("idResolucionFAP"));
		} else if(rolActivo.equals("gestorLasPalmas")){
			rows = (List<SolicitudGenerica>) resolBase.getSolicitudesAResolverLP(ids.get("idResolucionFAP"));
		}else {
			rows = (List<SolicitudGenerica>) resolBase.getSolicitudesAResolver(ids.get("idResolucionFAP"));
		}
		
		List<SolicitudGenerica> rowsFiltered = rows; //Tabla sin permisos, no filtra
		tables.TableRenderResponse<SolicitudGenerica> response = new tables.TableRenderResponse<SolicitudGenerica>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("id", "expedienteAed.idAed", "estadoValue", "estado", "estadoUsuario", "solicitante.id", "solicitante.nombreCompleto"));
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void prepararResolucion(Long idResolucionFAP, String btnPrepararResolucion) {
	
		if (ResolucionBase.isGeneradoDocumentoResolucion()) {
			checkAuthenticity();
			if (!permisoPrepararResolucion("editar")) {
				Messages.error("No tiene permisos suficientes para realizar la acción");
			}

		
			if (!Messages.hasErrors()) {
				
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
		} else {
			ResolucionFAP resolucionFAP = EditarResolucionController.getResolucionFAP(idResolucionFAP);
			if (resolucionFAP.registro.oficial.uri == null) {
				redirect("AportarDocumentoResolucionController.index", AportarDocumentoResolucionController.getAccion(), idResolucionFAP);
			} else {
				redirect("CambiarDocumentoResolucionController.index", CambiarDocumentoResolucionController.getAccion(), idResolucionFAP);
			}
		}
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
	
	/**
	 * 
	 * @param id				Identificador de la resolución actual (idResolucionFAP)
	 * @param idsSeleccionados	Identificador del expediente seleccionado para su resolución
	 */
	public static void crearResolucion(Long id, List<Long> idsSeleccionados) {
		if (idsSeleccionados == null) {
			play.Logger.error("Se debe seleccionar un expediente");
			Messages.error("Se debe seleccionar un expediente");
			Messages.keep();
		} else if (idsSeleccionados.size() > 1) {
			play.Logger.error("Se debe seleccionar sólo un expediente");
			Messages.error("Se debe seleccionar sólo un expediente");
			Messages.keep();
		} else {
			ResolucionBase resolBase = null;
			try {
				resolBase = getResolucionObject(id);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
			resolBase.setLineasDeResolucion(id, idsSeleccionados);
			ResolucionFAP resolucion = EditarResolucionController.getResolucionFAP(id);
			resolBase.avanzarFase_Borrador(resolucion);
		}
		index("editar", id);
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
		ResolucionUtils.actualizarSolicitudesFirmaPortafirmaAntiguasResolucion(dbResolucionFAP);
		
		EditarResolucionController.formSelectJefeServicioBindReferences(resolucionFAP);
		
		if (!Messages.hasErrors()) {
			EditarResolucionController.formSelectJefeServicioValidateCopy("editar", dbResolucionFAP, resolucionFAP);
			
			if (properties.FapProperties.getBoolean("fap.platino.portafirma")) {
				PlatinoBDOrganizacionServiceImpl platinoDBOrgPort = InjectorConfig.getInjector().getInstance(PlatinoBDOrganizacionServiceImpl.class);
				try {
					dbResolucionFAP.solicitudFirmaPortafirma.uriFuncionarioSolicitante = platinoDBOrgPort.recuperarURIPersona(dbResolucionFAP.solicitudFirmaPortafirma.idSolicitante);
				} catch (DBOrganizacionException_Exception e) {
					play.Logger.error("Error al obtener la uri del funcionario solicitante en la Base de Datos de Organización: " + e.getMessage());
					Messages.error("Error al obtener la uri del funcionario solicitante en la Base de Datos de Organización.");
				}
				if ((dbResolucionFAP.solicitudFirmaPortafirma.uriFuncionarioSolicitante == null) || (dbResolucionFAP.solicitudFirmaPortafirma.uriFuncionarioSolicitante.isEmpty()))
					Messages.error("El usuario "+dbResolucionFAP.solicitudFirmaPortafirma.idSolicitante+" especificado no se encuentra en la Base de Datos de Organización.");
			}
		}

		if (!Messages.hasErrors()) {
			EditarResolucionController.formSelectJefeServicioValidateRules(dbResolucionFAP, resolucionFAP);

			try {
				dbResolucionFAP.solicitudFirmaPortafirma.agenteHaceSolicitud = AgenteController.getAgente();
				PortafirmaFapService portafirmaService = InjectorConfig.getInjector().getInstance(PortafirmaFapService.class);
				PortafirmaCrearSolicitudResponse response = portafirmaService.crearSolicitudFirma(dbResolucionFAP);
				dbResolucionFAP.solicitudFirmaPortafirma.uriSolicitud = response.getIdSolicitud();
				dbResolucionFAP.solicitudFirmaPortafirma.solicitudEstadoComentario = response.getComentarios();
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
				Messages.ok("Se ha enviado correctamente al portafirma la solicitud de la firma");
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
		
		CustomValidation.valid("resolucionFAP.solicitudFirmaPortafirma", resolucionFAP.solicitudFirmaPortafirma);
		CustomValidation.valid("resolucionFAP", resolucionFAP);
		CustomValidation.required("resolucionFAP.solicitudFirmaPortafirma.idDestinatario", resolucionFAP.solicitudFirmaPortafirma.idDestinatario);
		CustomValidation.validValueFromTable("resolucionFAP.solicitudFirmaPortafirma.idDestinatario", resolucionFAP.solicitudFirmaPortafirma.idDestinatario);
		dbResolucionFAP.solicitudFirmaPortafirma.idDestinatario = resolucionFAP.solicitudFirmaPortafirma.idDestinatario;
		CustomValidation.required("resolucionFAP.solicitudFirmaPortafirma.prioridad", resolucionFAP.solicitudFirmaPortafirma.prioridad);
		CustomValidation.validValueFromTable("resolucionFAP.solicitudFirmaPortafirma.prioridad", resolucionFAP.solicitudFirmaPortafirma.prioridad);
		dbResolucionFAP.solicitudFirmaPortafirma.prioridad = resolucionFAP.solicitudFirmaPortafirma.prioridad;
		CustomValidation.required("resolucionFAP.solicitudFirmaPortafirma.plazoMaximo", resolucionFAP.solicitudFirmaPortafirma.plazoMaximo);
		dbResolucionFAP.solicitudFirmaPortafirma.plazoMaximo = resolucionFAP.solicitudFirmaPortafirma.plazoMaximo;
		CustomValidation.required("resolucionFAP.numero_folios", resolucionFAP.numero_folios);
		dbResolucionFAP.numero_folios = resolucionFAP.numero_folios;
		if (properties.FapProperties.getBoolean("fap.platino.portafirma")) {
			CustomValidation.required("resolucionFAP.solicitudFirmaPortafirma.idSolicitante", resolucionFAP.solicitudFirmaPortafirma.idSolicitante);
			dbResolucionFAP.solicitudFirmaPortafirma.idSolicitante = resolucionFAP.solicitudFirmaPortafirma.idSolicitante;
			CustomValidation.required("resolucionFAP.solicitudFirmaPortafirma.passwordSolicitante", resolucionFAP.solicitudFirmaPortafirma.passwordSolicitante);
			dbResolucionFAP.solicitudFirmaPortafirma.passwordSolicitante = resolucionFAP.solicitudFirmaPortafirma.passwordSolicitante;
		}
		else {
			dbResolucionFAP.solicitudFirmaPortafirma.idSolicitante = FapProperties.get("portafirma.usuario");
		}
		
		if (dbResolucionFAP.solicitudFirmaPortafirma.plazoMaximo != null) {
			DateTime today = new DateTime().withTimeAtStartOfDay();
			if (dbResolucionFAP.solicitudFirmaPortafirma.plazoMaximo.isBefore(today)) {
				play.Logger.error("La fecha tope de firma no puede ser anterior a hoy.");
				CustomValidation.error("La fecha tope de firma no puede ser anterior a hoy.","resolucionFAP.solicitudFirmaPortafirma.plazoMaximo", resolucionFAP.solicitudFirmaPortafirma.plazoMaximo);
			}
			int dias = 0;
			// Comprobar la fecha de tope de firma con el ResolucionBase
			try {
				dias = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getDiasLimiteFirma", dbResolucionFAP.id);
				DateTime diaLimite = new DateTime();
				diaLimite = diaLimite.plusDays(dias);
				if (diaLimite.isBefore(dbResolucionFAP.solicitudFirmaPortafirma.plazoMaximo)) {
					play.Logger.error("La fecha tope de firma no puede ser posterior a "+diaLimite+".");
					CustomValidation.error("La fecha tope de firma no puede ser posterior a "+diaLimite+".", "resolucionFAP.solicitudFirmaPortafirma.plazoMaximo", resolucionFAP.solicitudFirmaPortafirma.plazoMaximo);					
				}
			} catch (Throwable e) {
				e.printStackTrace();
				play.Logger.error("No se ha podido calcular el límite de fecha para la firma."+e);
				CustomValidation.error("No se ha podido calcular el límite de fecha para la firma", "resolucionFAP.solicitudFirmaPortafirma.plazoMaximo", resolucionFAP.solicitudFirmaPortafirma.plazoMaximo);
			}
		}
		
		dbResolucionFAP.save();
		
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
			ResolucionUtils.actualizarSolicitudesFirmaPortafirmaAntiguasResolucion(dbResolucionFAP);
			try {
				PortafirmaFapService portafirmaService = InjectorConfig.getInjector().getInstance(PortafirmaFapService.class);
				if (portafirmaService.comprobarSiSolicitudFirmada(dbResolucionFAP.solicitudFirmaPortafirma)) {
					ResolucionBase resolBase = null;
					try {
						resolBase = getResolucionObject(idResolucionFAP);
					} catch (Throwable e) {
						new Exception ("No se ha podido obtener el objeto resolución", e);
					}
					Messages.ok("La solicitud de firma asociada a la resolución se ha firmado y finalizado correctamente.");
					resolBase.avanzarFase_PendienteFirmarDirector(dbResolucionFAP);
					dbResolucionFAP.registro.fasesRegistro.firmada = true;
					// TODO: ¿SE DEBE ELIMINAR?
					//portafirmaService.eliminarSolicitudFirma(dbResolucionFAP.solicitudFirmaPortafirma);
					dbResolucionFAP.save();
				} else {
					play.Logger.warn("La resolución ["+dbResolucionFAP.id+"] no ha sido firmada y finalizada ");
					Messages.warning("El documento de resolución no ha sido firmado y finalizado");
					String estado = portafirmaService.obtenerEstadoFirma(dbResolucionFAP.solicitudFirmaPortafirma);
					if (estado == null) {
						throw new PortafirmaFapServiceException("No se pudo obtener el estado de la firma: Response null. ");
					}
					play.Logger.info("El estado de la solicitud en el portafirma es: "+estado);
					Messages.warning("El estado de la solicitud en el portafirma es: "+estado);
					if (estado.equalsIgnoreCase("Rechazada")) {
						//Volver a estado anterior
						ResolucionBase resolBase = null;
						try {
							resolBase = getResolucionObject(idResolucionFAP);
						} catch (Throwable e) {
							new Exception ("No se ha podido obtener el objeto resolución", e);
						}
						resolBase.retrocederFase_Modificacion(dbResolucionFAP);
						//portafirmaService.eliminarSolicitudFirma(dbResolucionFAP.solicitudFirmaPortafirma);
						dbResolucionFAP.save();
					} else {
						play.Logger.warn("La Solicitud está en el estado: " + estado);
						//TODO: Recuperar comentario del response (que ahora no existe)
						//play.Logger.warn("La Solicitud está en el estado: "+estado+ ": "+response.getComentario());
						Messages.warning("La Solicitud está en el estado: " + estado);
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

		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		
		/// 2. Crear el expediente de la convocatoria en el AED por si no existe
		if (!Messages.hasErrors()) {
			if ((dbResolucionFAP.registro.fasesRegistro.registro) && (!dbResolucionFAP.registro.fasesRegistro.expedienteAed)) {
				// TODO: Crear expediente en el AED
				tx.begin();
				try {
					gestorDocumentalService.crearExpedienteConvocatoria();
					dbResolucionFAP.registro.fasesRegistro.expedienteAed = true;
					dbResolucionFAP.save();
				} catch (GestorDocumentalServiceException e) {
					play.Logger.error("Error. No se ha podido crear el expediente de la resolución el el AED.", e);
					Messages.error("Error. No se ha podido crear el expediente de la resolución el el AED.");
				}
				tx.commit();
			}
		}
		
		// 3. Clasificar el documento de resolución y de consultas
		if (!Messages.hasErrors()) {
			if ((dbResolucionFAP.registro.fasesRegistro.expedienteAed) && (!dbResolucionFAP.registro.fasesRegistro.clasificarAed)) {
				tx.begin();
				try {
					gestorDocumentalService.clasificarDocumentoResolucion(dbResolucionFAP);
					gestorDocumentalService.clasificarDocumentosConsulta(dbResolucionFAP);
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
				Agente agente = dbResolucionFAP.solicitudFirmaPortafirma.agenteHaceSolicitud;
				ResolucionFAP resolucionfap = dbResolucionFAP;
				play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("resolucionfap", resolucionfap);
				play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("agente", agente);
				Mails.enviar("registrarResolucion", resolucionfap, agente);
			} catch (Exception e) {
				play.Logger.fatal("No se ha podido enviar el correo al Jefe de Servicio: "+dbResolucionFAP.solicitudFirmaPortafirma.idDestinatario+" de la resolución: "+dbResolucionFAP.id);
				Messages.error("No se ha podido enviar el correo al Jefe de Servicio: "+dbResolucionFAP.solicitudFirmaPortafirma.idDestinatario+" de la resolución: "+dbResolucionFAP.id);
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

	/**
	 * 
	 * @param id				Identificador de la resolución actual (idResolucionFAP)
	 * @param idsSeleccionados	Identificadores de los expedientes seleccionados para su resolución
	 */
	public static void seleccionar(Long id, List<Long> idsSeleccionados) {
		if (idsSeleccionados == null) {
			play.Logger.error("Se debe seleccionar al menos un expediente");
			Messages.error("Se debe seleccionar al menos un expediente");
			Messages.keep();
		} else {
			ResolucionBase resolBase = null;
			try {
				resolBase = getResolucionObject(id);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
			resolBase.setLineasDeResolucion(id, idsSeleccionados);
			ResolucionFAP resolucion = EditarResolucionController.getResolucionFAP(id);
			resolBase.avanzarFase_Borrador(resolucion);
		}
		index("editar", id);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void copiaExpediente(Long idResolucionFAP, String btnCopiaExpediente) {
		checkAuthenticity();
		if (!permisoCopiaExpediente("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			EditarResolucionController.copiaExpedienteValidateRules();
		}
		ResolucionBase resolBase = null;
		if (!Messages.hasErrors()) {
			try {
				resolBase = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucionFAP);
			} catch (Throwable e) {
				new Exception ("No se ha podido obtener el objeto resolución", e);
			}
		}
		if (!Messages.hasErrors()) {
			resolBase.copiarEnExpedientes(idResolucionFAP);
			//resolBase.resolucion.estadoPublicacion = EstadoResolucionPublicacionEnum.publicada.name();
			resolBase.resolucion.save();
		}
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PaginaPublicarResolucion/PaginaPublicarResolucion.html" + " , intentada sin éxito (Problemas de Validación)");
		EditarResolucionController.copiaExpedienteRender(idResolucionFAP);
	
	}
	
	public static void tablalineasResolucion(Long idResolucionFAP) {
		boolean flag = true;
		java.util.List<LineaResolucionFAP> rows = LineaResolucionFAP.find("select lineaResolucionFAP from ResolucionFAP resolucionFAP join resolucionFAP.lineasResolucion lineaResolucionFAP where resolucionFAP.id=?", idResolucionFAP).fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<LineaResolucionFAP> rowsFiltered = rows; //Tabla sin permisos, no filtra
		
		tables.TableRenderResponse<LineaResolucionFAP> response = new tables.TableRenderResponse<LineaResolucionFAP>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);
		
		for (LineaResolucionFAP row:rows) {
			if (row.registro == null) {
				row.registro = new Registro();
				row.save();
			}
			if ((row.registro.oficial.uri == null) || (row.registro.fasesRegistro.firmada == null) || (row.registro.fasesRegistro.firmada == false)){
				flag = false;
				break;
			}
		}
		if (!flag) {
			renderJSON(response.toJSON("id", "solicitud.expedienteAed.idAed", "solicitud.estado", "solicitud.solicitante.numeroId", "solicitud.solicitante.nombreCompleto", "estado"));
		}
		else {
			renderJSON(response.toJSON("id", "solicitud.expedienteAed.idAed", "solicitud.estado", "solicitud.solicitante.numeroId", "solicitud.solicitante.nombreCompleto", "estado", "registro.oficial.enlaceDescargaFirmado", "registro.justificante.enlaceDescarga"));
		}
		
	}
	
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void volverExpedientes(Long idResolucionFAP, String btnVolverExpedientes) {
		checkAuthenticity();
		if (!permisoVolverExpedientes("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {

		}

		if (!Messages.hasErrors()) {
			EditarResolucionController.volverExpedientesValidateRules();
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {
			// Obtenemos la resolucion que estamos editando
			ResolucionFAP resolucionFAP = getResolucionFAP(idResolucionFAP);
			// Antes de volver cambiamos el estado de la resolucion a borrador 
			resolucionFAP.estado = "borrador";
			// Borramos las lineas de resolucion y guardamos la resolucion en la BBDD
			resolucionFAP.lineasResolucion.clear();
			resolucionFAP.save();
			
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/EditarResolucion/EditarResolucion.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		EditarResolucionController.volverExpedientesRender(idResolucionFAP);
	}
	
}
