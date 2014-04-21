package controllers.popups;

import java.util.ArrayList;
import java.util.List;

import messages.Messages;
import models.Agente;
import models.Metadato;
import models.SolicitudGenerica;
import models.TipoDocumento;
import controllers.fap.AgenteController;
import controllers.gen.popups.RellenarMetadatosControllerGen;

public class RellenarMetadatosController extends RellenarMetadatosControllerGen {

	public static void index(String accion, Long idTipoDocumento, String urlRedirigir) {
		if (accion == null)
			accion = getAccion();
		if (!permiso(accion)) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("gen/popups/RellenarMetadatos.html");
		}

		TipoDocumento tipoDocumento = null;
		if ("crear".equals(accion)) {
			tipoDocumento = RellenarMetadatosController.getTipoDocumento();
			if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")) {

				tipoDocumento.save();
				idTipoDocumento = tipoDocumento.id;

				accion = "editar";
			}
			String variablesRedirigir = "";
			urlRedirigir += variablesRedirigir;

		} else if (!"borrado".equals(accion))
			tipoDocumento = RellenarMetadatosController.getTipoDocumento(idTipoDocumento);

		Agente logAgente = AgenteController.getAgente();
		log.info("Visitando página: " + "gen/popups/RellenarMetadatos.html" + " Agente: " + logAgente);
		List<Metadato> metadatos = (List<Metadato>) play.cache.Cache.get("metadatos");
		renderTemplate("fap/Documentacion/PopupRellenarMetadatos.html", accion, idTipoDocumento, tipoDocumento, urlRedirigir, metadatos);
	}
	
	public static void editar(String accion, Long idSolicitud, String uriTipoDocumento, String urlRedirigir) {
		if (accion == null)
			accion = getAccion();
		if ((accion == null) || (!permiso(accion))) {
			Messages.fatal("No tiene suficientes privilegios para acceder a esta solicitud");
			renderTemplate("gen/popups/RellenarMetadatos.html");
		}
		
		log.info(String.format("%s.editar(%s,%s,%s)", RellenarMetadatosController.class , accion, uriTipoDocumento, urlRedirigir));
		
		TipoDocumento tipo = TipoDocumento.find("byUri", uriTipoDocumento).first();
		Long idTipoDocumento = tipo.id;
		Messages.ok("Pasando por el manual");
		index(accion, idTipoDocumento, urlRedirigir);
	}
}