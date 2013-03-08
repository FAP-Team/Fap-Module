package templates;

import java.util.List;
import java.util.Map;
import java.util.Set;

import es.fap.simpleled.led.Entity;
import es.fap.simpleled.led.VisorFactura
import generator.utils.*;


public class GVisorFactura extends GSaveCampoElement {
	
	GElement gPaginaPopup;
	VisorFactura VisorFactura;
	
	public GVisorFactura(VisorFactura visorFactura, GElement container) {
		super(visorFactura, container);
		this.visorFactura = visorFactura;
		campo = CampoUtils.create(visorFactura.campo);
		gPaginaPopup = getPaginaOrPopupContainer();
	}
	
	public String view() {
		TagParameters params = new TagParameters()
		if(visorFactura.name != null)
			params.putStr("id", visorFactura.name)
		String controllerName = gPaginaPopup.name;
		params.putStr("campo", campo.firstLower())
		params.putStr("controllerName", "${controllerName}");
		return "#{fap.visorfactura ${params.lista()} /}	";
	}
	
	public String controller() {
		String out = """
			public static void getItemsFactura(Long idFacturaDatos) {				
				java.util.List<ItemsFactura> rows = ItemsFactura.find("select informacionDetallada from FacturaDatos facturaDatos join facturaDatos.informacionDetallada informacionDetallada where facturaDatos.id=?", idFacturaDatos).fetch();
				Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
				List<ItemsFactura> rowsFiltered = rows;
				tables.TableRenderResponse<ItemsFactura> response = new tables.TableRenderResponse<ItemsFactura>(rowsFiltered, false, false, false, "", "", "", getAccion(), ids);
				renderJSON(response.toJSON("descripcionItem", "cantidad", "unidadMedida", "precioUnidadSinImpuestos_formatFapTabla", "importeCargo_formatFapTabla", "totalImporteDescuentos_formatFapTabla", "totalImporteBruto_formatFapTabla", "id"));
			}
		""";
		return out;
	}

	public String routes(){
		String url = "/" + gPaginaPopup.name.toLowerCase() + "/getitemsfactura";
		String action = gPaginaPopup.controllerFullName() + ".getitemsfactura";
		return RouteUtils.to("GET", url, action).toString() + "\n";
	}
	
}