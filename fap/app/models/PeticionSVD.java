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
public class PeticionSVD extends FapModel {
	// Código de los atributos

	public String uidUsuario;

	public String codigoCertificado;

	public String idTransmision;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "peticionsvd_solicitudtransmision")
	public List<SolicitudTransmision> solicitudTransmision;

	public PeticionSVD() {
		init();
	}

	public void init() {

		if (solicitudTransmision == null)
			solicitudTransmision = new ArrayList<SolicitudTransmision>();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
