package templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.eclipse.emf.ecore.EObject


import generator.utils.*;
import es.fap.simpleled.led.*;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

import generator.utils.CampoUtils;

public class GTabla extends GElement{

	Tabla tabla;
	GElement gPaginaPopup;
	Controller controller;
	CampoUtils campo;
	boolean botonBorrar;
	String stringParamsAdded;
	Set<Entity> globalSetEntityParent;
	
	public GTabla(Tabla tabla, GElement container){
		super(tabla, container);
		this.tabla = tabla;
		campo = CampoUtils.create(tabla.campo);
		gPaginaPopup = getPaginaOrPopupContainer();
		//stringParamsAdded = new ArrayList<String>();
	}
	
	public String viewWithParams (Set<Entity> setEntityParent){
		globalSetEntityParent = setEntityParent;
		view();
	}
	
	public String view(){
		String controllerName = gPaginaPopup.controllerFullName();
		TagParameters params = new TagParameters();
		params.putStr 'id', id();
		Entidad entidad = campo.entidad;
		if(campo.getCampo().getAtributos() != null && !entidad.isSingleton()) {
			// No le han llegado los parámetros
			String finalStrParams = "";
			if (globalSetEntityParent != null && globalSetEntityParent.size() > 0) {
				finalStrParams = StringUtils.params(globalSetEntityParent.collect{if (!entidad.id.equals(it.id)) if ((it.id) != null) it.id});
				// Debemos eliminar los que no empiezan por id y los que ya están en params
				if (finalStrParams.trim().length() > 0)
					params.put 'urlTabla', "@${controllerName}.${controllerMethodName()}(${entidad.id}, "+finalStrParams+")";
				else
					params.put 'urlTabla', "@${controllerName}.${controllerMethodName()}(${entidad.id})";
			} else {
				params.put 'urlTabla', "@${controllerName}.${controllerMethodName()}(${entidad.id})";
			}
		}
		else
			params.put 'urlTabla', "@${controllerName}.${controllerMethodName()}(${StringUtils.params(globalSetEntityParent.collect{it.id})})";
        if (tabla.titulo)
			params.putStr 'titulo', tabla.titulo;
		params.putStr 'campo', campo.str;
		if (tabla.alto)
			params.putStr 'alto', tabla.alto;
		if (tabla.color){
			String consulta = "";
			if(tabla.color.codePrint){
				params.putStr 'codePrint', tabla.color.codePrint;
			}
			else if (tabla.color.default) {
				consulta = "if (record.data.permisoEditar) { return \\'docFirmado\\'; } else { return \\'docNoFirmado\\'; }";
				params.putStr 'codePrint', consulta;
			}
			else{
				if(tabla.color.textoB){
					consulta += "if (record.data.permisoBorrar) { return \\'${tabla.color.claseB}\\'; } "
				}
				if(tabla.color.textoE){
					consulta += "if (record.data.permisoEditar) { return \\'${tabla.color.claseE}\\'; }";
				}
				if(tabla.color.textoL){
					consulta += "if (record.data.permisoLeer) { return \\'${tabla.color.claseL}\\'; }";
				}
				params.putStr 'codePrint', consulta;
			}
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
		if (gPaginaPopup instanceof GPopup)
			params.putStr 'tipoContainer', "popup";
		else
			params.putStr 'tipoContainer', "pagina";
		params.putStr("idEntidad", "${Entidad.create(campo.ultimaEntidad).id}");
	
		controller = Controller.create(getPaginaOrPopupContainer());
		if (tabla.campo.entidad.name.equals(controller.campo?.ultimaEntidad?.name) && (tabla.pagina || tabla.paginaCrear || tabla.popup || tabla.popupCrear) && !controller.entidad.isSingleton()){
			params.put 'crearEntidad', "accion == 'crear'";
			params.putStr 'nameContainer', gPaginaPopup.name;
			params.putStr 'idContainer', controller.entidad.id;
			params.put 'urlContainerCrear', controller.getRouteIndex("crear", false, false);
			params.put 'urlContainerEditar', controller.getRouteIndex("editar", false, true);
			params.put 'urlCrearEntidad', controller.getRouteAccion("crearForTablas");
		}
		
		if (tabla.popup || tabla.popupCrear){
			params.put 'urlRedirigir', controller.getRouteIndex("editar", false, true);
		}
		
		if (tabla.pagina || tabla.paginaCrear || tabla.paginaBorrar || tabla.paginaEditar || tabla.paginaLeer){
			params.put 'urlBeforeOpenPageTable', controller.getRouteBeforeOpenPageTable("editar");
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

		if (controller.algoQueGuardar())
			params.put("saveEntity", true);
		else
			params.put("saveEntity", false);

		String view = """
#{fap.tabla ${params.lista(true)}
}
	${columnasView}
#{/fap.tabla}
"""
		return view;
	}
	
	private String id(){
		return tabla.name ?: campo.str.replace(".", "_");
	}
	
	private void botonesPopup(TagParameters params){
		if (tabla.popup != null) {
			Controller popupUtil = Controller.create(GElement.getInstance(tabla.popup, null));
			params.put 'urlLeer', popupUtil.getRouteIndex("leer", true, true);
			params.putStr 'popupLeer', tabla.popup.name;
			params.put 'urlCrear', popupUtil.getRouteIndex("crear", true, true);
			params.putStr 'popupCrear', tabla.popup.name;
			params.put 'urlEditar', popupUtil.getRouteIndex("editar", true, true);
			params.putStr 'popupEditar', tabla.popup.name;
			params.put 'urlBorrar', popupUtil.getRouteIndex("borrar", true, true);
			params.putStr 'popupBorrar', tabla.popup.name;
			if (tabla.popup.permiso)
				params.putStr 'permisoCrear', tabla.popup.permiso.name;
		}
		if (tabla.popupLeer != null) {
			params.put 'urlLeer', Controller.create(GElement.getInstance(tabla.popupLeer, null)).getRouteIndex("leer", true, true);
			params.putStr 'popupLeer', tabla.popupLeer.name;
		}
		if (tabla.popupCrear != null) {
			params.put 'urlCrear', Controller.create(GElement.getInstance(tabla.popupCrear, null)).getRouteIndex("crear", true, true);
			params.putStr 'popupCrear', tabla.popupCrear.name;
			if (tabla.popupCrear.permiso)
				params.putStr 'permisoCrear', tabla.popupCrear.permiso.name;
		}
		if (tabla.popupEditar != null) {
			params.put 'urlEditar', Controller.create(GElement.getInstance(tabla.popupEditar, null)).getRouteIndex("editar", true, true);
			params.putStr 'popupEditar', tabla.popupEditar.name;
		}
		if (tabla.popupBorrar != null) {
			params.put 'urlBorrar', Controller.create(GElement.getInstance(tabla.popupBorrar, null)).getRouteIndex("borrar", true, true);
			params.putStr 'popupBorrar', tabla.popupBorrar.name;
		}
	}
	
	private void botonesPagina(TagParameters params){
		if (tabla.pagina != null) {
			Controller pagUtil = Controller.create(GElement.getInstance(tabla.pagina, null));
			params.put 'urlLeer', pagUtil.getRouteIndex("leer", true, true);
			params.put 'urlCrear', pagUtil.getRouteIndex("crear", true, true);
			params.put 'urlEditar', pagUtil.getRouteIndex("editar", true, true);
			params.put 'urlBorrar', pagUtil.getRouteIndex("borrar", true, true);
			if (tabla.pagina.permiso)
				params.putStr 'permisoCrear', tabla.pagina.permiso.name;
		}
		if (tabla.paginaLeer != null)
			params.put 'urlLeer', Controller.create(GElement.getInstance(tabla.paginaLeer, null)).getRouteIndex("leer", true, true);
		if (tabla.paginaCrear != null) {
			params.put 'urlCrear', Controller.create(GElement.getInstance(tabla.paginaCrear, null)).getRouteIndex("crear", true, true);
			if (tabla.paginaCrear.permiso)
				params.putStr 'permisoCrear', tabla.paginaCrear.permiso.name;
		}
		if (tabla.paginaEditar != null)
			params.put 'urlEditar', Controller.create(GElement.getInstance(tabla.paginaEditar, null)).getRouteIndex("editar", true, true);
		if (tabla.paginaBorrar != null)
			params.put 'urlBorrar', Controller.create(GElement.getInstance(tabla.paginaBorrar, null)).getRouteIndex("borrar", true, true);
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
		String positionDefault = "left";
		if(c.campo != null){
			params.putStr("campo", CampoUtils.create(c.campo).sinEntidad());
			if (CampoUtils.create(c.campo).getUltimoAtributo()?.type?.special?.type?.equals("Moneda")) {
				// Si es un campo Moneda lo alineamos a la derecha
				positionDefault = "right";
			}
		}
		else if(c.funcion != null){
			String str = c.funcion;
			params.putStr("funcion", funcionSinEntidades(str));	
		}
		
		if (c.position != null) {
			params.putStr("alignPosition", c.position);
		} else {
			params.putStr("alignPosition", positionDefault);
		}
		
		if (c.permiso != null){
			params.putStr('permiso', c.permiso.name)
		}	

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
			CampoUtils _campo = CampoUtils.create(c.campo)
			campos.add(_campo);
			if (LedEntidadUtils.isMoneda(_campo.getUltimoAtributo())){
				campos.add(CampoUtils.create(CampoUtils.getCampoStr(c.campo)+"_formatFapTabla"));
			}
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
	
	public String controllerWithParams (Set<Entity> setEntityParent){
		//println setEntityParent;
		globalSetEntityParent = setEntityParent;
		stringParamsAdded = StringUtils.params(setEntityParent.collect{it.typeId});
		controller();
	}
	
	public String controller(){
		List<CampoUtils> campos = uniqueCamposTabla(tabla);
				
		// Si tiene permiso definido la tabla
		String renderCode = "";
		String codeConPermiso = "";

		//Clase de la entidad que contiene la lista
		
		Entidad entidad = Entidad.create(campo.getUltimaEntidad());
		Entidad entidadRaiz = campo.entidad;
		entidadRaiz.singletonsId = true;
		
		//La consulta depende de si se listan todas las entidades de una clase, o se accede a un campo
		String query = null;
		String param = "";
		String idSingleton = "";
		
		if (tabla.metodoFilas)
			query = """${tabla.metodoFilas}""";
		else if (!campo.getCampo().getAtributos()) //Lista todas las entidades de ese tipo
			query = """${entidad.clase}.find("select ${entidadRaiz.variable} from ${entidadRaiz.clase} ${entidadRaiz.variable}").fetch()""";
		else{ //Acceso a los campos de una entidad
			query = """${entidad.clase}.find("select ${entidad.variable} from ${entidadRaiz.clase} ${entidadRaiz.variable} join ${campo.firstLower()} ${entidad.variable} where ${entidadRaiz.variable}.id=?", ${entidadRaiz.id}).fetch()""";
			if (entidadRaiz.isSingleton())
				idSingleton = "${entidadRaiz.typeId} = ${entidadRaiz.clase}.get(${entidadRaiz.clase}.class).id;";
			else
				param = entidadRaiz.typeId;
		}
		
		String rowsStr = campos.collect { '"' + it.sinEntidad() + '"'  }.join(", ");

		String finalStrParams = "";
		if (stringParamsAdded != null && stringParamsAdded.length() > 0) {
			finalStrParams = StringUtils.params(globalSetEntityParent.collect{if (!param.contains(it.typeId)) it.typeId});
			// Debemos eliminar los que no empiezan por id y los que ya están en params
			if ((param.trim().size() > 0) && (finalStrParams.trim().size() > 0))
				finalStrParams = param + ", "+finalStrParams;
			else if (param.trim().size() > 0)
				finalStrParams = param;
		} else {
			finalStrParams = param;
		}
		// Deberemos añadir además, los id necesarios en el formulario, página o popup que contiene a esta tabla.

		return """
			public static void ${controllerMethodName()}(${finalStrParams}){
				${idSingleton}
				java.util.List<${entidad.clase}> rows = ${query};
				${getCodePermiso(entidad)}
			    ${getCodeFilasPermiso(entidad)}
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
	private String getCodePermiso(Entidad entidad) {
		if(tabla.permiso == null){
			return """
				Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
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
	
	private String getCodeFilasPermiso(Entidad entidad) {
		String ret="";
		String paramsPermiso="";
		String paramsNombrePermiso="";
		if (tabla.pagina != null){
			Pagina pagina = (Pagina)tabla.pagina;
			if (pagina.permiso != null){
				paramsPermiso+="true, true, true, \"${pagina.permiso.name}\", \"${pagina.permiso.name}\", \"${pagina.permiso.name}\", getAccion(), ids";
			} else {
				paramsPermiso+="false, false, false, \"\", \"\", \"\", getAccion(), ids";
			}
		}
		else if(tabla.popup != null){
			Popup popUp = (Popup)tabla.popup;
			if (popUp.permiso != null){
				paramsPermiso+="true, true, true, \"${popUp.permiso.name}\", \"${popUp.permiso.name}\", \"${popUp.permiso.name}\", getAccion(), ids";
			} else {
				paramsPermiso+="false, false, false, \"\", \"\", \"\", getAccion(), ids";
			}
		} else {
			if(tabla.paginaEditar != null){
				Pagina pagina = (Pagina)tabla.paginaEditar;
				if (pagina.permiso != null){
					paramsPermiso+="true, ";
					paramsNombrePermiso+="\"${pagina.permiso.name}\", ";
				} else{
					paramsPermiso+="false, ";
					paramsNombrePermiso+="\"\", ";
				}
			} else if(tabla.popupEditar != null){
				Popup popUp = (Popup)tabla.popupEditar;
				if (popUp.permiso != null){
					paramsPermiso+="true, ";
					paramsNombrePermiso+="\"${popUp.permiso.name}\", ";
				} else{
					paramsPermiso+="false, ";
					paramsNombrePermiso+="\"\", ";
				}
			} else {
				paramsPermiso+="false, ";
				paramsNombrePermiso+="\"\", ";
			}
			if(tabla.paginaBorrar != null){
				Pagina pagina = (Pagina)tabla.paginaBorrar;
				if (pagina.permiso != null){
					paramsPermiso+="true, ";
					paramsNombrePermiso+="\"${pagina.permiso.name}\", ";
				} else{
					paramsPermiso+="false, ";
					paramsNombrePermiso+="\"\", ";
				}
			} else if(tabla.popupBorrar != null){
				Popup popUp = (Popup)tabla.popupBorrar;
				if (popUp.permiso != null){
					paramsPermiso+="true, ";
					paramsNombrePermiso+="\"${popUp.permiso.name}\", ";
				} else{
					paramsPermiso+="false, ";
					paramsNombrePermiso+="\"\", ";
				}
			} else {
				paramsPermiso+="false, ";
				paramsNombrePermiso+="\"\", ";
			}
			if(tabla.paginaLeer != null){
				Pagina pagina = (Pagina)tabla.paginaLeer;
				if (pagina.permiso != null){
					paramsPermiso+="true, ";
					paramsNombrePermiso+="\"${pagina.permiso.name}\"";
				} else{
					paramsPermiso+="false, ";
					paramsNombrePermiso+="\"\"";
				}
			} else if(tabla.popupLeer != null){
				Popup popUp = (Popup)tabla.popupLeer;
				if (popUp.permiso != null){
					paramsPermiso+="true, ";
					paramsNombrePermiso+="\"${popUp.permiso.name}\"";
				} else{
					paramsPermiso+="false, ";
					paramsNombrePermiso+="\"\"";
				}
			} else {
				paramsPermiso+="false, ";
				paramsNombrePermiso+="\"\"";
			}
			paramsPermiso+=paramsNombrePermiso+", getAccion(), ids"
		}

		ret+="""tables.TableRenderResponse<${entidad.clase}> response = new tables.TableRenderResponse<${entidad.clase}>(rowsFiltered, ${paramsPermiso});
			"""
		return ret;
	}
	
	private controllerMethodName(){
		return "tabla" + id();
	}
	
	public String routes(){
		String url = gPaginaPopup.url() + "/" + id();
		String action = gPaginaPopup.controllerFullName() + "." + controllerMethodName();
		return RouteUtils.to("GET", url, action).toString() + "\n";
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
	
	public void addParams2Controller (String stringParams) {
		stringParamsAdded = stringParams;
	}
}
