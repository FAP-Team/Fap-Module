package messages;

import java.util.*;

import play.cache.Cache;
import play.data.validation.Error;
import play.data.validation.Validation;
import play.mvc.Scope.Flash;
import play.mvc.Scope.Params;
import play.mvc.Scope.Session;

public class Messages {

    static ThreadLocal<Messages> current = new ThreadLocal<Messages>();
	
    public enum MessageType{ERROR,INFO, OK, WARNING, FATAL}
	
	private static MessageType[] MessageTypeValues = MessageType.values();

	private HashMap<MessageType, List<String>> messages = new HashMap<MessageType, List<String>>();
    public boolean keep = false;

    protected Messages() {
    }

    private static Messages current() {
		Messages msn = current.get();
		if(msn == null){
			msn = new Messages();
			current.set(msn);
		}
		return msn;
    }

    public static String allMessages(){
        return current().messages.values().toString() + Validation.current().errorsMap();
    }
    
    public static boolean getErrorMessages (String field){
    	if (Validation.current().error(field) != null)
    		return true;
    	return false;
    }
    
    public static List<String> messages(MessageType key) {
        return current().messages.get(key);
    }

    private static void add(MessageType key, String message) {
   		if (current().messages.get(key) == null)
    		current().messages.put(key, new ArrayList<String>());
    	current().messages.get(key).add(message);
   	}

    private static boolean hasMessages(MessageType key) {
    	if (current().messages.containsKey(key))
    		return current().messages(key).size() > 0;
    	return false;
    }
    
    public static void keep() {
		if (Validation.current().hasErrors()) {
			
			//Params.current().flash();
			
			// Los parametros no se flashean porque en paginas grandes pueden
			// superar el limite de 4k de la cookie. Se guardan en cache
			Cache.set(Session.current().getId(), Params.current().all(),"5mn");			
			Validation.current().keep();
		}
		current().keep = true;
    }
    
    /**
     * Mantiene los parámetros flash para la siguiente petición
     * Los parametros no se almacen en Flash porque en paginas grandes pueden
     * superar el limite de 4k de la cookie. Se guardan en cache
     */
    public static void setFlash(String id, Object o){	
    	String flashName = getFlashName(id);
    	Cache.set(flashName, o, "5mn");
    	
    	//Alamacena en cache los parámetro guardados
    	String cached = Cache.get(getCachedNamesKey(), String.class);
    	if(cached == null){
    		cached = id;
    	}else{
    		cached += "," + id;
    	}
    	Cache.set(getCachedNamesKey(), cached);
    }
    
    public static boolean containsFlash(String id){
    	String cached = Cache.get(getCachedNamesKey(), String.class);
    	if(cached != null){
    		for(String key : cached.split(",")){
    			if(id.equals(key))
    				return true;
    		}
    	}
    	return false;
    }
    
    
    public static String getCachedNamesKey(){
    	return Session.current().getId() + "cachedParams";
    }
    
    public static Object getFlash(String id){
    	Object o = Cache.get(getFlashName(id));
    	return o;
    }
    
    private static String getFlashName(String id){
    	String sessionId = Session.current().getId();
    	return sessionId + "-cacheParam-" + id;
    }
    
    public static void deleteFlash(){
    	String cached =  Cache.get(getCachedNamesKey(), String.class);
    	if(cached != null){
	    	for(String key : cached.split(",")){
	    		Cache.delete(key);
	    	}
    	}
    	Cache.delete(getCachedNamesKey());
    }
    
    public static Object flashOrValue(String name, Object value){
	 	if(containsFlash(name)){
			return getFlash(name);
		}else{
			return value;
		}
    }
    
    
    public static boolean isKeep() {
    	return current().keep;
    }

    public static void toFlash(){
    	for (MessageType type: Messages.current().messages.keySet()){
    		int i = 0;
    		List<String> msgs = Messages.current().messages.get(type);
    		while (i < msgs.size()){
        		Flash.current().put(type.toString() + i, msgs.get(i));
        		i++;
    		}
    	}
    }
    
    public static void fromFlash(){
    	for (MessageType type: MessageTypeValues){
    		int i = 0;
    		while (Flash.current().get(type.toString() + i) != null){
    			Messages.current().add(type, Flash.current().get(type.toString() + i));
    			i++;
    		}
    	}
    }
    
    public static boolean hasMessages() {
    	boolean hasmsg = false;
    	for (MessageType key : current().messages.keySet()){
    		if (current().hasMessages(key))
    			hasmsg = true;
    	}
    	return (hasmsg) || (Validation.current().hasErrors());
    }

    public static boolean hasErrors(){
    	return current().hasMessages(MessageType.ERROR) || current().hasMessages(MessageType.FATAL) || Validation.current().hasErrors();
    }
    
    public static void clear() {
    	current().messages.clear();
    	current().keep = false;
    }

    public static void ok(String msg) {
    	current().add(MessageType.OK,msg);
    }
    
    public static void info(String msg) {
    	current().add(MessageType.INFO,msg);
    }

    public static void error(String msg) {
    	current().add(MessageType.ERROR,msg);
    }

    public static void warning(String msg) {
    	current().add(MessageType.WARNING,msg);
    }

    public static void fatal(String msg) {
    	current().add(MessageType.FATAL,msg);
    }
}
