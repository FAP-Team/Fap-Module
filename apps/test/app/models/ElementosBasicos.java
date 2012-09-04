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
public class ElementosBasicos extends FapModel {
	// Código de los atributos

	public String texto;

	@Column(columnDefinition = "LONGTEXT")
	public String areaDeTexto;

	public Boolean mBoolean;

	public Integer numeroI;

	public Long numeroL;

	public Double numeroD;

	public ElementosBasicos() {
		init();
	}

	public void init() {

		if (mBoolean == null)
			mBoolean = true;

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
