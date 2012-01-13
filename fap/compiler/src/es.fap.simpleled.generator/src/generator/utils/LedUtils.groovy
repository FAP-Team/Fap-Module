package generator.utils;

import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import java.lang.reflect.Method
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource;

import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.Menu
import es.fap.simpleled.led.Model;
import es.fap.simpleled.led.impl.EntityImpl
import es.fap.simpleled.led.impl.MenuImpl

public class LedUtils {

	public static Map<EClass, List<EObject>> mapNodes;
	
	public static Resource resource;

	public static void setFapResources(EObject obj){
		if (mapNodes != null)
			return;
		mapNodes = new HashMap<EClass, List<EObject>>();
		resource = obj.eResource();
		for (Resource r: resource.getResourceSet().getResources()){
			EList<EObject> contents = r.getContents();
			if (!contents.isEmpty() && contents.get(0) instanceof Model){
				for (EObject o: r.getAllContents()){
					if (mapNodes.get(o.eClass()) == null)
						mapNodes.put(o.eClass(), new ArrayList<EObject>());
					mapNodes.get(o.eClass()).add(o);
				}
			}
		}
	}

	/*
	 * Obtiene todos los nodos de un mismo tipo (por ejemplo de la EClass LedPackage.Literals.PAGINA).
	 */
	public static List<EObject> getNodes(EClass clazz){
		return mapNodes.get(clazz);
	}

}