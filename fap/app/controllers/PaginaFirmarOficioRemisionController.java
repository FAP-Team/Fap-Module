package controllers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Util;
import properties.FapProperties;
import resolucion.ResolucionBase;
import security.Accion;
import services.GestorDocumentalService;
import services.RegistroService;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;
import controllers.gen.PaginaFirmarOficioRemisionControllerGen;

public class PaginaFirmarOficioRemisionController extends PaginaFirmarOficioRemisionControllerGen {

	@Util
	public static void firmaFirmarOficioRemisionFormFirmarOficioRemision(Long idResolucionFAP, Long idLineaResolucionFAP, String firma) {
		LineaResolucionFAP lineaResolucionFAP = PaginaFirmarOficioRemisionController.getLineaResolucionFAP(idResolucionFAP, idLineaResolucionFAP);

		play.Logger.info("Metodo: firmaFirmarOficioRemisionFormFirmarOficioRemision");
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		Map<String, Object> vars = new HashMap<String, Object>();
		if (secure.checkAcceso("editarFirma", "editar", ids, vars)) {
			if (lineaResolucionFAP.registro.firmantes.todos == null || lineaResolucionFAP.registro.firmantes.todos.size() == 0) {
				List<Agente> agentes = Agente.find("select agente from Agente agente join agente.roles rol where rol = 'gestor'").fetch();
				for (int i = 0; i < agentes.size(); i++) {
					Firmante firmante = new Firmante(agentes.get(i));
					lineaResolucionFAP.registro.firmantes.todos.add(firmante);
				}
				lineaResolucionFAP.registro.firmantes.save();
			}
			FirmaUtils.firmar(lineaResolucionFAP.registro.oficial, lineaResolucionFAP.registro.firmantes.todos, firma, null);
		} else {
			//ERROR
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		if (!Messages.hasErrors()) {

			lineaResolucionFAP.registro.fasesRegistro.firmada = true;
			lineaResolucionFAP.save();

			SolicitudGenerica solicitud = SolicitudGenerica.findById(lineaResolucionFAP.solicitud.id);
			RegistroService registroService = InjectorConfig.getInjector().getInstance(RegistroService.class);
			GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
			
			try  {

				// Se obtiene el justificante de registro de salida del oficio de remisión
				models.JustificanteRegistro justificanteSalida = registroService.registroDeSalida(solicitud.solicitante, lineaResolucionFAP.registro.oficial, solicitud.expedientePlatino, "Oficio de remisión");				
				lineaResolucionFAP.registro.informacionRegistro.setDataFromJustificante(justificanteSalida);
				Documento documento = lineaResolucionFAP.registro.justificante;
				documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSalida");
				documento.descripcion = "Justificante de registro de salida del oficio de remisión";
				documento.save();
				InputStream is = justificanteSalida.getDocumento().contenido.getInputStream();
				gestorDocumentalService.saveDocumentoTemporal(documento, is, "JustificanteOficioRemision" + ".pdf");
				play.Logger.info("Justificante del documento oficio de remisión almacenado en el AED");
				lineaResolucionFAP.registro.fasesRegistro.registro = true;
				
				// Se pone la fecha de registro a los documentos
				List<Documento> documentos = new ArrayList<Documento>();
				documentos.add(lineaResolucionFAP.registro.oficial);
				documentos.add(lineaResolucionFAP.registro.justificante);
				for (Documento doc: documentos) {
					if (doc.fechaRegistro == null) {
						doc.fechaRegistro = lineaResolucionFAP.registro.informacionRegistro.fechaRegistro;
						doc.save();
					}
				}
				
				// Se clasifican los documentos
				gestorDocumentalService.clasificarDocumentos(solicitud, documentos, true);
				lineaResolucionFAP.registro.fasesRegistro.clasificarAed = true;

				lineaResolucionFAP.save();
				solicitud.save();

			} catch (Throwable e)   {
				
			}
		}
		
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/PaginaFirmarOficioRemision/PaginaFirmarOficioRemision.html" + " , intentada con éxito" + ", usuario: " + AgenteController.getAgente().name + " Solicitud: " + params.get("idSolicitud"));
		} else
			log.info("Acción Editar de página: " + "gen/PaginaFirmarOficioRemision/PaginaFirmarOficioRemision.html" + " , intentada sin éxito (Problemas de Validación)");

		redirect("PaginaNotificarResolucionController.index", "editar", idResolucionFAP);
	}
	
}
