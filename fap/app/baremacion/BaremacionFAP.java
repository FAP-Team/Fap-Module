package baremacion;

import java.util.List;

import messages.Messages;
import models.CEconomico;
import models.CEconomicosManuales;
import models.Criterio;
import models.Evaluacion;
import models.SolicitudGenerica;
import models.TipoEvaluacion;
import play.db.jpa.GenericModel.JPAQuery;
import services.BaremacionService;

public class BaremacionFAP {
	// Clase de la que extiende la Baremacion de cada Aplicacion independiente
	
	public static void iniciarBaremacion(){
		
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		
		//Comprueba que todas las solicitudes tengan su evaluación creada
		List<SolicitudGenerica> solicitudesSinEvaluacion = SolicitudGenerica.find("select solicitud from Solicitud solicitud " +
					   "where solicitud.estado=? and " +
				       "not exists (select evaluacion from Evaluacion evaluacion " +
				       "where evaluacion.tipo.id=? and evaluacion.solicitud = solicitud)", "iniciada", tipoEvaluacion.id).fetch();
		
	
		
		// Actualizamos los datos de las evaluaciones, por si hay algun parametro variable que se ha insertado nuevo (Ej: TipoCEconomico, TipoCriterio)
		List<Evaluacion> evaluaciones = Evaluacion.all().fetch();
		
		for(Evaluacion evaluacion: evaluaciones){
			evaluacion.actualizar(tipoEvaluacion);
			evaluacion.save();
		}
		
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
	
	public static void validarCEconomicos(long idSolicitud, List<CEconomico> ceconomicos){
	}
}
