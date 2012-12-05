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

/*Entidad INSSR001{
 String cabeceraPrimera
 String cabeceraSegunda
 RegistroDatos registroDetalle
 }

 Entidad INSSA008{
 String cabeceraPrimera
 String cabeceraSegunda
 RegistroDatosA008 registroDetalle
 }

 Entidad RegistroDetalle{
 String tipoRegistro
 String regimen
 String ccc
 }

 Entidad RegistroAEAT{
 String nDocumento
 String nombre
 String ident
 String cert
 String negat
 String datosPropios
 String referencia
 }

 Entidad ATC{
 String nombre
 RegistroDatos registroDetalle
 }

 Entidad RegistroInssATC{
 String tipoRegistro
 String nDocumento
 String estado
 }

 Entidad RegistroINSSA008{
 String tipoRegistro
 String regimen
 String cccPpal
 String numMedioTrabajadores
 String nombre
 DateTime fechaSolicitud
 String estado
 }*/

@Entity
public class CesionPDF extends FapModel {
	// Código de los atributos

	@ValueFromTable("listaCesiones")
	public String tipo;

	public String cabeceraPrimera;

	public String cabeceraSegunda;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public RegistroCesion registro;

	public CesionPDF() {
		init();
	}

	public void init() {

		if (registro == null)
			registro = new RegistroCesion();
		else
			registro.init();

		postInit();
	}

	// === MANUAL REGION START ===

	// === MANUAL REGION END ===

}
