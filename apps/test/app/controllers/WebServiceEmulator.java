package controllers;

import java.util.ArrayList;
import java.util.List;

import play.mvc.Controller;
import ws.WSEmulatorResult;

public class WebServiceEmulator extends Controller {

	public static void json(){
		WSEmulatorResult result = WSEmulatorResult.test();
		renderJSON(result);
	}
	
	public static void xml(){
		WSEmulatorResult result = WSEmulatorResult.test();
		renderXml(result);
	}
	

	
	

	
}
