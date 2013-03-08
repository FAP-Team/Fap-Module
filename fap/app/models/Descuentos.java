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
import es.mityc.facturae32.DiscountType;
import es.mityc.facturae32.DiscountsAndRebatesType;

// === IMPORT REGION END ===

@Entity
public class Descuentos extends FapModel {
	// Código de los atributos

	public String descripcionDescuento;

	@Moneda
	public Double porcentajeDescuento;

	@Moneda
	public Double importeDescuento;

	@Transient
	public String porcentajeDescuento_formatFapTabla;

	@Transient
	public String importeDescuento_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getPorcentajeDescuento_formatFapTabla() {
		return format.FapFormat.formatMoneda(porcentajeDescuento);
	}

	// Getter del atributo del tipo moneda
	public String getImporteDescuento_formatFapTabla() {
		return format.FapFormat.formatMoneda(importeDescuento);
	}

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	public static Descuentos getDataFromDescuentos(DiscountType discountType) {

		if (discountType != null) {
			Descuentos descuentos = new Descuentos();
			descuentos.descripcionDescuento = discountType.getDiscountReason();
			descuentos.porcentajeDescuento = discountType.getDiscountRate();
			descuentos.importeDescuento = discountType.getDiscountAmount();
			return descuentos;
		}
		return null;
	}

	// === MANUAL REGION END ===

}
