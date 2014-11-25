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
public class SolicitanteDatosSVDFAP extends FapModel {
	// Código de los atributos

	public String tipo;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===
	
	public SolicitanteDatosSVDFAP(){
		init();
	}
	
	public SolicitanteDatosSVDFAP(String tipo){
		init();
		this.tipo = tipo;
	}
	
	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	// === MANUAL REGION END ===

}
