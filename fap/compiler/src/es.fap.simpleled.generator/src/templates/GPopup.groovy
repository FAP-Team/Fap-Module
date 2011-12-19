package templates;

import java.util.List;
import java.util.Map;

import generator.utils.*;
import generator.utils.HashStack.HashStackName;
import es.fap.simpleled.led.*;
import generator.utils.EntidadUtils;
import es.fap.simpleled.led.util.LedCampoUtils;
import es.fap.simpleled.led.util.LedEntidadUtils;

public class GPopup {

	def elementoGramatica;
	def Popup popup;
	String name;
	def	formulario = "";
	
	List<EntidadUtils> saveEntity;
	List<String> saveExtra;
	List<String> saveCode;
	List<String> saveController;
	
	EntidadUtils almacen;
	EntidadUtils entidad;
	
	public static String generate(Popup popup){
		GPopup g = new GPopup();
		g.popup = popup;
		g.elementoGramatica = popup;
		g.name = popup.name;
		g.formulario = ModelUtils.getActualContainer().name

		HashStack.push(HashStackName.ROUTES, g)
		HashStack.push(HashStackName.GPOPUP, g)
		
		Attribute attr = LedCampoUtils.getUltimoAtributo(popup.campo);
		if (LedEntidadUtils.xToMany(attr)){
			g.almacen = EntidadUtils.create(popup.campo.entidad);
			g.entidad = EntidadUtils.create(LedEntidadUtils.getEntidad(attr));
		}
		else{
			g.entidad = EntidadUtils.create(popup.campo.entidad);
		}
		
		g.view();
		g.controller();
		
		HashStack.pop(HashStackName.GPOPUP)
	}
	
	public String controllerName(){
		return popup.name + "Controller";
	}
	
	public String controllerFullName(){
		return "popups." + controllerName();
	}
	
	public String controllerGenName(){
		return controllerName() + "Gen";
	}
	
	public String controllerGenFullName(){
		return "popups." + controllerGenName();
	}
	
	public String view(){
		String elementos = "";
		
		int sizeEntity = HashStack.size(HashStackName.SAVE_ENTITY)
		int sizeExtra = HashStack.size(HashStackName.SAVE_EXTRA)
		int sizeCode = HashStack.size(HashStackName.SAVE_CODE)
		int sizeController = HashStack.size(HashStackName.CONTROLLER)
		
		for(Elemento elemento : popup.getElementos()){
			elementos += Expand.expand(elemento);
		}
		
		saveEntity = HashStack.popUntil(HashStackName.SAVE_ENTITY, sizeEntity).unique();
		saveExtra = HashStack.popUntil(HashStackName.SAVE_EXTRA, sizeExtra);
		saveCode = HashStack.popUntil(HashStackName.SAVE_CODE, sizeCode);
		saveController = HashStack.popUntil(HashStackName.CONTROLLER, sizeController)
				
		String titulo = popup.titulo ?: popup.name;
		
        TagParameters params = new TagParameters();
        params.putStr("popup", popup.name)
        params.putStr("titulo", titulo)
        params.put("action", 'accion')

        if (hayCamposEnPopup()){
			if (almacen != null){
				params.put('hidden', "[${almacen.id}:${almacen.id}, ${entidad.id}: ${entidad.id}]");
			}
			else{
				params.put('hidden', "[${entidad.id}: ${entidad.id}]");
			}
        }

		if (popup.permiso != null) {
			params.putStr('permiso', popup.permiso.name)
		}

		String view = """
#{fap.popup ${params.lista()}}
	${elementos}
#{/fap.popup}
		"""
					
		FileUtils.overwrite(FileUtils.getRoute('VIEW'), "popups/${popup.name}.html", view);
	}
	
	public String controller(){
	    boolean popupCompleto = !((popup.crear) || (popup.modificar) || (popup.borrar) || (popup.ver));

		//Parámetros del método crear
		//def saveParamsStr = saveParams != null ? ", " + saveParams.unique().join(",") : "";
		def saveParamsStr = saveEntity != null ? ", " + saveEntity.collect { "${it.clase} ${it.variable}" }.join(",") : "";
		
		String controllerHS = "";
		for(elemento in saveController){
			controllerHS += elemento.controller();
		}

		CampoUtils popupCampo = CampoUtils.create(popup.campo);

        //Getters
        String getters;
        if(almacen != null){
            getters = """
            ${ControllerUtils.simpleGetter(almacen, true)}
            ${ControllerUtils.complexGetter(almacen, entidad, popupCampo)}
            """
        }else{
            getters = ControllerUtils.simpleGetter(entidad, true)
        }

        //metodo Abrir
        def abrirParams = ["String accion"];
        abrirParams.add(entidad.typeId)
        if (almacen != null) abrirParams.add(almacen.typeId)

        String getterCall = almacen != null? ControllerUtils.complexGetterCall(almacen, entidad): ControllerUtils.simpleGetterCall(entidad, true)

        def abrirRenderParams = ["\"gen/popups/${popup.name}.html\"", "accion"]
        abrirRenderParams.add(entidad.id)
        abrirRenderParams.add(entidad.variable)
        if(almacen != null) abrirRenderParams.add(almacen.id)

		boolean hayTabla = hayTablaEnPopup();
		String guardarAlCrear = "";
		if (hayTabla){
			guardarAlCrear = """${entidad.variable}.save();		
			${entidad.id} = ${entidad.variable}.id;
			((Map<String, Long>)tags.TagMapStack.top("idParams")).put("${entidad.id}", ${entidad.id});"""
		}
		
        String metodoAbrir = """
	public static void abrir(${abrirParams.join(",")}){
		$entidad.clase $entidad.variable;
		if(accion.equals("crear")){
            $entidad.variable = new $entidad.clase();
			${guardarAlCrear}
		}else{
		    $entidad.variable = $getterCall;
		}

		if (!permiso(accion)){
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		renderArgs.put("controllerName", "${controllerGenName()}");
		renderTemplate(${abrirRenderParams.join(',')});
	}
        """

        //permiso

        String metodoPermiso = """
			@Util
            protected static boolean permiso(String accion) {
                ${ControllerUtils.permisoContent(popup.permiso)}
            }
        """

        //Metodo editar
        String metodoEditar = ""

		if ((popup.modificar) || (popupCompleto)) {

            def editarParams = []
            if(almacen != null) editarParams.add(almacen.typeId)
            editarParams.addAll([entidad.typeId, entidad.typeVariable]);

            def editarAbrirCallParams = ['"editar"', entidad.id]
            if(almacen != null) editarAbrirCallParams.add(almacen.id)

            metodoEditar = """
                public static void editar(${editarParams.join(',')}){
                    checkAuthenticity();
                    if(!permiso("update")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    ${ControllerUtils.fullGetterCall(almacen, entidad)}

                    if(!Messages.hasErrors()){
                        ${ControllerUtils.validateCopyCall(this, entidad)};
                    }

                    if(!Messages.hasErrors()){
                        ${entidad.variableDb}.save();
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
                    }else{
                        Messages.keep();
                        abrir(${editarAbrirCallParams.join(',')});
                    }

                }
            """
        }

        String metodoCrear = ""
        if(popup.crear || popupCompleto){
            def crearParams = []
            if(almacen != null) crearParams.add(almacen.typeId)
			if (hayTabla) crearParams.add(entidad.typeId)
            crearParams.add(entidad.typeVariable);

            String crearCrearSaveCall = ""
            if(almacen == null){
                crearCrearSaveCall = "${entidad.variableDb}.save();"
            }else{
                crearCrearSaveCall = """
                ${entidad.variableDb}.save();
                db${popupCampo.str}.add(${entidad.variableDb});
                ${almacen.variableDb}.save();
                """
            }

            def crearAbrirCallParams = ['"crear"', null]
            if(almacen != null) crearAbrirCallParams.add(almacen.id)

			String newEntidad = "${entidad.clase} ${entidad.variableDb} = new ${entidad.clase}();";
			if (hayTabla){
				newEntidad = "${entidad.clase} ${entidad.variableDb} = ${entidad.clase}.findById(${entidad.id});";
			}
			
            metodoCrear = """
                public static void crear(${crearParams.join(",")}){
                    checkAuthenticity();
                    if(!permiso("create")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    ${newEntidad}
                    ${ControllerUtils.fullGetterCall(null, almacen)}

                    if(!Messages.hasErrors()){
                        ${ControllerUtils.validateCopyCall(this, entidad)};
                    }


                    if(!Messages.hasErrors()){
                        $crearCrearSaveCall
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro creado correctamente"));
                    }else{
                        Messages.keep();
                        abrir(${crearAbrirCallParams.join(',')});
                    }
                }
            """
        }


        String metodoBorrar = ""
        if ((popup.borrar) || (popupCompleto)) {
            def borrarParams = []
            if(almacen != null) borrarParams.add(almacen.typeId)
            borrarParams.addAll([entidad.typeId]);

            def borrarGetter = ControllerUtils.fullGetterCall(almacen, entidad);

            def borrarBorrarEntidad = ""
            if(almacen != null){
                borrarBorrarEntidad = """${popupCampo.firstLower()}.remove($entidad.variableDb);
                ${almacen.variable}.save();
                """
            }
			
			if (!popup.noBorrarEntidad) {
				borrarBorrarEntidad += "${entidad.variableDb}.delete();"
			}
			
            def borrarAbrirCallParams = ['"borrar"', entidad.id]
            if(almacen != null) borrarAbrirCallParams.add(almacen.id)

            metodoBorrar  ="""
                public static void borrar(${borrarParams.join(",")}){
                    checkAuthenticity();
                    if(!permiso("delete")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    $borrarGetter

                    if(!Messages.hasErrors()){
                        $borrarBorrarEntidad
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro borrado correctamente"));
                    }else{
                        Messages.keep();
                        abrir(${borrarAbrirCallParams.join(',')});
                    }
                }
            """
        }


		String controllerGen = """
package controllers.gen.popups;

import play.*;
import play.mvc.*;
import play.db.jpa.Model;
import controllers.fap.*;
import validation.*;
import messages.Messages;

import models.*;
import tags.ReflectionUtils;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import security.Secure;
import javax.inject.Inject;

public class ${controllerGenName()} extends GenericController {

	@Inject
	protected static Secure secure;

    $getters

    $metodoAbrir

    $metodoPermiso

    $metodoCrear

    $metodoEditar

    $metodoBorrar

    ${ControllerUtils.validateCopyMethod(this, entidad)}

    $controllerHS
}
"""
		
		FileUtils.overwrite(FileUtils.getRoute('CONTROLLER_GEN'),controllerGenFullName().replaceAll("\\.", "/") + ".java", controllerGen);
			
		String controller = """
package controllers.popups;

import controllers.gen.popups.${controllerGenName()};
			
public class ${controllerName()} extends ${controllerGenName()} {

}
		"""
		FileUtils.write(FileUtils.getRoute("CONTROLLER"), controllerFullName().replaceAll("\\.", "/") + ".java", controller);
	}

	public String url(){
		String idSolicitud = "";
		if (ModelUtils.isSolicitudForm())
			idSolicitud = "/{idSolicitud}";
		return "/${formulario}${idSolicitud}/${popup.name.toLowerCase()}/{idEntidad}";			
	}
	
	public String generateRoutes(){
		StringBuffer sb = new StringBuffer();
		StringUtils.appendln sb, Route.to("GET", url() + "/abrir", controllerFullName() + ".abrir")
		StringUtils.appendln sb, Route.to("POST", url() + "/editar", controllerFullName() + ".editar")
		StringUtils.appendln sb, Route.to("POST", url() + "/borrar", controllerFullName() + ".borrar")
		StringUtils.appendln sb, Route.to("POST", url() + "/crear", controllerFullName() + ".crear")
		StringUtils.appendln sb, Route.to("POST", url() + "/cancelarcrear", controllerFullName() + ".cancelarCrear")
		return sb.toString();
	}
	
	private boolean hayCamposEnPopup(){
		return hayCampos(popup);
	}
	
	private boolean hayCampos(Object o){
		if(o.metaClass.respondsTo(o,"getElementos")){
			for (Object elemento: o.elementos){
				if (hayCampos(elemento)){
					return true;
				}
			}
			return false;
		}
		if(o.metaClass.respondsTo(o,"getCampo")){
			if(! (o instanceof Tabla)){
				return true;
			}
		}
	}
	
	private boolean hayTablaEnPopup(){
		return hayTabla(popup);
	}
	
	private boolean hayTabla(Object o){
		if(o.metaClass.respondsTo(o,"getElementos")){
			for (Object elemento: o.elementos){
				if (hayTabla(elemento)){
					return true;
				}
			}
			return false;
		}
		if(o instanceof Tabla){
			return true;
		}
	}
	
}
