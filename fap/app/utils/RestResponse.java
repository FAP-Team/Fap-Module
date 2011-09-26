package utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.Play;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesSupport;
import play.exceptions.PlayException;
import play.exceptions.TemplateNotFoundException;
import play.mvc.Controller;
import play.templates.Template;
import play.templates.TemplateLoader;

public class RestResponse implements LocalVariablesSupport {
	public boolean success;
	public String message;
	
	public DataType dataType;
	public Object data;

	public enum DataType {
		html, json
	};
	
	public RestResponse(){}

	public RestResponse(boolean success, String message, DataType dataType, Object data) {
		super();
		this.success = success;
		this.message = message;
		this.dataType = dataType;
		this.data = data;
	}

	public static RestResponse error(String message){
		RestResponse response = new RestResponse();
		response.success = false;
		response.message = message;
		return response;
	}
    
	public static RestResponse ok(String message){
		RestResponse response = new RestResponse();
		response.success = true;
		response.message = message;
		return response;
	}
    
}
