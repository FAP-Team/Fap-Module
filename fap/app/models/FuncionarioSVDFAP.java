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
public class FuncionarioSVDFAP extends FapModel {
	// Código de los atributos

	public String nombreCompletoFuncionario;

	public String nifFuncionario;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===
	
	public FuncionarioSVDFAP(){
		init();
	}
	
	public FuncionarioSVDFAP(String nifFuncionario, String nombreCompleto){
		init();
		this.setNifFuncionario(nifFuncionario);
		this.setNombreCompletoFuncionario(nombreCompleto);
	}

	public String getNombreCompletoFuncionario() {
		return nombreCompletoFuncionario;
	}

	public void setNombreCompletoFuncionario(String nombreCompletoFuncionario) {
		this.nombreCompletoFuncionario = nombreCompletoFuncionario;
	}

	public String getNifFuncionario() {
		return nifFuncionario;
	}

	public void setNifFuncionario(String nifFuncionario) {
		this.nifFuncionario = nifFuncionario;
	}
	
	// === MANUAL REGION END ===

}
