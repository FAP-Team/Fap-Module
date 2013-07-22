package validation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import antlr.StringUtils;

import messages.Messages;
import models.CCC;
import models.Direccion;
import models.Documento;
import models.DocumentoExterno;
import models.Nip;
import models.Persona;
import models.PersonaFisica;
import models.PersonaJuridica;
import models.RepresentantePersonaJuridica;
import models.Solicitante;
import models.TableKeyValue;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import play.Logger;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.data.validation.EmailCheck;
import play.data.validation.Error;
import play.data.validation.RequiredCheck;
import play.data.validation.ValidCheck;
import play.data.validation.Validation;
import play.data.validation.Validation.ValidationResult;
import play.exceptions.UnexpectedException;
import properties.FapProperties;
import tags.ReflectionUtils;

public class CustomValidation {
    static ThreadLocal<CustomValidation> current = new ThreadLocal<CustomValidation>();
    List<String> entidadesValidadas = new ArrayList<String>();
    boolean versionError = false;

    protected CustomValidation() {
    }
    

    public static CustomValidation current() {
		CustomValidation validation = current.get();
		if(validation == null){
			validation = new CustomValidation();
			current.set(validation);
		}
		return validation;
    }

    static boolean isValidada(String key) {
    	return current().entidadesValidadas.contains(key);
    }

    public static void clearValidadas() {
    	current().entidadesValidadas.clear();
    	current().versionError = false;
    }

    static void validada(String key) {
    	current().entidadesValidadas.add(key);
    }
    
    static ValidationResult applyCheck(NipCheck check, String key, Object o) {
    	StringBuilder texto = new StringBuilder();
        try {
            ValidationResult result = new ValidationResult();
            if (!check.validaNip((Nip)o, texto)) {
            	
            	String field = key;
            	String message = texto.toString();
            	String[] variables = new String[0];
                
            	Error error = new Error(field, message, variables);
                Validation.addError(field, message, variables);
                
                result.error = error;
                result.ok = false;
            } else {
                result.ok = true;
            }
            return result;
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }
    
    static ValidationResult applyCheck(CifCheck check, String key, Object o) {
    	StringBuilder texto = new StringBuilder();
        try {
            ValidationResult result = new ValidationResult();
            if (!check.validaCif(o.toString(), texto)) {
            	
            	String field = key;
            	String message = texto.toString();
            	String[] variables = new String[0];
                
            	Error error = new Error(field, message, variables);
                Validation.addError(field, message, variables);
                
                result.error = error;
                result.ok = false;
            } else {
                result.ok = true;
            }
            return result;
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }
    
    static ValidationResult applyCheck(CCCCheck check, String key, Object o) {
    	StringBuilder texto = new StringBuilder();
        try {
            ValidationResult result = new ValidationResult();
            if (!check.validaCCC((CCC)o, texto)) {
            	
            	String field = key;
            	String message = texto.toString();
            	String[] variables = new String[0];
                
            	Error error = new Error(field, message, variables);
                Validation.addError(field, message, variables);
                
                result.error = error;
                result.ok = false;
            } else {
                result.ok = true;
            }
            return result;
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

    static ValidationResult applyCheck(AbstractAnnotationCheck<?> check, String key, Object o) {
        try {
            ValidationResult result = new ValidationResult();
            if (!check.isSatisfied(o, o, null, null)) {
            	String field = key;
            	String message = check.getClass().getDeclaredField("mes").get(null) + "";
            	String[] variables = check.getMessageVariables() == null ? new String[0] : check.getMessageVariables().values().toArray(new String[0]);
                
            	
            	Error error = new Error(field, message, variables);
                Validation.addError(field, message, variables);

                result.error = error;
                result.ok = false;
            } else {
                result.ok = true;
            }
            return result;
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }
	
    static ValidationResult applyCheck(RepresentanteCheck check, String key, Object o, Object o2) {
    	StringBuilder texto = new StringBuilder();
        try {
            ValidationResult result = new ValidationResult();
            if (!check.validaRepresentante((Solicitante)o, (List<RepresentantePersonaJuridica>)o2, texto)) {
            	
            	String field = key;
            	String message = texto.toString();
            	String[] variables = new String[0];
                
            	Error error = new Error(field, message, variables);
                Validation.addError(field, message, variables);
                
                result.error = error;
                result.ok = false;
            } else {
                result.ok = true;
            }
            return result;
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }
    
    static ValidationResult applyCheck(GRBCheck grbCheck, String key, Object o, ArrayList<String> values) {
    	ValidationResult result = new ValidationResult();
    	
    	if (!grbCheck.validaGRB(((String) o), values)) {
    		
        	String field = key;
        	String message = ((String) o);
        	String[] variables = new String[0];
            
        	Error error = new Error(field, message, variables);
            Validation.addError(field, message, variables);
            
            result.error = error;
            result.ok = false;
    		
    	} else
    		result.ok = true;
    	
    	return result;
    }
    
    public static ValidationResult required(String key, Object o) {
    	CustomRequiredCheck requiredCheck = new CustomRequiredCheck();
    	if (o instanceof PersonaFisica){
    		PersonaFisica personaFisica = (PersonaFisica)o;
    		ValidationResult result = new ValidationResult();
    		result.ok = true;
    		result.ok = applyCheck(requiredCheck, key + ".nombre", personaFisica.nombre).ok && result.ok;
    		result.ok = applyCheck(requiredCheck, key + ".primerApellido", personaFisica.primerApellido).ok && result.ok;
    		result.ok = applyCheck(requiredCheck, key + ".nip", personaFisica.nip).ok && result.ok;
    		return result;
    	}
    	else if (o instanceof PersonaJuridica){
    		PersonaJuridica personaJuridica = (PersonaJuridica)o;
    		ValidationResult result = new ValidationResult();
    		result.ok = true;
    		result.ok = applyCheck(requiredCheck, key + ".cif", personaJuridica.cif).ok && result.ok;
    		result.ok = applyCheck(requiredCheck, key + ".entidad", personaJuridica.entidad).ok && result.ok;
    		return result;
    		
    	}
    	else if (o instanceof Direccion){
    		Direccion direccion = (Direccion)o;
    		ValidationResult result = new ValidationResult();
    		result.ok = true;
    		if ((!FapProperties.getBoolean("fap.direccion.anterior.version2.1")) && (direccion.tipo != null)){ 
    			result.ok = applyCheck(requiredCheck, key + ".tipo", direccion.tipo).ok && result.ok;
	    		if (direccion.tipo.equals("canaria")){
	    			result.ok = applyCheck(requiredCheck, key + ".provincia", direccion.provincia.replace(",", "").trim()).ok && result.ok;
	    			result.ok = applyCheck(requiredCheck, key + ".isla", direccion.isla).ok && result.ok;
	    			result.ok = applyCheck(requiredCheck, key + ".municipio", direccion.municipio.replace(",", "").trim()).ok && result.ok;
	    		}
	    		if (direccion.tipo.equals("nacional")){
	    			result.ok = applyCheck(requiredCheck, key + ".comunidad", direccion.comunidad).ok && result.ok;
	    			result.ok = applyCheck(requiredCheck, key + ".provincia", direccion.provincia.replace(",", "").trim()).ok && result.ok;
	    			result.ok = applyCheck(requiredCheck, key + ".municipio", direccion.municipio.replace(",", "").trim()).ok && result.ok;
	    		}
	    		if (direccion.tipo.equals("internacional")){
	    			result.ok = applyCheck(requiredCheck, key + ".pais", direccion.pais).ok && result.ok;
	    			result.ok = applyCheck(requiredCheck, key + ".provinciaInternacional", direccion.provinciaInternacional).ok && result.ok;
	    			result.ok = applyCheck(requiredCheck, key + ".localidad", direccion.localidad).ok && result.ok;
	    		}
    		} else {
    			result.ok = applyCheck(requiredCheck, key + ".municipio", direccion.municipio.replace(",", "").trim()).ok && result.ok;
    		}
    		result.ok = applyCheck(requiredCheck, key + ".codigoPostal", direccion.codigoPostal).ok && result.ok;
    		result.ok = applyCheck(requiredCheck, key + ".calle", direccion.calle).ok && result.ok;
    		result.ok = applyCheck(requiredCheck, key + ".numero", direccion.numero).ok && result.ok;
    		return result;
    		
    	}
    	else if (o instanceof Documento){
    		Documento documento = (Documento)o;
    		ValidationResult result = new ValidationResult();
    		result.ok = true;
    		result.ok = applyCheck(requiredCheck, key + ".tipo", documento.tipo).ok && result.ok;
    		if(documento.isMultiple()){
    			result.ok = applyCheck(requiredCheck, key + ".descripcion", documento.descripcion).ok && result.ok;
    		}
    		return result;    		
    	}
    	else if (o instanceof DocumentoExterno){
    		DocumentoExterno documento = (DocumentoExterno)o;
    		ValidationResult result = new ValidationResult();
    		result.ok = true;
    		result.ok = applyCheck(requiredCheck, key + ".tipo", documento.tipo).ok && result.ok;
    		if(documento.isMultiple()){
    			result.ok = applyCheck(requiredCheck, key + ".descripcion", documento.descripcion).ok && result.ok;
    		}
    		return result;    		
    	}
    	else{
    		return applyCheck(requiredCheck, key, o);
    	}
        
    }
    
    static String getLocalName(Object o) {
        List<String> names = LocalVariablesNamesTracer.getAllLocalVariableNames(o);
        if (names.size() > 0) {
            return names.get(0);
        }
        return "";
    }
    
    // Para validar que el representante no sea el mismo que el solicitante
    public static ValidationResult valid(String key, Object o, Object o2) { // o2 tiene la lista de representantes
    	ValidationResult result = new ValidationResult();
		if (o instanceof Solicitante){ // Para validar que el representante de un solicitante no es el propio solicitante
			RepresentanteCheck checkRepresentante = new RepresentanteCheck();
			result.ok = true;
			result.ok = applyCheck(checkRepresentante, key, o, o2).ok && result.ok;
			if (!result.ok)
				return result;
		}
    	return valid(key, o);
    }
    
    public static ValidationResult valid(String key, Object o) {
    	if (!isValidada(key)) {
        	validada(key);
    		CustomValidCheck check = new CustomValidCheck();
    		check.setKey(key);
    		
			ValidationResult result = new ValidationResult();
			result.ok = true;
			result.ok = applyCheck(check, key, o).ok && result.ok;

    		if (o instanceof Persona) {
    			if(((Persona) o).isPersonaFisica())
    				result.ok = valid(key + ".fisica", ((Persona) o).fisica).ok && result.ok;
    			if(((Persona) o).isPersonaJuridica())
    				result.ok = valid(key + ".juridica", ((Persona) o).juridica).ok && result.ok;
    			return result;
    		} else if (o instanceof PersonaFisica) {
    			NipCheck nCheck = new NipCheck();
    			return applyCheck(nCheck, key + ".nip", ((PersonaFisica) o).nip);
    		} else if (o instanceof PersonaJuridica) {
    			CifCheck cCheck = new CifCheck();
    			return applyCheck(cCheck, key + ".cif", ((PersonaJuridica) o).cif);
    		} else if (o instanceof CCC) {
    			CCCCheck cccCheck = new CCCCheck();
    			return applyCheck(cccCheck, key, ((CCC) o));
    		}
    		return result;
    	}
		ValidationResult result = new ValidationResult();
		result.ok = true;
		return result;
    }
    
    public static ValidationResult valid(String key, Object o, ArrayList<String> values) {
    	if (o != null)
			if (!isValidada(key)) {
		    	validada(key);
				GRBCheck grbCheck = new GRBCheck();
				return applyCheck(grbCheck, key, o, values);
				
			}

		ValidationResult result = new ValidationResult();
		result.ok = true;
		return result;
    }
    
    public static ValidationResult validValueFromTable(String key, Object o) {
		if (o != null) {
			ValidationResult result = new ValidationResult();
			result.ok = true;
			
			
			Class clazz = ReflectionUtils.getClassByName(key);
			Field f = ReflectionUtils.getFieldRecursively(clazz, key);
			
			ValueFromTable annotation = f.getAnnotation(ValueFromTable.class);			
			if(annotation != null){
				List<String> valores = new ArrayList<String>();
				
				if(Set.class.isAssignableFrom(o.getClass())){
					Set set = (Set)o;
					valores.addAll(set);
				}else{
					valores.add(o.toString());
				}
				
				
				for(String s : valores){
					if ((s != null) && (!s.trim().equals("")))
						result.ok = result.ok && TableKeyValue.getValue(annotation.value(), s) != null;
				}
				
				if(!result.ok){
					String field = key;
					String message = "Elija un valor de la lista";
					String[] variables = new String[0];
					Validation.addError(field, message, variables);
				}
			}
			return result;
		}
		ValidationResult result = new ValidationResult();
		result.ok = true;
		return result;				
    }
    
    public static ValidationResult validListOfValuesFromTable(String key, Object o) {
    	
		if (o != null) {
			ValidationResult result = new ValidationResult();
			result.ok = true;
			Class clazz = ReflectionUtils.getClassByName(key);
			Field f = ReflectionUtils.getFieldRecursively(clazz, key);
			ValueFromTable annotation = f.getAnnotation(ValueFromTable.class);			
			if(annotation != null){
				Set<String> list = (Set<String>) o;
				for (String value: list){		
					if (TableKeyValue.getValue(annotation.value(), value) == null) {
						String field = key;
						String message = "Elija un valor de la lista";
						String[] variables = new String[0];
               
						Error error = new Error(field, message, variables);
						Validation.addError(field, message, variables);

						result.ok = false;
					}
				}
			}
			return result;
		}
		ValidationResult result = new ValidationResult();
		result.ok = true;
		return result;				
    }
    
    public static ValidationResult version(String string, Object versionO, Object dbVersionO) {
		String field = string + ".version";
		ValidationResult result = new ValidationResult();
		
		if ((versionO == null) || (dbVersionO == null)) {
			result.ok = true;
		} else {
			Long version = (Long)versionO;
			Long dbVersion = (Long)versionO;
			if ((version != null) && (dbVersion != null) && (!current().versionError) && (dbVersion > version)) {
			current().versionError = true;
        	String message = "optimisticLocking.modelHasChanged";
        	String[] variables = new String[0];
            
        	Error error = new Error(field, message, variables);
            Validation.addError(field, message, variables);
            result.ok = false;
			} else {
			result.ok = true;
			}
		}
		return result;
	}
	
	public static ValidationResult version(String string, Long version) {
		String field = string + ".version";
		ValidationResult result = new ValidationResult();

			result.ok = true;
		return result;
	}

	/**
	 * Comprueba que dos campos sean iguales, y si no lo son, añade un error de validación.
	 * (No se utiliza por ahora ya que se hace mediante javascript)
	 * @param string
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static ValidationResult equals(String string, Object obj1, Object obj2) {
		ValidationResult result = new ValidationResult();
		String field = string;
		result.ok = true;
		if (!obj1.equals(obj2)) {
			String message = "No coinciden los campos";
        	String[] variables = new String[0];
            
        	Error error = new Error(field, message, variables);
            Validation.addError(field, message, variables);
			result.ok = false;
		}
		return result;
	}
	
	public static ValidationResult compare(String comprobar, String copia) {
		ValidationResult result = new ValidationResult();
		if (comprobar.equals(copia))
			result.ok = true;
		else {
			String message = "No coinciden los campos";
        	String[] variables = new String[0];
            Validation.addError("solicitud.solicitante.email", message, variables);
			result.ok = false;
		}
		return result;
    }
	
	public static ValidationResult compare(String campo, String comprobar, String copia) {
		ValidationResult result = new ValidationResult();
		if (comprobar.equals(copia))
			result.ok = true;
		else {
			String message = "No coinciden los campos";
        	String[] variables = new String[0];
            Validation.addError(campo, message, variables);
			result.ok = false;
		}
		return result;
    }
	
	/**
	 * Emite un mensaje de error, al igual que el Message.error, pero permite recuperar de flash lo que habia antes no como el Messages.error que no recupera
	 * @param mensaje, Mensaje de error a emitir
	 * @param key, Nombre del campo que falla, en string (igual que obj1 pero con "")
	 * @param obj1, Campo que falla
	 * @return
	 */
	public static ValidationResult error(String mensaje, String key, Object obj1) {
		ValidationResult result = new ValidationResult();
		String field = key;
		String message = mensaje;
        String[] variables = new String[0];  
        Validation.addError(field, message, variables);
		result.ok = false;
		return result;
	}
}
