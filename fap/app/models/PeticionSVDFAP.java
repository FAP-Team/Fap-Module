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
	public String estadoPeticion;

	@ValueFromTable("NombreServicioSVDFAP")
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

	public void rellenarSolicitud(List<SolicitudGenerica> solicitudes) {

		for (SolicitudGenerica solicitud : solicitudes) {
			DatosGenericosPeticionSVDFAP datosGenericos = rellenarDatosGenericos(solicitud);
			SolicitudTransmisionSVDFAP solicitudTransmision = new SolicitudTransmisionSVDFAP(solicitud, datosGenericos, null);
			this.solicitudesTransmision.add(solicitudTransmision);
		}

	}

	public DatosGenericosPeticionSVDFAP rellenarDatosGenericos(SolicitudGenerica solicitud) {
		return null;

		//		FuncionarioSVDFAP funcionario = new FuncionarioSVDFAP(AgenteController.getAgente().username, AgenteController.getAgente().name);
		//		ProcedimientoSVDFAP procedimiento = new ProcedimientoSVDFAP("codigoProcedimiento", "nombreProcedimiento");
		//
		//		SolicitanteSVDFAP solicitante = new SolicitanteSVDFAP(FapProperties.get("identificadorSolicitante"), FapProperties.get("nombreCompleto"), solicitud.expedienteAed.idAed, "SI", "finalidad", "unidadTramitadora", funcionario, procedimiento);
		//
		//		TitularSVDFAP titular = new TitularSVDFAP("nombre del titular", "nombre completo del titular anterior", "primer apellido", "segundo apellido", "nif del titular del expediente de la solicitudGenerica", "Tipo de documento (NIF, DNI, NIE, CIF)");
		//
		//		DatosGenericosPeticionSVDFAP datosGenericos = new DatosGenericosPeticionSVDFAP(solicitante, titular);
		//
		//		return datosGenericos;
	}

	public DatosEspecificosPeticionSVDFAP rellenarDatosEspecificos(SolicitudGenerica solicitud) {
		return null;
		//		SolicitanteDatosSVDFAP solicitanteDatos = new SolicitanteDatosSVDFAP("app/fun");
		//
		//		MunicipioSVDFAP municipio = new MunicipioSVDFAP("codigo", "nombre");
		//		ProvinciaSVDFAP provincia = new ProvinciaSVDFAP("codigo", "nombre");
		//
		//		ResidenciaSVDFAP residencia = new ResidenciaSVDFAP(municipio, provincia);
		//		NacimientoSVDFAP nacimiento = new NacimientoSVDFAP(new DateTime(), municipio, provincia);
		//
		//		SolicitudSVDFAP solicitudEspecifica = new SolicitudSVDFAP(residencia, nacimiento, "espaniol");
		//
		//		DatosEspecificosPeticionSVDFAP datosEspecificos = new DatosEspecificosPeticionSVDFAP(solicitanteDatos, solicitudEspecifica);
		//
		//		return datosEspecificos;
	}

	// === MANUAL REGION END ===

}
