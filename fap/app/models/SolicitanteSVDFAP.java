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
public class SolicitanteSVDFAP extends FapModel {
	// Código de los atributos

	public String identificadorSolicitante;

	public String nombreSolicitante;

	public String finalidad;

	public String idExpediente;

	public String unidadTramitadora;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public ProcedimientoSVDFAP procedimiento;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public FuncionarioSVDFAP funcionario;

	@ValueFromTable("TipoConsentimiento")
	public String consentimiento;

	public SolicitanteSVDFAP() {
		init();
	}

	public void init() {

		if (procedimiento == null)
			procedimiento = new ProcedimientoSVDFAP();
		else
			procedimiento.init();

		if (funcionario == null)
			funcionario = new FuncionarioSVDFAP();
		else
			funcionario.init();

		postInit();
	}

	// === MANUAL REGION START ===

	public SolicitanteSVDFAP(String identificadorSolicitante, String nombreSolicitante, String idExpediente, String consentimiento, String finalidad, String unidadTramitadora, FuncionarioSVDFAP funcionario, ProcedimientoSVDFAP procedimiento) {

		init();
		this.setIdentificadorSolicitante(identificadorSolicitante);
		this.setNombreSolicitante(nombreSolicitante);
		this.setIdExpediente(idExpediente);
		this.setConsentimiento(consentimiento);
		this.setFinalidad(finalidad);
		this.setUnidadTramitadora(unidadTramitadora);
		this.setFuncionario(funcionario);
		this.setProcedimiento(procedimiento);
	}

	public String getIdentificadorSolicitante() {
		return identificadorSolicitante;
	}

	public void setIdentificadorSolicitante(String identificadorSolicitante) {
		this.identificadorSolicitante = identificadorSolicitante;
	}

	public String getNombreSolicitante() {
		return nombreSolicitante;
	}

	public void setNombreSolicitante(String nombreSolicitante) {
		this.nombreSolicitante = nombreSolicitante;
	}

	public String getFinalidad() {
		return finalidad;
	}

	public void setFinalidad(String finalidad) {
		this.finalidad = finalidad;
	}

	public String getIdExpediente() {
		return idExpediente;
	}

	public void setIdExpediente(String idExpediente) {
		this.idExpediente = idExpediente;
	}

	public String getUnidadTramitadora() {
		return unidadTramitadora;
	}

	public void setUnidadTramitadora(String unidadTramitadora) {
		this.unidadTramitadora = unidadTramitadora;
	}

	public ProcedimientoSVDFAP getProcedimiento() {
		return procedimiento;
	}

	public void setProcedimiento(ProcedimientoSVDFAP procedimiento) {
		this.procedimiento = procedimiento;
	}

	public FuncionarioSVDFAP getFuncionario() {
		return funcionario;
	}

	public void setFuncionario(FuncionarioSVDFAP funcionario) {
		this.funcionario = funcionario;
	}

	public String getConsentimiento() {
		return consentimiento;
	}

	public void setConsentimiento(String consentimiento) {
		this.consentimiento = consentimiento;
	}

	// === MANUAL REGION END ===

}
