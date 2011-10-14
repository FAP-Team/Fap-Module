package es.fap.simpleled.validation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.validation.Check;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.Columna;
import es.fap.simpleled.led.CompoundType;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.impl.EntityImpl;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class LedJavaValidator extends AbstractLedJavaValidator {
	
	public void myError(String message, EStructuralFeature feature){
		error(message, feature);
	}
	
	public void myError(String message, EObject o, EStructuralFeature feature, int index){
		error(message, o, feature, index);
	}
	
	public void myWarning(String message, EStructuralFeature feature){
		warning(message, feature);
	}
	
	public void myWarning(String message, EObject o, EStructuralFeature feature, int index){
		warning(message, o, feature, index);
	}
	
	/**
	 * Comprueba que el atributo length sea sólamente aplicado a campos String o LongText
	 * (en los demás no tiene sentido)
	 * @param attributeAnotations
	 */
	@Check
	public void checkLengthOnlyString(Attribute attr) {
		String tipo = attr.getType().getSimple().getType();
		if ((attr.isHasLength()) && (tipo == null || (!(tipo.equals("String")) && !(tipo.equals("LongTexto"))))){
			error("Anotado con la propiedad \"length\" un atributo que no es de tipo String o LongText", LedPackage.Literals.ATTRIBUTE__HAS_LENGTH);
		}
	}

	/**
	 * Comprueba que las entidades empiecen con mayúscula
	 * @param entity
	 */
	@Check
	public void checkEntitiesStartsWithCapital(Entity entity) {
		if (!Character.isUpperCase(entity.getName().charAt(0))){
			error("Los nombres de las entidades deben empezar con mayuscula", LedPackage.Literals.ENTITY__NAME);
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
			if (father.getName().equals(((EntityImpl)attribute.getType().getCompound().getEntidad()).getName())){
				error("Las entidades no se deben referenciar a si mismas mediante un atributo", LedPackage.Literals.ATTRIBUTE__TYPE);
			}
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
				if (compound.getEntidad() != null){
					warning("Valores por defecto no aplicables sobre el tipo "+attribute.getType().getCompound().getEntidad().getName()+" (solo aplicables a tipos simples)", LedPackage.Literals.ATTRIBUTE__DEFAULT_VALUE);
				}
				else{
					warning("Valores por defecto no aplicables sobre listas (solo aplicables a tipos simples)", LedPackage.Literals.ATTRIBUTE__DEFAULT_VALUE);
				}
			}
		}
	}
	
	@Check
	public void checkSolicitudSimpleAttributos(Attribute attr) {
		Entity entidad = (Entity) attr.eContainer();
		if (entidad.getName().equals("Solicitud")){
			if (LedEntidadUtils.esSimple(attr)){
				warning("La entidad \"Solicitud\" no debe tener atributos simples", LedPackage.Literals.ATTRIBUTE__TYPE);
			}
		}
	}
	
	@Check
	public void checkEntityHasId(Entity entidad) {
		LedEntidadUtils.addId(entidad);
	}
	
	@Check
	public void checkFuncionColumna(Columna columna) {
		FuncionColumnaValidator.checkFuncionColumna(columna, this);
	}
	
	@Check
	public void checkCampo(Campo campo){
		if (! LedCampoUtils.validCampo(campo)){ // El error lo detecta LedScopeProvider en el linking
			return;
		}
		LedElementValidator validator = LedCampoUtils.getElementValidator(campo);
		if (validator != null){
			validator.validateCampoEntidad(campo, this);
			validator.validateCampo(campo, this);
		}
	}
	
}
