package controllers;

import java.util.ArrayList;
import java.util.List;

import messages.Messages;
import models.CertificadoSolicitado;
import models.TableKeyValue;
import models.TipoCertificado;
import play.mvc.Util;
import properties.FapProperties;
import tags.ComboItem;
import controllers.fap.AnotacionesAdministrativasAutorizadasFapController;
import controllers.gen.PedirCertificadoControllerGen;

public class PedirCertificadoController extends PedirCertificadoControllerGen {

	public static List<ComboItem> tipoCertificado() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		List<TipoCertificado> tiposCertificados = TipoCertificado.findAll();
		for (TipoCertificado tipoCertificado: tiposCertificados){
			result.add(new ComboItem(tipoCertificado.tipoDocumento, tipoCertificado.nombre));
		}
		return result;
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void pedirCertificado(Long idSolicitud, Long idCertificadoSolicitado, CertificadoSolicitado certificadoSolicitado, String obtenerCertificado) {
		checkAuthenticity();
		if (!permisoPedirCertificado("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			TipoCertificado tipoCertificado = null;
			if ((certificadoSolicitado.tipo.tipoDocumento != null) && (!certificadoSolicitado.tipo.tipoDocumento.isEmpty()))
				tipoCertificado = TipoCertificado.find("select tc from TipoCertificado tc where tc.tipoDocumento=?", certificadoSolicitado.tipo.tipoDocumento).first();
			if (tipoCertificado != null){
				try {
					AnotacionesAdministrativasAutorizadasFapController.invoke(AnotacionesAdministrativasAutorizadasFapController.class, "obtenerCertificado", idSolicitud, tipoCertificado.id);
				} catch (Throwable e) {
					play.Logger.error("No se ha podido obtener el Certificado de la solicitud "+idSolicitud+" - "+e.getMessage());
					Messages.error("Imposible obtener el certificado");
				}
			} else {
				play.Logger.error("No se ha podido obtener el Certificado de la solicitud "+idSolicitud+" del tipo: "+certificadoSolicitado.tipo.tipoDocumento);
				Messages.error("Imposible obtener el certificado de ese tipo");
			}
		}

		if (!Messages.hasErrors()) {
			log.info("Acción Editar de página: " + "gen/PedirCertificado/PedirCertificado.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/PedirCertificado/PedirCertificado.html" + " , intentada sin éxito (Problemas de Validación)");
		PedirCertificadoController.pedirCertificadoRender(idSolicitud, idCertificadoSolicitado);
	}
	
	@Util
	public static void pedirCertificadoRender(Long idSolicitud, Long idCertificadoSolicitado) {
		if (!Messages.hasMessages()) {
			Messages.ok("Certificado generado correctamente");
			Messages.keep();
			redirect("CertificadosSolicitadosController.index", "leer", idSolicitud);
		}
		Messages.keep();
		redirect("CertificadosSolicitadosController.index", "leer", idSolicitud);
	}
			

}
