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
import utils.AedUtils;
import utils.DocumentosUtils;
import properties.FapProperties;

// === IMPORT REGION END ===

@Entity
public class Documento extends FapModel {
	// Código de los atributos

	public String uri;

	@ValueFromTable("tiposDocumentos")
	public String tipo;

	public String descripcion;

	@Transient
	public String descripcionVisible;

	public Boolean clasificado;

	public String hash;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaSubida"), @Column(name = "fechaSubidaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaSubida;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaRegistro"), @Column(name = "fechaRegistroTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRegistro;

	@Transient
	public String urlDescarga;

	public Boolean verificado;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===
	public Documento() {
		clasificado = false;
	}

	public boolean isMultiple() {
		return (tipo != null && DocumentosUtils.esTipoMultiple(tipo));
	}

	public String getUrlDescarga() {
		return AedUtils.crearUrl(uri);
	}

	/**
	 * Transformamos la entidad Documento del gestor documental del Gobierno de Canarias en una entidad Documento de FAP.
	 * 
	 */
	public void docAed2Doc(es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento propiedadesDoc, String tipoDocumento) {
		uri = propiedadesDoc.getUri();
		tipo = tipoDocumento;
		descripcion = propiedadesDoc.getDescripcion();
	}

	public static Documento findByUri(String uri) {
		Documento documento = models.Documento.find("byUri", uri).first();
		return documento;
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
