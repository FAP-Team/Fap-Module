package generator.utils


import java.util.Map;
import java.util.ArrayList;

import es.fap.simpleled.led.*
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

import org.eclipse.emf.ecore.EObject;

import com.sun.media.sound.RealTimeSequencer.PlayThread;

import utils.*;

/**
 * Created by IntelliJ IDEA.
 * User: fap
 * Date: 21/06/11
 * Time: 10:11
 * To change this template use File | Settings | File Templates.
 */
class ControllerUtils {

	static List<String> camposSolicitante = "tipo,fisica.nombre,fisica.primerApellido,fisica.segundoApellido,fisica.nip,juridica.cif,juridica.entidad,representado".split(',')
	static List<String> camposSolicitantePersonaFisica = "tipo,fisica.nombre,fisica.primerApellido,fisica.segundoApellido,fisica.nip,representado".split(',');
	static List<String> camposSolicitantePersonaJuridica = "tipo,juridica.cif,juridica.entidad,representado".split(',');
	static List<String> camposPersona = "tipo,fisica.nombre,fisica.primerApellido,fisica.segundoApellido,fisica.nip,juridica.cif,juridica.entidad".split(',')
	static List<String> camposPersonaFisica = "nombre,primerApellido,segundoApellido,nip".split(",")
	static List<String> camposPersonaJuridica = "cif,entidad".split(',')
	static List<String> camposAed = "tipo,descripcion".split(",")
		
	static Stack<Set<String>> validatedFields;
	
    /**
     * Crea un getter que recupera una entidad desde base de datos por el ID
     * El id es obligatorio
     * @param entidad
     * @return
     */
    public static String simpleGetter(EntidadUtils entidad, boolean byId) {
		if (entidad.isSingleton()){
			return """
				@Util
				protected static ${entidad.clase} get${entidad.clase}(){
					return ${entidad.clase}.get(${entidad.clase}.class);
				}
			"""
		}
		if (entidad.variable.equals("solicitud") || byId){
			return """
				@Util
				protected static ${entidad.clase} get${entidad.clase}(${entidad.typeId}){
					${entidad.clase} ${entidad.variable} = null;
					if(${entidad.id} == null){
						Messages.fatal("Falta parámetro ${entidad.id}");
					}else{
						${entidad.variable} = ${entidad.clase}.findById($entidad.id);
						if($entidad.variable == null){
							Messages.fatal("Error al recuperar ${entidad.clase}");
						}
					}
					return ${entidad.variable};
				}
			"""
		}
		else{
			return """
				@Util
				protected static ${entidad.clase} get${entidad.clase}(){
					return new ${entidad.clase}();
				}
			"""
		}
    }

    public static String simpleGetterCall(EntidadUtils entidad, boolean byId) {
		if (entidad.variable.equals("solicitud") || byId){
			return "get${entidad.clase}(${entidad.id})";
		}
		else{
			return "get${entidad.clase}()";
		}
    }

    /**
     * Crea un getter, para una entidad que tiene que buscar en una lista
     * El id de las dos entidades es obligatorio
     * @param almacen
     * @param entidad
     * @return
     */
    public static String complexGetter(EntidadUtils almacen, EntidadUtils entidad, CampoUtils campo) {
        if (almacen == null)
            return simpleGetter(entidad, true);

        return """
	@Util
    protected static ${entidad.clase} get${entidad.clase}(${almacen.typeId}, ${entidad.typeId}){
        ${entidad.clase} ${entidad.variable} = null;
        if(${almacen.id} == null){
            Messages.fatal("Falta parámetro $almacen.id");
        }else if($entidad.id == null){
            Messages.fatal("Falta parámetro $entidad.id");
        }else{
            $entidad.variable = ${entidad.clase}.find("select $entidad.variable from $almacen.clase $almacen.variable join ${campo.firstLower()} $entidad.variable where ${almacen.variable}.id=? and ${entidad.variable}.id=?", $almacen.id, $entidad.id).first();
            if($entidad.variable == null){
                Messages.fatal("Error al recuperar ${entidad.clase}");
            }
        }
        return $entidad.variable;
    }
        """
    }

    public static String complexGetterCall(EntidadUtils almacen, EntidadUtils entidad) {
        if (almacen == null)
            return simpleGetterCall(entidad, true)
        return "get${entidad.clase}(${almacen.id}, ${entidad.id})";
    }


    public static String fullGetterCall(EntidadUtils almacen, EntidadUtils entidad) {
        if(almacen == null && entidad == null) return ""

        if (almacen != null) {
            return """
            ${entidad.clase} ${entidad.variableDb} = null;
            ${almacen.clase} ${almacen.variable} = null;
            if(!Messages.hasErrors()){
                $entidad.variableDb = ${ControllerUtils.complexGetterCall(almacen, entidad)};
                $almacen.variable = ${ControllerUtils.simpleGetterCall(almacen, true)};
            }
            """
        } else {
            return """
            $entidad.clase $entidad.variableDb = null;
            if(!Messages.hasErrors()){
                $entidad.variableDb = ${ControllerUtils.complexGetterCall(almacen, entidad)};
            }
            """
        }
    }

	public static String validateCopyMethod(gElemento, EntidadUtils ... entities){
		if (entities.length == 0){
			return "";
		}
		def params = [];
		validatedFields = new Stack<Set<String>>();
		validatedFields.push(new HashSet<String>());
		
		entities.each{ entity ->
			params.add("${entity.typeDb}")
			params.add("${entity.typeVariable}")
		}
		
		return """
			@Util
			protected static void ${gElemento.name}ValidateCopy(${StringUtils.params(params, gElemento.saveExtra)}){
				CustomValidation.clearValidadas();
				${validateCopy(gElemento.elementoGramatica)}
				${gElemento.saveCode.collect{ it.saveCode() }.join(";")}
			}
		"""
	}
	
    public static String validateCopyCall(gElemento, EntidadUtils ... entities){
		if (entities.length == 0){
			return "";
		}
		def params = []
        entities.each { entity ->
            params.add(entity.variableDb)
            params.add(entity.variable)
        }
		return "${gElemento.name}ValidateCopy(${StringUtils.params(params, gElemento.saveExtra.collect{it.split(" ")[1]}.unique())});"
    }


	public static String botonMethodCall(gElemento, boton, EntidadUtils ... entities){
		return "${gElemento.name}${StringUtils.firstUpper(boton)}(${StringUtils.params(gElemento.saveExtra.collect{it.split(" ")[1]}.unique())});"
	}

	
	public static String validateRulesMethod(gElemento, EntidadUtils ... entities){
		if (entities.length == 0){
			return "";
		}
		def params = [];
		validatedFields = new Stack<Set<String>>();
		validatedFields.push(new HashSet<String>());
		
		entities.each{ entity ->
			params.add("${entity.typeDb}")
			params.add("${entity.typeVariable}")
		}
		
		return """
			@Util
			protected static void ${gElemento.name}ValidateRules(${StringUtils.params(params, gElemento.saveExtra)}){
				//Sobreescribir para validar las reglas de negocio
			}
		"""
	}

	public static String validateRulesCall(gElemento, EntidadUtils ... entities){
		if (entities.length == 0){
			return "";
		}
		def params = []
		entities.each { entity ->
			params.add(entity.variableDb)
			params.add(entity.variable)
		}
		return "${gElemento.name}ValidateRules(${StringUtils.params(params, gElemento.saveExtra.collect{it.split(" ")[1]}.unique())});"
	}

	
    private static String validateCopy(objeto) {
        String out = "";
        if ((Pagina.class.isInstance(objeto)) || (Grupo.class.isInstance(objeto)) || (Popup.class.isInstance(objeto)) || Form.class.isInstance(objeto) || EntidadAutomatica.class.isInstance(objeto)) {
			
			if (Grupo.class.isInstance(objeto)) {
				// Si es un grupo debemos incluir lood IF de los mostrarSiCheck, mostrarSiCampo, ...
				if (objeto.siCombo != null) {
					if (Combo.class.isInstance(objeto.siCombo)) {
						String arrayName = "mArray"+StringUtils.getRandomName();
						CampoUtils campo = CampoUtils.create(objeto.siCombo.campo);
						out += "String[] ${arrayName} = new String[] {"+objeto.siComboValues.values.collect { '"'+it+'"' }.join(',')+"};\n";
						out += "if (Arrays.asList(${arrayName}).contains(${campo.firstLower()})) {\n";
					}
				}
				else if (objeto.siCheck != null) {
					println ("Hay un check");
					CampoUtils campo = CampoUtils.create(objeto.siCheck.campo);
					out += "if ((${campo.firstLower()} != null) && (${campo.firstLower()} == ${objeto.siCheckValues})) {\n";
				}
				else if (objeto.campo != null) {
					String arrayName = "mArray"+StringUtils.getRandomName();
					CampoUtils campo = CampoUtils.create(objeto.campo);
					out += "String[] ${arrayName} = new String[] {"+objeto.siCampoValues.values.collect { '"'+it+'"' }.join(',')+"};\n";
					out += "if (Arrays.asList(${arrayName}).contains(${campo.firstLower()})) {\n";
				}
				else if (objeto.siExpresion != null) {
					out += "if (${objeto.siExpresion}) {"
				}
			}
			
			if (objeto.permiso != null){
                out += """if (secure.check("${objeto.permiso.name}", "update", (Map<String,Long>)tags.TagMapStack.top("idParams"), null)) {\n"""
				validatedFields.push(new HashSet<String>());
			}
					
			for (Elemento elemento: objeto.elementos) {
				out += validateCopy(elemento);
			}
			
			if (Grupo.class.isInstance(objeto)) {
				// Si es un grupo debemos incluir lood IF de los mostrarSiCheck, mostrarSiCampo, ...
				if (objeto.siCombo != null) {
					if (Combo.class.isInstance(objeto.siCombo)) {
						out += "\n}";
					}
				}
				if ((objeto.siCheck != null) || (objeto.campo != null) || (objeto.siExpresion != null)) {
					out += "\n}"
				}
			}
			
            if (objeto.permiso != null) {
                out += "\n}\n"
				validatedFields.pop();
			}
        }
		else if (objeto.metaClass.respondsTo(objeto, "getCampo")) {
            if (objeto.campo != null) {
				if (objeto instanceof Solicitante) {
					CampoUtils campo = CampoUtils.create(objeto.campo);
					if (((Solicitante) objeto).representantePersonaFisica){
						out += """${campo.firstLower()}.representante.tipo = "fisica";
						"""
					}
					if (((Solicitante) objeto).elemento == "SolicitantePersonaFisica"){
						out += """${campo.firstLower()}.tipo = "fisica";
						"""
					}
					else if (((Solicitante) objeto).elemento == "SolicitantePersonaJuridica"){
						out += """${campo.firstLower()}.tipo = "juridica";
						"""
					}
				}
                out += validate(objeto);
                out += copy(objeto);
            }
        }

        return out;
    }

	
    /**
     * Devuelve el codigo de copia de un elemento de la pagina, grupo o popUp
     * @param objeto
     * @return
     */
    public static String copy(EObject objeto) {
        if ((Grupo.class.isInstance(objeto)) || (Pagina.class.isInstance(objeto))
                || (Popup.class.isInstance(objeto)) || (Wiki.class.isInstance(objeto))
                || (SubirArchivo.class.isInstance(objeto)) || (Tabla.class.isInstance(objeto))) {
            return "";
        }

        if (objeto.campo == null) {
            return "";
        }
		CampoUtils campo = CampoUtils.create(objeto.campo);
		
		// Si la referencia es un ManyToOne o ManyToMany, solo igualamos ella, no sus campos
		if (campo.getUltimoAtributo()?.type?.compound?.tipoReferencia?.type?.equals("ManyToOne")) {
			return copyCampoMany2One(campo);
		} else if (campo.getUltimoAtributo()?.type?.compound?.tipoReferencia?.type?.equals("ManyToMany")) { 
			return copyCampoMany2Many(campo);
		}
		String validOut = "";
        List<String> camposFiltrados;
		if (objeto instanceof Solicitante) {
			if (((Solicitante) objeto).elemento == "Solicitante"){
				camposFiltrados = camposSolicitante
				if (!((Solicitante) objeto).isNoRepresentante()) {
					validOut += """if (${campo.firstLower()}.isPersonaFisica()) {
					"""
					validOut += copyRepresentanteFisica (campo.str)
					if (!((Solicitante) objeto).representantePersonaFisica)
						validOut += copyRepresentanteJuridica (campo.str)
					validOut += """\n}""";
				}
			}
			else if (((Solicitante) objeto).elemento == "SolicitantePersonaFisica"){
				camposFiltrados = camposSolicitantePersonaFisica
				if (!((Solicitante) objeto).isNoRepresentante()) {
					validOut += """if (${campo.firstLower()}.isPersonaFisica()) {
					"""
					validOut += copyRepresentanteFisica (campo.str)
					if (!((Solicitante) objeto).representantePersonaFisica)
						validOut += copyRepresentanteJuridica (campo.str)
					validOut += """\n}""";
				}
			}
			else if (((Solicitante) objeto).elemento == "SolicitantePersonaJuridica"){
				camposFiltrados = camposSolicitantePersonaJuridica
				if (!((Solicitante) objeto).isNoRepresentante()) {
					validOut += """if (${campo.firstLower()}.isPersonaFisica()) {
					"""
					validOut += copyRepresentanteFisica (campo.str)
					if (!((Solicitante) objeto).representantePersonaFisica)
						validOut += copyRepresentanteJuridica (campo.str)
					validOut += """\n}""";
				}
			}
		} else if (objeto instanceof Persona) {
			camposFiltrados = camposPersona
        } else if (objeto instanceof PersonaFisica) {
            camposFiltrados = camposPersonaFisica
        } else if (objeto instanceof PersonaJuridica) {
            camposFiltrados = camposPersonaJuridica
        } else if(objeto instanceof SubirArchivoAed){
			camposFiltrados = camposAed
		} else if(objeto instanceof EditarArchivoAed) {
			camposFiltrados = camposAed
		}

		String out = "";
		if (camposFiltrados == null) {
			out = copyCamposTodos(campo);
		} else {
			out = copyCamposFiltrados(campo, camposFiltrados);
		}
		
        return out + validOut;
    }

	public static String copyCamposTodos(CampoUtils campo) {
		Entity entidad;
		Attribute last = campo.getUltimoAtributo();
		if (last != null){
			if (LedEntidadUtils.xToMany(last)){
				return "";
			}
			entidad = LedEntidadUtils.getEntidad(last);
		}
		else{
			entidad = campo.entidad;
		}
		if (entidad != null){
			String out = "";
			for (Attribute at: LedEntidadUtils.getAllDirectAttributesExceptId(entidad)){
				out += copyCamposTodos(CampoUtils.create(campo.addAttribute(at)));
			}
			return out;
		}
		else{
			return copyCampoSimple(campo);
		}
	}
	
	public static String copyCamposFiltrados(CampoUtils campo, List<String> campos) {
		String out = "";
		for (String campoStr: campos){
			CampoUtils c = CampoUtils.create(campo.addMore(campoStr));
			if (c != null){
				out += copyCampoSimple(c);
			}
		}
		return out;
	}
	
	public static String copyCampoSimple(CampoUtils campo) {
		if (campo.getUltimoAtributo()?.type?.compound?.tipoReferencia?.type?.equals("ManyToOne"))
			return copyCampoMany2One(campo);
		else if (campo.getUltimoAtributo()?.type?.compound?.tipoReferencia?.type?.equals("ManyToMany"))
			return copyCampoMany2Many(campo);
		else if (campo.getUltimoAtributo()?.type.compound?.multiple){
			return """
			db${campo.str}.retainAll(${campo.firstLower()});
			db${campo.str}.addAll(${campo.firstLower()});
			"""
		}
		return "db${campo.str} = ${campo.firstLower()};\n";
	}
	
	/**
	 * Realiza la copia de los campos Many2One. Cambiada por el problema de los IDs.
	 * @param campo
	 * @return
	 */
	public static String copyCampoMany2One(CampoUtils campo) {
		if (campo.getUltimoAtributo()?.type.compound?.multiple){
			return """
			db${campo.str}.retainAll(${campo.firstLower()});
			db${campo.str}.addAll(${campo.firstLower()});
			"""
		}
		String entity = campo.getUltimaEntidad().name;
		String str_ = campo.getStr_();
		return """
			String ${str_} = params.get("$str_");
			//CustomValidation.validValueFromTable("${campo.str}", ${str_});
			if ((${str_} != null) && (!${str_}.trim().equals(""))) {
				$entity ${str_}ctr = ${entity}.findById(Long.parseLong(${str_}.trim()));
				db${campo.str} = ${str_}ctr;
			} else {
				db${campo.str} = null;
			}
		"""; 
	}
	
	/**
	* Realiza la copia de los campos Many2Many. Cambiada por el problema de los IDs.
	* @param campo
	* @return
	*/
   public static String copyCampoMany2Many(CampoUtils campo) {
	   if (campo.getUltimoAtributo()?.type.compound?.multiple){
		   return """
		   db${campo.str}.retainAll(${campo.firstLower()});
		   db${campo.str}.addAll(${campo.firstLower()});
		   """
	   }
	   String entity = campo.getUltimaEntidad().name;
	   String str_ = campo.getStr_();
	   return """
		   ArrayList<$entity> ${str_}aCT = new ArrayList<$entity>();
		   String[] $str_ = params.getAll("$str_");
		   if ($str_ != null) {
		   		for (String idString : $str_) {
		   			$entity ctr = ${entity}.findById(Long.parseLong(idString.trim()));
					${str_}aCT.add(ctr);
				}
		   }
		   db${campo.str}.clear();
		   db${campo.str}.addAll(${str_}aCT);
	   """;
   }
	
	/**
	 * Codigo de copia y validación si el representante es una persona fisica
	 * @param campo
	 * @return
	 */
	public static String copyRepresentanteFisica (String campo) {
		return copyRepresentanteFisica (campo, "");
	}
	
	/**
	 * Código del representante si es una persona fisica (perteneciente a persona)
	 * @param campo
	 * @param persona String indicando el campo partir de "campo" anterior donde está la persona física
	 * @return
	 */
	public static String copyRepresentanteFisica (String campo, String persona) {
		def camposFJ = camposPersonaFisica
		String campol = StringUtils.firstLower(campo);
		String validOut = """
			// Para un representante físico
			if ((${campol}${persona}.representado != null) && (${campol}${persona}.representado) && (${campol}${persona}.representante.isPersonaFisica())) {
			"""
		validOut += "db${campo}${persona}.representante.tipo = ${campol}${persona}.representante.tipo;"
		validOut += "if (db${campo}${persona}.representante == null) db${campo}${persona}.representante = ${campol}${persona}.representante;\n"
		camposFJ.each {field -> validOut += "db${campo}${persona}.representante.fisica.${field} = ${campol}${persona}.representante.fisica.${field};\n"}
		validOut += "\n}";
	}
	
	/**
	 * Código del representante si es una persona jurídica
	 * @param campo
	 * @param persona
	 * @return
	 */
	public static String copyRepresentanteJuridica (String campo) {
		return copyRepresentanteJuridica (campo, "");
	}
	
	/**
	 * Código del representante si es una persona jurídica
	 * @param campo
	 * @return
	 */
	public static String copyRepresentanteJuridica (String campo, String persona) {
		def camposFJ = camposPersonaJuridica
		String campol = StringUtils.firstLower(campo);
		String validOut = """
			// Para un representante jurídico
			if ((${campol}${persona}.representado != null) && (${campol}${persona}.representado) && (${campol}${persona}.representante.isPersonaJuridica())) {
			"""
		validOut += "db${campo}${persona}.representante.tipo = ${campol}${persona}.representante.tipo;"
		validOut += "if (db${campo}${persona}.representante == null) db${campo}${persona}.representante = ${campol}${persona}.representante;\n"
		camposFJ.each {field -> validOut += "db${campo}${persona}.representante.juridica.${field} = ${campol}${persona}.representante.juridica.${field};\n"}
		validOut += "\n}";
	}
	
	
	public static String copyRepresentantes (String campo) {
		
	}
	
	/**
     * Devuelve el código de validación para un objeto
     * @param object
     * @return
     */
    public static String validate(objeto) {
        String out = "";
		
        if (objeto.metaClass.respondsTo(objeto, "getCampo") && (!Tabla.class.isInstance(objeto))) {
			String campo = CampoUtils.create(objeto.campo).str;
			String campol = StringUtils.firstLower(campo);
			
			if (ModelUtils.isCheckEntity(objeto)) {
                out += valid(campo);
            } else {
                // Debemos validar normalmente (sus entidades padre)
                int dotPlace = campol.length();
                dotPlace = campol.lastIndexOf('.', dotPlace - 1);
                while (dotPlace != -1) {
                    String lCampo = campol.substring(0, dotPlace);
                    out += valid(lCampo);
                    dotPlace = campol.lastIndexOf('.', dotPlace - 1);
                }
            }
			
			if ((objeto instanceof Solicitante)) {
                out += validValueFromTable(campo + ".tipo");
				// Debemos validar el representante de la persona física, si lo tiene
				out += """if (${campol}.isPersonaFisica() && (${campol}.representado != null) && (${campol}.representado)) {
					${required(campo + ".representante")}
					${valid(campo + ".representante")}
				}
				"""
			} else if ((objeto instanceof Persona)) {
                out += validValueFromTable(campo + ".tipo");
			} else if (objeto instanceof Direccion) {
                out += validValueFromTable(campo + ".municipio");
				if (objeto.isProvincia())
                	out += validValueFromTable(campo + ".provincia");
				if (objeto.isPais())
                	out += validValueFromTable(campo + ".pais");
			}
			if (objeto instanceof Combo){
				Combo combo = (Combo)objeto;
				if (LedCampoUtils.getUltimoAtributo(combo.campo).type.compound?.multiple){
					out += validListOfValuesFromTable(campo);
				}
				else{
					out += validValueFromTable(campo);
				}
			}
			if (objeto.metaClass.respondsTo(objeto, "isRequerido") && objeto.isRequerido()) {
				out += required(campo);
			}

        }
        return out;
    }
	
	
    public static String permisoContent(Permiso permiso) {
		String permisoContent = "";
		if(permiso != null){
			String name = permiso.name;
			permisoContent = """accion = secure.transform(accion);
				Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
				Map<String, Object> vars = null;
				return secure.check("${name}", accion, ids, vars);"""
		}else{
			permisoContent = """//Sobreescribir para incorporar permisos a mano
			return true;"""
		}
		return permisoContent;
	}
	
	
	private static refPaginaInternal(Pagina pagina){
		String ref = "";
		String entidad = pagina.eContainer().name;
		String link = pagina.name;
		
		// Si conocemos la entidad, la colocamos en el enlace (solo formularios "coj****"
		if ((entidad != null) && (entidad.equals("Solicitud"))) {
			ref = "${link}Controller.index(id${entidad})"
		} else {
			ref = "${link}Controller.index()"
		}
		return ref;
	}
	
	public static refPagina(Pagina pagina) {
		String ref = refPaginaInternal(pagina);
		return "@{${ref}}"
	}
	
	public static refPaginaAction(Pagina pagina){
		String ref = refPaginaInternal(pagina);
		return "@" + ref;
	}

	private static String valid(String campo){
		campo = StringUtils.firstLower(campo);
		for (Set<String> set: validatedFields){
			if (set.contains(campo)){
				return "";
			}
		}
		validatedFields.peek().add(campo);
		return "CustomValidation.valid(\"${campo}\", ${campo});\n";
	}
	
	private static String required(String campo){
		campo = StringUtils.firstLower(campo);
		return "CustomValidation.required(\"${campo}\", ${campo});\n";
	}
	
	private static String validValueFromTable(String campo){
		campo = StringUtils.firstLower(campo);
		return "CustomValidation.validValueFromTable(\"${campo}\", ${campo});\n";
	}
	
	private static String validListOfValuesFromTable(String campo){
		campo = StringUtils.firstLower(campo);
		return "CustomValidation.validListOfValuesFromTable(\"${campo}\", ${campo});\n";
	}
	
}
