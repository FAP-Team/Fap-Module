package templates;

import java.util.ArrayList;
import java.util.List;

import generator.utils.*;

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
	public String url;
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
import controllers.fap.WSController;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import java.util.List;
import java.util.ArrayList;
import models.*;
import com.google.gson.Gson;
import org.joda.time.DateTime;

public class ${controllerGenFullName} extends WSController {

	protected static Logger log = Logger.getLogger("ServicioWeb");

${metodoIndex()}

${metodoGetInfoWS()}

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
		public static void index() {
			${desglosarCampo()}
		}"""
		
		return out;
	}
	
	private String desglosarCampo(){
		List<EntidadInfo> subcampos = CampoUtils.calcularSubcampos(campoEntidad?.campo);
		List<Entidad> allEntities = new ArrayList<Entidad>();
		if (subcampos.size() > 0 && !subcampos.get(0).almacen.nulo()) {
			allEntities.add(subcampos.get(0).almacen);
		} //???
		for (EntidadInfo subcampo: subcampos) {
			allEntities.add(subcampo.entidad);
		}
		String ret = """ListaConsultas listaConsultas = new ListaConsultas();
						List<ConsultaWS> consulta = new ArrayList<ConsultaWS>();
					 """;
		
		// En allEntities tendria todas las entidades xToMany que tuviera concatenadas el campoEntidad
		if (allEntities.size() > 0) {
			ret += 	"""List<${allEntities.get(0).entidad.name}> ${allEntities.get(0).getVariable()} = ${allEntities.get(0).entidad.name}.findAll();
				ConsultaWS objetito = null;
				for(${allEntities.get(0).entidad.name} ${allEntities.get(0).getVariable()}Aux: ${allEntities.get(0).getVariable()}){
				// Ir creando el Array de Array de Objetitos por defecto.
				// El Objetito sería el "id${allEntities.get(0).getVariable()}":${allEntities.get(0).getVariable()}Aux.id

					  objetito = new ConsultaWS("id${allEntities.get(0).entidad.name}", ${allEntities.get(0).getVariable()}Aux.id);
				   	  consulta.add(objetito);
			 """
			if (allEntities.size() > 1) {
				int i = 1;
				for (i = 1; i < allEntities.size(); i++) {
					ret += """List<${allEntities.get(i).entidad.name}> ${allEntities.get(i).getVariable()} = ${allEntities.get(i).entidad.name}.find("select ${allEntities.get(i).getVariable()} from ${allEntities.get(0).entidad.name} ${allEntities.get(0).getVariable()} join ${campoEntidad.str} ${allEntities.get(i).getVariable()} where ${allEntities.get(0).getVariable()}.id=?", ${allEntities.get(0).getVariable()}Aux.id).fetch();
							  ConsultaWS objetito${allEntities.get(i).getVariable()} = null;
							 	  
							  for(${allEntities.get(i).entidad.name} ${allEntities.get(i).getVariable()}Aux: ${allEntities.get(i).getVariable()}){
							  // Ir creando el Array de Array de Objetitos por defecto.
						      // El Objetito sería el "id${allEntities.get(i).getVariable()}":${allEntities.get(i).getVariable()}Aux.id
							  		
							  	List<ConsultaWS> listaObjetitos = new ArrayList<ConsultaWS>();
								
								 objetito${allEntities.get(i).getVariable()} = new ConsultaWS("id${allEntities.get(i).entidad.name}", ${allEntities.get(i).getVariable()}Aux.id);
								 consulta.add(objetito${allEntities.get(i).getVariable()});
								 listaObjetitos.add(objetito${allEntities.get(i).getVariable()});
								"""
				}

				Iterator it2 = camposAtributos.entrySet().iterator();
				String variableName = "";
				while (it2.hasNext()) {
					Map.Entry e = (Map.Entry)it2.next();
					variableName = ((CampoUtils)e.getValue()).str.split("\\.")[1];
					if (!variableName.equals("id")) {
						ret += 	"""
								ConsultaWS objetito${variableName} = null;
								objetito${variableName} = new ConsultaWS("${e.getKey()}", ${allEntities.get(allEntities.size()-1).getVariable()}Aux.${variableName});
								consulta.add(objetito${variableName});
								listaObjetitos.add(objetito${variableName});
								"""
					}
				}
				
				ret += 	"""
						ConsultasWS c = new ConsultasWS();
						c.consultaWS.add(objetito);
						for (int j = 0; j < listaObjetitos.size(); j++) {
							c.consultaWS.add(listaObjetitos.get(j));
						}
						listaConsultas.consultasWS.add(c);
						}
						"""

			} else {
				String variableName = "";
				ret += """List<ConsultaWS> listaObjetitos = new ArrayList<ConsultaWS>();	
						""";
				Iterator it2 = camposAtributos.entrySet().iterator();
				while (it2.hasNext()) {
					Map.Entry e = (Map.Entry)it2.next();
					variableName = ((CampoUtils)e.getValue()).str.split("\\.")[1];
					if (!variableName.equals("id")) {
						ret += 	"""
								ConsultaWS objetito${variableName} = null;
								objetito${variableName} = new ConsultaWS("${e.getKey()}", ${allEntities.get(allEntities.size()-1).getVariable()}Aux.${variableName});
								consulta.add(objetito${variableName});
								listaObjetitos.add(objetito${variableName});
								"""
					}
				}

				ret +=	"""
						ConsultasWS c = new ConsultasWS();
						c.consultaWS.add(objetito);
						for (int j = 0; j < listaObjetitos.size(); j++) {
							c.consultaWS.add(listaObjetitos.get(j));
						}
						listaConsultas.consultasWS.add(c);
						"""
			}
		}
		ret += 	"""
				}
				DatosGraficas datos = new DatosGraficas();
				DateTime de = new DateTime();
				datos.fechaConsulta = de.toString();
				datos.lista.add(listaConsultas);

				Gson gson = new Gson();
				String string_json = gson.toJson(datos);
				renderJSON(string_json);
				"""
		return ret;
	}
	
	private String metodoGetInfoWS(){
		String out ="""
					public static ServiciosWebApp getInfoWS() {
						ServiciosWebApp swi = new ServiciosWebApp();
					"""
		Iterator it2 = camposAtributos.entrySet().iterator();
		while (it2.hasNext()) {
			Map.Entry e = (Map.Entry)it2.next();
			String nombreParam = ((CampoUtils)e.getValue()).str;
			String atr = ((CampoUtils)e.getValue()).str.split("\\.")[1];
			Campo s = ((CampoUtils)e.getValue()).campo;
			String tipoParam = s.getAtributos().getAtributo().getType().getSimple().getType().toString();
			
			out += 	"""
					InfoWS info${atr} = new InfoWS();
					info${atr}.tipo = "${tipoParam}";
					info${atr}.nombreParam = "${nombreParam}";
					swi.infoRet.add(info${atr});
					"""
		}
		out += 	"""
					swi.nombre = "${servicioWeb.getName()}";
					swi.urlWS = "/${servicioWeb.getName()}";
					return swi;
				}
				"""
		return out;
	}
	
	public String routes() {
		String url = "/WSInfo/" + servicioWeb.getName();
		String action = "servicioweb." + controllerFullName + ".index";
		return RouteUtils.to("GET", url, action).toString() + "\n";
	}

}
