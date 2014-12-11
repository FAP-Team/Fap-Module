package baremacion;

import play.mvc.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import controllers.fap.AgenteController;
import controllers.fap.ConsultarEvaluacionesController;
import controllers.fap.PresentacionFapController;
import enumerado.fap.gen.EstadosEvaluacionEnum;

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

	public static void validarCEconomicosCopia(long idSolicitud, List<CEconomico> ceconomicos){
	}
	
	public static List<Documento> getDocumentosAccesibles(Long idSolicitud, Long idEvaluacion){
		List <Documento> documentos = new ArrayList<Documento>();
		SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(idSolicitud);
		Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
		documentos.addAll(dbSolicitud.documentacion.documentos);
		documentos.add(evaluacion.solicitudEnEvaluacion);
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
					if (ceconomicoE.tipo.nombre.equals(ceconomicoS.tipo.nombre)
							&& ceconomicoE.tipo.jerarquia.equals(ceconomicoS.tipo.jerarquia)){
						for (int i=0; i<tipoEvaluacion.duracion; i++){
							ceconomicoE.valores.get(i).valorSolicitado = ceconomicoS.valores.get(i).valorSolicitado;
//							ceconomicoE.valores.get(i).valorEstimado = ceconomicoS.valores.get(i).valorSolicitado;
						}
						break;
					}
				}
				if (ceconomicoS.tipo.tipoOtro){
					for (CEconomicosManuales ceconomicoManual: ceconomicoS.otros){
						for(CEconomico ceconomicoE : evaluacion.ceconomicos){
							if (ceconomicoE.tipo.nombre.equals(ceconomicoManual.tipo.nombre)
									&& ceconomicoE.tipo.jerarquia.equals(ceconomicoManual.tipo.jerarquia)){
								for (int i=0; i<tipoEvaluacion.duracion; i++){
									ceconomicoE.valores.get(i).valorSolicitado = ceconomicoManual.valores.get(i).valorSolicitado;
//									ceconomicoE.valores.get(i).valorEstimado = ceconomicoManual.valores.get(i).valorSolicitado;
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
	
	
	public static void setOficialEvaluacion(Long idSolicitud, Long idEvaluacion) throws Exception{
		File solicitudEnEvaluacion = null;
		Evaluacion evaluacion = Evaluacion.findById(idEvaluacion);
		if (evaluacion == null){
			throw new Exception("No existe la evaluación");
		}
		if (evaluacion.solicitudEnEvaluacion == null){
			evaluacion.solicitudEnEvaluacion = new Documento();
			evaluacion.save();
		}
        if(evaluacion.solicitudEnEvaluacion.uri == null){
            try {
            	TramiteBase tramite = PresentacionFapController.invoke(PresentacionFapController.class, "getTramiteObject", idSolicitud);
            	SolicitudGenerica solicitud = SolicitudGenerica.findById(idSolicitud);
            	solicitudEnEvaluacion = tramite.getDocumentoBorrador();
            	evaluacion.solicitudEnEvaluacion.descripcion="Solicitud a Evaluar";
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
                throw ex2;
            } catch (Throwable e) {
            	Messages.error("Error generando el documento de solicitud para ver en evaluación.");
                play.Logger.error("Error generando el documento de solicitud para ver en evaluación, fallo en getTramiteObject: "+e.getMessage());
                throw new Exception(e.getMessage());
			}
        }
	}
	
	public static void validarCEconomicosEvaluados(long idSolicitud, List<CEconomico> ceconomicos) {
	}
	
	public static void finalizarEvaluaciones() {
		if (!Messages.hasErrors()) {
			List<Evaluacion> evaluaciones = Evaluacion.findAll();
			for (Evaluacion evaluacion: evaluaciones) {
				if (evaluacion.estado == null)
					Messages.error("La evaluación del expediente "+evaluacion.solicitud.expedienteAed.idAed+" está aún sin estado");
				else if ((!evaluacion.estado.equals(EstadosEvaluacionEnum.rechazada.name())) && (!evaluacion.estado.equals(EstadosEvaluacionEnum.evaluada.name()))) {
					Messages.error("La evaluación del expediente "+evaluacion.solicitud.expedienteAed.idAed+" está aún en estado: "+evaluacion.estado);
				}
			}
		}
		if (Messages.hasErrors()) {
			Messages.keep();
		} else { // Todo ha ido bien, se puede Finalizar (Pasar a la siguiente Fase de relleno de los dos últimos valores de los conceptos economicos)
			List<Evaluacion> evaluaciones = Evaluacion.findAll();
			for (Evaluacion evaluacion: evaluaciones) {
				for (CEconomico conceptoE: evaluacion.ceconomicos) {
					for (CEconomico conceptoS: evaluacion.solicitud.ceconomicos) {
						if (conceptoS.tipo.jerarquia.equals(conceptoE.tipo.jerarquia)) {
							for (int i=0; i<evaluacion.tipo.duracion; i++) {
								conceptoS.valores.get(0).valorEstimado = conceptoE.valores.get(0).valorEstimado;
								conceptoS.valores.get(0).valorPropuesto = conceptoE.valores.get(0).valorPropuesto;
							}
							conceptoS.save();
							break;
						}
					}
				}
			}
			TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
			// Mejorar: no se debería tocar.
			tipoEvaluacion.estado="evaluada";
			tipoEvaluacion.save();
			Messages.ok("Todas las evaluaciones han finalizado correctamente");
		}
	}
	
	
	public static void recalcularEvaluaciones() {
		boolean admin = "administradorgestor".contains(AgenteController.getAgente().rolActivo);
		if (!Messages.hasErrors()) {
			List<Evaluacion> evaluaciones = Evaluacion.find("select evaluacion from Evaluacion evaluacion where evaluacion.estado = ?", "evaluada").fetch();
			for (Evaluacion evaluacion: evaluaciones) {
				Messages.clear();
				BaremacionService.calcularTotales(evaluacion, admin, true);
				evaluacion.save();
			}
		}
		if (!Messages.hasErrors()) {
			Messages.ok("Se han recalculado las evaluaciones correctamente");
		}
	}
	
	public static boolean checkFinalizarEvaluacion(Evaluacion evaluacion) {
		//Sobreescribir si es necesario realizar una comprobacion antes de finalizar la evaluacion
		return true;
	}
	
}
