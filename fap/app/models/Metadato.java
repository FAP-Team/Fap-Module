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
public class Metadato extends FapModel {
	// Código de los atributos

	public String nombre;

	public String valor;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

    public Metadato(DefinicionMetadatos definicion, String valor, Documento documento) {
        this.documento = documento;
        this.definicion = definicion;
        this.nombre = definicion.nombre;
        this.valor = valor;
    }


    // === MANUAL REGION END ===

}
