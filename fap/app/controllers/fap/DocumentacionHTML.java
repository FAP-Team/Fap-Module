package controllers.fap;

import play.Logger;
import play.Play;
import play.libs.IO;
import play.mvc.Controller;
import play.vfs.VirtualFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentacionHTML extends Controller {

    public static void index(String id) throws Exception {

    	if (id.equals("index")){
    		VirtualFile aplicacion = Play.getVirtualFile("/documentation/html/");
    		VirtualFile fap = aplicacion.fromRelativePath("{module:fap}/documentation/html");
    		List ficheros;
    		ficheros=aplicacion.list();
    		for (VirtualFile fichero: fap.list()){
    			ficheros.add(fichero);
    		}
    		renderTemplate("documentationHTML/Index.html", ficheros); 
    	} else{
    		String title=id;
    		VirtualFile tf = Play.getVirtualFile("/documentation/html/"+id+".html");
    		String pagina = tf.contentAsString();
    		renderTemplate("documentationHTML/template.html", pagina, title); 
    	}
    }
}              
