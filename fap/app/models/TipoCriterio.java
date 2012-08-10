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
public class TipoCriterio extends FapModel {
	// Código de los atributos

	public String nombre;

	@Column(columnDefinition = "LONGTEXT")
	public String descripcion;

	@Column(columnDefinition = "LONGTEXT")
	public String instrucciones;

	@ValueFromTable("LstClaseCriterio")
	@Required
	public String claseCriterio;

	@Required
	public String jerarquia;

	public Integer valorPrecision;

	public Double valorMaximo;

	public Double valorMinimo;

	public Boolean esNuevo;

	@ValueFromTable("LstTipoValorCriterio")
	@Required
	public String tipoValor;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tipocriterio_listavalores")
	public List<CriterioListaValores> listaValores;

	public boolean mostrarValor;

	public Boolean transparencia;

	public Boolean comentariosAdministracion;

	public Boolean comentariosSolicitante;

	public TipoCriterio() {
		init();
	}

	public void init() {

		esNuevo = false;

		if (listaValores == null)
			listaValores = new ArrayList<CriterioListaValores>();
		comentariosAdministracion = false;
		comentariosSolicitante = false;

		postInit();
	}

	// === MANUAL REGION START ===

	public boolean esIgual(TipoCriterio tipoCriterio) {
		if (this.jerarquia != null && this.jerarquia.equals(tipoCriterio.jerarquia))
			return true;
		return false;
	}

	public void actualizar(TipoCriterio tipoCriterio) {
		this.claseCriterio = tipoCriterio.claseCriterio;
		this.comentariosAdministracion = tipoCriterio.comentariosAdministracion;
		this.comentariosSolicitante = tipoCriterio.comentariosSolicitante;
		this.descripcion = tipoCriterio.descripcion;
		this.instrucciones = tipoCriterio.instrucciones;
		this.jerarquia = tipoCriterio.jerarquia;
		this.listaValores.clear();
		this.listaValores.addAll(tipoCriterio.listaValores);
		this.mostrarValor = tipoCriterio.mostrarValor;
		this.nombre = tipoCriterio.nombre;
		this.tipoValor = tipoCriterio.tipoValor;
		this.transparencia = tipoCriterio.transparencia;
		this.valorMaximo = tipoCriterio.valorMaximo;
		this.valorMinimo = tipoCriterio.valorMinimo;
		this.valorPrecision = tipoCriterio.valorPrecision;
	}

	public int estoyContenido(List<TipoCriterio> lista) {
		for (TipoCriterio busqueda : lista) {
			if (this.esIgual(busqueda))
				return lista.indexOf(busqueda);
		}
		return -1;
	}

	// === MANUAL REGION END ===

}
