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

	Tabla tabla;
	def contenedor;
	Controller controller;
	String tipo;
	CampoUtils campo;
	String permisoBotonLeer;
	String permisoBotonEditar;
	String permisoBotonBorrar;
	boolean botonLeer;
	boolean botonEditar;
	boolean botonBorrar;
	
	public static String generate(Tabla tabla){
		GTabla g = new GTabla();
		g.tabla = tabla;
		g.campo = CampoUtils.create(tabla.campo);
		
		g.contenedor = HashStack.top(HashStackName.CONTAINER);
		if (g.contenedor instanceof GPopup){
			g.tipo = "popup";
			g.controller = Controller.fromPopup(g.contenedor.popup).initialize();
		}
		else{
			g.tipo = "pagina";
			g.controller = Controller.fromPagina(g.contenedor.pagina).initialize();
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
        	params.put 'urlTabla', "@${controllerName}.${controllerMethodName()}(${entidad.id})"
		else
			params.put 'urlTabla', "@${controllerName}.${controllerMethodName()}()"
			
        if (tabla.titulo){
			params.putStr 'titulo', tabla.titulo
		}
		params.putStr 'campo', campo.str

		if (tabla.alto){
			params.putStr 'alto', tabla.alto
		}
		
		botonesPopup(params);
		botonesPagina(params);
		
		if (tabla.recargarPagina)
			params.put("recargarPagina", true)
			
	    List <Attribute> excludes, includes;
	
		if (tabla.seleccionable) {
			params.putStr("seleccionable", tabla.seleccionable)
			params.putStr("urlSeleccionable", "${controllerName}.${seleccionableMethodName()}")
		}

		params.putStr 'tipoContainer', tipo;
		params.putStr("idEntidad", "${EntidadUtils.create(campo.ultimaEntidad).id}");
	
		if (tabla.campo.entidad.name.equals(controller.campo?.ultimaEntidad?.name) && (tabla.pagina || tabla.paginaCrear || tabla.popup || tabla.popupCrear) && !controller.entidad.isSingleton()){
			params.put 'crearEntidad', "accion == 'crear'";
			params.putStr 'nameContainer', contenedor.name;
			params.putStr 'idContainer', controller.entidad.id;
			params.put 'urlContainerCrear', controller.getRouteIndex("crear", false, false);
			params.put 'urlContainerEditar', controller.getRouteIndex("editar", false, true);
			params.put 'urlCrearEntidad', controller.getRouteAccion("crearForTablas");
		}
		
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
			boolean listo = false;
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
	    }
		if(tabla.columnas.isEmpty()){
			Columna c = LedFactory.eINSTANCE.createColumna();
			c.campo = CampoUtils.create("${LedCampoUtils.getUltimaEntidad(tabla.campo).name}.id").campo;
			tabla.columnas.add(c);
		}
		for(Columna c : tabla.columnas)
			columnasView.append (columnaView(c));

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
			params.put 'urlLeer', popupUtil.getRouteIndex("leer", true, true);
			params.putStr 'popupLeer', tabla.popup.name;
			params.put 'urlCrear', popupUtil.getRouteIndex("crear", true, true);
			params.putStr 'popupCrear', tabla.popup.name;
			params.put 'urlEditar', popupUtil.getRouteIndex("editar", true, true);
			params.putStr 'popupEditar', tabla.popup.name;
			params.put 'urlBorrar', popupUtil.getRouteIndex("borrar", true, true);
			params.putStr 'popupBorrar', tabla.popup.name;
			botonLeer = true;
			botonEditar = true;
			botonBorrar = true;
			if (tabla.popup.permiso){
				params.putStr 'permisoCrear', tabla.popup.permiso.name;
				permisoBotonLeer = tabla.popup.permiso.name;
				permisoBotonEditar = tabla.popup.permiso.name;
				permisoBotonBorrar = tabla.popup.permiso.name;
			}
		}
		if (tabla.popupLeer != null) {
			params.put 'urlLeer', Controller.fromPopup(tabla.popupLeer).initialize().getRouteIndex("leer", true, true);
			params.putStr 'popupLeer', tabla.popupLeer.name;
			botonLeer = true;
			if (tabla.popupLeer.permiso)
				permisoBotonLeer = tabla.popupLeer.permiso.name;
		}
		if (tabla.popupCrear != null) {
			params.put 'urlCrear', Controller.fromPopup(tabla.popupCrear).initialize().getRouteIndex("crear", true, true);
			params.putStr 'popupCrear', tabla.popupCrear.name;
			if (tabla.popupCrear.permiso)
				params.putStr 'permisoCrear', tabla.popupCrear.permiso.name;
		}
		if (tabla.popupEditar != null) {
			params.put 'urlEditar', Controller.fromPopup(tabla.popupEditar).initialize().getRouteIndex("editar", true, true);
			params.putStr 'popupEditar', tabla.popupEditar.name;
			botonEditar = true;
			if (tabla.popupEditar.permiso)
				permisoBotonEditar = tabla.popupEditar.permiso.name;
		}
		if (tabla.popupBorrar != null) {
			params.put 'urlBorrar', Controller.fromPopup(tabla.popupBorrar).initialize().getRouteIndex("borrar", true, true);
			params.putStr 'popupBorrar', tabla.popupBorrar.name;
			botonBorrar = true;
			if (tabla.popupBorrar.permiso)
				permisoBotonBorrar = tabla.popupBorrar.permiso.name;
		}
	}
	
	private void botonesPagina(TagParameters params){
		if (tabla.pagina != null) {
			Controller pagUtil = Controller.fromPagina(tabla.pagina).initialize();
			params.put 'urlLeer', pagUtil.getRouteIndex("leer", true, true);
			params.put 'urlCrear', pagUtil.getRouteIndex("crear", true, true);
			params.put 'urlEditar', pagUtil.getRouteIndex("editar", true, true);
			params.put 'urlBorrar', pagUtil.getRouteIndex("borrar", true, true);
			botonLeer = true;
			botonEditar = true;
			botonBorrar = true;
			if (tabla.pagina.permiso){
				params.putStr 'permisoCrear', tabla.pagina.permiso.name;
				permisoBotonLeer = tabla.pagina.permiso.name;
				permisoBotonEditar = tabla.pagina.permiso.name;
				permisoBotonBorrar = tabla.pagina.permiso.name;
			}
		}
		if (tabla.paginaLeer != null) {
			params.put 'urlLeer', Controller.fromPagina(tabla.paginaLeer).initialize().getRouteIndex("leer", true, true);
			botonLeer = true;
			if (tabla.paginaLeer.permiso)
				permisoBotonLeer = tabla.paginaLeer.permiso.name;
		}
		if (tabla.paginaCrear != null) {
			params.put 'urlCrear', Controller.fromPagina(tabla.paginaCrear).initialize().getRouteIndex("crear", true, true);
			if (tabla.paginaCrear.permiso)
				params.putStr 'permisoCrear', tabla.paginaCrear.permiso.name;
		}
		if (tabla.paginaEditar != null) {
			params.put 'urlEditar', Controller.fromPagina(tabla.paginaEditar).initialize().getRouteIndex("editar", true, true);
			botonEditar = true;
			if (tabla.paginaEditar.permiso)
				permisoBotonEditar = tabla.paginaEditar.permiso.name;
		}
		if (tabla.paginaBorrar != null) {
			params.put 'urlBorrar', Controller.fromPagina(tabla.paginaBorrar).initialize().getRouteIndex("borrar", true, true);
			botonBorrar = true;
			if (tabla.paginaBorrar.permiso)
				permisoBotonBorrar = tabla.paginaBorrar.permiso.name;
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
		
		EntidadUtils entidad = EntidadUtils.create(campo.getUltimaEntidad());
		EntidadUtils entidadRaiz = EntidadUtils.create(campo.getEntidad());
		
		//La consulta depende de si se listan todas las entidades de una clase, o se accede a un campo
		String query = null;
		String param = "";
		String idSingleton = "";
		if(campo.getCampo().getAtributos() == null){ //Lista todas las entidades de ese tipo
			query = """ "select ${entidadRaiz.variable} from ${entidadRaiz.clase} ${entidadRaiz.variable}" """
		}
		else{ //Acceso a los campos de una entidad
			query = """ "select ${entidad.variable} from ${entidadRaiz.clase} ${entidadRaiz.variable} join ${campo.firstLower()} ${entidad.variable} where ${entidadRaiz.variable}.id=?", ${entidadRaiz.id} """
			if (entidadRaiz.isSingleton())
				idSingleton = "${entidadRaiz.typeId} = ${entidadRaiz.clase}.get(${entidadRaiz.clase}.class).id;";
			else
				param = entidadRaiz.typeId;
		}
		
		String rowsStr = campos.collect { '"' + it.sinEntidad() + '"'  }.join(", ");

		return """
			public static void ${controllerMethodName()}(${param}){
				${idSingleton}
				java.util.List<${entidad.clase}> rows = ${entidad.clase}.find(${query}).fetch();
				${getCodePermiso(entidad)}
				tables.TableRenderResponse<${entidad.clase}> response = new tables.TableRenderResponse<${entidad.clase}>(${controller.controllerName}.${controllerMethodName()}Permisos(rowsFiltered));
				renderJSON(response.toJSON($rowsStr));
			}

			@Util
			public static List<TableRecord<${entidad.clase}>> ${controllerMethodName()}Permisos(List<${entidad.clase}> rowsFiltered){
				Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
				List<TableRecord<${entidad.clase}>> records = new ArrayList<TableRecord<${entidad.clase}>>();
				Map<String, Object> vars = new HashMap<String, Object>();
				for (${entidad.clase} ${entidad.variable}: rowsFiltered){
					TableRecord<${entidad.clase}> record = new TableRecord<${entidad.clase}>();
					records.add(record);
					record.objeto = ${entidad.variable};
					vars.put("${entidad.variable}", ${entidad.variable});
					record.permisoLeer = ${permisoBotonLeer? "secure.checkAcceso(\"${permisoBotonLeer}\", \"leer\", ids, vars)" : botonLeer};
					record.permisoEditar = ${permisoBotonEditar? "secure.checkAcceso(\"${permisoBotonEditar}\", \"editar\", ids, vars)" : botonEditar};
					record.permisoBorrar = ${permisoBotonBorrar? "secure.checkAcceso(\"${permisoBotonBorrar}\", \"borrar\", ids, vars)" : botonBorrar};
				}
				return records;
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
			List<${entidad.clase}> rowsFiltered = new ArrayList<${entidad.clase}>();
			Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
			for(${entidad.clase} ${entidad.variable}: rows){
				Map<String, Object> vars = new HashMap<String, Object>();
				vars.put("${entidad.variable}", ${entidad.variable});
				if (secure.checkAcceso("${tabla.permiso.name}", "leer", ids, vars)) {
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
	
	public String getNameRoute () {
		return (contenedor.url() + "/" + id());
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
