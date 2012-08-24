package binders;

import java.lang.reflect.Type;

import java.text.Annotation;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.data.binding.Global;
import play.data.binding.TypeBinder;

@Global
public class DoubleBinder implements TypeBinder<Double> {
	
	static final Pattern doubleNumber = Pattern.compile("[-+]?\\d{1,3}(\\.?\\d{3})*(\\,[0-9]+)?([eE][-+]?[0-9]+)?");
	
	/**
	 * Sobreescribimos el Bind de las Monedas
	 */
	@Override
	public Object bind(String name,
			java.lang.annotation.Annotation[] annotations, String value,
			Class actualClass, Type genericType) throws Exception {

		Matcher mBest = doubleNumber.matcher(value);
		if (mBest.matches()) {
			value = value.replaceAll("[.]", "");
			value = value.replaceAll(",", ".");			
		} else {
			value = value.replaceAll("[.]", ",");
		}
				
		boolean nullOrEmpty = value == null || value.trim().length() == 0;
        if (nullOrEmpty) {
            return actualClass.isPrimitive() ? 0d : null;
        }
		return Double.parseDouble(value);
	}
	
}