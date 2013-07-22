package resolucion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityTransaction;

import play.db.jpa.JPA;
import config.InjectorConfig;
import controllers.fap.ResolucionControllerFAP;
import enumerado.fap.gen.EstadoResolucionEnum;

import reports.Report;
import services.FirmaService;
import services.GestorDocumentalService;
import models.Documento;
import models.ExpedienteAed;
import models.LineaResolucionFAP;
import models.ResolucionFAP;
import models.SolicitudGenerica;

public class ResolucionSimpleEjecucion extends ResolucionSimple {

	public ResolucionSimpleEjecucion(ResolucionFAP resolucion) {
		super(resolucion);
	}
	
	//TODO: Modificar para utilizar documentoOficioRemison de lineaResolucionFAP
	//      Y tener en cuenta la solicitud para generar el documento
	public File generarDocumentoOficioRemision (LineaResolucionFAP linea) {
		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", linea.solicitud);
		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("resolucion", this.resolucion);
		File report = null;
		try {
			report = new Report(getBodyReportOficioRemision())
								.header(getHeaderReport())
								.footer(getFooterReport())
								.renderTmpFile(linea.solicitud, resolucion);
			
			linea.documentoOficioRemision = new Documento();
			linea.documentoOficioRemision.tipo = getTipoDocumentoOficioRemision();
			linea.documentoOficioRemision.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return report;
	}
	
	 @Override
	 public void notificarCopiarEnExpedientes (long idResolucion){	
		ResolucionBase resolucion = null;
		try {
			resolucion = ResolucionControllerFAP.invoke(ResolucionControllerFAP.class, "getResolucionObject", idResolucion);
		}catch (Throwable e) {
			// TODO: handle exception
		}
		
		List<ExpedienteAed> listaExpedientes = new ArrayList<ExpedienteAed>();
		play.Logger.info("Resolución: "+resolucion.resolucion.id+" tiene "+resolucion.resolucion.lineasResolucion.size()+" líneas de resolución");
		GestorDocumentalService gestorDocumental = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);
	
		for (LineaResolucionFAP linea: resolucion.resolucion.lineasResolucion) {
			try  {
				//Genera el documento oficio de remision
				SolicitudGenerica sol = SolicitudGenerica.findById(linea.solicitud.id);
				File documentoOficioRemision = generarDocumentoOficioRemision(linea);
				String uri = gestorDocumental.saveDocumentoTemporal(linea.documentoOficioRemision, documentoOficioRemision);
				//Firmarlo
				firmaService.firmarEnServidor(linea.documentoOficioRemision);
				listaExpedientes.add(linea.solicitud.expedienteAed);
				//Y copiarlo al expediente
				gestorDocumental.copiarDocumentoEnExpediente(uri, listaExpedientes);
				listaExpedientes.clear();
				sol.save();
			} catch (Throwable e)   {
				
			}
		}
		
		//Una vez copiados los expedientes se comprueba si hay documentos de baremacion
		//y se avanza de fase segun el tipo de la resolucion
		if (!resolucion.resolucion.conBaremacion) {
				EntityTransaction tx = JPA.em().getTransaction();
				tx.commit();
				tx.begin();
				if (EstadoResolucionEnum.publicada.name().equals(resolucion.resolucion.estado))
					resolucion.avanzarFase_PublicadaYONotificada(resolucion.resolucion);
				else
					resolucion.avanzarFase_Registrada_Notificada(resolucion.resolucion);
				tx.commit();
				tx.begin();
		}
	}
	
}
