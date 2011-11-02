package templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher
import java.util.regex.Pattern

import generator.utils.*;
import generator.utils.HashStack.HashStackName;
import es.fap.simpleled.led.*;

import generator.utils.CampoUtils;

public class GTabla {

	def Tabla tabla;
	
	def contenedor;
	def tipo;
	CampoUtils campo;
	
	public static String generate(Tabla tabla){
		GTabla g = new GTabla();
		g.tabla = tabla;
		g.campo = CampoUtils.create(tabla.campo);
		
		g.contenedor = HashStack.top(HashStackName.GPAGINA);
		g.tipo = "pagina";
		if (g.contenedor == null){
			g.contenedor = HashStack.top(HashStackName.GPOPUP);
			g.tipo = "popup";
		}
		
		HashStack.push(HashStackName.CONTROLLER, g);
		HashStack.push(HashStackName.ROUTES, g);
		return g.view();
		
		
	}

	private String id(){
		return tabla.name ?: campo.str.replace(".", "_");
	}
	
	public String view(){
		String controllerName = contenedor.controllerFullName();
		
		TagParameters params = new TagParameters();
		params.putStr 'id', id()

		EntidadUtils entidad = EntidadUtils.create(campo.entidad);
		
		if(campo.getCampo().getAtributos() != null)
        	params.put 'url', "@${controllerName}.${controllerMethodName()}(${entidad.id})"
		else
			params.put 'url', "@${controllerName}.${controllerMethodName()}()"

        if (tabla.titulo){
			params.putStr 'titulo', tabla.titulo
		}
		params.putStr 'campo', campo.str

		if (tabla.alto){
			params.putStr 'alto', tabla.alto
		}

				
		if (tabla.popup != null){
			params.putStr 'popup', tabla.popup.name;
			if (tabla.popup.permiso)
				params.putStr 'permisoPopup', tabla.popup.permiso.name;
		}
		
		if (tabla.popupCrear != null){
			params.putStr 'popupCrear', tabla.popupCrear.name;
			if (tabla.popupCrear.permiso)
				params.putStr 'permisoPopupCrear', tabla.popupCrear.permiso.name;
		}
		if (tabla.popupBorrar != null){
			params.putStr 'popupBorrar', tabla.popupBorrar.name;
			if (tabla.popupBorrar.permiso)
				params.putStr 'permisoPopupBorrar', tabla.popupBorrar.permiso.name;
		}
		if (tabla.popupModificar != null){
			params.putStr 'popupModificar', tabla.popupModificar.name;
			if (tabla.popupModificar.permiso)
				params.putStr 'permisoPopupModificar', tabla.popupModificar.permiso.name;
		}
		if (tabla.popupVer != null){
			params.putStr 'popupVer', tabla.popupVer.name;
			if (tabla.popupVer.permiso)
				params.putStr 'permisoPopupVer', tabla.popupVer.permiso.name;
		}

		// Si el enlace es a una página, no a un popUp
		if (tabla.pagina != null) {
			params.put 'pagina', "\"@{${ParameterUtils.parameter(tabla.pagina.name)}Controller.index()}\""
		}
		else if (tabla.paginaProperty != null) {
			params.put 'pagina', "\"@{${ParameterUtils.parameter(tabla.paginaProperty)}Controller.index()}\""
		}
		
		if (tabla.recargarPagina)
			params.put("recargarPagina", true)
			
		if (tabla.columnasAutomaticas)
			tabla.columnas.addAll(ColumnasUtils.columnas(campo.campo));

		
		params.putStr('idProperty', idProperty(tabla))
		
		params.putStr 'tipo', tipo;
		
		StringBuffer columnasView = new StringBuffer();
		
			
		for(Columna c : tabla.columnas){
			columnasView.append (columnaView(c));
		}

		String view = """
#{fap.tabla ${params.lista()}}
	${columnasView}
#{/fap.tabla}
"""
		return view;
	}
	
	
	private String columnaView(Columna c){
		List<CampoUtils> campos = GTabla.camposDeColumna(c);
		c.titulo = c.titulo ?: campos.collect{it.str}.join(', ');
		c.ancho  = c.ancho ?: "200";
				
		TagParameters params = new TagParameters();
		params.putStr "cabecera", c.titulo;
		params.put "ancho", c.ancho
		if(c.campo != null){
			params.putStr("campo", CampoUtils.create(c.campo).sinEntidad());
		}
		else if(c.funcion != null){
			String str = c.funcion;
			params.putStr("funcion", funcionSinEntidades(str));	
		}
		
		if (c.permiso != null)
			params.putStr('permiso', c.permiso.name)	

		if(c.isExpandir())
			params.put("expandir", "true")	
		
		return """
	#{fap.columna ${params.lista()} /}
		"""
	}
	
	public static String funcionSinEntidades(String funcion){
		Pattern funcionSinEntidadPattern = Pattern.compile('\\$\\{(.*?)\\}');
		Matcher matcher = funcionSinEntidadPattern.matcher(funcion);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String replacement = '${' + CampoUtils.create(matcher.group(1).trim()).sinEntidad() + '}';
			if (replacement != null) {
				matcher.appendReplacement(buffer, "");
				buffer.append(replacement);
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}
	
	/**
	 * Calcula los campos que se muestran en la columna
	 * En el caso de que se haya especificado el atributo campo : se muestra un único campo
	 * Si se especifico una función. Se analiza la función buscando los campos que aparecen en ella
	 * @return
	 */
	public static List<CampoUtils> camposDeColumna(Columna c){
		List<CampoUtils> campos = new ArrayList<CampoUtils>();
		if(c.campo != null){
			campos.add(CampoUtils.create(c.campo));
		}
		else if(c.funcion != null){
			Pattern funcionSinEntidadPattern = Pattern.compile('\\$\\{(.*?)\\}');
			Matcher matcher = funcionSinEntidadPattern.matcher(c.funcion);
			StringBuffer buffer = new StringBuffer();
			while (matcher.find()) {
				campos.add(CampoUtils.create(matcher.group(1).trim()));
			}
		}
		return campos;
	}
	
	/**
	 * Convierte la funcion de la columna a una función de javascript
	 * @param c
	 * @return
	 */
	public static String renderer(Columna c){
		if(c.funcion ==  null) return null;
		def f = c.funcion
		return  "return '" + (c.funcion.replaceAll(/\$\{(.*?)\}/, '\' + record[\'$1\'] + \'')) + "';"
	}
	
	public static List<CampoUtils> uniqueCamposTabla(Tabla tabla){
		List<CampoUtils> campos = new ArrayList<CampoUtils>();
		for(Columna c : tabla.columnas){
			campos.addAll(camposDeColumna(c));
		}
		//Añade el ID de la entidad
		campos.add(CampoUtils.create(campos[0].entidad.name + ".id"));
		return campos.unique();
	}
	
	public static String idProperty(Tabla tabla){
		List<CampoUtils> campos = uniqueCamposTabla(tabla);
		return EntidadUtils.create(campos[0].entidad).variable + "_id";
	}
	
	public String controller(){
		List<CampoUtils> campos = uniqueCamposTabla(tabla);
				
		// Si tiene permiso definido la tabla
		String renderCode = "";
		String codeConPermiso = "";

		//Clase de la entidad que contiene la lista
		
		EntidadUtils entidadHija = EntidadUtils.create(campo.getUltimaEntidad());
		EntidadUtils entidadRaiz = EntidadUtils.create(campo.getEntidad());
		
		//La consulta depende de si se listan todas las entidades de una clase, o se accede a un campo
		String query = null;
		String param = null;
		if(campo.getCampo().getAtributos() == null){ //Lista todas las entidades de ese tipo
			query = """ "select ${entidadRaiz.variable} from ${entidadRaiz.clase} ${entidadRaiz.variable}" """
			param = "";
		}else{ //Acceso a los campos de una entidad
			query = """ "select ${entidadHija.variable} from ${entidadRaiz.clase} ${entidadRaiz.variable} join ${campo.firstLower()} ${entidadHija.variable} where ${entidadRaiz.variable}.id=?", ${entidadRaiz.id} """
			param = "Long ${entidadRaiz.id}";
		}
		
		String rowsStr = campos.collect { '"' + it.sinEntidad() + '"'  }.join(", ");

		return """
	public static void ${controllerMethodName()}(${param}){
		java.util.List<${entidadHija.clase}> rows = ${entidadHija.clase}.find(${query}).fetch();
		${getCodePermiso(entidadHija)}
			
		tables.TableRenderResponse<${entidadHija.clase}> response = new tables.TableRenderResponse<${entidadHija.clase}>(rowsFiltered);
		renderJSON(response.toJSON($rowsStr));
	}
	"""
	}
	
	/**
	 * Devuelve el codigo para la consulta de la tabla con permisos, dada la entidad
	 * sobre la que se busca en la tabla
	 * @param entidad Campo de la tabla
	 * @param permiso Nombre del permiso que se va a consultar en esa tabla
	 * @param camposStr String con los campos que debemos devolver
	 * @return
	 */
	private String getCodePermiso(EntidadUtils entidad) {
		if(tabla.permiso == null){
			return """
		List<${entidad.clase}> rowsFiltered = rows; //Tabla sin permisos, no filtra""";
		}
		String idsString = "";
		if(campo.getCampo().getAtributos() != null){ // El método de la tabla recibe un parámetro id"Entidad"
			idsString = """ids.put("${entidad.id}", ${entidad.id});"""
		}
		return """
		Map<String, Long> ids = new HashMap<String, Long>();
		${idsString}
		List<${entidad.clase}> rowsFiltered = new ArrayList<${entidad.clase}>();
		for(${entidad.clase} ${entidad.variable}: rows){
			Map<String, Object> vars = new HashMap<String, Object>();
			vars.put("${entidad.variable}", ${entidad.variable});
			if (${PermisosUtils.className()}${tabla.permiso.name}("read", ids, vars)) {
				rowsFiltered.add(${entidad.variable});
			}
		}
		"""
	}
	
	private controllerMethodName(){
		return "tabla" + id();
	}
	
	public String generateRoutes(){
		String url = contenedor.url() + "/" + id();
		String action = contenedor.controllerFullName() + "." + controllerMethodName();
		return Route.to("GET", url, action);
	}
}

