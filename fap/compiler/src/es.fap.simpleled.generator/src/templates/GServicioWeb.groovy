package templates;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import es.fap.simpleled.led.util.LedDocumentationUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class GServicioWeb extends GElement{

	ServicioWeb servicioWeb;
	String controllerGenFullName;
	String controllerFullName;
	CampoUtils campoEntidad;
	public String url;
	Map <String, CampoUtils> camposAtributos;
	Map <CampoUtils, String> campoWhen;
	
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
package controllers.gen.serviciosweb;

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
import com.google.gson.JsonElement;
import org.joda.time.DateTime;
import messages.Messages;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import play.libs.WS;
import play.libs.WS.WSRequest;
import play.mvc.Http;
import properties.FapProperties;

public class ${controllerGenFullName} extends WSController {

	protected static Logger log = Logger.getLogger("ServicioWeb");

${metodoIndex()}

${metodoGetInfoWS()}

}
"""
		FileUtils.overwrite(FileUtils.getRoute('CONTROLLER_GEN_SERVICIOWEB'),controllerGenFullName.replaceAll("\\.", "/") + ".java", BeautifierUtils.formatear(controllerGen));
			
		String controller = """
package controllers.serviciosweb;

import controllers.gen.serviciosweb.${controllerGenFullName};
			
public class ${controllerFullName} extends ${controllerGenFullName} {

}
		"""
		FileUtils.write(FileUtils.getRoute('CONTROLLER_SERVICIOWEB'), controllerFullName.replaceAll("\\.", "/") + ".java", BeautifierUtils.formatear(controller));
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

		String ret = """
					List<ResultadoPeticion> resultadoPeticion = new ArrayList<ResultadoPeticion>();""";
		String condicion;
		// En allEntities tendria todas las entidades xToMany que tuviera concatenadas el campoEntidad
		if (allEntities.size() > 0) {
			condicion = metodoSiWhen("${allEntities.get(0).getVariable()}Aux");
			ret += 	"""
					List<${allEntities.get(0).entidad.name}> ${allEntities.get(0).getVariable()} = ${allEntities.get(0).entidad.name}.findAll();
					List<ResultadosPeticion> listaPeticiones = new ArrayList<ResultadosPeticion>();

					for(${allEntities.get(0).entidad.name} ${allEntities.get(0).getVariable()}Aux: ${allEntities.get(0).getVariable()}){""";
			if (condicion != "" && condicion.contains("solicitudAux")) {
				ret += 	"""if (${condicion}) {""";
			}
			
			if (allEntities.size() > 1) {
				int i = 1;
				ret += 	"""
						List<${allEntities.get(i).entidad.name}> ${allEntities.get(i).getVariable()} = ${allEntities.get(i).entidad.name}.find("select ${allEntities.get(i).getVariable()} from ${allEntities.get(0).entidad.name} ${allEntities.get(0).getVariable()} join ${campoEntidad.str} ${allEntities.get(i).getVariable()} where ${allEntities.get(0).getVariable()}.id=?", ${allEntities.get(0).getVariable()}Aux.id).fetch();
						""";
						
				for (i = 1; i < allEntities.size(); i++) {
					condicion = metodoSiWhen("${allEntities.get(i).getVariable()}Aux");
					ret += 	"""
							for(${allEntities.get(i).entidad.name} ${allEntities.get(i).getVariable()}Aux: ${allEntities.get(i).getVariable()}){""";
								   
					if (condicion != "" && !condicion.contains("solicitudAux")) {
						ret += 	"""
								if (${condicion}) {""";
					}
					
					ret += 	"""
							List<ResultadoPeticion> listaResultados = new ArrayList<ResultadoPeticion>();
							
							ResultadoPeticion resultado = new ResultadoPeticion("id${allEntities.get(0).entidad.name}", ${allEntities.get(0).getVariable()}Aux.id);
							resultadoPeticion.add(resultado);
							listaResultados.add(resultado);
							
							ResultadoPeticion resultado${allEntities.get(i).getVariable()} = new ResultadoPeticion("id${allEntities.get(i).entidad.name}", ${allEntities.get(i).getVariable()}Aux.id);
							resultadoPeticion.add(resultado${allEntities.get(i).getVariable()});
							listaResultados.add(resultado${allEntities.get(i).getVariable()});
							""";
				}
				
				ret += writeResultadosPeticion(camposAtributos, allEntities.get(allEntities.size()-1).getVariable());
				
				ret += 	"""
						ResultadosPeticion lista = new ResultadosPeticion();
						for (int j = 0; j < listaResultados.size(); j++) {
							lista.resultadoPeticion.add(listaResultados.get(j));
						}
						listaPeticiones.add(lista);
						}
						""";
				if (condicion != "" && !condicion.contains("solicitudAux")) {
					ret += 	"""}""";
				}
			} else {

				ret += 	"""
						List<ResultadoPeticion> listaResultados = new ArrayList<ResultadoPeticion>();	
						""";
				condicion = metodoSiWhen("${allEntities.get(allEntities.size()-1).getVariable()}Aux");
				if (condicion != "" && !condicion.contains("solicitudAux")) {
					ret += 	"""
							if (${condicion}) {
							""";
				}
				ret += 	"""
						ResultadoPeticion resultado = new ResultadoPeticion("id${allEntities.get(0).entidad.name}", ${allEntities.get(0).getVariable()}Aux.id);
						resultadoPeticion.add(resultado);
						listaResultados.add(resultado);
						""";
				
				ret += writeResultadosPeticion(camposAtributos, allEntities.get(allEntities.size()-1).getVariable());

				ret +=	"""
						ResultadosPeticion lista = new ResultadosPeticion();
						for (int j = 0; j < listaResultados.size(); j++) {
							lista.resultadoPeticion.add(listaResultados.get(j));
						}
						listaPeticiones.add(lista);
						"""
				if (condicion != "" && !condicion.contains("solicitudAux")) {
					ret += 	"""}""";
				}
			}
			if (condicion != "" && condicion.contains("solicitudAux")) {
				ret += 	"""}""";
			}
			
		}
		
		ret += 	"""
				}
				ListaResultadosPeticion resultadoJSON = new ListaResultadosPeticion();
				resultadoJSON.resultadosPeticion = listaPeticiones;
				Gson gson = new Gson();
				String string_json = gson.toJson(resultadoJSON);
				renderJSON(string_json);
				"""
		return ret;
	}
	
	/**
	 * Función que genera el código necesario por cada atributo
	 * especificado en el "return" del servicio web en el DSL.
	 * @param camposAtributos
	 * @param nombreEntidad
	 * @return
	 */
	private String writeResultadosPeticion(Map<String, CampoUtils> camposAtributos, String nombreEntidad) {
		String ret = "";
		Iterator it2 = camposAtributos.entrySet().iterator();
		
		while (it2.hasNext()) {
			String variableName = "";
			String variableComp = "";
			Map.Entry e = (Map.Entry)it2.next();
			String[] vars = ((CampoUtils)e.getValue()).str.split("\\.");
			
			if (vars.size() > 2) {
				int k = 1;
				for (k = 1; k < vars.size(); k++) {
					variableComp += vars[k];
					if (k < vars.size()-1) {
						variableComp += ".";
					}
				}
			} else
				variableComp = ((CampoUtils)e.getValue()).str.split("\\.")[-1];

			variableName = ((CampoUtils)e.getValue()).str.split("\\.")[-1];
			
			if (!variableName.equals("id")) {
				ret += 	"""
						ResultadoPeticion resultado${variableName} = new ResultadoPeticion("${e.getKey()}", ${nombreEntidad}Aux.${variableComp});
						resultadoPeticion.add(resultado${variableName});
						listaResultados.add(resultado${variableName});
						""";
			}
		}
		return ret;
	}
	
	/**
	 * Función a la que se llama en el caso de que se haya especificado
	 * la regla "When".
	 * @param entidad
	 * @return
	 */
	private String metodoSiWhen(String entidad) {
		if (this.servicioWeb.when != null) {
			String out = wsRuleCode(servicioWeb.when.rule, entidad);
			return out;
		}
		return "";
	}
	
	/**
	 * Se crea la función getInfoWS() para obtener un resumen de
	 * la información de cada servicio web.
	 * @return
	 */
	private String metodoGetInfoWS(){
		String out ="""
					public static ServicioWebInfo getInfoWS() {
						ServicioWebInfo swi = new ServicioWebInfo();
					"""
		Iterator it2 = camposAtributos.entrySet().iterator();
		while (it2.hasNext()) {
			Map.Entry e = (Map.Entry)it2.next();
			String nombreParam = ((CampoUtils)e.getValue()).str;
			String atr = ((CampoUtils)e.getValue()).str.split("\\.")[-1];
			String tipoParam = getTipoCampo((CampoUtils)e.getValue());
			
			out += 	"""
				InfoParams info${atr} = new InfoParams();
				info${atr}.tipo = "${tipoParam}";
				info${atr}.nombreParam = "${atr}";
				swi.infoParams.add(info${atr});
				""";
		}

		out += 	"""
					swi.nombre = "${servicioWeb.getName()}";
					swi.urlWS = "/${servicioWeb.getName()}";
					return swi;
				}
				""";

		return out;
	}
	
	private String wsRuleCode(WSRuleOr r, String entidad){
		return wsRuleCode(r.getLeft(), entidad) + " || " + wsRuleCode(r.getRight(), entidad);
	}

	private String wsRuleCode(WSRuleAnd r, String entidad){
		return wsRuleCode(r.getLeft(), entidad) + " && " + wsRuleCode(r.getRight(), entidad);
	}

	private String getWSRuleCheckRightStr(WSRuleCheckRight right, String entidad){
		if (right.str != null)
			return "\"" + right.str + "\"";
		if (right.isNulo())
			return "null";
		return "";
	}
	
	private String wsRuleCode(WSPrimary r, String entidad){
		return "(" + wsRuleCode(r.getLeft(), entidad) + ")";
	}
	
	/**
	 * Función que analiza las condiciones especificadas en el "When"
	 * de la regla. Dependiendo del operador los operandos deberán de
	 * ser de un tipo u otro. Se genera el código correspondiente para
	 * cada condición.
	 * @param r
	 * @param entidad
	 * @return
	 */
	private String wsRuleCode(WSRuleCheck r, String entidad){
		String out = "";
		CampoUtils campo = CampoUtils.create(r.getLeft());
		String campoCompleto = campo.str;
		String campoEntidad = campoCompleto.split("\\.")[0];
		String campoFinal = campoCompleto.replaceFirst(campoEntidad, entidad, );
		
		if (entidad.equals(campoEntidad.toLowerCase()+"Aux")) {
			if (r.getGroupOp()) { // Si es "in" o "not in"
				String realOp = r.getGroupOp().op.replaceAll("\\s+", "")
				String group = r.getRightGroup().collect{
					return getWSRuleCheckRightStr(it, entidad);
				}?.join(", ");
				out += "utils.StringUtils.${realOp}(${campoFinal}, ${group})"
			}
			else if (r.getSimpleOp()) { // Si es "=" o "!="
				String right = getWSRuleCheckRightStr(r.right, entidad);
				if (right.contains("/")) {
					out += getDateOutput(right, campo, campoFinal, r.getSimpleOp().op);
				}
				else if ((right.equals("null"))) {
					String op = r.getSimpleOp().op.equals('=') ? '==' : r.getSimpleOp().op;
					out += "${campoFinal} ${op} null";
				}
				else if(r.getSimpleOp().op.equals("="))
					out += "${campoFinal}.toString().equals(${right}.toString())";
				else
					out += "!${campoFinal}.toString().equals(${right}.toString())";
			}
			else if (r.getCompareOp()) { // Si es "<" o ">" o "<=" o ">="
				String right = getWSRuleCheckRightStr(r.right, entidad);
				if (right.contains("/")) {
					out += getDateOutput(right, campo, campoFinal, r.getCompareOp().op);
				}	
			}
		}
		else {
			out = ""
		}
		return out;
	}
	
	/**
	 * Función que analiza si el string del lado derecho de la condición
	 * es una fecha del tipo "dd/MM/yyyy" y, si lo es, genera el código
	 * necesario.
	 * @param right
	 * @param campo
	 * @param campoFinal
	 * @param op
	 * @return
	 */
	private String getDateOutput(String right, CampoUtils campo, String campoFinal, String op) {
		String out = "";
		
		if (getTipoCampo(campo).equals("DateTime")) {
			String dia;
			String mes;
			String agno;
			try {
				dia = right.split("/")[0];
				mes = right.split("/")[1];
				agno = right.split("/")[2];
			} catch (IndexOutOfBoundsException ex) {
				println("Fecha escrita de forma incorrecta: " + ex.detailMessage);
			}

			String fecha = dia + "-" + mes + "-" + agno;
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			Date date2 = sdf.parse(fecha.split("\"")[1]);
			String dateLong = date2.getTime().toString();
			String operador = op.equals('=') ? '==' : op;
				
			out += "(${campoFinal} != null ? ${campoFinal}.toDate().getTime() : new Date().getTime()) ${operador} Long.parseLong(\"${dateLong}\")";
		}
		return out;
	}
	
	/**
	 * Función que devuelve el tipo del campo que se está
	 * analizando.
	 * @param campoU
	 * @return
	 */
	private String getTipoCampo(CampoUtils campoU) {
		Campo campo = campoU.campo;
		CampoAtributos attrs = campo.getAtributos();
		CampoAtributos anterior;
		String tipoParam;

		while (attrs != null){
			anterior = attrs;
			attrs = attrs.getAtributos();
			if (attrs == null) {
				tipoParam = anterior.getAtributo().getType().getSimple();
				if (tipoParam == null) {
					if (anterior.getAtributo().getType().getSpecial() != null) {
						tipoParam = anterior.getAtributo().getType().getSpecial().getType();
					} else {
						tipoParam = "Lista";
					}
				}
				else
					tipoParam = anterior.getAtributo().getType().getSimple().getType();
			}
		}

		return tipoParam;
	}
	
	/**
	 * Función que es llamada desde rutas() en End.groovy para generar la
	 * ruta correspondiente a cada servicio web.
	 */
	public String routes() {
		String url = "/WSInfo/" + servicioWeb.getName();
		String action = "serviciosweb." + controllerFullName + ".index";
		return RouteUtils.to("GET", url, action).toString() + "\n";
	}

}
