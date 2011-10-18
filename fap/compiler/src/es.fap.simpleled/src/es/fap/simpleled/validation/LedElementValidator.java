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
	
	public List<String> completeEntidades(Set<Entity> entidades) {
		List<String> proposals = new ArrayList<String>();
		for (Entity entidad: entidades){
			if (aceptaEntidad(entidad)){
				proposals.add(entidad.getName() + "  -  Entidad");
			}
			proposals.addAll(completeEntidad(entidad.getName(), entidad));
		}
		return proposals;
	}

	public List<String> completeEntidad(String prefijo, Entity entidad){
		HashSet<String> entidadesEnCampo = new HashSet<String>();
		entidadesEnCampo.add(entidad.getName());
		return completeEntidad(prefijo, entidad, entidadesEnCampo);
	}
	
	private List<String> completeEntidad(String prefijo, Entity entidad, Set<String> entidadesEnCampo){
		if (! prefijo.equals("")){
			prefijo += ".";
		}
		List<String> proposals = new ArrayList<String>();
		for (Attribute attr: LedEntidadUtils.getAllDirectAttributes(entidad)){
			if (aceptaAtributo(attr)){
				proposals.add(prefijo + attr.getName() + "  -  " + getType(attr));
			}
			if (LedEntidadUtils.xToOne(attr)){
				entidad = attr.getType().getCompound().getEntidad();
				if (!entidadesEnCampo.contains(entidad.getName())){
					entidadesEnCampo.add(entidad.getName());
					proposals.addAll(completeEntidad(prefijo + attr.getName(), entidad, entidadesEnCampo));
				}
			}
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
			referencia = compound.getTipoReferencia();
		}
		return referencia + "<" + compound.getEntidad().getName() + ">";
	}
	
}
