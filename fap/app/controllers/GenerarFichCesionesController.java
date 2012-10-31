package controllers;

import java.util.List;
import java.util.Map;

import security.ResultadoPermiso;
import security.SecureFap;
import tables.TableRecord;

import models.PeticionCesiones;
import controllers.gen.GenerarFichCesionesControllerGen;

public class GenerarFichCesionesController extends GenerarFichCesionesControllerGen {

	public static void tablatblCesiones() {

		java.util.List<PeticionCesiones> rows = PeticionCesiones.find("select peticionCesiones from PeticionCesiones peticionCesiones").fetch();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<PeticionCesiones> rowsFiltered = rows; //Tabla sin permisos, no filtra

		tables.TableRenderResponse<PeticionCesiones> response = new tables.TableRenderResponse<PeticionCesiones>(rowsFiltered, true, false, false, "ultimaEditable", "", "", getAccion(), ids);
		Long iter = new Long(0);
		for (TableRecord<PeticionCesiones> pC: response.rows) {
			ids.remove("idPeticionCesiones");
			ids.put("idPeticionCesiones", ++iter);

			//Llamar al permiso
			ResultadoPermiso rp = secure.check("ultimaEditable", "editable", "leer", ids, null);
			if(rp.getPrimeraAccion().equals("editar")){
				pC.permisoEditar = true;
				pC.permisoBorrar = true;
				pC.permisoLeer = true;
			}else{
				pC.permisoEditar = false;
				pC.permisoBorrar = false;
				pC.permisoLeer = true;				
			}
		}
		renderJSON(response.toJSON("tipo", "fechaGen", "fechaValidez", "estado", "id"));
	}
	
}
