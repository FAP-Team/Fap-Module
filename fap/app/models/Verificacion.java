package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.joda.time.DateTime;

import validation.ValueFromTable;

// === IMPORT REGION START ===

// === IMPORT REGION END ===

@Entity
public class Verificacion extends FapModel {
	// Código de los atributos

	public String uriVerificacion;

	public String uriProcedimiento;

	@ManyToOne
	@Transient
	public Tramite tramiteNombre;

	public String uriTramite;

	public String expediente;

	@ValueFromTable("estadosVerificacion")
	public String estado;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "verificacion_documentos")
	public List<VerificacionDocumento> documentos;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "verificacion_nuevosdocumentos")
	public List<Documento> nuevosDocumentos;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "verificacion_verificaciontiposdocumentos")
	public List<Documento> verificacionTiposDocumentos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Requerimiento requerimiento;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaCreacion"), @Column(name = "fechaCreacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaCreacion;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaUltimaActualizacion"), @Column(name = "fechaUltimaActualizacionTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaUltimaActualizacion;

	public String incluirFichMultiple;

	public Verificacion() {
		init();
	}

	@Override
	public void init() {

		if (tramiteNombre != null)
			tramiteNombre.init();

		if (documentos == null)
			documentos = new ArrayList<VerificacionDocumento>();

		if (nuevosDocumentos == null)
			nuevosDocumentos = new ArrayList<Documento>();

		if (verificacionTiposDocumentos == null)
			verificacionTiposDocumentos = new ArrayList<Documento>();

		if (requerimiento == null)
			requerimiento = new Requerimiento();
		else
			requerimiento.init();

		postInit();
	}

	// === MANUAL REGION START ===

	public Tramite getTramiteNombre() {
		if (uriTramite == null)
			return null;
		Tramite tramite = Tramite.find("select tramite from Tramite tramite where tramite.uri=?", uriTramite).first();
		return tramite;
	}

	public void setNullOneToMany() {
		play.Logger.error("Eliminando de la Verificación " + this.id + " la Lista Documentos: " + this.documentos.toString());
		this.documentos = null;
		play.Logger.error("Eliminando de la Verificación " + this.id + " la Lista Nuevos Documentos: " + this.nuevosDocumentos.toString());
		this.nuevosDocumentos = null;
		play.Logger.error("Eliminando de la Verificación " + this.id + " la Lista Verificacion Tipos Documentos: " + this.verificacionTiposDocumentos.toString());
		this.verificacionTiposDocumentos = null;
	}

	//Método que nos permite ordenar las verificaciones mediante la fecha de última actualización.
	public static Comparator<Verificacion> VerificacionComparator = new Comparator<Verificacion>() {
		@Override
		public int compare(Verificacion v1, Verificacion v2) {
			DateTime x = v1.fechaUltimaActualizacion;
			DateTime y = v2.fechaUltimaActualizacion;

			//De forma ascendente
			//return x.compareTo(y);

			//De forma descendente

			if (y == null)
				return (x == null) ? 0 : -1;

			if (x == null)
				return 1;

			return y.compareTo(x);
		}
	};

	// === MANUAL REGION END ===

}
