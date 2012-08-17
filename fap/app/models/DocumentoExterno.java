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
import utils.DocumentosUtils;

// === IMPORT REGION END ===

@Entity
public class DocumentoExterno extends FapModel {
	// Código de los atributos

	@ValueFromTable("tiposDocumentos")
	public String tipo;

	public String descripcion;

	@Transient
	public String descripcionVisible;

	public String organo;

	public String expediente;

	@Column(columnDefinition = "LONGTEXT")
	public String uri;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	public boolean isMultiple() {
		return (tipo != null && DocumentosUtils.esTipoMultiple(tipo));
	}

	public String getDescripcionVisible() {
		String descripcionDevolver = "";
		if ((this.descripcion != null) && !(this.descripcion.trim().equals("")))
			return this.descripcion;
		descripcionDevolver = TableKeyValue.getValue("tiposDocumentos", tipo);
		if ((descripcionDevolver == null) || (descripcionDevolver.trim().equals(""))) {
			play.Logger.error("La descripción no se pudo obtener a partir del tipo: " + tipo);
			return this.descripcion;
		}
		return descripcionDevolver;
	}

	// === MANUAL REGION END ===

}
