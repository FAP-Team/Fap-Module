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

// === IMPORT REGION END ===

@Entity
public class FacturaDatos extends FapModel {
	// Código de los atributos

	public String numeroFactura;

	public String numeroSerie;

	public String fechaExpedicion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TotalesFactura totalesFactura;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "facturadatos_informaciondetallada")
	public List<ItemsFactura> informacionDetallada;

	public FacturaDatos() {
		init();
	}

	public void init() {

		if (totalesFactura == null)
			totalesFactura = new TotalesFactura();
		else
			totalesFactura.init();

		if (informacionDetallada == null)
			informacionDetallada = new ArrayList<ItemsFactura>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
