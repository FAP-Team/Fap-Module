package templates;

import java.util.List;
import org.eclipse.emf.ecore.EObject


import generator.utils.*;
import es.fap.simpleled.led.*;
import es.fap.simpleled.led.util.LedCampoUtils
import es.fap.simpleled.led.util.LedEntidadUtils;

public class GForm extends GGroupElement{

	Form form;
	String name;
	String target;
	
	public GForm(Form form, GElement container){
		super(form, container);
		
		if (container == null)
			container = container.container;
		
		this.form = form;
		this.name = StringUtils.firstLower(form.name);
		if(form.destino != null){
			this.target = form.destino;
		} else {
			this.target = null;
		}
		elementos = form.getElementos();
	}
	
	public String view(){
		String elementos = "";
		for(Elemento elemento: form.elementos){
			elementos += getInstance(elemento).view();
		}
		
		String encTypeStr = "";
		if (getInstancesOf(GSubirArchivo.class).size() > 0)
			encTypeStr = ", enctype:\"multipart/form-data\"";
		
		String view;
		if (target != null){
			view = """
				#{form ${Controller.create(this).getRouteAccion(name)} ${encTypeStr}, class:"form-horizontal", target:"${target}", id:"${name}"}
					${elementos}
				#{/form}
			""";
		} else {
			view = """
				#{form ${Controller.create(this).getRouteAccion(name)} ${encTypeStr}, class:"form-horizontal", id:"${name}"}
					${elementos}
				#{/form}
			""";
		}
		if(form.autoEnviar){
			view += """
				<script>
					\$(function(){
						\$('#${name} input, #${name} select, #${name} textarea').change(function(){
							\$('#${name}').submit();
						});
				</script>
			""";
		}
		
		if (form.permiso != null) {
			view = """
				#{fap.permiso permiso:"${form.permiso.name}", accion:accion}
					$view
				#{/fap.permiso}		
			""";
		}
		return view;
	}
	
	public String controller(){
		Controller controller = Controller.create(this);
		Controller containerController = Controller.create(getPaginaOrPopupContainer());
		return """
			${controller.metodoEditar()}
			${controller.metodoEditarRender()}
			${controller.metodoValidateCopy()}
			${controller.metodoEditarValidateRules()}
			${controller.metodoPermiso()}
			${controller.metodosControllerElementos()}
			${controller.metodoBindReferences()}
			${controller.metodosGettersForm(containerController)}
		"""; 
	}

	public String routes(){
		return Controller.create(this).generateRoutes();
	}
	
}
