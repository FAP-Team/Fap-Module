package services;

import models.AsientoCIFap;
import models.ReturnComunicacionInternaFap;

public interface ComunicacionesInternasService {

	public ReturnComunicacionInternaFap crearNuevoAsiento(AsientoCIFap asientoFap);

	public void mostrarInfoInyeccion();
	
}
