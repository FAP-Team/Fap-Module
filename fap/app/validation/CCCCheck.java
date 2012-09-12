package validation;

import java.util.regex.*;

import models.CCC;
import models.Nip;
import play.Logger;
import play.data.validation.Check;

public class CCCCheck extends Check {
	
	@Override
	public boolean isSatisfied(Object validatedObject, Object value) {
		StringBuilder texto = new StringBuilder();
		if(value instanceof CCC){			
			CCC ccc = (CCC)value;
			boolean result = validaCCC(ccc, texto);
			setMessage(texto.toString());
			return result;
		}
		//No es un ccc, no lo valida
		return true;
	}
	
	public boolean validaCCC (CCC ccc, StringBuilder texto) {
		if (ccc.cccCodigoEntidad.isEmpty() && ccc.cccCodigoOficina.isEmpty() && ccc.cccDigitosControl.isEmpty() && ccc.cccNumeroCuenta.isEmpty())
			return true;
		if ((ccc.cccCodigoEntidad != null && ccc.cccCodigoEntidad.matches("\\d{4}$")) &&
			(ccc.cccCodigoOficina != null  && ccc.cccCodigoOficina.matches("\\d{4}$")) &&
			(ccc.cccDigitosControl != null  && ccc.cccDigitosControl.matches("\\d{2}$")) &&
			(ccc.cccNumeroCuenta != null  && ccc.cccNumeroCuenta.matches("\\d{10}$"))){
			if (ccc.cccDigitosControl.equals(calcularDigitosControl(ccc.cccCodigoEntidad, ccc.cccCodigoOficina, ccc.cccNumeroCuenta)))
				return true;
			else{
				texto.append("El Número de la Cuenta Bancaria es incorrecto.");
				return false;
			}
		} else{
			texto.append("Longitud de Número de Cuenta incorrecto.");
			return false;
		}
	}
	
	private String calcularDigitosControl(String entidad, String oficina, String cuenta){
		return calcularDigitoControl("00"+entidad+oficina)+calcularDigitoControl(cuenta);
	}
	
	private String calcularDigitoControl (String numero){
		int aPesos[] = {1,2,4,8,5,10,9,7,3,6};

	    int nResto = 0;
	    for (int nItem = 0; nItem < numero.length(); nItem++){
	    	 if (!numero.substring(nItem, nItem+1).isEmpty())
	    		 nResto += Integer.parseInt(numero.substring(nItem, nItem+1)) * aPesos[nItem];
	    }
	 
        nResto = 11 - (nResto % 11);
	    if (nResto == 11)
	         nResto = 0;
	    else if (nResto == 10)
	         nResto = 1;
	    
	    return String.valueOf(nResto);
	}

}
