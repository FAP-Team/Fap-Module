package templates;

import generator.utils.CampoUtils
import generator.utils.Entidad
import generator.utils.EntidadInfo
import generator.utils.FileUtils;
import generator.utils.LedUtils;
import generator.utils.StringUtils;
import generator.utils.BeautifierUtils;

import es.fap.simpleled.led.*;
import es.fap.simpleled.led.util.ModelUtils;
import es.fap.simpleled.led.impl.AttributeImpl
import es.fap.simpleled.led.impl.CompoundTypeImpl
import es.fap.simpleled.led.impl.EntityImpl
import es.fap.simpleled.led.impl.PaginaImpl
import es.fap.simpleled.led.impl.TypeImpl
import es.fap.simpleled.led.impl.LedFactoryImpl
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.eclipse.emf.ecore.EObject

import es.fap.simpleled.led.util.LedDocumentationUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class GServicioWeb extends GElement{
	
	ServicioWeb servicioWeb;
	String controllerGenFullName;
	String controllerFullName;
	CampoUtils campoEntidad;
	Map <String, CampoUtils> camposAtributos;
	
	public GServicioWeb(ServicioWeb servicioWeb, GElement container){
		super(servicioWeb, container);
		this.servicioWeb = servicioWeb;
		this.controllerFullName = this.servicioWeb.name + "SWController";
		this.controllerGenFullName = this.servicioWeb.name + "SWControllerGen";
		campoEntidad = CampoUtils.create(this.servicioWeb.campo);
		camposAtributos = new HashMap<String, CampoUtils>();
		camposAtributos.put(this.servicioWeb.ret.titulo, CampoUtils.create(this.servicioWeb.ret.campoRet));
		for (WSReturn ret: this.servicioWeb.retMore){
			camposAtributos.put(ret.titulo, CampoUtils.create(ret.campoRet));
		}
	}
	
	public void generate(){
		String controllerGen = """
package controllers.servicioweb.gen;

import play.*;
import play.mvc.*;
import controllers.fap.GenericController;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import java.util.List;
import java.util.ArrayList;
import models.*;

public class ${controllerGenFullName} extends GenericController {

	protected static Logger log = Logger.getLogger("ServicioWeb");

${metodoIndex()}

}
"""
		FileUtils.overwrite(FileUtils.getRoute('SERVICIOWEB_GEN'),controllerGenFullName.replaceAll("\\.", "/") + ".java", BeautifierUtils.formatear(controllerGen));
			
		String controller = """
package controllers.servicioweb;

import controllers.servicioweb.gen.${controllerGenFullName};
			
public class ${controllerFullName} extends ${controllerGenFullName} {

}
		"""
		FileUtils.write(FileUtils.getRoute('SERVICIOWEB'), controllerFullName.replaceAll("\\.", "/") + ".java", BeautifierUtils.formatear(controller));
	}
	
	private String metodoIndex(){

		String out = """
		public static String index() {
			${desglosarCampo()}
			return "";
}"""
		
		return out;
	}
	
	private String desglosarCampo(){
//		Iterator it = camposAtributos.entrySet().iterator();
//		while (it.hasNext()) {
//			Map.Entry e = (Map.Entry)it.next();
//			System.out.println(e.getKey() + " " + ((CampoUtils)e.getValue()).sinEntidad());
//		}
		List<EntidadInfo> subcampos = CampoUtils.calcularSubcampos(campoEntidad?.campo);
		List<Entidad> allEntities = new ArrayList<Entidad>();
		if (subcampos.size() > 0 && !subcampos.get(0).almacen.nulo())
			allEntities.add(subcampos.get(0).almacen);
		for (EntidadInfo subcampo: subcampos){
			allEntities.add(subcampo.entidad);
		}
		String ret = "";
		// En allEntities tendria todas las entidades xToMany que tuviera concatenadas el campoEntidad
		for (Entidad ent: allEntities){
			//System.out.println("Valgo: "+ent.entidad.name);
			if (allEntities.size() > 1){
				
			} else {
			   ret += """List<${ent.entidad.name}> ${ent.getVariable()} = ${ent.entidad.name}.findAll();
				   	     List<ConsultaWS> consultas = new ArrayList<ConsultaWS>();
			   			 for(${ent.entidad.name} ${ent.getVariable()}Aux: ${ent.getVariable()}){
			   					// Ir creando el Array de Array de Objetitos por defecto.
			   					// El Objetito sería el "id${ent.getVariable()}":${ent.getVariable()}Aux.id

			   					ConsultaWS objetito = new ConsultaWS("id${ent.entidad.name}", ${ent.getVariable()}Aux.id.intValue());
			   					consultas.add(objetito);
			   			 }
"""
			}
		}
		return ret;
	}
}
