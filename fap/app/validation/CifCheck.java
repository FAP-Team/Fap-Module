package validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.data.validation.Check;

public class CifCheck extends Check {
	
	private static final int CIF_LENGTH = 9;
	
	private static final String ERROR_TAM_CIF = "Longitud del CIF inválida";
	private static final String ERROR_FORMATO_CIF = "Formato del CIF incorrecto";
	private static final String ERROR_CONTROL_CIF = "Dígito de control del CIF incorrecto";

	@Override
	public boolean isSatisfied(Object validatedObject, Object value) {
		StringBuilder texto = new StringBuilder();
		if (value instanceof String) {
			boolean result = validaCif((String)value, texto);
			setMessage(texto.toString());
			return result;
		}
		return false;
	}
	
	public static boolean validaCif (String cif, StringBuilder texto) {
		
		if (cif.length() == 0)
			return true;

		if (cif.length() != CIF_LENGTH) {
			texto.append(ERROR_TAM_CIF);
			return false;
		}

		Pattern cifPattern = Pattern
				.compile("([ABCDEFGHJKLMNPQRSUVWabcdefghjklmnpqrsuvw])(\\d)(\\d)(\\d)(\\d)(\\d)(\\d)(\\d)([abcdefghijABCDEFGHIJ0123456789])");

		Matcher m = cifPattern.matcher(cif);
		if (m.matches()) {
			//log.info("CIF cumple el patrón:");

			// Sumamos las posiciones pares de los números centrales (en
			// realidad posiciones 3,5,7 generales)
			int sumaPar = Integer.parseInt(m.group(3))
					+ Integer.parseInt(m.group(5))
					+ Integer.parseInt(m.group(7));

			//log.info("Suma par: " + sumaPar);

			// Multiplicamos por 2 las posiciones impares de los números
			// centrales (en realidad posiciones 2,4,6,8 generales)
			// Y sumamos ambos dígitos: el primer digito sale al dividir por 10
			// (es un entero y quedará 0 o 1)
			// El segundo dígito sale de modulo 10
			int sumaDigito2 = ((Integer.parseInt(m.group(2)) * 2) % 10)
					+ ((Integer.parseInt(m.group(2)) * 2) / 10);
			int sumaDigito4 = ((Integer.parseInt(m.group(4)) * 2) % 10)
					+ ((Integer.parseInt(m.group(4)) * 2) / 10);
			int sumaDigito6 = ((Integer.parseInt(m.group(6)) * 2) % 10)
					+ ((Integer.parseInt(m.group(6)) * 2) / 10);
			int sumaDigito8 = ((Integer.parseInt(m.group(8)) * 2) % 10)
					+ ((Integer.parseInt(m.group(8)) * 2) / 10);

			int sumaImpar = sumaDigito2 + sumaDigito4 + sumaDigito6
					+ sumaDigito8;
			//log.info("Suma impar: " + sumaImpar);
			int suma = sumaPar + sumaImpar;
			int control = 10 - (suma % 10);
			// La cadena comienza en el caracter 0, J es 0, no 10
			if (control == 10)
				control = 0;
			String letras = "JABCDEFGHI";
			//log.info("Control: " + control + " ó "
			//		+ letras.substring(control, control + 1));

			// El dígito de control es una letra
			if (m.group(1).equalsIgnoreCase("K")
					|| m.group(1).equalsIgnoreCase("P")
					|| m.group(1).equalsIgnoreCase("Q")
					|| m.group(1).equalsIgnoreCase("S")) {

				//log.debug("Tiene que ser una letra");
				if (m.group(9).equalsIgnoreCase(
						letras.substring(control, control + 1)))
					return true;
				else {
					texto.append(ERROR_FORMATO_CIF);
					return false;
				}
			}
			// El dígito de control es un número
			else if (m.group(1).equalsIgnoreCase("A")
					|| m.group(1).equalsIgnoreCase("B")
					|| m.group(1).equalsIgnoreCase("E")
					|| m.group(1).equalsIgnoreCase("H")) {

				//log.info("Tiene que ser un numero");
				if (m.group(9).equalsIgnoreCase("" + control))
					return true;
				else {
					texto.append(ERROR_CONTROL_CIF);
					return false;
				}
			}
			// El dígito de control puede ser un número o una letra
			else {
				if (m.group(9).equalsIgnoreCase(
						letras.substring(control, control + 1))
						|| m.group(9).equalsIgnoreCase("" + control))
					return true;
				else {
					texto.append(ERROR_CONTROL_CIF);
					return false;
				}
			}
		} else {
			texto.append(ERROR_FORMATO_CIF);
			return false;
		}

	}

}
