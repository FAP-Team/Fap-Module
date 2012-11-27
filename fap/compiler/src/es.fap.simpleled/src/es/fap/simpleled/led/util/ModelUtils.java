package es.fap.simpleled.led.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.containers.DescriptionAddingContainer;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;
import com.google.inject.Inject;

import es.fap.simpleled.led.Campo;
import es.fap.simpleled.led.Elemento;

public class ModelUtils{

	@Inject
	public static IContainer.Manager manager;
	
	@Inject
	public static ResourceDescriptionsProvider indexProvider;
	
	@SuppressWarnings("unchecked")
	public static <T extends EObject> List<T> getVisibleNodes(EClass clazz, String name, Resource res) {
		List<T> nodes = new ArrayList<T>();
		IResourceDescriptions index = indexProvider.getResourceDescriptions(res);
		IResourceDescription descr = index.getResourceDescription(res.getURI());
		for (IContainer visibleContainer: manager.getVisibleContainers(descr, index)) { 
			if (visibleContainer instanceof DescriptionAddingContainer) // tiene descripciones repetidas
				continue;
			for (IEObjectDescription desc: visibleContainer.getExportedObjectsByType(clazz)) {
				T obj = (T) desc.getEObjectOrProxy();
				if (obj.eIsProxy())
					obj = (T) EcoreUtil.resolve(obj, res);
				if (name == null)
					nodes.add(obj);
				else{
					try {
						Method method = obj.getClass().getMethod("getName");
						if (method != null && name.equals(method.invoke(obj)))
							nodes.add(obj);
					} catch (Exception e) {}
				}
			}
		}
		return nodes;
	}
	
	public static <T extends EObject> List<T> getVisibleNodes(EClass clazz, Resource res) {
		return getVisibleNodes(clazz, null, res);
	}
	
	public static <T extends EObject> T getVisibleNode(EClass clazz, String name, Resource res) {
		List<T> nodes = getVisibleNodes(clazz, name, res);
		if (nodes.size() > 0) return nodes.get(0);
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends EObject> T getVisibleNode(EClass clazz, Resource res) {
		return (T)getVisibleNode(clazz, null, res);
	}
	
	/*
	 * Se busca un elemento padre concreto.
	 */
	public static EObject getContenedorPadre(EObject eo, EClass clazz) {
		while (!(clazz.isInstance(eo))) {
			EObject eo1 = (EObject)eo.eContainer();
			eo = eo1;
			if (eo == null)
				break;
		}
		return eo;
	}
	
	public static Elemento getElemento(EObject object) {
		if (object instanceof Elemento) 
			return (Elemento) object;
		for (Method method : object.getClass().getMethods()) {
			if (method.getReturnType().equals(Campo.class)) {
				try {
					return (Elemento) method.invoke(object);
				} catch (Exception e) {
					return null;
				}
			}
		}
		return null;
	}
	
	public static List<Elemento> buscarElementosRecursivos(EObject container){
		EList<Elemento> elementos = LedCampoUtils.getElementos(container);
		List<Elemento> listaElementos = new ArrayList<Elemento>();
		if (elementos != null){
			for (EObject obj: elementos){
				listaElementos.addAll(buscarElementosRecursivos(obj));
			}
		} else {
			listaElementos.add(getElemento(container));
		}
		return listaElementos;
	}
	
}
