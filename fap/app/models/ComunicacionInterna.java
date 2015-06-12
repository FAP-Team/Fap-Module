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
public class ComunicacionInterna extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public AsientoCIFap asiento;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public RespuestaCIFap respuesta;

	@ValueFromTable("estadosComunicacionInterna")
	@FapEnum("enumerado.fap.gen.EstadosComunicacionInternaEnum")
	public String estado;

	@Transient
	public String numRegistroHiperReg;

	@Transient
	public String fechaHiperReg;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	public String getNumRegistroHiperReg() {
		if (respuesta != null && respuesta.contadorUO != null && respuesta.numeroRegistro != null)
			return respuesta.contadorUO + "/" + respuesta.numeroRegistro;

		return "";
	}

	public String getFechaHiperReg() {
		if (respuesta != null && respuesta.fecha != null && respuesta.hora != null)
			return respuesta.fecha + " " + respuesta.hora;

		return "";
	}

	// === MANUAL REGION END ===

}
