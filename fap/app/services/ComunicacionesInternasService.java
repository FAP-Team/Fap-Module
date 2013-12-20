package services;

import java.util.List;

import models.AsientoCIFap;
import models.ReturnComunicacionInternaFap;

public interface ComunicacionesInternasService {

	public ReturnComunicacionInternaFap crearNuevoAsiento(AsientoCIFap asientoFap);

	public void mostrarInfoInyeccion();
	
	public List<String> obtenerUnidadesOrganicas(String userId, String password);
	
}
