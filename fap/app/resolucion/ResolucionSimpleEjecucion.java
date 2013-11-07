package resolucion;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityTransaction;

import org.joda.time.DateTime;

import play.db.jpa.JPA;
import properties.FapProperties;
import config.InjectorConfig;
import controllers.fap.AgenteController;
import controllers.fap.ResolucionControllerFAP;
import enumerado.fap.gen.EstadoLineaResolucionEnum;
import enumerado.fap.gen.EstadoNotificacionEnum;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.EstadosSolicitudEnum;

import reports.Report;
import services.FirmaService;
import services.GestorDocumentalService;
import services.NotificacionService;
import services.RegistroService;
import services.async.NotificacionServiceAsync;
import utils.NotificacionUtils;
import messages.Messages;
import models.Documento;
import models.DocumentoNotificacion;
import models.ExpedienteAed;
import models.LineaResolucionFAP;
import models.Notificacion;
import models.Registro;
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
		
		NotificacionServiceAsync notificacionServiceAsync = InjectorConfig.getInjector().getInstance(NotificacionServiceAsync.class);
		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);
		RegistroService registroService = InjectorConfig.getInjector().getInstance(RegistroService.class);
		
		for (LineaResolucionFAP linea: resolucion.resolucion.lineasResolucion) {

			SolicitudGenerica solicitud = SolicitudGenerica.findById(linea.solicitud.id);
			
			try  {
				
				// Se genera el documento oficio de remisión
				File documentoOficioRemision = generarDocumentoOficioRemision(linea);
				String uri = gestorDocumentalService.saveDocumentoTemporal(linea.documentoOficioRemision, documentoOficioRemision);
				
				// Se firma el documento oficio de remisión
				firmaService.firmarEnServidor(linea.documentoOficioRemision);
				listaExpedientes.add(linea.solicitud.expedienteAed);
				
				// Se copia el documento oficio de remisión al expediente
				gestorDocumentalService.copiarDocumentoEnExpediente(uri, listaExpedientes);
				
				// Se obtiene el justificante de registro de salida del oficio de remisión
				models.JustificanteRegistro justificanteSalida = registroService.registroDeSalida(solicitud.solicitante, linea.documentoOficioRemision, solicitud.expedientePlatino, "Oficio de remisión");				
				linea.registroDocumentoOficioRemision.informacionRegistro.setDataFromJustificante(justificanteSalida);
				Documento documento = linea.registroDocumentoOficioRemision.justificante;
				documento.tipo = FapProperties.get("fap.aed.tiposdocumentos.justificanteRegistroSalida");
				documento.descripcion = "Justificante de registro de salida del oficio de remisión";
				documento.save();
				InputStream is = justificanteSalida.getDocumento().contenido.getInputStream();
				uri = gestorDocumentalService.saveDocumentoTemporal(documento, is, "JustificanteOficioRemision" + ".pdf");
				play.Logger.info("Justificante del documento oficio de remisión almacenado en el AED");
				List<Documento> documentos = new ArrayList<Documento>();
		        documentos.add(linea.registroDocumentoOficioRemision.justificante);
				gestorDocumentalService.clasificarDocumentos(solicitud, documentos, true);
				
				// Copiarlo al expediente
				gestorDocumentalService.copiarDocumentoEnExpediente(uri, listaExpedientes);
				listaExpedientes.clear();
				solicitud.save();
			} catch (Throwable e)   {
				
			}
			
			// Se crea la notificación y se añade a la solicitud correspondiente
			
			Notificacion notificacion = new Notificacion();
			DocumentoNotificacion docANotificar = new DocumentoNotificacion(resolucion.resolucion.registro.oficial.uri);
			notificacion.documentosANotificar.add(docANotificar);
			DocumentoNotificacion docANotificar2 = new DocumentoNotificacion(linea.registroDocumentoOficioRemision.justificante.uri);
			notificacion.documentosANotificar.add(docANotificar2);
			notificacion.interesados.addAll(solicitud.solicitante.getAllInteresados());
			notificacion.descripcion = "Notificación de resolución simple de la fase de ejecución";
			notificacion.plazoAcceso = FapProperties.getInt("fap.notificacion.plazoacceso");
			notificacion.plazoRespuesta = FapProperties.getInt("fap.notificacion.plazorespuesta");
			notificacion.frecuenciaRecordatorioAcceso = FapProperties.getInt("fap.notificacion.frecuenciarecordatorioacceso");
			notificacion.frecuenciaRecordatorioRespuesta = FapProperties.getInt("fap.notificacion.frecuenciarecordatoriorespuesta");
			notificacion.estado = EstadoNotificacionEnum.creada.name();
			notificacion.idExpedienteAed = solicitud.expedienteAed.idAed;
			notificacion.asunto = "Notificación de resolución";
			notificacion.save();
			solicitud.notificaciones.add(notificacion);
			solicitud.save();

			// Se envía la notificación
			
			try {
				notificacionServiceAsync.enviarNotificaciones(notificacion, AgenteController.getAgente());
				play.Logger.info("Se ha puesto a disposición la notificación "+notificacion.id);
				notificacion.fechaPuestaADisposicion = new DateTime();
				notificacion.save();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				play.Logger.error("No se ha podido enviar la notificación "+notificacion.id+": "+e.getMessage());
				Messages.error("No se envío la notificación por problemas con la llamada al Servicio Web");
			}
				
			NotificacionUtils.recargarNotificacionesFromWS(FapProperties.get("fap.notificacion.procedimiento"));
		}
		
		//Una vez copiados los expedientes se comprueba si hay documentos de baremacion
		//y se avanza de fase segun el tipo de la resolucion
		if (!resolucion.resolucion.conBaremacion) {
				EntityTransaction tx = JPA.em().getTransaction();
				tx.commit();
				tx.begin();
				if (EstadoResolucionEnum.publicada.name().equals(resolucion.resolucion.estado))
					resolucion.avanzarFase_Registrada_PublicadaYNotificada(resolucion.resolucion);
				else
					resolucion.avanzarFase_Registrada_Notificada(resolucion.resolucion);
				tx.commit();
				tx.begin();
		}
	}

	@Override
	public void setLineasDeResolucion(Long idResolucion, List<Long> idsSeleccionados) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		Long idSeleccionado = idsSeleccionados.get(0);
		LineaResolucionFAP lResolucion = new LineaResolucionFAP();
		lResolucion.solicitud = SolicitudGenerica.find("select solicitud from SolicitudGenerica solicitud where ((solicitud.estado not in('borrador')) and (solicitud.id =?))", idSeleccionado).first();
		lResolucion.estado = EstadoLineaResolucionEnum.afectada.name();
		lResolucion.save();
		resolucion.lineasResolucion.add(lResolucion);
		resolucion.save();
	}
}
