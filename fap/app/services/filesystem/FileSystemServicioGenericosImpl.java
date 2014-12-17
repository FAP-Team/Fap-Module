package services.filesystem;

import java.util.List;

import models.ReturnUnidadOrganicaFap;
import services.ServiciosGenericosService;
import swhiperreg.service.ArrayOfReturnUnidadOrganica;
import utils.ComunicacionesInternasUtils;
import utils.ServiciosGenericosUtils;

public class FileSystemServicioGenericosImpl implements ServiciosGenericosService{

	@Override
	public void mostrarInfoInyeccion() {
		play.Logger.info("El servicio generico ha sido inyectado con FileSystem y está operativo.");
	}

	@Override
	public List<ReturnUnidadOrganicaFap> obtenerUnidadesOrganicas(Long codigo, String userId, String password) {
		List<ReturnUnidadOrganicaFap> lstUO = null;
		try {
			if ((codigo != null) && (codigo == 0)) {
				lstUO = ServiciosGenericosUtils.unidadesOrganicasNivel(0);
			} else {
				ReturnUnidadOrganicaFap unidad = ReturnUnidadOrganicaFap.find("Select unidadOrganica from ReturnUnidadOrganicaFap unidadOrganica where unidadOrganica.codigo = ?", codigo).first();
				if (unidad != null)
					lstUO = ServiciosGenericosUtils.obtenerDescendeciaUO(unidad);
			}
			
		} catch (Exception e) {
			play.Logger.error("No se han podido recuperar las Unidades Orgánicas: " + e.getMessage());
		}
		
		return lstUO;
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
