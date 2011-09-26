package es.fap.simpleled.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.validation.Check;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.AttributeAnotations;
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.CampoAtributos;
import es.fap.simpleled.led.Columna;
import es.fap.simpleled.led.CompoundType;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.LedFactory;
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.Model;
import es.fap.simpleled.led.impl.EntityImpl;
import es.fap.simpleled.led.impl.LedFactoryImpl;
import es.fap.simpleled.scoping.LedScopeProvider;
 

public class LedJavaValidator extends AbstractLedJavaValidator {
	
	/**
	 * Comprueba que el atributo length sea sólamente aplicado a campos String o LongText
	 * (en los demás no tiene sentido)
	 * @param attributeAnotations
	 */
	@Check
	public void checkLengthOnlyString (AttributeAnotations attributeAnotations) {
		
		String tipo = ((Attribute)(attributeAnotations.eContainer())).getType().getSimple();
		if ( (attributeAnotations.isHasLength()) && 
			 ( tipo == null || !(tipo.equals("String")) || !(tipo.equals("LongTexto")) ) ){
//			error("Anotado con la propiedad \"length\" un atributo que no es del tipo \"String\"", LedPackage.ATTRIBUTE__NAME);
		}
		
	}

	/**
	 * Comprueba que las entidades empiecen con mayúscula
	 * @param entity
	 */
	@Check
	public void checkEntitiesStartsWithCapital (Entity entity) {
		if (!Character.isUpperCase(entity.getName().charAt(0))){
			//warning("El nombre de la entidad \""+entity.getName()+"\" debe empezar con mayúscula", entity, LedPackage.ATTRIBUTE__NAME);
			String name = entity.getName();
			entity.setName(name.substring(0, 1).toUpperCase() + name.substring(1));
		}
	}
	
	
	/**
	 * Comprueba que la refencia de un atributo no sea a la entidad que lo contiene
	 * @param attribute
	 */
	@Check
	public void checkReferencesToEntities (Attribute attribute) {
		Entity father = ((Entity)(attribute.eContainer()));
		if (EntityImpl.class.isInstance(attribute.getType().getCompound().getEntidad())) {
//			if ((father.getName().equals(((EntityImpl)attribute.getType().getCompound().getEntidad()).getName())))
//				error("La entidad "+father.getName()+" no se debe referenciar a si misma mediante un atributo", LedPackage.ATTRIBUTE__TYPE);
		}
	}
	
	/**
	 * Comprueba que los valores por defecto se establezacan solamente a tipos simples
	 * @param attribute
	 */
	@Check
	public void checkDefaultValueOnlySimpleType (Attribute attribute) {
		if (attribute.getDefaultValue() != null) {
			CompoundType compound = attribute.getType().getCompound();
			if (compound != null) {
//				if (compound.getEntidad() != null)
//					warning ("Valores por defecto no aplicables sobre el tipo "+attribute.getType().getCompound().getEntidad().getName()+" (solo aplicables a tipos simples)", LedPackage.ATTRIBUTE__TYPE);
//				else
//					warning ("Valores por defecto no aplicables sobre listas (solo aplicables a tipos simples)", LedPackage.ATTRIBUTE__TYPE);
			}
		}
	}
	
	@Check
	public void checkEntityHasId(Entity entidad) {
		LedScopeProvider.addId(entidad);
	}
	
	@Check
	public void checkSolicitudSimpleAttributos(Attribute attr) {
		Entity entidad = (Entity) attr.eContainer();
		if (entidad.getName().equals("Solicitud")){
//			if (attr.getType().getSimple() != null){
//				warning("La entidad Solicitud no debe tener atributos simples.", LedPackage.ATTRIBUTE__TYPE);
//			}
		}
	}
	
	@Check
	public void checkFuncionColumna(Columna columna) {
		if (columna.getFuncion() == null){
			return;
		}
		Pattern funcionSinEntidadPattern = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher matcher = funcionSinEntidadPattern.matcher(columna.getFuncion());
		while (matcher.find()){
			String campo = matcher.group(1).trim();
//			if (create(campo, columna) == null){
//				error("El campo \"" + campo + "\" no es valido.", columna.eClass().getEStructuralFeature("funcion").getFeatureID());
//			}
		}
	}
	
	public Campo create(String campoStr, EObject obj){
		String[] splitted = campoStr.split("\\.");
		if (splitted.length == 0){
			return null;
		}
		if (splitted.length == 1){
			if (splitted[0].trim().equals("")){
				return null;
			}
		}
		String entidad = splitted[0];
		String campo2 = entidad;
		List<String> atributos = new ArrayList<String>();
		for (int i = 1; i < splitted.length; i++){
			if (splitted[i].equals("")){
				return null;
			}
			atributos.add(splitted[i]);
			campo2 += "." + splitted[i];
		}
		if (!campo2.equals(campoStr)){
			return null;
		}
		Entity entity = buscarEntidad(entidad, obj);
		if (entity == null){
			return null;
		}
		LedFactory factory = new LedFactoryImpl();
		Campo campoResult = factory.createCampo();
		campoResult.setEntidad(entity);
		if (atributos.size() == 0){
			return campoResult;
		}
		CampoAtributos attrsResult = factory.createCampoAtributos();
		campoResult.setAtributos(attrsResult);
		for (int i = 0; i < atributos.size(); i++){
			String atributo = atributos.get(i);
			if (entity == null){
				return null;
			}
			Attribute attr = getAttribute(entity, atributo);
			if (attr == null){
				return null;
			}
			if (attr.getType().getCompound() == null){
				entity = null;
			}
			else{
				entity = attr.getType().getCompound().getEntidad();
			}
			attrsResult.setAtributo(attr);
			if (i < atributos.size() - 1){
				attrsResult.setAtributos(factory.createCampoAtributos());
				attrsResult = attrsResult.getAtributos();
			}
		}
		return campoResult;
	}
	
	public static Attribute getAttribute(Entity entidad, String atributo){
		for (Attribute attr: getAttributes(entidad)){
			if (attr.getName().equals(atributo)){
				return attr;
			}
		}
		return null;
	}
	
	public static List<Attribute> getAttributes(Entity entidad){
		List<Attribute> attrs = new ArrayList<Attribute>(); 
		while (entidad != null){
			for (Attribute attr: entidad.getAttributes()){
				attrs.add(attr);
			}
			entidad = entidad.getExtends();
		}
		return attrs;
	}
	
	private List<Entity> getEntidades(EObject obj){
		ArrayList<Entity> entidades = new ArrayList<Entity>();
		for (Resource r: obj.eResource().getResourceSet().getResources()){
			if (r.getContents().get(0) instanceof Model){
				Model model = (Model) r.getContents().get(0);
				for (Entity entidad: model.getEntidades()){
					entidades.add(entidad);
				}
			}
		}
		return entidades;
	}
	
	private Entity buscarEntidad(String entidad, EObject obj){
		List<Entity> entidades = getEntidades(obj);
		Entity resultado = null;
		for (Entity e: entidades){
			if (e.getName().equals(entidad)){
				resultado = e;
				break;
			}
		}
		return resultado;
	}
	
}
