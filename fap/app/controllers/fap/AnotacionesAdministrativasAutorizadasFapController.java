package controllers.fap;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.joda.time.DateTime;

import config.InjectorConfig;

import enumerado.fap.gen.EstadosSolicitudEnum;

import messages.Messages;
import models.CertificadoSolicitado;
import models.Documento;
import models.SolicitudGenerica;
import models.TipoCertificado;
import reports.Report;
import services.GestorDocumentalService;
import utils.ModelUtils;

import play.Play;
import play.utils.Java;

public class AnotacionesAdministrativasAutorizadasFapController extends InvokeClassController{
	
	public static void obtenerCertificado (Long idSolicitud, Long idTipoCertificado){
		boolean puedePedirCertificado;
		try {
			puedePedirCertificado = AnotacionesAdministrativasAutorizadasFapController.invoke(AnotacionesAdministrativasAutorizadasFapController.class, "prepararParaPedirCertificado", idSolicitud, idTipoCertificado);
			if (puedePedirCertificado) {
				AnotacionesAdministrativasAutorizadasFapController.invoke(AnotacionesAdministrativasAutorizadasFapController.class, "generarCertificado", idSolicitud, idTipoCertificado);
			} else {
				play.Logger.error("No se ha validado correctamente para que el solicitante en la solicitud "+idSolicitud+" pueda pedir el certificado de tipo "+idTipoCertificado);
				if (!Messages.hasErrors()){
					TipoCertificado tipoCertificado=TipoCertificado.findById(idTipoCertificado);
					String nombreCertificado="'Certificado no encontrado'";
					if (tipoCertificado != null)
						nombreCertificado = tipoCertificado.nombre;
					Messages.error("No cumple todos los requisitos para la obtención del certificado "+nombreCertificado);
				}
			}
		} catch (Throwable e) {
			play.Logger.error("Hubo un error en los invokes de AAA para obtener el Certificado "+e.getMessage());
			Messages.error("Imposible generar el certificado. Hubo un problema interno.");
			Messages.keep();
		}
		
	}
	
	public static boolean prepararParaPedirCertificado (Long idSolicitud, Long idTipoCertificado){
		return true;
	}

	public static void generarCertificado (Long idSolicitud, Long idTipoCertificado){
		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
		TipoCertificado tipoCertificado=TipoCertificado.findById(idTipoCertificado);
		CertificadoSolicitado certificado = new CertificadoSolicitado(); 
    	File certificadoPDF;
		try {
			play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", solicitud);
			certificadoPDF = new Report("reports/"+tipoCertificado.nombrePlantilla+".html").registroSize().renderTmpFile(solicitud);
			certificado.documento.tipo=tipoCertificado.tipoDocumento;
			gestorDocumentalService.saveDocumentoTemporal(certificado.documento, certificadoPDF);
			certificado.fechaCreacion=new DateTime();
			certificado.tipo=tipoCertificado;
			// TODO: Firma con sello de la ACIISI
			// TODO: Añadir mas info al certificado
			certificado.save();
			solicitud.certificados.add(certificado);
			solicitud.save();
		} catch (Exception e) {
			play.Logger.error("Hubo un error generando el certificado PDF "+e.getMessage());
			Messages.error("Imposible generar el certificado");
			Messages.keep();
		}
	}
}
