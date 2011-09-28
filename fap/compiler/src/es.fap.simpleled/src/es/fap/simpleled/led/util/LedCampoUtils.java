package es.fap.simpleled.led.util;

import java.lang.reflect.Method;

import org.eclipse.emf.ecore.EObject;

import es.fap.simpleled.led.*;
import es.fap.simpleled.validation.*;

public class LedCampoUtils {
	
	public static Entity getUltimaEntidad(Campo campo){
		Entity result = campo.getEntidad();
		CampoAtributos attrs = campo.getAtributos();
		while (attrs != null){
			Attribute attr = attrs.getAtributo();
			if (LedEntidadUtils.getEntidad(attr) != null){
				result = LedEntidadUtils.getEntidad(attr);
			}
			if (! LedEntidadUtils.xToOne(attr)){
				return result;
			}
			attrs = attrs.getAtributos();
		}
		return result;
	}
	
	public static Attribute getUltimoAtributo(Campo campo){
		Attribute result = null;
		CampoAtributos attrs = campo.getAtributos();
		while (attrs != null){
			result = attrs.getAtributo();
			attrs = attrs.getAtributos();
		}
		return result;
	}
	
	public static CampoAtributos getUltimoCampoAtributos(Campo campo){
		if (campo.getAtributos() == null){
			return null;
		}
		CampoAtributos attrs = campo.getAtributos();
		while (attrs.getAtributos() != null){
			attrs = attrs.getAtributos();
		}
		return attrs;
	}
	
	public static Campo getCampo(EObject model) {
		if (model instanceof Campo) {
			return (Campo) model;
		}
		else {
			for (Method method : model.getClass().getMethods()) {
				if (method.getReturnType().equals(Campo.class)) {
					try {
						return (Campo) method.invoke(model, null);
					} catch (Exception e) {
						return null;
					}
				}
			}
		}
		return null;
	}
	
	public static Campo getCampoContainer(CampoAtributos atributos){
		while (atributos.eContainer() instanceof CampoAtributos){
			atributos = (CampoAtributos) atributos.eContainer();
		}
		return (Campo) atributos.eContainer();
	}
	
	public static EObject getElementosContainer(Campo campo){
		EObject container = campo.eContainer().eContainer();
		while (container != null){
			if (container instanceof Tabla || container instanceof Pagina || container instanceof Popup || container instanceof Form){
				return container;
			}
			container = container.eContainer();
		}
		return null;
	}
	
	public static boolean validCampo(Campo campo){
		if (campo.getEntidad() == null || campo.getEntidad().getName() == null){
			return false;
		}
		CampoAtributos atributos = campo.getAtributos();
		while (atributos != null){
			if (atributos.getAtributo() == null || atributos.getAtributo().getName() == null){
				return false;
			}
			atributos = atributos.getAtributos();
		}
		return true;
	}
	
	public static LedElementValidator getElementValidator(Campo campo){
		EObject container = campo.eContainer();
		if (container instanceof Fecha) {
			return new FechaValidator();
		}
		if (container instanceof Columna) {
			return new ColumnaValidator();
		}
		if (container instanceof Tabla || container instanceof Popup) {
			return new TablaValidator();
		}
		if (container instanceof Form) {
			return new FormValidator();
		}
		if (container instanceof Texto || container instanceof AreaTexto) {
			return new TextoValidator();
		}
		if (container instanceof Grupo) {
			return new GrupoValidator();
		}
		if (container instanceof Check) {
			return new CheckValidator();
		}
		if (container instanceof Combo) {
			return new ComboValidator();
		}
		if (container instanceof SubirArchivoAed || container instanceof EditarArchivoAed || container instanceof FirmaPlatinoSimple) {
			return new EntidadValidator("Documento");
		}
		if (container instanceof Direccion) {
			return new EntidadValidator("Direccion");
		}
		if (container instanceof Nip) {
			return new EntidadValidator("Nip");
		}
		if (container instanceof Persona) {
			return new EntidadValidator("Persona");
		}
		if (container instanceof PersonaFisica) {
			return new EntidadValidator("PersonaFisica");
		}
		if (container instanceof PersonaJuridica) {
			return new EntidadValidator("PersonaJuridica");
		}
		if (container instanceof Solicitante) {
			return new EntidadValidator("Solicitante");
		}
		if (container instanceof EntidadAutomatica) {
			return new EntidadAutomaticaValidator();
		}
		if (container instanceof Enlace) {
			return new EnlaceValidator();
		}
		return null;
	}
}
