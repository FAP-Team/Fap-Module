package templates;

import java.awt.event.ItemEvent;

import es.fap.simpleled.led.*;
import generator.utils.CampoPermisoUtils
import generator.utils.EntidadUtils
import generator.utils.FileUtils;
import generator.utils.HashStack;
import generator.utils.StringUtils;
import generator.utils.PermisosUtils;
import generator.utils.HashStack.HashStackName;

public class GPermiso {

	def Permiso permiso;

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
			
			if(var.sql== null){
				//Variable simple
				varStr += """
		//${var.name}
		${entity.clase} ${varName} = null;
		if((vars != null) && (vars.containsKey("${varName}"))){
			${varName} = (${entity.clase}) vars.get("${varName}");
		}else if((ids != null) && (ids.containsKey("${entity.id}"))){
			${varName} = ${entity.clase}.findById(ids.get("${entity.id}"));
		}else if(Singleton.class.isAssignableFrom(${entity.clase}.class)){
			try {
				${varName} = (${entity.clase}) ${entity.clase}.class.getMethod("get", Class.class).invoke(null, ${entity.clase}.class);
			} catch (Exception e) {}
		}
		
		if (${varName} == null)
			return false;
"""
			}else{
				String params = var.sqlParams?.sqlParams?.collect{
					return CampoPermisoUtils.create(it).str;
				}?.join(",");
				if(params != null && !params.trim().isEmpty())
					params = ", " + params;
				else
					params = "";	
					
			//Variable con consulta
				varStr += """
		//${var.name}
		${entity.clase} ${varName} = ${entity.clase}.find("${var.sql}"${params}).first();
				"""
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
		if (r.getPermiso() != null) {
			if (r.isNot()){
				out = "!" + PermisosUtils.className() + r.getPermiso().getName() + "(action, ids, vars)";
			}
			else{
				out = PermisosUtils.className() + r.getPermiso().getName() + "(action, ids, vars)";
			}
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
				else if(r.getSimpleOp().equals("=")){
					out = "${campo.str}.toString().equals(${right}.toString())";
				}
				else{
				// !=
				out = "!${campo.str}.toString().equals(${right}.toString())";
				}
			}
		}
		return out;
	}

	private String getPermisoRuleCheckRightStr(PermisoRuleCheckRight right){
		if (right.str != null){
			return "\"" + right.str + "\"";
		}
		if (right.isNulo()){
			return "null";
		}
		return CampoPermisoUtils.create(right.campo).str;
	}
	
	private String permisoRuleCode(PermisoPrimary r){
		return "(" + permisoRuleCode(r.getLeft()) + ")";
	}
	
	public String permisoCode(){
		String varStr = "";
		if (permiso.varSection?.vars != null) {
			varStr = permisoVarsCode(permiso.getVarSection().getVars());
		}
		String ruleStr = permisoRuleCode(permiso.rule);
		String ret;
		if(permiso.then.equals("grant")){
			ret = "return resultado;"	
		}else{
			ret = "return !resultado;";
		}
		
		String out = """	
	public static boolean ${permiso.name} (String action, Map<String, Long> ids, Map<String, Object> vars){
		//Variables
		Agente agente = AgenteController.getAgente();
		${varStr}
		boolean resultado = ${ruleStr};
		${ret}
	}
""";
	return out;
	}
}
