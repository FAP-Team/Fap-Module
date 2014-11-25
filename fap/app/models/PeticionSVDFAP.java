package models;

import java.util.*;

import javax.persistence.*;

import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.data.validation.*;
import properties.FapProperties;

import org.joda.time.DateTime;

import controllers.fap.AgenteController;
import es.gobcan.platino.servicios.svd.peticiontipodocumentacion.TipoDocumentacion;
import models.*;
import messages.Messages;
import validation.*;
import audit.Auditable;

import java.text.ParseException;
import java.text.SimpleDateFormat;

// === IMPORT REGION START ===

// === IMPORT REGION END ===

@Entity
public class PeticionSVDFAP extends FapModel {
	// Código de los atributos

	public String uidUsuario;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public AtributosSVDFAP atributos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitudesSVDFAP solicitudes;

	@ValueFromTable("tipoEstadoPeticionSVDFAP")
	public String estadoPeticion;

	public PeticionSVDFAP() {
		init();
	}

	public void init() {

		if (atributos == null)
			atributos = new AtributosSVDFAP();
		else
			atributos.init();

		if (solicitudes == null)
			solicitudes = new SolicitudesSVDFAP();
		else
			solicitudes.init();

		postInit();
	}

	// === MANUAL REGION START ===
	
	
	public String getUidUsuario() {
		return uidUsuario;
	}

	public void setUidUsuario(String uidUsuario) {
		this.uidUsuario = uidUsuario;
	}

	public AtributosSVDFAP getAtributos() {
		return atributos;
	}

	public void setAtributos(AtributosSVDFAP atributos) {
		this.atributos = atributos;
	}

	public SolicitudesSVDFAP getSolicitudes() {
		return solicitudes;
	}

	public void setSolicitudes(SolicitudesSVDFAP solicitudes) {
		this.solicitudes = solicitudes;
	}

	public String getEstadoPeticion() {
		return estadoPeticion;
	}

	public void setEstadoPeticion(String estadoPeticion) {
		this.estadoPeticion = estadoPeticion;
	}
	
	public void rellenarSolicitud(List<SolicitudGenerica> solicitudes){
	    	
		for (SolicitudGenerica solicitud: solicitudes){
    		DatosGenericosPeticionSVDFAP datosGenericos = rellenarDatosGenericos(solicitud);
    		SolicitudTransmisionSVDFAP solicitudTransmision = new SolicitudTransmisionSVDFAP(solicitud, datosGenericos, null);
    		getSolicitudes().getSolicitudTransmision().add(solicitudTransmision);
	    }
	    	
	}
	
	public DatosGenericosPeticionSVDFAP rellenarDatosGenericos(SolicitudGenerica solicitud){
		
		FuncionarioSVDFAP funcionario = new FuncionarioSVDFAP(AgenteController.getAgente().username, AgenteController.getAgente().name);
		ProcedimientoSVDFAP procedimiento = new ProcedimientoSVDFAP("codigoProcedimiento", "nombreProcedimiento");
		
		SolicitanteSVDFAP solicitante =  new SolicitanteSVDFAP(FapProperties.get("identificadorSolicitante"), FapProperties.get("nombreCompleto"), solicitud.expedienteAed.idAed, 
				"SI", "finalidad", "unidadTramitadora", funcionario, procedimiento);
		
		TitularSVDFAP titular = new TitularSVDFAP("nombre del titular", "nombre completo del titular anterior", "primer apellido", "segundo apellido", 
				"nif del titular del expediente de la solicitudGenerica", "Tipo de documento (NIF, DNI, NIE, CIF)");
		
		DatosGenericosPeticionSVDFAP datosGenericos = new DatosGenericosPeticionSVDFAP(solicitante, titular);
		
		return datosGenericos;
	}
	
	public DatosEspecificosPeticionSVDFAP rellenarDatosEspecificos(SolicitudGenerica solicitud){
		SolicitanteDatosSVDFAP solicitanteDatos = new SolicitanteDatosSVDFAP("app/fun");

		MunicipioSVDFAP municipio = new MunicipioSVDFAP("codigo", "nombre");
		ProvinciaSVDFAP provincia = new ProvinciaSVDFAP("codigo", "nombre");
		
		ResidenciaSVDFAP residencia = new ResidenciaSVDFAP(municipio, provincia);
		NacimientoSVDFAP nacimiento = new NacimientoSVDFAP(new DateTime(), municipio, provincia);
		
		SolicitudSVDFAP solicitudEspecifica = new SolicitudSVDFAP(residencia, nacimiento, "espaniol");
		
		DatosEspecificosPeticionSVDFAP datosEspecificos = new DatosEspecificosPeticionSVDFAP(solicitanteDatos, solicitudEspecifica);
		
		return datosEspecificos;
	}

	// === MANUAL REGION END ===

}
