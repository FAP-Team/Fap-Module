package simpletest;

import org.junit.*;

import play.test.*;
import play.mvc.*;
import play.mvc.Http.*;

import models.TableKeyValue;


public class ApplicationTest extends FunctionalTest {

    @Test
    public void testThatIndexPageWorks() {
    	play.Logger.info("Borrando DDBB");
    	Fixtures.deleteDatabase();
    	Fixtures.loadModels("listas/initial-data/agentes.yml");
    	
    	TableKeyValue.loadFromFiles();
    	
    	play.Logger.info("Se ha borrado la DDBB");
    	
//        Response response = GET("/login");
//        assertIsOk(response);
//        assertContentType("text/html", response);
//        assertCharset(play.Play.defaultWebEncoding, response);
    }
    
}