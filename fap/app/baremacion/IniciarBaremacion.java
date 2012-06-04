package baremacion;

import java.util.List;

import models.CEconomico;
import models.Criterio;
import models.Evaluacion;
import models.SolicitudGenerica;
import models.TipoEvaluacion;
import play.db.jpa.GenericModel.JPAQuery;
import services.BaremacionService;

public class IniciarBaremacion {
	// Clase de la que extiende el Iniciar Baremacion de cada Aplicacion independiente
	
	public static void iniciar(){
		
		TipoEvaluacion tipoEvaluacion = TipoEvaluacion.all().first();
		
		//Comprueba que todas las solicitudes tengan su evaluación creada
		List<SolicitudGenerica> solicitudesSinEvaluacion = SolicitudGenerica.find("select solicitud from Solicitud solicitud " +
				       "where not exists (select evaluacion from Evaluacion evaluacion " +
				       "where evaluacion.tipo.id=? and evaluacion.solicitud = solicitud)", tipoEvaluacion.id).fetch();
	
		for(SolicitudGenerica solicitud : solicitudesSinEvaluacion){
			Evaluacion evaluacion = new Evaluacion();
			evaluacion.init(tipoEvaluacion);
			evaluacion.solicitud = solicitud;
			evaluacion.save();
		}
	}
}
