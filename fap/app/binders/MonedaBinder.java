package binders;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.Annotation;

import play.data.binding.Global;
import play.data.binding.TypeBinder;

@Global
public class MonedaBinder implements TypeBinder<BigDecimal> {
	
	/**
	 * Sobreescribimos el Bind de las Monedas
	 */
	@Override
	public Object bind(String name,
			java.lang.annotation.Annotation[] annotations, String value,
			Class actualClass, Type genericType) throws Exception {

		if (!value.matches(".*[eE].*")) {
			value = value.replaceAll("[.]", "");
			value = value.replaceAll(",", ".");
		}
		
		boolean nullOrEmpty = value == null || value.trim().length() == 0;
        if (nullOrEmpty) {
            return actualClass.isPrimitive() ? 0d : null;
        }
		return new BigDecimal(value);
	}
}