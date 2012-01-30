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

	def permiso;

	public static String generate(def permiso){
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
		if (r.permisoGrafico || r.permisoAcceso) {
			String _permiso = r.result;
			if (!_permiso.equals("_permiso") && !_permiso.equals("accion"))
				_permiso = """ "${_permiso}" """;
			String name = r.permisoGrafico?.name;
			if (!name) name = r.permisoAcceso.name;
			out = """secure.check("${name}", ${_permiso}, accion, ids, vars)""";
			if (r.not)
				out = "!" + out;
		}
		else{
			CampoPermisoUtils campo = CampoPermisoUtils.create(r.left);
			if(r.getGroupOp() != null){
				String realOp = r.getGroupOp().replaceAll("\\s+", "")
				String group = r.getRightGroup().collect{
					return getPermisoRuleCheckRightStr(it);
				}?.join(", ");
				out = "utils.StringUtils.${realOp}(${campo.str}.toString(), ${group})"
			}
			else{
				String right = getPermisoRuleCheckRightStr(r.right);
				if ((right.equals("null"))) {
					String op = r.getSimpleOp().equals('=') ? '==' : r.getSimpleOp();
					out = "${campo.str} ${op} null";
				}
				else if(r.getSimpleOp().equals("="))
					out = "${campo.str}.toString().equals(${right}.toString())";
				else
					out = "!${campo.str}.toString().equals(${right}.toString())";
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
		for (def when: permiso.whens){
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
			elseCondicion = "return false;";	
		return """	
			private boolean ${permiso.name} (String _permiso, String accion, Map<String, Long> ids, Map<String, Object> vars){
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
		if (! (permiso instanceof PermisoAcceso))
			return "";
		String vars = "";
		if (permiso.varSection?.vars)
			vars = permisoVarsCode(permiso.varSection.vars);
		
		String condiciones = "";
		if (permiso.ret)
			condiciones += "return ${getPrimeraAccion(permiso.ret)};";
		for (PermisoWhenAcceso when: permiso.whens){
			condiciones += """
				acciones.clear();
				${getAcciones(when.ret)}
				for (String accion: acciones){
					if (${permisoRuleCode(when.rule)})
						return accion;
				}
			""";
		}
		String elseCondicion = "";
		if (permiso.getElse())
			elseCondicion = "return ${getPrimeraAccion(permiso.else)};";
		else if(permiso.ret == null)
			elseCondicion = "return null;";
		String acciones = "";
		return """
			private String ${permiso.name}Accion (String _permiso, Map<String, Long> ids, Map<String, Object> vars){
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
	
	private static String getCheck(def p){
		if (p instanceof PermisoReturnGrafico){
			String metodo;
			if(p.ret.equals("editable"))
				metodo = "checkIsEditableOrLess";
			if(p.ret.equals("visible"))
				metodo = "checkIsVisibleOrLess";
			if(p.ret.equals("none"))
				metodo = "checkIsNone";
			return "return ${metodo}(_permiso);";
		}
		if (p instanceof PermisoReturnAcceso){
			String checks = "";
			if(p.all || p.acciones.contains("leer"))
				checks += 'if ("leer".equals(accion)) return true;\n';
			if(p.all || p.acciones.contains("editar"))
				checks += 'if ("editar".equals(accion)) return true;\n';
			if(p.all || p.acciones.contains("crear"))
				checks += 'if ("crear".equals(accion)) return true;\n';
			if(p.all || p.acciones.contains("borrar"))
				checks += 'if ("borrar".equals(accion)) return true;\n';
			checks += 'return false;\n';
			return checks;
		}
	}
	
	private static String getAcciones(PermisoReturnAcceso p){
		String adds = "";
		if(p.all){
			return """
				acciones.add("editar");
				acciones.add("leer");
				acciones.add("crear");
				acciones.add("borrar");
			""";
		}
		for (String accion: p.acciones)
			adds += """ acciones.add("${accion}");\n """;
		return adds;
	}
	
	private static String getPrimeraAccion(PermisoReturnAcceso p){
		if(p.all)
			return '"editar"';
		if(p.acciones.size() > 0)
			return '"${p.acciones.get(0)}"';
		return "null";
	}
	
}
