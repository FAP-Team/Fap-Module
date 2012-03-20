package simpletest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import play.mvc.Router;
import play.mvc.Router.ActionDefinition;
import play.test.FunctionalTest;
import play.test.UnitTest;

public class UrlTest extends FunctionalTest {

    @Test
    public void aVeryImportantThingToTest() {
    	Map<String, Object> params = new HashMap<String, Object>();
		params.put("k", "pepe");
		play.Logger.info("reverse: "+Router.reverse("fap.DescargasAedController.descargar", params).toString());
		
		Router.ActionDefinition ad = Router.reverse("fap.DescargasAedController.descargar", params);
		play.Logger.info("reverse: "+ad.url);
		ad.absolute();
		play.Logger.info("reverse: "+ad.url);
		
    }

}
