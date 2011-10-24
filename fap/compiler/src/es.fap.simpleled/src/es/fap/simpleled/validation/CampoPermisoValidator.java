package es.fap.simpleled.validation;

import java.util.ArrayList;
import java.util.List;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.PermisoVar;
import es.fap.simpleled.led.util.Proposal;

public class CampoPermisoValidator extends LedElementValidator {
	
	public List<Proposal> completeVariables(List<PermisoVar> vars) {
		List<Proposal> proposals = new ArrayList<Proposal>();
		for (PermisoVar var: vars){
			proposals.add(new Proposal(var.getName() + "  -  " + var.getTipo().getName(), true));
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
