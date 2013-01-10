package controllers;

import java.util.List;
import java.util.Map;

import models.ItemsFactura;
import controllers.gen.VerFacturaControllerGen;

public class VerFacturaController extends VerFacturaControllerGen {
	
	public static void getItemsFactura(Long idFacturaDatos) {
		
		java.util.List<ItemsFactura> rows = ItemsFactura.find("select informacionDetallada from FacturaDatos facturaDatos join facturaDatos.informacionDetallada informacionDetallada where facturaDatos.id=?", idFacturaDatos).fetch();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		List<ItemsFactura> rowsFiltered = rows; //Tabla sin permisos, no filtra
		tables.TableRenderResponse<ItemsFactura> response = new tables.TableRenderResponse<ItemsFactura>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);
		
		renderJSON(response.toJSON("descripcionItem", "cantidad", "unidadMedida", "precioUnidadSinImpuestos_formatFapTabla", "importeCargo_formatFapTabla", "totalImporteDescuentos_formatFapTabla", "totalImporteBruto_formatFapTabla", "id"));
	}
}
