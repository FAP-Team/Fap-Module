package tags;

import groovy.lang.Binding;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.InvokerHelper;

import play.Play;
import play.i18n.Lang;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Router;
import play.mvc.Router.ActionDefinition;
import play.mvc.Router.Route;
import play.templates.BaseTemplate;
import play.templates.GroovyTemplate;
import play.templates.GroovyTemplate.ExecutableTemplate;
import play.templates.GroovyTemplateCompiler;
import play.templates.TemplateCompiler;
import play.templates.TemplateLoader;
import properties.FapProperties;

public class ParameterUtils {
															
	static final String Action = "@";
	
	public static ActionDefinition pageParameter(String param) {
		if (param != null) {
			ActionDefinition pagina = null;
			GroovyTemplate template = (GroovyTemplate) TemplateLoader.loadString(param);
			template.compile();			
			if (param.startsWith(Action)) {
				Binding binding = new Binding();
				binding.setVariable("play", new Play());
			    binding.setVariable("messages", new Messages());
			    binding.setVariable("lang", Lang.get());
			    StringWriter writer = new StringWriter();
			    binding.setProperty("out", new PrintWriter(writer));				    
				ExecutableTemplate t = (ExecutableTemplate) InvokerHelper.createScript(template.compiledTemplate, binding);
		        t.template = template;
		        t.run();
		        pagina = new ActionDefinition();
				pagina.url = writer.toString();
			}
			return pagina;
		}
		return null;
	}	
}
