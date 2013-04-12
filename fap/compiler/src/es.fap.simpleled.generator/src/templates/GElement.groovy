package templates;

import es.fap.simpleled.led.*;
import generator.utils.Entidad;

import org.eclipse.emf.ecore.EObject;

import templates.elements.*;

public class GElement {

	static private Map<EObject, GElement> cache;
	
	/*
	 * Elemento que se genera
	 */
	public EObject element;
	
	/*
	 * GElemento que contiene al actual
	 */
	public GElement container;
	
	public GElement(EObject element, GElement container){
		this.element = element;
		this.container = container;
	}
	
	public void generate(){}
	
	public String view(){
		return "";	
	}
	
	public String controller(){
		return "";
	}
	
	public String routes(){
		return "";
	}
	
	public String saveCode(){
		return "";
	}
	
	public Set<Entidad> saveEntities(){
		return new HashSet<Entidad>();
	}
	
	public Set<Entidad> dbEntities(){
		return new HashSet<Entidad>();
	}
	
	public List<String> extraParams(){
		return new ArrayList<String>();
	}
	
	public String validateCopy(Stack<Set<String>> validatedFields){
		return "";
	}
	
	public String validate(Stack<Set<String>> validatedFields){
		return "";
	}
	
	public String copy(){
		return "";
	}
	
	public String validateCopy(){
		Stack<Set<String>> validatedFields = new Stack<Set<String>>();
		validatedFields.push(new HashSet<String>());
		return validateCopy(validatedFields);
	}
	
	public String bindReferences(){
		return "";
	}
	
	public GGroupElement getGroupContainer(){
		GElement c = container;
		while (c != null && ! (c instanceof GGroupElement))
			c = c.container;
		return c;
	}
	
	public GGroupElement getPaginaOrPopupContainer(){
		GElement c = container;
		while (c != null && ! (c instanceof GPopup || c instanceof GPagina))
			c = c.container;
		return c;
	}
	
	public GGroupElement getControllerContainer(){
		GElement c = container;
		while (c != null && ! (c instanceof GPopup || c instanceof GPagina || c instanceof GForm))
			c = c.container;
		return c;
	}
	
	public List<GElement> getInstancesOf(Class clazz){
		List<GElement> instances = new ArrayList<GElement>();
		if (this.getClass().equals(clazz))
			instances.add(this);
		return instances;
	}
	
	public GElement getInstance(EObject object){
		return getInstance(object, this);
	}
	
	public static GElement getInstance(EObject element, GElement container){
		if (cache == null)
			cache = new HashMap<EObject, GElement>();
			
		if (cache.get(element) == null){
			
			if(element instanceof Entity)
				cache.put(element, new GEntidad(element, container));
		
			else if(element instanceof Formulario)
				cache.put(element, new GFormulario(element, container));

			else if(element instanceof Pagina)
				cache.put(element, new GPagina(element, container));
			
			else if(element instanceof Popup)
				cache.put(element, new GPopup(element, container));

			else if(element instanceof Texto)
				cache.put(element, new GTexto(element, container));

			else if(element instanceof Fecha)
				cache.put(element, new GFecha(element, container));
			
			else if(element instanceof Combo)
				cache.put(element, new GCombo(element, container));
		
			else if(element instanceof Tabla)
				cache.put(element, new GTabla(element, container));
			
			else if(element instanceof Lista)
				cache.put(element, new GLista(element, container));
			
			else if(element instanceof Grupo)
				cache.put(element, new GGrupo(element, container));
			
			else if(element instanceof Menu)
				cache.put(element, new GMenu(element, container));

			else if(element instanceof Nip)
				cache.put(element, new GNip(element, container));
			
			else if(element instanceof PersonaFisica)
				cache.put(element, new GPersonaFisica(element, container));
			
			else if(element instanceof PersonaJuridica)
				cache.put(element, new GPersonaJuridica(element, container));
		
			else if (element instanceof Solicitante)
				cache.put(element, new GSolicitante(element, container));
			
			else if(element instanceof Persona)
				cache.put(element, new GPersona(element, container));

			else if(element instanceof Direccion)
				cache.put(element, new GDireccion(element, container));
				
			else if(element instanceof DireccionMapa)
				cache.put(element, new GDireccionMapa(element, container));
			
			else if(element instanceof Boton)
				cache.put(element, new GBoton(element, container));
		
			else if(element instanceof Check)
				cache.put(element, new GCheck(element, container));
		
			else if(element instanceof RadioBooleano)
				cache.put(element, new GRadioBooleano(element, container));
				
			else if(element instanceof Wiki)
				cache.put(element, new GWiki(element, container));
				
			else if(element instanceof Codigo)
				cache.put(element, new GCodigo(element, container));
			
			else if(element instanceof EntidadAutomatica)
				cache.put(element, new GEntidadAutomatica(element, container));
		
			else if(element instanceof AreaTexto)
				cache.put(element, new GAreaTexto(element, container));

			else if(element instanceof Enlace)
				cache.put(element, new GEnlace(element, container));
			
			else if(element instanceof AgruparCampos)
				cache.put(element, new GAgruparCampos(element, container));
		
			else if(element instanceof Permiso)
				cache.put(element, new GPermiso(element, container));
		
			else if(element instanceof Form)
				cache.put(element, new GForm(element, container));
	
			else if(element instanceof SubirArchivo)
				cache.put(element, new GSubirArchivo(element, container));
			
			else if(element instanceof EditarArchivo)
				cache.put(element, new GEditarArchivo(element, container));
			
			else if(element instanceof FirmaSimple)
				cache.put(element, new GFirmaSimple(element, container));
			
			else if(element instanceof AgrupaBotones)
				cache.put(element, new GAgrupaBotones(element, container));
			
			else if (element instanceof Accion)
				cache.put(element, new GElement(element, container));
				
			else if (element instanceof BarraDeslizante)
				cache.put(element, new GBarraDeslizante(element, container));

			else if (element instanceof ServicioWeb)
				cache.put(element, new GServicioWeb(element, container));

			else if(element instanceof CCC)
				cache.put(element, new GCCC(element, container));
			
			else if(element instanceof SubirFactura)
				cache.put(element, new GSubirFactura(element, container));
			
			else if(element instanceof EditarFactura)
				cache.put(element, new GEditarFactura(element, container));
			
			else if(element instanceof VisorFactura)
				cache.put(element, new GVisorFactura(element, container));

			else if(element instanceof TextoOculto)
				cache.put(element, new GTextoOculto(element, container));
				
			else if(element instanceof RadioButton)
				cache.put(element, new GRadioButton(element, container));
			
			else if(element instanceof GrupoRadioButtons)
				cache.put(element, new GGrupoRadioButtons(element, container));
		}
		
		return cache.get(element);
	}
	
}
