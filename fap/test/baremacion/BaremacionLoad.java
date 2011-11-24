package baremacion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import controllers.fap.InitController;

import models.CEconomico;
import models.Criterio;
import models.Documento;
import models.Evaluacion;
import models.SolicitudGenerica;
import models.TipoCriterio;
import models.TipoEvaluacion;
import play.db.Model;
import play.mvc.Scope.Params;
import play.test.Fixtures;
import play.test.UnitTest;
import play.vfs.VirtualFile;

public class BaremacionLoad extends UnitTest {

	@Test
	public void load() throws Throwable {
		//Borra todas las evaluaciones y los tipos
		//CEconomico.deleteAll();
		//Criterio.deleteAll();
		List<Class<? extends Model>> modelsToDelete = new ArrayList<Class<? extends Model>>();
		modelsToDelete.add(Evaluacion.class);
		modelsToDelete.add(TipoEvaluacion.class);
		Fixtures.delete(modelsToDelete);
		
		
		
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
		TipoEvaluacion tipoEv = new TipoEvaluacion();
		tipoEv.nombre = "Evaluacion innovación";
		tipoEv.criterios = tipocriterios;
		tipoEv.tiposDocumentos = new ArrayList<String>();
		tipoEv.tiposDocumentos.add("eadmon://gobcan.es/tiposDocumentos/TDC000000000000000001/v01");
		
		return tipoEv;
	}
	
	private Evaluacion createEvaluacion(TipoEvaluacion tipo){
		Evaluacion evaluacion = new Evaluacion();
		evaluacion.init(tipo);
		
		return evaluacion;
	}
	
	private Documento createDocumento(String tipo){
		Documento d = new Documento();
		d.tipo = tipo;
		return d;
	}
	
	private SolicitudGenerica getOrCreateSolicitudGenerica() throws Throwable {
		SolicitudGenerica sg = SolicitudGenerica.all().first();
		if(sg == null){
			sg = (SolicitudGenerica)InitController.inicialize();
		}
		
		sg.documentacion.documentos.add(createDocumento("eadmon://gobcan.es/tiposDocumentos/TDC000000000000000001/v01"));
		sg.documentacion.documentos.add(createDocumento("eadmon://gobcan.es/tiposDocumentos/TDC000000000000000001/v01"));
		sg.documentacion.documentos.add(createDocumento("eadmon://gobcan.es/tiposDocumentos/TDC000000000000000002/v01"));
		
		return sg;
	}
	
}
