package services;

import java.util.List;
import models.ReturnUnidadOrganicaFap;

public interface ServiciosGenericosService {
	public void mostrarInfoInyeccion();
	public List<ReturnUnidadOrganicaFap> obtenerUnidadesOrganicas(Long codigo, String userId, String password); 
	public List<ReturnUnidadOrganicaFap> obtenerUnidadesOrganicasV(Long codigo, String credencialesXml); 
	public boolean validarUsuario (String userId, String password);
}
