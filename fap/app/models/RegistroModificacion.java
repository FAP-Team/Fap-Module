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
import enumerado.fap.gen.EstadosModificacionEnum;
// === IMPORT REGION END ===

@Entity
public class RegistroModificacion extends FapModel {
	// Código de los atributos

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaCreacion"), @Column(name = "fechaCreacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaCreacion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaLimite"), @Column(name = "fechaLimiteTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaLimite;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaCancelacion"), @Column(name = "fechaCancelacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaCancelacion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaRegistro"), @Column(name = "fechaRegistroTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRegistro;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Registro registro;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "registromodificacion_jsonpeticionesmodificacion")
	public List<JsonPeticionModificacion> jsonPeticionesModificacion;

	@Transient
	public String estado;

	public RegistroModificacion() {
		init();
	}

	public void init() {

		if (jsonPeticionesModificacion == null)
			jsonPeticionesModificacion = new ArrayList<JsonPeticionModificacion>();

		postInit();
	}

	// === MANUAL REGION START ===

	public String getEstado() {
		if (this.registro.fasesRegistro.registro)
			return EstadosModificacionEnum.registrada.value(); // Registrada correctamente (Presentada en tiempo y forma)
		else if ((this.fechaCancelacion == null) && (this.fechaRegistro == null) && (this.fechaLimite.isBeforeNow()))
			return EstadosModificacionEnum.expirada.value(); // Restaurada automáticamente tras pasarse la fecha límite y no ser presentada en tiempo y forma
		else if ((this.fechaCancelacion != null) && (this.fechaCancelacion.isBefore(this.fechaLimite)))
			return EstadosModificacionEnum.cancelada.value(); // Restaurada manualmente por un gestor o administrador antes de acabar la fecha límite
		else
			return "En Curso"; // Modificable actualmente
	}

	// === MANUAL REGION END ===

}
