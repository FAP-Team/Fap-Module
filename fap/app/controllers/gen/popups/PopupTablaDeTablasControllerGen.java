
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



public class PopupTablaDeTablasControllerGen extends GenericController {

    
				@Util
				protected static TableKeyValue getTableKeyValue(Long idTableKeyValue){
					TableKeyValue tableKeyValue = null;
					if(idTableKeyValue == null){
						Messages.fatal("Falta parámetro idTableKeyValue");
					}else{
						tableKeyValue = TableKeyValue.findById(idTableKeyValue);
						if(tableKeyValue == null){
							Messages.fatal("Error al recuperar TableKeyValue");
						}
					}
					return tableKeyValue;
				}
			

    
	public static void abrir(String accion,Long idTableKeyValue){
		TableKeyValue tableKeyValue;
		if(accion.equals("crear")){
            tableKeyValue = new TableKeyValue();
		}else{
		    tableKeyValue = getTableKeyValue(idTableKeyValue);
		}

		if (!permiso(accion)){
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		renderArgs.put("controllerName", "PopupTablaDeTablasControllerGen");
		renderTemplate("gen/popups/PopupTablaDeTablas.html",accion,idTableKeyValue,tableKeyValue);
	}
        

    
			@Util
            protected static boolean permiso(String accion) {
                //Sobreescribir para incorporar permisos a mano
			return true;
            }
        

    
                public static void crear(TableKeyValue tableKeyValue){
                    checkAuthenticity();
                    if(!permiso("create")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    TableKeyValue dbTableKeyValue = new TableKeyValue();
                    

                    if(!Messages.hasErrors()){
                        PopupTablaDeTablasValidateCopy(dbTableKeyValue, tableKeyValue);;
                    }


                    if(!Messages.hasErrors()){
                        dbTableKeyValue.save();
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro creado correctamente"));
                    }else{
                        Messages.keep();
                        abrir("crear",null);
                    }
                }
            

    
                public static void editar(Long idTableKeyValue,TableKeyValue tableKeyValue){
                    checkAuthenticity();
                    if(!permiso("update")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    
            TableKeyValue dbTableKeyValue = null;
            if(!Messages.hasErrors()){
                dbTableKeyValue = getTableKeyValue(idTableKeyValue);
            }
            

                    if(!Messages.hasErrors()){
                        PopupTablaDeTablasValidateCopy(dbTableKeyValue, tableKeyValue);;
                    }

                    if(!Messages.hasErrors()){
                        dbTableKeyValue.save();
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
                    }else{
                        Messages.keep();
                        abrir("editar",idTableKeyValue);
                    }

                }
            

    
                public static void borrar(Long idTableKeyValue){
                    checkAuthenticity();
                    if(!permiso("delete")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    
            TableKeyValue dbTableKeyValue = null;
            if(!Messages.hasErrors()){
                dbTableKeyValue = getTableKeyValue(idTableKeyValue);
            }
            

                    if(!Messages.hasErrors()){
                        dbTableKeyValue.delete();
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro borrado correctamente"));
                    }else{
                        Messages.keep();
                        abrir("borrar",idTableKeyValue);
                    }
                }
            

    
			@Util
			protected static void PopupTablaDeTablasValidateCopy(TableKeyValue dbTableKeyValue, TableKeyValue tableKeyValue){
				CustomValidation.clearValidadas();
				CustomValidation.valid("tableKeyValue", tableKeyValue);
dbTableKeyValue.table = tableKeyValue.table;
dbTableKeyValue.key = tableKeyValue.key;
dbTableKeyValue.value = tableKeyValue.value;

				
			}
		

    
}
