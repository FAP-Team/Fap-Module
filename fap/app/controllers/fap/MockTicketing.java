package controllers.fap;

import play.Play;
import play.mvc.Controller;
import services.filesystem.FilesystemMockResult;

public class MockTicketing extends Controller {

	public static void index(String asunto, String ticketing) throws Exception {
		if (Play.mode.isDev()) {
			FilesystemMockResult resultObject = new FilesystemMockResult("NIF", "11111111H", "http://urimock");
			renderJSON(resultObject);
		}
		forbidden();
    }
	
}
