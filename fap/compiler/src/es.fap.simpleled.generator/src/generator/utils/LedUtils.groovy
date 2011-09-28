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
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource;

import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.Menu
import es.fap.simpleled.led.Model;
import es.fap.simpleled.led.impl.EntityImpl
import es.fap.simpleled.led.impl.MenuImpl

public class LedUtils {

	private static List<EObject> allNodes = null;
	private static Map<Class, List<EObject>> mapNodes = null;
	
	// True si se está generando el módulo. False si la aplicación.
	public static boolean generatingModule; 
	
	public static void setFapResources(EObject obj){
		if (allNodes != null){
			return;
		}
		allNodes = new ArrayList<EObject>();
		mapNodes = new HashMap<Class, List<EObject>>();
		
		for (Resource r: obj.eResource().getResourceSet().getResources()){
			if (r.getContents().size() != 0 && r.getContents().get(0) instanceof Model){
				for (EObject o: r.getAllContents()){
					allNodes.add(o);
					if (mapNodes.get(o.getClass()) == null){
						List<EObject> list = new ArrayList<EObject>();
						mapNodes.put(o.getClass(), list);
						mapNodes.put(o.getClass().getInterfaces()[0], list);
					}
					mapNodes.get(o.getClass()).add(o);
				}
			}
		}
	}
	
	/* 
	 * Obtiene todos los nodos del arbol sintactico.
	 */
	public static List<EObject> getAllNodes(){
		return allNodes;
	}
	
	/*
	 * Obtiene todos los nodos de un mismo tipo (por ejemplo de la clase PaginaImpl).
	 */
	public static List<EObject> getNodes(Class clazz){
		return mapNodes.get(clazz);
	}
	
	/*
	 * Devuelve el nodo de tipo clazz identificado por name. Si la clase clazz
	 * no tiene un campo name, devuelve null.
	 */
	public static EObject getNode(Class clazz, String name){
		try{
			Method method = clazz.getMethod("getName", null);
		}
		catch(Exception e){
			return null;
		}
		List<EObject> nodes = getNodes(clazz);
		for (EObject node: nodes){
			if (node.name.equals(name)){
				return node;
			}
		}
		return null;
	}
	
	/*
	 * Devuelve la entidad Solicitud, y si no la encuentra (porque se está generando el
	 * módulo en vez de la aplicación), devuelve la entidad SolicitudGenerica. 
	 */
	public static Entity findSolicitud(){
		Entity solicitud = getNode(Entity, "Solicitud");
		if (solicitud == null){
			solicitud = getNode(Entity, "SolicitudGenerica");
		}
		return solicitud;
	}
	
	public static String findComment(EObject o) {
		String ruleName = "ML_COMMENT";
		String startTag = "/\\*\\*?"; // regular expression
		
		String returnValue = "";
		ICompositeNode node = NodeModelUtils.getNode(o);
		if (node != null) {
			// get the last multi line comment before a non hidden leaf node
			for (INode abstractNode : node.getAsTreeIterable()) {
				if (abstractNode instanceof ILeafNode && !((ILeafNode) abstractNode).isHidden())
					break;
				if (abstractNode instanceof ILeafNode && abstractNode.getGrammarElement() instanceof TerminalRule
						&& ruleName.equalsIgnoreCase(((TerminalRule) abstractNode.getGrammarElement()).getName())) {
					String comment = ((ILeafNode) abstractNode).getText();
					if (comment.matches("(?s)" + startTag + ".*")) {
						returnValue = comment;
					}
				}
			}
		}
		return returnValue;
	}

	public static String parse(String comment) {
		String startTag = "/\\*\\*?"; // regular expression
		String endTag = "\\*/"; // regular expression
		String linePrefix = "\\** ?"; // regular expression
		String linePostfix = "\\**"; // regular expression
		String whitespace = "( |\\t)*"; // regular expression
		
		if (comment != null && !comment.equals("")) {
			comment = comment.replaceAll("\\A" + startTag, "");
			comment = comment.replaceAll(endTag + "\\z", "");
			comment = comment.replaceAll("(?m)^"+ whitespace + linePrefix, "");
			comment = comment.replaceAll("(?m)" + whitespace + linePostfix + whitespace + "\$", "");
			return comment.trim();
		} else
			return "";
	}
	
	
}
