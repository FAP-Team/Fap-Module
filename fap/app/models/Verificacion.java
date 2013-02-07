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
public class Verificacion extends FapModel {
	// Código de los atributos

	public String uriVerificacion;

	public String uriProcedimiento;

	@ManyToOne
	@Transient
	public Tramite tramiteNombre;

	public String uriTramite;

	public String expediente;

	@ValueFromTable("estadosVerificacion")
	public String estado;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "verificacion_documentos")
	public List<VerificacionDocumento> documentos;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "verificacion_nuevosdocumentos")
	public List<Documento> nuevosDocumentos;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "verificacion_verificaciontiposdocumentos")
	public List<Documento> verificacionTiposDocumentos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Requerimiento requerimiento;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaCreacion"), @Column(name = "fechaCreacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaCreacion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaUltimaActualizacion"), @Column(name = "fechaUltimaActualizacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaUltimaActualizacion;

	public String incluirFichMultiple;

	public Verificacion() {
		init();
	}

	public void init() {

		if (tramiteNombre != null)
			tramiteNombre.init();

		if (documentos == null)
			documentos = new ArrayList<VerificacionDocumento>();

		if (nuevosDocumentos == null)
			nuevosDocumentos = new ArrayList<Documento>();

		if (verificacionTiposDocumentos == null)
			verificacionTiposDocumentos = new ArrayList<Documento>();

		if (requerimiento == null)
			requerimiento = new Requerimiento();
		else
			requerimiento.init();

		postInit();
	}

	// === MANUAL REGION START ===

	public Tramite getTramiteNombre() {
		if (uriTramite == null)
			return null;
		Tramite tramite = Tramite.find("select tramite from Tramite tramite where tramite.uri=?", uriTramite).first();
		return tramite;
	}

	public void setNullOneToMany() {
		play.Logger.error("Eliminando de la Verificación " + this.id + " la Lista Documentos: " + this.documentos.toString());
		this.documentos = null;
		play.Logger.error("Eliminando de la Verificación " + this.id + " la Lista Nuevos Documentos: " + this.nuevosDocumentos.toString());
		this.nuevosDocumentos = null;
		play.Logger.error("Eliminando de la Verificación " + this.id + " la Lista Verificacion Tipos Documentos: " + this.verificacionTiposDocumentos.toString());
		this.verificacionTiposDocumentos = null;
	}

	// === MANUAL REGION END ===

}
