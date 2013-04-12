package validation;

import java.util.ArrayList;

import models.PersonaFisica;
import play.data.validation.Check;

public class GRBCheck extends Check {

	@Override
	public boolean isSatisfied(Object validatedObject, Object value) {
		return validaGRB(((String) validatedObject), (ArrayList<String>) value);
	}
	
	public boolean validaGRB (String texto, ArrayList<String> values) {
		return (values.indexOf(texto) != -1); //El valor no se encuentra entre los permitidos
	}
}
