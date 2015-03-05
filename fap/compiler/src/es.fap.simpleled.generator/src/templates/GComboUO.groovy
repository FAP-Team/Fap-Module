package templates

import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import es.fap.simpleled.led.ComboUO;
import es.fap.simpleled.led.Combo;
import es.fap.simpleled.led.Grupo;
import generator.utils.CampoUtils;
import generator.utils.TagParameters;
import es.fap.simpleled.led.util.LedCampoUtils;
import generator.utils.*;

class GComboUO extends GElement{

	ComboUO combo;
	CampoUtils campo;
	GElement gPaginaPopup;
	
	public GComboUO(ComboUO combo, GElement container){
		super(combo, container);
		this.combo = combo;
		campo = CampoUtils.create(combo.campo);
		gPaginaPopup = getPaginaOrPopupContainer();
	}
	
	public String view(){
		TagParameters params = new TagParameters();
		
		Entidad entidad = null;
		if (campo.getUltimaEntidad() != null) {
			entidad = Entidad.create(campo.getUltimaEntidad())
			params.putStr("entidad", entidad.clase);
		}
		
		String controllerName = gPaginaPopup.controllerFullName();
		if (controllerName != null)
			params.putStr("controllerName", controllerName);

		if(combo.name != null)
			params.putStr("id", combo.name);
		if(combo.titulo != null)
			params.putStr("titulo", combo.titulo);
		if(combo.requerido)
			params.put "requerido", true;
		if(combo.busqueda)
			params.put "searchable", true;
	
		return """
           #{fap.comboUO ${params.lista()} /}		
		""";
	}
	
	public String controller(){
		return """
			public static String handlerComboUO(int codigo, int subnivel){
				List<ReturnUnidadOrganicaFap> lstUO = null;
				List<ReturnUnidadOrganicaFap> lstUOSubNivel = null;
				List<ComboItem> lstCombo = new ArrayList<ComboItem>();
				String resultados = null;
				
				if (!Messages.hasErrors()) {
					lstUO = ServiciosGenericosUtils.obtenerUnidadesOrganicasBD((long) codigo);
					if (lstUO != null){
						ServiciosGenericosUtils.cargarUnidadesOrganicas(lstUO);
						lstUOSubNivel = new ArrayList<ReturnUnidadOrganicaFap>();
						for (ReturnUnidadOrganicaFap unidad : lstUO){
							if (ServiciosGenericosUtils.calcularNivelUO(unidad) == subnivel)
								lstUOSubNivel.add(unidad);
						}
						
						if (lstUOSubNivel != null) {
							for (ReturnUnidadOrganicaFap unidad: lstUOSubNivel)
								lstCombo.add(new ComboItem(unidad.codigo, unidad.codigoCompleto + " - " + unidad.descripcion));
								
							resultados = new Gson().toJson(lstCombo);
						}
					}
				}
				
				return resultados;
			}
		""";
	}
}
