package controllers;

import static play.modules.pdf.PDF.renderPDF;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.codec.BinaryDecoder;

import play.mvc.Util;
import services.VerificarDatosService;
import services.VerificarDatosServiceException;
import messages.Messages;
import models.Agente;
import models.Respuesta;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.PeticionPDFRecuperadaControllerGen;
import es.gobcan.platino.servicios.svd.RespuestaPdf;

public class PeticionPDFRecuperadaController extends PeticionPDFRecuperadaControllerGen {
	
	public static void index(String accion, Long idRespuesta) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/PeticionPDFRecuperada/PeticionPDFRecuperada.html");
		}

		Respuesta respuesta = null;
		if ("crear".equals(accion)) {
			respuesta = PeticionPDFRecuperadaController.getRespuesta();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				respuesta.save();
				idRespuesta = respuesta.id;

				accion = "editar";
			}

		} else if (!"borrado".equals(accion)){
			respuesta = PeticionPDFRecuperadaController.getRespuesta(idRespuesta);
			String codigoPeticion = respuesta.atributos.idPeticion;
			String idTransmision = respuesta.transmisiones.transmisionDatos.get(0).datosGenericos.transmision.idTransmision;
			String uidUsuario = respuesta.transmisiones.transmisionDatos.get(0).datosGenericos.emisor.nombreEmisor;
			respuesta.delete();
			recuperacion(codigoPeticion, idTransmision, uidUsuario);		
		}
		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "gen/PeticionPDFRecuperada/PeticionPDFRecuperada.html" + " Agente: " + logAgente);
		renderTemplate("gen/PeticionPDFRecuperada/PeticionPDFRecuperada.html", accion, idRespuesta, respuesta);
	}
	
	
	public static boolean recuperacion(String codigoPeticion, String idTransmision, String uidUsuario) {

		try{
			VerificarDatosService verificarDatosService = InjectorConfig.getInjector().getInstance(VerificarDatosService.class);
			RespuestaPdf respuesta = verificarDatosService.peticionPDF(codigoPeticion, idTransmision, uidUsuario);	

			DataHandler pdf = respuesta.getPdf();
			DataSource datos = pdf.getDataSource();
			try{
				InputStream is = datos.getInputStream();
				renderBinary(is);
				return true;
			}
			catch(Exception e){
				System.out.println("ERROR al copiar en el fichero. Excepción: " + e.getMessage());
				return false;
			}
			
		}
		catch(VerificarDatosServiceException e){
			play.Logger.error("No se ha podido resolver la petición. Causa: " + e.getMessage());
			VerificacionDatosSVDController.index("editar");;
			return false;
		}
	}
	
}
