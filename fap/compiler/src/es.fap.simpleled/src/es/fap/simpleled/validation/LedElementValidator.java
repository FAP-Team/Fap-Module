package es.fap.simpleled.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import com.google.inject.Inject;

import es.fap.simpleled.led.AreaTexto;
import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.BarraDeslizante;
import es.fap.simpleled.led.CCC;
import es.fap.simpleled.led.CalcularFirmantes;
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.Check;
import es.fap.simpleled.led.Columna;
import es.fap.simpleled.led.Combo;
import es.fap.simpleled.led.CompoundType;
import es.fap.simpleled.led.Direccion;
import es.fap.simpleled.led.DireccionMapa;
import es.fap.simpleled.led.EditarArchivo;
import es.fap.simpleled.led.Enlace;
import es.fap.simpleled.led.EntidadAutomatica;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.Fecha;
import es.fap.simpleled.led.FirmaDocumento;
import es.fap.simpleled.led.FirmaFirmantes;
import es.fap.simpleled.led.FirmaSetCampo;
import es.fap.simpleled.led.FirmaSetTrue;
import es.fap.simpleled.led.FirmaSimple;
import es.fap.simpleled.led.Formulario;
import es.fap.simpleled.led.Grupo;
import es.fap.simpleled.led.LedFactory;
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.Nip;
import es.fap.simpleled.led.Pagina;
import es.fap.simpleled.led.Persona;
import es.fap.simpleled.led.PersonaFisica;
import es.fap.simpleled.led.PersonaJuridica;
import es.fap.simpleled.led.Popup;
import es.fap.simpleled.led.RadioBooleano;
import es.fap.simpleled.led.ServicioWeb;
import es.fap.simpleled.led.Solicitante;
import es.fap.simpleled.led.SubirArchivo;
import es.fap.simpleled.led.Tabla;
import es.fap.simpleled.led.Texto;
import es.fap.simpleled.led.WSReturn;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.led.util.Proposal;

public abstract class LedElementValidator {
	
	public EObject element;
	public Entity raiz;
	
	@Inject
	private LedPackage ledPackage;
	
	public abstract boolean aceptaEntidad(Entity entidad);
	
	public abstract boolean aceptaAtributo(Attribute atributo);
	
	public abstract String mensajeError();

	public boolean aceptaString(){
		return true;
	}
	
	public LedElementValidator(EObject element){
		this.element = element;
	}
	
	public void validateCampoEntidad(Campo campo, LedJavaValidator validator){
		if (campo.getMethod() == null && !LedCampoUtils.getEntidadesValidas(campo.eContainer()).containsKey(campo.getEntidad().getName()))
			validator.myError("En este contexto no se puede utilizar esta entidad", campo, ledPackage.getCampo_Entidad(), 0);
	}
	
	public void validateCampo(Campo campo, LedJavaValidator validator) {
		if (campo.getMethod() != null){
			if (!aceptaString())
				validator.myError("En este contexto no se puede utilizar un String para el campo", campo, null, 0);
			return;
		}
		Attribute attr = LedCampoUtils.getUltimoAtributo(campo);
		if (attr != null){
			if (!aceptaAtributo(attr)){
				validator.myError(mensajeError(), campo, null, 0);
			}
		}
		else{
			if (!aceptaEntidad(campo.getEntidad())){
				validator.myError(mensajeError(), campo, null, 0);
			}
		}
	}
	
	private boolean aceptaEntidadRecursivo(Entity entidad){
		return aceptaEntidadRecursivo(entidad, new HashSet<String>());
	}
	
	private boolean aceptaEntidadRecursivo(Entity entidad, Set<String> entidadesEnCampo){
		for (Attribute attr: LedEntidadUtils.getAllDirectAttributes(entidad)){
			if (aceptaAtributo(attr)){
				return true;
			}
			if (LedEntidadUtils.xToOne(attr) || ((this instanceof PaginaValidator) && LedEntidadUtils.isReferencia(attr))){
				entidad = LedEntidadUtils.getEntidad(attr);
				if (!entidadesEnCampo.contains(entidad.getName())){
					entidadesEnCampo.add(entidad.getName());
					if (aceptaEntidadRecursivo(entidad, entidadesEnCampo)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public List<Proposal> completeEntidades(String contextPrefix, Collection<Entity> entidades, EObject elemento) {
		List<Proposal> proposals = new ArrayList<Proposal>();
		EObject container = LedCampoUtils.getCampoScope(elemento);
		List<Entity> prios = null;
		System.out.println(container);
		if (container instanceof Pagina || container instanceof Popup)
			prios = LedEntidadUtils.getEntidadesPaginaPopup(container);
		for (Entity entidad: entidades){
			raiz = entidad;
			if (!entidad.getName().startsWith(contextPrefix))
				continue;
			String tipo = "Entidad";
			if (LedEntidadUtils.esSingleton(entidad))
				tipo = "Singleton";
			int prio = 0;
			if (prios != null) prio = prios.indexOf(entidad) + 2;
			if (aceptaEntidad(entidad))
				proposals.add(new Proposal(entidad.getName() + "  -  " + tipo, true, entidad, prio));
			else if (aceptaEntidadRecursivo(entidad))
				proposals.add(new Proposal(entidad.getName() + "  -  " + tipo, false, entidad, prio));
		}
		if (proposals.size() == 1){
			Proposal p = proposals.get(0);
			if (!p.valid)
				proposals = completeEntidad("", (Entity) p.atributo, p.getEditorText());
		}
		return proposals;
	}
	
	public List<Proposal> completeEntidad(String contextPrefix, Entity entidad, String prefijo){
		List<Proposal> proposals = new ArrayList<Proposal>();
		for (Attribute attr: LedEntidadUtils.getAllDirectAttributes(entidad)){
			if (!attr.getName().startsWith(contextPrefix)){
				continue;
			}
			if (aceptaAtributo(attr)){
				proposals.add(new Proposal(prefijo + attr.getName() + "  -  " + getType(attr), true, attr));
			}
			else if ((LedEntidadUtils.xToOne(attr) || ((this instanceof PaginaValidator) && LedEntidadUtils.isReferencia(attr))) && aceptaEntidadRecursivo(LedEntidadUtils.getEntidad(attr))){
				proposals.add(new Proposal(prefijo + attr.getName() + "  -  " + getType(attr), false, attr));
			}
		}
		if (proposals.size() == 1){
			Proposal p = proposals.get(0);
			if (!p.valid)
				proposals = completeEntidad("", ((Attribute)p.atributo).getType().getCompound().getEntidad(), p.getEditorText());
		}
		return proposals;
	}
	
	public String getType(Attribute attr){
		CompoundType compound = attr.getType().getCompound();
		if (LedEntidadUtils.esSimple(attr)){
			return LedEntidadUtils.getSimpleTipo(attr);
		}
		if (LedEntidadUtils.esLista(attr)){
			String multiple = "";
			if (compound.isMultiple()){
				multiple = " (multiple)";
			}
			return "Lista " + compound.getLista().getName() + multiple;
		}
		if (LedEntidadUtils.esColeccion(attr)){
			return compound.getCollectionType() + " <" + compound.getCollectionReferencia().getType() + ">";
		}
		String referencia = "OneToOne";
		if (compound.getTipoReferencia() != null){
			referencia = compound.getTipoReferencia().getType();
		}
		if (compound.getEntidad().isEmbedded()){
			referencia = "Embedded";
		}
		return referencia + "<" + compound.getEntidad().getName() + ">";
	}
	
	public static LedElementValidator getElementValidator(EObject container){
		if (container instanceof Fecha) {
			return new FechaValidator(container);
		}
		if (container instanceof Columna) {
			return new ColumnaValidator(container);
		}
		if (container instanceof Tabla) {
			return new TablaValidator(container);
		}
		if (container instanceof WSReturn) {
			return new WSReturnValidator(container);
		}
		if (container instanceof ServicioWeb) {
			return new ServicioWebValidator(container);
		}
		if (container instanceof Pagina || container instanceof Formulario || container instanceof Popup) {
			return new PaginaValidator(container);
		}
		if (container instanceof Texto || container instanceof AreaTexto) {
			return new TextoValidator(container);
		}
		if (container instanceof Grupo) {
			return new GrupoValidator(container);
		}
		if (container instanceof Check) {
			return new CheckValidator(container);
		}
		if (container instanceof RadioBooleano) {
			return new RadioBooleanoValidator(container);
		}
		if (container instanceof BarraDeslizante) {
			return new BarraDeslizanteValidator(container);
		}
		if (container instanceof Combo) {
			return new ComboValidator(container);
		}
		if (container instanceof SubirArchivo || container instanceof EditarArchivo) {
			return new SimpleEntidadValidator(container, "Documento", false);
		}
		if (container instanceof Direccion) {
			return new SimpleEntidadValidator(container, "Direccion", false);
		}
		if (container instanceof DireccionMapa) {
			return new SimpleEntidadValidator(container, "DireccionMapa", false);
		}
		if (container instanceof CCC) {
			return new SimpleEntidadValidator(container, "CCC", false);
		}
		if (container instanceof Nip) {
			return new SimpleEntidadValidator(container, "Nip", false);
		}
		if (container instanceof Persona) {
			return new SimpleEntidadValidator(container, "Persona", false);
		}
		if (container instanceof PersonaFisica) {
			return new SimpleEntidadValidator(container, "PersonaFisica", false);
		}
		if (container instanceof PersonaJuridica) {
			return new SimpleEntidadValidator(container, "PersonaJuridica", false);
		}
		if (container instanceof Solicitante) {
			return new SimpleEntidadValidator(container, "Solicitante", false);
		}
		if (container instanceof EntidadAutomatica) {
			return new EntidadAutomaticaValidator(container);
		}
		if (container instanceof Enlace) {
			return new EnlaceValidator(container);
		}
		if (container instanceof FirmaDocumento) {
			return new SimpleEntidadValidator(container, "Documento", false);
		}
		if (container instanceof FirmaFirmantes) {
			return new ListaEntidadValidator(container, "Firmante");
		}
		if (container instanceof CalcularFirmantes) {
			return new SimpleEntidadValidator(container, "Solicitante", true);
		}
		if (container instanceof FirmaSetCampo || container instanceof FirmaSimple) {
			return new TextoValidator(container);
		}
		if (container instanceof FirmaSetTrue) {
			return new CheckValidator(container);
		}
		return null;
	}
	
}
