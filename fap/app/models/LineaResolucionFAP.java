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
public class LineaResolucionFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitudGenerica solicitud;

	@ValueFromTable("estadoLineaResolucion")
	public String estado;

	public Double puntuacionBaremacion;

	@Moneda
	public Double importeTotal;

	@Moneda
	public Double importeConcedido;

	@Transient
	public String importeTotal_formatFapTabla;

	@Transient
	public String importeConcedido_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getImporteTotal_formatFapTabla() {
		return format.FapFormat.formatMoneda(importeTotal);
	}

	// Getter del atributo del tipo moneda
	public String getImporteConcedido_formatFapTabla() {
		return format.FapFormat.formatMoneda(importeConcedido);
	}

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	public static List<String> getOrdenEstados() {
		List<LineaResolucionFAP> listalineasResolucion = LineaResolucionFAP.findAll();
		List<String> result = new ArrayList<String>();
		for (LineaResolucionFAP linea : listalineasResolucion) {
			if (!result.contains(linea.estado)) { //Añadir nuevo estado a lista
				result.add(linea.estado);
			}
		}
		return result;
	}
	// === MANUAL REGION END ===

}
