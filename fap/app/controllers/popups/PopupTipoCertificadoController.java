package controllers.popups;

import java.util.ArrayList;
import java.util.List;

import models.TableKeyValue;
import models.TipoDocumento;

import properties.FapProperties;

import tags.ComboItem;
import controllers.gen.popups.PopupTipoCertificadoControllerGen;

public class PopupTipoCertificadoController extends PopupTipoCertificadoControllerGen {

	public static List<ComboItem> tipoDocumento() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		String tiposValidos = FapProperties.get("fap.anotaciones.administrativas.autorizadas.tipos.documentos");
		String [] tipoValidosSplit = tiposValidos.split(",");
		String nombre;
		for (String tipo : tipoValidosSplit){
			nombre = TableKeyValue.getValue("tiposDocumentos", tipo.trim());
			result.add(new ComboItem(tipo.trim(), nombre));
		}
		return result;
	}
	
}
