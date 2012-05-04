package es.fap.simpleled.led.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.CampoAtributos;
import es.fap.simpleled.led.Elemento;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.FirmaSimple;
import es.fap.simpleled.led.LedFactory;
import es.fap.simpleled.led.LedPackage;
import es.fap.simpleled.led.Model;
import es.fap.simpleled.led.Pagina;
import es.fap.simpleled.led.Popup;
import es.fap.simpleled.led.Tabla;
import es.fap.simpleled.led.Formulario;


public class LedCampoUtils {
	
	public static Entity getUltimaEntidad(Campo campo){
		if (campo == null)
			return null;
		Entity result = campo.getEntidad();
		CampoAtributos attrs = campo.getAtributos();
		while (attrs != null){
			Attribute attr = attrs.getAtributo();
			if (LedEntidadUtils.getEntidad(attr) != null)
				result = LedEntidadUtils.getEntidad(attr);
			attrs = attrs.getAtributos();
		}
		return result;
	}
	
	public static Attribute getUltimoAtributo(Campo campo){
		if (campo == null) return null;
		Attribute result = null;
		CampoAtributos attrs = campo.getAtributos();
		while (attrs != null){
			result = attrs.getAtributo();
			attrs = attrs.getAtributos();
		}
		return result;
	}
	
	public static boolean xToMany(Campo campo){
		return LedEntidadUtils.xToMany(getUltimoAtributo(campo));
	}
	
	public static boolean xToOne(Campo campo){
		return LedEntidadUtils.xToOne(getUltimoAtributo(campo));
	}
	
	public static boolean ManyToOne(Campo campo){
		return LedEntidadUtils.isManyToOne(getUltimoAtributo(campo));
	}
	
	public static boolean hasAtributos(Campo campo){
		return campo.getAtributos() != null;
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
	
	@SuppressWarnings("unchecked")
	public static EList<Elemento> getElementos(EObject container){
		try {
			return (EList<Elemento>) container.getClass().getMethod("getElementos").invoke(container);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static boolean equals(Campo campo1, Campo campo2){
		if (campo1 == null || campo2 == null)
			return campo1 == campo2;
		if (!campo1.getEntidad().getName().equals(campo2.getEntidad().getName()))
			return false;
		CampoAtributos atributos1 = campo1.getAtributos();
		CampoAtributos atributos2 = campo2.getAtributos();
		while (atributos1 != null && atributos2 != null){
			if (!atributos1.getAtributo().getName().equals(atributos2.getAtributo().getName()))
				return false;
			atributos1 = atributos1.getAtributos();
			atributos2 = atributos2.getAtributos();
		}
		return atributos1 == atributos2;
	}
	
	public static boolean hayCamposGuardables(EObject container){
		EList<Elemento> elementos = getElementos(container);
		if (elementos != null){
			for (EObject obj: elementos){
				if (hayCamposGuardables(obj))
					return true;
			}
			return false;
		}
		return (!((container instanceof Tabla) || (container instanceof FirmaSimple))) && getCampo(container) != null;
	}
	
	public static boolean hayCamposGuardablesOrTablaOneToMany(EObject container){
		EList<Elemento> elementos = getElementos(container);
		if (elementos != null){
			for (EObject obj: elementos){
				if (hayCamposGuardablesOrTablaOneToMany(obj))
					return true;
			}
			return false;
		}
		if (container instanceof Tabla)
			return LedCampoUtils.getUltimoAtributo(getCampo(container)) != null;
		return getCampo(container) != null;
	}
	
	public static Campo getCampo(EObject object) {
		if (object instanceof Campo) 
			return (Campo) object;
		for (Method method : object.getClass().getMethods()) {
			if (method.getReturnType().equals(Campo.class)) {
				try {
					return (Campo) method.invoke(object);
				} catch (Exception e) {
					return null;
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
	
	public static EObject getCampoScope(EObject obj){
		EObject container = obj.eContainer();
		if (obj instanceof Campo)
			container = container.eContainer();
		while (container != null){
			if (container instanceof Tabla || container instanceof Pagina || container instanceof Popup || container instanceof Model){
				return container;
			}
			container = container.eContainer();
		}
		return null;
	}
	
	public static boolean validCampo(Campo campo){
		if (campo.getMethod() != null)
			return true;
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
	
	/*
	 * Devuelve el campo asociado a una página o a un popup, que será el que tengan definido, o si es null el que
	 * haya definido en el Formulario que lo contiene.
	 */
	public static Campo getCampoPaginaPopup(EObject paginaPopup){
		Campo campo = null;
		if (paginaPopup instanceof Pagina)
			campo = ((Pagina)paginaPopup).getCampo();
		if (paginaPopup instanceof Popup)
			campo = ((Popup)paginaPopup).getCampo();
		if (campo != null)
			return campo;
		return ((Formulario) paginaPopup.eContainer()).getCampo();
	}
	
	public static Map<String, Entity> getEntidadesValidas(EObject elemento){
		EObject container = LedCampoUtils.getCampoScope(elemento);
		Map<String, Entity> entidades = new HashMap<String, Entity>();
		if (container instanceof Model || elemento instanceof Tabla){
			for (Entity e: ModelUtils.<Entity>getVisibleNodes(LedPackage.Literals.ENTITY, elemento.eResource()))
				entidades.put(e.getName(), e);	
			return entidades;
		}
		if (container instanceof Pagina || container instanceof Popup){
			for (Entity entidad: LedEntidadUtils.getEntidadesPaginaPopup(container))
				entidades.put(entidad.getName(), entidad);
		}
		if (container instanceof Tabla){
			Entity ultimaEntidad = LedCampoUtils.getUltimaEntidad(LedCampoUtils.getCampo(container));
			entidades.put(ultimaEntidad.getName(), ultimaEntidad);
		}
		else{
			for (Entity single: LedEntidadUtils.getSingletons(elemento.eResource()))
				entidades.put(single.getName(), single);
		}
		return entidades;
	}

	public static Campo concatena(Campo primero, Campo segundo){
		if (primero == null && segundo == null)
			return null;
		if (primero == null && segundo != null)
			return segundo;
		if (primero != null && segundo == null)
			return primero;
		Entity entidad = segundo.getEntidad();
		Campo result = LedFactory.eINSTANCE.createCampo();
		result.setEntidad(primero.getEntidad());
		CampoAtributos atributos = primero.getAtributos();
		if (LedEntidadUtils.equals(entidad, primero.getEntidad()))
			atributos = null;
		CampoAtributos attrs = null;
		while (atributos != null){
			if (attrs == null){
				result.setAtributos(LedFactory.eINSTANCE.createCampoAtributos());
				attrs = result.getAtributos();
			}
			else{
				attrs.setAtributos(LedFactory.eINSTANCE.createCampoAtributos());
				attrs = attrs.getAtributos();
			}
			attrs.setAtributo(atributos.getAtributo());
			if (LedEntidadUtils.equals(entidad, LedEntidadUtils.getEntidad(atributos.getAtributo())))
				break;
			else
				atributos = atributos.getAtributos();
			if (atributos == null)
				return null; // los campos no se pueden concatenar
		}
		atributos = segundo.getAtributos();
		while (atributos != null){
			if (attrs == null){
				result.setAtributos(LedFactory.eINSTANCE.createCampoAtributos());
				attrs = result.getAtributos();
			}
			else{
				attrs.setAtributos(LedFactory.eINSTANCE.createCampoAtributos());
				attrs = attrs.getAtributos();
			}
			attrs.setAtributo(atributos.getAtributo());
			atributos = atributos.getAtributos();
		}
		return result;
	}
	
	/*
	 * campo = Solicitud.documentos, start = Solicitud ---> true
	 * campo = Solicitud.documentos, start = Documento ---> false
	 */
	public static boolean startsWith(Campo campo, Campo start){
		if (campo == null || start == null)
			return campo == start;
		if (!campo.getEntidad().getName().equals(start.getEntidad().getName()))
			return false;
		CampoAtributos campoAttrs = campo.getAtributos();
		CampoAtributos startAttrs = start.getAtributos();
		while (startAttrs != null){
			if (campoAttrs == null)
				return false;
			if (!campoAttrs.getAtributo().getName().equals(startAttrs.getAtributo().getName()))
				return false;
			campoAttrs = campoAttrs.getAtributos();
			startAttrs = startAttrs.getAtributos();
		}
		return true;
	}
	
	// Convierte un campo en su equivalente en String
	
	public static String getCampoStr(Campo campo){
		if (campo == null){
			return null;
		}
		String campoStr = campo.getEntidad().getName();
		if (campoStr.equals("SolicitudGenerica")){
			campoStr = "Solicitud";
		}
		CampoAtributos attrs = campo.getAtributos();
		while (attrs != null){
			campoStr += "." + attrs.getAtributo().getName();
			attrs = attrs.getAtributos();
		}
		return campoStr;
	}
	
	public static List<Campo> buscarCamposRecursivos (EObject container){
		EList<Elemento> elementos = LedCampoUtils.getElementos(container);
		List<Campo> campos = new ArrayList<Campo>();
		if (elementos != null){
			for (EObject obj: elementos){
				campos.addAll(buscarCamposRecursivos(obj));
			}
		} else {
			campos.add(LedCampoUtils.getCampo(container));
		}
		return campos;
	}
	
}
