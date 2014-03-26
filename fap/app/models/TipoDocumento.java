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
public class TipoDocumento extends FapModel {
	// Código de los atributos

	public String uri;

	public String nombre;

	public String aportadoPor;

	public String obligatoriedad;

	public String tramitePertenece;

	public String cardinalidad;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tipodocumento_definicionmetadatos")
	public List<DefinicionMetadatos> definicionMetadatos;

	public TipoDocumento() {
		init();
	}

	public void init() {

		if (definicionMetadatos == null)
			definicionMetadatos = new ArrayList<DefinicionMetadatos>();

		postInit();
	}

	// === MANUAL REGION START ===

	public static List<TipoDocumento> findTiposFacturas() {
		String tipos = properties.FapProperties.get("fap.gestordocumental.tiposfacturas.url");
		String[] lista = tipos.split(", ");
		List<TipoDocumento> listaTipos = new ArrayList<TipoDocumento>();
		boolean typeNotExist = false;
		for (int i = 0; i < lista.length; i++) {
			Long idTipo = TipoDocumento.find("select id from TipoDocumento where uri=?", lista[i]).first();
			if (idTipo == null) {
				typeNotExist = true;
				continue;
			}
			TipoDocumento tipo = TipoDocumento.findById(idTipo);
			listaTipos.add(tipo);
		}
		if (typeNotExist)
			play.Logger.warn("Alguno de los tipos de factura no existe en los tipos de documentos. Tipo facturas: " + tipos);
		return listaTipos;
	}

	// === MANUAL REGION END ===

}
