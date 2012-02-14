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
public class Tramite extends Model {
	// Código de los atributos

	public String uri;

	public String nombre;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tramite_documentos")
	public List<TipoDocumento> documentos;

	public Tramite() {
		init();
	}

	public void init() {

		if (documentos == null)
			documentos = new ArrayList<TipoDocumento>();

	}

	// === MANUAL REGION START ===
	public static List<TipoDocumento> findTipoDocumentosFrom(String tramite) {
		List<TipoDocumento> tiposDocumentos = TipoDocumento
				.find("select tipoDocumento from Tramite tramite "
						+ "join tramite.documentos tipoDocumento where tramite.nombre=?",
						tramite).fetch();
		return tiposDocumentos;
	}

	public static List<TipoDocumento> findTipoDocumentosAportadosPor(
			String tramite, String aportadoPor) {
		List<TipoDocumento> tiposDocumentos = TipoDocumento
				.find("select tipoDocumento from Tramite tramite "
						+ "join tramite.documentos tipoDocumento where tramite.nombre=? "
						+ "and tipoDocumento.aportadoPor = ?", tramite,
						aportadoPor).fetch();
		return tiposDocumentos;
	}

	public static List<TipoDocumento> findTipoDocumentosAportadosPor(
			String aportadoPor) {
		List<TipoDocumento> tiposDocumentos = TipoDocumento
				.find("select tipoDocumento from Tramite tramite "
						+ "join tramite.documentos tipoDocumento where tipoDocumento.aportadoPor = ?",
						aportadoPor).fetch();
		return tiposDocumentos;
	}
	// === MANUAL REGION END ===

}
