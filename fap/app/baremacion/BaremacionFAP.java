package baremacion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import controllers.fap.PresentacionFapController;

import messages.Messages;
import models.CEconomico;
import models.CEconomicosManuales;
import models.Criterio;
import models.Documento;
import models.Evaluacion;
import models.SolicitudGenerica;
import models.TipoEvaluacion;
import play.db.jpa.GenericModel.JPAQuery;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import reports.Report;
import services.BaremacionService;
import services.GestorDocumentalService;
import tramitacion.TramiteBase;
import utils.BaremacionUtils;

@InjectSupport
public class BaremacionFAP {
	// Clase de la que extiende la Baremacion de cada Aplicacion independiente
	
	@Inject
	public static GestorDocumentalService gestorDocumentalService;
	
	public static void iniciarBaremacion(){
		iniciarNuevasEvaluaciones();
	}
	
	public static void validarCEconomicos(long idSolicitud, List<CEconomico> ceconomicos){
	}
	
	public static List<Documento> getDocumentosAccesibles(Long idSolicitud, Long idEvaluacion){
		List <Documento> documentos = new ArrayList<Documento>();
		SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(idSolicitud);
		documentos.addAll(dbSolicitud.documentacion.documentos);
		documentos.add(getOficialEvaluacion(idSolicitud, idEvaluacion));
		return documentos;
	}
	
	public static void iniciarNuevasEvaluaciones(){
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		
		//Comprueba que todas las solicitudes tengan su evaluación creada
		List<SolicitudGenerica> solicitudesSinEvaluacion = SolicitudGenerica.find("select solicitud from Solicitud solicitud " +
					   "where (solicitud.estado=? or (solicitud.estado=?)) and " +
				       "not exists (select evaluacion from Evaluacion evaluacion " +
				       "where evaluacion.tipo.id=? and evaluacion.solicitud = solicitud)", "iniciada", "verificado", tipoEvaluacion.id).fetch();
		
		if (solicitudesSinEvaluacion != null)
			play.Logger.info("Se van a crear "+solicitudesSinEvaluacion.size()+" nuevas evaluaciones de las solicitudes: "+solicitudesSinEvaluacion.toString());
		
		for(SolicitudGenerica solicitud : solicitudesSinEvaluacion){
			Evaluacion evaluacion = new Evaluacion();
			evaluacion.init(tipoEvaluacion);
			evaluacion.solicitud = solicitud;
			
			// Asignamos a la entidad evaluacion, los valores introducidos por el solicitante de CEconomicos, si los hubiera
			for(CEconomico ceconomicoS : solicitud.ceconomicos){
				for(CEconomico ceconomicoE : evaluacion.ceconomicos){
					if (ceconomicoE.tipo.nombre.equals(ceconomicoS.tipo.nombre)){
						for (int i=0; i<tipoEvaluacion.duracion; i++){
							ceconomicoE.valores.get(i).valorSolicitado = ceconomicoS.valores.get(i).valorSolicitado;
							ceconomicoE.valores.get(i).valorEstimado = ceconomicoS.valores.get(i).valorSolicitado;
						}
						break;
					}
				}
				if (ceconomicoS.tipo.tipoOtro){
					for (CEconomicosManuales ceconomicoManual: ceconomicoS.otros){
						for(CEconomico ceconomicoE : evaluacion.ceconomicos){
							if (ceconomicoE.tipo.nombre.equals(ceconomicoManual.tipo.nombre)){
								for (int i=0; i<tipoEvaluacion.duracion; i++){
									ceconomicoE.valores.get(i).valorSolicitado = ceconomicoManual.valores.get(i).valorSolicitado;
									ceconomicoE.valores.get(i).valorEstimado = ceconomicoManual.valores.get(i).valorSolicitado;
								}
								break;
							}
						}
					}
				}
			}
			
			evaluacion.save();
		}
	}
	
	public static Documento getOficialEvaluacion(Long idSolicitud, Long idEvaluacion){
		File solicitudEnEvaluacion = null;
		Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
        if(evaluacion.solicitudEnEvaluacion == null){
            try {
            	TramiteBase tramite = PresentacionFapController.invoke("getTramiteObject", idSolicitud);
            	SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
            	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", solicitud);
            	solicitudEnEvaluacion = new Report(tramite.getBodyReport()).header(tramite.getHeaderReport()).registroSize().renderTmpFile(solicitud);
                evaluacion.solicitudEnEvaluacion = new Documento();
                evaluacion.solicitudEnEvaluacion.tipo = FapProperties.get("fap.baremacion.evaluacion.documento.solicitud");
                evaluacion.save();
                gestorDocumentalService.saveDocumentoTemporal(evaluacion.solicitudEnEvaluacion, solicitudEnEvaluacion);
                List<Documento> documentos = new ArrayList();
                documentos.add(evaluacion.solicitudEnEvaluacion);
                gestorDocumentalService.clasificarDocumentos(solicitud, documentos);
                evaluacion.save();
            } catch (Exception ex2) {
                Messages.error("Error generando el documento de solicitud para ver en evaluación");
                play.Logger.error("Error generando el de solicitud para ver en evaluación: "+ex2.getMessage());
            } catch (Throwable e) {
            	Messages.error("Error generando el documento de solicitud para ver en evaluación.");
                play.Logger.error("Error generando el documento de solicitud para ver en evaluación, fallo en getTramiteObject: "+e.getMessage());
			}
        }
		return evaluacion.solicitudEnEvaluacion;
	}
}
