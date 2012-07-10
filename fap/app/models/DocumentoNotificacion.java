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
import utils.AedUtils;
import validation.*;
import audit.Auditable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// === IMPORT REGION START ===

// === IMPORT REGION END ===

@Entity
public class DocumentoNotificacion extends FapModel {
	// Código de los atributos

	public String uri;

	@Transient
	public String urlDescarga;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===
	
	public DocumentoNotificacion (String uri){
		init();
		this.uri = uri; 
	}
	
	public String getUrlDescarga() {
		return AedUtils.crearUrl(uri);
	}

	// === MANUAL REGION END ===

}
