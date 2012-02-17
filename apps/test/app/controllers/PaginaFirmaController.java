
package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import messages.Messages;
import models.Documento;
import models.Firmante;
import models.FuncionariosHabilitados;
import models.Solicitud;
import models.SolicitudGenerica;
import platino.FirmaUtils;
import play.modules.guice.InjectSupport;
import play.mvc.Util;
import properties.FapProperties;
import reports.Report;
import services.FirmaService;
import services.GestorDocumentalService;

import com.google.inject.Inject;

import config.InjectorConfig;
import controllers.gen.PaginaFirmaControllerGen;
import enumerado.fap.gen.FaseRegistroEnum;

public class PaginaFirmaController extends PaginaFirmaControllerGen {
	
	@Inject
	protected static GestorDocumentalService gestorDocumentalService;
	
	
	@Util
	public static void prepararParaFirmarFormPreparar(Long idSolicitud){
		//Sobreescribir este método para asignar una acción
		Solicitud dbSolicitud = PaginaFirmaController.getSolicitud(idSolicitud);

		play.Logger.info("Preparamos para firmar");
		FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);
		
		FirmaUtils.generarOficial (dbSolicitud);
		
		if (!Messages.hasErrors()) {
			dbSolicitud.registro.fasesRegistro.borrador = true;
			dbSolicitud.solicitante.autorizaFuncionario = false;
			dbSolicitud.registro.fasesRegistro.setFase(FaseRegistroEnum.preparada);
			dbSolicitud.save();
		
			//Borra firmantes anteriores
			List<Firmante> firmantesBack = new ArrayList<Firmante>(dbSolicitud.registro.firmantes);
			dbSolicitud.registro.firmantes.clear();
			dbSolicitud.registro.save();
			FirmaUtils.borrarFirmantes(firmantesBack);
		
			//Calcula quién puede firmar la solicitud
			dbSolicitud.registro.firmantes = dbSolicitud.solicitante.calcularFirmantes();
			
			//dbSolicitud.solicitante.autorizaFuncionario = true;
			dbSolicitud.registro.save();
		
			play.Logger.info("FIRMANTES");
			for(Firmante f : dbSolicitud.registro.firmantes){
				play.Logger.info(f.toString());
			}
		}
		
		if (!Messages.hasErrors())
			Messages.ok("Solicitud preparada para firmar");
	}
	
	@Util
	public static void prepararParaFirmarFuncionarioHabilitadoFormPreparar(Long idSolicitud){
		play.Logger.info("Preparamos para firmar por el funcionario Habilitado (no hace nada aún)");
		prepararParaFirmarFormPreparar(idSolicitud);
		Solicitud dbSolicitud = PaginaFirmaController.getSolicitud(idSolicitud);
		
		if (!Messages.hasErrors()) {
			//Borra firmantes anteriores
			List<Firmante> firmantesBack = new ArrayList<Firmante>(dbSolicitud.registro.firmantes);
			dbSolicitud.registro.firmantes.clear();
			dbSolicitud.registro.save();
			FirmaUtils.borrarFirmantes(firmantesBack);
		
			//Calcula quién puede firmar la solicitud (los funcionarios habilitados)
			dbSolicitud.registro.firmantes = FuncionariosHabilitados.getFirmantes();
			
			dbSolicitud.registro.save();
		
			play.Logger.info("FIRMANTES");
			for(Firmante f : dbSolicitud.registro.firmantes){
				play.Logger.info(f.toString());
			}
			dbSolicitud.solicitante.autorizaFuncionario = true;
			dbSolicitud.registro.fasesRegistro.setFase(FaseRegistroEnum.preparadaFuncionario);
			dbSolicitud.save();
			// Debemos comprobar que el documento con el tipo de URI haya sido añadido a la documentación
			Messages.ok("Preparado para firmar por el funcionario correctamente");
		}
	}

	
	@Util
	public static void formModificarEstadoValidateRules(Solicitud dbSolicitud){
		dbSolicitud.registro.fasesRegistro.reiniciar();
		dbSolicitud.save();
		Messages.ok("Fase de registro de la Solicitud reiniciada");
	}
	
	
	@Util
	public static void botonSimpleForm(Long idSolicitud, platino.Firma firma){
		// Realizamos la creación del documento y lo subimos al AED
		Solicitud solicitud = getSolicitud(idSolicitud);
		
		if ((solicitud.registro.oficial == null) || (solicitud.registro.oficial.uri == null)) {
			try {//Genera el documento oficial
				play.Logger.info("Debemos crear el documento y subirlo al AED");
				File oficial =  new Report("reports/solicitud_simple.html").header("reports/header.html").registroSize().renderTmpFile(solicitud);
				solicitud.registro.oficial = new Documento();
				solicitud.registro.oficial.tipo = FapProperties.get("fap.aed.tiposdocumentos.solicitud");
				solicitud.registro.oficial.descripcion = "Descripción del documento incluida a mano";
				gestorDocumentalService.saveDocumentoTemporal(solicitud.registro.oficial, oficial);
				play.Logger.info("Documento creado y subido al AED: ");
				play.Logger.info("uri -> "+solicitud.registro.oficial.uri);
				play.Logger.info("url -> "+solicitud.registro.oficial.urlDescarga);
				solicitud.save();
			} catch (Exception e) {
				play.Logger.error("Error");
				e.printStackTrace();
				Messages.error("Error: "+e.getMessage());
			}
		} else {
			play.Logger.info("Documento YA creado y subido al AED: ");
			play.Logger.info("uri -> "+solicitud.registro.oficial.uri);
			play.Logger.info("url -> "+solicitud.registro.oficial.urlDescarga);
		}
	}
}
		