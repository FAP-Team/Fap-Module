package templates

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import messages.Messages;
import messages.Messages.MessageType;
import models.ReturnUnidadOrganicaFap;

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
		params.putStr("campo", campo.firstLower());
		
		Entidad entidad = null;
		if (campo.getUltimaEntidad() != null) {
			entidad = Entidad.create(campo.getUltimaEntidad())
			//params.putStr("entidad", entidad.clase);
			
			if (!entidad.clase.equals("ReturnUnidadOrganicaFap")){
				throw new Exception("Error generando combo en cascada para unidades orgánicas, la entidad de destino debe ser del tipo ReturnUnidadOrganicaFap");
			}
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

			public static ReturnUnidadOrganicaFap getUnidadOrganicaFAP(Long codUnidadOrganica){
				ReturnUnidadOrganicaFap unidad = null;
				if (codUnidadOrganica == null) {
					if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro codUnidadOrganica"))
						Messages.fatal("Falta parÃ¡metro codUnidadOrganica");
				} else {
					unidad = ReturnUnidadOrganicaFap.find("Select unidadOrganica from ReturnUnidadOrganicaFap unidadOrganica where unidadOrganica.codigo = ?", codUnidadOrganica).first();
					if (unidad == null) {
						Messages.fatal("Error al recuperar Unidad Orgánica");
					}
				}
				return unidad;
			}
		""";
	}
	
	public String validateCopy(Stack<Set<String>> validatedFields){
		String validation = super.validate(validatedFields);
		if (combo.requerido)
			validation += "CustomValidation.required(\"${campo.firstLower()}\", ${campo.firstLower()});\n";
		return validation;
	}
}
