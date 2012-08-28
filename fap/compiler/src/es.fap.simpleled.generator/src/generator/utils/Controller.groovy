package generator.utils;

import es.fap.simpleled.led.Accion
import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Boton
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.CampoAtributos;
import es.fap.simpleled.led.Elemento
import es.fap.simpleled.led.Entity
import es.fap.simpleled.led.FirmaSetCampo
import es.fap.simpleled.led.Form
import es.fap.simpleled.led.FirmaSimple;
import es.fap.simpleled.led.MenuEnlace
import es.fap.simpleled.led.Enlace
import es.fap.simpleled.led.Pagina
import es.fap.simpleled.led.PaginaAccion
import es.fap.simpleled.led.Permiso
import es.fap.simpleled.led.PermisoWhen
import es.fap.simpleled.led.Popup
import es.fap.simpleled.led.PopupAccion
import es.fap.simpleled.led.Tabla
import es.fap.simpleled.led.impl.FormImpl;
import es.fap.simpleled.led.util.ModelUtils
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.LedFactory;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IGenerator

import templates.GBoton;
import templates.GElement
import templates.GFirmaSimple;
import templates.GForm;
import templates.GPagina;
import templates.GPermiso;
import templates.GPopup;
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.led.util.LedCampoUtils;

public class Controller implements Comparator<Entidad>{
	
	static private Map<EObject, Controller> cache;
	
	// Lista de atributos que tienen que recibirse como parámetros
	public EObject element; // Popup, Pagina o Form
	public GElement gElement; // GPopup, GPagina o GForm
	public boolean index;
	public boolean crear;
	public boolean editar;
	public boolean postIndex;
	public boolean borrar;
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
	
	// Lista de atributos que son calculados en esta clase
	private boolean initialized;
	private Set<Entidad> allEntities;
	private Set<Entidad> saveDbEntities;
	private Set<Entidad> saveEntities;
	private Set<Entidad> dbEntities;
	private List<String> extraParams;
	private Entidad almacen;
	private Entidad entidad;
	private boolean hayAnterior;
	private String nameEditar;
	private String sufijoPermiso;
	private String sufijoBoton;
	private CampoUtils lastSubcampo;
	private Accion accionCrear;
	private Accion accionEditar;
	private Accion accionBorrar;
	private String strProcesandoMethods = "";
	
	public Controller initialize(){
		saveEntities = gElement.saveEntities();
		dbEntities = gElement.dbEntities();
		extraParams = gElement.extraParams();
		
		List<EntidadInfo> subcampos = CampoUtils.calcularSubcampos(campo?.campo);
		if (subcampos.size() > 0){
			EntidadInfo last = subcampos.get(subcampos.size() - 1);
			entidad = last.entidad;
			almacen = last.almacen;
			lastSubcampo = last.campo;
		}
		else{
			entidad = Entidad.create((Entity) null);
			almacen = Entidad.create((Entity) null);
		}
		hayAnterior = hayAnterior(element);
		
		nameEditar = "editar";
		sufijoPermiso = "";
		sufijoBoton = "";
		if (isForm()){
			nameEditar = name;
			sufijoPermiso = StringUtils.firstUpper(name);
			sufijoBoton = StringUtils.firstUpper(name);
		}
		allEntities = new TreeSet<Entidad>(this);
		if (subcampos.size() > 0 && !subcampos.get(0).almacen.nulo())
			allEntities.add(subcampos.get(0).almacen);
		for (EntidadInfo subcampo: subcampos)
			allEntities.add(subcampo.entidad);
		allEntities.addAll(saveEntities);
		allEntities.addAll(dbEntities);
		saveDbEntities = new TreeSet<Entidad>(this);
		saveDbEntities.addAll(saveEntities);
		saveDbEntities.addAll(dbEntities);
		
		initialized = true;
		return this;
	}
	
	public Set<String> getMyAllEntities () {
		return allEntities;
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
import security.ResultadoPermiso;
import java.util.Arrays;
import properties.FapProperties;
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

${metodoValidateCopyBeforeOpenPageTable()}

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

${metodosGetters()}

${metodoCheckRedirigir()}

${metodosControllerElementos()}

${metodoBefore()}

${metodoProcesandoEntidades()}

}
"""
		FileUtils.overwrite(FileUtils.getRoute('CONTROLLER_GEN'),controllerGenFullName.replaceAll("\\.", "/") + ".java", BeautifierUtils.formatear(controllerGen));
			
		String controller = """
package ${packageName};

import ${packageGenName}.${controllerGenName};
			
public class ${controllerName} extends ${controllerGenName} {

}
		"""
		FileUtils.write(FileUtils.getRoute('CONTROLLER'), controllerFullName.replaceAll("\\.", "/") + ".java", BeautifierUtils.formatear(controller));
	}
	
	public String generateRoutes(){
		StringBuffer sb = new StringBuffer();
		if (index)
			StringUtils.appendln sb, RouteUtils.to("GET", url, controllerFullName + ".index");
		if (index && postIndex)
			StringUtils.appendln sb, RouteUtils.to("POST", url, controllerFullName + ".index");
		if (editar || isForm())
			StringUtils.appendln sb, RouteUtils.to("POST", url + "/${nameEditar}", controllerFullName + ".${nameEditar}");
		if (borrar)
			StringUtils.appendln sb, RouteUtils.to("POST", url + "/borrar", controllerFullName + ".borrar");
		if (crear)
			StringUtils.appendln sb, RouteUtils.to("POST", url + "/crear", controllerFullName + ".crear");
		if (hayTablasDeEntidad(element))
			StringUtils.appendln sb, RouteUtils.to("POST", url + "/beforeOpenPageTable", controllerFullName + ".beforeOpenPageTable");
		
		String routes = sb.toString();
		for(Elemento elemento : element.getElementos())
			routes += gElement.getInstance(elemento).routes();
		return routes;
	}
	
	private String metodosGetters(){
		String getters = "";
		for (Entidad entity: allEntities)
			getters += complexGetter(entity);
		if (!entidad.isSingleton())
			getters += simpleGetter(entidad, false); // get sin parametros, que dentro hace un new()
		
		return getters;
	}
	
	private String metodosGettersForm(Controller c){
		String getters = "";
		for (Entidad entity: saveDbEntities){
			if (!c.allEntities.contains(entity))
				getters += complexGetter(entity);
		}
		return getters;
	}
	
	private String metodoIndex(){
		
		String crearSaveCall = """
			${entidad.variable}.save();
			${entidad.id} = ${entidad.variable}.id;
		""";
		if(!almacen.nulo()){
			String lastSubcampoMinuscula = lastSubcampo.str.substring(0,1).toLowerCase()+lastSubcampo.str.substring(1);
			if (LedCampoUtils.xToMany(lastSubcampo?.campo))
				crearSaveCall += "${lastSubcampoMinuscula}.add(${entidad.variable});\n";
			else
				crearSaveCall += "${lastSubcampoMinuscula} = ${entidad.variable};\n";
			// Recorre el ${lastSubcampo.str}, y voy comprobando que cada elemento (cortado por '.') es ManyToOne, en ese caso hago su save, ya que ahora por defecto FAP no pone Cascade a los ManyToOne, por lo que no se guardaria y fallaria.
			String[] camposToSaveIfManyToOne = lastSubcampoMinuscula.split("\\.");
			String campoConcatenado=camposToSaveIfManyToOne[0];
			for(int i=1; i<camposToSaveIfManyToOne.size(); i++){
				campoConcatenado+="."+camposToSaveIfManyToOne[i];
				CampoUtils campo = CampoUtils.create((campoConcatenado.substring(0,1).toUpperCase()+campoConcatenado.substring(1)));
				if (LedCampoUtils.ManyToOne(campo.campo)){
					crearSaveCall += "${campoConcatenado}.save();\n";
				}
			}
			crearSaveCall += "${almacen.variable}.save();\n";
		}
		
		
		String getEntidad = "";
		if (!entidad.nulo()){
			if (!entidad.isSingleton()){
				getEntidad = """
					$entidad.clase $entidad.variable = null;
					if("crear".equals(accion)){
						$entidad.variable = ${simpleGetterCall(entidad, false)};
						if (properties.FapProperties.getBoolean("fap.entidades.guardar.antes")){
							${crearSaveCall}
							accion="editar";
						}
					} else if (!"borrado".equals(accion))
						$entidad.variable = ${complexGetterCall(entidad)};
				""";
			}
			else
				getEntidad = """$entidad.clase $entidad.variable = ${simpleGetterCall(entidad, false)};""";
		}
		return """
			public static void index(${StringUtils.params("String accion", allEntities.collect{it.typeId})}){
				if (accion == null)
					accion = getAccion();
				if (!permiso(accion)){
					Messages.fatal("${permiso?.mensaje? permiso.mensaje : "No tiene permisos suficientes para realizar esta acción"}");
					renderTemplate(${renderView});
				}
				${hayAnterior? "checkRedirigir();" : ""}
				${allEntities.collect{
					if (it != entidad)
						return "$it.clase $it.variable = ${complexGetterCall(it)};"
					else return "";
				}.join("\n")}
				${getEntidad}
				log.info("Visitando página: "+${renderView});
				renderTemplate(${StringUtils.params(
					renderView,
					"accion",
					allEntities.collect{it.id},
					allEntities.collect{it.variable}
				)});
			}
		""";
	}
	
	private String metodoEditar(){
		if (!editar && !crear && !isForm())
			return "";
		
		List<GBoton> botones = gElement.getInstancesOf(GBoton.class);
		List<GFirmaSimple> botonesFirma = gElement.getInstancesOf(GFirmaSimple.class);
			
		String metodoEditar = "";
		String editarRenderCall = "${controllerName}.${nameEditar}Render(${StringUtils.params(allEntities.collect{it.id})});";
		String botonCode = "";
		for (GBoton boton: botones) {
			botonCode += """
				if (${boton.boton.name} != null){
					${controllerName}.${StringUtils.firstLower(boton.boton.name)}${sufijoBoton}(${StringUtils.params(
						allEntities.collect{it.id}, saveEntities.collect{it.variable}, extraParams.collect{it.split(" ")[1]}
					)});
					${editarRenderCall}
				}
			""";
		}
		for (GFirmaSimple boton: botonesFirma) {
			botonCode += """
				if (${boton.firmaSimple.name} != null){
					${controllerName}.${StringUtils.firstLower(boton.firmaSimple.name)}${sufijoBoton}(${StringUtils.params(
						allEntities.collect{it.id}, extraParams.collect{it.split(" ")[1]}
					)});
					${editarRenderCall}
				}
			""";
		}
		if (isForm() && botones.size() == 1 && botonesFirma.size() == 0)
			botonCode = "";
		metodoEditar = """
			@Util // Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
			public static void ${nameEditar}(${StringUtils.params(
				allEntities.collect{it.typeId},
				saveEntities.collect{it.typeVariable},
				extraParams,
				botones.collect{"String ${it.boton.name}"},
				botonesFirma.collect{"String ${it.firmaSimple.name}"}
			)}){
				checkAuthenticity();
				if(!permiso${sufijoPermiso}("editar")){
					Messages.error("${permiso?.mensaje? permiso.mensaje : "No tiene permisos suficientes para realizar la acción"}");
				}
				${saveDbEntities.collect{"$it.clase $it.variableDb = ${complexGetterCall(it)};"}.join("\n")}
				${saveEntities.size() > 0? bindReferencesCall() : ""}
				${botonCode}
	   """;
	    if (editar){
			metodoEditar += """	
				if(!Messages.hasErrors()){
					${validateCopyCall("\"editar\"")}
				}
			""";
		}
		metodoEditar += """
			if(!Messages.hasErrors()){
				${controllerName}.${nameEditar}ValidateRules(${StringUtils.params(
					saveEntities.collect{it.variableDb},
					saveEntities.collect{it.variable},
					extraParams.collect{it.split(" ")[1]}
				)});
			}
			if(!Messages.hasErrors()){
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
		entidad.singletonsId = true;
		String metodoCrearLogica = "";
		String crearSaveCall = """
			${entidad.variableDb}.save();
			${entidad.id} = ${entidad.variableDb}.id;
		""";
		if(!almacen.nulo()){
			if (LedCampoUtils.xToMany(lastSubcampo?.campo))
				crearSaveCall += "db${lastSubcampo.str}.add(${entidad.variableDb});\n";
			else
				crearSaveCall += "db${lastSubcampo.str} = ${entidad.variableDb};\n";
			// Recorre el ${lastSubcampo.str}, y voy comprobando que cada elemento (cortado por '.') es ManyToOne, en ese caso hago su save, ya que ahora por defecto FAP no pone Cascade a los ManyToOne, por lo que no se guardaria y fallaria.
			String[] camposToSaveIfManyToOne = lastSubcampo.str.split("\\.");
			String campoConcatenado=camposToSaveIfManyToOne[0];
			for(int i=1; i<camposToSaveIfManyToOne.size(); i++){
				campoConcatenado+="."+camposToSaveIfManyToOne[i];
				CampoUtils campo = CampoUtils.create(campoConcatenado);
				if (LedCampoUtils.ManyToOne(campo.campo)){
					crearSaveCall += "db${campoConcatenado}.save();\n";
				}
			}
			crearSaveCall += "${almacen.variableDb}.save();\n";
		}
		metodoCrearLogica = """
			@Util
			public static Long crearLogica(${StringUtils.params(
				allEntities.collect{if (it != entidad) it.typeId},
				saveEntities.collect{it.typeVariable},
				extraParams
			)}){
				checkAuthenticity();
				if(!permiso("crear")){
					Messages.error("${permiso?.mensaje? permiso.mensaje : "No tiene permisos suficientes para realizar la acción"}");
				}
				$entidad.clase $entidad.variableDb = ${simpleGetterCall(entidad, false)};
				${almacen.nulo()? "": "$almacen.clase $almacen.variableDb = ${complexGetterCall(almacen)};"}
				${saveDbEntities.collect{
					if (!it.equals(entidad) && !it.equals(almacen))
						return "$it.clase $it.variableDb = ${complexGetterCall(it)};";
					else return "";
				}.join("\n")}
				${saveEntities.size() > 0? bindReferencesCall() : ""}
		""";
		if (saveEntities.size() > 0){
			metodoCrearLogica += """
				if(!Messages.hasErrors()){
					${validateCopyCall("\"crear\"")}
				}
			""";
		}
		metodoCrearLogica += """
				if(!Messages.hasErrors()){
					${controllerName}.crearValidateRules(${StringUtils.params(
						saveEntities.collect{it.variableDb},
						saveEntities.collect{it.variable},
						extraParams.collect{it.split(" ")[1]}
					)});
				}
				${entidad.typeId} = null;
				if(!Messages.hasErrors()){
					$crearSaveCall
					${saveEntities.collect{
						if (!it.equals(entidad))
							return "${it.variableDb}.save();";
						else return "";
					}.join("\n")}
					log.info("Acción Crear de página: "+${renderView}+" , intentada con éxito");
				}
				else{
					log.info("Acción Crear de página: "+${renderView}+" , intentada sin éxito (Problemas de Validación)");
				}
				return ${entidad.id};
			}
		""";
		entidad.singletonsId = false;
		return metodoCrearLogica;
	}
	
	private String metodoCrear(){
		if (!crear)
			return "";
		
		String crearLogica = """
			${entidad.isSingleton()? "": "${entidad.id} = "}${controllerName}.crearLogica(${StringUtils.params(
				allEntities.collect{if (it != entidad) it.id},
				saveEntities.collect{it.variable},
				extraParams.collect{it.split(" ")[1]}
			)});
			${controllerName}.crearRender(${StringUtils.params(allEntities.collect{it.id})});
		""";
		String crearCabecera = """
			public static void crear(${StringUtils.params(
				allEntities.collect{it.typeId},
				saveEntities.collect{it.typeVariable},
				extraParams
			)}){
		""";
		if (entidad.isSingleton()){
			return """
				${crearCabecera}
					${crearLogica}
				}
			""";
		}
		return """
			${crearCabecera}
				if (${entidad.id} != null)
					${controllerName}.editar(${StringUtils.params(
						allEntities.collect{it.id},
						saveEntities.collect{it.variable},
						extraParams.collect{it.split(" ")[1]},
						gElement.getInstancesOf(GBoton.class).collect{"null"},
						gElement.getInstancesOf(GFirmaSimple.class).collect{"null"}
					)});
				else{
					${crearLogica}
				}
			}
		""";
	}
	
	private String metodoBorrar(){
		if (!borrar)
			return "";
		def borrarEntidad = "";
		if (!almacen.nulo()){
			if (LedCampoUtils.xToMany(lastSubcampo?.campo))
				borrarEntidad += "db${lastSubcampo.str}.remove(${entidad.variableDb});\n";
			else
				borrarEntidad += "db${lastSubcampo.str} = null;\n";
			borrarEntidad += "${almacen.variableDb}.save();\n";
		}
		if (!noBorrarEntidad) {
			borrarEntidad += """
				${borrarListasXToMany()}
				${entidad.variableDb}.delete();
			""";
		}
		return """
			public static void borrar(${StringUtils.params(allEntities.collect{it.typeId})}){
				checkAuthenticity();
				if(!permiso("borrar")){
					Messages.error("${permiso?.mensaje? permiso.mensaje : "No tiene permisos suficientes para realizar la acción"}");
				}
				$entidad.clase $entidad.variableDb = ${complexGetterCall(entidad)};
				${almacen.nulo()? "": "$almacen.clase $almacen.variableDb = ${complexGetterCall(almacen)};"}
				if(!Messages.hasErrors()){
					${controllerName}.borrarValidateRules(${StringUtils.params(entidad.variableDb)});
				}
				if(!Messages.hasErrors()){
					$borrarEntidad
					log.info("Acción Borrar de página: "+${renderView}+" , intentada con éxito");
				} else{
					log.info("Acción Borrar de página: "+${renderView}+" , intentada sin éxito (Problemas de Validación)");
				}
				${controllerName}.borrarRender(${StringUtils.params(allEntities.collect{it.id})});
			}
		""";
	}
	
	private String metodoEditarRender(){
		if (!editar && !crear && !isForm())
			return "";
		String redirectMethod = "\"${controllerFullName}.index\"";
		String redirectMethodOk = redirectMethod;
		String redirectActionOk = "\"editar\"";
		if (accionEditar.redirigir != null){
			redirectMethodOk = "\"${GElement.getInstance(accionEditar.redirigir.pagina, null).controllerName()}.index\"";
			redirectActionOk = getAccion(accionEditar.redirigir);
		}
		String redirigirOk = "redirect(${StringUtils.params(redirectMethodOk, redirectActionOk, allEntities.collect{it.id})});";
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
			public static void ${nameEditar}Render(${StringUtils.params(allEntities.collect{it.typeId})}){
				if (!Messages.hasMessages()) {
					${mensajeEditadoOk}
					Messages.keep();
					${redirigirOk}
				}
				Messages.keep();
				redirect(${StringUtils.params(redirectMethod, "\"editar\"", allEntities.collect{it.id})});
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
			redirectMethodOk = "\"${GElement.getInstance(accionCrear.redirigir.pagina, null).controllerName()}.index\"";
			redirectActionOk = getAccion(accionCrear.redirigir);
		}
		String redirigirOk = "redirect(${StringUtils.params(redirectMethodOk, redirectActionOk, allEntities.collect{it.id})});";
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
			public static void crearRender(${StringUtils.params(allEntities.collect{it.typeId})}){
				if (!Messages.hasMessages()) {
					${mensajeCreadoOk}
				}
				Messages.keep();
				redirect(${StringUtils.params(redirectMethod, '"crear"', allEntities.collect{it.id})});
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
			redirectMethodOk = "\"${GElement.getInstance(accionBorrar.redirigir.pagina, null).controllerName()}.index\"";
			redirectActionOk = getAccion(accionBorrar.redirigir);
		}
		String redirigirOk = "redirect(${StringUtils.params(redirectMethodOk, redirectActionOk, allEntities.collect{if (entidad != it) it.id})});";
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
			public static void borrarRender(${StringUtils.params(allEntities.collect{it.typeId})}){
				if (!Messages.hasMessages()) {
					${mensajeBorradoOk}
					Messages.keep();
					${redirigirOk}
				}
				Messages.keep();
				redirect(${StringUtils.params(redirectMethod, '"borrar"', allEntities.collect{it.id})});
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
		if (!editar && !crear && !isForm())
			return "";
		return """
			@Util
			public static void ${nameEditar}ValidateRules(${StringUtils.params(
				saveEntities.collect{it.typeDb},
				saveEntities.collect{it.typeVariable},
				extraParams
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
				saveEntities.collect{it.typeDb},
				saveEntities.collect{it.typeVariable},
				extraParams
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
	
	private String metodosControllerElementos(){
		String controllers = "";
		for(Elemento elemento : element.getElementos()) {
			if ((element instanceof Boton) || (element instanceof FirmaSimple)) {
				strProcesandoMethods += ", \"${controllerName}."+elemento.name+"\", \"${controllerGenName}."+elemento.name+"\"";
			}
			if ((elemento instanceof Tabla)) {
				controllers += gElement.getInstance(elemento).controllerWithParams(allEntities);
			} else {
				controllers += gElement.getInstance(elemento).controller();
			}
		}
		return controllers;
	}
	
	private String metodoBefore(){
		return """
			@Before
			static void beforeMethod() {
				renderArgs.put("controllerName", "${controllerGenName}");
			}
		""";
	}	
	
	public String bindReferencesCall(){
		if (saveEntities.size() == 0) return "";
		return """
			${controllerName}.${name}BindReferences(${StringUtils.params(
				saveEntities.collect{ it.variable },
				extraParams.collect{it.split(" ")[1]}.unique()
			)});
		""";
	}
	
	public String validateCopyCall(String accion){
		if (saveEntities.size() == 0) return "";
		return """
			${controllerName}.${name}ValidateCopy(${StringUtils.params(
				accion,
				saveDbEntities.collect{
					if (saveEntities.contains(it)) return "${it.variableDb}, ${it.variable}";
					else return "${it.variableDb}";
				},	
				extraParams.collect{it.split(" ")[1]}.unique())}
			);
		""";
	}
	
	public String metodoValidateCopy(){
		if (saveEntities.size() == 0 || (!editar && !crear)) return "";
		return """
			@Util
			public static void ${name}ValidateCopy(String accion, ${StringUtils.params(
				saveDbEntities.collect{
					if (saveEntities.contains(it)) return "${it.typeDb}, ${it.typeVariable}";
					else return "${it.typeDb}";
				},
				extraParams
			)}){
				CustomValidation.clearValidadas();
				${gElement.validateCopy()}
				${gElement.saveCode()}
			}
		"""
	}
	
	public boolean algoQueGuardar(){
		if (saveEntities.size() == 0 || (!editar && !crear && !borrar) || (!hayTablasDeEntidad(element))) 
			return false;
		return true;
	}
	
	public String metodoValidateCopyBeforeOpenPageTable(){
		if (saveEntities.size() == 0 || (!editar && !crear && !borrar) || (!hayTablasDeEntidad(element))) 
			return "";
		String redirectMethod = "\"${controllerFullName}.index\"";
		return """
			@Util
			public static void beforeOpenPageTable(String accion, String irDespuesDeValidar, ${StringUtils.params(
				allEntities.collect{it.typeId},
				saveDbEntities.collect{
					if (saveEntities.contains(it)) return "${it.typeVariable}";
				},
				extraParams
			)}){
				CustomValidation.clearValidadas();
				${saveDbEntities.collect{"$it.clase $it.variableDb = ${complexGetterCall(it)};"}.join("\n")}
				${gElement.validateCopy()}

				${gElement.saveCode()}

				if(!Messages.hasErrors()){
					${saveEntities.collect{"${it.variableDb}.save();"}.join("\n")}
					redirect(irDespuesDeValidar.replaceAll("@", "&"));
				} else {
					Messages.keep();
					redirect(${StringUtils.params(redirectMethod, "\"editar\"", allEntities.collect{it.id})});
				}
			}
		"""
	}
	
	public String metodoBindReferences(){
		if ((!editar && !crear) || saveEntities.size() == 0)
			return "";
		return """
			@Util
			public static void ${name}BindReferences(${StringUtils.params(
				saveEntities.collect{it.typeVariable}, extraParams)}
			){
				${gElement.bindReferences()}
			}
		""";
	}
	
	public static String getAccion(PaginaAccion paginaAccion){
		if (paginaAccion.accion)
			return "\"${paginaAccion.accion}\"";
		return "controllers.${paginaAccion.pagina.name}Controller.getAccion()";
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
	
	public String metodosCrearForTablas(){
		if (!crear || !hayTablasDeEntidad(element) || entidad.isSingleton())
			return "";
		return """
			public static void crearForTablas(${StringUtils.params(
				allEntities.collect{if (entidad != it) it.typeId},
				saveEntities.collect{it.typeVariable},
				extraParams
			)}){
				${entidad.typeId} = ${controllerName}.crearLogica(${StringUtils.params(
					allEntities.collect{if (entidad != it) it.id},
					saveEntities.collect{it.variable},
					extraParams.collect{it.split(" ")[1]}
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
		if (o == null)
			return false;
		if (o.metaClass.respondsTo(o,"getElementos")){
			for (Object elemento: o.elementos){
				if (hayTablasDeEntidad(elemento))
					return true;
			}
		}
		else if (o instanceof Tabla){
			Tabla tabla = o;
			if ((lastSubcampo != null) && tabla.campo.entidad.name.equals(lastSubcampo.getUltimaEntidad().name) && (tabla.pagina || tabla.paginaCrear || tabla.popup || tabla.popupCrear))
				return true;
		}
		return false;
	}
	
	public boolean isPopup(){
		return element instanceof Popup;
	}
	
	public boolean isPagina(){
		return element instanceof Pagina;
	}
	
	public boolean isForm(){
		return element instanceof Form;
	}
	
	private static Controller getFromCache(EObject container){
		if (cache == null)
			cache = new HashMap<EObject, Controller>();
		if (cache.get(container) == null)
			cache.put(container, new Controller());
		return cache.get(container);
	}
	
	public static Controller create(GPagina gpag){
		Controller controller = getFromCache(gpag.pagina);
		if (controller.initialized)
			return controller;
		
		controller.createOpcionesAccion(gpag.pagina);
		controller.gElement = gpag;
		controller.element = gpag.pagina;
		controller.index = true;
		controller.findPaginaReferencias(gpag.pagina);
		controller.findTablasReferencias(gpag.pagina);
		controller.crear = gpag.hasForm && (controller.crear || controller.accionCrear.crearSiempre);
		controller.editar = gpag.hasForm && (controller.editar || controller.crear || controller.accionEditar.crearSiempre);
		controller.borrar = gpag.hasForm && (controller.borrar || controller.accionBorrar.crearSiempre);
		controller.campo = gpag.campo;
		controller.renderView = "\"gen/${gpag.pagina.name}/${gpag.pagina.name}.html\"";
		controller.permiso = gpag.pagina.permiso;
		if (gpag.pagina.permiso == null)
			controller.permiso = gpag.pagina.eContainer().permiso;
		controller.controllerGenName = gpag.controllerGenName();
		controller.controllerName = gpag.controllerName();
		controller.controllerGenFullName = gpag.controllerGenFullName();
		controller.controllerFullName = gpag.controllerFullName();
		controller.url = gpag.url();
		controller.packageName = "controllers";
		controller.packageGenName = "controllers.gen";
		controller.noBorrarEntidad = controller.accionBorrar?.noBorrarEntidad? true : false;
		controller.noAutenticar = gpag.pagina.noAutenticar;
		controller.name = gpag.pagina.name;
		controller.initialize();
		return controller;
	}
	
	public static Controller create(GPopup gpop){
		Controller controller = getFromCache(gpop.popup);
		if (controller.initialized)
			return controller;
		
		controller.createOpcionesAccion(gpop.popup);
		controller.gElement = gpop;
		controller.element = gpop.popup;
		controller.index = true;
		controller.findPopupReferencias(gpop.popup);
		controller.crear = controller.crear || controller.accionCrear.crearSiempre;
		controller.editar = controller.editar || controller.accionEditar.crearSiempre;
		controller.borrar = controller.borrar || controller.accionBorrar.crearSiempre;
		controller.campo = gpop.campo;
		controller.renderView = "\"gen/popups/${gpop.viewName()}\"";
		controller.permiso = gpop.popup.permiso;
		if (gpop.popup.permiso == null)
			controller.permiso = gpop.popup.eContainer().permiso;
		controller.controllerGenName = gpop.controllerGenName();
		controller.controllerName = gpop.controllerName();
		controller.controllerGenFullName = gpop.controllerGenFullName();
		controller.controllerFullName = gpop.controllerFullName();
		controller.url = gpop.url();
		controller.packageName = "controllers.popups";
		controller.packageGenName = "controllers.gen.popups";
		controller.noBorrarEntidad = controller.accionBorrar?.noBorrarEntidad? true : false;
		controller.noAutenticar = false;
		controller.name = gpop.popup.name;
		controller.initialize();
		return controller;
	}
	
	public static Controller create(GForm gform){
		Controller controller = getFromCache(gform.form);
		if (controller.initialized)
			return controller;

		Controller containerController = create(gform.getPaginaOrPopupContainer());
		controller.createOpcionesAccion(null);
		controller.gElement = gform;
		controller.element = gform.form;
		controller.editar = true;
		controller.campo = containerController.campo;
		controller.renderView = containerController.renderView;
		controller.permiso = gform.form.permiso;
		controller.controllerGenName = containerController.controllerGenName;
		controller.controllerName = containerController.controllerName;
		controller.controllerGenFullName = containerController.controllerGenFullName;
		controller.controllerFullName = containerController.controllerFullName;
		controller.url = containerController.url;
		controller.packageName = containerController.packageName;
		controller.packageGenName = containerController.packageGenName;
		controller.noBorrarEntidad = containerController.noBorrarEntidad;
		controller.noAutenticar = containerController.noAutenticar;
		controller.name = StringUtils.firstLower(gform.form.name);
		controller.initialize();
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
			if (elementoAccion instanceof PaginaAccion) permiso = (Permiso)elementoAccion.pagina.permiso;
			if (elementoAccion instanceof PopupAccion) permiso = (Permiso)elementoAccion.popup.permiso;
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
		for (MenuEnlace enlace: LedUtils.getNodes(LedFactory.eINSTANCE.getLedPackage().getMenuEnlace())){
			if (enlace.pagina != null && enlace.pagina.pagina.name.equals(pagina.name))
				checkReferencia(enlace.pagina);
		}
		for (Enlace enlace: LedUtils.getNodes(LedFactory.eINSTANCE.getLedPackage().getEnlace())){
			if (enlace.pagina != null && enlace.pagina.pagina.name.equals(pagina.name))
				checkReferencia(enlace.pagina);
		}
		for (Boton boton: LedUtils.getNodes(LedFactory.eINSTANCE.getLedPackage().getBoton())){
			if (boton.pagina != null && boton.pagina.pagina.name.equals(pagina.name))
				checkReferencia(boton.pagina);
		}
		for (Accion accion: LedUtils.getNodes(LedFactory.eINSTANCE.getLedPackage().getAccion())){
			if (accion.redirigir != null && accion.redirigir.pagina.name.equals(pagina.name))
				checkReferencia(accion.redirigir);
		}
		for (Pagina p: LedUtils.getNodes(LedFactory.eINSTANCE.getLedPackage().getPagina())){
			Controller c = new Controller();
			c.createOpcionesAccion(pagina);
			if (c.accionCrear.redirigir?.pagina?.name.equals(pagina.name))
				checkReferencia(c.accionCrear.redirigir);
			if (c.accionEditar.redirigir?.pagina?.name.equals(pagina.name))
				checkReferencia(c.accionEditar.redirigir);
			if (c.accionBorrar.redirigir?.pagina?.name.equals(pagina.name))
				checkReferencia(c.accionBorrar.redirigir);
		}
		for (Tabla tabla: LedUtils.getNodes(LedFactory.eINSTANCE.getLedPackage().getTabla())){
			if (tabla.pagina != null && tabla.pagina.name.equals(pagina.name))
				crear = borrar = editar = true;
			if (tabla.paginaCrear != null && tabla.paginaCrear.name.equals(pagina.name))
				crear = true;
			if (tabla.paginaEditar != null && tabla.paginaEditar.name.equals(pagina.name))
				editar = true;
			if (tabla.paginaBorrar != null && tabla.paginaBorrar.name.equals(pagina.name))
				borrar = true;
		}
		for (Form form: LedUtils.getNodes(LedFactory.eINSTANCE.getLedPackage().getForm())){
			if (form.redirigir != null && form.redirigir.pagina.name.equals(pagina.name))
				checkReferencia(form.redirigir);
		}
	}
	
	public void findTablasReferencias(Pagina pagina){
		postIndex = false;
		
		for (Tabla tabla: LedUtils.getNodes(LedFactory.eINSTANCE.getLedPackage().getTabla())){
			if (tabla.pagina != null && tabla.pagina.name.equals(pagina.name))
				postIndex = true;
			else if (tabla.paginaCrear != null && tabla.paginaCrear.name.equals(pagina.name))
				postIndex = true;
			else if (tabla.paginaEditar != null && tabla.paginaEditar.name.equals(pagina.name))
				postIndex = true;
			else if (tabla.paginaBorrar != null && tabla.paginaBorrar.name.equals(pagina.name))
				postIndex = true;
			else if (tabla.paginaLeer != null && tabla.paginaLeer.name.equals(pagina.name))
				postIndex = true;
		}

	}
	
	public void findPopupReferencias(Popup popup){
		editar = crear = borrar = false;
		for (MenuEnlace enlace: LedUtils.getNodes(LedFactory.eINSTANCE.getLedPackage().getMenuEnlace())){
			if (enlace.popup != null && enlace.popup.popup.name.equals(popup.name))
				checkReferencia(enlace.popup);
		}
		for (Enlace enlace: LedUtils.getNodes(LedFactory.eINSTANCE.getLedPackage().getEnlace())){
			if (enlace.popup != null && enlace.popup.popup.name.equals(popup.name))
				checkReferencia(enlace.popup);
		}
		for (Boton boton: LedUtils.getNodes(LedFactory.eINSTANCE.getLedPackage().getBoton())){
			if (boton.popup != null && boton.popup.popup.name.equals(popup.name))
				checkReferencia(boton.popup);
		}
		for (Tabla tabla: LedUtils.getNodes(LedFactory.eINSTANCE.getLedPackage().getTabla())){
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
		List<String> idParams = allEntities.collect {
			if (it.isSingleton() || (entidad.equals(it) && "crear".equals(accion)))
				return "";
			EntidadInfo info = it.getInfo(campo?.campo);	
			String id;
			if (info.campo && !LedCampoUtils.xToMany(info.campo.campo))
				id = "${info.campo.idWithNullCheck()}";
			else
				id = "${it.idCheck}";
			if (forTabla)
				return "'${it.id}':${id}? ${id}:'_${it.id}_'";
			else
				return "'${it.id}':${id}";
		};
		String redirigirAnterior = "";
		if (hayAnterior && redirigir)
			redirigirAnterior = "'redirigir': 'anterior'";
		String params = StringUtils.params(accionParam, idParams, redirigirAnterior);
		String map = "";
		if (! params.equals("")) map = ", [${params}]";
		return """play.mvc.Router.reverse("${controllerFullName}.index" ${map})""";
	}
	
	/*
	* Para saber la ruta de la funcion que se encargara de validar y guardar la pagina antes de abrir la pagina de una tabla
	*/
   public String getRouteBeforeOpenPageTable(String accion){
	   String accionParam = "";
	   if (accion) accionParam = "'accion':'${accion}'";
	   List<String> idParams = allEntities.collect {
		   if (it.isSingleton() || (entidad.equals(it) && "crear".equals(accion)))
			   return "";
		   EntidadInfo info = it.getInfo(campo?.campo);
		   String id;
		   if (info.campo && !LedCampoUtils.xToMany(info.campo.campo))
			   id = "${info.campo.idWithNullCheck()}";
		   else
			   id = "${it.idCheck}";
		   return "'${it.id}':${id}";
	   };
		   
	   String irDespuesDeValidar = "'irDespuesDeValidar':''";
	
	   String params = StringUtils.params(accionParam, idParams, irDespuesDeValidar);
	   String map = "";
	   if (! params.equals("")) map = ", [${params}]";
	   return """play.mvc.Router.reverse("${controllerFullName}.beforeOpenPageTable" ${map})""";
   }
	
	public String getRouteIndex(String accion){
		return getRouteIndex(accion, true, false);
	}
	
	public String getRouteAccion(String accion){
		List<String> idParams = allEntities.collect {
			if (it.isSingleton() || (entidad.equals(it) && "crear".startsWith(accion)))
				return "";
			EntidadInfo info = it.getInfo(campo?.campo);
			if (info.campo && !LedCampoUtils.xToMany(info.campo.campo))
				return "'${it.id}':${info.campo.idWithNullCheck()}";
			else
				return "'${it.id}':${it.idCheck}";
		};
		String params = StringUtils.params(idParams);
		String map = "";
		if (params != "")
			map = ", [${params}]";
		return "play.mvc.Router.reverse('${controllerFullName}.${accion}' ${map})";
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
	
	public String complexGetter(Entidad entidad) {
		EntidadInfo info = entidad.getInfo(campo?.campo);
		if (info.almacen.nulo())
			return simpleGetter(entidad, true);
		Entidad almacen = Entidad.create(info.almacen.entidad);
		almacen.singletonsId = true;
		String singleton = "";
		String noSingleton = "";
		if (almacen.isSingleton()){
			singleton = "${almacen.typeId} = ${simpleGetterCall(almacen, false)}.id;";
		}
		else{
			noSingleton = """
				if(${almacen.id} == null){
					if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro ${almacen.id}"))
					Messages.fatal("Falta parámetro ${almacen.id}");
				}
			""";
		}
		return """
			@Util
			public static ${entidad.clase} get${entidad.clase}(${StringUtils.params(info.almacen.typeId, entidad.typeId)}){
				${entidad.clase} ${entidad.variable} = null;
				${singleton}
				${noSingleton}
				if(${entidad.id} == null){
					if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro ${entidad.id}"))
						Messages.fatal("Falta parámetro ${entidad.id}");
				}
				if(${almacen.id} != null && $entidad.id != null){
					$entidad.variable = ${entidad.clase}.find("select $entidad.variable from ${almacen.clase} ${almacen.variable} join ${info.campo.firstLower()} $entidad.variable where ${almacen.variable}.id=? and ${entidad.variable}.id=?", ${almacen.id}, $entidad.id).first();
					if($entidad.variable == null)
						Messages.fatal("Error al recuperar ${entidad.clase}");
				}
				return $entidad.variable;
			}
		""";
	}

	public String complexGetterCall(Entidad entidad) {
		EntidadInfo info = entidad.getInfo(campo?.campo);
		if (info.almacen.nulo() || info.almacen.isSingleton())
			return simpleGetterCall(entidad, true);
		return "${controllerName}.get${entidad.clase}(${info.almacen.id}, ${entidad.id})";
	}
	
	public String simpleGetter(Entidad entidad, boolean byId) {
		if (entidad.entidad == null){
			return "";
		}
		if (entidad.isSingleton()){
			return """
				@Util
				public static ${entidad.clase} get${entidad.clase}(){
					return ${entidad.clase}.get(${entidad.clase}.class);
				}
			"""
		}
		if (byId){
			return """
				@Util
				public static ${entidad.clase} get${entidad.clase}(${entidad.typeId}){
					${entidad.clase} ${entidad.variable} = null;
					if(${entidad.id} == null){
						if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro ${entidad.id}"))
							Messages.fatal("Falta parámetro ${entidad.id}");
					}
					else{
						${entidad.variable} = ${entidad.clase}.findById($entidad.id);
						if($entidad.variable == null){
							Messages.fatal("Error al recuperar ${entidad.clase}");
						}
					}
					return ${entidad.variable};
				}
			"""
		}
		return """
			@Util
			public static ${entidad.clase} get${entidad.clase}(){
				return new ${entidad.clase}();
			}
		"""
	}

	public String simpleGetterCall(Entidad entidad, boolean byId) {
		if (byId && !entidad.isSingleton()){
			return "${controllerName}.get${entidad.clase}(${entidad.id})";
		}
		else{
			return "${controllerName}.get${entidad.clase}()";
		}
	}

	public int compare(Entidad e1, Entidad e2) {
		if (e1.equals(e2)) return 0;
		if (campo?.campo){
			int index1 = campo.indexOf(e1);
			int index2 = campo.indexOf(e2);
			if (index1 != index2){
				if (index1 == -1 || index2 == -1)
					return index2 - index1;
				return index1 - index2;
			}
		}
		String s1 = e1?.entidad?.name;
		String s2 = e2?.entidad?.name;
		if (!s1) s1 = "";
		if (!s2) s2 = "";
		return s1.compareTo(s2);
	}
	
	private String metodoProcesandoEntidades () {
		return """
			@After(only={"${controllerName}.${nameEditar}", "${controllerGenName}.${nameEditar}" $strProcesandoMethods})
			protected static void setEntidadesProcesada () {
				unsetEntidadesProcesando();
			}

			@Before(only={"${controllerName}.${nameEditar}", "${controllerGenName}.${nameEditar}" $strProcesandoMethods})
			protected static void setEntidadesProcesandose () {
				setEntidadesProcesando();
			}
		"""
	}
			
}
