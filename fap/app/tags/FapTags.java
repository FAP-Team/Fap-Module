package tags;

import play.templates.BaseTemplate;
import play.templates.FastTags;
import play.templates.GroovyTemplate;
import play.templates.JavaExtensions;
import play.templates.TagContext;
import play.templates.Template;
import play.templates.TemplateLoader;
import groovy.lang.Closure;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.google.common.base.Joiner;

import config.InjectorConfig;
import controllers.fap.SecureController;

import models.Solicitante;
import models.SolicitudGenerica;
import models.TableKeyValue;





import play.Logger;
import play.data.validation.*;
import play.data.validation.Error;
import play.db.jpa.Model;
import play.exceptions.TagInternalException;
import play.exceptions.TemplateExecutionException;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.libs.I18N;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Router;
import play.mvc.Router.ActionDefinition;
import play.mvc.Scope.Flash;
import play.templates.FastTags;
import static play.templates.JavaExtensions.*;
import play.templates.GroovyTemplate.ExecutableTemplate;
import properties.FapProperties;
import security.Secure;
import utils.RoutesUtils;
import validation.Moneda;
import validation.ValueFromTable;

@FastTags.Namespace("fap")
public class FapTags extends FastTags {

	
	public static void _agruparCampos(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		TagMapStack.push("agruparCampos", true);
		out.println("<div class=\"agrupacion\">");
		body.call();
		out.println("</div>");
		TagMapStack.pop("agruparCampos");
	}
	
	public static void _field(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Map<String,Object> field = new HashMap<String,Object>();
        String _arg = args.get("arg").toString();
        Object obj = args.get("obj");
               
        field.put("name", _arg);
        field.put("id", _arg.replace('.','_'));
        field.put("flash", Flash.current().get(_arg));
        
        
        //Muestra todos los errores
        List<Error> errors = Validation.errors(_arg);
        String error = null;
        
        boolean hasErrors = errors.size() > 0;
        field.put("hasErrors", hasErrors);
        if(hasErrors){
        	error = "";
        	Iterator<Error> iterator = errors.iterator();
        	while(iterator.hasNext()){
        		error += iterator.next().message();
        		if(iterator.hasNext())
        			error += ", ";
        	}
        }
        
        field.put("error", error);
        field.put("errorClass", field.get("error") != null ? "hasError" : "");
        
        Object value = ReflectionUtils.getValueRecursively(obj, _arg);
        field.put("value", value);
        
        // El valor está en una llamada a un método
        if (obj == null) {
        	field.put("flashorvalue", value);
            body.setProperty("field", field);
            body.call();
            return;
        }
        
        //Comprueba las anotaciones del campo
        Field f = ReflectionUtils.getFieldRecursively(obj.getClass(), _arg);
        if(f != null){
	        //Requerido
	        Required required = f.getAnnotation(Required.class);
	        boolean requerido = required != null;
	        field.put("required", requerido);
	        
	        // Tipos de moneda con mensaje en property
	        Moneda monedaM = f.getAnnotation(Moneda.class);
	        boolean moneda = monedaM != null;
	        if(moneda && hasErrors){
	        	error = "";
	        	Iterator<Error> iterator = errors.iterator();
	        	while(iterator.hasNext()){
	        		String nextMessage = iterator.next().message();
	        		if (nextMessage.equals("Valor incorrecto")) {
	        			error += nextMessage + "." + FapProperties.get("fap.mensaje.error.moneda");
	        		} else {
	        			error += nextMessage;
	        		}
	        		if(iterator.hasNext())
	        			error += ", ";
	        	}
	            field.put("error", error);
	        }
	        
	        //Value of table
	        ValueFromTable valueFromTable = f.getAnnotation(ValueFromTable.class);
	        if(valueFromTable != null){
	        	field.put("table", valueFromTable.value());
	        }else{
	        	field.put("table", null);
	        }
	        
	        field.put("class", f.getType());
	        field.put("isCollection", Collection.class.isAssignableFrom(f.getType()));
	        
	        if(f.getAnnotation(ManyToMany.class) != null){
	        	field.put("reference", true);
            	field.put("toMany", true);
	        	field.put("referenceClass", ReflectionUtils.getListClass(f));
	        	
	        	List<Model> listModels = new ArrayList<Model>();
	        	String conId = _arg.replace('.', '_');
	        	if (Flash.current().get(conId) != null) { 
	        		for (String idString : Flash.current().get(conId).split(",")) {
	        			Object myValue = Long.valueOf(idString);
		            	Field myF = ReflectionUtils.getFieldByName(_arg);
	        			Class modelClass = ReflectionUtils.getClassFromGenericField(myF);
	        			Model model = null;
	        			try {
	        				model = (Model) modelClass.getMethod("findById", Object.class).invoke(null, myValue);
	        				listModels.add(model);
	        			} catch (Exception e) {
	        				// TODO Auto-generated catch block
	        				e.printStackTrace();
	        			}
	        		}
	        		field.put("flash", listModels);
	        	}
	        }else if(f.getAnnotation(ManyToOne.class) != null){
            	field.put("reference", true);
	        	field.put("referenceClass", f.getType());

	            String conId = _arg.replace('.', '_');
	            if ((Flash.current().get(conId) != null) && (!Flash.current().get(conId).trim().equals(""))) {
	            	Object myValue = Long.valueOf(Flash.current().get(conId));//ReflectionUtils.getValueRecursively(obj, conId);	            
	            	Field myF = ReflectionUtils.getFieldByName(_arg);
	            	String nameClass = ((String)myF.toString()).split(" ")[1].split("\\.")[1];
	            	Class<Model> modelClass = (Class<Model>) ReflectionUtils.getClassByName(nameClass);
	            	Model model = null;
	            	try {
	            		model = (Model) modelClass.getMethod("findById", Object.class).invoke(null, myValue);
	            		field.put("flash", model);
	            	} catch (Exception e) {
	            		// TODO Auto-generated catch block
	            		e.printStackTrace();
	            	}
	            }
	        }
        }

        //Value Or Flash
        Object flashOrValue = field.get("flash");
        // Primero se comento el Validation.error, ya que debia entrar por aquí cuando es un campo no Editable (no tiene Flash) y hay errores de validacion.
        // Despues a la pregunta ¿Fallaría en otro caso?: Tras un tiempo nos dimos cuenta que Sí. En el caso de un combo ManyToX, si se setea a blanco y hay un fallo de validacion, recupera de BBDD y no deja el blanco
        // Posteriormente se solucionó volviendo a descomentar el Validation.error, y añadiendo en el texto.html (y combo.html posteriormente), la condición de que si el parametro 'disabled' es true, pues el value, lo coge directamente de BBDD (o sea field.value).
        // Tras hacer todo esto, la pregunta sigue en el aire con la nueva forma de arreglarlo: ¿Fallaría esto último en otro caso?
        if ((flashOrValue == null) && (Validation.errors().size() == 0)) { 
        	flashOrValue = field.get("value");
        }
        
        field.put("flashorvalue", flashOrValue);
        String copiar = Flash.current().get(field.get("id").toString()+"copy");
        field.put("copy", copiar);
        body.setProperty("field", field);
        body.call();
    }
	
	public static void _valueFromTableTables(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        String table = args.get("arg").toString();
        //String value = TableKeyValue.getValue(table, clave);
    	//body.setProperty("valor", value);
	}
	
	public static void _valueFromTable(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        String campo = args.get("arg").toString();
        String[] pieces = campo.split("\\.");
        Object obj = body.getProperty(pieces[0]);
        
        String clave = ReflectionUtils.getValueRecursively(obj, campo).toString();
        Field f = ReflectionUtils.getFieldRecursively(obj.getClass(), campo);
        ValueFromTable valueFromTable = f.getAnnotation(ValueFromTable.class);

        body.setProperty("clave", clave);
        if(valueFromTable != null){
        	String table = valueFromTable.value();
        	body.setProperty("tabla", table);
        	String value = TableKeyValue.getValue(table, clave);
        	body.setProperty("valor", value);
        }
        body.call();
	}
	
	
	public static void _fieldList(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
        String campo = args.get("arg").toString();
        Field field = ReflectionUtils.getFieldByName(campo);
        
        
        Class genericClass = ReflectionUtils.getClassFromGenericField(field);
        
        List<String> genericFieldsName = ReflectionUtils.getFieldsNamesForClass(genericClass);
        
        body.setProperty("fieldsNames", genericFieldsName);
        body.call();
	}
	
	
	public static void _tableOfTables(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		String table = args.get("arg").toString();
		List<TableKeyValue> entries = TableKeyValue.findByTable(table);
		body.setProperty("entries", entries);
		body.call();
	}

	public static void _tableOfTablesAsJSMap(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		String table = args.get("arg").toString();
		List<TableKeyValue> entries = TableKeyValue.findByTable(table);
		
		Iterator<TableKeyValue> iterator = entries.iterator();
		String js = "{";
		
		while(iterator.hasNext()){
			TableKeyValue next = iterator.next();
			js += "'" + next.key + "':'" + next.value + "'";
			if(iterator.hasNext()){
				js += ", ";
			}
		}
		
		js += "}";
		
		out.write(js);
	}
	
	public static void _toJSArray(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		List<String> lista = (List<String>)args.get("arg");
		Iterator<String> iterator = lista.iterator();
		String js = "[";
		
		while(iterator.hasNext()){
			js += "'" + iterator.next() + "'";
			if(iterator.hasNext()){
				js += ", ";
			}
		}
		
		js += "]";
		
		out.write(js);
	}
	
	/**
	 *	Convierte una Lista a una Map de Javascript, con clave y sin valores
	 *  Ejemplo
R
	 */
	public static void _toJSMapNoValue(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		List<String> lista = (List<String>)args.get("arg");
		Iterator<String> iterator = lista.iterator();
		String js = "{";
		
		while(iterator.hasNext()){
			js += "'" + iterator.next() + "':''";
			if(iterator.hasNext()){
				js += ", ";
			}
		}
		
		js += "}";
		
		out.write(js);
	}
	
	public static void _toJSMap(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Map<String,Object> map = (Map<String,Object>)args.get("arg");
		Iterator<String> iterator = map.keySet().iterator();
		String js = "{";
		while(iterator.hasNext()){
			String key = iterator.next();
			js += "'" + key + "':'" + map.get(key) +"'";
			if(iterator.hasNext()){
				js += ", ";
			}
		}
		
		js += "}";
		
		out.write(js);
	}
	
	
	private static String campo2id(String campo){
		return campo.replaceAll("\\.", "_");
	}
	
	public static void _campo2id(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		String campo = args.get("arg").toString();
		if(campo == null)
			error(template, fromLine, "El campo no puede ser null");
		out.write(campo2id(campo));
	}
	
	private static Pattern columnaFuncionPattern = Pattern.compile("\\$\\{(.*?)\\}");

	
	
	/**
	 * Parametros:
	 *    campo
	 *    funcion
	 *    renderer
	 *    cabecera
	 *    ancho
	 *    expandir
	 */
	public static void _columna(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		if( (!TagContext.hasParentTag("fap.tabla")) && (!TagContext.hasParentTag("fap.tablaSiCombo")) ) {
			String msg = "El tag fap.columna puede aparecer únicamente dentro de un tag fap.tabla o tag.tablaSiCombo";
			throw new TemplateExecutionException(template.template, fromLine, msg, new TagInternalException(msg));
		}
		
		
		String campo = (String)args.get("campo");
		String funcion = (String)args.get("funcion");
		String funcionRaw = (String)args.get("funcionRaw");
		String renderer = (String)args.get("renderer");
		String permiso = (String)args.get("permiso");
		
		boolean hasPermiso = true;
		if (permiso != null) {
			Secure secure = InjectorConfig.getInjector().getInstance(Secure.class);
			hasPermiso = secure.checkGrafico(permiso, "visible", "editar", (Map<String, Long>)tags.TagMapStack.top("idParams"), null);
		}
		
		if (hasPermiso) {
			if(campo == null && funcion == null && funcionRaw == null){
				String msg = "Especifica un campo o función o renderer + campo";
				throw new TemplateExecutionException(template.template, fromLine, msg, new TagInternalException(msg));
			}
			else if(funcion != null && funcionRaw != null){
				String msg = "Solo se puede especificar funcion o funcionRaw, no ambas al mismo tiempo";
				throw new TemplateExecutionException(template.template, fromLine, msg, new TagInternalException(msg));
			}
			
			List<String> campos = new ArrayList<String>();
			String dataIndex = null;
			String rendererFunctionContent = null;
			if(renderer != null){
				dataIndex = campo2id(campo);
				rendererFunctionContent = renderer;
				campos.add(campo); //Campo único			
			}else if(funcion != null){
				//Función
				//Busca los campos que aparecen en la función
				Matcher m = columnaFuncionPattern.matcher(funcion);
				
				//Crea el contenido de la función renderer y almacena los campos encontrados
				StringBuffer renderFunc = new StringBuffer();
				renderFunc.append("return '");
				while(m.find()){
					String campoEncontrado = m.group(1);
					campos.add(campoEncontrado); //Lo añade a la lista de campos
					
	                String replacement = "' + record.data['" + campo2id(campoEncontrado) + "'] + '";
	                m.appendReplacement(renderFunc, "");
	                renderFunc.append(replacement);
				}
				m.appendTail(renderFunc);
				renderFunc.append("';");
				rendererFunctionContent = renderFunc.toString();
				
				
				if(campos.size() == 0){
					String msg = "Especificaste una función sin ningún campo. Los campos se especifican con ${campo}";
					throw new TemplateExecutionException(template.template, fromLine, msg, new TagInternalException(msg));				
				}
				
				//El dataIndex al primer campo, por especificar alguno
				//En la función se va a utilizar el record, no el value
				dataIndex = campo2id(campos.get(0));
				
			}else if(funcionRaw != null){
				//Función Raw
				//Busca los campos que aparecen en la función
				Matcher m = columnaFuncionPattern.matcher(funcionRaw);
				
				//Crea el contenido de la función renderer y almacena los campos encontrados
				StringBuffer renderFunc = new StringBuffer();
				renderFunc.append("return ");
				while(m.find()){
					String campoEncontrado = m.group(1);
					campos.add(campoEncontrado); //Lo añade a la lista de campos
					
	                String replacement = "record.data['" + campo2id(campoEncontrado) + "']";
	                m.appendReplacement(renderFunc, "");
	                renderFunc.append(replacement);
				}
				m.appendTail(renderFunc);
				renderFunc.append(";");
				rendererFunctionContent = renderFunc.toString();
				
				//El dataIndex al primer campo, por especificar alguno
				//En la función se va a utilizar el record, no el value
				dataIndex = campos.size() == 0 ? null : campo2id(campos.get(0));
				
			}else{
				//Campo
				dataIndex = campo2id(campo);
				campos.add(campo);
			}
				
			
			String cabecera = play.i18n.Messages.get((String)args.get("cabecera"));
			Object ancho    = args.get("ancho"); ancho = ancho != null ? ancho : 200;
			Boolean expandir = (Boolean)args.get("expandir"); expandir = expandir != null ? expandir : false;
			
			JsonParameters params = new JsonParameters();
			params.putStr("text", cabecera);
			params.putStr("dataIndex", dataIndex);
			params.put("sortable", true);
			
			String alignPosition = (String)args.get("alignPosition");
			if (alignPosition != null && !alignPosition.isEmpty()) {
				params.putStr("align", alignPosition);
			}
			
			if(expandir){
				params.put("flex", true);
			}else{
				params.put("width", ancho);
			}
			
			if(rendererFunctionContent != null)
				params.put("renderer", "function(value, meta, record){" + rendererFunctionContent + " }");
			Set<String> camposTabla = null;
			List<String> columnasTabla = null;
			if (TagContext.hasParentTag("fap.tabla")) {
				camposTabla = (Set<String>)TagContext.parent("fap.tabla").data.get("campos");
				columnasTabla = (List<String>)TagContext.parent("fap.tabla").data.get("columnas");
			}
			else {
				camposTabla = (Set<String>)TagContext.parent("fap.tablaSiCombo").data.get("campos");
				columnasTabla = (List<String>)TagContext.parent("fap.tablaSiCombo").data.get("columnas");
			}
			//Set<String> camposTabla = (Set<String>)TagContext.parent("fap.tabla").data.get("campos");
			camposTabla.addAll(campos);
			//List<String> columnasTabla = (List<String>)TagContext.parent("fap.tabla").data.get("columnas");
			columnasTabla.add(params.getMap());
		}	
	}
	
	private static void error(ExecutableTemplate template, int fromLine, String message){
		throw new TemplateExecutionException(template.template, fromLine, message, new TagInternalException(message));
	}
	
	public static void _hiddens(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Object arg = args.get("arg");
		
		if(arg == null)
			return; // Para evitar que de error cuando en un PopUp sólo hay una Tabla
			//error(template, fromLine, "Parámetro nulo");
		
		if(!(arg instanceof Map))
			error(template, fromLine, "El tag hiddens espera como parámetro un map<String, String>");
		
		Map<?,?> map = (Map)arg;
		for(Map.Entry<?, ?> e : map.entrySet()){
			out.println("<input type=\"hidden\" name=\"" + e.getKey() + "\" value=\"" + e.getValue()+ "\" />");
		}
	}
	
	public static void _idParams(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Map<String, Long> map = (Map<String, Long>)tags.TagMapStack.top("idParams");
		Iterator<String> iterator = map.keySet().iterator();
		String js = "";
		while(iterator.hasNext()){
			String key = iterator.next();
			js += "'" + key + "': " + map.get(key) +"";
			if(iterator.hasNext()){
				js += ", ";
			}
		}
		out.print(js);
	}
	
	
	/**
	 * Imprime un mapa en formato javascript
	 * @param args
	 * @param body
	 * @param out
	 * @param template
	 * @param fromLine
	 */
	public static void _printMap(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Map<String, ?> map = (Map<String, ?>)args.get("arg");
		Iterator<String> iterator = map.keySet().iterator();
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		while(iterator.hasNext()){
			String key = iterator.next();
			sb.append("" + key + ": " + map.get(key).toString() +"");
			if(iterator.hasNext()){
				sb.append(", ");
			}
		}
		sb.append("}");
		out.print(sb.toString());
	}

	
	/**
	 * Para los links del menu activos
	 */
	public static void _activeRoute(Map<?, ?> args, Closure body, PrintWriter out, GroovyTemplate.ExecutableTemplate template, int fromLine) {
	      Router.ActionDefinition actionDef = (Router.ActionDefinition) args.get("href");
	      String activeStyle = (String) args.get("activeClass");
	      String inactiveStyle  = (String) args.get("inactiveClass");

	      if (Http.Request.current().action.equals(actionDef.action) && activeStyle != null) {
	         out.print(activeStyle);
	      }
	      else if (!Http.Request.current().action.equals(actionDef.action) && inactiveStyle != null) {
	         out.print(inactiveStyle);
	      }
	   }
	
	
	private static String breadcrumbLi(String content){
		return "<li>" + content + "<li>";
	}
	
	private static String breadcrumbLi(String content, String url){
		return breadcrumbLi("<a href=\"" + url +"\">" + content + "</a>");
	}
	
	/**
	 * Para información de migas de pan
	 */
	public static void _breadcrumbs(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		List<String> crumbs = new ArrayList<String>();
		
		
		if(!RoutesUtils.isDefaultRoute()){
			String url = RoutesUtils.getDefaultRoute();
			crumbs.add(breadcrumbLi("Inicio",  url));
		}
		
		Long idSolicitud = (Long)args.get("idSolicitud");
		if(idSolicitud != null){
			String identificador = idSolicitud.toString();
			String idAed = (String)args.get("idAed");
			if(idAed != null)
				identificador = idAed;
			
			String url = RoutesUtils.getPrimeraPaginaSolicitud(idSolicitud);
			crumbs.add(breadcrumbLi("Solicitud " + identificador, url));
		}
		
		Object title = args.get("title");			
		String controllerName = Http.Request.current().controller.replace("Controller", "");
		String pageName = title != null? title.toString() : controllerName;
		crumbs.add(breadcrumbLi("<a href=\"#\">" + pageName + "</a>"));
		
		
		Joiner joiner = Joiner.on("<p class=\"barra\">/</p>").skipNulls();
		out.write(joiner.join(crumbs));
	}
	
	
	/**
	 * Recupera el valor de un elemento de la tabla de tablas
	 * @param args
	 * @param body
	 * @param out
	 * @param template
	 * @param fromLine
	 */
	public static void _tableOfTableValue(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Map<String, ?> arguments = (Map<String, ?>)args; 
		Object argTable = arguments.get("table");
		if(!(argTable instanceof String)){
			String msg = "El parámetro 'table' debe de ser un string";
			throw new TemplateExecutionException(template.template, fromLine, msg, new TagInternalException(msg));
	
		}
		Object argKey = arguments.get("key");
		if(!(argKey instanceof String)){
			String msg = "El parámetro 'key' debe de ser un string";
			throw new TemplateExecutionException(template.template, fromLine, msg, new TagInternalException(msg));
	
		}
		String table = (String) argTable;
		String key = (String) argKey;
		out.print(TableKeyValue.getValue(table, key));
	}
	
	
	
}
