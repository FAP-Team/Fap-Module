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
		borrarFicherosAntiguos();
		if (LedUtils.generatingModule){
			DocumentationUtils.makeDocumentation();
		}
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
			page = myFirstPages.get(0);
			log.warn("No se indicó una página como inicial, se utiliza: <"+page+">");
		}
		String content = "fap.app.firstPage="+page;
		FileUtils.writeInRegion(FileUtils.getRoute('CONF_APPLICATION'), content);
	}
	

  /**
   * Genera el fichero de permisos a partir de las entidad GPermisos almacenadas
   * en la hashStack
   * @return
   */
  private String permisos(){
	  String permisos = "";
	  String lName = "";
	  String defLine = "";
	  String extendz = "";
	  String generateExtends = "";
	  
	  // Establecemos la línea de definición de la clase
	  
	  // Si estamos generando para el módulo en el modulo
	  if (!createSolicitud.equals("true")) {
		  lName = "PermissionFap";
		  defLine = "public class ${lName}";
		  extendz = "extends ${lName}Gen"
	  } else {
	  	  lName = "Permission";
	  	  defLine = "public class ${lName}";
	  	  extendz = "extends ${lName}Gen";
		  generateExtends = "extends PermissionFap"
	  }
	  
	  for(Object o in HashStack.allElements(HashStackName.PERMISSION)){
		  GPermiso p = (GPermiso)o;
	  	  permisos += p.permisoCode();	
	  }
	  
	  // Permisos generados
		String out = """
package secure.gen;

import java.util.*;
import models.*;
import controllers.fap.AgenteController;
import secure.*;
		
${defLine}Gen ${generateExtends} {	
${permisos}
}
""";

FileUtils.overwrite(FileUtils.getRoute('PERMISSION_GEN'), "${lName}Gen.java", out);

  // Permisos manual
  String outManual = """
package secure;
	  
import java.util.*;
import models.*;
import secure.gen.*;
import controllers.fap.SecureController;
			  
${defLine} ${extendz} {
}
""";
	  
	  FileUtils.write(FileUtils.getRoute('PERMISSION'), "${lName}.java", outManual);
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
 
    
}
