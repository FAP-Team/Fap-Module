package es.fap.simpleled.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.validation.Check;

import com.google.inject.Inject;

import es.fap.simpleled.led.*;
import es.fap.simpleled.led.impl.EntityImpl;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.led.util.ModelUtils;

public class LedJavaValidator extends AbstractLedJavaValidator {
	
	@Inject
	private IQualifiedNameProvider qnProvider;
	
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
		if (attr.eContainer() instanceof Entity){
			Entity entidad = (Entity) attr.eContainer();
			if (entidad.getName().equals("Solicitud")){
				if (LedEntidadUtils.esSimple(attr)){
					warning("La entidad \"Solicitud\" no debe tener atributos simples", LedPackage.Literals.ATTRIBUTE__TYPE);
				}
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
	public void checkNameVariableInPermiso(PermisoVar permisoVar) {
		if (Pattern.compile("^[A-Z]").matcher(permisoVar.getName()).find()) {
			error("El nombre de la variable en permiso debe comenzar por minúscula", LedPackage.Literals.PERMISO_VAR__NAME);
		}
	}
	
	@Check
	public void checkExtends(Entity entidad){
		Set<String> intermedias = new HashSet<String>();
		Entity father = entidad.getExtends();
		while (father != null){
			if (father.getName().equals(entidad.getName())){
				error(entidad.getName() + " no puede extender de " + entidad.getExtends().getName() + " porque se produce un lazo infinito", LedPackage.Literals.ENTITY__EXTENDS);
				return;
			}
			if (intermedias.contains(father.getName())){
				return;
			}
			intermedias.add(father.getName());
			father = father.getExtends();
		}
	}
	
	public void checkPaginaEntidad(Pagina pagina){
		Formulario formulario = (Formulario) pagina.eContainer();
		if (pagina.getCampo() == null && formulario.getCampo() == null && LedCampoUtils.hayCamposGuardablesOrTablaOneToMany(pagina)){
			error("Tiene que definir el campo que va a usar en esta página, o en el formulario entero", LedPackage.Literals.PAGINA__NAME);
		}
	}
	
	public void checkTablaCampoPopup(Tabla tabla, Popup popup, Campo concatenado, EReference ref){
		if (popup != null && !LedCampoUtils.equals(tabla.getCampo(), popup.getCampo()) && !LedCampoUtils.equals(concatenado, popup.getCampo()))
			error( "El popup referenciado no es válido para el campo especificado en la tabla", ref);
	}
	
	public void checkTablaCampoPagina(Tabla tabla, Pagina pagina, Campo concatenado, EReference ref){
		if (pagina != null && !LedCampoUtils.equals(tabla.getCampo(), LedCampoUtils.getCampoPagina(pagina)) && !LedCampoUtils.equals(concatenado, LedCampoUtils.getCampoPagina(pagina)))
			error("La página referenciada no es válida para el campo especificado en la tabla", ref);
	}
	
	@Check
	public void checkTablaCampo(Tabla tabla){
		EObject container = LedCampoUtils.getElementosContainer(tabla);
		Campo campoContainer;
		if (container instanceof Pagina)
			campoContainer = LedCampoUtils.getCampoPagina((Pagina) container);
		else
			campoContainer = LedCampoUtils.getCampo(container);
		Campo concatenado = LedCampoUtils.concatena(campoContainer, tabla.getCampo());
		
		checkTablaCampoPopup(tabla, tabla.getPopup(), concatenado, LedPackage.Literals.TABLA__POPUP);
		checkTablaCampoPopup(tabla, tabla.getPopupBorrar(), concatenado, LedPackage.Literals.TABLA__POPUP_BORRAR);
		checkTablaCampoPopup(tabla, tabla.getPopupCrear(), concatenado, LedPackage.Literals.TABLA__POPUP_CREAR);
		checkTablaCampoPopup(tabla, tabla.getPopupEditar(), concatenado, LedPackage.Literals.TABLA__POPUP_EDITAR);
		checkTablaCampoPopup(tabla, tabla.getPopupLeer(), concatenado, LedPackage.Literals.TABLA__POPUP_LEER);
		
		checkTablaCampoPagina(tabla, tabla.getPagina(), concatenado, LedPackage.Literals.TABLA__PAGINA);
		checkTablaCampoPagina(tabla, tabla.getPaginaBorrar(), concatenado, LedPackage.Literals.TABLA__PAGINA_BORRAR);
		checkTablaCampoPagina(tabla, tabla.getPaginaCrear(), concatenado, LedPackage.Literals.TABLA__PAGINA_CREAR);
		checkTablaCampoPagina(tabla, tabla.getPaginaEditar(), concatenado, LedPackage.Literals.TABLA__PAGINA_EDITAR);
		checkTablaCampoPagina(tabla, tabla.getPaginaLeer(), concatenado, LedPackage.Literals.TABLA__PAGINA_LEER);
	}
	
	@Check
	public void checkNombreEntidadUnico(Entity entidad){
		for (Entity e : ModelUtils.<Entity>getVisibleNodes(LedPackage.Literals.ENTITY, entidad.eResource())) {
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
		for (Pagina p : ModelUtils.<Pagina>getVisibleNodes(LedPackage.Literals.PAGINA, pagina.eResource())) {
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
		for (Formulario f : ModelUtils.<Formulario>getVisibleNodes(LedPackage.Literals.FORMULARIO, formulario.eResource())) {
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
	
	@Check
	public void checkPermisoAction(PermisoRuleCheck rule){
		if (!rule.getLeft().isAction())
			return;
		if (rule.getRight() != null && rule.getRight().getAction() == null)
			error("Tienes que especificar una de las siguientes acciones: leer, editar, crear o borrar", LedPackage.Literals.PERMISO_RULE_CHECK__RIGHT);
		for (PermisoRuleCheckRight right: rule.getRightGroup()){
			if (right.getAction() == null)
				error("Tienes que especificar una de las siguientes acciones: leer, editar, crear o borrar", LedPackage.Literals.PERMISO_RULE_CHECK__LEFT);
		}
	}
	
	@Check
	public void checkPermisoGraficoNombreUnico(PermisoGrafico permiso){
		for (PermisoAcceso p : ModelUtils.<PermisoAcceso>getVisibleNodes(LedPackage.Literals.PERMISO_ACCESO, permiso.eResource())) {
			if (permiso.getName().equals(p.getName()))
				error("Existe un permiso de acceso con el mismo nombre", LedPackage.Literals.PERMISO_GRAFICO__NAME);
		}
	}
	
	@Check
	public void checkPermisoAccesoNombreUnico(PermisoAcceso permiso){
		for (PermisoGrafico p : ModelUtils.<PermisoGrafico>getVisibleNodes(LedPackage.Literals.PERMISO_GRAFICO, permiso.eResource())) {
			if (permiso.getName().equals(p.getName()))
				error("Existe un permiso gráfico con el mismo nombre", LedPackage.Literals.PERMISO_ACCESO__NAME);
		}
	}
	
	@Check
	public void checkSubirArchivoMimeTypes(SubirArchivo subirArchivo){
		Pattern pattern = Pattern.compile("[\\w-]+/(\\*|[\\w-]+)");
		for (int i = 0; i < subirArchivo.getMimes().size(); i++){
			if (!pattern.matcher(subirArchivo.getMimes().get(i)).matches())
				error("El tipo mime especificado no es válido. Tiene que ser tipo/subtipo o tipo/*. Por ejemplo: application/pdf", LedPackage.Literals.SUBIR_ARCHIVO__MIMES, i);
		}
	}
	
}
