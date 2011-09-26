package validation;

import java.util.Collection;

import models.Nip;
import models.Persona;
import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;
import play.data.validation.Required;
import play.db.Model.BinaryField;
import play.exceptions.UnexpectedException;

/**
 * 
 * Mejora de la clase RequiredCheck, para añadir nuevas validaciones a los
 * elementos
 *
 */
@SuppressWarnings("serial")
public class CustomRequiredCheck extends AbstractAnnotationCheck<Required> {
    
    final static String mes = "validation.required";

    private boolean requiredString(Object value){
    	return value.toString().trim().length() > 0;
    }
    
    public boolean isSatisfied(Object validatedObject, Object value, OValContext context, Validator validator) {
        if (value == null) {
            return false;
        }
        if (value instanceof String) {
            return requiredString(value);
        }
        if (value instanceof Collection<?>) {
            return ((Collection<?>)value).size() > 0;
        }
        if (value instanceof BinaryField) {
            return ((BinaryField)value).exists();
        }
        if (value.getClass().isArray()) {
            try {
                return java.lang.reflect.Array.getLength(value) > 0;
            } catch(Exception e) {
                throw new UnexpectedException(e);
            }
        }
        if(value instanceof Nip){
        	Nip nip = (Nip) value;
        	return requiredString(nip.tipo) && requiredString(nip.valor);
        }
        
        return true;
    }
}
