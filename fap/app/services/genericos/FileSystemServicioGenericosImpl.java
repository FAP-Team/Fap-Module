package services.genericos;

import java.util.List;
import models.ReturnUnidadOrganicaFap;
import swhiperreg.service.ArrayOfReturnUnidadOrganica;

public class FileSystemServicioGenericosImpl implements ServiciosGenericosService{

	@Override
	public void mostrarInfoInyeccion() {
		play.Logger.info("El servicio generico ha sido inyectado con FileSystem y está operativo.");
	}

	@Override
	public List<ReturnUnidadOrganicaFap> obtenerUnidadesOrganicas(Long codigo, String userId, String password) {
		return ServiciosGenericosUtils.obtenerUnidadesOrganicasBD(codigo);
	}

	@Override
	public List<ReturnUnidadOrganicaFap> obtenerUnidadesOrganicasV(Long codigo, String credencialesXml) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validarUsuario(String userId, String password) {
		return true;
	}

}
