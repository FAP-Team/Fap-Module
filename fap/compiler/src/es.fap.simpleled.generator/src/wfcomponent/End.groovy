package wfcomponent;

import es.fap.simpleled.led.LedPackage;

import es.fap.simpleled.led.impl.AttributeImpl;

import java.text.AttributedCharacterIterator.Attribute;

import java.lang.reflect.Array;
import java.util.ArrayList;

import es.fap.simpleled.led.impl.AttributeImpl;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;

import templates.GElement
import templates.GPagina;
import templates.GPermiso;
import templates.GPopup;
import templates.elements.*;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Formulario
import es.fap.simpleled.led.LedFactory;
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.Permiso
import es.fap.simpleled.led.PermisoVar
import es.fap.simpleled.led.Popup
import es.fap.simpleled.led.Type
import es.fap.simpleled.led.Pagina
import es.fap.simpleled.led.impl.EntityImpl;
import es.fap.simpleled.led.impl.AttributeImpl;
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.led.util.ModelUtils;

import generator.utils.*;

public class End implements IWorkflowComponent {

	String modelPath
	String targetDir
	String createSolicitud
	
	private static Logger log = Logger.getLogger(End.class)

	@Override
	public void invoke(IWorkflowContext ctx) {
		LedUtils.setFapResources();
		
		controllersAndViews();
		
		if (!Start.generatingModule){
			entitySolicitud();
			properties();
		}
		
		permisos();
		rutas();
		
		if (Start.generatingModule){
			DocumentationUtils.makeDocumentation();
		}
		
		borrarFicherosAntiguos();
		config();
	}

	@Override
	public void postInvoke() {
		// TODO Auto-generated method stub
	}

	private void controllersAndViews(){
		for (Formulario formulario: LedUtils.getNodes(LedPackage.Literals.FORMULARIO)){
			if (!LedUtils.inPath(formulario)) continue;
			for (Pagina pagina: formulario.paginas){
				GPagina gpagina = GElement.getInstance(pagina, null);
				gpagina.view();
				gpagina.controller();
			}
			for (Popup popup: formulario.popups){
				GPopup gpopup = GElement.getInstance(popup, null);
				gpopup.view();
				gpopup.controller();
			}
			if(formulario.menu)
				GElement.getInstance(formulario.menu, null).generate();
		}
	}
	
	private void rutas(){
		String content = "";
		for(Pagina pagina in LedUtils.getNodes(LedPackage.Literals.PAGINA)){
			if (!LedUtils.inPath(pagina)) continue;
			content += GElement.getInstance(pagina, null).routes();
		}
		for(Popup popup in LedUtils.getNodes(LedPackage.Literals.POPUP)){
			if (!LedUtils.inPath(popup)) continue;
			content += GElement.getInstance(popup, null).routes();
		}
		if (!Start.generatingModule){
			content = "  # Home page\n" + rutaPaginaInicial() + "\n" + content;
		}
		FileUtils.writeInRegion(FileUtils.getRoute('CONF_ROUTES'), content);
	}
	
	private String rutaPaginaInicial(){
		List<Formulario> formularios = ModelUtils.getVisibleNodes(LedPackage.Literals.FORMULARIO, LedUtils.resource);
		Formulario formInicial;
		Pagina pagInicial;
		for (Formulario f: formularios){
			if (f.inicial){
				formInicial = f;
				break;
			}
		}
		if (formInicial == null || formInicial.paginas.size() == 0){
			return RouteUtils.to("GET", "/", "SolicitudesController.index");
		}
		pagInicial = formInicial.paginas.get(0);
		for (Pagina pag: formInicial.paginas){
			if (pag.inicial){
				pagInicial = pag;
				break;
			}
		}
		return RouteUtils.to("GET", "/", pagInicial.name + "Controller.index");
	}
	
	private void properties(){
		List<Formulario> formsSolicitud = ModelUtils.getVisibleNodes(LedPackage.Literals.FORMULARIO, "Solicitud", LedUtils.resource);
		Pagina pagInicial;
		boolean indicada;
		for (Formulario f: formsSolicitud){
			for (Pagina pag: f.paginas){
				if (pagInicial == null){
					pagInicial = pag;
				}
				if (pag.inicial){
					pagInicial = pag;
					indicada = true;
					break;
				}
			}
		}
		String content = "fap.app.firstPage=" + pagInicial.name;
		FileUtils.writeInRegion(FileUtils.getRoute('CONF_APPLICATION'), content);
	}

	private String permisos(){	  
		String clazzName = Start.generatingModule ? "SecureFap" : "SecureApp"; 
		String clazzGenName = clazzName + "Gen";
		String permisosCode = "";
		String switchCode = "";
		String switchAccionCode = "";
		String els = "";
		Map<String, Entity> variables = new HashMap<String, Entity>();
	  
		for(Permiso p in LedUtils.getNodes(LedPackage.Literals.PERMISO)){
			if (!LedUtils.inPath(p)) continue;
			GPermiso permiso = GElement.getInstance(p, null);
			if (permiso.permiso.varSection != null){
				for(PermisoVar var : permiso.permiso.varSection.vars)
					variables.put(var.tipo.name, var.tipo);
			}
			permisosCode += permiso.permisoCode();
			String permisoName = permiso.permiso.name;	
			switchCode += """
				${els} if("${permisoName}".equals(id))
					return ${permisoName}(grafico, action, ids, vars);
			""";
			switchAccionCode += """
				${els} if("${permisoName}".equals(id))
					return ${permisoName}Accion(ids, vars);
			""";
			els = "else";
		}
	  	  
		String vars = "";
		for (Entity e: variables.values()){
			Entidad entidad = Entidad.create(e);
			vars += """
				public ${entidad.clase} get${entidad.clase}(Map<String, Long> ids, Map<String, Object> vars){
					if (vars != null && vars.containsKey("${entidad.variable}"))
						return (${entidad.clase}) vars.get("${entidad.variable}");
					else if (ids != null && ids.containsKey("${entidad.id}"))
						return ${entidad.clase}.findById(ids.get("${entidad.id}"));
					${entidad.isSingleton()? "return ${entidad.clase}.get(${entidad.clase}.class);" : "return null;"}
				}
			""";
		}
		
		// Permisos generados
		String secureGen = """
package security;

import java.util.Map;
import models.*;
import controllers.fap.AgenteController;
import java.util.ArrayList;
import java.util.List;

public class ${clazzGenName} extends Secure {

	public ${clazzGenName}(Secure next) {
		super(next);
	}

	@Override
	public ResultadoPermiso check(String id, String grafico, String action, Map<String, Long> ids, Map<String, Object> vars) {
		${switchCode}		
		return nextCheck(id, grafico, action, ids, vars);
	}

	@Override
	public ResultadoPermiso accion(String id, Map<String, Long> ids, Map<String, Object> vars) {
		${switchAccionCode}		
		return nextAccion(id, ids, vars);
	}
	
	${permisosCode}

	${vars}
}
""";

		FileUtils.overwrite(FileUtils.getRoute('PERMISSION'), "${clazzGenName}.java", BeautifierUtils.formatear(secureGen));
		
		// Permisos manual
		String secure = """
package security;

import java.util.Map;

public class ${clazzName} extends Secure {
	
	public ${clazzName}(Secure next) {
		super(next);
	}

	@Override
	public ResultadoPermiso check(String id, String grafico, String action, Map<String, Long> ids, Map<String, Object> vars) {		
		return nextCheck(id, grafico, action, ids, vars);
	}

	@Override
	public ResultadoPermiso accion(String id, Map<String, Long> ids, Map<String, Object> vars) {		
		return nextAccion(id, ids, vars);
	}
}
""";

		FileUtils.write(FileUtils.getRoute('PERMISSION'), "${clazzName}.java", BeautifierUtils.formatear(secure));
	}
  
  
	private void entitySolicitud() {
		Resource res = LedUtils.resource;
		if (ModelUtils.getVisibleNode(LedPackage.Literals.ENTITY, "Solicitud", res) == null){
			if (ModelUtils.getVisibleNodes(LedPackage.Literals.FORMULARIO, "Solicitud", res).size() > 1)
				log.warn("No se ha creado la entidad Solicitud. Se creará una por defecto.");
			EntityImpl solicitud = LedFactory.eINSTANCE.createEntity();
			solicitud.setName("Solicitud");
			EntityImpl solicitudGen = LedFactory.eINSTANCE.createEntity();
			solicitudGen.setName("SolicitudGenerica");
			solicitud.setExtends(solicitudGen);
			GElement.getInstance(solicitud, null).generate();
		}
	}
 
 
 	private void borrarFicherosAntiguos(){
		 List<File> dirs = new ArrayList<File>();
		 
		 dirs.add(new File(FileUtils.getRoute('CONTROLLER_GEN')));
		 dirs.add(new File(FileUtils.getRoute('CONTROLLER_GEN_POPUP')));
		 dirs.add(new File(FileUtils.getRoute('LIST')));
		 dirs.add(new File(FileUtils.getRoute('JSON_DOCUMENTATION')));
		 dirs.add(new File(FileUtils.getRoute('ENUM')));
		 dirs.add(new File(FileUtils.getRoute('ENUM_FAP')));
		 //dirs.add(new File(FileUtils.getRoute('MODEL'))); Se puede descomentar.
		 
		 for (File dview: new File(FileUtils.getRoute('VIEW')).listFiles()){
			 if (dview.isDirectory() && !dview.name.equals(".svn")){
				 dirs.add(dview);
			 }
		 }
		 
		 for (File dir: dirs){
			 for (File c: dir.listFiles()){
				 if (!c.isDirectory() && !FileUtils.overwrittenFiles.contains(c.getAbsolutePath())){
					log.info("Borrando el fichero: " + c.getAbsolutePath());
					 FileUtils.delete(c);
				 }
			 }
		 } 
	 }
 
	private void config(){
		if(!Start.generatingModule){
			String appConfigFolder = FileUtils.getRoute('APP_CONFIG');
			String config = 
"""
package config;

import security.*;

/**
 * Configuración de Guice.
 * 
 * En esta clase puedes personalizar la configuración de Guice.
 * 
 * La configuración por defecto personaliza el método secure para
 * configurar correctamente los permisos.
 * 
 * Si quieres añadir nueva configuración de guice puede
 * sobreescribir el metodo <config> (recuerda llamar al super)
 * 
 * Si quieres descartar la configuración del módulo y únicamente
 * utilizar la tuya elimina el "extends FapModule".
 */
public class AppModule extends FapModule {
	
	@Override
	protected void secure() {
		bind(Secure.class).toInstance(new SecureApp(new SecureAppGen(new SecureFap(new SecureFapGen(null)))));
	}
	
}
"""
			FileUtils.write(appConfigFolder, "AppModule.java", BeautifierUtils.formatear(config));
		}	
	}
	
	@Override
	public void preInvoke() {
		// TODO Auto-generated method stub
	}
}
