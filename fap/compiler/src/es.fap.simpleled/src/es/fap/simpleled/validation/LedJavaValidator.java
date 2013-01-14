package es.fap.simpleled.validation;

import java.util.HashSet;
import java.util.List;
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
	
	@Inject
	private LedPackage ledPackage;
	
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
			error("Anotado con la propiedad \"length\" un atributo que no es de tipo String o LongText", ledPackage.getAttribute_HasLength());
		}
	}

	/**
	 * Comprueba que las entidades empiecen con mayúscula
	 * @param entity
	 */
	@Check
	public void checkEntitiesStartsWithCapital(Entity entity) {
		if (!Character.isUpperCase(entity.getName().charAt(0))){
			error("Los nombres de las entidades deben empezar con mayuscula", ledPackage.getEntity_Name());
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
				error("Las entidades no se deben referenciar a si mismas mediante un atributo", ledPackage.getAttribute_Type());
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
					warning("Valores por defecto no aplicables sobre el tipo "+LedEntidadUtils.getEntidad(attribute).getName()+" (solo aplicables a tipos simples)", ledPackage.getAttribute_DefaultValue());
				}
				else if ((compound.getLista() == null) || (compound.isMultiple())) {
					warning("Valores por defecto no aplicables sobre listas de tipo multiple", ledPackage.getAttribute_DefaultValue());
				}
			}
		}
	}
	
	/**
	 * Comprueba que los valores por defecto no se establezcan sobre transient
	 * @param attribute
	 */
	@Check
	public void checkDefaultValueNoTransient (Attribute attribute) {
		if (attribute.getDefaultValue() != null)
			if (attribute.isIsTransient())
				error("Valores por defecto no aplicables sobre atributos transient", ledPackage.getAttribute_IsTransient());
	}
	
	@Check
	public void checkSolicitudSimpleAttributos(Attribute attr) {
		if (attr.eContainer() instanceof Entity){
			Entity entidad = (Entity) attr.eContainer();
			if (entidad.getName().equals("Solicitud")){
				if (LedEntidadUtils.esSimple(attr)){
					warning("La entidad \"Solicitud\" no debe tener atributos simples", ledPackage.getAttribute_Type());
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
		if (! LedCampoUtils.validCampo(campo))
			return; // El error lo detecta LedScopeProvider en el linking
		LedElementValidator validator = LedElementValidator.getElementValidator(campo.eContainer());
		if (validator != null){
			validator.validateCampoEntidad(campo, this);
			validator.validateCampo(campo, this);
		}
	}
	
	@Check
	public void checkNameVariableInPermiso(PermisoVar permisoVar) {
		if (Pattern.compile("^[A-Z]").matcher(permisoVar.getName()).find()) {
			error("El nombre de la variable en permiso debe comenzar por minúscula", ledPackage.getPermisoVar_Name());
		}
	}
	
	@Check
	public void checkNameVariableAgenteInPermiso(PermisoVar permisoVar) {
		if ((permisoVar.getTipo().getName().equals("Agente")) && (permisoVar.getName().equals("agente"))) {
			error("El nombre de una variable Agente no puede llamarse agente. Existe ya una variable agente global.", ledPackage.getPermisoVar_Name());
		}
	}
	
	@Check
	public void checkExtends(Entity entidad){
		Set<String> intermedias = new HashSet<String>();
		Entity father = entidad.getExtends();
		while (father != null){
			if (father.getName().equals(entidad.getName())){
				error(entidad.getName() + " no puede extender de " + entidad.getExtends().getName() + " porque se produce un lazo infinito", ledPackage.getEntity_Extends());
				return;
			}
			if (intermedias.contains(father.getName())){
				return;
			}
			intermedias.add(father.getName());
			father = father.getExtends();
		}
	}
	
	public void checkTablaCampoPopup(Tabla tabla, Popup popup, Campo concatenado, EReference ref){
		if (popup == null) return;
		Campo campo = LedCampoUtils.getCampoPaginaPopup(popup);
		if (!LedCampoUtils.equals(tabla.getCampo(), campo) && !LedCampoUtils.equals(concatenado, campo))
			error( "El popup referenciado no es válido para el campo especificado en la tabla", ref);
	}
	
	public void checkTablaCampoPagina(Tabla tabla, Pagina pagina, Campo concatenado, EReference ref){
		if (pagina == null) return;
		Campo campo = LedCampoUtils.getCampoPaginaPopup(pagina);
		if (!LedCampoUtils.equals(tabla.getCampo(), campo) && !LedCampoUtils.equals(concatenado, campo))
			error("La página referenciada no es válida para el campo especificado en la tabla", ref);
	}
	
	@Check
	public void checkTablaCampo(Tabla tabla){
		EObject container = LedCampoUtils.getCampoScope(tabla);
		Campo concatenado = LedCampoUtils.concatena(LedCampoUtils.getCampoPaginaPopup(container), tabla.getCampo());
		
		checkTablaCampoPopup(tabla, tabla.getPopup(), concatenado, ledPackage.getTabla_Popup());
		checkTablaCampoPopup(tabla, tabla.getPopupBorrar(), concatenado, ledPackage.getTabla_PopupBorrar());
		checkTablaCampoPopup(tabla, tabla.getPopupCrear(), concatenado, ledPackage.getTabla_PopupCrear());
		checkTablaCampoPopup(tabla, tabla.getPopupEditar(), concatenado, ledPackage.getTabla_PopupEditar());
		checkTablaCampoPopup(tabla, tabla.getPopupLeer(), concatenado, ledPackage.getTabla_PopupLeer());
		
		checkTablaCampoPagina(tabla, tabla.getPagina(), concatenado, ledPackage.getTabla_Pagina());
		checkTablaCampoPagina(tabla, tabla.getPaginaBorrar(), concatenado, ledPackage.getTabla_PaginaBorrar());
		checkTablaCampoPagina(tabla, tabla.getPaginaCrear(), concatenado, ledPackage.getTabla_PaginaCrear());
		checkTablaCampoPagina(tabla, tabla.getPaginaEditar(), concatenado, ledPackage.getTabla_PaginaEditar());
		checkTablaCampoPagina(tabla, tabla.getPaginaLeer(), concatenado, ledPackage.getTabla_PaginaLeer());
	}
	
	@Check
	public void checkNombreEntidadUnico(Entity entidad){
		for (Entity e : ModelUtils.<Entity>getVisibleNodes(ledPackage.getEntity(), entidad.eResource())) {
			String uri1 = entidad.eResource().getURI().toString();
			String uri2 = e.eResource().getURI().toString();
			if (entidad.getName().equals(e.getName()) && !uri1.equals(uri2))
				error("La entidad " + entidad.getName() + " ya existe", ledPackage.getEntity_Name());
		}
	}
	
	/*
	 * Se se llama a cada método que desea comparar dos páginas, para checkear condiciones.
	 */
	@Check
	public void checkPaginasStuff(Pagina pagina){
		for (Pagina p : ModelUtils.<Pagina>getVisibleNodes(ledPackage.getPagina(), pagina.eResource())) {
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
	
	/*
	 * Se se llama a cada método que desea comparar dos popups, para checkear condiciones.
	 */
	@Check
	public void checkPopupsStuff(Popup popup){
		for (Popup p : ModelUtils.<Popup>getVisibleNodes(ledPackage.getPopup(), popup.eResource())) {
			String qn1 = qnProvider.getFullyQualifiedName(popup).toString();
			String qn2 = qnProvider.getFullyQualifiedName(p).toString();
			String uri1 = popup.eResource().getURI().toString();
			String uri2 = p.eResource().getURI().toString();
			if (!qn1.equals(qn2) || !uri1.equals(uri2))
				checkNombrePopupUnico(popup, p);
		}
	}
	
	@Check
	public void checkFormularioInicialUnico(Formulario formulario){
		for (Formulario f : ModelUtils.<Formulario>getVisibleNodes(ledPackage.getFormulario(), formulario.eResource())) {
			String qn1 = qnProvider.getFullyQualifiedName(formulario).toString();
			String qn2 = qnProvider.getFullyQualifiedName(f).toString();
			String uri1 = formulario.eResource().getURI().toString();
			String uri2 = f.eResource().getURI().toString();
			if (!qn1.equals(qn2) || !uri1.equals(uri2)){
				if (formulario.isInicial() && f.isInicial()){
					error("Ya existe otro formulario definido como inicial", ledPackage.getFormulario_Inicial());
				}
			}
		}
	}
	
	public void checkNombrePaginaUnico(Pagina pagina, Pagina other){
		Formulario formulario = (Formulario)other.eContainer();
		if (pagina.getName().equals(other.getName()))
			error("La página " + pagina.getName() + " ya existe en el formulario " + formulario.getName(), ledPackage.getPagina_Name());
	}
	
	public void checkNombrePopupUnico(Popup popup, Popup other){
		Formulario formulario = (Formulario)other.eContainer();
		if (popup.getName().equals(other.getName()))
			error("El popup " + popup.getName() + " ya existe en el formulario " + formulario.getName(), ledPackage.getPopup_Name());
	}
	
	public void checkPaginaInicialUnica(Pagina pagina, Pagina other){
		Formulario formulario = (Formulario)pagina.eContainer();
		Formulario otherForm = (Formulario)other.eContainer();
		if (pagina.isInicial() && other.isInicial() && formulario.getName().equals(otherForm.getName())){
			error("Ya existe en el formulario otra página definida como inicial", ledPackage.getPagina_Inicial());
		}
	}
	
	@Check
	public void checkPermisoAction(PermisoRuleCheck rule){
		if (!rule.getLeft().isAction())
			return;
		if (rule.getRight() != null && rule.getRight().getAction() == null)
			error("Tienes que especificar una de las siguientes acciones: leer, editar, crear o borrar", ledPackage.getPermisoRuleCheck_Right());
		for (PermisoRuleCheckRight right: rule.getRightGroup()){
			if (right.getAction() == null)
				error("Tienes que especificar una de las siguientes acciones: leer, editar, crear o borrar", ledPackage.getPermisoRuleCheck_Left());
		}
	}
	
	@Check
	public void checkPermisoReturn(PermisoReturn permisoReturn){
		if (permisoReturn.getPares().size() == 0) return;
		Set<String> allAcciones = new HashSet<String>();
		for (AccionesGrafico acciones: permisoReturn.getPares()){
			for (String accion: acciones.getAcciones().getAcciones()){
				if (allAcciones.contains(accion))
					error("No se pueden repetir acciones", acciones, ledPackage.getAccionesGrafico_Acciones(), 0);
				else
					allAcciones.add(accion);
			}
		}
		
	}
	
	@Check
	public void checkAcciones(Acciones acciones){
		if (acciones.isMultiple() && acciones.getAcciones().size() == 0)
			error("La lista de acciones no puede ser vacía", ledPackage.getAcciones_Acciones());
	}
	
	@Check
	public void checkSubirArchivoMimeTypes(SubirArchivo subirArchivo){
		Pattern pattern = Pattern.compile("[\\w-]+/(\\*|[\\w-]+)");
		for (int i = 0; i < subirArchivo.getMimes().size(); i++){
			if (!pattern.matcher(subirArchivo.getMimes().get(i)).matches())
				error("El tipo mime especificado no es válido. Tiene que ser tipo/subtipo o tipo/*. Por ejemplo: application/pdf", ledPackage.getSubirArchivo_Mimes(), i);
		}
	}
	
	@Check
	public void checkFirmaSimple(FirmaSimple firma){
		if ("firma".equals(firma.getName()))
			error("FirmaSimple no puede llamarse \"firma\"", ledPackage.getFirmaSimple_Name());
	}
	
	@Check
	public void checkGuardarParaPreparar(Pagina pagina){
		if (pagina.isGuardarParaPreparar()){
			Campo campo = LedCampoUtils.getCampoPaginaPopup(pagina);
			String entidad = "";
			if (campo != null)
				entidad = LedCampoUtils.getUltimaEntidad(campo).getName();
			if (!entidad.equals("Solicitud") && !entidad.equals("SolicitudGenerica"))
				error("El atributo guardarParaPreparar solo puede aplicarse a paginas de Solicitud", ledPackage.getPagina_GuardarParaPreparar());
		
		}
	}
	
	@Check
	public void checkCampoUsadoEnPagina(Pagina pagina){
		checkCampoUsado(pagina);
	}
	
	@Check
	public void checkCampoUsadoEnPopups(Popup popup){
		checkCampoUsado(popup);
	}
	
	public void checkCampoUsado(EObject obj){
		List<Campo> campos = LedCampoUtils.buscarCamposRecursivos (obj);
		Set<String> unicos = new HashSet<String>();
		String campoStr = "";
		for (Campo campo: campos){
			campoStr = LedCampoUtils.getCampoStr(campo);
			if (!unicos.contains(campoStr))
				unicos.add(campoStr);
			else{
				if (campo != null)
					warning("El campo esta siendo utilizado por otro elemento en la misma pagina", campo, ledPackage.getCampo_Atributos(), 0);
			}
		}
	}
	
	@Check
	public void checkCascadeType(CascadeListSimpleType tipos){
		if (tipos.getList().size() == 0) return;
		Set<String> allTipos = new HashSet<String>();
		for (String tipo: tipos.getList()){
			if (allTipos.contains(tipo))
				error("No se pueden repetir las mismas opciones de cascada", tipos.eContainer().eContainer(), ledPackage.getAttribute_Cascade(), 0);
			else
				allTipos.add(tipo);
		}
	}
	
	/**
	 * Comprueba que los atributos no empiecen con mayúscula
	 * @param entity
	 */
	@Check
	public void checkAttributeNoStartsWithCapital(Attribute attr) {
		if ((attr.getName() != null) && (Character.isUpperCase(attr.getName().charAt(0))))
			error("Los atributos de las entidades deben empezar con minuscula", ledPackage.getAttribute_Name());
	}
	
	/**
	 * Comprueba que el valor minimo de un elemento barra deslizamiento no sea mayor que el maximo
	 * @param entity
	 */
	@Check
	public void checkMinimoNoMayorMaximo(BarraDeslizante barraDeslizante) {
		if (barraDeslizante.isMin() && barraDeslizante.isMax() && (barraDeslizante.getMinimo() > barraDeslizante.getMaximo()))
			error("El valor minimo no puede ser mayor que el maximo", ledPackage.getBarraDeslizante_Minimo());
		else if (!barraDeslizante.isMin() && barraDeslizante.isMax() && (0 > barraDeslizante.getMaximo()))
			error("El valor maximo debe ser superior a 0, o especificar un valor minimo menor que el maximo", ledPackage.getBarraDeslizante_Maximo());
		else if (barraDeslizante.isMin() && !barraDeslizante.isMax() && (barraDeslizante.getMinimo() < 100))
			error("El valor minimo debe ser inferior a 100, o especificar un valor maximo mayor que el minimo", ledPackage.getBarraDeslizante_Minimo());
	}
	
	/**
	 * Comprueba que el valor por defecto este comprendido entre el minimo y el maximo
	 * @param entity
	 */
	@Check
	public void checkValorDefectoBetweenMinimoMaximo(BarraDeslizante barraDeslizante) {
		if (barraDeslizante.isDefecto() && barraDeslizante.isMin() && barraDeslizante.isMax() && ((barraDeslizante.getValorDefecto() > barraDeslizante.getMaximo()) || (barraDeslizante.getValorDefecto() < barraDeslizante.getMinimo())))
			error("El valor por defecto tiene que estar comprendido entre el minimo y el maximo", ledPackage.getBarraDeslizante_ValorDefecto());
		else if (barraDeslizante.isDefecto() && barraDeslizante.isMin() && !barraDeslizante.isMax() && ((barraDeslizante.getValorDefecto() > 100) || (barraDeslizante.getValorDefecto() < barraDeslizante.getMinimo())))
			error("El valor por defecto tiene que estar comprendido entre el minimo y 100", ledPackage.getBarraDeslizante_ValorDefecto());
		else if (barraDeslizante.isDefecto() && !barraDeslizante.isMin() && barraDeslizante.isMax() && ((barraDeslizante.getValorDefecto() > barraDeslizante.getMaximo()) || (barraDeslizante.getValorDefecto() < 0)))
			error("El valor por defecto tiene que estar comprendido entre el 0 y el maximo", ledPackage.getBarraDeslizante_ValorDefecto());
		else if (barraDeslizante.isDefecto() && !barraDeslizante.isMin() && !barraDeslizante.isMax() && ((barraDeslizante.getValorDefecto() > 100) || (barraDeslizante.getValorDefecto() < 0)))
			error("El valor por defecto tiene que estar comprendido entre el 0 y el 100", ledPackage.getBarraDeslizante_ValorDefecto());
	}
	
	
	/*
	 * Se llama para comprobar que si el popup tiene una tabla:
	 * 		- No se recomienda emplear un popup en una tabla que está en un popup
	 * 		- Se recuerda que al introducir una nueva entrada en la tabla, se perderán los valores de 
	 * 		  los demás elementos del popup (Si la tabla tiene una página).
	 */
	@Check
	public void checkPopupsWarning(Popup popup){
		List<Elemento> elementos = popup.getElementos();
		if (!elementos.isEmpty()){
			for (Elemento elemento : elementos) {
				if (elemento instanceof Tabla){  //El popup contiene una tabla
					Tabla tbl = (Tabla)elemento;
					//Si la tabla contiene un popup
					if((tbl.getPopup() != null) || (tbl.getPopupBorrar() != null) || (tbl.getPopupCrear() != null) || (tbl.getPopupEditar() != null) || (tbl.getPopupLeer() != null))
						warning("No se recomienda emplear un Popup dentro de otro Popup", null);
					//Si la tabla contiene una página, recordar que se perderá el valor de los otros campos
					if((elementos.size() > 1) && ((tbl.getPagina() != null) || (tbl.getPaginaBorrar() != null) || (tbl.getPaginaCrear() != null) || (tbl.getPaginaEditar() != null) || (tbl.getPaginaLeer() != null))){
						warning("Al introducir una nueva entrada en la tabla, se perderán los valores de los demás elementos del Popup", null);
					}
				}
			}
		}
	}
	
	@Check
	public void checkColorTablas(Tabla tabla){
		if ((tabla.getColor().getTexto() != null && ((tabla.getColor().getCodePrint() == null) && (tabla.getColor().getDefault() == null) && (tabla.getColor().getTextoB() == null)&& (tabla.getColor().getTextoE() == null) && (tabla.getColor().getTextoL() == null))))
			error("El campo Color no ha sido rellenado correctamente", null);
	}
	
	@Check
	public void checkPersonaFisicaNoUtilizarSetearTipoPadre(PersonaFisica personaFisica){
		if (personaFisica.isSetearTipoPadre()){
			error("setearTipoPadre no se debe utilizar nunca.", ledPackage.getPersonaFisica_SetearTipoPadre());
		}
	}
	
	@Check
	public void checkNombreSercivioWebUnico(ServicioWeb servicioWeb){
		for (ServicioWeb e : ModelUtils.<ServicioWeb>getVisibleNodes(ledPackage.getServicioWeb(), servicioWeb.eResource())) {
			String uri1 = servicioWeb.eResource().getURI().toString();
			String uri2 = e.eResource().getURI().toString();
			if (servicioWeb.getName().equals(e.getName()) && !uri1.equals(uri2))
				error("El servicio web " + servicioWeb.getName() + " ya existe.", ledPackage.getServicioWeb_Name());
		}
	}
	
	/*
	 * Se comprueba que el primer campo del Return sea correcto.
	 */
	
	@Check
	public void checkCampoRet(ServicioWeb servicioWeb) {
		String entidad = LedCampoUtils.getUltimaEntidad(servicioWeb.getCampo()).getName();
		
		if (servicioWeb.getRet().getCampoRet().getEntidad().getName() != entidad) {
			error("La entidad \"" + servicioWeb.getRet().getCampoRet().getEntidad().getName() + "\" no es válida.", ledPackage.getServicioWeb_Ret());
		}
	}
	
	/*
	 * Se comprueba que los campos del Return sean los correctos.
	 */
	@Check
	public void checkCampoRetMore(ServicioWeb servicioWeb) {
		String entidad = LedCampoUtils.getUltimaEntidad(servicioWeb.getCampo()).getName();
	
		for (int i = 0; i < servicioWeb.getRetMore().size(); i++) {
			String ret = servicioWeb.getRetMore().get(i).getCampoRet().getEntidad().getName();
			if (ret != entidad) {
				error("La entidad \"" + ret + "\" no es válida.", ledPackage.getServicioWeb_RetMore());
			}
		}
	}

	/*
	 * En caso de que haya un Form, dar error cuando:
	 * 		- El elemento padre es una Pagina y no tiene el atributo noForm.
	 * 		- Si el elemento padre no es una Pagina, buscar hacia arriba.
	 * 			Si es una página comprobar que tiene el atributo noForm.
	 * 		- El elemento padre es un grupo con una condición (mostrarSiCheck,
	 * 			mostrarSiCombo).
	 */
	@Check
	public void checkFormEnPaginaNoForm(Form form) {
		EObject eo = (EObject)form.eContainer();
		if (eo instanceof Pagina) {
			if (!((Pagina) eo).isNoForm()) {
				//error("La página que contiene el Form debe tener el atributo 'noForm'.", ledPackage.getForm_Name());
				warning("La página que contiene el Form debe tener el atributo 'noForm'.", ledPackage.getForm_Name());
			}
		} else {
			EObject pagina = ModelUtils.getContenedorPadre(eo, ledPackage.getPagina());
			if (pagina != eo) {
				if (!((Pagina) pagina).isNoForm()) {
					//error("La página que contiene el Form debe tener el atributo 'noForm'.", ledPackage.getForm_Name());
					warning("La página que contiene el Form debe tener el atributo 'noForm'.", ledPackage.getForm_Name());
				}
			}
		}
		
		if (eo instanceof Grupo) {
			if ((((Grupo) eo).getSiCheck() != null) || (((Grupo) eo).getSiCombo() != null) || (((Grupo) eo).getSiRadioBooleano() != null)) {
				error("El grupo tiene que ir dentro del Form para que la condición tenga efecto.", ledPackage.getForm_Name());
			}
		}
	}
	
	/*
	 * En el caso de que un grupo tenga una condición, sea por 
	 * Check o por Combo o por RadioBooleano, ese elemento tiene que estar definido
	 * dentro del Form más cercano.
	 */
	@Check
	public void checkFormContieneCondicion(Grupo grupo) {
		EObject eo = (EObject)grupo.eContainer();
		boolean encontrado = false;
		if (grupo.getSiCheck() != null) {
			if (eo instanceof Form) {
				List<Elemento> elementos = ((Form) eo).getElementos();
				if (!elementos.isEmpty()) {
					for (Elemento elemento : elementos) {
						if (elemento instanceof es.fap.simpleled.led.Check) {
							if (((es.fap.simpleled.led.Check) elemento).getName().toString().equals(grupo.getSiCheck().getName())) {
								encontrado = true;
							}
						}
					}
					if (!encontrado) {
						error("El Check al que se hace referencia debe estar definido dentro del Form más cercano.", ledPackage.getGrupo_SiCheck());
						encontrado = false;
					}
				}
			} else {
				EObject form = ModelUtils.getContenedorPadre(eo, ledPackage.getForm());
				if (form != eo) {
					List<Elemento> elementos = ((Form) form).getElementos();
					if (!elementos.isEmpty()) {
						for (Elemento elemento : elementos) {
							if (elemento instanceof es.fap.simpleled.led.Check) {
								if (((es.fap.simpleled.led.Check) elemento).getName().toString().equals(grupo.getSiCheck().getName())) {
									encontrado = true;
								}
							}
						}
						if (!encontrado) {
							error("El Check al que se hace referencia debe estar definido dentro del Form más cercano.", ledPackage.getGrupo_SiCheck());
							encontrado = false;
						}
					}
				}
			}
		} else if (grupo.getSiCombo() != null) {
			if (eo instanceof Form) {
				List<Elemento> elementos = ((Form) eo).getElementos();
				if (!elementos.isEmpty()) {
					for (Elemento elemento : elementos) {
						if (elemento instanceof Combo) {
							if (((Combo) elemento).getName().toString().equals(grupo.getSiCombo().getName())) {
								encontrado = true;
							}
						}
					}
					if (!encontrado) {
						error("El Combo al que se hace referencia debe estar definido dentro del Form más cercano.", ledPackage.getGrupo_SiCombo());
						encontrado = false;
					}
				}
			} else {
				EObject form = ModelUtils.getContenedorPadre(eo, ledPackage.getForm());
				if (form != eo) {
					List<Elemento> elementos = ((Form) form).getElementos();
					if (!elementos.isEmpty()) {
						for (Elemento elemento : elementos) {
							if (elemento instanceof Combo) {
								if (((Combo) elemento).getName().toString().equals(grupo.getSiCombo().getName())) {
									encontrado = true;
								}
							}
						}
						if (!encontrado) {
							error("El Combo al que se hace referencia debe estar definido dentro del Form más cercano.", ledPackage.getGrupo_SiCombo());
							encontrado = false;
						}
					}
				}
			}
		} else if (grupo.getSiRadioBooleano() != null) {
				if (eo instanceof Form) {
					List<Elemento> elementos = ((Form) eo).getElementos();
					if (!elementos.isEmpty()) {
						for (Elemento elemento : elementos) {
							if (elemento instanceof RadioBooleano) {
								if (((RadioBooleano) elemento).getName().toString().equals(grupo.getSiRadioBooleano().getName())) {
									encontrado = true;
								}
							}
						}
						if (!encontrado) {
							error("El RadioBoolean al que se hace referencia debe estar definido dentro del Form más cercano.", ledPackage.getGrupo_SiRadioBooleano());
							encontrado = false;
						}
					}
				} else {
					EObject form = ModelUtils.getContenedorPadre(eo, ledPackage.getForm());
					if (form != eo) {
						List<Elemento> elementos = ((Form) form).getElementos();
						if (!elementos.isEmpty()) {
							for (Elemento elemento : elementos) {
								if (elemento instanceof RadioBooleano) {
									if (((RadioBooleano) elemento).getName().toString().equals(grupo.getSiRadioBooleano().getName())) {
										encontrado = true;
									}
								}
							}
							if (!encontrado) {
								error("El RadioBooleano al que se hace referencia debe estar definido dentro del Form más cercano.", ledPackage.getGrupo_SiRadioBooleano());
								encontrado = false;
							}
						}
					}
				}
		}
	}


}


