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
import es.fap.simpleled.led.Form
import es.fap.simpleled.led.Menu
import es.fap.simpleled.led.Model;
import es.fap.simpleled.led.Pagina
import es.fap.simpleled.led.Popup
import es.fap.simpleled.led.impl.EntityImpl
import es.fap.simpleled.led.impl.MenuImpl

public class LedUtils {

	public static Map<EClass, List<EObject>> allNodes;
	
	public static Resource resource;

	public static void setFapResources(){
		allNodes = new HashMap<EClass, List<EObject>>();
		for (Resource r: resource.getResourceSet().getResources()){
			EList<EObject> contents = r.getContents();
			if (!contents.isEmpty() && contents.get(0) instanceof Model){
				for (EObject o: r.getAllContents()){
					if (allNodes.get(o.eClass()) == null)
						allNodes.put(o.eClass(), new ArrayList<EObject>());
					allNodes.get(o.eClass()).add(o);
				}
			}
		}
	}

	/*
	 * Obtiene todos los nodos de un mismo tipo (por ejemplo de la EClass LedPackage.Literals.PAGINA).
	 */
	public static List<EObject> getNodes(EClass clazz){
		List<EObject> nodes = allNodes.get(clazz);
		if (nodes == null) nodes = new ArrayList<EObject>();  
		return nodes;
	}
	
	public static boolean inPath(EObject obj){
		if (obj.eResource() == null)
			return true;
		File fileResource = new File(obj.eResource().getURI().toFileString());
		File filePath = new File(wfcomponent.Start.path);
		return fileResource.getAbsolutePath().startsWith(filePath.getAbsolutePath());
	}
	
}