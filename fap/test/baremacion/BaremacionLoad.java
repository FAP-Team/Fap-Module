package baremacion;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import controllers.fap.InitController;

import models.Evaluacion;
import models.SolicitudGenerica;
import models.TipoCriterio;
import models.TipoEvaluacion;
import play.mvc.Scope.Params;
import play.test.UnitTest;
import play.vfs.VirtualFile;

public class BaremacionLoad extends UnitTest {

	@Test
	public void load() throws Throwable {
		//Borra todas las evaluaciones y los tipos
		Evaluacion.deleteAll();
		TipoEvaluacion.deleteAll();
		
		//Carga el tipo de evaluación a partir del GSON
		TipoEvaluacion tipo = loadFromJson();
		tipo.save();
		
		SolicitudGenerica solicitud = getOrCreateSolicitudGenerica();
		
		Evaluacion evaluacion = createEvaluacion(tipo);
		evaluacion.solicitud = solicitud;
		evaluacion.save();
		
		assertNotNull(tipo);
		
	}
	
	private TipoEvaluacion loadFromJson(){
		//Recuepera lista de criterios del json
		VirtualFile vf = VirtualFile.fromRelativePath("{module:fap}/test/baremacion/baremacionInnovacion.json");
		Type collectionType = new TypeToken<Collection<TipoCriterio>>(){}.getType();
		List<TipoCriterio> tipocriterios = new Gson().fromJson(vf.contentAsString(), collectionType);
		
		//Crea el nuevo tipo de evaluacion
		TipoEvaluacion tipoEvaluacion = new TipoEvaluacion();
		tipoEvaluacion.nombre = "Evaluacion innovación";
		tipoEvaluacion.criterios = tipocriterios;
		
		return tipoEvaluacion;
	}
	
	private Evaluacion createEvaluacion(TipoEvaluacion tipo){
		Evaluacion evaluacion = new Evaluacion();
		evaluacion.init(tipo);
		return evaluacion;
	}
	
	private SolicitudGenerica getOrCreateSolicitudGenerica() throws Throwable {
		SolicitudGenerica solicitudGenerica = SolicitudGenerica.all().first();
		if(solicitudGenerica == null){
			solicitudGenerica = (SolicitudGenerica)InitController.inicialize();
		}
		return solicitudGenerica;
	}
	
}
