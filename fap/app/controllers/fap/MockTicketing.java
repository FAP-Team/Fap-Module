package controllers.fap;

import play.Play;
import play.mvc.Controller;
import services.filesystem.FilesystemMockResult;

public class MockTicketing extends Controller {

	public static void index(String asunto, String ticketing) throws Exception {
		if (Play.mode.isDev()) {
			FilesystemMockResult resultObject = null;
			if ("1".equals(ticketing)) {
				resultObject = new FilesystemMockResult("NIF", "11111111H", "http://urimock");
		    } else 
		    	if ("2".equals(ticketing)) {
		    		resultObject = new FilesystemMockResult("NIF", "12345678Z", "http://urimock");
		    	}
			
			renderJSON(resultObject);
		}
		forbidden();
    }
	
}
