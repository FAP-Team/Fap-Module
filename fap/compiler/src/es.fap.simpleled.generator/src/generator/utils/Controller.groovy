package generator.utils;

import es.fap.simpleled.led.Accion
import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Boton
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.CampoAtributos;
import es.fap.simpleled.led.Entity
import es.fap.simpleled.led.Form
import es.fap.simpleled.led.FirmaPlatinoSimple;
import es.fap.simpleled.led.MenuEnlace
import es.fap.simpleled.led.Enlace
import es.fap.simpleled.led.Pagina
import es.fap.simpleled.led.PaginaAccion
import es.fap.simpleled.led.Permiso
import es.fap.simpleled.led.PermisoWhen
import es.fap.simpleled.led.Popup
import es.fap.simpleled.led.PopupAccion
import es.fap.simpleled.led.Tabla
import es.fap.simpleled.led.util.ModelUtils;
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.LedFactory;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IGenerator

import templates.GForm
import templates.GPagina
import templates.GPopup
import templates.GPermiso
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.led.util.LedCampoUtils;

public class Controller{
	
	// Lista de atributos que tienen que recibirse como parámetros
	public EObject container; // Elemento Popup, Pagina o Form
	public boolean index;
	public boolean crear;
	public boolean editar;
	public boolean borrar;
	public List<Object> saveController;
	public CampoUtils campo;
	public String renderView;
	public Permiso permiso;
	public String controllerGenName;
	public String controllerName;
	public String controllerGenFullName;
	public String controllerFullName;
	public String url;
	public String packageName;
	public String packageGenName;
	public boolean noBorrarEntidad;
	public boolean noAutenticar;
	public String name;
	public List<String> saveExtra;
	public List<String> saveCode;
	public List<String> saveBoton;
	public List<String> firmaBoton;
	public List<EntidadUtils> saveEntities;
	public List<EntidadUtils> indexEntities;
	public EObject elementoGramatica;
	
	// Lista de atributos que son calculados en esta clase
	private EntidadUtils almacen;
	private EntidadUtils almacenNoSingle;  // nulo() retorna true cuando el almacen es Singleton
	private EntidadUtils entidad;
	private EntidadUtils entidadNoSingle;  // nulo() retorna true cuando la entidad es Singleton
	private EntidadUtils entidadPagina;		// Si no hay campos guardables nulo() sera true
	private List<EntidadUtils> intermedias;
	private boolean hayAnterior;
	private boolean xToMany;
	private String nameEditar;
	private String sufijoPermiso;
	private String sufijoBoton;
	private List<AlmacenEntidad> campos;  // No incluye el ultimo
	private List<AlmacenEntidad> camposTodos;
	private Accion accionCrear;
	private Accion accionEditar;
	private Accion accionBorrar;
	
	public Controller initialize(){
		campos = calcularSubcampos(campo?.campo);
		camposTodos = new ArrayList<AlmacenEntidad>(campos);
		if (campos.size() > 0){
			campo = campos.get(campos.size() - 1).campo;
			campos.remove(campos.size() - 1);
		}
		intermedias = new ArrayList<EntidadUtils>();
		for (AlmacenEntidad subcampo: campos)
			intermedias.add(subcampo.almacen);
		
		hayAnterior = hayAnterior(container);
			
		entidad = EntidadUtils.create(campo?.getUltimaEntidad());
		entidadPagina = EntidadUtils.create(entidad.entidad);
		if (!LedCampoUtils.hayCamposGuardables(container))
			entidadPagina = EntidadUtils.create((Entity)null);
		almacen = EntidadUtils.create((Entity)null);
		
		if (campo?.campo?.atributos != null){
			almacen = EntidadUtils.create(campo.getEntidad());
			if (LedEntidadUtils.xToMany(campo?.getUltimoAtributo()))
				xToMany = true;
			else
				xToMany = false;
		}
		
		nameEditar = "editar";
		sufijoPermiso = "";
		sufijoBoton = "";
		if (isForm()){
			nameEditar = name;
			sufijoPermiso = StringUtils.firstUpper(name);
			sufijoBoton = StringUtils.firstUpper(name);
		}
					
		for (int i = 0; i < saveEntities.size(); i++){
			if (saveEntities.get(i).equals(entidad)){
				saveEntities.remove(i);
				break;
			}
		}
		for (int i = 0; i < indexEntities.size(); i++){
			if (indexEntities.get(i).equals(entidad)){
				indexEntities.remove(i);
				break;
			}
		}
		almacenNoSingle = EntidadUtils.create(almacen.entidad);
		if (almacen.isSingleton()){
			almacenNoSingle.entidad = null;
		}
		entidadNoSingle = EntidadUtils.create(entidad.entidad);
		if (entidad.isSingleton()){
			entidadNoSingle.entidad = null;
		}
		return this;
	}
	
	public void controller(){
		String withSecure = "";
		if (!noAutenticar)
			withSecure = "@With(CheckAccessController.class)"
			
		String controllerGen = """
package ${packageGenName};

import play.*;
import play.mvc.*;
import play.db.jpa.Model;
import controllers.fap.*;
import validation.*;
import messages.Messages;
import messages.Messages.MessageType;
import controllers.${controllerFullName};
import utils.GestorDocumentalUtils;
import tables.TableRecord;
import models.*;
import tags.ReflectionUtils;
import security.Accion;
import platino.FirmaUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import services.FirmaService;
import com.google.inject.Inject;

${withSecure}
public class ${controllerGenName} extends GenericController {

	protected static Logger log = Logger.getLogger("Paginas");

${metodoIndex()}

${metodoEditar()}

${metodoCrear()}

${metodoCrearLogica()}

${metodoBorrar()}

${metodoValidateCopy()}

${metodoPermiso()}

${metodoPrimeraAccion()}

${metodoEditarRender()}

${metodoCrearRender()}

${metodoBorrarRender()}

${metodosCrearForTablas()}

${metodoEditarValidateRules()}

${metodoCrearValidateRules()}

${metodoBorrarValidateRules()}

${metodoBindReferences()}

${botonesMethods()}

${getters()}

${metodoCheckRedirigir()}

${metodosHashStack()}

${beforeMethod()}

}
"""
		FileUtils.overwrite(FileUtils.getRoute('CONTROLLER_GEN'),controllerGenFullName.replaceAll("\\.", "/") + ".java", Beautifier.formatear(controllerGen));
			
		String controller = """
package ${packageName};

import ${packageGenName}.${controllerGenName};
			
public class ${controllerName} extends ${controllerGenName} {

}
		"""
		FileUtils.write(FileUtils.getRoute('CONTROLLER'), controllerFullName.replaceAll("\\.", "/") + ".java", Beautifier.formatear(controller));
	}
	
	public String generateRoutes(){
		StringBuffer sb = new StringBuffer();
		if (index)
			StringUtils.appendln sb, Route.to("GET", url, controllerFullName + ".index");
		if (editar || isForm())
			StringUtils.appendln sb, Route.to("POST", url + "/${nameEditar}", controllerFullName + ".${nameEditar}");
		if (borrar)
			StringUtils.appendln sb, Route.to("POST", url + "/borrar", controllerFullName + ".borrar");
		if (crear)
			StringUtils.appendln sb, Route.to("POST", url + "/crear", controllerFullName + ".crear");
		return sb.toString();
	}
	
	private String getters(){
		String getters = "";
		if(almacen.entidad != null){
			for (AlmacenEntidad subcampo: camposTodos)
				getters += ControllerUtils.complexGetter(controllerName, subcampo.almacen, subcampo.entidad, subcampo.campo);
			getters += ControllerUtils.simpleGetter(camposTodos.get(0).almacen, true);
		}
		else{
			getters += ControllerUtils.simpleGetter(entidad, true);
		}
		if (!entidad.isSingleton())
			getters += ControllerUtils.simpleGetter(entidad, false); // get sin parametros, que dentro hace un new()
		for (EntidadUtils entity: saveEntities)
			getters += ControllerUtils.simpleGetter(entity, false);
		for (EntidadUtils entity: indexEntities)
			getters += ControllerUtils.simpleGetter(entity, false);
		return getters;
	}
	
	private String gettersForm(Controller c){
		List<String> saveContainer = new ArrayList<String>();
		for (EntidadUtils entity: c.saveEntities)
			saveContainer.add(entity.variable);
		String getters = "";
		for (EntidadUtils entity: saveEntities){
			if (!saveContainer.contains(entity.variable))
				getters += ControllerUtils.simpleGetter(entity, false);
		}
		return getters;
	}
	
	private String metodoIndex(){
		EntidadUtils primerAlmacen = camposTodos.size() > 0? camposTodos.get(0).almacen : EntidadUtils.create((Entity)null);
		String getEntidad = "";
		if (!entidad.nulo()){
			getEntidad = """
				$entidad.clase $entidad.variable = null;
				if("crear".equals(accion))
					${ControllerUtils.newCall(entidad)}
				else if (!"borrado".equals(accion))
					$entidad.variable = ${ControllerUtils.complexGetterCall(controllerName, almacen, entidad)};
			""";
		}
		return """
			public static void index(${StringUtils.params(
				"String accion",
				intermedias.collect{it.typeId},
				almacenNoSingle.typeId,
				entidadNoSingle.typeId
			)}){
				if (accion == null)
					accion = getAccion();
				if (!permiso(accion)){
					Messages.fatal("${permiso?.mensaje? permiso.mensaje : "No tiene permisos suficientes para realizar esta acción"}");
					renderTemplate(${renderView});
				}
				${hayAnterior? "checkRedirigir();" : ""}
				${primerAlmacen.entidad? "$primerAlmacen.clase $primerAlmacen.variable = ${ControllerUtils.simpleGetterCall(controllerName, primerAlmacen, true)};" : ""}
				${campos.collect{"$it.entidad.clase $it.entidad.variable = ${ControllerUtils.complexGetterCall(controllerName, it.almacen, it.entidad)};"}.join("\n")}
				${indexEntities.collect{"$it.clase $it.variable = ${ControllerUtils.simpleGetterCall(controllerName, it, false)};"}.join("\n")}
				${saveEntities.collect{"$it.clase $it.variable = ${ControllerUtils.simpleGetterCall(controllerName, it, false)};"}.join("\n")}
				${getEntidad}
				log.info("Visitando página: "+${renderView});
				renderTemplate(${StringUtils.params(
					renderView,
					"accion",
					intermedias.collect{it.variable},
					almacenNoSingle.id,
					almacen.variable,
					entidadNoSingle.id,
					entidad.variable,
					indexEntities.collect{it.variable},
					saveEntities.collect{it.variable}
				)});
			}
		"""
	}
	
	private String metodoEditar(){
		if (!editar && !isForm())
			return "";
		String metodoEditar = "";
		String editarRenderCall = "${controllerName}.${nameEditar}Render(${StringUtils.params(intermedias.collect{it.id}, almacenNoSingle.id, entidadNoSingle.id)});";
		String botonCode = "";
		List<String> saveBotones = new ArrayList<String>(saveBoton);
		// Le añadimos los botones de firma
		List<FirmaPlatinoSimple> firmaBotones = new ArrayList<FirmaPlatinoSimple>(firmaBoton);
		for (FirmaPlatinoSimple _firma: firmaBotones) {
			saveBotones.add(_firma.name);
		}
		if (isForm() && saveBotones.size() == 1){
			saveBotones.clear();
			if (firmaBotones.size() > 0) {
				String _name = firmaBotones.get(0).name;
				// Un único boton de firma
				botonCode = """
				${controllerName}.${StringUtils.firstLower(_name)}${sufijoBoton}(${StringUtils.params(
					intermedias.collect{it.id}, almacenNoSingle.id, entidad.id,
					entidadPagina.variable, saveEntities.collect{it.variable}, saveExtra.collect{it.split(" ")[1]}
				)});
				"""
			}
		}
		else if (saveBotones.size() > 0){
			boolean primero = true;
			for (String boton : saveBotones) {
				if (primero == true) {
					botonCode += "if (${boton} != null) {";
					primero = false;
				}
				else
					botonCode += "else if (${boton} != null) {"
				botonCode += """
						${controllerName}.${StringUtils.firstLower(boton)}${sufijoBoton}(${StringUtils.params(
							intermedias.collect{it.id}, almacenNoSingle.id, entidad.id,
							entidadPagina.variable, saveEntities.collect{it.variable}, saveExtra.collect{it.split(" ")[1]}
						)});
						${editarRenderCall}
					}
				""";
			}
		}
		metodoEditar = """
			@Util // Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
			public static void ${nameEditar}(${StringUtils.params(
				intermedias.collect{it.typeId},
				almacenNoSingle.typeId,
				entidadNoSingle.typeId,
				entidadPagina.typeVariable,
				saveEntities.collect{it.typeVariable},
				saveExtra,
				saveBotones.collect{"String ${it}"}
			)}){
				checkAuthenticity();
				if(!permiso${sufijoPermiso}("editar")){
					Messages.error("${permiso?.mensaje? permiso.mensaje : "No tiene permisos suficientes para realizar la acción"}");
				}
				${botonCode}
				${!entidad.nulo()? "${entidad.clase} $entidad.variableDb = ${ControllerUtils.complexGetterCall(controllerName, almacen, entidad)};":""}
				${ControllerUtils.listGetterCall(controllerName, EntidadUtils.create(null), EntidadUtils.create(null), saveEntities)}
				${!entidadPagina.nulo()? ControllerUtils.bindReferencesCall(controllerName, this, entidad, saveEntities) : ""}
	   """;
	    if (editar && !entidadPagina.nulo()){
			metodoEditar += """	
				if(!Messages.hasErrors()){
					${ControllerUtils.validateCopyCall(controllerName, "\"editar\"", this, entidad, saveEntities)}
				}
			""";
		}
		metodoEditar += """	
			if(!Messages.hasErrors()){
				${controllerName}.${nameEditar}ValidateRules(${StringUtils.params(
					entidad.variableDb, saveEntities.collect{it.variableDb}, entidadPagina.variable,
					saveEntities.collect{it.variable}, saveExtra.collect{it.split(" ")[1]}
				)});
			}
			if(!Messages.hasErrors()){
				${entidadPagina.entidad? "${entidad.variableDb}.save();" : ""}
				${saveEntities.collect{"${it.variableDb}.save();"}.join("\n")}
				log.info("Acción Editar de página: "+${renderView}+" , intentada con éxito");
			}
			else log.info("Acción Editar de página: "+${renderView}+" , intentada sin éxito (Problemas de Validación)");
			${editarRenderCall}
		}
		""";
		return metodoEditar;
	}
	
	private String metodoCrearLogica(){
		if (!crear)
			return "";
		String metodoCrearLogica = "";
		AlmacenEntidad ultimoAlmacen;
		if (camposTodos.size() > 0)
			ultimoAlmacen = camposTodos.get(camposTodos.size() - 1);
		else
			ultimoAlmacen = new AlmacenEntidad();
		String crearSaveCall = """
			${entidad.variableDb}.save();
			${entidad.id} = ${entidad.variableDb}.id;
		""";
		if(almacen.entidad != null){
			if (xToMany)
				crearSaveCall += "db${campo.str}.add(${entidad.variableDb});\n";
			else
				crearSaveCall += "db${campo.str} = ${entidad.variableDb};\n";
			crearSaveCall += "${almacen.variableDb}.save();\n";
		}
		metodoCrearLogica = """
			@Util
			public static Long crearLogica(${StringUtils.params(
				intermedias.collect{it.typeId},
				almacenNoSingle.typeId,
				entidadPagina.typeVariable,
				saveEntities.collect{it.typeVariable},
				saveExtra
			)}){
				checkAuthenticity();
				if(!permiso("crear")){
					Messages.error("${permiso?.mensaje? permiso.mensaje : "No tiene permisos suficientes para realizar la acción"}");
				}
				${entidad.clase} ${entidad.variableDb} = null;
				if(!Messages.hasErrors()){
					${entidad.variableDb} = ${ControllerUtils.simpleGetterCall(controllerName, entidad, false)};
				}
				${ControllerUtils.almacenGetterCall(controllerName, ultimoAlmacen)}
				${ControllerUtils.listGetterCall(controllerName, EntidadUtils.create(null), EntidadUtils.create(null), saveEntities)}
				${!entidadPagina.nulo()? ControllerUtils.bindReferencesCall(controllerName, this, entidad, saveEntities) : ""}
		""";
		if (!entidadPagina.nulo()){
			metodoCrearLogica += """
				if(!Messages.hasErrors()){
					${ControllerUtils.validateCopyCall(controllerName, "\"crear\"", this, entidad, saveEntities)}
				}
			""";
		}
		metodoCrearLogica += """
				if(!Messages.hasErrors()){
					${controllerName}.crearValidateRules(${StringUtils.params(
						entidad.variableDb, saveEntities.collect{it.variableDb}, entidadPagina.variable,
						saveEntities.collect{it.variable}, saveExtra.collect{it.split(" ")[1]}
					)});
				}
				${entidad.typeId} = null;
				if(!Messages.hasErrors()){
					log.info("Acción Crear de página: "+${renderView}+" , intentada con éxito");
					$crearSaveCall
					${saveEntities.collect{"${it.variableDb}.save();"}.join("\n")}
				}
				else{
					log.info("Acción Crear de página: "+${renderView}+" , intentada sin éxito (Problemas de Validación)");
				}
				return ${entidad.id};
			}
		""";
		return metodoCrearLogica;
	}
	
	private String metodoCrear(){
		if (!crear)
			return "";
		List<String> saveBotones = new ArrayList<String>(saveBoton);
		saveBotones.addAll(firmaBoton);
		return """
			public static void crear(${StringUtils.params(
				intermedias.collect{it.typeId},
				almacenNoSingle.typeId,
				entidadPagina.typeVariable,
				entidad.typeId, 
				saveEntities.collect{it.typeVariable},
				saveExtra
			)}){
				if (${entidad.id} != null)
					${controllerName}.editar(${StringUtils.params(
						intermedias.collect{it.id},
						almacenNoSingle.id,
						entidadNoSingle.id,
						entidadPagina.variable, 
						saveEntities.collect{it.variable},
						saveExtra.collect{it.split(" ")[1]},
						saveBotones.collect{"null"}
					)});
				else{
					${entidad.id} = ${controllerName}.crearLogica(${StringUtils.params(
						intermedias.collect{it.id},
						almacenNoSingle.id,
						entidadPagina.variable, 
						saveEntities.collect{it.variable},
						saveExtra.collect{it.split(" ")[1]}
					)});
					${controllerName}.crearRender(${StringUtils.params(intermedias.collect{it.id}, almacenNoSingle.id, entidad.id)});
				}
			}
		""";
	}
	
	private String metodoBorrar(){
		if (!borrar)
			return "";
		AlmacenEntidad ultimoAlmacen;
		if (camposTodos.size() > 0)
			ultimoAlmacen = camposTodos.get(camposTodos.size() - 1);
		else
			ultimoAlmacen = new AlmacenEntidad();
		def borrarEntidad = "";
		if(almacen.entidad != null){
			if (xToMany)
				borrarEntidad += "db${campo.str}.remove(${entidad.variableDb});\n";
			else
				borrarEntidad += "db${campo.str} = null;\n";
			borrarEntidad += "${almacen.variableDb}.save();\n";
		}
		if (!noBorrarEntidad) {
			borrarEntidad += """
				${borrarListasXToMany()}
				${entidad.variableDb}.delete();
			""";
		}
		return """
			public static void borrar(${StringUtils.params(intermedias.collect{it.typeId}, almacenNoSingle.typeId, entidadNoSingle.typeId)}){
				checkAuthenticity();
				if(!permiso("borrar")){
					Messages.error("${permiso?.mensaje? permiso.mensaje : "No tiene permisos suficientes para realizar la acción"}");
				}
				${ControllerUtils.almacenGetterCall(controllerName, ultimoAlmacen)}
				${entidad.clase} ${entidad.variableDb} = ${ControllerUtils.complexGetterCall(controllerName, almacen, entidad)};
				if(!Messages.hasErrors()){
					${controllerName}.borrarValidateRules(${StringUtils.params(entidad.variableDb)});
				}
				if(!Messages.hasErrors()){
					log.info("Acción Borrar de página: "+${renderView}+" , intentada con éxito");
					$borrarEntidad
				} else{
					log.info("Acción Borrar de página: "+${renderView}+" , intentada sin éxito (Problemas de Validación)");
				}
				${controllerName}.borrarRender(${StringUtils.params(intermedias.collect{it.id}, almacenNoSingle.id, entidadNoSingle.id)});
			}
		""";
	}
	
	private String metodoEditarRender(){
		if (!editar && !isForm())
			return "";
		String redirectMethod = "\"${controllerFullName}.index\"";
		String redirectMethodOk = redirectMethod;
		String redirectActionOk = "\"editar\"";
		if (accionEditar.redirigir != null){
			redirectMethodOk = "\"${new GPagina(accionEditar.redirigir.pagina).controllerName()}.index\"";
			redirectActionOk = getAccion(accionEditar.redirigir);
		}
		String redirigirOk = "redirect(${StringUtils.params(redirectMethodOk, redirectActionOk, intermedias.collect{it.id}, almacenNoSingle.id, entidadNoSingle.id)});";
		if (accionEditar.redirigirUrl)
			redirigirOk = "redirect(\"${accionEditar.redirigirUrl}\");";
		else if (accionEditar.anterior)
			redirigirOk = redirigirOkCode(redirigirOk);
		String mensajeEditadoOk;
		if (isPopup())
			mensajeEditadoOk = """renderJSON(utils.RestResponse.ok("${accionEditar.mensajeOk}"));""";
		else
			mensajeEditadoOk = """Messages.ok("${accionEditar.mensajeOk}");""";
		return """
			@Util
			public static void ${nameEditar}Render(${StringUtils.params(intermedias.collect{it.typeId}, almacenNoSingle.typeId, entidadNoSingle.typeId)}){
				if (!Messages.hasMessages()) {
					${mensajeEditadoOk}
					Messages.keep();
					${redirigirOk}
				}
				Messages.keep();
				redirect(${StringUtils.params(redirectMethod, "\"editar\"", intermedias.collect{it.id}, almacenNoSingle.id, entidadNoSingle.id)});
			}
		""";
	}

	private String metodoCrearRender(){
		if (!crear)
			return "";
		String redirectMethod = "\"${controllerFullName}.index\"";
		String redirectMethodOk = redirectMethod;
		String redirectActionOk = "\"editar\"";
		if (accionCrear.redirigir != null){
			redirectMethodOk = "\"${new GPagina(accionCrear.redirigir.pagina).controllerName()}.index\"";
			redirectActionOk = getAccion(accionCrear.redirigir);
		}
		String redirigirOk = "redirect(${StringUtils.params(redirectMethodOk, redirectActionOk, intermedias.collect{it.id}, almacenNoSingle.id, entidad.id)});";
		if (accionCrear.redirigirUrl)
			redirigirOk = "redirect(\"${accionCrear.redirigirUrl}\");";
		else if (accionCrear.anterior)
			redirigirOk = redirigirOkCode(redirigirOk);
		String mensajeCreadoOk;
		if (isPagina()){
			mensajeCreadoOk = """
				Messages.ok("${accionCrear.mensajeOk}");
				Messages.keep();
				${redirigirOk}
			""";
		}
		else if (isPopup())
			mensajeCreadoOk = """renderJSON(utils.RestResponse.ok("${accionCrear.mensajeOk}"));""";
		return """
			@Util
			public static void crearRender(${StringUtils.params(intermedias.collect{it.typeId}, almacenNoSingle.typeId, entidad.typeId)}){
				if (!Messages.hasMessages()) {
					${mensajeCreadoOk}
				}
				Messages.keep();
				redirect(${StringUtils.params(redirectMethod, '"crear"', intermedias.collect{it.id}, almacenNoSingle.id, entidad.id)});
			}
		""";
	}
	
	private String metodoBorrarRender(){
		if (!borrar)
			return "";
		String redirectMethod = "\"${controllerFullName}.index\"";
		String redirectMethodOk = redirectMethod;
		String redirectActionOk = "\"borrado\"";
		if (accionBorrar.redirigir != null){
			redirectMethodOk = "\"${new GPagina(accionBorrar.redirigir.pagina).controllerName()}.index\"";
			redirectActionOk = getAccion(accionBorrar.redirigir);
		}
		String redirigirOk = "redirect(${StringUtils.params(redirectMethodOk, redirectActionOk, intermedias.collect{it.id}, almacenNoSingle.id)});";
		if (accionBorrar.redirigirUrl)
			redirigirOk = "redirect(\"${accionBorrar.redirigirUrl}\");";
		else if (accionBorrar.anterior)
			redirigirOk = redirigirOkCode(redirigirOk);
		String mensajeBorradoOk;
		if (isPagina())
			mensajeBorradoOk = """Messages.ok("${accionBorrar.mensajeOk}");""";
		else if (isPopup())
			mensajeBorradoOk = """renderJSON(utils.RestResponse.ok("${accionBorrar.mensajeOk}"));""";
		return """
			@Util
			public static void borrarRender(${StringUtils.params(intermedias.collect{it.typeId}, almacenNoSingle.typeId, entidadNoSingle.typeId)}){
				if (!Messages.hasMessages()) {
					${mensajeBorradoOk}
					Messages.keep();
					${redirigirOk}
				}
				Messages.keep();
				redirect(${StringUtils.params(redirectMethod, '"borrar"', intermedias.collect{it.id}, almacenNoSingle.id, entidadNoSingle.id)});
			}
		""";
	}
		
	private String redirigirOkCode(String redirigirOk){
		return """
			if (request.cookies.containsKey("redirigir${name}"))
				redirect(request.cookies.get("redirigir${name}").value);
			else
				${redirigirOk}
		""";
	} 
	
	private String metodoCheckRedirigir(){
		if (!hayAnterior)
			return "";
		return """
			@Util
			public static void checkRedirigir(){
				renderArgs.put("container", "${name}");
				if (params._contains("redirigir") && !"no".equals(params.get("redirigir"))){
					if ("anterior".equals(params.get("redirigir")) && request.headers.get("referer") != null){
						String referer = request.headers.get("referer").value();
						try {
							URL url = new URL(referer);
							String refererHost = url.getHost();
							if (url.getPort() != -1)
								refererHost += ":" + url.getPort();
							else
								refererHost += ":80";
							String host = request.host;
							if (host.indexOf(":") == -1)
								host += ":80";
							if (refererHost.equals(host))
								response.setCookie("redirigir${name}", referer.replaceFirst("redirigir=anterior", "redirigir=no"));
						} catch (MalformedURLException e) {}
					}
					else{
						response.setCookie("redirigir${name}", params.get("redirigir").replaceFirst("redirigir=anterior", "redirigir=no"));
					}
				}
			}
		""";
	}
	
	private String metodoEditarValidateRules(){
		if (!editar && !isForm())
			return "";
		return """
			@Util
			public static void ${nameEditar}ValidateRules(${StringUtils.params(
				entidad.typeDb,
				saveEntities.collect{it.typeDb},
				entidadPagina.typeVariable,
				saveEntities.collect{it.typeVariable},
				saveExtra
			)}){
				//Sobreescribir para validar las reglas de negocio
			}
		""";
	}
	
	private String metodoCrearValidateRules(){
		if (!crear)
			return "";
		return """
			@Util
			public static void crearValidateRules(${StringUtils.params(
				entidad.typeDb,
				saveEntities.collect{it.typeDb},
				entidadPagina.typeVariable,
				saveEntities.collect{it.typeVariable},
				saveExtra
			)}){
				//Sobreescribir para validar las reglas de negocio
			}
		""";
	}
	
	private String metodoBorrarValidateRules(){
		if (!borrar)
			return "";
		return """
			@Util
			public static void borrarValidateRules(${StringUtils.params(entidad.typeDb)}){
				//Sobreescribir para validar las reglas de negocio
			}
		""";
	}
	
	private String metodoPermiso(){
		String content;
		if (permiso){
			content = """
				if (Accion.parse(accion) == null) return false;
				Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
				return secure.checkAcceso("${permiso.name}", accion, ids, null);
			""";
		}
		else{
			content =  """
				//Sobreescribir para incorporar permisos a mano
				return true;
			""";
		}
		return """
			@Util
			public static boolean permiso${sufijoPermiso}(String accion) {
				${content}
			}
		""";
	}
	
	private String metodoPrimeraAccion(){
		String content;
		if (permiso){
			content = """
				Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
				return secure.getPrimeraAccion("${permiso.name}", ids, null);
			""";
		}
		else{
			content = """
				return "editar";
			""";
		}
		return """
			@Util
			public static String getAccion() {
				${content}
			}
		"""
	}
	
	private String metodosHashStack(){
		String controllerHS = "";
		for(elemento in saveController){
			controllerHS += elemento.controller();
		}
		return controllerHS;
	}
	
	private String beforeMethod(){
		return """
			@Before
			static void beforeMethod() {
				renderArgs.put("controllerName", "${controllerGenName}");
			}
		""";
	}	
	
	private String metodoValidateCopy(){
		if ((editar || crear) && !entidadPagina.nulo()){
			return "${ControllerUtils.validateCopyMethod(this, entidad, saveEntities)}";
		}
		return "";
	}
	
	private String metodoBindReferences(){
		if ((editar || crear) && !entidadPagina.nulo()){
			return "${ControllerUtils.bindReferencesMethod(this, entidad, saveEntities)}";
		}
		return "";
	}
	
	private String botonesMethods(){
		String botonesMethod = "";
		List<String> saveBotones = new ArrayList<String>(saveBoton);
		if (isForm() && saveBotones.size() == 1){
			saveBotones.clear();
		}
		for (String boton: saveBotones){
			botonesMethod += """
				@Util
				public static void ${StringUtils.firstLower(boton)}${sufijoBoton}(${StringUtils.params(
					intermedias.collect{it.typeId},
					almacenNoSingle.typeId,
					entidad.typeId,
					entidadPagina.typeVariable,
					saveEntities.collect{it.typeVariable},
					saveExtra
				)}){
					//Sobreescribir este método para asignar una acción
				}
			""";
		}
		return botonesMethod;
	}
	
	public static String getAccion(PaginaAccion paginaAccion){
		if (paginaAccion.accion)
			return "\"${paginaAccion.accion}\"";
		return "controllers.${paginaAccion.pagina.name}Controller.getAccion()";
	}
	
	private String metodosDeFirma(){		
		String botonesMethod = "";
		List<FirmaPlatinoSimple> firmaBotones = new ArrayList<FirmaPlatinoSimple>(firmaBoton);
		for (FirmaPlatinoSimple _firma: firmaBotones){
			CampoUtils documento = CampoUtils.create(_firma.campo);
			
			String previousCampoFirmantes = """List<Firmante> firmantes = new ArrayList<Firmante>();
				FirmaUtils.calcularFirmantes(solicitud.solicitante, firmantes);
			""";
			String strCampoFirmantes = "firmantes";
			if (_firma.firmantes != null) {
				CampoUtils firmantes = CampoUtils.create(_firma.firmantes);
				strCampoFirmantes = "${firmantes.firstLower()}";
				previousCampoFirmantes = "";
			}
			String strCampoToTrue = "";
			if (_firma.setToTrue != null) {
				CampoUtils campoToTrue = CampoUtils.create(_firma.setToTrue);
				strCampoToTrue = """${campoToTrue.firstLower()} = true;
						${campoToTrue.sinUltimoAtributo()}.save();
				""";
			}
			String strCampoSetTo = "";
			if ((_firma.setCampos != null) && (_firma.setCampos.size() > 0)) {
				for (int i = 0; i < _firma.setCampos.size(); i++) {
					CampoUtils campoSetTo = CampoUtils.create(_firma.setCampos[i]);
					strCampoSetTo += """${campoSetTo.firstLower()} = "${_firma.value[i]}";
						${campoSetTo.sinUltimoAtributo()}.save();
				""";
				}
			}			
			botonesMethod += """
				@Util
				public static void ${StringUtils.firstLower(_firma.name)}${sufijoBoton}(${StringUtils.params(
					intermedias.collect{it.typeId},
					almacenNoSingle.typeId,
					entidad.typeId,
					entidadPagina.typeVariable,
					saveEntities.collect{it.typeVariable},
					saveExtra
				)}){
					${entidad.entidad? "$entidad.entidad.name $entidad.variable = ${ControllerUtils.complexGetterCall(controllerName, almacen, entidad)};" : ""}
					${previousCampoFirmantes}
					FirmaUtils.firmar(${documento.firstLower()}, ${strCampoFirmantes}, firma, null);
					if (!Messages.hasErrors()) {
						${strCampoToTrue}
						${strCampoSetTo}
						${entidad.variable}.save();
					}
				}
			""";
		}
		return botonesMethod;
	}
	
	private static boolean hayAnterior(Object o){
		if(o instanceof Popup || o instanceof Pagina){
			if (o.eContainer().menu){
				if (hayAnterior(o.eContainer().menu))
					return true;
			}
		}
		if(o.metaClass.respondsTo(o,"getElementos")){
			for (Object elemento: o.elementos){
				if (hayAnterior(elemento))
					return true;
			}
			return false;
		}
		if(o instanceof Accion || o instanceof Boton || o instanceof Enlace || o instanceof MenuEnlace){
			if (o.anterior)
				return true;
		}
	}
	
	private String metodosCrearForTablas(){
		if (!crear || !hayTablasDeEntidad(container) || entidad.isSingleton())
			return "";
		return """
			public static void crearForTablas(${StringUtils.params(
				intermedias.collect{it.typeId},
				almacenNoSingle.typeId,
				entidadPagina.typeVariable,
				saveEntities.collect{it.typeVariable},
				saveExtra
			)}){
				${entidad.typeId} = ${controllerName}.crearLogica(${StringUtils.params(
					intermedias.collect{it.id},
					almacenNoSingle.id,
					entidadPagina.variable,
					saveEntities.collect{it.variable},
					saveExtra.collect{it.split(" ")[1]}
				)});
				Map<String, Object> json = new HashMap<String, Object>();
				if (!Messages.hasMessages()) {
					Messages.ok("Página creada correctamente");
					json.put("id", ${entidad.id});
				}
				else{
					json.put("id", "none");
				}
				Messages.keep();
				renderJSON(json);
			}
		""";
	}
	
	private boolean hayTablasDeEntidad(Object o){
		if (o.metaClass.respondsTo(o,"getElementos")){
			for (Object elemento: o.elementos){
				if (hayTablasDeEntidad(elemento))
					return true;
			}
		}
		else if (o instanceof Tabla){
			Tabla tabla = o;
			if (tabla.campo.entidad.name.equals(campo.getUltimaEntidad().name) && (tabla.pagina || tabla.paginaCrear || tabla.popup || tabla.popupCrear))
				return true;
		}
		return false;
	}
	
	public boolean isPopup(){
		return container instanceof Popup;
	}
	
	public boolean isPagina(){
		return container instanceof Pagina;
	}
	
	public boolean isForm(){
		return container instanceof Form;
	}
	
	public static Controller fromPagina(Pagina pagina){
		GPagina pag = new GPagina(pagina);
		Controller controller = new Controller();
		controller.createOpcionesAccion(pagina);
		controller.container = pagina;
		controller.index = true;
		controller.findPaginaReferencias(pagina);
		controller.crear = controller.crear && (pag.hasForm || controller.accionCrear.crearSiempre);
		controller.editar = (controller.editar || controller.crear) && (pag.hasForm || controller.accionEditar.crearSiempre);
		controller.borrar = controller.borrar && (pag.hasForm || controller.accionBorrar.crearSiempre);
		controller.saveController = [];
		controller.campo = pag.campo;
		controller.renderView = "\"gen/${pagina.name}/${pagina.name}.html\"";
		controller.permiso = pagina.permiso;
		if (pagina.permiso == null)
			controller.permiso = pagina.eContainer().permiso;
		controller.controllerGenName = pag.controllerGenName();
		controller.controllerName = pag.controllerName();
		controller.controllerGenFullName = pag.controllerGenFullName();
		controller.controllerFullName = pag.controllerFullName();
		controller.url = pag.url();
		controller.packageName = "controllers";
		controller.packageGenName = "controllers.gen";
		controller.noBorrarEntidad = controller.accionBorrar?.noBorrarEntidad? true : false;
		controller.noAutenticar = pagina.noAutenticar;
		controller.name = pagina.name;
		controller.saveExtra = [];
		controller.saveCode = [];
		controller.saveEntities = [];
		controller.indexEntities = [];
		controller.elementoGramatica = pagina;
		return controller;
	}
	
	public static Controller fromPopup(Popup popup){
		GPopup gpopup = new GPopup(popup);
		Controller controller = new Controller();
		controller.createOpcionesAccion(popup);
		controller.container = popup;
		controller.index = true;
		controller.findPopupReferencias(popup);
		controller.crear = controller.crear || controller.accionCrear.crearSiempre;
		controller.editar = controller.editar || controller.accionEditar.crearSiempre;
		controller.borrar = controller.borrar || controller.accionBorrar.crearSiempre;
		controller.saveController = [];
		controller.campo = gpopup.campo;
		controller.renderView = "\"gen/popups/${gpopup.viewName()}\"";
		controller.permiso = popup.permiso;
		if (popup.permiso == null)
			controller.permiso = popup.eContainer().permiso;
		controller.controllerGenName = gpopup.controllerGenName();
		controller.controllerName = gpopup.controllerName();
		controller.controllerGenFullName = gpopup.controllerGenFullName();
		controller.controllerFullName = gpopup.controllerFullName();
		controller.url = gpopup.url();
		controller.packageName = "controllers.popups";
		controller.packageGenName = "controllers.gen.popups";
		controller.noBorrarEntidad = controller.accionBorrar?.noBorrarEntidad? true : false;
		controller.noAutenticar = false;
		controller.name = popup.name;
		controller.saveExtra = [];
		controller.saveCode = [];
		controller.saveEntities = [];
		controller.indexEntities = [];
		controller.elementoGramatica = popup;
		return controller;
	}
	
	public static Controller fromForm(Form form){
		GForm gform = new GForm(form);
		EObject container = LedCampoUtils.getElementosContainer(form);
		Controller containerController;
		if (container instanceof Pagina)
			containerController = Controller.fromPagina(container);
		else if (container instanceof Popup)
			containerController = Controller.fromPopup(container);
		Controller controller = new Controller();
		controller.createOpcionesAccion(null);
		controller.container = form;
		controller.editar = true;
		controller.saveController = [];
		controller.campo = containerController.campo;
		controller.renderView = containerController.renderView;
		controller.permiso = form.permiso;
		controller.controllerGenName = containerController.controllerGenName;
		controller.controllerName = containerController.controllerName;
		controller.controllerGenFullName = containerController.controllerGenFullName;
		controller.controllerFullName = containerController.controllerFullName;
		controller.url = containerController.url;
		controller.packageName = containerController.packageName;
		controller.packageGenName = containerController.packageGenName;
		controller.noBorrarEntidad = containerController.noBorrarEntidad;
		controller.noAutenticar = containerController.noAutenticar;
		controller.name = StringUtils.firstLower(form.name);
		controller.saveExtra = [];
		controller.saveCode = [];
		controller.saveEntities = [];
		controller.indexEntities = [];
		controller.elementoGramatica = form;
		return controller;
	}
	
	public void createOpcionesAccion(Object o){
		if (o != null)
			findOpcionesAccion(o);
		if (accionCrear == null)
			accionCrear = LedFactory.eINSTANCE.createAccion();
		if (accionCrear.boton == null)
			accionCrear.boton = "Guardar";
		if (accionCrear.mensajeOk == null){
			if (o instanceof Popup)
				accionCrear.mensajeOk = "Registro creado correctamente";
			else
				accionCrear.mensajeOk = "Página creada correctamente";
		}
		if (accionEditar == null)
			accionEditar = LedFactory.eINSTANCE.createAccion();
		if (accionEditar.boton == null)
			accionEditar.boton = "Guardar";
		if (accionEditar.mensajeOk == null){
			if (o instanceof Popup)
				accionEditar.mensajeOk = "Registro actualizado correctamente";
			else
				accionEditar.mensajeOk = "Página editada correctamente";
		}
		if (accionBorrar == null)
			accionBorrar = LedFactory.eINSTANCE.createAccion();
		if (accionBorrar.boton == null)
			accionBorrar.boton = "Borrar";
		if (accionBorrar.mensajeOk == null){
			if (o instanceof Popup)
				accionBorrar.mensajeOk = "Registro borrado correctamente";
			else
				accionBorrar.mensajeOk = "Página borrada correctamente";
		}
	}
	
	private void findOpcionesAccion(Object o){
		if(o.metaClass.respondsTo(o,"getElementos")){
			for (Object elemento: o.elementos)
				findOpcionesAccion(elemento);
		}
		if(o instanceof Accion){
			Accion accion = (Accion)o;
			if ("crear".equals(accion.name))
				accionCrear = accion;
			if ("editar".equals(accion.name))
				accionEditar = accion;
			if ("borrar".equals(accion.name))
				accionBorrar = accion;
		}
	}

	/*
	 * elementoAccion puede ser un PaginaAccion o un PopupAccion
	 */
	public void checkReferencia(def elementoAccion){
		List<String> acciones = new ArrayList<String>();
		if (elementoAccion.accion)
			acciones.add(elementoAccion.accion);
		else{
			Permiso permiso;
			if (elementoAccion instanceof PaginaAccion) permiso = elementoAccion.pagina.permiso;
			if (elementoAccion instanceof PopupAccion) permiso = elementoAccion.popup.permiso;
			if (!permiso) acciones.add("editar");
			else{
				if (permiso.ret)
					acciones.add(GPermiso.getPrimeraAccion(permiso.ret));
				for (PermisoWhen when: permiso.whens)
					acciones.add(GPermiso.getPrimeraAccion(when.ret));
				if (permiso.getElse())
					acciones.add(GPermiso.getPrimeraAccion(permiso.getElse()));
			}
		}
		for (String accion: acciones){
			if ("editar".equals(accion)) editar = true;
			if ("crear".equals(accion)) crear = true;
			if ("borrar".equals(accion)) borrar = true;
		}
	}
		
	public void findPaginaReferencias(Pagina pagina){
		editar = crear = borrar = false;
		for (MenuEnlace enlace: LedUtils.getNodes(LedPackage.Literals.MENU_ENLACE))
			if (enlace.pagina != null && enlace.pagina.pagina.name.equals(pagina.name))
				checkReferencia(enlace.pagina);
		for (Enlace enlace: LedUtils.getNodes(LedPackage.Literals.ENLACE))
			if (enlace.pagina != null && enlace.pagina.pagina.name.equals(pagina.name))
				checkReferencia(enlace.pagina);
		for (Boton boton: LedUtils.getNodes(LedPackage.Literals.BOTON))
			if (boton.pagina != null && boton.pagina.pagina.name.equals(pagina.name))
				checkReferencia(boton.pagina);
		for (Pagina p: LedUtils.getNodes(LedPackage.Literals.PAGINA)){
			Controller c = new Controller();
			c.createOpcionesAccion(pagina);
			if (c.accionCrear.redirigir?.pagina?.name.equals(pagina.name))
				checkReferencia(c.accionCrear.redirigir);
			if (c.accionEditar.redirigir?.pagina?.name.equals(pagina.name))
				checkReferencia(c.accionEditar.redirigir);
			if (c.accionBorrar.redirigir?.pagina?.name.equals(pagina.name))
				checkReferencia(c.accionBorrar.redirigir);
		}
		for (Tabla tabla: LedUtils.getNodes(LedPackage.Literals.TABLA)){
			if (tabla.pagina != null && tabla.pagina.name.equals(pagina.name))
				crear = borrar = editar = true;
			if (tabla.paginaCrear != null && tabla.paginaCrear.name.equals(pagina.name))
				crear = true;
			if (tabla.paginaEditar != null && tabla.paginaEditar.name.equals(pagina.name))
				editar = true;
			if (tabla.paginaBorrar != null && tabla.paginaBorrar.name.equals(pagina.name))
				borrar = true;
		}
		for (Form form: LedUtils.getNodes(LedPackage.Literals.FORM)){
			if (form.redirigir != null && form.redirigir.pagina.name.equals(pagina.name))
				checkReferencia(form.redirigir);
		}
	}
	
	public void findPopupReferencias(Popup popup){
		editar = crear = borrar = false;
		for (MenuEnlace enlace: LedUtils.getNodes(LedPackage.Literals.MENU_ENLACE))
			if (enlace.popup != null && enlace.popup.popup.name.equals(popup.name))
				checkReferencia(enlace.popup);
		for (Enlace enlace: LedUtils.getNodes(LedPackage.Literals.ENLACE))
			if (enlace.popup != null && enlace.popup.popup.name.equals(popup.name))
				checkReferencia(enlace.popup);
		for (Boton boton: LedUtils.getNodes(LedPackage.Literals.BOTON))
			if (boton.popup != null && boton.popup.popup.name.equals(popup.name))
				checkReferencia(boton.popup);
		for (Tabla tabla: LedUtils.getNodes(LedPackage.Literals.TABLA)){
			if (tabla.popup != null && tabla.popup.name.equals(popup.name))
				crear = borrar = editar = true;
			if (tabla.popupCrear != null && tabla.popupCrear.name.equals(popup.name))
				crear = true;
			if (tabla.popupEditar != null && tabla.popupEditar.name.equals(popup.name))
				editar = true;
			if (tabla.popupBorrar != null && tabla.popupBorrar.name.equals(popup.name))
				borrar = true;
		}
	}
	
	public Accion getAccion(String accion){
		if ("editar".equals(accion)) return accionEditar;
		if ("crear".equals(accion)) return accionCrear;
		if ("borrar".equals(accion)) return accionBorrar;
		return null;
	}
	
	/*
	 * Devuelve un String con la llamada a play.mvc.Router.reverse que a su vez retorna la URL asociada a este controlador y a la acción especificada.
	 * @param forTabla	Si es true, en el parámetro que corresponde al ID de la entidad pone un String, debido a que para su uso en tablas, el identificador
	 * de la entidad no se conoce hasta que el usuario selecciona una fila en la tabla.  
	 */
	public String getRouteIndex(String accion, boolean redirigir, boolean forTabla){
		String accionParam = "";
		if (accion) accionParam = "'accion':'${accion}'";
		String almacenId = (almacen.nulo() || almacen.isSingleton())? "" : "'${almacen.id}':${almacen.idCheck}? ${almacen.idNoCheck}:'_${almacen.id}_'";
		String entidadId = "";
		if (!entidad.nulo() && !entidad.isSingleton() && !"crear".equals(accion)){
			if (forTabla){
				entidadId = "'${entidad.id}':'_${entidad.id}_'";
			}
			else{
				if (xToMany)
					entidadId = "'${entidad.id}':${entidad.idCheck}";
				else
					entidadId = "'${entidad.id}':${campo.idWithNullCheck()}";
			}
		}
		String redirigirAnterior = "";
		if (hayAnterior && redirigir)
			redirigirAnterior = "'redirigir': 'anterior'";
		List<String> intermediasStr = intermedias.collect {"'${it.id}':${it.idCheck}? ${it.idNoCheck}:'_${it.id}_'"};
		String params = StringUtils.params(accionParam, almacenId, entidadId, intermediasStr, redirigirAnterior);
		String ids = "";
		if (! params.equals("")) ids = ", [${params}]";
		return """play.mvc.Router.reverse("${controllerFullName}.index" ${ids})""";
	}
	
	public String getRouteIndex(String accion){
		return getRouteIndex(accion, true, false);
	}
	
	public String getRouteAccion(String accion){
		String almacenId = (almacen.nulo() || almacen.isSingleton())? "" : "'${almacen.id}':${almacen.idCheck}";
		String entidadId = "";
		if (!entidad.nulo() && !entidad.isSingleton() && !accion.startsWith("crear")){
			if (xToMany)
				entidadId = "'${entidad.id}':${entidad.idCheck}";
			else
				entidadId = "'${entidad.id}':${campo.idWithNullCheck()}";
		}
		List<String> intermediasStr = intermedias.collect {"'${it.id}':${it.idCheck}"};
		String params = StringUtils.params(almacenId, entidadId, intermediasStr);
		String ids = "";
		if (params != "")
			ids = ", [${params}]";
		return "play.mvc.Router.reverse('${controllerFullName}.${accion}' ${ids})";
	}
	
	private static List<AlmacenEntidad> calcularSubcampos(Campo campo){
		List<AlmacenEntidad> lista = new ArrayList<AlmacenEntidad>();
		if (campo == null || campo.atributos == null) return lista;
		String campoStr = campo.getEntidad().getName();
		EntidadUtils almacen = EntidadUtils.create(campo.getEntidad());
		EntidadUtils almacenAnterior = EntidadUtils.create((Entity)null);
		CampoAtributos atributos = campo.atributos;
		while (atributos != null){
			Attribute attr = atributos.getAtributo();
			campoStr += "." + attr.getName();
			if (LedEntidadUtils.xToMany(attr) || atributos.getAtributos() == null){
				EntidadUtils entidad = EntidadUtils.create(LedEntidadUtils.getEntidad(attr));
				lista.add(new AlmacenEntidad(CampoUtils.create(campoStr), almacenAnterior, almacen, entidad));
				campoStr = entidad.getClase();
				almacenAnterior = almacen;
				almacen = entidad;
			}
			atributos = atributos.getAtributos();
		}
		return lista;
	}
	
	/*
	 * Recorre los atributos de la entidad, y en aquellos que sean OneToMany o ManyToMany,
	 * hace un clear() para vaciar la lista y que Hibernate no cante error al borrar la entidad.
	 */
	private String borrarListasXToMany(){
		String code = "";
		for (Attribute attr: LedEntidadUtils.getAllDirectAttributes(entidad.entidad)){
			if (LedEntidadUtils.xToMany(attr)){
				code += "${entidad.variableDb}.${attr.name}.clear();\n";
			}
		}
		return code;
	}
			
}
