package controllers.fap;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import models.Agente;
import models.Participacion;

import org.apache.log4j.Logger;
import org.hibernate.Session;


import play.Play;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.exceptions.PlayException;
import play.exceptions.TemplateNotFoundException;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Scope;
import play.mvc.With;
import play.mvc.results.RenderTemplate;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.utils.Java;
import utils.BinaryResponse;

@With({PropertiesFap.class, SecureController.class, AgenteController.class})
public class GenericController extends Controller {

	@Before
	protected static void packageParams() throws Exception {
		//Añade a la pila todos los parámetros que empiezan por id
		//Se utiliza en la propagacion de identificadores
		tags.TagMapStack.clear();
		Map<String, String> allSimple = params.allSimple();
		Map<String, Long> idParams = new HashMap<String, Long>();
		for(Map.Entry<String, String> entry : allSimple.entrySet()){
			if(entry.getKey().startsWith("id")){
				try {
					idParams.put(entry.getKey(), Long.parseLong(entry.getValue()));
				}catch(Exception e){
					//El parámetro no era un long
				}
			}
		}
		tags.TagMapStack.push("idParams", idParams);
	}
	
	protected static void renderBinary(BinaryResponse response){
		try {
			renderBinary(response.contenido.getInputStream(), response.nombre);
		} catch (IOException e) {
			play.Logger.error(e, "Error en renderBinary, no puedo obtener el inputStream");
			error();
		}
	}
	
}
