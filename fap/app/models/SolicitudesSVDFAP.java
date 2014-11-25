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
public class SolicitudesSVDFAP extends FapModel {
	// Código de los atributos

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "solicitudessvdfap_solicitudtransmision")
	public List<SolicitudTransmisionSVDFAP> solicitudTransmision;

	public SolicitudesSVDFAP() {
		init();
	}

	public void init() {

		if (solicitudTransmision == null)
			solicitudTransmision = new ArrayList<SolicitudTransmisionSVDFAP>();

		postInit();
	}

	// === MANUAL REGION START ===
	
	public List<SolicitudTransmisionSVDFAP> getSolicitudTransmision() {
		return solicitudTransmision;
	}

	public void setSolicitudTransmision(List<SolicitudTransmisionSVDFAP> solicitudTransmision) {
		this.solicitudTransmision = solicitudTransmision;
	}

	// === MANUAL REGION END ===

}
