package es.fap.simpleled.ui.documentation;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider;
import org.eclipse.xtext.ui.editor.hover.html.XtextBrowserInformationControlInput;

import com.google.inject.Inject;

import es.fap.simpleled.led.Attribute;
import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.util.DocElemento;
import es.fap.simpleled.led.util.DocParametro;
import es.fap.simpleled.led.util.LedDocumentationUtils;

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
		"Entity:Entidad", "Attribute", "Formulario", "Menu",
		"MenuGrupo:Grupo", "MenuEnlace:Enlace", "Pagina", "Popup", "Grupo", "AgruparCampos", "Texto",
		"AreaTexto", "Check", "Enlace", "Wiki", "Boton", "Fecha", "Combo", "Form", "Tabla",
		"Columna", "SubirArchivo", "SubirArchivoAed", "EditarArchivoAed", "FirmaPlatinoSimple:FirmaSimple",
		"Direccion", "Nip", "PersonaFisica", "PersonaJuridica", "Persona", "Solicitante",
		"EntidadAutomatica", "Lista"
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
		String feature = getFeature(node_);
		if ("campo".equals(feature)){
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
		if (feature != null){
			DocParametro parametro = JsonDocumentation.getParametro(feature, semantic);
			if (parametro != null){
				return LedDocumentationUtils.getDocumentation(parametro);
			}
		}
		return "";
	}
	
	public String getDocumentation(Entity entidad, String name) {
		String text = NodeModelUtils.getNode(entidad).getText();
		text = text.trim().replaceAll("\n", "</br>").replaceAll(" ", "&nbsp;").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		Pattern pa = Pattern.compile("(\\W)" + name + "(\\W)");
		Matcher m = pa.matcher(text);
		if (m.find()){
			return m.replaceFirst(m.group(1) + "<b>" + name + "</b>" + m.group(2));
		}
		return text;
	}
	
	public static String getFeature(INode node){
		while (node != null){
			EObject grammar = node.getGrammarElement();
			while (grammar != null){
				if (grammar instanceof Assignment){
					Assignment assignment = (Assignment) grammar;
					ParserRule rule = getParserRule(assignment);
					if (docRules.containsKey(rule.getName())){
						return assignment.getFeature();
					}
				}
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
			html.replace("ISO-8859-1", "UTF-8");
			return new XtextBrowserInformationControlInput(previous, element, html, labelProvider);
		}
		return null;
	}
	
	protected String getFirstLine(EObject o) {
		return "";
	}
	
}