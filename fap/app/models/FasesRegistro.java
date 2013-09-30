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
import enumerado.fap.gen.*;

// === IMPORT REGION END ===

@Entity
public class FasesRegistro extends FapModel {
	// Código de los atributos

	public Boolean borrador;

	public Boolean firmada;

	public Boolean expedientePlatino;

	public Boolean registro;

	public Boolean expedienteAed;

	public Boolean clasificarAed;

	public String fase;

	@Transient
	public String firmadaVisible;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===
	public void setFase(FaseRegistroEnum f) {
		//play.Logger.info("setFase: "+f.toString());
		fase = f.toString();
	}

	public FaseRegistroEnum getFaseEnum() {
		//play.Logger.info("getValue ("+fase+")"+": "+FaseRegistroEnum.valueOf(fase));
		return FaseRegistroEnum.valueOf(fase);
	}

	public FasesRegistro() {
		setFase(FaseRegistroEnum.borrador);
		reiniciar();
	}

	public String getFirmadaVisible() {
		if ((this.firmada != null) && (this.firmada == true))
			return "Sí";
		return "No";
	}

	public void reiniciar() {
		setFase(FaseRegistroEnum.borrador);
		borrador = false;
		firmada = false;
		expedientePlatino = false;
		registro = false;
		expedienteAed = false;
		clasificarAed = false;
	}

	// === MANUAL REGION END ===

}
