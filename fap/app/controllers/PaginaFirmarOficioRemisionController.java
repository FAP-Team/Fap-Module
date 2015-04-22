package controllers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityTransaction;

import messages.Messages;
import messages.Messages.MessageType;
import models.Agente;
import models.Documento;
import models.ExpedienteAed;
import models.Firmante;
import models.Firmantes;
import models.LineaResolucionFAP;
import models.ResolucionFAP;
import models.SolicitudGenerica;
import platino.FirmaUtils;
import play.db.jpa.JPA;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Util;
import properties.FapProperties;
import resolucion.ResolucionBase;
import security.Accion;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.RegistroService;
import services.RegistroServiceException;
import services.platino.PlatinoGestorDocumentalService;
import utils.AedUtils;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.PaginaFirmarOficioRemisionControllerGen;
import enumerado.fap.gen.RolesEnum;

public class PaginaFirmarOficioRemisionController extends PaginaFirmarOficioRemisionControllerGen {

	public static void index(String accion, Long idResolucionFAP, Long idLineaResolucionFAP) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/PaginaFirmarOficioRemision/PaginaFirmarOficioRemision.html");
		}

		ResolucionFAP resolucionFAP = PaginaFirmarOficioRemisionController.getResolucionFAP(idResolucionFAP);

		LineaResolucionFAP lineaResolucionFAP = null;
		if ("crear".equals(accion)) {
			lineaResolucionFAP = PaginaFirmarOficioRemisionController.getLineaResolucionFAP();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				lineaResolucionFAP.save();
				idLineaResolucionFAP = lineaResolucionFAP.id;
				resolucionFAP.lineasResolucion.add(lineaResolucionFAP);
				resolucionFAP.save();

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			lineaResolucionFAP = PaginaFirmarOficioRemisionController.getLineaResolucionFAP(idResolucionFAP, idLineaResolucionFAP);

		log.info("Visitando página: " + "gen/PaginaFirmarOficioRemision/PaginaFirmarOficioRemision.html" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		renderTemplate("fap/PaginaFirmarOficioRemision/PaginaFirmarOficioRemision.html", accion, idResolucionFAP, idLineaResolucionFAP, resolucionFAP, lineaResolucionFAP);
	}
	
	@Util
	public static void firmaFirmarOficioRemisionFormFirmarOficioRemision(Long idResolucionFAP, Long idLineaResolucionFAP, String firma) {
		ResolucionFAP resolucionFAP = PaginaFirmarOficioRemisionController.getResolucionFAP(idResolucionFAP);
		LineaResolucionFAP lineaResolucionFAP = PaginaFirmarOficioRemisionController.getLineaResolucionFAP(idResolucionFAP, idLineaResolucionFAP);
		Messages.clear();
		
		PlatinoGestorDocumentalService platinoaed = InjectorConfig.getInjector().getInstance(PlatinoGestorDocumentalService.class);
		
		EntityTransaction tx = JPA.em().getTransaction();
		if (tx.isActive())
			tx.commit();
		
		play.Logger.info("Metodo: firmaFirmarOficioRemisionFormFirmarOficioRemision");
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		Map<String, Object> vars = new HashMap<String, Object>();
		if (secure.checkAcceso("editarFirma", "editar", ids, vars)) {
			tx.begin();
			
			play.Logger.info("Gestor que puede firmar el Oficio de Remision Generado");
			lineaResolucionFAP.registro.oficial.firmantes.todos = new ArrayList<Firmante>();
			Agente gestorSeleccionado = resolucionFAP.getAgenteSolicitadoFirmaOficiosRemision();
			Firmante firmante = new Firmante(gestorSeleccionado);
			//Se añade el firmante al documento Oficio de Remisión
			lineaResolucionFAP.registro.oficial.firmantes.todos.add(firmante);
			
			play.Logger.info("Gestor: "+firmante.nombre);
			
			//ResolucionFAP resolucionFAP = PaginaFirmarOficioRemisionController.getResolucionFAP(idResolucionFAP);
			if (!Messages.hasErrors()) {
				if (!lineaResolucionFAP.registro.fasesRegistro.firmada) {
					FirmaUtils.firmar(lineaResolucionFAP.registro.oficial, lineaResolucionFAP.registro.oficial.firmantes.todos, firma, resolucionFAP.idSolicitudFirmaOficiosRemision);
					if (!Messages.hasErrors()) {
						lineaResolucionFAP.registro.fasesRegistro.firmada = true;
						//Se sube el documento firmado a Platino
						platinoaed.obtenerURIPlatino(lineaResolucionFAP.registro.oficial.uri, PlatinoGestorDocumentalService.class);
					} else {
						Messages.error("Se produjeron errores en la firma, inténtelo de nuevo.");
						play.Logger.error("Se produjeron errores en la firma, inténtelo de nuevo.");
					}
				}
			} else {
				Messages.error("No se ha podido recuperar la resolución");
			}
			lineaResolucionFAP.save();
			tx.commit();
		} else {
			//ERROR
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		
		if (!Messages.hasErrors()) {
			SolicitudGenerica solicitud = SolicitudGenerica.findById(lineaResolucionFAP.solicitud.id);
			RegistroService registroService = InjectorConfig.getInjector().getInstance(RegistroService.class);
			GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
			
			try  {
				tx.begin();
				if (!lineaResolucionFAP.registro.fasesRegistro.registro){
					play.Logger.info("Se inicia el proceso de Registro");
					// Se obtiene el justificante de registro de salida del oficio de remisión
					models.JustificanteRegistro justificanteSalida = registroService.registroDeSalida(solicitud.solicitante, lineaResolucionFAP.registro.oficial, solicitud.expedientePlatino, "Oficio de remisión");				
					lineaResolucionFAP.registro.refresh();
					lineaResolucionFAP.registro.informacionRegistro.setDataFromJustificante(justificanteSalida);
					Documento documento = lineaResolucionFAP.registro.justificante;
					documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSalida");
					documento.descripcion = "Justificante de registro de salida del oficio de remisión";
					documento.save();
					play.Logger.info("Creado el documento de justificante en local, se procede a almacenar en el AED");
					InputStream is = justificanteSalida.getDocumento().contenido.getInputStream();
					gestorDocumentalService.saveDocumentoTemporal(documento, is, "JustificanteOficioRemision" + ".pdf");
					play.Logger.info("Justificante del documento oficio de remisión almacenado en el AED");
					lineaResolucionFAP.registro.fasesRegistro.registro = true;

					List<Documento> documentos = new ArrayList<Documento>();
					documentos.add(lineaResolucionFAP.registro.oficial);
					documentos.add(lineaResolucionFAP.registro.justificante);
					
					// Se pone la fecha de registro a los documentos
					for (Documento doc: documentos) {
						if (doc.fechaRegistro == null) {
							doc.fechaRegistro = lineaResolucionFAP.registro.informacionRegistro.fechaRegistro;
							doc.save();
						}
					}
					Messages.ok("Se realizó el registro correctamente");
				}
				if (!lineaResolucionFAP.registro.fasesRegistro.clasificarAed){
					List<Documento> documentos = new ArrayList<Documento>();
					documentos.add(lineaResolucionFAP.registro.oficial);
					documentos.add(lineaResolucionFAP.registro.justificante);
					play.Logger.info("Se procede a clasificar los documentos oficial y justificante de la línea: "+lineaResolucionFAP.id);
					// Se clasifican los documentos
					gestorDocumentalService.clasificarDocumentos(solicitud, documentos, false);
					lineaResolucionFAP.registro.fasesRegistro.clasificarAed = true;
					play.Logger.info("Documentos clasificados");
					lineaResolucionFAP.save();
					solicitud.save();
					Messages.ok("Se realizó la clasificación correctamente");
				}
				tx.commit();
			} catch (RegistroServiceException reg)   {
				Messages.error("Error realizando el proceso de registro");
				play.Logger.error("Error realizando el proceso de registro");
			} catch (GestorDocumentalServiceException e) {
				Messages.error("Error clasificando los documentos de oficio de remisión en el AED");
				play.Logger.error("Error clasificando los documentos de oficio de remisión en el AED");
			} catch (Throwable e){
				play.Logger.error("Error en el metodo firmaOficioRemision: " + e.getMessage());
			}	
		}
		
		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PaginaFirmarOficioRemision/PaginaFirmarOficioRemision.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		} else
			log.info("Acción Editar de página: " + "gen/PaginaFirmarOficioRemision/PaginaFirmarOficioRemision.html" + " , intentada sin éxito (Problemas de Validación)");

		Messages.keep();
		redirect("PaginaNotificarResolucionController.index", "editar", idResolucionFAP);
	}
	
}
