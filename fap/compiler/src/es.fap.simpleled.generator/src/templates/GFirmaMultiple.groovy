package templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.emf.ecore.EObject;
import org.apache.commons.lang3.StringEscapeUtils;

import generator.utils.*;
import es.fap.simpleled.led.*;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

import generator.utils.CampoUtils;

public class GFirmaMultiple extends GElement{

	FirmaMultiple firmaMultiple;
	GElement gPaginaPopup;
	Controller controller;
	CampoUtils campo;
	String stringParamsAdded;
	Set<Entity> globalSetEntityParent;
	
	public GFirmaMultiple(FirmaMultiple firmaMultiple, GElement container){
		super(firmaMultiple, container);
		this.firmaMultiple = firmaMultiple;
		campo = CampoUtils.create(firmaMultiple.documentos.campo);
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
					params.put 'urlFirmaMultiple', "@${controllerName}.${controllerMethodName()}(${entidad.id}, "+finalStrParams+")";
				else
					params.put 'urlFirmaMultiple', "@${controllerName}.${controllerMethodName()}(${entidad.id})";
			} else {
				params.put 'urlFirmaMultiple', "@${controllerName}.${controllerMethodName()}(${entidad.id})";
			}
		}
		else
			params.put 'urlFirmaMultiple', "@${controllerName}.${controllerMethodName()}(${StringUtils.params(globalSetEntityParent.collect{it.id})})";
        if (firmaMultiple.titulo)
			params.putStr 'titulo', firmaMultiple.titulo;
		params.putStr 'campo', campo.str;
		if (firmaMultiple.alto)
			params.putStr 'alto', firmaMultiple.alto;

		botonesPopup(params);
		botonesPagina(params);
		if (firmaMultiple.recargarPagina)
			params.put("recargarPagina", true)

		if (gPaginaPopup instanceof GPopup)
			params.putStr 'tipoContainer', "popup";
		else
			params.putStr 'tipoContainer', "pagina";
		params.putStr("idEntidad", "${Entidad.create(campo.ultimaEntidad).id}");
	
		controller = Controller.create(getPaginaOrPopupContainer());
		
		//Nombre de los botones (opcional)
		if(firmaMultiple.nombreBotonVer != null){
			params.putStr 'nombreBotonVer', firmaMultiple.nombreBotonVer;
		}
		
		StringBuffer columnasView = new StringBuffer();

		if(firmaMultiple.columnas.isEmpty()){
			Columna c = LedFactory.eINSTANCE.createColumna();
			c.campo = CampoUtils.create("${LedCampoUtils.getUltimaEntidad(firmaMultiple.documentos.campo).name}.id").campo;
			firmaMultiple.columnas.add(c);
		}
		for(Columna c : firmaMultiple.columnas)
			columnasView.append (columnaView(c));

		if (controller.algoQueGuardar())
			params.put("saveEntity", true);
		else
			params.put("saveEntity", false);

		String view = """
#{fap.firmaMultiple ${params.lista(true)}
}
	${columnasView}
#{/fap.firmaMultiple}
"""
		return view;
	}
	
	private String id(){
		return firmaMultiple.name ?: campo.str.replace(".", "_");
	}
	
	private void botonesPopup(TagParameters params){
		if (firmaMultiple.popupLeer != null) {
			params.put 'urlLeer', Controller.create(GElement.getInstance(firmaMultiple.popupLeer, null)).getRouteIndex("leer", true, true);
			params.putStr 'popupLeer', firmaMultiple.popupLeer.name;
		}
	}
	
	private void botonesPagina(TagParameters params){
		if (firmaMultiple.paginaLeer != null)
			params.put 'urlLeer', Controller.create(GElement.getInstance(firmaMultiple.paginaLeer, null)).getRouteIndex("leer", true, true);
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
		List<CampoUtils> campos = GFirmaMultiple.camposDeColumna(c);
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
			String str = StringEscapeUtils.escapeJava(c.funcion).replace("'", "\\'");
			params.putStr("funcion", funcionSinEntidades(str));	
		}
		else if(c.funcionRaw != null){
			String str = StringEscapeUtils.escapeJava(c.funcionRaw).replace("'", "\\'");
			params.putStr("funcionRaw", funcionSinEntidades(str));
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
		else if(c.funcion != null || c.funcionRaw != null){
			String strFuncion = c.funcion != null ? c.funcion : c.funcionRaw;
			Pattern funcionSinEntidadPattern = Pattern.compile('\\$\\{(.*?)\\}');
			Matcher matcher = funcionSinEntidadPattern.matcher(strFuncion);
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
		String strFuncion = c.funcion != null ? c.funcion : c.funcionRaw;
		if(strFuncion == null)
			return null;
		if(c.funcionRaw != null)
			return "return " + (strFuncion.replaceAll(/\$\{(.*?)\}/, 'record[\'$1\']')) + ";"
		else
			return  "return '" + (strFuncion.replaceAll(/\$\{(.*?)\}/, '\' + record[\'$1\'] + \'')) + "';"
	}
	
	public List<CampoUtils> uniqueCamposFirmaMultiple(FirmaMultiple firmaMultiple){
		List<CampoUtils> campos = new ArrayList<CampoUtils>();
		for(Columna c : firmaMultiple.columnas){
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
		List<CampoUtils> campos = uniqueCamposFirmaMultiple(firmaMultiple);
				
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
		
		if (firmaMultiple.metodoFilas)
			query = """${firmaMultiple.metodoFilas}""";
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

	public static String obtenerUrlDocumento${id()}(Long idDocumento){
		return FirmaUtils.obtenerUrlDocumento(idDocumento);
	}

	public static String obtenerFirmadoDocumento${id()}(Long idDocumento) {
		Documento documento = Documento.find("select documento from Documento documento where documento.id=?", idDocumento).first();
		if (documento != null) {
			play.Logger.info("El documento " + documento.id + " tiene la uri " + documento.uri + " y  firmado a " + documento.firmado);
			if (documento.firmado != null && documento.firmado == true) {
				return "true";
			}
			return "false";
		}
		play.Logger.info("Error al obtener el documento "+idDocumento);
		return null;
	}

	@Util
	public static boolean firmar${id()}(Long idDocumento, String firma) {

		Documento documento = Documento.find("select documento from Documento documento where documento.id=?", idDocumento).first();
		
		if (documento != null) {
			play.Logger.info("Firmando documento " + documento.uri);
	
			Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
			Map<String, Object> vars = new HashMap<String, Object>();
			if (secure.checkAcceso("editarFirmaDocumento", "editar", ids, vars)) {
				if (documento.firmantes == null) {
					documento.firmantes = new Firmantes();
					documento.save();
				}
				if (documento.firmantes.todos == null || documento.firmantes.todos.size() == 0) {
					Long idSolicitud = ids.get("idSolicitud");
					documento.firmantes.todos = calcularFirmantes${id()}(idSolicitud);
					documento.firmantes.save();
				}
				FirmaUtils.firmarDocumento(documento, documento.firmantes.todos, firma, null);
			} else {
				//ERROR
				Messages.error("No tiene permisos suficientes para realizar la acción++");
			}
	
			if (!Messages.hasErrors()) {
				play.Logger.info("Firma de documento " + documento.uri + " con éxito");
				return true;
			}
			play.Logger.info("Firma de documento " + documento.uri + " sin éxito");
		} else {
			play.Logger.info("Error al obtener el documento "+idDocumento);
		}
		return false;
	}
	
	public static List<Firmante> calcularFirmantes${id()}(Long idSolicitud) {

		SolicitudGenerica solicitud = null;
		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		} else {
			solicitud = SolicitudGenerica.findById(idSolicitud);
			if (solicitud == null) {
				Messages.fatal("Error al recuperar SolicitudGenerica");
			}
		}
		
		Firmantes firmantes = new Firmantes();
		//Solicitante de la solicitud
		Firmante firmanteSolicitante = new Firmante(solicitud.solicitante, "unico");
		firmantes.todos.add(firmanteSolicitante);

		//Comprueba los representantes
		if (solicitud.solicitante.isPersonaFisica() && solicitud.solicitante.representado) {
			// Representante de persona física
			Firmante representante = new Firmante(solicitud.solicitante.representante, "representante", "unico");
			firmantes.todos.add(representante);
		} else if (solicitud.solicitante.isPersonaJuridica()) {
			//Representantes de la persona jurídica
			for (RepresentantePersonaJuridica r : solicitud.solicitante.representantes) {
				String cardinalidad = null;
				if (r.tipoRepresentacion.equals("mancomunado")) {
					cardinalidad = "multiple";
				} else if ((r.tipoRepresentacion.equals("solidario")) || (r.tipoRepresentacion.equals("administradorUnico"))) {
					cardinalidad = "unico";
				}
				Firmante firmante = new Firmante(r, "representante", cardinalidad);
				firmantes.todos.add(firmante);
			}
		}
		return firmantes.todos;
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
	private String getCodePermiso(Entidad entidad) {
		if(firmaMultiple.permiso == null){
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
				if (secure.checkAcceso("${firmaMultiple.permiso.name}", "leer", ids, vars)) {
					rowsFiltered.add(${entidad.variable});
				}
			}
		"""
	}
	
	private String getCodeFilasPermiso(Entidad entidad) {
		String ret="";
		String paramsPermiso="";
		String paramsNombrePermiso="";
		
		paramsPermiso+="false, ";
		paramsNombrePermiso+="\"\", ";
		
		paramsPermiso+="false, ";
		paramsNombrePermiso+="\"\", ";
		
		if(firmaMultiple.paginaLeer != null){
			Pagina pagina = (Pagina)firmaMultiple.paginaLeer;
			if (pagina.permiso != null){
				paramsPermiso+="true, ";
				paramsNombrePermiso+="\"${pagina.permiso.name}\"";
			} else{
				paramsPermiso+="false, ";
				paramsNombrePermiso+="\"\"";
			}
		} else if(firmaMultiple.popupLeer != null){
			Popup popUp = (Popup)firmaMultiple.popupLeer;
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

		ret+="""tables.TableRenderResponse<${entidad.clase}> response = new tables.TableRenderResponse<${entidad.clase}>(rowsFiltered, ${paramsPermiso});
			"""
		return ret;
	}
	
	private controllerMethodName(){
		return "firmaMultiple" + id();
	}
	
	public String routes(){
		String url = gPaginaPopup.url() + "/" + id();
		String action = gPaginaPopup.controllerFullName() + "." + controllerMethodName();
		
		return RouteUtils.to("GET", url, action).toString() + "\n";
	}
	
	public void addParams2Controller (String stringParams) {
		stringParamsAdded = stringParams;
	}
}