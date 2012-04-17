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
public class TipoCEconomico extends FapModel {
	// Código de los atributos

	public String nombre;

	public String descripcion;

	public String instrucciones;

	public String jerarquia;

	@ValueFromTable("LstClaseCEconomico")
	public String clase;

	public Boolean comentariosAdministracion;

	public Boolean comentariosSolicitante;

	public TipoCEconomico() {
		init();
	}

	public void init() {

		comentariosAdministracion = false;
		comentariosSolicitante = false;

		postInit();
	}

	// === MANUAL REGION START ===

	public TipoCEconomico(TipoCEconomico tipoCEconomico) {
		this.clase = tipoCEconomico.clase;
		this.comentariosAdministracion = tipoCEconomico.comentariosAdministracion;
		this.comentariosSolicitante = tipoCEconomico.comentariosSolicitante;
		this.descripcion = tipoCEconomico.clase;
		this.instrucciones = tipoCEconomico.instrucciones;
		this.jerarquia = tipoCEconomico.jerarquia;
		this.nombre = tipoCEconomico.nombre;
	}

	// === MANUAL REGION END ===

}
