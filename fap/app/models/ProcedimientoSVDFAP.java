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
public class ProcedimientoSVDFAP extends FapModel {
	// Código de los atributos

	public String codigoProcedimiento;

	public String nombreProcedimiento;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===
	
	public ProcedimientoSVDFAP(){
		init();
	}
	
	public ProcedimientoSVDFAP(String codigoProcedimiento, String nombreProcedimiento){
		init();
		this.codigoProcedimiento = codigoProcedimiento;
		this.nombreProcedimiento = nombreProcedimiento;
	}

	public String getCodigoProcedimiento() {
		return codigoProcedimiento;
	}

	public void setCodigoProcedimiento(String codigoProcedimiento) {
		this.codigoProcedimiento = codigoProcedimiento;
	}

	public String getNombreProcedimiento() {
		return nombreProcedimiento;
	}

	public void setNombreProcedimiento(String nombreProcedimiento) {
		this.nombreProcedimiento = nombreProcedimiento;
	}
	
	// === MANUAL REGION END ===

}
