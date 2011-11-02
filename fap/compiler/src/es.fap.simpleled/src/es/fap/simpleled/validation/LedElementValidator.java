package es.fap.simpleled.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.CompoundType;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.Form;
import es.fap.simpleled.led.Formulario;
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.Pagina;
import es.fap.simpleled.led.Popup;
import es.fap.simpleled.led.Tabla;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;
import es.fap.simpleled.led.util.Proposal;

public abstract class LedElementValidator {
	
	
	public abstract boolean aceptaEntidad(Entity entidad);
	
	public abstract boolean aceptaAtributo(Attribute atributo);
	
	public abstract String mensajeError();

	
	public void validateCampoEntidad(Campo campo, LedJavaValidator validator){
		EObject container = LedCampoUtils.getElementosContainer(campo);
		if (LedEntidadUtils.esSingleton(campo.getEntidad()) && !(container instanceof Tabla)){
			return;
		}
		Campo campoContainer = LedCampoUtils.getCampo(container);
		Entity entidad = null;
		if (container instanceof Form && campoContainer == null){
			while (!(container instanceof Pagina)){
				container = container.eContainer();
			}
		}
		if (campoContainer == null || !LedCampoUtils.validCampo(campoContainer)){
			if (!(container instanceof Pagina)){
				return;
			}
			entidad = LedEntidadUtils.getEntidad((Pagina)container);
			if (entidad == null){
				return;
			}
		}
		if (container instanceof Tabla || container instanceof Popup || container instanceof Form){
			entidad = LedCampoUtils.getUltimaEntidad(campoContainer);
		}
		if (! entidad.getName().equals(campo.getEntidad().getName())){
			validator.myError("En este contexto solo se puede utilizar la entidad \"" + entidad.getName() + "\"", campo, LedPackage.Literals.CAMPO__ENTIDAD, 0);
		}
	}
	
	public void validateCampo(Campo campo, LedJavaValidator validator) {
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
			if (LedEntidadUtils.xToOne(attr)){
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
	
	public List<Proposal> completeEntidades(Set<Entity> entidades) {
		List<Proposal> proposals = new ArrayList<Proposal>();
		List<Proposal> childProposals = null;
		for (Entity entidad: entidades){
			String tipo = "Entidad";
			int priority = 1;
			if (LedEntidadUtils.esSingleton(entidad)){
				tipo = "Singleton";
				priority = 0;
			}
			if (aceptaEntidad(entidad)){
				proposals.add(new Proposal(entidad.getName() + "  -  " + tipo, true, priority));
			}
			else if (aceptaEntidadRecursivo(entidad)){
				proposals.add(new Proposal(entidad.getName() + "  -  " + tipo, false, priority));
				if (proposals.size() == 1){
					childProposals = completeEntidad(entidad.getName(), entidad);
				}
			}
		}
		if (proposals.size() == 1 && childProposals != null && childProposals.size() > 0){
			return childProposals;
		}
		return proposals;
	}
	
	public List<Proposal> completeEntidad(String prefijo, Entity entidad){
		if (! prefijo.equals("")){
			prefijo += ".";
		}
		List<Proposal> proposals = new ArrayList<Proposal>();
		List<Proposal> childProposals = null;
		for (Attribute attr: LedEntidadUtils.getAllDirectAttributes(entidad)){
			if (aceptaAtributo(attr)){
				proposals.add(new Proposal(prefijo + attr.getName() + "  -  " + getType(attr), true));
			}
			else if (LedEntidadUtils.xToOne(attr) && aceptaEntidadRecursivo(LedEntidadUtils.getEntidad(attr))){
				proposals.add(new Proposal(prefijo + attr.getName() + "  -  " + getType(attr), false));
				if (proposals.size() == 1){
					childProposals = completeEntidad(prefijo + attr.getName(), LedEntidadUtils.getEntidad(attr));
				}
			}
		}
		if (proposals.size() == 1 && childProposals != null && childProposals.size() > 0){
			return childProposals;
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
		return referencia + "<" + compound.getEntidad().getName() + ">";
	}
	
}
