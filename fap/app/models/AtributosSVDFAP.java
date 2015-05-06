package models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

// === IMPORT REGION START ===

// === IMPORT REGION END ===

@Entity
public class AtributosSVDFAP extends FapModel {
	// Código de los atributos

	public String idPeticion;

	public String codigoCertificado;

	public String timestamp;

	public Integer numElementos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public EstadoRespuestaSVDFAP estado;

	public AtributosSVDFAP() {
		init();
	}

	@Override
	public void init() {

		if (estado == null)
			estado = new EstadoRespuestaSVDFAP();
		else
			estado.init();

		postInit();
	}

	// === MANUAL REGION START ===

	public String getCodigoCertificado() {
		return codigoCertificado;
	}

	public void setCodigoCertificado(String codigoCertificado) {
		this.codigoCertificado = codigoCertificado;
	}

	public String getIdPeticion() {
		return idPeticion;
	}

	public void setIdPeticion(String idPeticion) {
		this.idPeticion = idPeticion;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getNumElementos() {
		return numElementos;
	}

	public void setNumElementos(Integer numElementos) {
		this.numElementos = numElementos;
	}



	// === MANUAL REGION END ===

}
