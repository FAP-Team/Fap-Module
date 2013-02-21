package templates;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject

import com.google.gson.Gson;


import es.fap.simpleled.led.*
import es.fap.simpleled.led.impl.PopupImpl
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;
import generator.utils.*

public class GPagina extends GGroupElement{

	Pagina pagina;
	Formulario formulario;
	boolean hasForm;
	CampoUtils campo;
	String name;
	boolean mensajeFinal;
	
	public GPagina(Pagina pagina, GElement container){
		super(pagina, container);
		this.pagina = pagina;
		this.name = pagina.name;
		this.formulario = pagina.eContainer();
		this.campo = CampoUtils.create(LedCampoUtils.getCampoPaginaPopup(pagina));
		this.hasForm = this.campo != null && !pagina.noForm && !hayForm(pagina);
		this.mensajeFinal = pagina.mensajeFinal;
		elementos = pagina.getElementos();
	}

	public String view(){
		String viewElementos = "";
		
		for(Elemento elemento : pagina.getElementos()) {
			if (elemento instanceof Tabla) {
				Controller c = Controller.create(this);
				c.initialize();
				viewElementos += getInstance(elemento).viewWithParams(c.getMyAllEntities());
			} else {
				viewElementos += getInstance(elemento).view();
			}
		}
		
		TagParameters params = new TagParameters();
		
		if (getInstancesOf(GSubirArchivo.class).size() > 0)
			params.putStr "encType", "multipart/form-data";
		else
			params.putStr "encType", "application/x-www-form-urlencoded";

		if (pagina.permiso != null) {
			params.putStr "permiso", "${pagina.permiso.name}";
			if (pagina.permiso.mensaje != null)
				params.putStr "permisoMensaje", pagina.permiso.mensaje;
		}
		if (formulario.permiso != null) {
			params.putStr "permiso", formulario.permiso.name;
			if (formulario.permiso.mensaje != null)
				params.putStr "permisoMensaje", formulario.permiso.mensaje;
		}
		
		Controller c = Controller.create(this);
		
		params.put("accion", "accion");
		params.put("mensajeFinal", mensajeFinal);
		params.put("urlEditar", c.getRouteAccion("editar"));
		params.put("urlCrear", c.getRouteAccion("crear"));
		params.put("urlBorrar", c.getRouteAccion("borrar"));
		params.putStr("titulo", pagina.titulo != null ? pagina.titulo : pagina.name);
		params.putStr("formulario", formulario.name);
		params.putStr("pagina", name);
		params.put("hayForm", hasForm);
		params.putStr("botonEditar", c.accionEditar.boton);
		params.putStr("botonCrear", c.accionCrear.boton);
		params.putStr("botonBorrar", c.accionBorrar.boton);
		
		String view = """
#{fap.pagina ${params.lista(true)}
}
	${viewElementos}
#{/fap.pagina}
		""";
		
		FileUtils.overwrite(FileUtils.getRoute('VIEW'), "${pagina.name}/${pagina.name}.html", view);
	}
	
	public String controllerName(){
		return "${pagina.name}Controller";
	}

	public String controllerGenName(){
		return controllerName() + "Gen";
	}

	public String controllerFullName(){
		return controllerName();
	}

	public String controllerGenFullName(){
		return controllerGenName();
	}

	public String url(){
		return "/${formulario.name}/${pagina.name.toLowerCase()}";
	}

	public String controller(){
		Controller.create(this).controller();
	}
	
	public String routes(){
		Controller.create(this).generateRoutes();
	}
	
	public String saveCode(){
		String saveCode = super.saveCode();
		String saveSolicitud = "";
		
		if ((pagina.copia) && (!Controller.create(this).getItvariableDb().contains("dbSolicitud"))){
			saveSolicitud="""dbSolicitud.save(); """;
		}
		if (pagina.guardarParaPreparar){
			saveCode += """
				if(!validation.hasErrors()){
					dbSolicitud.savePages.pagina${pagina.name} = true;
				}
			""";
			
		}
		if (pagina.copia){
			saveCode += """
						   if (hayModificaciones){
							   Gson gson = new Gson();
							   String jsonPM = gson.toJson(peticionModificacion);
							   JsonPeticionModificacion jsonPeticionModificacion = new JsonPeticionModificacion();
							   jsonPeticionModificacion.jsonPeticion = jsonPM;
							   dbSolicitud.registroModificacion.get(dbSolicitud.registroModificacion.size()-1).jsonPeticionesModificacion.add(jsonPeticionModificacion);
						   	   dbSolicitud.save();
							}
						""";
						//${saveSolicitud}
		}
		return saveCode;
	}
	
	public static boolean hayForm(EObject container){
		if (container instanceof Form)
			return true;
		EList<Elemento> elementos = LedCampoUtils.getElementos(container);
		if (elementos != null){
			for (EObject obj: elementos){
				if (hayForm(obj)){
					return true;
				}
			}
			return false;
		}
		return false;
	}
	

	
}
