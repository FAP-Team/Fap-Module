package models;

import java.util.*;
import javax.persistence.*;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.data.validation.*;
import org.joda.time.DateTime;
import models.*;
import messages.Messages;
import validation.*;
import audit.Auditable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// === IMPORT REGION START ===
import es.mityc.facturae32.InvoiceLineType;

// === IMPORT REGION END ===

@Entity
public class ItemsFactura extends FapModel {
	// Código de los atributos

	public String descripcionItem;

	public Double cantidad;

	public String unidadMedida;

	@Moneda
	@Transient
	public Double totalImporteDescuentos;

	@Moneda
	public Double totalImporteBruto;

	@Moneda
	public Double importeCargo;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "itemsfactura_descuentos")
	public List<Descuentos> descuentos;

	@Moneda
	public Double precioUnidadSinImpuestos;

	@Transient
	public String totalImporteDescuentos_formatFapTabla;

	@Transient
	public String totalImporteBruto_formatFapTabla;

	@Transient
	public String importeCargo_formatFapTabla;

	@Transient
	public String precioUnidadSinImpuestos_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getTotalImporteDescuentos_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalImporteDescuentos);
	}

	// Getter del atributo del tipo moneda
	public String getTotalImporteBruto_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalImporteBruto);
	}

	// Getter del atributo del tipo moneda
	public String getImporteCargo_formatFapTabla() {
		return format.FapFormat.formatMoneda(importeCargo);
	}

	// Getter del atributo del tipo moneda
	public String getPrecioUnidadSinImpuestos_formatFapTabla() {
		return format.FapFormat.formatMoneda(precioUnidadSinImpuestos);
	}

	public ItemsFactura() {
		init();
	}

	public void init() {

		if (descuentos == null)
			descuentos = new ArrayList<Descuentos>();

		postInit();
	}

	// === MANUAL REGION START ===

	public Double getTotalImporteDescuentos() {
		Double total = 0.0;
		for (int i = 0; i < descuentos.size(); i++) {
			total += descuentos.get(i).importeDescuento;
		}
		return total;
	}

	public static ItemsFactura getDataFromItemFacturas(InvoiceLineType invoiceLineType) {

		if (invoiceLineType != null) {
			ItemsFactura item = new ItemsFactura();
			item.descripcionItem = invoiceLineType.getItemDescription();
			item.cantidad = invoiceLineType.getQuantity();
			item.unidadMedida = invoiceLineType.getUnitOfMeasure();
			item.precioUnidadSinImpuestos = invoiceLineType.getUnitPriceWithoutTax();
			if (invoiceLineType.getDiscountsAndRebates() != null) {
				for (int i = 0; i < invoiceLineType.getDiscountsAndRebates().getDiscount().size(); i++) {
					Descuentos descuento = new Descuentos();
					descuento = Descuentos.getDataFromDescuentos(invoiceLineType.getDiscountsAndRebates().getDiscount().get(i));
					item.descuentos.add(descuento);
				}
			}
			//Solo nos interesa el importe total de cargos.
			double totalCargo = 0.0;
			if (invoiceLineType.getCharges() != null) {
				for (int i = 0; i < invoiceLineType.getCharges().getCharge().size(); i++) {
					totalCargo += invoiceLineType.getCharges().getCharge().get(i).getChargeAmount();
				}
			}
			item.importeCargo = totalCargo;
			item.totalImporteBruto = invoiceLineType.getGrossAmount();
			return item;
		}
		return null;
	}

	// === MANUAL REGION END ===

}
