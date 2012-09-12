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
public class ServiciosWebAplicacion extends FapModel {
	// Código de los atributos

	public String nombre;

	public String urlWS;

	public Boolean activo;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "servicioswebaplicacion_serviciowebinfo")
	public List<InfoWS> servicioWebInfo;

	public ServiciosWebAplicacion() {
		init();
	}

	public void init() {

		if (activo == null)
			activo = true;

		if (servicioWebInfo == null)
			servicioWebInfo = new ArrayList<InfoWS>();

		postInit();
	}

	// === MANUAL REGION START ===
	@Override
	public String toString() {
		return "Info [nombre=" + nombre + ", urlWS=" + urlWS + ", activo=" + activo + ", lista=" + servicioWebInfo + "]";
	}

	//	public String getActivo() {
	//		if (activo)
	//			return "Sí";
	//		return "No";
	//	}
	// === MANUAL REGION END ===

}
