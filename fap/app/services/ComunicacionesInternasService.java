package services;

import java.util.List;
import models.AsientoAmpliadoCIFap;
import models.AsientoCIFap;
import models.RespuestaCIAmpliadaFap;
import models.RespuestaCIFap;
import models.ReturnUnidadOrganicaFap;

public interface ComunicacionesInternasService {

	public void mostrarInfoInyeccion();
	public RespuestaCIFap crearNuevoAsiento(AsientoCIFap asientoFap) throws ComunicacionesInternasServiceException;
	public RespuestaCIAmpliadaFap crearNuevoAsientoAmpliado(AsientoAmpliadoCIFap asientoAmpliadoFap) throws ComunicacionesInternasServiceException;

}
