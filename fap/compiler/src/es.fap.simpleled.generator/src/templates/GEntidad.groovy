package templates;

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

public class GEntidad extends GElement{
	
	Entity entity;
	String moreImports;
	boolean incluirPostInit;
	ArrayList<Attribute> attributosAdd;
	
	public GEntidad(Entity entity, GElement container){
		super(entity, container);
		this.entity = entity;
	}
	
	public void generate(){
		String extendz;
		moreImports = "";
		incluirPostInit=false;
		
		if (entity.name.equals("Solicitud")){
			solicitudStuff();
		}
		
		if (entity.getExtends() != null){
			extendz = "extends "+entity.getExtends().name;
			incluirPostInit=true;
			for(Entity ent: getExtendsRecursively(entity)){
				if (ent.embedded){
					incluirPostInit=false;
					break;
				}
			}
		} else {
			if (entity.embedded)
				extendz = "";
			else {
				extendz = "extends FapModel";
				incluirPostInit=true;
			}
		}
		
		String doc = LedDocumentationUtils.findComment(entity);
		
		/// PERSISTENCIA
		/// Si es una entidad "NonPersist" no debemos establecerle la anotación @Entity
		String auditable = ""
		String persist = "@Entity";
		if (entity.auditable){
			auditable = "@Auditable";
		}
		if (entity.nonPersist){
			persist = "";
			auditable = "";
		}
		if (entity.embedded){
			persist = "@Embeddable";
			auditable = "";
		}
		if (entity.superClass){
			persist = "@MappedSuperclass";
			auditable = "";
		}
		if (entity.inheritance) {
			// Por ahora solo tenemos este tipo
			persist += "\n@Inheritance(strategy=InheritanceType.JOINED)";
		}
			
		if ((entity?.tableName != null) && (!entity?.tableName.trim().equals(""))) {
			persist += "\n@Table(name=\"" + entity?.tableName + "\")";
		}
		
		String attributesCode = """// Código de los atributos""";
		for(Attribute attr : entity.attributes){
			attributesCode += generate(attr);
		}
		String gettersAttrMonedaType = "";
		if (attributosAdd != null) {
			for (Attribute attr : attributosAdd) {
				gettersAttrMonedaType += getMonedaType(attr);
				attributesCode += generate(attr);
			}
			entity.getAttributes().addAll(attributosAdd);
		}
		
		String file = FileUtils.getRoute('MODEL')+ entity.name + ".java";
		
		String initCode = generateInit();
		
		
		String out = """
package models;

import java.util.*;
import javax.persistence.*;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.data.validation.*;
import org.joda.time.DateTime;
import models.*;
import messages.Messages;
import validation.*;
import audit.Auditable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
$moreImports

${FileUtils.addRegion(file, FileUtils.REGION_IMPORT)}	
${doc}
${auditable}
${persist}
public class ${entity.name} ${extendz} {
	${attributesCode}
	${gettersAttrMonedaType}
	${initCode}
	${savePagesPrepared()}
${FileUtils.addRegion(file, FileUtils.REGION_MANUAL)}	
	
	}
		"""
		
		FileUtils.overwrite(file, BeautifierUtils.formatear(out));
		return;
	}
	
	/**
	 * Genera el getterEspecialPara las tablas del tipo Moneda
	 * @param attribute
	 * @return
	 */
	private String getMonedaType (Attribute attribute) {
		String nameSin = attribute.name.split("_formatFapTabla")[0];
		String ret = """
			// Getter del atributo del tipo moneda
			public String get${StringUtils.firstUpper(attribute.name)} () {
				return format.FapFormat.formatMoneda(${nameSin});
			}
		"""
	}
	
	private String generate(Attribute attribute){
	
		if (attribute.name.equals("id")){
			return "";
		}
		
		String type;
		String referenceTypeTransient;
		String name = attribute.name;
		List<String> anotaciones = new ArrayList<String>();
		List<String> anotacionesJPA = new ArrayList<String>(); // Éstas anotaciones no se utilizarán si el atributo es Transient
		List<String> columnAnotations = new ArrayList<String>();
		
		String cascadeType = "cascade=CascadeType.ALL,";
		if (attribute.cascade != null ){
			if (attribute.cascade.type != null){
			    if (attribute.cascade.type.equals("NONE"))
					cascadeType = "";
			} else {
				String [] cascadeListSimpleType = attribute.cascade.simpleType.getList();
				if (cascadeListSimpleType.size() == 1){
					cascadeType = "cascade=CascadeType."+cascadeListSimpleType[0]+", ";
				} else {
					cascadeType = "cascade={CascadeType."+cascadeListSimpleType[0];
					for (int i=1; i<cascadeListSimpleType.size(); i++){
						cascadeType += ", CascadeType."+cascadeListSimpleType[i];
					}
					cascadeType += "}, ";
				}
			}
		}
		
		if(attribute.type.compound?.entidad?.name.equals("Nip")){
			anotaciones.add "@CheckWith(NipCheck.class)"
		}
		
		// Manual validator
		if ((attribute?.checkWith != null) && (!attribute?.checkWith.trim().equals(""))) {
			anotaciones.add("@CheckWith("+attribute?.checkWith+".class)");
			moreImports += "import "+attribute?.checkWith+";\n";
		}
		
		if(attribute.type.simple != null){
			//Atributo de tipo simple
			type = attribute.type.simple.type;
			if(type.equals("Email")){
				type = "String";
				anotaciones.add "@Email";
			}else if(type.equals("Moneda")){
				type = "Double";
				anotaciones.add "@Moneda";
				// Creamos el atributo con el formatDeMoneda
				Attribute formatMoneda = LedFactory.eINSTANCE.createAttribute();
				formatMoneda.type = LedFactory.eINSTANCE.createType();
				formatMoneda.type.simple = LedFactory.eINSTANCE.createSimpleType();
				formatMoneda.type.simple.type = "String";
				formatMoneda.name = attribute.getName()+"_formatFapTabla";
				formatMoneda.isTransient = true;
				if (attributosAdd == null)
					attributosAdd = new ArrayList<Attribute>();
				attributosAdd.add(formatMoneda);
			}else if(type.equals("Telefono")){
				type="String";
			}else if(type.equals("LongText")) {
				type="String";
				columnAnotations.add("columnDefinition=\"LONGTEXT\"");
			} else if (type.equals("Cif")) {
				type="String";
				anotaciones.add "@CheckWith(CifCheck.class)"
			}
						
		} else if (attribute.type.special != null) {
			type = attribute.type.special.type;
			if(type.equals("Email")){
				type = "String";
				anotaciones.add "@Email";
			}else if(type.equals("Moneda")){
				type = "Double";
				anotaciones.add "@Moneda";
				// Creamos el atributo con el formatDeMoneda
				Attribute formatMoneda = LedFactory.eINSTANCE.createAttribute();
				formatMoneda.type = LedFactory.eINSTANCE.createType();
				formatMoneda.type.simple = LedFactory.eINSTANCE.createSimpleType();
				formatMoneda.type.simple.type = "String";
				formatMoneda.name = attribute.getName()+"_formatFapTabla";
				formatMoneda.isTransient = true;
				if (attributosAdd == null)
					attributosAdd = new ArrayList<Attribute>();
				attributosAdd.add(formatMoneda);
			}else if(type.equals("Telefono")){
				type="String";
			}else if(type.equals("DateTime")){
				anotaciones.add("""@org.hibernate.annotations.Columns(columns={@Column(name="${name}"),@Column(name="${name}TZ")})""")
				anotaciones.add("""@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")""")
			} else if (type.equals("Cif")) {
				type="String";
				anotaciones.add "@CheckWith(CifCheck.class)"
			}
			
		}else{
			//Atributo compuesto
			CompoundType compuesto = attribute.type.compound;
			
			
			if(compuesto.lista != null){
				//Listas
				
				if (compuesto.multiple){
					anotaciones.add("@ElementCollection");
					type = "Set<String>";
				}
				else{					
					type = "String";
				}
				anotaciones.add("@ValueFromTable(\"${compuesto.lista.name}\")");
			}else if (compuesto?.entidad?.embedded){
				type = compuesto.entidad.name;
				anotaciones.add "@Embedded";
			}else if (compuesto.collectionType != null) {
				//Colecciones
				anotaciones.add("@ElementCollection");
				type = "${compuesto.collectionType.type}<${compuesto.collectionReferencia.type}>";
			}
			else{
				//Referencia
				String tipoReferencia = compuesto.tipoReferencia?.type ?: "OneToOne" //Si no especifica tipo es una OneToOne
				referenceTypeTransient = tipoReferencia;
				if (tipoReferencia.equals("ManyToOne")) { // Si es ManyToOne, no ponemos la anotacion de cascade
					anotacionesJPA.add "@${tipoReferencia}(fetch=FetchType.LAZY)"
				} else {
					anotacionesJPA.add "@${tipoReferencia}(${cascadeType} fetch=FetchType.LAZY)"
				}
				type= compuesto.entidad.name;
				if(LedEntidadUtils.xToMany(attribute)){
					type = "List<${type}>"
					anotacionesJPA.add """@JoinTable(name="${entity.name.toLowerCase()}_${attribute.name.toLowerCase()}")"""
				}

			}
			
			if ((attribute.defaultValue != null) && (compuesto.lista == null)) {
				System.out.println("WARNING: A los atributos de tipo compuesto que no sean listas no se les permite indicarle un valor por defecto (Entidad: "+entity.name+", Atributo: "+attribute.name+")")
			}
		}
		
		if(attribute.required){
			anotaciones.add "@Required"
		}
		
		if (attribute.column != null)
			columnAnotations.add("name=\""+attribute.column+"\"");
			
		// Si tiene atributo length (sólo los de tipo String y LongText -> la comprobación se hace en el editor)
		if (attribute.hasLength)
			columnAnotations.add("length = "+attribute.length);

		// Si tiene atributo validate
		if ((attribute.validate != null) && (attribute.type.simple != null)) {
			String validType = attribute.type.simple.type;
			if ((validType.equals("String")) || ((validType.equals("LongText")))) {
				anotaciones.add("@Match(\"${attribute.validate}\")");
			}
			else if ((validType.equals("Long")) || (validType.equals("Integer")) || (validType.equals("Double"))) {
				anotaciones.add("@Range(${attribute.validate})")
			}
			else if (validType.equals("Boolean") || validType.equals("boolean")) {
				anotaciones.add("@IsTrue")
			}
		}
		// Anotaciones que deben ir en @Column
		if (columnAnotations?.size() > 0) {
			anotaciones.add("""@Column(${columnAnotations.join(",")})""");
		}
		
		// Si el atributo es transient, eliminamos todas las anotaciones
		String transientStr = "";
		if (attribute.isTransient) {
			anotacionesJPA.clear();
			transientStr = "@Transient";
			// Ponemos el tipo de referencia para que si alguien asigna un transient en un combo, ese combo sepa que tipo de referencia es, en el combo.html, y cargue los items correctamente
			if (referenceTypeTransient != null)
				anotacionesJPA.add("@${referenceTypeTransient}");
		}

		// Si el atributo tiene comentarios
		String comments = LedDocumentationUtils.findComment(attribute);
		
		return """
	$comments
	${anotacionesJPA.join('\n	')}
	${anotaciones.join('\n	')}
	${transientStr}
	public ${type} ${name};
		""";
	}
	
	private String generateInit(){
		String refInit = "";
		for(Attribute attribute : entity.attributes){
			CompoundType compuesto = attribute.type.compound;
			SpecialType special = attribute.type.special;
			String tipo = compuesto?.entidad?.name;
			// Si el atributo es Transient, SI necesita init
			// En una versión anterior no se estaba haciendo y el objeto era null
			if (compuesto?.entidad?.embedded){
				refInit += """
			if (${attribute.name} == null)
				${attribute.name} = new ${compuesto.entidad.name}();
			""";
			}
			else if (compuesto?.multiple?.equals(true)){
				refInit += """
			if (${attribute.name} == null)
				${attribute.name} = new HashSet<String>();
			""";
			}
			else if(tipo != null && !attribute.noConstruct){
				if(LedEntidadUtils.xToMany(attribute)){
					refInit += """
						if (${attribute.name} == null)
							${attribute.name} = new ArrayList<${tipo}>();
						""";
				}else{
					if(LedEntidadUtils.isOneToOne(attribute)){
						refInit += """
							if (${attribute.name} == null)
								${attribute.name} = new ${tipo}();
							else
								${attribute.name}.init();
						""";
					}else{ 
						//Las referencia ManyToOne no se inicializan
						refInit += """
							if (${attribute.name} != null)
								${attribute.name}.init();	
						"""
					}
				}
			}
			
			/** Valores por defecto de los atributos */
			if (attribute.defaultValue != null) {
				/** Valores por defecto para los tipos simples */
				if (attribute?.type?.simple != null) { 
					tipo = attribute?.type?.simple?.type;
					if (!tipo.equals("boolean") && !defaultValue(attribute.defaultValue, tipo, attribute.name).isEmpty()){
						refInit += """
							if (${attribute.name} == null)
						""";
					}
					refInit += defaultValue(attribute.defaultValue, tipo, attribute.name);
					
				} else if (attribute?.type?.special != null) {
					/** Valores por defecto para tipos especiales */
					tipo = attribute?.type?.special?.type;
					if (tipo.equals("Telefono")) {
						if (!defaultValue(attribute.defaultValue, "String", attribute.name).isEmpty()){
							refInit += """
								if (${attribute.name} == null)
							""";
						}
						refInit += defaultValue(attribute.defaultValue, "String", attribute.name);
					} else if (tipo.equals("Email")) {
						if (isValidEmailAddress((String)attribute.defaultValue)){
							if (!defaultValue(attribute.defaultValue, "String", attribute.name).isEmpty()){
								refInit += """
									if (${attribute.name} == null)
								""";
							}
							refInit += defaultValue(attribute.defaultValue, "String", attribute.name);
						} else
							println """WARNING: "${attribute.defaultValue}" no es una dirección email válida para ${entity.name}.${attribute.name}""";
					} else if (tipo.equals("Cif")) {
						// TODO: Validar el CIF
						if (!defaultValue(attribute.defaultValue, "String", attribute.name).isEmpty()){
							refInit += """
								if (${attribute.name} == null)
							""";
						}
						refInit += defaultValue(attribute.defaultValue, "String", attribute.name);
					} else if (tipo.equals("Moneda")) {
						if (!defaultValue(attribute.defaultValue, "Double", attribute.name).isEmpty()){
							refInit += """
								if (${attribute.name} == null)
							""";
						}
						refInit += defaultValue(attribute.defaultValue, "Double", attribute.name);
					}  else if (tipo.equals("DateTime")) {
						// TODO: Validar el DateTime
						if (!defaultValue(attribute.defaultValue, tipo, attribute.name).isEmpty()){
							refInit += """
								if (${attribute.name} == null)
							""";
						}
						refInit += defaultValue(attribute.defaultValue, tipo, attribute.name);
					}
				} else {
					// Valor por defecto de una lista definida (no en otro fichero)
					if ((attribute?.type?.compound?.lista != null) && (!attribute?.type?.compound?.isMultiple())) {
						String tabla = attribute?.type?.compound?.lista.name;
						String value = attribute.defaultValue;
						// Error al iniciar por primera vez la app -> refInit += """if (TableKeyValue.contains("${tabla}", "${value}"))""";
						refInit +=  """	if (${attribute.name} == null)
											${attribute.name} = "${value}";
							""";
					} else {
						println "WARNING: Valor por defecto no permitido en este tipo";
					}
				}
			}
		}
		
		//Si la clase no tiene referencias miramos si es embedded
		if(refInit.isEmpty()) {
			if (entity?.embedded) {               // hay q inicialiar algun atributo para
				for(Attribute attribute : entity.attributes){  // que se pueda guardar en BD
					if (attribute.type.simple != null) {
						refInit += """
		if (${attribute.name} == null)
			${attribute.name} = new ${attribute.type.simple.type}();""";
						break;
					}
					if (attribute.type.special != null) {
						refInit += """
		if (${attribute.name} == null)
			${attribute.name} = new ${attribute.type.special.type}();""";
						break;
					}
				}
			}
//			else if (entity.getExtends() == null) {
//				return ""; // no genera constructor
//			}
		}
		
		
		//Constructor para inicializar las referencias
		String out = ""
		if (!entity.noConstruct && !refInit.isEmpty()) {
			out += """
	public ${entity.name} (){
		init();
	}
	"""
		}
		String postInit="";
		if (incluirPostInit)
			postInit="postInit();"
		out += """

	public void init(){
		${entity.getExtends()? "super.init();": ""}
		${refInit}
		${postInit}
	}
		"""
		return out;
	}
	
	private String savePagesPrepared() {
		if (!entity.name.equals("Solicitud"))
			return "";
		String out = "public void savePagesPrepared () {"
		for (PaginaImpl pag: ModelUtils.getVisibleNodes(LedFactory.eINSTANCE.getLedPackage().getPagina(), LedUtils.resource)){
			if (!pag.guardarParaPreparar)
				continue;
			String name = "pagina" + pag.name;
			String title = name;
			if ((pag.titulo != null) && (!pag.titulo.isEmpty()))
				title = pag.titulo;
			out += """
				if ((savePages.${name} == null) || (!savePages.${name}))
					Messages.error("La página ${title} no fue guardada correctamente");
			"""
		}
		out += "}";
		return out;
	}
	
	private Entity getEntitySavePages(){
		EntityImpl savePages = LedFactory.eINSTANCE.createEntity();
		savePages.setName("SavePages");
	
		for (PaginaImpl pag: ModelUtils.getVisibleNodes(LedFactory.eINSTANCE.getLedPackage().getPagina(), LedUtils.resource)){
			if (!pag.guardarParaPreparar){
				continue;
			}
			Type tipoBoolean = LedFactory.eINSTANCE.createType();
			tipoBoolean.setSimple(LedFactory.eINSTANCE.createSimpleType());
			tipoBoolean.getSimple().setType("Boolean");
			AttributeImpl at = LedFactory.eINSTANCE.createAttribute();
			at.setName("pagina" + pag.name);
			at.setType(tipoBoolean);
			savePages.getAttributes().add(at);
		}
		return savePages;
	}
	
	private void solicitudStuff(){
		if (!entity.getExtends()?.name.equals("SolicitudGenerica"))
			entity.setExtends(ModelUtils.getVisibleNode(LedFactory.eINSTANCE.getLedPackage().getEntity(), "SolicitudGenerica", LedUtils.resource));
		Entity savePages = getEntitySavePages();
		GElement.getInstance(savePages, null).generate();
		Type tipo = LedFactory.eINSTANCE.createType();
		CompoundType compound = LedFactory.eINSTANCE.createCompoundType();
		compound.setEntidad(savePages);
		tipo.setCompound(compound);
		Attribute at = LedFactory.eINSTANCE.createAttribute();
		at.setName("savePages");
		at.setType(tipo);
		entity.getAttributes().add(at);
	}
	
	/**
	 * Devuelve el string que asigna el valor por defecto
	 * @param value Valor por defecto asignado
	 * @param type Tipo del atributo de la entidad
	 * @param name Atributo de la entidad
	 * @return
	 */
	private String defaultValue(String value, String type, String name) {
		if ((value != null)) {
			def defaultValue = value;
			if (type.equals("Double")) {
				defaultValue = Double.parseDouble(value);
			} else if (type.equals("Integer")) {
				defaultValue = Integer.parseInt(value);
			} else if (type.equals("Long")) {
				defaultValue = Long.parseLong(value) + "L";
			}
		
			if (defaultValue != null) {
				if (type.equals("String") || (type.equals("LongText")))
					defaultValue = "\"${defaultValue}\"";
				else if (type.equals("DateTime")) {
					return """try {
	${name} = new DateTime ((new SimpleDateFormat("dd/MM/yyyy")).parse("${defaultValue}"));
} catch (ParseException e) {
	e.printStackTrace();
}
					"""
				}
				return  """${name} = ${defaultValue};\n"""
			}
		}
		return ""
	}
	
	/**
	 * Indica se el string pasado es un email válido
	 */
	public boolean isValidEmailAddress(String emailAddress){
		String  expression="^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}\$";
		CharSequence inputStr = emailAddress;
		Pattern pattern = Pattern.compile(expression,Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		return matcher.matches();
	}
	
	public List<Entity> getExtendsRecursively(Entity entity){
		List<Entity> extendsList = new ArrayList<Entity>();
		while (entity != null){
			extendsList.add(entity);
			entity = entity.getExtends();
		}
		return extendsList;
	}
	
}
