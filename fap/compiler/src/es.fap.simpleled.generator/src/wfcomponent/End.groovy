package wfcomponent;

import es.fap.simpleled.led.LedPackage;

import es.fap.simpleled.led.impl.AttributeImpl;

import java.text.AttributedCharacterIterator.Attribute;

import java.lang.reflect.Array;
import java.util.ArrayList;

import es.fap.simpleled.led.impl.AttributeImpl;

import org.apache.log4j.Logger;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;

import templates.GEntidad;
import templates.GPagina
import templates.GPermiso;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.LedFactory;
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.Type
import es.fap.simpleled.led.impl.EntityImpl;
import es.fap.simpleled.led.impl.AttributeImpl;
import es.fap.simpleled.led.impl.LedFactoryImpl;
import generator.utils.HashStack.HashStackName;

import generator.utils.*;

public class End implements IWorkflowComponent {

	String modelPath
	String targetDir
	String createSolicitud
	
	private static Logger log = Logger.getLogger(End.class)
	
	@Override
	public void preInvoke() {
		// TODO Auto-generated method stub
	}

	@Override
	public void invoke(IWorkflowContext ctx) {
		if (createSolicitud.equals("true")) {
			if (HashStack.size(HashStackName.SOLICITUD) == 0){
				log.warn("No se ha creado la entidad Solicitud. Se creará una por defecto.");
				entitySolicitud();
			}
			properties();
		}
		permisos();
		rutas();
		if (LedUtils.generatingModule){
			DocumentationUtils.makeDocumentation();
		}
		borrarFicherosAntiguos();
		config();
	}

	@Override
	public void postInvoke() {
		// TODO Auto-generated method stub
	}

	
	private void rutas(){
		def elementos = HashStack.allElements(HashStackName.ROUTES);
		String content = elementos.collect{it -> it.generateRoutes()}.join('\n');
		FileUtils.writeInRegion(FileUtils.getRoute('CONF_ROUTES'), content);
	}
	
	private void properties(){
		def firstPages = HashStack.allElements(HashStackName.FIRST_PAGE);
		String page;
		if (firstPages.size() == 1) {
			page = firstPages.get(0);
		} else if (firstPages.size() > 1){
			page = firstPages.get(0);
			log.warn("Se indicaron mas de una página como inicial, se utiliza: <"+page+">");
		} else {
			log.warn("No se indicó una página como inicial");
			/** Utilizamos la primera encontrada */
			def myFirstPages = HashStack.allElements(HashStackName.PAGE_NAME);
			if (myFirstPages.size > 0){
				page = myFirstPages.get(0);
				log.warn("No se indicó una página como inicial, se utiliza: <"+page+">");
			}
		}
		String content = "";
		if (page != null){
			content = "fap.app.firstPage="+page;
		}
		FileUtils.writeInRegion(FileUtils.getRoute('CONF_APPLICATION'), content);
	}
	

  /**
   * Genera el fichero de permisos a partir de las entidad GPermisos almacenadas
   * en la hashStack
   * @return
   */
  private String permisos(){	  
	  String clazzName = LedUtils.generatingModule ? "SecureFap" : "SecureApp"; 
	  String clazzGenName = clazzName + "Gen";
	  
	  def permisos = [];
	  def permisosCode = "";
	  def switchCode = "";
	  for(Object o in HashStack.allElements(HashStackName.PERMISSION)){
		  GPermiso permiso = (GPermiso)o
		  permisos.add(permiso)
	  	  permisosCode += permiso.permisoCode()
		
		  String permisoName = permiso.permiso.name;	
		  if(switchCode.isEmpty()){
			  	
		  	switchCode = """
		if("${permisoName}".equals(id))
			return ${permisoName}(action, ids, vars);
"""	
		  }else{
		  switchCode += """
		else if("${permisoName}".equals(id))
			return ${permisoName}(action, ids, vars);
	  """
		  }
	  }
	  
	  	  
	  // Permisos generados
		String secureGen = """
package security;

import java.util.Map;

import models.*;
import controllers.fap.AgenteController;

public class ${clazzGenName} extends Secure {

	public ${clazzGenName}(Secure next) {
		super(next);
	}

	@Override
	public boolean check(String id, String action, Map<String, Long> ids, Map<String, Object> vars) {
${switchCode}		
		return nextCheck(id, action, ids, vars);
	}
	
	${permisosCode}
}
""";

FileUtils.overwrite(FileUtils.getRoute('PERMISSION'), "${clazzGenName}.java", secureGen);

  // Permisos manual
  String secure = """
package security;

import java.util.Map;

public class ${clazzName} extends Secure {
	
	public ${clazzName}(Secure next) {
		super(next);
	}

	@Override
	public boolean check(String id, String action, Map<String, Long> ids, Map<String, Object> vars) {		
		return nextCheck(id, action, ids, vars);
	}
}""";
	  
	  FileUtils.write(FileUtils.getRoute('PERMISSION'), "${clazzName}.java", secure);
		}
  
  
 private void entitySolicitud () {
	 LedFactoryImpl factory = new LedFactoryImpl();
	 
	 EntityImpl solicitud = factory.createEntity();
	 solicitud.setName("Solicitud");
	 EntityImpl solicitudGen = factory.createEntity();
	 solicitudGen.setName("SolicitudGenerica");
	 solicitud.setExtends(solicitudGen);
	 GEntidad.generate(solicitud);
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
		if(!LedUtils.generatingModule){
			String appConfigFolder = FileUtils.getRoute('APP_CONFIG');
			String configGen = 
"""
package config;

import security.*;

import com.google.inject.AbstractModule;

/**
 * Configuración de Guice generada.
 *
 * Clase automática, cada vez que se genere la aplicación
 * se sobreescribirá esta clase. Para personalizar
 * la configuración consula la clase config.AppModule. 
 */
public class AppModuleGen extends AbstractModule {
	
	@Override
	protected void configure() {
		secure();
		custom();
	}
	
	protected void secure(){
		bind(Secure.class).toInstance(new SecureApp(new SecureAppGen(new SecureFap(new SecureFapGen(null)))));
	}

	protected void custom(){
	}
	
}
"""
			FileUtils.overwrite(appConfigFolder, "AppModuleGen.java", configGen);
			String config = 
"""
package config;

/**
 * Configuración de Guice.
 * 
 * En esta clase puedes personalizar la configuración de Guice.
 * Puedes sobreescribir los métodos ya definidos, como por ejemplo
 * <secure> para personalizar la cadena de mando
 * que se va a utilizar para resolver un permiso. Además puedes
 * añadir configuración adicional utilizando el método <custom>.
 */
public class AppModule extends AppModuleGen {
	
}
"""
			FileUtils.write(appConfigFolder, "AppModule.java", config);
		}	
	}  
}
