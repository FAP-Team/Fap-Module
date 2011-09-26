package templates;


import es.fap.simpleled.led.*;
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
			String varName = var.getVar();
			EntidadUtils entity = EntidadUtils.create( var.getTipo());
			
			if(var.sql== null){
				//Variable simple
				varStr += """
		//${var.var}
		${entity.clase} ${varName} = null;
		if((vars != null) && (vars.containsKey("${varName}"))){
			${varName} = (${entity.clase}) vars.get("${varName}");
		}else if((ids != null) && (ids.containsKey("${entity.id}"))){
			${varName} = ${entity.clase}.findById(ids.get("${entity.id}"));
		}else if(Singleton.class.isAssignableFrom(${entity.clase}.class)){
			${varName} = ${entity.clase}.all().first();
		}
		
		if (${varName} == null)
			return false;
"""
			}else{
				String params = var.sqlParams?.sqlParams?.join(",");
				if(params != null && !params.trim().isEmpty())
					params = ", " + params;
				else
					params = "";	
					
			//Variable con consulta
				varStr += """
		//${var.var}
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
		} else if(r.getGroupOp() != null){
			String realOp = r.getGroupOp().replaceAll("\\s+", "")
			String group = r.getRightGroup().join(", ");
			out = "utils.StringUtils.${realOp}(${r.left}.toString(), ${group})"	
		}else{
			if ((r.left.equals("null")) || (r.right.equals("null"))) {
				String op = r.getSimpleOp().equals('=') ? '==' : r.getSimpleOp();
				out = "${r.left} ${op} ${r.right}";
			}
			else if(r.getSimpleOp().equals("=")){
				out = "${r.left}.toString().equals(${r.right}.toString())";
			}else{
				// !=
				out = "!${r.left}.toString().equals(${r.right}.toString())";
			}
		}
		return out;
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
