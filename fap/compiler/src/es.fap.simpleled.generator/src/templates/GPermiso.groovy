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
		if (r.permiso != null) {
			String _permiso = r.result;
			if (!_permiso.equals("_permiso"))
				_permiso = """ "${_permiso}" """;
			out = """secure.check("${r.getPermiso().getName()}", ${_permiso}, action, ids, vars)""";
			if ("is-not".equals(r.op))
				out = "!" +out;
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
		String vars = "";
		if (permiso.varSection?.vars != null)
			vars = permisoVarsCode(permiso.getVarSection().getVars());
		
		String condiciones = "";
		if (permiso.ret)
			condiciones += "return ${getMetodoCheck(permiso.ret)}(_permiso);";
		for (PermisoWhen when: permiso.whens){
			condiciones += """
				if (${permisoRuleCode(when.rule)})
					return ${getMetodoCheck(when.ret)}(_permiso);
			""";
		}
		String elseCondicion = "";
		if (permiso.getElse())
			elseCondicion = "return ${getMetodoCheck(permiso.getElse())}(_permiso);";
		else if(permiso.ret == null)
			elseCondicion = "return false;";
				
		return """	
			private boolean ${permiso.name} (String _permiso, String action, Map<String, Long> ids, Map<String, Object> vars){
				//Variables
				Agente agente = AgenteController.getAgente();
				${vars}
				Secure secure = config.InjectorConfig.getInjector().getInstance(security.Secure.class);
				${condiciones}
				${elseCondicion}
			}
		""";
	}
	
	private static String getMetodoCheck(PermisoReturn p){
		if(p.ret.equals("editable"))
			return "checkIsEditableOrLess";
		if(p.ret.equals("visible"))
			return "checkIsVisibleOrLess";
		if(p.ret.equals("none"))
			return "checkIsNone";
	}
	
}
