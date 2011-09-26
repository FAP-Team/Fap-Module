package validation;

import models.PersonaFisica;
import play.data.validation.Check;

public class PersonaFisicaCheck extends Check {

	@Override
	public boolean isSatisfied(Object arg0, Object arg1) {
		return validaPersonaFisica((PersonaFisica)arg1);
	}
	
	private boolean validaPersonaFisica (PersonaFisica personaF) {
		if (personaF.nombre.isEmpty()) {
			setMessage("validation.personafisica.nonombre");
			return false;
		}
		if (personaF.primerApellido.isEmpty()) {
			setMessage("validation.personafisica.noprimerapellido");
			return false;
		}
		
		if (personaF.nip.tipo.isEmpty()) {
			setMessage("validation.personafisica.noniptipo");
			return false;
		}
		
		if (personaF.nip.valor.isEmpty()) {
			setMessage("validation.personafisica.nonipvalor");
			return false;
		}
		NipCheck nCheck = new NipCheck();
		StringBuilder texto = new StringBuilder();
		boolean result =  nCheck.validaNip(personaF.nip, texto);
		if (!result) {
			setMessage(texto.toString());
			return false;
		}
		return true;
	}

}
