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

	@ValueFromTable("tiposDocumentos")
	public String tipo;

	public String descripcion;

	public Boolean clasificado;

	public String hash;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaSubida"), @Column(name = "fechaSubidaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaSubida;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaRegistro"), @Column(name = "fechaRegistroTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRegistro;

	@Transient
	public String urlDescarga;

	public void init() {

	}

	// === MANUAL REGION START ===
	public Documento() {
		clasificado = false;
	}

	public boolean isOtros() {
		return (tipo != null && tipo.equals(FapProperties.get("fap.aed.tiposdocumentos.otros")));
	}

	/**
	 * Prepara un documento para subir al AED
	 * 
	 * - Los documentos para subir al aed deben tener tipo y descripción. Si el 
	 *   documento no tiene descripción y no es de tipo otros, la asigna según
	 *   el tipo de documento.
	 *   
	 */
	public void prepararParaSubir() {
		// Si no tiene descripción y no es de tipo otros, pone como tipo
		// el nombre del tipo de documento
		if ((descripcion == null || descripcion.isEmpty()) && !isOtros()) {
			descripcion = TableKeyValue.getValue("tiposDocumentos", tipo);
		}
	}

	/**
	 * Actualiza la descripcion si el tipo de documentos no es Otro
	 * 
	 */

	public void actualizaDescripcion() {
		if (!isOtros()) {
			descripcion = TableKeyValue.getValue("tiposDocumentos", tipo);
		}
	}

	public String getUrlDescarga() {
		return AedUtils.crearUrl(uri);
	}

	public static Documento findByUri(String uri) {
		Documento documento = models.Documento.find("byUri", uri).first();
		return documento;
	}

	// === MANUAL REGION END ===

}
