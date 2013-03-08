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

}
