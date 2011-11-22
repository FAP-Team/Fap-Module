
package templates;

import generator.utils.FileUtils;
import generator.utils.LedUtils;
import generator.utils.HashStack;
import generator.utils.HashStack.HashStackName;
import generator.utils.StringUtils;

import es.fap.simpleled.led.*;
import es.fap.simpleled.led.impl.AttributeImpl
import es.fap.simpleled.led.impl.CompoundTypeImpl
import es.fap.simpleled.led.impl.EntityImpl
import es.fap.simpleled.led.impl.LedFactoryImpl
import es.fap.simpleled.led.impl.PaginaImpl
import es.fap.simpleled.led.impl.TypeImpl
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.eclipse.emf.ecore.EObject
import es.fap.simpleled.led.util.LedDocumentationUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class GEntidad {
	
	public static String moreImports = "";
	
	public static String generate(Entity entity){
		String extendz;
		String attrSolicitud;
		
		if (entity.name.equals("Solicitud")){
			solicitudStuff(entity);
		}
		
		if (entity.getExtends() != null){
			extendz = "extends "+entity.getExtends().name;
		} else {
			if (entity.embedded)
				extendz = "";
			else
				extendz = "extends Model";
		}
		
		String doc = LedDocumentationUtils.findComment(entity);
		
		/// PERSISTENCIA
		/// Si es una entidad "NonPersist" no debemos establecerle la anotación @Entity
		String auditable = "@Auditable"
		String persist = "@Entity";
		if (entity.noAuditable){
			auditable = "";
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
			attributesCode += generate(entity, attr);
		}
		
		String file = FileUtils.getRoute('MODEL')+ entity.name + ".java";
		
		String initCode = generateInit(entity);
		
		
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
${FileUtils.addRegion(file, FileUtils.REGION_IMPORT)}	
${doc}
${auditable}
${persist}
public class ${entity.name} ${extendz} {
	${attributesCode}
	${initCode}
	${savePagesPrepared(entity)}
${FileUtils.addRegion(file, FileUtils.REGION_MANUAL)}	
	
	}
		"""
		
		FileUtils.overwrite(file, out);
		return;
	}
	
	private static String generate(Entity entity, Attribute attribute){
	
		if (attribute.name.equals("id")){
			return "";
		}
		
		String type;
		String name = attribute.name;
		List<String> anotaciones = new ArrayList<String>();
		List<String> columnAnotations = new ArrayList<String>();
		
		String cascadeType = "cascade=CascadeType.ALL,";
		if (attribute.noCascade) {
			cascadeType = "";
		}
		
		if(attribute.type.compound?.entidad?.name.equals("Nip")){
			anotaciones.add "@CheckWith(NipCheck.class)"
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
				anotaciones.add "@${tipoReferencia}(${cascadeType} fetch=FetchType.LAZY)"
				type= compuesto.entidad.name;
				if(LedEntidadUtils.xToMany(attribute)){
					type = "List<${type}>"
					anotaciones.add """@JoinTable(name="${entity.name.toLowerCase()}_${attribute.name.toLowerCase()}")"""
				}

			}
			
			if ((attribute.defaultValue != null)) {
				System.out.println("WARNING: A los atributos de tipo compuesto no se les permite indicarle un valor por defecto (Entidad: "+entity.name+", Atributo: "+attribute.name+")")
			}
		}
		
		if(attribute.required){
			anotaciones.add "@Required"
		}
		
		if (attribute.column != null)
			anotaciones.add("""@Column(name="${attribute.column}")""");
		
		// Si el atributo es transient
		if (attribute.isTransient)
			anotaciones.add("@Transient");
			
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
			else if (validType.equals("Boolean")) {
				anotaciones.add("@IsTrue")
			}
		}
		// Anotaciones que deben ir en @Column
		if (columnAnotations?.size() > 0) {
			anotaciones.add("""@Column(${columnAnotations.join(",")})""");
		}

		// Si el atributo tiene comentarios
		String comments = LedDocumentationUtils.findComment(attribute);
		
		String out =
	"""
	$comments
	${anotaciones.join('\n	')}
	public ${type} ${name};
	"""
		return out;
	
	}
	
	private static String generateInit(Entity entity){
		String refInit = "";
		for(Attribute attribute : entity.attributes){
			CompoundType compuesto = attribute.type.compound;
			String tipo = compuesto?.entidad?.name;
			if (compuesto?.entidad?.embedded){
				refInit += """
			if (${attribute.name} != null)
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
					refInit += defaultValue(attribute.defaultValue, tipo, attribute.name);
					
				} else if (attribute?.type?.special != null) {
					/** Valores por defecto para tipos especiales */
					tipo = attribute?.type?.special?.type;
					if (tipo.equals("Telefono")) {
						refInit += defaultValue(attribute.defaultValue, "String", attribute.name);
					} else if (tipo.equals("Email")) {
						if (isValidEmailAddress((String)attribute.defaultValue)) {
							refInit += defaultValue(attribute.defaultValue, "String", attribute.name);
						} else {
							println "WARNING: El valor por defecto para email no es correcto";
						}
					} else if (tipo.equals("Cif")) {
						// TODO: Validar el CIF
						refInit += defaultValue(attribute.defaultValue, "String", attribute.name);
					} else if (tipo.equals("Moneda")) {
						refInit += defaultValue(attribute.defaultValue, "Double", attribute.name);
					}  else if (tipo.equals("DateTime")) {
						// TODO: Validar el DateTime
						refInit += defaultValue(attribute.defaultValue, tipo, attribute.name);
					}
				} else {
					// Valor por defecto de una lista definida (no en otro fichero)
					if ((attribute?.type?.compound?.lista != null) && (!attribute?.type?.compound?.isMultiple())) {
						String tabla = attribute?.type?.compound?.lista.name;
						String value = attribute.defaultValue;
						// Error al iniciar por primera vez la app -> refInit += """if (TableKeyValue.contains("${tabla}", "${value}"))""";
						refInit +=  """	${attribute.name} = "${value}";""";
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
		
		out += """

	public void init(){
		${entity.getExtends() != null? "super.init();": ""}
		${refInit}
	}
		"""
		return out;
	}
	
	private static String savePagesPrepared(Entity entity) {
		if (!entity.name.equals("Solicitud")){
			return "";
		}
		String out = "";
		out += """
		
		public void savePagesPrepared () {
			"""
		for (PaginaImpl pag: LedUtils.getNodes(PaginaImpl)){
			if (!pag.guardarParaPreparar){
				continue;
			}
			String name = "pagina" + pag.name;
			out += """if ((savePages.${name} == null) || (!savePages.${name}))
					Messages.error("La página ${name.substring(6)} no fue guardada correctamente");
			"""
		}
		out += """}
			""";
		return out;
	}
	
	private static Entity getEntitySavePages(){
		LedFactoryImpl factory = new LedFactoryImpl();
		EntityImpl savePages = factory.createEntity();
		savePages.setName("SavePages");
	
		for (PaginaImpl pag: LedUtils.getNodes(PaginaImpl)){
			if (!pag.guardarParaPreparar){
				continue;
			}
			Type tipoBoolean = factory.createType();
			tipoBoolean.setSimple(factory.createSimpleType());
			tipoBoolean.getSimple().setType("Boolean");
			AttributeImpl at = factory.createAttribute();
			at.setName("pagina" + pag.name);
			at.setType(tipoBoolean);
			savePages.getAttributes().add(at);
		}
		return savePages;
	}
	
	private static void solicitudStuff(Entity solicitud){
		HashStack.push(HashStackName.SOLICITUD, solicitud);
		if (!solicitud.getExtends()?.name.equals("SolicitudGenerica")){
			solicitud.setExtends(LedUtils.getNode(Entity, "SolicitudGenerica"));
		}
		Entity savePages = getEntitySavePages();
		GEntidad.generate(savePages);
		Type tipo = new TypeImpl();
		CompoundType compound = new CompoundTypeImpl();
		compound.setEntidad(savePages);
		tipo.setCompound(compound);
		Attribute at = new AttributeImpl();
		at.setName("savePages");
		at.setType(tipo);
		solicitud.getAttributes().add(at);
	}
	
	/**
	 * Devuelve el string que asigna el valor por defecto
	 * @param value Valor por defecto asignado
	 * @param type Tipo del atributo de la entidad
	 * @param name Atributo de la entidad
	 * @return
	 */
	private static String defaultValue(String value, String type, String name) {
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
	public static boolean isValidEmailAddress(String emailAddress){
		String  expression="^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}\$";
		CharSequence inputStr = emailAddress;
		Pattern pattern = Pattern.compile(expression,Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		return matcher.matches();
	  }
	
	
}
