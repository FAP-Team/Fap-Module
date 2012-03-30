package es.fap.simpleled.ui.documentation;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;
import org.eclipse.xtext.impl.KeywordImpl;
import org.eclipse.xtext.impl.RuleCallImpl;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.impl.LeafNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider;
import org.eclipse.xtext.ui.editor.hover.html.XtextBrowserInformationControlInput;

import com.google.inject.Inject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.FirmaDocumento;
import es.fap.simpleled.led.FirmaFirmantes;
import es.fap.simpleled.led.FirmaSetTrue;
import es.fap.simpleled.led.util.DocElemento;
import es.fap.simpleled.led.util.DocParametro;
import es.fap.simpleled.led.util.LedDocumentationUtils;
import es.fap.simpleled.ui.coloring.FapSemanticHighlighting;

public class FapDocumentationProvider extends DefaultEObjectHoverProvider implements IEObjectDocumentationProvider {

	public static INode node;
	public static boolean reference;
	
	@Inject
	private ILabelProvider labelProvider;
	
	private IInformationControlCreator hoverControlCreator;
	
	/*
	 * Reglas de la gramática que están documentadas. Sintaxis: 
	 * Si la regla y la palabra reservada son distintas ---> nombre_regla:nombre_palabra_reservada
	 * Si son iguales ---> nombre_regla
	 */
	public static String[] docRulesArray = {
		"Entity:Entidad", "Attribute", "Formulario", "Menu", "MenuGrupo:Grupo", 
		"MenuEnlace:Enlace", "Accion", "Pagina", "Popup", "Grupo", "AgruparCampos",
		"Texto", "AreaTexto", "Check", "Enlace", "Wiki", "Boton", "Fecha", "Combo",
		"Form", "Tabla", "Columna", "SubirArchivo",
		"FirmaSimple", "Direccion", "Nip", "PersonaFisica",
		"PersonaJuridica", "Persona", "Solicitante", "EntidadAutomatica", "Lista",
		"FirmaDocumento", "FirmaFirmantes", "FirmaSetCampo", "FirmaSetTrue"
	};
	
	public static Map<String, String> docRules = arrayToSet(docRulesArray);
	
	public static Map<String, String> arrayToSet(String[] array){
		Map<String, String> map = new HashMap<String, String>();
		for (String str: array){
			if (str.contains(":")){
				map.put(str.split(":")[0], str.split(":")[1]);
			}
			else{
				map.put(str, str);
			}
		}
		return map;
	}
	
	public static EObject getDocRule(EObject semantic){
		while (semantic != null && !docRules.containsKey(JsonDocumentation.getRuleName(semantic))){
			semantic = semantic.eContainer();
		}
		return semantic;
	}
	
	public String getDocumentation(EObject o) {
		INode node_ = node;
		node = null;
		if (o.eIsProxy()){
			return null;
		}
		Assignment assign = getDocFeature(node_);
		if (assign != null && (assign.getTerminal() instanceof RuleCall) && "Campo".equals(((RuleCall)assign.getTerminal()).getRule().getName())){
			if (o instanceof Entity){
				Entity entidad = (Entity) o;
				return getDocumentation(entidad, entidad.getName());
			}
			if (o instanceof Attribute){
				Attribute atributo = (Attribute) o;
				return getDocumentation((Entity) atributo.eContainer(), atributo.getName());
			}
		}
		EObject semantic = null;
		if (node_ != null){
			semantic = getDocRule(NodeModelUtils.findActualSemanticObjectFor(node_));
		}
		else{
			semantic = o;
		}
		if (semantic == null){
			return "";
		}
		if (semantic instanceof FirmaDocumento || semantic instanceof FirmaFirmantes || semantic instanceof FirmaSetTrue)
			semantic = semantic.eContainer();
		if (o instanceof Keyword){
			Keyword keyword = (Keyword) o;
			char first = keyword.getValue().charAt(0);
			if (!Character.isLetter(first)){
				return "";
			}
			if (first == Character.toUpperCase(first)){
				DocElemento elemento = JsonDocumentation.getElemento(keyword, semantic);
				if (elemento != null){
					return LedDocumentationUtils.getDocumentation(elemento);
				}
			}
			else{
				DocParametro parametro = JsonDocumentation.getParametro(keyword, semantic);
				if (parametro != null){
					return LedDocumentationUtils.getDocumentation(parametro);
				}
			}
		}
		if (assign != null){
			DocParametro parametro = JsonDocumentation.getParametro(assign.getFeature(), semantic);
			if (parametro != null){
				return LedDocumentationUtils.getDocumentation(parametro);
			}
		}
		return "";
	}
	
	public String getDocumentation(Entity entidad, String name) {
		String result = "</br>";
		Attribute last = null;
		boolean atributoNuevo = false;
		Iterator<ILeafNode> it = NodeModelUtils.getNode(entidad).getLeafNodes().iterator();
		while (it.hasNext()){
			ILeafNode ileaf = it.next();
			if (!ileaf.getClass().getSimpleName().equals("LeafNode")){
				continue;
			}
			LeafNode leaf = (LeafNode) ileaf;
			String text = leaf.getText();
			EObject semantic = leaf.getSemanticElement();
			EObject grammar = leaf.getGrammarElement();
			Assignment assign = getDocFeature(leaf);
			if (assign != null && "extends".equals(assign.getFeature())){
				result += span(text, FapSemanticHighlighting.referenceColor, false);
				continue;
			}
			if (semantic instanceof Attribute && ((Attribute)semantic).getName().equals(text)){
				if (text.equals(name))
					result += span(text, new RGB(0,0,0), false, true);
				else
					result += span(text, FapSemanticHighlighting.nameColor, false);
				atributoNuevo = false;
				continue;
			}
			if (semantic instanceof Entity && ((Entity)semantic).getName().equals(text)){
				if (text.equals(name))
					result += span(text, new RGB(0,0,0), false, true);
				else
					result += span(text, FapSemanticHighlighting.nameColor, false);
				continue;
			}
			if (grammar instanceof KeywordImpl){
				KeywordImpl keyword = (KeywordImpl) grammar;
				if (keyword.getValue().equals("<")){
					result += span(" &lt;" + it.next().getText() + "&gt; </span> ", FapSemanticHighlighting.referenceColor, false); 
					it.next();
					continue;
				}
				if (keyword.getValue().equals("}")){
					result += "</br>}";
					continue;
				}
			}
			Attribute current = getAttribute(semantic);
			if (last != current){
				result += "</br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
				atributoNuevo = true;
			}
			last = current;
			if (grammar instanceof RuleCallImpl){
				RuleCallImpl rule = (RuleCallImpl) grammar;
				String ruleName = rule.getRule().getName();
				if ("STRING".equals(ruleName)){
					result += span(text, FapSemanticHighlighting.stringColor, false);
					continue;
				}
				result += span(text, FapSemanticHighlighting.referenceColor, false);
				continue;
			}
			if (atributoNuevo){
				result += span(text, FapSemanticHighlighting.referenceColor, false);
				continue;
			}
			if (grammar instanceof KeywordImpl){
				Keyword keyword = (Keyword) grammar;
				if ("Entidad".equals(keyword.getValue())){
					result += span(text, FapSemanticHighlighting.elementColor, true);
					continue;
				}
				if (Character.isLetter(text.charAt(0))){
					result += span(text, FapSemanticHighlighting.keywordColor, true);
					continue;
				}
			}
			result += text + " ";
			semantic.eAdapters();
		}
		return result;
	}

	public static String span(String text, RGB color, boolean bold){
		return span(text, color, bold, false);
	}
	
	public static String span(String text, RGB color, boolean bold, boolean italic){
		String start = "";
		String end = "";
		if (bold){
			start += "<b>";
			end += "</b>";
		}
		if (italic){
			start += "<i>";
			end += "</i>";
		}
		return start + "<span style=\"color:#" + rgb2hex(color) + "\">" + text + "</span>" + end + " ";
	}
	
	public static Attribute getAttribute(EObject semantic){
		while (semantic != null && ! (semantic instanceof Attribute)){
			semantic = semantic.eContainer();
		}
		return (Attribute) semantic;
	}
	
	public static String rgb2hex(RGB rgb){
		String all = "";
		String hex = Integer.toHexString(rgb.red);
		if (hex.length() == 1){
			hex = "0" + hex;
		}
		all += hex;
		hex = Integer.toHexString(rgb.green);
		if (hex.length() == 1){
			hex = "0" + hex;
		}
		all += hex;
		hex = Integer.toHexString(rgb.blue);
		if (hex.length() == 1){
			hex = "0" + hex;
		}
		return all + hex;
	}
	
	public static Assignment getDocFeature(INode node){
		while (node != null){
			EObject grammar = node.getGrammarElement();
			while (grammar != null){
				if (grammar instanceof Assignment){
					Assignment assignment = (Assignment) grammar;
					ParserRule rule = getParserRule(assignment);
					if (docRules.containsKey(rule.getName())){
						return assignment;
					}
				}
				grammar = grammar.eContainer();
			}
			node = node.getParent();
		}
		return null;
	}
	
//	public static String getDocFeature(INode node){
//		while (node != null){
//			EObject grammar = node.getGrammarElement();
//			while (grammar != null){
//				if (grammar instanceof Assignment){
//					Assignment assignment = (Assignment) grammar;
//					ParserRule rule = getParserRule(assignment);
//					if (docRules.containsKey(rule.getName())){
//						return assignment.getFeature();
//					}
//				}
//				grammar = grammar.eContainer();
//			}
//			node = node.getParent();
//		}
//		return null;
//	}
	
	public static String getFeature(INode node){
		while (node != null){
			EObject grammar = node.getGrammarElement();
			while (grammar != null){
				if (grammar instanceof Assignment)
					return ((Assignment) grammar).getFeature();
				grammar = grammar.eContainer();
			}
			node = node.getParent();
		}
		return null;
	}
	
	public static ParserRule getParserRule(Assignment assignment){
		EObject container = assignment;
		while (! (container instanceof ParserRule)){
			container = container.eContainer();
		}
		return (ParserRule) container;
	}
	
	protected String getHoverInfoAsHtml(EObject o) {
		StringBuffer buffer = new StringBuffer();
		String firstLine = getFirstLine(o);
		String documentation = getDocumentation(o);

		if (!firstLine.equals("")){
			buffer.append (firstLine);
			if (documentation != null && documentation.length() > 0) {
				buffer.append ("<p>");
				buffer.append (documentation);
				buffer.append("</p>");
			}
		}
		else{
			if (documentation != null && documentation.length() > 0) {
				buffer.append (documentation);
			}
		}
		String bufferString = buffer.toString();
		if (bufferString.equals("")){
			return null;
		}
		return bufferString;
	}
	
	public IInformationControlCreator getHoverControlCreator() {
		if (hoverControlCreator == null)
			hoverControlCreator = new HoverControlCreator(getInformationPresenterControlCreator());
		return hoverControlCreator;
	}
	
	public final class HoverControlCreator extends AbstractReusableInformationControlCreator {

		private final IInformationControlCreator fInformationPresenterControlCreator;

		public HoverControlCreator(IInformationControlCreator informationPresenterControlCreator) {
			fInformationPresenterControlCreator = informationPresenterControlCreator;
		}

		@Override
		public IInformationControl doCreateInformationControl(Shell parent) {
			String tooltipAffordanceString = EditorsUI.getTooltipAffordanceString();
			if (BrowserInformationControl.isAvailable(parent)) {
				String font = "org.eclipse.jdt.ui.javadocfont"; // FIXME: PreferenceConstants.APPEARANCE_JAVADOC_FONT;
				MyBrowserInformationControl iControl = new MyBrowserInformationControl(parent, font,
						tooltipAffordanceString) {
					/*
					 * @see org.eclipse.jface.text.IInformationControlExtension5#getInformationPresenterControlCreator()
					 */
					@Override
					public IInformationControlCreator getInformationPresenterControlCreator() {
						return fInformationPresenterControlCreator;
					}
				};
				addLinkListener(iControl);
				return iControl;
			} else {
				return new DefaultInformationControl(parent, tooltipAffordanceString);
			}
		}

		@Override
		public boolean canReuse(IInformationControl control) {
			if (!super.canReuse(control))
				return false;

			if (control instanceof IInformationControlExtension4) {
				String tooltipAffordanceString = EditorsUI.getTooltipAffordanceString();
				((IInformationControlExtension4) control).setStatusText(tooltipAffordanceString);
			}

			return true;
		}
	}
	
	public class MyBrowserInformationControl extends BrowserInformationControl{

		public MyBrowserInformationControl(Shell parent, String symbolicFontName, String statusFieldText) {
			super(parent, symbolicFontName, statusFieldText);
		}
		
		public void setSize(int width, int height) {
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			super.setSize(d.width / 4, d.height / 5);
		}
		
	}
	
	protected XtextBrowserInformationControlInput getHoverInfo(EObject element, IRegion hoverRegion,
			XtextBrowserInformationControlInput previous) {
		String html = getHoverInfoAsHtml(element);
		if (html != null) {
			StringBuffer buffer = new StringBuffer(html);
			HTMLPrinter.insertPageProlog(buffer, 0, getStyleSheet());
			HTMLPrinter.addPageEpilog(buffer);
			html = buffer.toString();
//			html = html.replace("ISO-8859-1", "UTF-8");
			return new XtextBrowserInformationControlInput(previous, element, html, labelProvider);
		}
		return null;
	}
	
	protected String getFirstLine(EObject o) {
		return "";
	}
	
}