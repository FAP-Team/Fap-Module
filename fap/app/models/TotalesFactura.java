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
import es.mityc.facturae32.InvoiceTotalsType;

// === IMPORT REGION END ===

@Entity
public class TotalesFactura extends FapModel {
	// Código de los atributos

	@Moneda
	public Double totalImporteBruto;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "totalesfactura_descuentos")
	public List<Descuentos> descuentos;

	@Moneda
	public Double totalImporteBrutoAntesDeImpuestos;

	@Moneda
	public Double totalImpuestosRepercutidos;

	@Moneda
	public Double totalImpuestosRetenidos;

	@Moneda
	public Double totalFactura;

	@Transient
	public String totalImporteBruto_formatFapTabla;

	@Transient
	public String totalImporteBrutoAntesDeImpuestos_formatFapTabla;

	@Transient
	public String totalImpuestosRepercutidos_formatFapTabla;

	@Transient
	public String totalImpuestosRetenidos_formatFapTabla;

	@Transient
	public String totalFactura_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getTotalImporteBruto_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalImporteBruto);
	}

	// Getter del atributo del tipo moneda
	public String getTotalImporteBrutoAntesDeImpuestos_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalImporteBrutoAntesDeImpuestos);
	}

	// Getter del atributo del tipo moneda
	public String getTotalImpuestosRepercutidos_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalImpuestosRepercutidos);
	}

	// Getter del atributo del tipo moneda
	public String getTotalImpuestosRetenidos_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalImpuestosRetenidos);
	}

	// Getter del atributo del tipo moneda
	public String getTotalFactura_formatFapTabla() {
		return format.FapFormat.formatMoneda(totalFactura);
	}

	public TotalesFactura() {
		init();
	}

	public void init() {

		if (descuentos == null)
			descuentos = new ArrayList<Descuentos>();

		postInit();
	}

	// === MANUAL REGION START ===

	public static TotalesFactura getDataFromTotalesFactura(InvoiceTotalsType invoiceTotalsType) {

		if (invoiceTotalsType != null) {
			TotalesFactura totales = new TotalesFactura();
			totales.totalImporteBruto = invoiceTotalsType.getTotalGrossAmount();
			if (invoiceTotalsType.getGeneralDiscounts() != null) {
				for (int i = 0; i < invoiceTotalsType.getGeneralDiscounts().getDiscount().size(); i++) {
					Descuentos descuento = new Descuentos();
					descuento = Descuentos.getDataFromDescuentos(invoiceTotalsType.getGeneralDiscounts().getDiscount().get(i));
					totales.descuentos.add(descuento);
				}
			}
			totales.totalImporteBrutoAntesDeImpuestos = invoiceTotalsType.getTotalGrossAmountBeforeTaxes();
			totales.totalImpuestosRepercutidos = invoiceTotalsType.getTotalTaxOutputs();
			totales.totalImpuestosRetenidos = invoiceTotalsType.getTotalTaxesWithheld();
			totales.totalFactura = invoiceTotalsType.getInvoiceTotal();
			return totales;
		}
		return null;
	}

	// === MANUAL REGION END ===

}
