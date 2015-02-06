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
public class NacimientoSVDFAP extends FapModel {
	// Código de los atributos

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fecha"), @Column(name = "fechaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fecha;

	@ValueFromTable("comunidadesAutonomas")
	public String comunidadAutonoma;

	@ValueFromTable("provincias")
	public String provincia;

	@ValueFromTable("islas")
	public String isla;

	@ValueFromTable("municipios")
	public String municipio;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	public DateTime getFecha() {
		return fecha;
	}

	public void setFecha(DateTime fecha) {
		this.fecha = fecha;
	}

	public String getComunidadAutonoma() {
		return comunidadAutonoma;
	}

	public void setComunidadAutonoma(String comunidadAutonoma) {
		this.comunidadAutonoma = comunidadAutonoma;
	}

	public String getProvincia() {
		return provincia;
	}

	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}

	public String getIsla() {
		return isla;
	}

	public void setIsla(String isla) {
		this.isla = isla;
	}

	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	// === MANUAL REGION END ===

}
