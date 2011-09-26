package controllers.fap;

import java.io.*;
import java.util.Enumeration;
import java.util.Map;


import play.Logger;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.utils.Properties;
import properties.FapProperties;

public class PropertiesFap extends Controller {

	@Before
	protected static void injectProperties() throws Exception {		
		renderArgs.put("configfap", FapProperties.get());
	}
	
}
