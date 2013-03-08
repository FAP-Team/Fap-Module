package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import messages.Messages;
import models.FacturasFAP;
import models.Solicitud;
import play.mvc.Util;
import services.GestorDocumentalServiceException;
import utils.BinaryResponse;
import controllers.gen.SubirFacturasControllerGen;
import es.mityc.facturae.FacturaeVersion;
import es.mityc.facturae.utils.UnmarshalException;
import es.mityc.facturae.utils.UnmarshallerUtil;
import es.mityc.facturae32.Facturae;

public class SubirFacturasController extends SubirFacturasControllerGen {
	@Util
	public static Long crearLogica(Long idSolicitud, FacturasFAP facturasFAP, java.io.File fileFactura1_documento) {
		checkAuthenticity();
		if (!permiso("crear")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		FacturasFAP dbFacturasFAP = SubirFacturasController.getFacturasFAP();
		Solicitud dbSolicitud = SubirFacturasController.getSolicitud(idSolicitud);

		SubirFacturasController.SubirFacturasBindReferences(facturasFAP, fileFactura1_documento);

		if (!Messages.hasErrors()) {
			SubirFacturasController.SubirFacturasValidateCopy("crear", dbFacturasFAP, facturasFAP, fileFactura1_documento);
		}
		
		if (!Messages.hasErrors()) {
			Facturae invoice32 = dbFacturasFAP.getFacturaeObject();
			FacturasFAP.getDataFromFacturae(invoice32, dbFacturasFAP);
		}

		if (!Messages.hasErrors()) {
			SubirFacturasController.crearValidateRules(dbFacturasFAP, facturasFAP, fileFactura1_documento);
		}
		
		Long idFacturasFAP = null;
		if (!Messages.hasErrors()) {

			dbFacturasFAP.save();
			idFacturasFAP = dbFacturasFAP.id;
			dbSolicitud.facturas.add(dbFacturasFAP);
			dbSolicitud.save();

			log.info("Acción Crear de página: " + "gen/SubirFacturas/SubirFacturas.html" + " , intentada con éxito");
		} else {
			log.info("Acción Crear de página: " + "gen/SubirFacturas/SubirFacturas.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		return idFacturasFAP;
	}
}
