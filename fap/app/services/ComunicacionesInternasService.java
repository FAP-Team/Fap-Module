package services;

import java.util.List;

import models.AsientoAmpliadoCIFap;
import models.AsientoCIFap;
import models.ReturnComunicacionInternaAmpliadaFap;
import models.ReturnComunicacionInternaFap;
import models.ReturnUnidadOrganicaFap;

public interface ComunicacionesInternasService {

	public ReturnComunicacionInternaFap crearNuevoAsiento(AsientoCIFap asientoFap) throws ComunicacionesInternasServiceException;
	
	public ReturnComunicacionInternaAmpliadaFap crearNuevoAsientoAmpliado(AsientoAmpliadoCIFap asientoAmpliadoFap) throws ComunicacionesInternasServiceException;

	public void mostrarInfoInyeccion();
	
}
