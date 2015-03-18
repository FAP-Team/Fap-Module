package services;

import java.util.List;
import models.AsientoCIFap;
import models.RespuestaCIFap;
import models.ReturnUnidadOrganicaFap;

public interface ComunicacionesInternasService {

	public void mostrarInfoInyeccion();
	public RespuestaCIFap crearNuevoAsiento(AsientoCIFap asientoFap) throws ComunicacionesInternasServiceException;
	public RespuestaCIFap crearNuevoAsientoAmpliado(AsientoCIFap asientoAmpliadoFap) throws ComunicacionesInternasServiceException;

}
