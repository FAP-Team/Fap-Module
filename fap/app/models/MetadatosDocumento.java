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
public class MetadatosDocumento extends FapModel {
	// Código de los atributos

	public String tipoDocumento;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "metadatosdocumento_listametadatos")
	public List<Metadato> listaMetadatos;

	public MetadatosDocumento() {
		init();
	}

	public void init() {

		if (tipoDocumento == null)
			tipoDocumento = "ALL";

		if (listaMetadatos == null)
			listaMetadatos = new ArrayList<Metadato>();

		postInit();
	}

	// === MANUAL REGION START ===
	public static void deleteAllMetadatos() {
		List<Metadatos> listaMetadatos = Metadatos.findAll();
		for (Metadatos metadatos : listaMetadatos) {
			metadatos.listaMetadatos = null;
			metadatos.delete();
		}
		Metadato.deleteAll();
	}
	// === MANUAL REGION END ===

}
