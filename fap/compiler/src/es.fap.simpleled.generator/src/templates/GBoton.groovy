package templates;

import org.eclipse.emf.ecore.EObject
import es.fap.simpleled.led.impl.EnlaceImpl;
import es.fap.simpleled.led.impl.LedFactoryImpl;
import es.fap.simpleled.led.util.ModelUtils
import es.fap.simpleled.led.Enlace;
import es.fap.simpleled.led.FirmaSimple
import es.fap.simpleled.led.LedFactory
import generator.utils.*;
import es.fap.simpleled.led.Boton;

public class GBoton extends GElement{

	Boton boton;
	
	public GBoton(Boton boton, GElement container){
		super(boton, container);
		this.boton = boton;
	}

	public void generate(){
		if (boton.pagina || boton.popup || boton.anterior) {
			Enlace enlace = LedFactory.eINSTANCE.createEnlace();
			enlace.name = (boton.name ?: "") + "IDenlace";
			enlace.titulo = boton.titulo;
			String btnType = (boton.type!= null && (!boton.type.toString().equals("default"))) ? "btn-"+boton.type : "";
			enlace.estilo = "btn ${btnType}";
			if (boton.pagina)
				enlace.getMetaClass().setAttribute(enlace, "pagina", boton.pagina);
			else if (boton.popup)
				enlace.getMetaClass().setAttribute(enlace, "popup", boton.popup);
			else if (boton.anterior)
				enlace.anterior = true;
			
			getGroupContainer().replaceElement(enlace, boton);
		}
	}
	
    public String view(){
        TagParameters params = new TagParameters();
        params.putStr("id", boton.name)
        params.putStr("titulo", boton.titulo)
        if (boton.ancho != null)
            params.put "ancho", boton.ancho;
		if (boton.isWaitPopup())
			params.put "waitPopup", boton.isWaitPopup();
		if (boton.type != null)
			params.putStr "type", "btn-"+boton.type;
		if (boton.refrescar)
			params.put "refrescar", true
		if (boton.ayuda != null) {
			if ((boton.tipoAyuda != null) && ((boton.tipoAyuda.type.equals("propover")) || (boton.tipoAyuda.type.equals("popover"))))
				params.put "ayuda", "tags.TagAyuda.popover('${boton.ayuda}')";
			else
				params.put "ayuda", "tags.TagAyuda.texto('${boton.ayuda}')";
		}
		if (boton.claveIdFijo)
			params.put "claveIdFijo", true
		if (boton.noSubmit)
			params.put "noSubmit", true
		if (boton.style)
		      params.putStr("style", boton.style)
		

		return """
			#{fap.boton ${params.lista()} /}
		""";
    }
	
	public String controller(){
		Controller controller = Controller.create(getControllerContainer());
		
		if (controller.isForm() && controller.gElement.getInstancesOf(GBoton.class).size() == 1 && controller.gElement.getInstancesOf(GFirmaSimple.class).size() == 0)
			return "";
		return """
			@Util
			public static void ${StringUtils.firstLower(boton.name)}${controller.sufijoBoton}(${StringUtils.params(
				controller.allEntities.collect{it.typeId},
				controller.saveEntities.collect{it.typeVariable},
				controller.extraParams
			)}){
				//Sobreescribir este método para asignar una acción
			}
		""";
	}
	
}
