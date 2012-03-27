package validation;

import java.util.List;

import models.RepresentantePersonaJuridica;
import models.Solicitante;
import play.data.validation.Check;

public class RepresentanteCheck extends Check {

	@Override
	public boolean isSatisfied(Object arg0, Object arg1) {
		StringBuilder texto = new StringBuilder();
		if (arg1 instanceof Solicitante) {
			boolean result = validaRepresentante((Solicitante)arg1, null, texto);
			setMessage(texto.toString());
			return result;
		}
		return false;
	}
	
	public boolean validaRepresentante (Solicitante solicitante, List<RepresentantePersonaJuridica> representantesJuridica, StringBuilder texto) {
		if (solicitante.representado){
			if (solicitante.tipo.equals("fisica")){ // Para comprobar el representante de una persona Fisica
				if (solicitante.representante.getNumeroId().toUpperCase().equals(solicitante.getNumeroId().toUpperCase())){
					texto.append("El solicitante no puede tenerse a él mismo como representante");
					return false;
				}
			} else if (solicitante.tipo.equals("juridica")){ // Para comprobar todos los representantes que pueda tener una persona Juridica
				if (representantesJuridica != null){
					for(RepresentantePersonaJuridica representante: representantesJuridica){
						if (representante.getNumeroId().toUpperCase().equals(solicitante.getNumeroId().toUpperCase())){
							texto.append("El solicitante no puede tenerse a él mismo como representante");
							return false;
						}
					}
				}
			}
		}
		return true;
	}

}
