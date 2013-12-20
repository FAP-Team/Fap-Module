package controllers;

import services.VerificarDatosService;
import services.VerificarDatosServiceException;
import messages.Messages;
import models.Agente;
import models.SolicitudGenerica;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.DatosVerificadosControllerGen;
import es.gobcan.platino.servicios.svd.Respuesta;
import verificacion.VerificacionUtils;

public class DatosVerificadosController extends DatosVerificadosControllerGen {
	
	public static void index(String accion, Long idSolicitud) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("fap/DatosVerificados/DatosVerificados.html");
		}

		SolicitudGenerica solicitud = null;
		if ("crear".equals(accion)) {
			solicitud = DatosVerificadosController.getSolicitudGenerica();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				solicitud.save();
				idSolicitud = solicitud.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion))
			solicitud = DatosVerificadosController.getSolicitudGenerica(idSolicitud);

		Agente logAgente = AgenteController.getAgente();
		
		try{
			VerificarDatosService verificarDatosService = InjectorConfig.getInjector().getInstance(VerificarDatosService.class);
			Respuesta response = verificarDatosService.peticionSincronaIdentidad("CDISFWS01", "desarrollo", "S2833002E", "MINISTERIO DE HACIENDA Y AP", 
					"PRUEBAS DE INTEGRACION SCSP", "", "SG COORD ESTUDIOS E IMPULSO ADMELECLT(MINHAP)", "SVDR_20101117_000254", 
					"PRUEBAS PARA LA INTEGRACION Y SOLUCION DE INCIDENCIAS", "Luz Diaz Soto", "00000003A", "Si", "10000322Z", "", "", "", "", "NIF");
			System.out.println("El codigo es es: " + response.getAtributos().getCodigoCertificado().toString());
			
			solicitud.respuestaSvd = VerificacionUtils.convertRespuestaSvdToRespuesta(response);
			
			}
			catch(VerificarDatosServiceException e){
				play.Logger.error("No se han podido resolver la petición. Causa: " + e.getMessage());
			}
		
		log.info("Visitando página: " + "fap/DatosVerificados/DatosVerificados.html" + " Agente: " + logAgente);
		renderTemplate("fap/DatosVerificados/DatosVerificados.html", accion, idSolicitud, solicitud);
	}

}
