
package controllers.popups;

import messages.Messages;
import models.TableKeyValue;
import play.mvc.Util;
import validation.CustomValidation;
import controllers.gen.popups.PopupTablaDeTablasControllerGen;
			
public class PopupTablaDeTablasController extends PopupTablaDeTablasControllerGen {
	public static void crear(TableKeyValue tableKeyValue){
		checkAuthenticity();
		if(!permiso("crear")){
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		TableKeyValue dbtableKeyValue = new TableKeyValue();


		if(!Messages.hasErrors()){
			PopupTablaDeTablasValidateCopy(dbtableKeyValue, tableKeyValue);;
		}

		if(!Messages.hasErrors()){
			TableKeyValue.setValue(tableKeyValue);
		}

		if(!Messages.hasErrors()){
			renderJSON(utils.RestResponse.ok("Registro creado correctamente"));
		}else{
			Messages.keep();
			index("crear",null, "");
		}
	}



	public static void editar(Long idTableKeyValue,TableKeyValue tableKeyValue){
		checkAuthenticity();
		if(!permiso("editar")){
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}


		TableKeyValue dbtableKeyValue = null;
		if(!Messages.hasErrors()){
			dbtableKeyValue = getTableKeyValue(idTableKeyValue);
		}

		if(!Messages.hasErrors()){
			PopupTablaDeTablasValidateCopy(dbtableKeyValue, tableKeyValue);;
		}

		if(!Messages.hasErrors()){
			TableKeyValue.updateValue(dbtableKeyValue, tableKeyValue);
		}

		if(!Messages.hasErrors()){
			renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
		}else{
			Messages.keep();
			index("editar",idTableKeyValue, "");
		}

	}



	public static void borrar(Long idTableKeyValue){
		checkAuthenticity();
		if(!permiso("borrar")){
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		TableKeyValue dbtableKeyValue = null;
		if(!Messages.hasErrors()){
			dbtableKeyValue = getTableKeyValue(idTableKeyValue);
		}

		if(!Messages.hasErrors()){
			TableKeyValue.removeValue(dbtableKeyValue);
		}

		if(!Messages.hasErrors()){
			renderJSON(utils.RestResponse.ok("Registro borrado correctamente"));
		}else{
			Messages.keep();
			index("borrar",idTableKeyValue, "");
		}
	}

	@Util
	protected static void PopupTablaDeTablasValidateCopy(TableKeyValue dbtableKeyValue, TableKeyValue tableKeyValue){
		CustomValidation.clearValidadas();
		CustomValidation.valid("tableKeyValue", tableKeyValue);
		CustomValidation.valid("tableKeyValue", tableKeyValue);
		CustomValidation.valid("tableKeyValue", tableKeyValue);
	}
}
		