package validation;

import play.data.validation.Error;
import play.i18n.Messages;

public class CustomError extends Error {
	
    public CustomError(String key, String message, String[] variables) {
		super(key, message, variables);
	}

	String message;
    String key;
    String[] variables;
    
    /**
     * @return The translated message
     */
    public String message() {
        return message(key);
    }
    
    /**
     * @return The field name
     */
    public String getKey() {
        return key;
    }
    
    /**
     * @param key Alternate field name (default to java variable name)
     * @return The translated message
     */
    public String message(String key) {
        key = Messages.get(key);
        Object[] args = new Object[variables.length + 1];
        System.arraycopy(variables, 0, args, 1, variables.length);
        args[0] = key;
        return Messages.get(message, args);
    }

    @Override
    public String toString() {
        return message();
    }
}
