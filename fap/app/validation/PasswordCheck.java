package validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.data.validation.Check;
import properties.FapProperties;

public class PasswordCheck extends Check {
	
	private static final String ERROR_TAM_PASSWORD = "La longitud de la contraseña debe tener entre 6 y 10 carateres";
	private static final String TAM_MIN = "fap.password.min";
	private static final String TAM_MAX = "fap.password.max";
	
	@Override
	public boolean isSatisfied(Object validatedObject, Object value) {
		StringBuilder texto = new StringBuilder();
		if (value != null) {
			String password = (String)value;
			if ( (password.length() < FapProperties.getInt(TAM_MIN)) 
					|| (password.length() > FapProperties.getInt(TAM_MAX)) ) {
				texto.append(ERROR_TAM_PASSWORD);
				setMessage(texto.toString());
				return false;
			}
		}
		return true;
	}

}
