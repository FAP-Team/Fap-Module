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

		} else if (!"borrado".equals(accion)){
			solicitud = DatosVerificadosController.getSolicitudGenerica(idSolicitud);
//			System.out.println("Solicitante: " + solicitud.peticion.solicitudTransmision.get(solicitud.peticion.solicitudTransmision.size()-1).datosGenericos.solicitante.nombreSolicitante);
//			System.out.println("Codigo:" + solicitud.peticion.codigoCertificado);
//			System.out.println("Usuario: " +  solicitud.peticion.uidUsuario);
		}
		Integer ultimaSolicitud = solicitud.peticion.solicitudTransmision.size()-1;
		String codigo = solicitud.peticion.codigoCertificado;
		String usuario = solicitud.peticion.uidUsuario;
		String idSolicitante = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.solicitante.identificadorSolicitante;
		String nombreSolicitante = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.solicitante.nombreSolicitante;
		String finalidad = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.solicitante.finalidad;
		String unidadTramitadora = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.solicitante.unidadTramitadora;
		String idExpediente = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.solicitante.idExpediente;
		String codProcedimiento = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.solicitante.procedimiento.codigoProcedimiento;
		String nombreProcedimiento = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.solicitante.procedimiento.nombreProcedimiento;
		String nombreFuncionario = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.solicitante.funcionario.nombreCompletoFuncionario;
		String nifFuncionario = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.solicitante.funcionario.nifFuncionario;
		String consentimiento = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.solicitante.consentimiento.toString();
		String nifTitular = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.titular.documentacion;
		String tipoDocumentacion = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.titular.tipoDocumentacion.toString();
		String nombreCompleto = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.titular.getNombreCompleto();
		String nombre = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.titular.nombre;
		String apellido1 = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.titular.apellido1;
		String apellido2 = solicitud.peticion.solicitudTransmision.get(ultimaSolicitud).datosGenericos.titular.apellido2;
		
		Agente logAgente = AgenteController.getAgente();
		
		try{
			VerificarDatosService verificarDatosService = InjectorConfig.getInjector().getInstance(VerificarDatosService.class);
			Respuesta response = verificarDatosService.peticionSincronaIdentidad(codigo, usuario, idSolicitante, nombreSolicitante, 
					finalidad, idExpediente, unidadTramitadora, codProcedimiento, nombreProcedimiento, nombreFuncionario, nifFuncionario, 
					consentimiento, nifTitular, nombreCompleto, nombre, apellido1, apellido2, tipoDocumentacion);
			
			System.out.println("El codigo es es: " + response.getAtributos().getCodigoCertificado().toString());
			
			solicitud.respuestaSvd = VerificacionUtils.convertRespuestaSvdToRespuesta(response);

			}
			catch(VerificarDatosServiceException e){
				log.info("Visitando página: " + "fap/DatosVerificados/DatosVerificadosB.html" + " Agente: " + logAgente);
				renderTemplate("fap/DatosVerificados/DatosVerificadosB.html", accion, idSolicitud, solicitud);
				play.Logger.error("No se han podido resolver la petición. Causa: " + e.getMessage());
			}
		
		log.info("Visitando página: " + "fap/DatosVerificados/DatosVerificados.html" + " Agente: " + logAgente);
		renderTemplate("fap/DatosVerificados/DatosVerificados.html", accion, idSolicitud, solicitud);
	}

}
