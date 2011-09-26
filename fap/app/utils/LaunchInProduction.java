package utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.Play;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesSupport;
import play.server.Server;

public class LaunchInProduction {

	public static void main(String[] args) throws Exception {
		System.setProperty("play.id", "prod");
		play.server.Server.main(args);
	}

}
	
