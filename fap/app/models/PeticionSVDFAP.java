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

/***** Peticion al Servicio ******/

@Entity
public class PeticionSVDFAP extends FapModel {
	// Código de los atributos

	public String uidUsuario;

	public String nifFuncionario;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public AtributosSVDFAP atributos;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "peticionsvdfap_solicitudestransmision")
	public List<SolicitudTransmisionSVDFAP> solicitudesTransmision;

	@ValueFromTable("tipoEstadoPeticionSVDFAP")
	@FapEnum("enumerado.fap.gen.TipoEstadoPeticionSVDFAPEnum")
	public String estadoPeticion;

	@ValueFromTable("NombreServicioSVDFAP")
	@FapEnum("enumerado.fap.gen.NombreServicioSVDFAPEnum")
	public String nombreServicio;

	public PeticionSVDFAP() {
		init();
	}

	public void init() {

		if (atributos == null)
			atributos = new AtributosSVDFAP();
		else
			atributos.init();

		if (solicitudesTransmision == null)
			solicitudesTransmision = new ArrayList<SolicitudTransmisionSVDFAP>();

		postInit();
	}

	// === MANUAL REGION START ===

	public AtributosSVDFAP getAtributos() {
		return atributos;
	}

	public void setAtributos(AtributosSVDFAP atributos) {
		this.atributos = atributos;
	}

	public String getUidUsuario() {
		return uidUsuario;
	}

	public void setUidUsuario(String uidUsuario) {
		this.uidUsuario = uidUsuario;
	}

	public String getNifFuncionario() {
		return nifFuncionario;
	}

	public void setNifFuncionario(String nifFuncionario) {
		this.nifFuncionario = nifFuncionario;
	}

	// === MANUAL REGION END ===

}
