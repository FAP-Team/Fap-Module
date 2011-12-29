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
import es.fap.simpleled.led.LedPackage;
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
		Campo campoContainer = LedCampoUtils.getCampo(container);
		if (campoContainer == null || !LedCampoUtils.validCampo(campoContainer)){
			return;
		}
		if (container instanceof Tabla || container instanceof Popup){
			Entity entidadTabla = LedCampoUtils.getUltimaEntidad(campoContainer);
			if (! entidadTabla.getName().equals(campo.getEntidad().getName())){
				validator.myError("En este contexto solo se puede utilizar la entidad \"" + entidadTabla.getName() + "\"", campo, LedPackage.Literals.CAMPO__ENTIDAD, 0);
			}
			return;
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
				entidad = attr.getType().getCompound().getEntidad();
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
	
	public List<Proposal> completeEntidades(String contextPrefix, Set<Entity> entidades) {
		List<Proposal> proposals = new ArrayList<Proposal>();
		for (Entity entidad: entidades){
			if (!entidad.getName().startsWith(contextPrefix)){
				continue;
			}
			if (aceptaEntidad(entidad)){
				proposals.add(new Proposal(entidad.getName() + "  -  Entidad", true, entidad));
			}
			else if (aceptaEntidadRecursivo(entidad)){
				proposals.add(new Proposal(entidad.getName() + "  -  Entidad", false, entidad));
			}
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
			else if (LedEntidadUtils.xToOne(attr) && aceptaEntidadRecursivo(attr.getType().getCompound().getEntidad())){
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
	
}
