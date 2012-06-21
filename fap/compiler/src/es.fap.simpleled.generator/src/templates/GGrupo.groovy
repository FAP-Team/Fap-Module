package templates;

import es.fap.simpleled.led.*;
import generator.utils.CampoUtils
import generator.utils.Entidad;
import generator.utils.ListUtils;
import generator.utils.TagParameters;

public class GGrupo extends GGroupElement{
	
	Grupo grupo;
	
	public GGrupo(Grupo grupo, GElement container){
		super(grupo, container);
		this.grupo = grupo;
		elementos = grupo.getElementos();
	}
	
	public String view(){
		String elementos = "";
		for(Elemento elemento : grupo.getElementos())
			elementos += getInstance(elemento).view();
		
		TagParameters params = new TagParameters();
		if (grupo.titulo)
			params.putStr("titulo", grupo.titulo);
		
		if (grupo.siCombo){
			params.putStr("siCombo", grupo.siCombo.name) 
			params.put("siComboValue", ListUtils.list2GroovyListString(grupo.siComboValues.values))
            if(grupo.signoSiCombo.equals("!="))
                params.put("reverse", true);	
		}
		
		if (grupo.siCheck){
			params.putStr("siCheck", grupo.siCheck.name)
			params.put("siCheckValue", grupo.siCheckValues)
		}
		
		if (grupo.campo){
			def valores = ListUtils.list2GroovyListString(grupo.siCampoValues.values);
			CampoUtils campo = CampoUtils.create(grupo.campo);
			params.put("mostrarSi",  "${valores}.contains(${campo.firstLower()})")
            if(grupo.signoSiCampo.equals("!="))
                params.put("reverse", true);
		}

		if (grupo.siExpresion)
			params.put("mostrarSi", grupo.siExpresion)
		
		if (grupo.borde)
			params.put("borde", grupo.borde.toBoolean());

		if (grupo.permiso)
			params.putStr("permiso", grupo.permiso.name);
		
		def out = """
			#{fap.grupo ${params.lista()}}
				${elementos}
			#{/fap.grupo}
		""";
		return out;	
	}
	
	public Set<Entidad> dbEntities(){
		Set<Entidad> dbEntities = super.dbEntities();
		if (grupo.campo)
			dbEntities.add(Entidad.create(grupo.campo.entidad));
		return dbEntities;
	}
	
	public String validateCopy(Stack<Set<String>> validatedFields){
		String out = "";
		if (grupo.permiso || grupo.siCombo || grupo.siCheck || grupo.campo || grupo.siExpresion){
			validatedFields.push(new HashSet<String>());
			if (grupo.permiso)
				out += """if (secure.checkGrafico("${grupo.permiso.name}", "editable", accion, (Map<String,Long>)tags.TagMapStack.top("idParams"), null)) {\n""";
			if (grupo.siCombo){
				String not = "";
					if(grupo.signoSiCombo.equals("!="))
						not = "!";
				CampoUtils campo = CampoUtils.create(grupo.siCombo.campo);
				out += """if (${not}Arrays.asList(new String[] {${grupo.siComboValues.values.collect{"\"${it}\""}.join(',')}}).contains(${campo.dbStr()})){\n""";
			}
			if (grupo.siCheck) {
				CampoUtils campo = CampoUtils.create(grupo.siCheck.campo);
				if(campo.getUltimoAtributo().type.simple.type != "boolean"){
					out += "if ((${campo.firstLower()} != null) && (${campo.firstLower()} == ${grupo.siCheckValues})) {\n";
				}
				else{
					out += "if (${campo.firstLower()} == ${grupo.siCheckValues}) {\n";
				}
			}
			if (grupo.campo){
				String not = "";
				if(grupo.signoSiCampo.equals("!="))
					not = "!";
				CampoUtils campo = CampoUtils.create(grupo.campo);
				out += """if (${not}Arrays.asList(new String[] {${grupo.siCampoValues.values.collect{"\"${it}\""}.join(',')}}).contains(${campo.dbStr()})){\n""";
			}
			if (grupo.siExpresion)
				out += "if (${grupo.siExpresion}) {"
		}
		
		for (Elemento elemento: elementos)
			out += getInstance(elemento).validateCopy(validatedFields);
		
		if (grupo.permiso || grupo.siCombo || grupo.siCheck || grupo.campo || grupo.siExpresion){
			validatedFields.pop();
			if (grupo.permiso) out += "\n}\n";
			if (grupo.siCombo) out += "\n}\n";
			if (grupo.siCheck) out += "\n}\n";
			if (grupo.campo) out += "\n}\n";
			if (grupo.siExpresion) out += "\n}\n";
		}
		return out;
	}
	
}
