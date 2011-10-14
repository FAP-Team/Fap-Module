package es.fap.simpleled.validation;

import java.util.ArrayList;
import java.util.List;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.PermisoVar;

public class CampoPermisoValidator extends LedElementValidator {
	
	public List<String> completeVariables(List<PermisoVar> vars) {
		List<String> proposals = new ArrayList<String>();
		for (PermisoVar var: vars){
			proposals.add(var.getName() + "  -  " + var.getTipo().getName());
			proposals.addAll(completeEntidad(var.getName(), var.getTipo()));
		}
		return proposals;
	}

	@Override
	public boolean aceptaEntidad(Entity entidad) {
		return true;
	}

	@Override
	public boolean aceptaAtributo(Attribute atributo) {
		return true;
	}

	@Override
	public String mensajeError() {
		return null;
	}

}
