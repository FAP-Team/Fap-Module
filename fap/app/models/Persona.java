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

@Auditable
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Persona extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public PersonaFisica fisica;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public PersonaJuridica juridica;

	@ValueFromTable("TipoDePersona")
	public String tipo;

	@Transient
	public String numeroId;

	@Transient
	public String nombreCompleto;

	@Transient
	public String tipoDeDocumento;

	public Persona() {
		init();
	}

	public void init() {

		if (fisica == null)
			fisica = new PersonaFisica();
		else
			fisica.init();

		if (juridica == null)
			juridica = new PersonaJuridica();
		else
			juridica.init();

		postInit();
	}

	// === MANUAL REGION START ===
	public boolean isPersonaFisica() {
		return tipo != null && tipo.equals("fisica");
	}

	public boolean isPersonaJuridica() {
		return tipo != null && tipo.equals("juridica");
	}

	public String getNombreCompleto() {
		if (isPersonaFisica())
			return fisica.getNombreCompleto();
		else if (isPersonaJuridica())
			return juridica.entidad;
		return null;
	}

	public String getNumeroId() {
		if ((isPersonaFisica()) && (fisica.nip != null))
			return fisica.nip.valor;
		else if (isPersonaJuridica())
			return juridica.cif;
		return null;
	}

	public String getTipoDocumento() {
		if ((isPersonaFisica()) && (fisica.nip != null)) {
			return fisica.nip.tipo;
		} else if (isPersonaJuridica()) {
			return "cif";
		}
		return null;
	}

	public static Persona createPersonaFisica() {
		Persona p = new Persona();
		p.tipo = "fisica";
		return p;
	}

	public static Persona createPersonaJuridica() {
		Persona p = new Persona();
		p.tipo = "juridica";
		return p;
	}

	// === MANUAL REGION END ===

}
