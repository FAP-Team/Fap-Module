package validation;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import models.Nip;
import models.PersonaFisica;
import models.PersonaJuridica;
import models.Solicitante;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.FieldContext;
import net.sf.oval.context.MethodParameterContext;
import net.sf.oval.context.OValContext;
import net.sf.oval.exception.OValException;
import play.data.validation.Error;
import play.data.validation.Required;
import play.data.validation.ValidCheck;
import play.data.validation.Validation;
import play.data.validation.ValidationPlugin;
import play.data.validation.Validation.ValidationResult;
import play.exceptions.UnexpectedException;
import play.utils.Java;

@SuppressWarnings("serial")
public class CustomValidCheck extends ValidCheck {

	private	Field superKey;

	final static String mes = "validation.object";
	
	public CustomValidCheck() {
		super();
		try {
			superKey = ValidCheck.class.getDeclaredField("key");
		} catch (Exception e) {
			e.printStackTrace();
		}
		superKey.setAccessible(true);
	}

	public String getKey () {
		try {
			return (String)superKey.get(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public void setKey (String key) {
		try {
			superKey.set(this, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
