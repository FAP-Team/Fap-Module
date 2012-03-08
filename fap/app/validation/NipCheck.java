package validation;

import java.util.regex.*;

import models.Nip;
import play.Logger;
import play.data.validation.Check;

public class NipCheck extends Check {

	private static final Pattern NIF_PATTERN = Pattern.compile("\\d{8}[A-Z]");
	private static final Pattern NIE_PATTERN = Pattern.compile("[XYZ]\\d{7}[A-Z]");
	
	private static final String NIF_NIE_ASOCIATION = "TRWAGMYFPDXBNJZSQVHLCKET";
	
	@Override
	public boolean isSatisfied(Object validatedObject, Object value) {
		StringBuilder texto = new StringBuilder();
		if(value instanceof Nip){			
			Nip nip = (Nip)value;
			boolean result = validaNip(nip, texto);
			setMessage(texto.toString());
			return result;
		}
		//No es un nip, no lo valida
		return true;
	}
	
	public static boolean validaNip (Nip nip, StringBuilder texto) {
		if(nip.tipo.isEmpty() && nip.valor.isEmpty()) //Tipo y valor vacios, no valida
			return true; 
		
		if(nip.tipo.isEmpty()){ //Falta tipo
			texto.append("validation.nip.notipo");
			return false;
		}
		
		if(nip.valor.isEmpty()){ //Falta valor
			texto.append("validation.nip.novalor");
			return false;
		}
		
		if(nip.tipo.equals("nif")){
			// Comprueba la longitud
			if (nip.valor.trim().length() != 9) {
				texto.append("validation.nip.nif.format");
				return false;
			}
			
			//Comprueba el formato
			Matcher matcher = NIF_PATTERN.matcher(nip.valor.toUpperCase());
			if(!matcher.find()){
				texto.append("validation.nip.nif.format");
				return false;
			}
			
			//Comprueba la letra
			if(!checkNifNieLetter(nip.valor.toUpperCase())){
				texto.append("validation.nip.nif.letter");
				return false;
			}
			
			//Nif correcto
			return true;
		}else if(nip.tipo.equals("nie")){
			// Comprueba la longitud
			if (nip.valor.trim().length() != 9) {
				texto.append("validation.nip.nif.format");
				return false;
			}
			
			//Comprueba el formato
			Matcher matcher = NIE_PATTERN.matcher(nip.valor.toUpperCase());
			if(!matcher.find()){
				texto.append("validation.nip.nie.format");
				return false;
			}
			
			//Comprueba la letra
			char charInicial = ' ';
			char firstLetter = nip.valor.toUpperCase().charAt(0);
			if (firstLetter == 'X') 
				charInicial = '0';
			else if (firstLetter == 'Y')
				charInicial = '1';
			else if (firstLetter == 'Z')
				charInicial = '2';
			String numero = charInicial + nip.valor.substring(1, nip.valor.length());
			if(!checkNifNieLetter(numero)){
				texto.append("validation.nip.nie.letter");
				return false;
			}
			
			//NIE Correcto
			return true;
		}else if(nip.tipo.equals("pasaporte")){
			//El pasaporte no se comprueba
			
		}else{
			texto.append("validation.nip.tipo");
			return false;
		}
		return true;
	}

	private static boolean checkNifNieLetter(String numero){
		int digitosNif = Integer.parseInt(numero.substring(0,8));
		int letraEsperada = NIF_NIE_ASOCIATION.charAt(digitosNif % 23); 
		int letraActual = numero.charAt(8);
		return (letraEsperada ==  letraActual);
	}
}
