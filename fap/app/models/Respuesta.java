package models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

// === IMPORT REGION START ===

// === IMPORT REGION END ===

@Entity
public class Respuesta extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public AtributosRespuesta atributos;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public TransmisionesRespuesta transmisiones;

	public Respuesta() {
		init();
	}

	@Override
	public void init() {

		if (atributos == null)
			atributos = new AtributosRespuesta();
		else
			atributos.init();

		if (transmisiones == null)
			transmisiones = new TransmisionesRespuesta();
		else
			transmisiones.init();

		postInit();
	}

	// === MANUAL REGION START ===



	// === MANUAL REGION END ===

}
