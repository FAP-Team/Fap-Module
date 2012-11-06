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
public class Solicitud extends SolicitudGenerica {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SavePages savePages;

	public Solicitud() {
		init();
	}

	public void init() {
		super.init();

		if (savePages == null)
			savePages = new SavePages();
		else
			savePages.init();

		postInit();
	}

	public void savePagesPrepared() {
		if ((savePages.paginaSolicitante == null) || (!savePages.paginaSolicitante))
			Messages.error("La página paginaSolicitante no fue guardada correctamente");

		if ((savePages.paginaPCEconomicos == null) || (!savePages.paginaPCEconomicos))
			Messages.error("La página Conceptos Económicos no fue guardada correctamente");
	}

	// === MANUAL REGION START ===
	public Solicitud(Agente agente) {
		super.init();
		init();
		this.save();

		//Crea la participacion
		Participacion p = new Participacion();
		p.agente = agente;
		p.solicitud = this;
		p.tipo = "creador";
		p.save();
	}

	/**
	 * Método para evaluar la obligatoriedad de los documentos
	 * aportados, este método se llama para todos los documentos 
	 * que tengan obligatoriedad automatica. Modificar para Cambiar
	 * el comportamiento.
	 * 
	 * @param uri: uri del documento a evaluar
	 * @return
	 * 		- true: si el documento es obligatorio
	 */
	@Override
	public boolean documentoEsObligatorio(String uri) {
		return false;
	}

	// === MANUAL REGION END ===

}
