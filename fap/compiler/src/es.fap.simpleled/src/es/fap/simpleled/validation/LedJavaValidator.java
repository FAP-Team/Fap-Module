package es.fap.simpleled.validation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;
import org.eclipse.xtext.validation.Check;

import com.google.inject.Inject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.Columna;
import es.fap.simpleled.led.CompoundType;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.Formulario;
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.Pagina;
import es.fap.simpleled.led.Tabla;
import es.fap.simpleled.led.impl.EntityImpl;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.led.util.ModelUtils;

public class LedJavaValidator extends AbstractLedJavaValidator {
	
	@Inject
	private IQualifiedNameProvider qnProvider;
	
	@Inject
	private ResourceDescriptionsProvider indexProvider;
	
	@Inject
	private IContainer.Manager manager;
	
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
		if (EntityImpl.class.isInstance(LedEntidadUtils.getEntidad(attribute))) {
			if (father.getName().equals(((EntityImpl)LedEntidadUtils.getEntidad(attribute)).getName())){
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
					warning("Valores por defecto no aplicables sobre el tipo "+LedEntidadUtils.getEntidad(attribute).getName()+" (solo aplicables a tipos simples)", LedPackage.Literals.ATTRIBUTE__DEFAULT_VALUE);
				}
				else if ((compound.getLista() == null) || (compound.isMultiple())) {
					warning("Valores por defecto no aplicables sobre listas de tipo multiple", LedPackage.Literals.ATTRIBUTE__DEFAULT_VALUE);
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
		LedElementValidator validator = LedElementValidator.getElementValidator(campo);
		if (validator != null){
			validator.validateCampoEntidad(campo, this);
			validator.validateCampo(campo, this);
		}
	}
	
	@Check
	public void checkPaginaEntidad(Pagina pagina){
		Formulario formulario = (Formulario) pagina.eContainer();
		if (pagina.getCampo() == null && formulario.getCampo() == null && LedCampoUtils.hayCamposGuardablesOrTablaOneToMany(pagina)){
			error("Tiene que definir el campo que va a usar en esta página, o en el formulario entero", LedPackage.Literals.PAGINA__NAME);
		}
	}
	
	@Check
	public void checkTablaCampo(Tabla tabla){
		Entity entidad = LedCampoUtils.getUltimaEntidad(tabla.getCampo());
		String error = "El popup referenciado no es válido para el campo especificado en la tabla";
		if (tabla.getPopup() != null && !LedEntidadUtils.equals(entidad, LedCampoUtils.getUltimaEntidad(tabla.getPopup().getCampo())))
			error(error, LedPackage.Literals.TABLA__POPUP);
		if (tabla.getPopupBorrar() != null && !LedEntidadUtils.equals(entidad, LedCampoUtils.getUltimaEntidad(tabla.getPopupBorrar().getCampo())))
			error(error, LedPackage.Literals.TABLA__POPUP_BORRAR);
		if (tabla.getPopupCrear() != null && !LedEntidadUtils.equals(entidad, LedCampoUtils.getUltimaEntidad(tabla.getPopupCrear().getCampo())))
			error(error, LedPackage.Literals.TABLA__POPUP_CREAR);
		if (tabla.getPopupModificar() != null && !LedEntidadUtils.equals(entidad, LedCampoUtils.getUltimaEntidad(tabla.getPopupModificar().getCampo())))
			error(error, LedPackage.Literals.TABLA__POPUP_MODIFICAR);
		if (tabla.getPopupVer() != null && !LedEntidadUtils.equals(entidad, LedCampoUtils.getUltimaEntidad(tabla.getPopupVer().getCampo())))
			error(error, LedPackage.Literals.TABLA__POPUP_VER);
		
		error = "La página referenciada no es válida para el campo especificado en la tabla";
		if (tabla.getPagina() != null){
			Entity entidadPagina = LedEntidadUtils.getEntidad(tabla.getPagina());
			if (!LedEntidadUtils.equals(entidad, entidadPagina))
				error(error, LedPackage.Literals.TABLA__PAGINA);
		}
		if (tabla.getPaginaBorrar() != null){
			Entity entidadPagina = LedEntidadUtils.getEntidad(tabla.getPaginaBorrar());
			if (!LedEntidadUtils.equals(entidad, entidadPagina))
				error(error, LedPackage.Literals.TABLA__PAGINA_BORRAR);
		}
		if (tabla.getPaginaCrear() != null){
			Entity entidadPagina = LedEntidadUtils.getEntidad(tabla.getPaginaCrear());
			if (!LedEntidadUtils.equals(entidad, entidadPagina))
				error(error, LedPackage.Literals.TABLA__PAGINA_CREAR);
		}
		if (tabla.getPaginaModificar() != null){
			Entity entidadPagina = LedEntidadUtils.getEntidad(tabla.getPaginaModificar());
			if (!LedEntidadUtils.equals(entidad, entidadPagina))
				error(error, LedPackage.Literals.TABLA__PAGINA_MODIFICAR);
		}
		if (tabla.getPaginaVer() != null){
			Entity entidadPagina = LedEntidadUtils.getEntidad(tabla.getPaginaVer());
			if (!LedEntidadUtils.equals(entidad, entidadPagina))
				error(error, LedPackage.Literals.TABLA__PAGINA_VER);
		}
	}
	
	@Check
	public void checkNombreEntidadUnico(Entity entidad){
		for (Entity e : ModelUtils.<Entity>getVisibleNodes(LedPackage.Literals.ENTITY, indexProvider, manager, entidad.eResource())) {
			String uri1 = entidad.eResource().getURI().toString();
			String uri2 = e.eResource().getURI().toString();
			if (entidad.getName().equals(e.getName()) && !uri1.equals(uri2))
				error("La entidad " + entidad.getName() + " ya existe", LedPackage.Literals.ENTITY__NAME);
		}
	}
	
	/*
	 * Se se llama a cada método que desea comparar dos páginas, para checkear condiciones.
	 */
	@Check
	public void checkPaginasFormularioStuff(Pagina pagina){
		for (Pagina p : ModelUtils.<Pagina>getVisibleNodes(LedPackage.Literals.PAGINA, indexProvider, manager, pagina.eResource())) {
			String qn1 = qnProvider.getFullyQualifiedName(pagina).toString();
			String qn2 = qnProvider.getFullyQualifiedName(p).toString();
			String uri1 = pagina.eResource().getURI().toString();
			String uri2 = p.eResource().getURI().toString();
			if (!qn1.equals(qn2) || !uri1.equals(uri2)){
				checkNombrePaginaUnico(pagina, p);
				checkPaginaInicialUnica(pagina, p);
			}
		}
	}
	
	@Check
	public void checkFormularioInicialUnico(Formulario formulario){
		for (Formulario f : ModelUtils.<Formulario>getVisibleNodes(LedPackage.Literals.FORMULARIO, indexProvider, manager, formulario.eResource())) {
			String qn1 = qnProvider.getFullyQualifiedName(formulario).toString();
			String qn2 = qnProvider.getFullyQualifiedName(f).toString();
			String uri1 = formulario.eResource().getURI().toString();
			String uri2 = f.eResource().getURI().toString();
			if (!qn1.equals(qn2) || !uri1.equals(uri2)){
				if (formulario.isInicial() && f.isInicial()){
					error("Ya existe otro formulario definido como inicial", LedPackage.Literals.FORMULARIO__INICIAL);
				}
			}
		}
	}
	
	public void checkNombrePaginaUnico(Pagina pagina, Pagina other){
		Formulario formulario = (Formulario)other.eContainer();
		if (pagina.getName().equals(other.getName()))
			error("La página " + pagina.getName() + " ya existe en el formulario " + formulario.getName(), LedPackage.Literals.PAGINA__NAME);
	}
	
	public void checkPaginaInicialUnica(Pagina pagina, Pagina other){
		Formulario formulario = (Formulario)pagina.eContainer();
		Formulario otherForm = (Formulario)other.eContainer();
		if (pagina.isInicial() && other.isInicial() && formulario.getName().equals(otherForm.getName())){
			error("Ya existe en el formulario otra página definida como inicial", LedPackage.Literals.PAGINA__INICIAL);
		}
	}
	
}
