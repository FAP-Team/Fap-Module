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
public class ResultadoPeticion extends FapModel {
	// Código de los atributos

	public String nombre;

	public Integer valorInteger;

	public Long valorLong;

	public String valorString;

	public Double valorDouble;

	public String valorDateTime;

	public Boolean valorBoolean;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	public ResultadoPeticion(String nombre, Integer integer) {
		this.nombre = nombre;
		this.valorInteger = integer;
		this.valorLong = null;
		this.valorBoolean = null;
		this.valorDateTime = null;
		this.valorDouble = null;
		this.valorString = null;
	}

	public ResultadoPeticion(String nombre, Long _long) {
		this.nombre = nombre;
		this.valorInteger = null;
		this.valorLong = _long;
		this.valorBoolean = null;
		this.valorDateTime = null;
		this.valorDouble = null;
		this.valorString = null;
	}

	public ResultadoPeticion(String nombre, Boolean bool) {
		this.nombre = nombre;
		this.valorInteger = null;
		this.valorLong = null;
		this.valorBoolean = bool;
		this.valorDateTime = null;
		this.valorDouble = null;
		this.valorString = null;
	}

	public ResultadoPeticion(String nombre, DateTime datetime) {
		this.nombre = nombre;
		this.valorInteger = null;
		this.valorLong = null;
		this.valorBoolean = null;
		if (datetime != null)
			this.valorDateTime = datetime.toString();
		else
			this.valorDateTime = null;
		this.valorDouble = null;
		this.valorString = null;
	}

	public ResultadoPeticion(String nombre, Double doubl) {
		this.nombre = nombre;
		this.valorInteger = null;
		this.valorLong = null;
		this.valorBoolean = null;
		this.valorDateTime = null;
		this.valorDouble = doubl;
		this.valorString = null;
	}

	public ResultadoPeticion(String nombre, String string) {
		this.nombre = nombre;
		this.valorInteger = null;
		this.valorLong = null;
		this.valorBoolean = null;
		this.valorDateTime = null;
		this.valorDouble = null;
		this.valorString = string;
	}

	public Object getType() {
		if (this.valorBoolean != null)
			return "Boolean";
		else if (this.valorDateTime != null)
			return "DateTime";
		else if (this.valorDouble != null)
			return "Double";
		else if (this.valorInteger != null)
			return "Integer";
		else if (this.valorLong != null)
			return "Long";
		else if (this.valorString != null)
			return "String";

		return null;
	}
	// === MANUAL REGION END ===

}
