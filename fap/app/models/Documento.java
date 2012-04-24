
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
	
	public boolean isOtros(){
		return (tipo != null && tipo.equals(FapProperties.get("fap.aed.tiposdocumentos.otros")));
	}
	
	/**
	 * Prepara un documento para subir al AED
	 */
	public void prepararParaSubir(){
		//Si no es de tipo otros, pone la despcrión igual al tipo
		//El AED da error con descripción null
		if(!isOtros() && ((descripcion == null) || (descripcion.trim().equals("")))){
			descripcion = TableKeyValue.getValue("tiposDocumentos", tipo);
		}
	}
	
	/**
	 * Actualiza la descripcion si el tipo de documentos no es Otro
	 * 
	*/
	
	public void actualizaDescripcion(){
		if(!isOtros()){
			descripcion = TableKeyValue.getValue("tiposDocumentos", tipo);
		}
	}
	
	public String getUrlDescarga(){
		return AedUtils.crearUrl(uri);
	}
	
	public String getTipoCiudadano() {
		return tipo;
	}

	public void setTipoCiudadano(String tipoCiudadano) {
		this.tipo = tipoCiudadano;
	}
	
// === MANUAL REGION END ===
	
	
	}
		