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
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

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
		
		g.contenedor = HashStack.top(HashStackName.CONTAINER);
		if (g.contenedor instanceof GPopup){
			g.tipo = "popup";
		}
		else{
			g.tipo = "pagina";
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
		
		if(campo.getCampo().getAtributos() != null && !entidad.isSingleton())
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
		
		params.putStr("idEntidad", "_${EntidadUtils.create(campo.ultimaEntidad).id}_");
		
		botonesPopup(params);
		botonesPagina(params);
		
		if (tabla.recargarPagina)
			params.put("recargarPagina", true)
			
	    List <Attribute> excludes, includes;
	
		if (tabla.seleccionable) {
			params.putStr("seleccionable", tabla.seleccionable)
			params.putStr("urlSeleccionable", "${controllerName}.${seleccionableMethodName()}")
		}

		params.putStr 'tipo', tipo;
		
		StringBuffer columnasView = new StringBuffer();
			
		
		if (tabla.columnasAutomaticas){
			List <Columna> listaAtributos;
			if (tabla.exclude != null){
			   excludes = tabla.exclude.atributos;
			   // listaAtributos: Devuelve la lista de columnas que hay que mostrar (es decir, en este caso, todas menos las excludes)
			   listaAtributos = ColumnasUtils.columnasExclude(campo.campo, excludes);
			} else if (tabla.include != null){
			   includes = tabla.include.atributos;
			   listaAtributos = ColumnasUtils.columnasInclude(campo.campo, includes);
			} else{
			   listaAtributos = ColumnasUtils.columnas(campo.campo);
			}
			List <Columna> aux = tabla.columnas;
			boolean listo=false;
			if (aux.isEmpty()){
				tabla.columnas.addAll(listaAtributos);
			}
			else{
				Columna co=null;
				// Para que en caso de que haya 'columnasAutomaticas' y 'Columna' normal, se coja la 'Columna' normal
				for(Columna lA : listaAtributos){
					for(Columna c : tabla.columnas){
						if ((CampoUtils.create(c.getCampo()).getUltimaEntidad().name.equals(CampoUtils.create(lA.getCampo()).getUltimaEntidad().name))
							&&(CampoUtils.create(c.getCampo()).getUltimoAtributo().name.equals(CampoUtils.create(lA.getCampo()).getUltimoAtributo().name))){
							aux.remove(lA);
							listo=true;
							co = c;
							break;
						}
						co = null;
					}
					if (!listo){
						if (co != null)
						   aux.remove(co);
						aux.add(lA);
					} else{
						listo = false;
					}
				}
			}
			for(Columna c : tabla.columnas){
				columnasView.append (columnaView(c));
			}
	    } else{
		    for(Columna c : tabla.columnas){
			   columnasView.append (columnaView(c));
		    }
		}
		
		if(tabla.columnas.isEmpty()){
			System.out.println("WARNING: La tabla: <"+tabla.getName()+"> no tiene ninguna columna como visible");
		}

		String view = """
#{fap.tabla ${params.lista(true)}
}
	${columnasView}
#{/fap.tabla}
"""
		return view;
	}
	
	private void botonesPopup(TagParameters params){
		if (tabla.popup != null) {
			Controller popupUtil = Controller.fromPopup(tabla.popup).initialize();
			params.put 'urlPopupLeer', popupUtil.getRouteIndex("leer", true);
			params.putStr 'popupLeer', tabla.popup.name;
			params.put 'urlPopupCrear', popupUtil.getRouteIndex("crear", true);
			params.putStr 'popupCrear', tabla.popup.name;
			params.put 'urlPopupEditar', popupUtil.getRouteIndex("editar", true);
			params.putStr 'popupEditar', tabla.popup.name;
			params.put 'urlPopupBorrar', popupUtil.getRouteIndex("borrar", true);
			params.putStr 'popupBorrar', tabla.popup.name;
			if (tabla.popup.permiso)
				params.putStr 'permisoPopup', tabla.popup.permiso.name;
		}
		if (tabla.popupVer != null) {
			params.put 'urlPopupLeer', Controller.fromPopup(tabla.popupVer).initialize().getRouteIndex("leer", true);
			params.putStr 'popupLeer', tabla.popupVer.name;
			if (tabla.popupVer.permiso)
				params.putStr 'permisoPopupLeer', tabla.popupVer.permiso.name;
		}
		if (tabla.popupCrear != null) {
			params.put 'urlPopupCrear', Controller.fromPopup(tabla.popupCrear).initialize().getRouteIndex("crear", true);
			params.putStr 'popupCrear', tabla.popupCrear.name;
			if (tabla.popupCrear.permiso)
				params.putStr 'permisoPopupCrear', tabla.popupCrear.permiso.name;
		}
		if (tabla.popupModificar != null) {
			params.put 'urlPopupEditar', Controller.fromPopup(tabla.popupModificar).initialize().getRouteIndex("editar", true);
			params.putStr 'popupEditar', tabla.popupModificar.name;
			if (tabla.popupModificar.permiso)
				params.putStr 'permisoPopupEditar', tabla.popupModificar.permiso.name;
		}
		if (tabla.popupBorrar != null) {
			params.put 'urlPopupBorrar', Controller.fromPopup(tabla.popupBorrar).initialize().getRouteIndex("borrar", true);
			params.putStr 'popupBorrar', tabla.popupBorrar.name;
			if (tabla.popupBorrar.permiso)
			params.putStr 'permisoPopupBorrar', tabla.popupBorrar.permiso.name;
		}
	}
	
	private void botonesPagina(TagParameters params){
		if (tabla.pagina != null) {
			Controller pagUtil = Controller.fromPagina(tabla.pagina).initialize();
			params.put 'urlPaginaLeer', pagUtil.getRouteIndex("leer", true);
			params.put 'urlPaginaCrear', pagUtil.getRouteIndex("crear", true);
			params.put 'urlPaginaEditar', pagUtil.getRouteIndex("editar", true);
			params.put 'urlPaginaBorrar', pagUtil.getRouteIndex("borrar", true);
			if (tabla.pagina.permiso)
				params.putStr 'permisoPagina', tabla.pagina.permiso.name;
		}
		if (tabla.paginaVer != null) {
			params.put 'urlPaginaLeer', Controller.fromPagina(tabla.paginaVer).initialize().getRouteIndex("leer", true);
			if (tabla.paginaVer.permiso)
				params.putStr 'permisoPaginaLeer', tabla.paginaVer.permiso.name;
		}
		if (tabla.paginaCrear != null) {
			params.put 'urlPaginaCrear', Controller.fromPagina(tabla.paginaCrear).initialize().getRouteIndex("crear", true);
			if (tabla.paginaCrear.permiso)
				params.putStr 'permisoPaginaCrear', tabla.paginaCrear.permiso.name;
		}
		if (tabla.paginaModificar != null) {
			params.put 'urlPaginaEditar', Controller.fromPagina(tabla.paginaModificar).initialize().getRouteIndex("editar", true);
			if (tabla.paginaModificar.permiso)
				params.putStr 'permisoPaginaEditar', tabla.paginaModificar.permiso.name;
		}
		if (tabla.paginaBorrar != null) {
			params.put 'urlPaginaBorrar', Controller.fromPagina(tabla.paginaBorrar).initialize().getRouteIndex("borrar", true);
			if (tabla.paginaBorrar.permiso)
				params.putStr 'permisoPaginaBorrar', tabla.paginaBorrar.permiso.name;
		}
	}
	
	private void addPopupBoton(Map popups, Popup popup, List<String> botones){
		if (popups.get(popup) != null){
			List last = popups.get(popup);
			last.addAll(botones);
			popups.put(popup, last);
		}
		else{
			popups.put(popup, botones);
		}
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
	
	public List<CampoUtils> uniqueCamposTabla(Tabla tabla){
		List<CampoUtils> campos = new ArrayList<CampoUtils>();
		for(Columna c : tabla.columnas){
			campos.addAll(camposDeColumna(c));
		}
		//Añade el ID de la entidad
		campos.add(CampoUtils.create(campo.getUltimaEntidad().name + ".id"));
		return campos.unique();
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
		String param = "";
		String idSingleton = "";
		if(campo.getCampo().getAtributos() == null){ //Lista todas las entidades de ese tipo
			query = """ "select ${entidadRaiz.variable} from ${entidadRaiz.clase} ${entidadRaiz.variable}" """
		}
		else{ //Acceso a los campos de una entidad
			query = """ "select ${entidadHija.variable} from ${entidadRaiz.clase} ${entidadRaiz.variable} join ${campo.firstLower()} ${entidadHija.variable} where ${entidadRaiz.variable}.id=?", ${entidadRaiz.id} """
			if (entidadRaiz.isSingleton())
				idSingleton = "${entidadRaiz.typeId} = ${entidadRaiz.clase}.get(${entidadRaiz.clase}.class).id;";
			else
				param = entidadRaiz.typeId;
		}
		
		String rowsStr = campos.collect { '"' + it.sinEntidad() + '"'  }.join(", ");

		return """
	public static void ${controllerMethodName()}(${param}){
		${idSingleton}
		java.util.List<${entidadHija.clase}> rows = ${entidadHija.clase}.find(${query}).fetch();
		${getCodePermiso(entidadHija)}
			
		tables.TableRenderResponse<${entidadHija.clase}> response = new tables.TableRenderResponse<${entidadHija.clase}>(rowsFiltered);
		renderJSON(response.toJSON($rowsStr));
	}

	${seleccionableMethod()}
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
				List<${entidad.clase}> rowsFiltered = rows; //Tabla sin permisos, no filtra
			""";
		}
		return """
			Map<String, Long> ids = new HashMap<String, Long>();
			List<${entidad.clase}> rowsFiltered = new ArrayList<${entidad.clase}>();
			for(${entidad.clase} ${entidad.variable}: rows){
				Map<String, Object> vars = new HashMap<String, Object>();
				vars.put("${entidad.variable}", ${entidad.variable});
				if (secure.check("${tabla.permiso.name}","read", ids, vars)) {
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
	
	private String seleccionableMethodName () {
		return StringUtils.firstLower(tabla.seleccionable.replaceAll(" ", ""))
	}

	private String seleccionableMethod () {
		if (! tabla.seleccionable)
			return "";
		return """
			public static void ${seleccionableMethodName()}(List<Long> idsSeleccionados){
				//Sobreescribir para incorporar funcionalidad
				//No olvide asignar los permisos
				//index();
			}
		"""
	}
}
