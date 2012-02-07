package templates;

import java.awt.event.ItemEvent;

import es.fap.simpleled.led.*;
import generator.utils.CampoPermisoUtils
import generator.utils.EntidadUtils
import generator.utils.FileUtils;
import generator.utils.HashStack;
import generator.utils.StringUtils;
import generator.utils.HashStack.HashStackName;

public class GPermiso {

	Permiso permiso;
	boolean isOrContains;

	public static String generate(Permiso permiso){
		GPermiso g = new GPermiso();
		g.permiso = permiso;
		HashStack.push(HashStackName.PERMISSION, g);
		return null;
	}

	private String permisoVarsCode(List<PermisoVar> vars){
		String varStr = "";
		for(PermisoVar var : vars){
			String varName = var.name;
			EntidadUtils entity = EntidadUtils.create(var.getTipo());
			if(var.sql != null){
				String params = var.sqlParams?.sqlParams?.collect{
					return CampoPermisoUtils.create(it).str;
				}?.join(",");
				if(params != null && !params.trim().isEmpty())
					params = ", " + params;
				else
					params = "";
					
				//Variable con consulta
				varStr += """
					${entity.clase} ${varName} = ${entity.clase}.find("${var.sql}"${params}).first();
				""";
			}
			else{
				varStr += """
					${entity.clase} ${varName} = get${entity.clase}(ids, vars);
				""";
			}
		}
		return varStr;
	}
	
	private String permisoRuleCode(PermisoRuleOr r){
		return permisoRuleCode(r.getLeft()) + " || " + permisoRuleCode(r.getRight());
	}

	private String permisoRuleCode(PermisoRuleAnd r){
		return permisoRuleCode(r.getLeft()) + " && " + permisoRuleCode(r.getRight());
	}

	private String permisoRuleCode(PermisoRuleCheck r){
		String out;
		if (r.permiso) {
			String consulta;
			if (r.result.equals("_grafico")){
				consulta = "grafico";
				isOrContains = true;
			}
			else if (r.result.equals("_accion")){
				consulta = "accion";
				isOrContains = true;
			}
			else{
				consulta = "\"${r.result}\"";
				if ("is".equals(r.op)) isOrContains = true;
			}
			if ("is".equals(r.op))
				out = """secure.checkGrafico("${r.permiso.name}", ${consulta}, accion, ids, vars)""";
			else
				out = """secure.checkAcceso("${r.permiso.name}", ${consulta}, ids, vars)""";
			if (r.not)
				out = "!" + out;
		}
		else{
			CampoPermisoUtils campo = CampoPermisoUtils.create(r.left);
			if ("accion".equals(campo.str))
				isOrContains = true;
			out = "";		
			for (String nullCheck: campo.nullCheckList())
				out += "${nullCheck} != null && ";
			if(r.getGroupOp()){
				String realOp = r.getGroupOp().op.replaceAll("\\s+", "")
				String group = r.getRightGroup().collect{
					return getPermisoRuleCheckRightStr(it);
				}?.join(", ");
				out += "utils.StringUtils.${realOp}(${campo.str}.toString(), ${group})"
			}
			else{
				String right = getPermisoRuleCheckRightStr(r.right);
				if ((right.equals("null"))) {
					String op = r.getSimpleOp().op.equals('=') ? '==' : r.getSimpleOp().op;
					out += "${campo.str} ${op} null";
				}
				else if(r.getSimpleOp().op.equals("="))
					out += "${campo.str}.toString().equals(${right}.toString())";
				else
					out += "!${campo.str}.toString().equals(${right}.toString())";
			}
		}
		return out;
	}

	private String getPermisoRuleCheckRightStr(PermisoRuleCheckRight right){
		if (right.action != null)
			return "\"" + right.action + "\"";
		if (right.str != null)
			return "\"" + right.str + "\"";
		if (right.isNulo())
			return "null";
		return CampoPermisoUtils.create(right.campo).str;
	}
	
	private String permisoRuleCode(PermisoPrimary r){
		return "(" + permisoRuleCode(r.getLeft()) + ")";
	}
	
	public String permisoCode(){
		return """
			${metodoCheck()}
			${metodoAccion()}
		""";
	}
	
	public String metodoCheck(){
		String vars = "";
		if (permiso.varSection?.vars)
			vars = permisoVarsCode(permiso.varSection.vars);
		
		String condiciones = "";
		if (permiso.ret)
			condiciones += "${getCheck(permiso.ret)}";
		for (PermisoWhen when: permiso.whens){
			condiciones += """
				if (${permisoRuleCode(when.rule)}){
					${getCheck(when.ret)}
				}
			""";
		}
		String elseCondicion = "";
		if (permiso.getElse())
			elseCondicion = "${getCheck(permiso.else)}";
		else if(permiso.ret == null)
			elseCondicion = "return null;";	
		return """	
			private ResultadoPermiso ${permiso.name} (String grafico, String accion, Map<String, Long> ids, Map<String, Object> vars){
				//Variables
				Agente agente = AgenteController.getAgente();
				${vars}
				Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
				${condiciones}
				${elseCondicion}
			}
		""";
	}
	
	public String metodoAccion(){
		String vars = "";
		if (permiso.varSection?.vars)
			vars = permisoVarsCode(permiso.varSection.vars);
		
		String condiciones = "";
		if (permiso.ret)
			condiciones += returnPrimeraAccion(permiso.ret);
		for (PermisoWhen when: permiso.whens){
			isOrContains = false;
			String condicion = permisoRuleCode(when.rule);
			if (isOrContains){
				condiciones += """
					acciones.clear();
					${getAcciones(when.ret)}
					for (String accion: acciones){
						if (${condicion})
							return new ResultadoPermiso(Accion.parse(accion));
					}
				""";
			}
			else{
				condiciones += """
					if (${condicion})
						${returnPrimeraAccion(when.ret)}
				""";
			}
			
		}
		String elseCondicion = "";
		if (permiso.getElse())
			elseCondicion = returnPrimeraAccion(permiso.getElse());
		else if(permiso.ret == null)
			elseCondicion = "return null;";
		String acciones = "";
		return """
			private ResultadoPermiso ${permiso.name}Accion (Map<String, Long> ids, Map<String, Object> vars){
				String grafico = "visible";
				//Variables
				Agente agente = AgenteController.getAgente();
				${vars}
				Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
				List<String> acciones = new ArrayList<String>();
				${condiciones}
				${elseCondicion}
			}
		""";
	}
	
	private static String getCheck(PermisoReturn p){
			String accion = "";
			String grafico = "";
			String checks = "";
			if (p.denegar)
				accion = "Accion.Denegar";
			if (p.all)
				accion = "Accion.All";
			if (p.grafico)
				grafico = "Grafico.${StringUtils.firstUpper(p.grafico.permiso)}";
			String els = "";
			for (AccionesGrafico par: p.pares){
				grafico = "";
				if (par.grafico)
					grafico = "Grafico.${StringUtils.firstUpper(par.grafico.permiso)}";
				
				for (String acc: par.acciones.acciones){
					accion = "Accion.${StringUtils.firstUpper(acc)}";
					checks += """${els}if ("${acc}".equals(accion)) return new ResultadoPermiso(${StringUtils.params(accion, grafico)});\n""";
					els = "else ";
				}
			}
			if (!"".equals(els))
				checks += """else return null;\n""";
			if ("".equals(checks))
				return """return new ResultadoPermiso(${StringUtils.params(accion, grafico)});\n""";
			return checks;
	}
	
	private static String getAcciones(PermisoReturn p){
		String adds = "";
		if(p.all || p.grafico){
			return """
				acciones.add("editar");
				acciones.add("leer");
				acciones.add("crear");
				acciones.add("borrar");
			""";
		}
		for (AccionesGrafico par: p.pares){
			for (String accion: par.acciones.acciones)
				adds += "acciones.add(\"${accion}\");\n";
		}
		return adds;
	}
	
	private static String returnPrimeraAccion(PermisoReturn p){
		String accion = getPrimeraAccion(p);
		if (accion)
			return "return new ResultadoPermiso(Accion.${StringUtils.firstUpper(accion)});";
		return "return null;";
	}
	
	public static String getPrimeraAccion(PermisoReturn p){
		if (p.all || p.grafico)
			return "editar";
		if (p.pares.size() > 0)
			return p.pares.get(0).acciones.acciones.get(0);
		return null;
	}
}
