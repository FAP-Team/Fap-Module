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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import format.FapFormat;

// === IMPORT REGION START ===

// === IMPORT REGION END ===

@Entity
public class CodigoExclusion extends FapModel {
	// Código de los atributos

	public String codigo;

	@OneToOne
	@Transient
	public TipoCodigoExclusion tipoCodigo;

	public CodigoExclusion() {
		init();
	}

	public void init() {

		if (tipoCodigo == null)
			tipoCodigo = new TipoCodigoExclusion();
		else
			tipoCodigo.init();

		postInit();
	}

	// === MANUAL REGION START ===

	public TipoCodigoExclusion getTipoCodigo() {
		TipoCodigoExclusion tipo = new TipoCodigoExclusion();
		if (this.codigo != null) {
			tipo = (TipoCodigoExclusion) TipoCodigoExclusion.find("select tipo from TipoCodigoExclusion tipo where tipo.codigo=?", this.codigo).first();
			return tipo;
		}
		return null;
	}

	// === MANUAL REGION END ===

}
