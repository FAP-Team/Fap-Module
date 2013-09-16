package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import messages.Messages;
import messages.Messages.MessageType;
import models.Agente;
import models.Documento;
import models.Firmante;
import models.Registro;
import models.RegistroModificacion;
import models.SolicitudGenerica;
import models.TableKeyValue;
import platino.FirmaUtils;
import play.mvc.Util;
import properties.FapProperties;
import services.RegistroServiceException;
import tramitacion.TramiteBase;
import controllers.fap.AgenteController;
import controllers.fap.PresentacionFapController;
import controllers.fap.PresentacionModificacionFapController;
import controllers.gen.SolicitudPresentarModificacionFAPControllerGen;

public class SolicitudPresentarModificacionFAPController extends SolicitudPresentarModificacionFAPControllerGen {
	
	public static void index(String accion, Long idSolicitud, Long idRegistroModificacion, Long idRegistro) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("gen/SolicitudPresentarModificacionFAP/SolicitudPresentarModificacionFAP.html");
		}

		SolicitudGenerica solicitud = SolicitudPresentarModificacionFAPController.getSolicitudGenerica(idSolicitud);
		idRegistroModificacion=solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).id;
		RegistroModificacion registroModificacion = SolicitudPresentarModificacionFAPController.getRegistroModificacion(idSolicitud, idRegistroModificacion);
		idRegistro = registroModificacion.registro.id;
		
		Registro registro = null;
		if ("crear".equals(accion)) {
			registro = SolicitudPresentarModificacionFAPController.getRegistro();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				registro.save();
				idRegistro = registro.id;
				registroModificacion.registro = registro;
				registroModificacion.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion)){
			registro = SolicitudPresentarModificacionFAPController.getRegistro(idRegistroModificacion, idRegistro);
		}

		log.info("Visitando página: " + "gen/SolicitudPresentarModificacionFAP/SolicitudPresentarModificacionFAP.html");
		renderTemplate("gen/SolicitudPresentarModificacionFAP/SolicitudPresentarModificacionFAP.html", accion, idSolicitud, idRegistroModificacion, idRegistro, solicitud, registroModificacion, registro);
	}
	
	@Util
	public static Registro getRegistro(Long idRegistroModificacion, Long idRegistro) {
		Registro registro = null;

		if (idRegistroModificacion == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idRegistroModificacion"))
				Messages.fatal("Falta parámetro idRegistroModificacion");
		}

		if (idRegistro == null) {
			RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
			idRegistro = registroModificacion.registro.id;
		}
		if (idRegistroModificacion != null && idRegistro != null) {
			registro = Registro.find("select registro from RegistroModificacion registroModificacion join registroModificacion.registro registro where registroModificacion.id=? and registro.id=?", idRegistroModificacion, idRegistro).first();
			if (registro == null)
				Messages.fatal("Error al recuperar Registro");
		}
		return registro;
	}
	
	@Util
	public static RegistroModificacion getRegistroModificacion(Long idSolicitud, Long idRegistroModificacion) {
		RegistroModificacion registroModificacion = null;

		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		}

		if (idRegistroModificacion == null) {
			SolicitudGenerica solicitud = PresentarModificacionFAPController.getSolicitudGenerica(idSolicitud);
			idRegistroModificacion=solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).id;
			if (idRegistroModificacion == null) {
				if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idRegistroModificacion"))
					Messages.fatal("Falta parámetro idRegistroModificacion");
			}
		}
		if (idSolicitud != null && idRegistroModificacion != null) {
			registroModificacion = RegistroModificacion.find("select registroModificacion from SolicitudGenerica solicitud join solicitud.registroModificacion registroModificacion where solicitud.id=? and registroModificacion.id=?", idSolicitud, idRegistroModificacion).first();
			if (registroModificacion == null)
				Messages.fatal("Error al recuperar RegistroModificacion");
		}
		return registroModificacion;
	}
	
	@Util
	public static void firmarRegistrarFHFormFirmaFH(Long idSolicitud, Long idRegistroModificacion, Long idRegistro, String firma) {
		SolicitudGenerica solicitud = SolicitudPresentarModificacionFAPController.getSolicitudGenerica(idSolicitud);
		idRegistroModificacion=solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).id;
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		idRegistro = registroModificacion.registro.id;
		Registro registro = SolicitudPresentarModificacionFAPController.getRegistro(idRegistroModificacion, idRegistro);

		play.Logger.info("Metodo: firmarRegistrarFHFormFirmaFH");
		Agente agente = AgenteController.getAgente();
		if (agente.getFuncionario()){
			List<Firmante> firmantes = new ArrayList<Firmante>();
			firmantes.add(new Firmante(agente));
			FirmaUtils.firmar(registro.oficial, firmantes, firma, null);
		} else {
			//ERROR
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		if (!Messages.hasErrors()) {

			registro.fasesRegistro.firmada = true;
			registro.save();
		}
	}
	
	@Util
	public static void firmarRegistrarNifFormFirmaPF(Long idSolicitud, Long idRegistroModificacion, Long idRegistro, String firma) {
		SolicitudGenerica solicitud = SolicitudPresentarModificacionFAPController.getSolicitudGenerica(idSolicitud);
		idRegistroModificacion=solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).id;
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		idRegistro = registroModificacion.registro.id;
		Registro registro = SolicitudPresentarModificacionFAPController.getRegistro(idRegistroModificacion, idRegistro);

		play.Logger.info("Metodo: firmarRegistrarNifFormFirmaPF");
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		Map<String, Object> vars = new HashMap<String, Object>();
		if (secure.checkAcceso("editarFirma", "editar", ids, vars)) {
			if (registro.firmantes.todos == null || registro.firmantes.todos.size() == 0) {
				registro.firmantes.todos = solicitud.solicitante.calcularFirmantes();
				registro.firmantes.save();
			}
			FirmaUtils.firmar(registro.oficial, registro.firmantes.todos, firma, null);
		} else {
			//ERROR
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		if (!Messages.hasErrors()) {

			registro.fasesRegistro.firmada = true;
			registro.save();

			registroModificacion.save();
		}
	}
	
	@Util
	public static void firmarRepresentanteFormFirmaRepresentante(Long idSolicitud, Long idRegistroModificacion, Long idRegistro, String firma) {
		SolicitudGenerica solicitud = SolicitudPresentarModificacionFAPController.getSolicitudGenerica(idSolicitud);
		idRegistroModificacion=solicitud.registroModificacion.get(solicitud.registroModificacion.size()-1).id;
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		idRegistro = registroModificacion.registro.id;
		Registro registro = SolicitudPresentarModificacionFAPController.getRegistro(idRegistroModificacion, idRegistro);

		play.Logger.info("Metodo: firmarRepresentanteFormFirmaRepresentante");
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		Map<String, Object> vars = new HashMap<String, Object>();
		if (secure.checkAcceso("editarFirma", "editar", ids, vars)) {
			if (registro.firmantes.todos == null || registro.firmantes.todos.size() == 0) {
				registro.firmantes.todos = solicitud.solicitante.calcularFirmantes();
				registro.firmantes.save();
			}
			FirmaUtils.firmar(registro.oficial, registro.firmantes.todos, firma, null);
		} else {
			//ERROR
			Messages.error("No tiene permisos suficientes para realizar la acción++");
		}
		if (!Messages.hasErrors()) {
			registro.fasesRegistro.firmada = true;
			registroModificacion.save();
			registro.save();
		}
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formFirmaFH(Long idSolicitud, Long idRegistroModificacion, Long idRegistro, String firma, String firmarRegistrarFH) {
		checkAuthenticity();
		if (!permisoFormFirmaFH("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = SolicitudPresentarModificacionFAPController.getSolicitudGenerica(idSolicitud);
		idRegistroModificacion=dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size()-1).id;
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		idRegistro = registroModificacion.registro.id;
		Registro dbRegistro = SolicitudPresentarModificacionFAPController.getRegistro(idRegistroModificacion, idRegistro);

		if (!Messages.hasErrors()) {
			try {
				PresentacionModificacionFapController.invoke("comprobarFechaLimitePresentacion", idSolicitud);
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar los métodos comprobarFechaLimitePresentacion: "+e1.getMessage());
				Messages.error("Error al validar las comprobaciones de la Fecha Límite de Presentación");
			}
		}
		
		if (!Messages.hasErrors()) {
			try {
				PresentacionModificacionFapController.invoke("beforeFirma", idSolicitud);
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar los métodos beforeFirma: "+e1.getMessage());
				Messages.error("Error al validar elementos previos a la firma");
			}
		}
		
		if (!Messages.hasErrors()) {
			try {
				TramiteBase tramite = PresentacionModificacionFapController.invoke("getTramiteObject", idSolicitud);
				SolicitudPresentarModificacionFAPController.firmarRegistrarFHFormFirmaFH(idSolicitud, idRegistroModificacion, idRegistro, firma);
				
				if (!Messages.hasErrors()) {
					try {
						PresentacionModificacionFapController.invoke("afterFirma", idSolicitud);
					} catch (Throwable e1) {
						log.error("Hubo un problema al invocar los métodos afterFirma: "+e1.getMessage());
						Messages.error("Error al validar elementos posteriores a la firma");
					}
				}
				
				if (!Messages.hasErrors()) {
					try {
						PresentacionModificacionFapController.invoke("beforeRegistro", idSolicitud);
					} catch (Throwable e1) {
						log.error("Hubo un problema al invocar los métodos beforeRegistro: "+e1.getMessage());
						Messages.error("Error al validar elementos previos al registro");
					}
				}
				
				if (!Messages.hasErrors()) {
					try {
						tramite.registrar();
						if (dbRegistro.fasesRegistro.clasificarAed){
							tramite.cambiarEstadoSolicitud();
							registroModificacion.fechaRegistro = dbRegistro.informacionRegistro.fechaRegistro;
							registroModificacion.save();
						}
						else{
							play.Logger.error("No se registro la solicitud correctamente por lo que no se cambiara el estado de la misma.");
							Messages.error("Error al intentar sólo registrar.");
						}
						if (!Messages.hasErrors()) {
							try {
								PresentacionModificacionFapController.invoke("afterRegistro", idSolicitud);
							} catch (Throwable e1) {
								log.error("Hubo un problema al invocar los métodos afterRegistro: "+e1.getMessage());
								Messages.error("Error al validar elementos posteriores al registro");
							}
						}
					} catch (Exception e) {
						log.error("Hubo un error al registrar la solicitud de modificación: "+ e.getMessage());
						Messages.error("No se pudo registrar la solicitud de modificación");
					}
				}
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar el metodo que devuelve la clase TramiteBase en la firma: "+e1.getMessage());
				Messages.error("Error al intentar firmar antes de registrar");
			}
		}

		if (!Messages.hasErrors()) {
			SolicitudPresentarModificacionFAPController.formFirmaFHValidateRules(firma);
		}
		if (!Messages.hasErrors()) {
			Messages.ok("Solicitud Firmada y Registrada correctamente");
			log.info("Acción Editar de página: " + "gen/SolicitudPresentarModificacionFAP/SolicitudPresentarModificacionFAP.html" + " , intentada con éxito");
			redirect("PresentarModificacionFAPController.index", "editar", idSolicitud, idRegistroModificacion, idRegistro);
		} else
			log.info("Acción Editar de página: " + "gen/SolicitudPresentarModificacionFAP/SolicitudPresentarModificacionFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		SolicitudPresentarModificacionFAPController.formFirmaFHRender(idSolicitud, idRegistroModificacion, idRegistro);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formFirmaPF(Long idSolicitud, Long idRegistroModificacion, Long idRegistro, String firma, String firmarRegistrarNif) {
		checkAuthenticity();
		if (!permisoFormFirmaPF("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = SolicitudPresentarModificacionFAPController.getSolicitudGenerica(idSolicitud);
		idRegistroModificacion=dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size()-1).id;
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		idRegistro = registroModificacion.registro.id;
		Registro dbRegistro = SolicitudPresentarModificacionFAPController.getRegistro(idRegistroModificacion, idRegistro);

		if (!Messages.hasErrors()) {
				try {
					PresentacionModificacionFapController.invoke("comprobarFechaLimitePresentacion", idSolicitud);
				} catch (Throwable e1) {
					log.error("Hubo un problema al invocar los métodos comprobarFechaLimitePresentacion: "+e1.getMessage());
					Messages.error("Error al validar las comprobaciones de la Fecha Límite de Presentación");
				}
			}
			
			if (!Messages.hasErrors()) {
				try {
					PresentacionModificacionFapController.invoke("beforeFirma", idSolicitud);
				} catch (Throwable e1) {
					log.error("Hubo un problema al invocar los métodos beforeFirma: "+e1.getMessage());
					Messages.error("Error al validar elementos previos a la firma");
				}
			}
			
			if (!Messages.hasErrors()) {
				try {
					TramiteBase tramite = PresentacionModificacionFapController.invoke("getTramiteObject", idSolicitud);
					tramite.firmar(firma);
					
					if (!Messages.hasErrors()) {
						try {
							PresentacionModificacionFapController.invoke("afterFirma", idSolicitud);
						} catch (Throwable e1) {
							log.error("Hubo un problema al invocar los métodos afterFirma: "+e1.getMessage());
							Messages.error("Error al validar elementos posteriores a la firma");
						}
					}
					
					if (!Messages.hasErrors()) {
						try {
							PresentacionModificacionFapController.invoke("beforeRegistro", idSolicitud);
						} catch (Throwable e1) {
							log.error("Hubo un problema al invocar los métodos beforeRegistro: "+e1.getMessage());
							Messages.error("Error al validar elementos previos al Registro");
						}
					}
					
					if (!Messages.hasErrors()) {
						try {
							tramite.registrar();
							if (dbRegistro.fasesRegistro.clasificarAed){
								tramite.cambiarEstadoSolicitud();
								registroModificacion.fechaRegistro = dbRegistro.informacionRegistro.fechaRegistro;
								registroModificacion.save();
							}
							else{
								play.Logger.error("No se registro la solicitud correctamente por lo que no se cambiara el estado de la misma.");
								Messages.error("Error al intentar sólo registrar.");
							}
							if (!Messages.hasErrors()) {
								try {
									PresentacionModificacionFapController.invoke("afterRegistro", idSolicitud);
								} catch (Throwable e1) {
									log.error("Hubo un problema al invocar los métodos afterRegistro: "+e1.getMessage());
									Messages.error("Error al validar elementos posteriores al registro");
								}
							}
						} catch (Exception e) {
							log.error("Hubo un error al registrar la solicitud de modificación: "+ e.getMessage());
							Messages.error("No se pudo registrar la solicitud de modificación");
						}
					}
				} catch (Throwable e1) {
					log.error("Hubo un problema al invocar el metodo que devuelve la clase TramiteBase en la firma: "+e1.getMessage());
					Messages.error("Error al intentar firmar antes de registrar");
				}
			}

		if (!Messages.hasErrors()) {
			SolicitudPresentarModificacionFAPController.formFirmaPFValidateRules(firma);
		}
		if (!Messages.hasErrors()) {
			Messages.ok("Solicitud registrada correctamente");
			log.info("Acción Editar de página: " + "gen/SolicitudPresentarModificacionFAP/SolicitudPresentarModificacionFAP.html" + " , intentada con éxito");
			redirect("PresentarModificacionFAPController.index", "editar", idSolicitud, idRegistroModificacion, idRegistro);
		} else
			log.info("Acción Editar de página: " + "gen/SolicitudPresentarModificacionFAP/SolicitudPresentarModificacionFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		SolicitudPresentarModificacionFAPController.formFirmaPFRender(idSolicitud, idRegistroModificacion, idRegistro);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formFirmaRepresentante(Long idSolicitud, Long idRegistroModificacion, Long idRegistro, String firma, String firmarRepresentante) {
		checkAuthenticity();
		if (!permisoFormFirmaRepresentante("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		SolicitudGenerica dbSolicitud = SolicitudPresentarModificacionFAPController.getSolicitudGenerica(idSolicitud);
		idRegistroModificacion=dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size()-1).id;
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		idRegistro = registroModificacion.registro.id;
		Registro dbRegistro = SolicitudPresentarModificacionFAPController.getRegistro(idRegistroModificacion, idRegistro);

		if (!Messages.hasErrors()) {
			try {
				PresentacionModificacionFapController.invoke("comprobarFechaLimitePresentacion", idSolicitud);
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar los métodos comprobarFechaLimitePresentacion: "+e1.getMessage());
				Messages.error("Error al validar las comprobaciones de la Fecha Límite de Presentación");
			}
		}

		if (!Messages.hasErrors()) {
			try {
				PresentacionModificacionFapController.invoke("beforeFirma", idSolicitud);
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar los métodos beforeFirma, en la firma con representantes: "+e1.getMessage());
				Messages.error("Error al validar elementos previos a la firma de representante");
			}
		}
		
		if (!Messages.hasErrors()) {
			try {
				TramiteBase tramite = PresentacionModificacionFapController.invoke("getTramiteObject", idSolicitud);
				tramite.firmar(firma);
			} catch (Throwable e1) {
				log.error("Hubo un problema al firmar con representante en presentacion: "+e1.getMessage());
				Messages.error("Error al intentar firmar el representante");
			}
		}
		
		if (!Messages.hasErrors()) {
			try {
				PresentacionModificacionFapController.invoke("afterFirma", idSolicitud);
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar los métodos afterFirma, en la firma con representantes: "+e1.getMessage());
				Messages.error("Error al validar elementos posteriores a la firma de representante");
			}
		}

		if (!Messages.hasErrors()) {
			SolicitudPresentarModificacionFAPController.formFirmaRepresentanteValidateRules(firma);
		}
		if (!Messages.hasErrors()) {
			Messages.ok("Solicitud Firmada correctamente.");
			log.info("Acción Editar de página: " + "gen/SolicitudPresentarModificacionFAP/SolicitudPresentarModificacionFAP.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/SolicitudPresentarModificacionFAP/SolicitudPresentarModificacionFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		SolicitudPresentarModificacionFAPController.formFirmaRepresentanteRender(idSolicitud, idRegistroModificacion, idRegistro);
	}
	
	@Util
	public static void formFirmaFHRender(Long idSolicitud, Long idRegistroModificacion, Long idRegistro) {
		Messages.keep();
		redirect("SolicitudPresentarModificacionFAPController.index", "editar", idSolicitud, idRegistroModificacion, idRegistro);
	}
	
	public static void tablatablaFirmantesHecho(Long idRegistro) {

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		SolicitudGenerica dbSolicitud = SolicitudPresentarModificacionFAPController.getSolicitudGenerica(ids.get("idSolicitud"));
		Long idRegistroModificacion=dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size()-1).id;
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		idRegistro = registroModificacion.registro.id;
		java.util.List<Firmante> rows = Firmante.find("select firmante from Registro registro join registro.firmantes.todos firmante where registro.id=? and firmante.tipo=? and firmante.fechaFirma is not null", idRegistro, "representante").fetch();

		List<Firmante> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Firmante> response = new tables.TableRenderResponse<Firmante>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("idvalor", "nombre", "fechaFirma", "id"));
	}

	public static void tablatablaFirmantesEspera(Long idRegistro) {

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		SolicitudGenerica dbSolicitud = SolicitudPresentarModificacionFAPController.getSolicitudGenerica(ids.get("idSolicitud"));
		Long idRegistroModificacion=dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size()-1).id;
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		idRegistro = registroModificacion.registro.id;
		
		java.util.List<Firmante> rows = Firmante.find("select firmante from Registro registro join registro.firmantes.todos firmante where registro.id=? and firmante.tipo=? and firmante.fechaFirma is not null", idRegistro, "representante").fetch();

		List<Firmante> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Firmante> response = new tables.TableRenderResponse<Firmante>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("idvalor", "nombre", "id"));
	}
	
	@Util
	public static void formFirmaPFRender(Long idSolicitud, Long idRegistroModificacion, Long idRegistro) {
		Messages.keep();
		redirect("SolicitudPresentarModificacionFAPController.index", "editar", idSolicitud, idRegistroModificacion, idRegistro);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmRegistrar(Long idSolicitud, Long idRegistroModificacion, Long idRegistro, String botonRegistrar) {
		checkAuthenticity();
		if (!permisoFrmRegistrar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		SolicitudGenerica dbSolicitud = SolicitudPresentarModificacionFAPController.getSolicitudGenerica(idSolicitud);
		idRegistroModificacion=dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size()-1).id;
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		Registro dbRegistro = SolicitudPresentarModificacionFAPController.getRegistro(idRegistroModificacion, idRegistro);
		
		if (!Messages.hasErrors()) {
			try {
				PresentacionModificacionFapController.invoke("comprobarFechaLimitePresentacion", idSolicitud);
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar los métodos comprobarFechaLimitePresentacion: "+e1.getMessage());
				Messages.error("Error al validar las comprobaciones de la Fecha Límite de Presentación");
			}
		}
		
		if (!Messages.hasErrors()) {
			try {
				PresentacionModificacionFapController.invoke("beforeRegistro", idSolicitud);
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar los métodos beforeRegistro: "+e1.getMessage());
				Messages.error("Error al validar elementos previos al Registro");
			}
		}
		
		if (!Messages.hasErrors()) {
			
			try {
				TramiteBase tramite = PresentacionModificacionFapController.invoke("getTramiteObject", idSolicitud);
				try {
					dbRegistro.fasesRegistro.borrador = true;
					dbRegistro.fasesRegistro.firmada = true;
					tramite.registrar();
					if (dbRegistro.fasesRegistro.clasificarAed){
						tramite.cambiarEstadoSolicitud();
						registroModificacion.fechaRegistro = dbRegistro.informacionRegistro.fechaRegistro;
						registroModificacion.save();
					}
					else{
						play.Logger.error("No se registro la solicitud de modificación correctamente por lo que no se cambiara el estado de la misma.");
						Messages.error("Error al intentar sólo registrar.");
					}
					if (!Messages.hasErrors()) {
						try {
							PresentacionModificacionFapController.invoke("afterRegistro", idSolicitud);
						} catch (Throwable e1) {
							log.error("Hubo un problema al invocar los métodos afterRegistro: "+e1.getMessage());
							Messages.error("Error al validar elementos posteriores al registro");
						}
					}
				} catch (RegistroServiceException e) {
					log.error("Error al intentar registrar en la presentacion en frmRegistrar: "+e.getMessage());
					Messages.error("Error al intentar sólo registrar");
				}
			} catch (Throwable e1) {
				log.error("Error al invocar al TramiteBase en frmRegistrar de SolicitudPresentarModificacionFAPController: "+e1.getMessage());
				Messages.error("Error al intentar sólo registrar");
			}
		}

		if (!Messages.hasErrors()) {
			SolicitudPresentarModificacionFAPController.frmRegistrarValidateRules();
		}
		if (!Messages.hasErrors()) {
			dbSolicitud.save();
			Messages.ok("Solicitud Registrada correctamente");
			log.info("Acción Editar de página: " + "gen/SolicitudPresentarModificacionFAP/SolicitudPresentarModificacionFAP.html" + " , intentada con éxito");
			redirect("PresentarFAPController.index", "editar", idSolicitud, dbSolicitud.registro.id);
		} else
			log.info("Acción Editar de página: " + "gen/SolicitudPresentarModificacionFAP/SolicitudPresentarModificacionFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		SolicitudPresentarModificacionFAPController.frmRegistrarRender(idSolicitud, idRegistroModificacion, idRegistro);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formHabilitarFH(Long idSolicitud, Long idRegistroModificacion, Long idRegistro, String btnHabilitarFH) {
		checkAuthenticity();
		if (!permisoFormHabilitarFH("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		SolicitudGenerica dbSolicitud = SolicitudPresentarModificacionFAPController.getSolicitudGenerica(idSolicitud);
		idRegistroModificacion=dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size()-1).id;
		RegistroModificacion registroModificacion = RegistroModificacion.findById(idRegistroModificacion);
		idRegistro = registroModificacion.registro.id;
		Registro dbRegistro = SolicitudPresentarModificacionFAPController.getRegistro(idRegistroModificacion, idRegistro);
		
		if (!Messages.hasErrors()) {
			try {
				PresentacionModificacionFapController.invoke("comprobarFechaLimitePresentacion", idSolicitud);
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar los métodos comprobarFechaLimitePresentacion: "+e1.getMessage());
				Messages.error("Error al validar las comprobaciones de la Fecha Límite de Presentación");
			}
		}

		if (!Messages.hasErrors()) {
			try {
				TramiteBase tramite = PresentacionModificacionFapController.invoke("getTramiteObject", idSolicitud);
				boolean encontrado = false;
				for (Documento doc: tramite.getDocumentos()){
					if (doc.tipo.equals(FapProperties.get("fap.firmaYRegistro.funcionarioHabilitado.tipoDocumento"))){
						encontrado = true;
						break;
					}
				}
				if (!encontrado){
					log.error("El documento que autoriza la firma de un funcionario habilitado no ha sido subido o su tipo no es correcto. Uri del tipo correcto: "+FapProperties.get("fap.firmaYRegistro.funcionarioHabilitado.tipoDocumento"));
					Messages.error("El documento que autoriza la firma de un funcionario habilitado no ha sido subido o su tipo no es correcto.");
					Messages.error("Asegurese de haber subido el documento pertinente con tipo: "+TableKeyValue.getValue("tiposDocumentos", FapProperties.get("fap.firmaYRegistro.funcionarioHabilitado.tipoDocumento")));
				}
			} catch (Throwable e) {
				log.error("Hubo un problema al intentar verificar la presencia del documento de autorizacion funcionario habilitado: "+e.getMessage());
				Messages.error("No se pudo habilitar la firma de un Funcionario");
			}
		}
		if (!Messages.hasErrors()) {
			SolicitudPresentarModificacionFAPController.formHabilitarFHValidateRules();
		}
		if (!Messages.hasErrors()) {
			dbRegistro.habilitaFuncionario=true;
			dbRegistro.save();
			Messages.ok("Se ha habilitado a un funcionario correctamente.");
			log.info("Acción Editar de página: " + "gen/SolicitudPresentarModificacionFAP/SolicitudPresentarModificacionFAP.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/SolicitudPresentarModificacionFAP/SolicitudPresentarModificacionFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		SolicitudPresentarModificacionFAPController.formHabilitarFHRender(idSolicitud, idRegistroModificacion, idRegistro);
	}
	
}
