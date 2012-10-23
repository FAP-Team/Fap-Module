package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.xml.internal.fastinfoset.sax.Properties;

import platino.FirmaUtils;
import play.mvc.Util;
import properties.FapProperties;
import services.RegistroServiceException;
import tramitacion.TramiteBase;

import messages.Messages;
import models.Agente;
import models.Documento;
import models.Firmante;
import models.Registro;
import models.SolicitudGenerica;
import models.TableKeyValue;
import controllers.fap.AgenteController;
import controllers.fap.PresentacionFapController;
import controllers.gen.SolicitudPresentarFAPControllerGen;
import emails.Mails;

public class SolicitudPresentarFAPController extends SolicitudPresentarFAPControllerGen {
	
	public static void index(String accion, Long idSolicitud, Long idRegistro) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/Presentacion/SolicitudPresentarFAP.html");
		}

		SolicitudGenerica solicitud = SolicitudPresentarFAPController.getSolicitudGenerica(idSolicitud);

		Registro registro = null;
		if ("crear".equals(accion))
			registro = SolicitudPresentarFAPController.getRegistro();
		else if (!"borrado".equals(accion))
			registro = SolicitudPresentarFAPController.getRegistro(idSolicitud, idRegistro);

		log.info("Visitando página: " + "fap/Presentacion/SolicitudPresentarFAP.html");
		renderTemplate("fap/Presentacion/SolicitudPresentarFAP.html", accion, idSolicitud, idRegistro, solicitud, registro);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formFirmaFH(Long idSolicitud, Long idRegistro, String firma, String firmarRegistrarFH) {
		checkAuthenticity();
		if (!permisoFormFirmaFH("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		try {
			PresentacionFapController.invoke("beforeFirma", idSolicitud);
		} catch (Throwable e1) {
			log.error("Hubo un problema al invocar los métodos beforeFirma: "+e1.getMessage());
			Messages.error("Error al validar elementos previos a la firma");
		}
		if (!Messages.hasErrors()) {
			SolicitudGenerica dbSolicitud = SolicitudPresentarFAPController.getSolicitudGenerica(idSolicitud);
			try {
				TramiteBase tramite = PresentacionFapController.invoke("getTramiteObject", idSolicitud);
				SolicitudPresentarFAPController.firmarRegistrarFHFormFirmaFH(idSolicitud, idRegistro, firma);
				if (!Messages.hasErrors()) {
					try {
						tramite.registrar();
						if (dbSolicitud.registro.fasesRegistro.clasificarAed)
							tramite.cambiarEstadoSolicitud();
						else{
							play.Logger.error("No se registro la solicitud correctamente por lo que no se cambiara el estado de la misma.");
							Messages.error("Error al intentar sólo registrar.");
						}
						if (!Messages.hasErrors()) {
							try {
								PresentacionFapController.invoke("afterRegistro", idSolicitud);
							} catch (Throwable e1) {
								log.error("Hubo un problema al invocar los métodos afterRegistro: "+e1.getMessage());
								Messages.error("Error al validar elementos posteriores al registro");
							}
						}
					} catch (Exception e) {
						log.error("Hubo un error al registrar la solicitud: "+ e.getMessage());
						Messages.error("No se pudo registrar la solicitud");
					}
				}
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar el metodo que devuelve la clase TramiteBase en la firma: "+e1.getMessage());
				Messages.error("Error al intentar firmar antes de registrar");
			}
		}
		
		
//		if (firmarRegistrarFH != null) {
//			SolicitudPresentarFAPController.firmarRegistrarFHFormFirmaFH(idSolicitud, idRegistro, firma);
//			SolicitudPresentarFAPController.formFirmaFHRender(idSolicitud, idRegistro);
//		}

		if (!Messages.hasErrors()) {
			SolicitudPresentarFAPController.formFirmaFHValidateRules(firma);
		}
		if (!Messages.hasErrors()) {
			Messages.ok("Solicitud Firmada y Registrada correctamente");
			log.info("Acción Editar de página: " + "gen/SolicitudPresentarFAP/SolicitudPresentarFAP.html" + " , intentada con éxito");
			redirect("PresentarFAPController.index", "editar", idSolicitud, idRegistro);
		} else
			log.info("Acción Editar de página: " + "gen/SolicitudPresentarFAP/SolicitudPresentarFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		SolicitudPresentarFAPController.formFirmaFHRender(idSolicitud, idRegistro);
	}
	
	@Util
	public static void firmarRegistrarFHFormFirmaFH(Long idSolicitud, Long idRegistro, String firma) {
		SolicitudGenerica solicitud = SolicitudPresentarFAPController.getSolicitudGenerica(idSolicitud);

		play.Logger.info("Metodo: firmarRegistrarFHFormFirmaFH");
		Agente agente = AgenteController.getAgente();
		if (agente.getFuncionario()){
			List<Firmante> firmantes = new ArrayList<Firmante>();
			firmantes.add(new Firmante(agente));
			FirmaUtils.firmar(solicitud.registro.oficial, firmantes, firma, null);
		} else {
			//ERROR
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		if (!Messages.hasErrors()) {

			solicitud.registro.fasesRegistro.firmada = true;
			solicitud.save();
		}
	}
	
	@Util
	public static void formFirmaFHRender(Long idSolicitud, Long idRegistro) {
		Messages.keep();
		redirect("SolicitudPresentarFAPController.index", "editar", idSolicitud, idRegistro);
	}
	
	public static void tablatablaFirmantesHecho(Long idSolicitud) {

		java.util.List<Firmante> rows =  Firmante
				.find("select firmante from SolicitudGenerica solicitud join solicitud.registro.firmantes.todos firmante where solicitud.id=? and firmante.tipo=? and firmante.fechaFirma is not null",
						idSolicitud, "representante").fetch();


		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Firmante> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Firmante> response = new tables.TableRenderResponse<Firmante>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("idvalor", "nombre", "fechaFirma", "id"));
	}

	
	public static void tablatablaFirmantesEspera(Long idSolicitud) {

		java.util.List<Firmante> rows = Firmante
				.find("select firmante from SolicitudGenerica solicitud join solicitud.registro.firmantes.todos firmante where solicitud.id=? and firmante.tipo=? and firmante.fechaFirma is null", idSolicitud, "representante").fetch();

		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<Firmante> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<Firmante> response = new tables.TableRenderResponse<Firmante>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);

		renderJSON(response.toJSON("idvalor", "nombre", "id"));
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formFirmaPF(Long idSolicitud, Long idRegistro, String firma, String firmarRegistrarNif) {
		checkAuthenticity();
		if (!permisoFormFirmaPF("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		try {
			PresentacionFapController.invoke("beforeFirma", idSolicitud);
		} catch (Throwable e1) {
			log.error("Hubo un problema al invocar los métodos beforeFirma: "+e1.getMessage());
			Messages.error("Error al validar elementos previos a la firma");
		}
		SolicitudGenerica dbSolicitud = SolicitudPresentarFAPController.getSolicitudGenerica(idSolicitud);
		if (!Messages.hasErrors()) {
			try {
				TramiteBase tramite = PresentacionFapController.invoke("getTramiteObject", idSolicitud);
				tramite.firmar(firma);
				if (!Messages.hasErrors()) {
					try {
						tramite.registrar();
						if (dbSolicitud.registro.fasesRegistro.clasificarAed)
							tramite.cambiarEstadoSolicitud();
						else{
							play.Logger.error("No se registro la solicitud correctamente por lo que no se cambiara el estado de la misma.");
							Messages.error("Error al intentar sólo registrar.");
						}
						if (!Messages.hasErrors()) {
							try {
								PresentacionFapController.invoke("afterRegistro", idSolicitud);
							} catch (Throwable e1) {
								log.error("Hubo un problema al invocar los métodos afterRegistro: "+e1.getMessage());
								Messages.error("Error al validar elementos posteriores al registro");
							}
						}
					} catch (Exception e) {
						log.error("Hubo un error al registrar la solicitud: "+ e.getMessage());
						Messages.error("No se pudo registrar la solicitud");
					}
				}
			} catch (Throwable e1) {
				log.error("Hubo un problema al invocar el metodo que devuelve la clase TramiteBase en la firma: "+e1.getMessage());
				Messages.error("Error al intentar firmar antes de registrar");
			}
		}
		
//		if (!Messages.hasErrors()) {
//			if (firmarRegistrarNif != null) {
//				// OJO creo que sobraria
//				//SolicitudPresentarFAPController.firmarRegistrarNifFormFirmaPF(idSolicitud, idRegistro, firma);
//				SolicitudPresentarFAPController.formFirmaPFRender(idSolicitud, idRegistro);
//			}
//		}

		if (!Messages.hasErrors()) {
			SolicitudPresentarFAPController.formFirmaPFValidateRules(firma);
		}
		if (!Messages.hasErrors()) {
			Messages.ok("Solicitud registrada correctamente");
			log.info("Acción Editar de página: " + "fap/Presentacion/SolicitudPresentarFAP.html" + " , intentada con éxito");
			redirect("PresentarFAPController.index", "editar", idSolicitud, idRegistro);
		} else
			log.info("Acción Editar de página: " + "fap/Presentacion/SolicitudPresentarFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		SolicitudPresentarFAPController.formFirmaPFRender(idSolicitud, idRegistro);
	}
	
	@Util
	public static void formFirmaPFRender(Long idSolicitud, Long idRegistro) {
		Messages.keep();
		redirect("SolicitudPresentarFAPController.index", "editar", idSolicitud, idRegistro);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formFirmaRepresentante(Long idSolicitud, Long idRegistro, String firma, String firmarRepresentante) {
		checkAuthenticity();
		if (!permisoFormFirmaRepresentante("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		try {
			PresentacionFapController.invoke("beforeFirma", idSolicitud);
		} catch (Throwable e1) {
			log.error("Hubo un problema al invocar los métodos beforeFirma, en la firma con representantes: "+e1.getMessage());
			Messages.error("Error al validar elementos previos a la firma de representante");
		}
		
		if (!Messages.hasErrors()) {
			try {
				TramiteBase tramite = PresentacionFapController.invoke("getTramiteObject", idSolicitud);
				tramite.firmar(firma);
			} catch (Throwable e1) {
				log.error("Hubo un problema al firmar con representante en presentacion: "+e1.getMessage());
				Messages.error("Error al intentar firmar el representante");
			}
		}
		
//		if (firmarRepresentante != null) {
//			SolicitudPresentarFAPController.firmarRepresentanteFormFirmaRepresentante(idSolicitud, idRegistro, firma);
//			SolicitudPresentarFAPController.formFirmaRepresentanteRender(idSolicitud, idRegistro);
//		}

		if (!Messages.hasErrors()) {
			SolicitudPresentarFAPController.formFirmaRepresentanteValidateRules(firma);
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "fap/Presentacion/SolicitudPresentarFAP.html" + " , intentada con éxito");
			Messages.ok("Solicitud Firmada correctamente.");
		} else
			log.info("Acción Editar de página: " + "fap/Presentacion/SolicitudPresentarFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		SolicitudPresentarFAPController.formFirmaRepresentanteRender(idSolicitud, idRegistro);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void frmRegistrar(Long idSolicitud, Long idRegistro, String botonRegistrar) {
		checkAuthenticity();
		if (!permisoFrmRegistrar("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		SolicitudGenerica dbSolicitud = SolicitudPresentarFAPController.getSolicitudGenerica(idSolicitud);
		
		if (!Messages.hasErrors()) {
			
			try {
				TramiteBase tramite = PresentacionFapController.invoke("getTramiteObject", idSolicitud);
				try {
					dbSolicitud.registro.fasesRegistro.borrador = true;
					dbSolicitud.registro.fasesRegistro.firmada = true;
					tramite.registrar();
					if (dbSolicitud.registro.fasesRegistro.clasificarAed)
						tramite.cambiarEstadoSolicitud();
					else{
						play.Logger.error("No se registro la solicitud correctamente por lo que no se cambiara el estado de la misma.");
						Messages.error("Error al intentar sólo registrar.");
					}
					if (!Messages.hasErrors()) {
						try {
							PresentacionFapController.invoke("afterRegistro", idSolicitud);
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
				log.error("Error al invocar al TramiteBase en frmRegistrar de SolicitudPresentarFAPController: "+e1.getMessage());
				Messages.error("Error al intentar sólo registrar");
			}
		}

		if (!Messages.hasErrors()) {
			dbSolicitud.save();
			SolicitudPresentarFAPController.frmRegistrarValidateRules();
		}
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "fap/Presentacion/SolicitudPresentarFAP.html" + " , intentada con éxito");
			Messages.ok("Solicitud Registrada correctamente");
			redirect("PresentarFAPController.index", "editar", idSolicitud, idRegistro);
		} else
			log.info("Acción Editar de página: " + "fap/Presentacion/SolicitudPresentarFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		SolicitudPresentarFAPController.frmRegistrarRender(idSolicitud, idRegistro);
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formHabilitarFH(Long idSolicitud, Long idRegistro, String btnHabilitarFH) {
		checkAuthenticity();
		if (!permisoFormHabilitarFH("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			try {
				TramiteBase tramite = PresentacionFapController.invoke("getTramiteObject", idSolicitud);
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
			SolicitudPresentarFAPController.formHabilitarFHValidateRules();
		}
		if (!Messages.hasErrors()) {
			Registro registro = SolicitudPresentarFAPController.getRegistro(idSolicitud, idRegistro);
			registro.habilitaFuncionario=true;
			registro.save();
			Messages.ok("Se ha habilitado a un funcionario correctamente.");
			log.info("Acción Editar de página: " + "fap/Presentacion/SolicitudPresentarFAP.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "fap/Presentacion/olicitudPresentarFAP.html" + " , intentada sin éxito (Problemas de Validación)");
		SolicitudPresentarFAPController.formHabilitarFHRender(idSolicitud, idRegistro);
	}
}
