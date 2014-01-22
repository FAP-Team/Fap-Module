package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

import javassist.bytecode.ByteArray;

import javax.activation.CommandInfo;
import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.aegis.type.mtom.DataHandlerType;

import com.itextpdf.text.pdf.codec.Base64.OutputStream;

import messages.Messages;
import models.Agente;
import models.Respuesta;
import models.SolicitudGenerica;
import models.TransmisionDatosRespuesta;
import play.libs.IO;
import play.modules.pdf.RenderPDFTemplate;
import play.mvc.Util;
import play.mvc.results.RenderBinary;
import security.Accion;
import services.VerificarDatosService;
import services.VerificarDatosServiceException;
import verificacion.VerificacionUtils;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.gen.VerificacionDatosSVDControllerGen;
import es.gobcan.platino.servicios.svd.RespuestaPdf;
import static play.modules.pdf.PDF.*;

public class VerificacionDatosSVDController extends VerificacionDatosSVDControllerGen {

	public static void index(String accion) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
			renderTemplate("gen/VerificacionDatosSVD/VerificacionDatosSVD.html");
		}

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "gen/VerificacionDatosSVD/VerificacionDatosSVD.html" + " Agente: " + logAgente);
		renderTemplate("fap/VerificacionDatosSVD/VerificacionDatosSVD.html", accion);
	}

	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void recuperaPeticionCodigo(String botonRecuperarPeticion) {
		checkAuthenticity();
		if (!permisoRecuperaPeticionCodigo("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			String accion = getAccion();
			redirect("PeticionRecuperadaController.index", accion);
		}

		if (!Messages.hasErrors()) {
			VerificacionDatosSVDController.recuperaPeticionCodigoValidateRules();
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/VerificacionDatosSVD/VerificacionDatosSVD.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/VerificacionDatosSVD/VerificacionDatosSVD.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		VerificacionDatosSVDController.recuperaPeticionCodigoRender();
	}

	@Util
	public static void recuperaPeticionCodigoRender() {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("VerificacionDatosSVDController.index", "editar");
		}
		Messages.keep();
		redirect("VerificacionDatosSVDController.index", "editar");
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void recuperaPDF(String botonRecuperarPeticionPdf) {
		checkAuthenticity();
		if (!permisoRecuperaPDF("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			String accion = getAccion();
			redirect("PeticionPDFRecuperadaController.index", accion);
		}

		if (!Messages.hasErrors()) {
			VerificacionDatosSVDController.recuperaPDFValidateRules();
		}
		Agente logAgente = AgenteController.getAgente();
		if (!Messages.hasErrors()) {

			log.info("Acción Editar de página: " + "gen/VerificacionDatosSVD/VerificacionDatosSVD.html" + " , intentada con éxito " + " Agente: " + logAgente);
		} else
			log.info("Acción Editar de página: " + "gen/VerificacionDatosSVD/VerificacionDatosSVD.html" + " , intentada sin éxito (Problemas de Validación)" + " Agente: " + logAgente);
		VerificacionDatosSVDController.recuperaPDFRender();
	}

	@Util
	public static void recuperaPDFRender() {
		if (!Messages.hasMessages()) {
			Messages.ok("Página editada correctamente");
			Messages.keep();
			redirect("VerificacionDatosSVDController.index", "editar");
		}
		Messages.keep();
		redirect("VerificacionDatosSVDController.index", "editar");
	}
	
	public static String recuperaPeticion(String codigoPeticion, String uidUsuario) {

		try{
			VerificarDatosService verificarDatosService = InjectorConfig.getInjector().getInstance(VerificarDatosService.class);
			es.gobcan.platino.servicios.svd.Respuesta response = verificarDatosService.peticionRecover(uidUsuario, codigoPeticion);
		
			Respuesta respuesta = VerificacionUtils.convertRespuestaSvdToRespuesta(response);
			respuesta.save();

			String accion = getAccion();
			String identificador = "?idRespuesta=" + respuesta.id.toString() + "&accion=" + accion;
			return identificador;
			}
		catch(VerificarDatosServiceException e){
			play.Logger.error("No se han podido resolver la petición. Causa: " + e.getMessage());
			VerificacionDatosSVDController.recuperaPeticionCodigoRender();
			return "Error";
		}
	}
	
	public static String recuperaPeticionPDF(String codigoPeticion, String idTransmision, String uidUsuario) {

		try{
			VerificarDatosService verificarDatosService = InjectorConfig.getInjector().getInstance(VerificarDatosService.class);
			RespuestaPdf respuesta = verificarDatosService.peticionPDF(codigoPeticion, idTransmision, uidUsuario);
			
			Respuesta respuestab = new Respuesta();
			TransmisionDatosRespuesta transmision = new  TransmisionDatosRespuesta();
			transmision.datosGenericos.transmision.idTransmision = idTransmision;
			transmision.datosGenericos.emisor.nombreEmisor = uidUsuario;
			respuestab.atributos.idPeticion = codigoPeticion;
			respuestab.transmisiones.transmisionDatos.add(transmision);
			respuestab.save();
			String accion = getAccion();
			String identificador = "?idRespuesta=" + respuestab.id.toString() + "&accion=" + accion;
			return identificador;	
			}
			catch(Exception e){
				play.Logger.error("No se ha podido resolver la petición. Causa: " + e.getMessage());
				VerificacionDatosSVDController.index("editar");
				return "error";
			}
	}
	
	
}
