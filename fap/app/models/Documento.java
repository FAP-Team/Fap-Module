
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
import utils.AedUtils;			
import utils.DocumentosUtils;
import properties.FapProperties;
// === IMPORT REGION END ===
	


@Entity
public class Documento extends Model {
	// Código de los atributos
	
	
	
	public String uri;
	
	
	
	@ValueFromTable("tipoDocumentosCiudadanos")
	@Transient
	public String tipoCiudadano;
	
	
	
	@ValueFromTable("tipoDocumentosOrganismos")
	@Transient
	public String tipoOrganismo;
	
	
	
	@ValueFromTable("tipoDocumentosOtrasEntidades")
	@Transient
	public String tipoOtraEntidad;
	
	
	
	@ValueFromTable("tipoDocumentos")
	public String tipo;
	
	
	
	
	public String descripcion;
	
	
	
	@Transient
	public String descripcionVisible;
	
	
	
	
	public Boolean clasificado;
	
	
	
	
	public String hash;
	
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaSubida"),@Column(name="fechaSubidaTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaSubida;
	
	
	
	@org.hibernate.annotations.Columns(columns={@Column(name="fechaRegistro"),@Column(name="fechaRegistroTZ")})
	@org.hibernate.annotations.Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRegistro;
	
	
	
	@Transient
	public String urlDescarga;
	
	
	
	
	public Boolean verificado;
	
	

	public void init(){
		
		
	}
		
	

// === MANUAL REGION START ===
	public Documento(){
		clasificado = false;
	}
	
	public String getUrlDescarga(){
		return AedUtils.crearUrl(uri);
	}
	
	public String getTipoCiudadano() {
		return tipo;
	}
	
	public boolean isMultiple() {
		return (tipo != null && DocumentosUtils.esTipoMultiple(tipo));
	}

	public void setTipoCiudadano(String tipoCiudadano) {
		this.tipo = tipoCiudadano;
	}
	
	public String getDescripcionVisible() {
		String descripcionDevolver = "";
		if ((this.descripcion != null) && !(this.descripcion.trim().equals("")))
			return this.descripcion;
		descripcionDevolver = TableKeyValue.getValue("tiposDocumentos", tipo);
		if ((descripcionDevolver == null) || (descripcionDevolver.trim().equals("")))
			play.Logger.error("La descripción no se pudo obtener a partir del tipo: " + tipo);
		return descripcionDevolver;
	}
	
// === MANUAL REGION END ===
	
	
	}
		