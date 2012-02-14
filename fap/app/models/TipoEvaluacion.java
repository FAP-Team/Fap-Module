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
public class TipoEvaluacion extends Model {
	// Código de los atributos

	public String nombre;

	@ElementCollection
	public List<String> tiposDocumentos;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tipoevaluacion_criterios")
	public List<TipoCriterio> criterios;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tipoevaluacion_ceconomicos")
	public List<TipoCEconomico> ceconomicos;

	public Boolean comentariosAdministracion;

	public Boolean comentariosSolicitante;

	public String tipoProcedimiento;

	public Long numeroEvaluacion;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tipoevaluacion_datosadicionales")
	public List<TipoDatoAdicional> datosAdicionales;

	public TipoEvaluacion() {
		init();
	}

	public void init() {

		if (criterios == null)
			criterios = new ArrayList<TipoCriterio>();

		if (ceconomicos == null)
			ceconomicos = new ArrayList<TipoCEconomico>();
		comentariosAdministracion = false;
		comentariosSolicitante = false;

		if (datosAdicionales == null)
			datosAdicionales = new ArrayList<TipoDatoAdicional>();

	}

	// === MANUAL REGION START ===
	public List<TipoDatoAdicional> getSortedDatosAdicionales() {
		List<TipoDatoAdicional> sortedDatosAdicionales = TipoDatoAdicional
				.find("select tda from TipoEvaluacion te join te.datosAdicionales tda where te.id=? order by tda.orden",
						this.id).fetch();
		return sortedDatosAdicionales;
	}
	// === MANUAL REGION END ===

}
