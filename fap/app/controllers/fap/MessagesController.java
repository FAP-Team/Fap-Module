package controllers.fap;

import java.util.Map;

import messages.Messages;
import play.cache.Cache;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;

public class MessagesController extends Controller {

	@After
    static void flashMessages(){
    	if (Messages.isKeep()){
    		Messages.toFlash();
    	}
    }
	
    @Before
    static void retrieveMessages(){
    	Messages.fromFlash();
    }
    
    @Finally
    static void clearMessages(){
    	Messages.clear();
    }
    
    @Before
	static void flashedParamsFromCache() {
		Map<String, String[]> par = (Map<String, String[]>) Cache.get(session.current().getId());
		if (par == null){
			return;
		}
		for (String key : par.keySet()) {
			if (par.get(key).length > 1) {
				StringBuilder sb = new StringBuilder();
				boolean coma = false;
				for (String d : par.get(key)) {
					if (coma) {
						sb.append(",");
					}
					sb.append(d);
					coma = true;
				}
				flash.now(key, sb.toString());
			} else {
				flash.now(key, par.get(key)[0]);
			}
		}
		Cache.delete(session.getId());
	}

}
