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
public class Firmantes extends FapModel {
	// Código de los atributos

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "firmantes_todos")
	public List<Firmante> todos;

	public Firmantes() {
		init();
	}

	public void init() {

		if (todos == null)
			todos = new ArrayList<Firmante>();

		postInit();
	}

	// === MANUAL REGION START ===

	public Firmantes(List<Firmante> firmantes) {
		this.todos = new ArrayList<Firmante>(firmantes);
	}

	public boolean hanFirmadoTodos() {
		if ((todos == null) || (todos.isEmpty())) {
			return false;
		}
        boolean firmado = true;
        boolean multiple = false;
        for (Firmante f : todos) {
            // Firmante único que ya ha firmado
            if (f.cardinalidad.equals("unico") && f.fechaFirma != null)
                return true;

            // Uno de los firmantes multiples no ha firmado
            if (f.cardinalidad.equals("multiple")) {
                multiple = true;
                if (f.fechaFirma == null) {
                    firmado = false;
                }
            }
        }

        // En el caso de que no haya firmado ningún único
        // Se devuelve true si todos los múltiples han firmado
        return multiple && firmado;
	}

	/**
	 * Borra una lista de firmantes, borrando cada uno de los firmantes y
	 * vaciando la lista
	 * 
	 * @param firmantes
	 */
	public void borrarFirmantes(List<Firmante> firmantes) {
		List<Firmante> firmantesBack = new ArrayList<Firmante>(firmantes);
		firmantes.clear();

		for (Firmante f : firmantesBack)
			f.delete();
	}

	public static Firmantes calcularFirmanteFromSolicitante(Solicitante solicitante) {
		if (solicitante == null)
			throw new NullPointerException();

		List<Firmante> firmantes = new ArrayList<Firmante>();

		// Solicitante de la solicitud
		Firmante firmanteSolicitante = new Firmante(solicitante, "unico");
		firmantes.add(firmanteSolicitante);

		// Comprueba los representantes
		if (solicitante.isPersonaFisica() && solicitante.representado) {
			// Representante de persona física
			Firmante representante = new Firmante(solicitante.representante, "representante", "unico");
			firmantes.add(representante);
		} else if (solicitante.isPersonaJuridica()) {
			// Representantes de la persona jurídica
			for (RepresentantePersonaJuridica r : solicitante.representantes) {
				String cardinalidad = null;
				if (r.tipoRepresentacion.equals("mancomunado")) {
					cardinalidad = "multiple";
				} else if ((r.tipoRepresentacion.equals("solidario")) || (r.tipoRepresentacion.equals("administradorUnico"))) {
					cardinalidad = "unico";
				}
				Firmante firmante = new Firmante(r, "representante", cardinalidad);
				firmantes.add(firmante);
			}
		}
		return new Firmantes(firmantes);
	}

	public boolean containsFirmanteConIdentificador(String id) {
		for (Firmante firmante : todos) {
			if (firmante.idvalor != null && firmante.idvalor.equals(id)) {
				return true;
			}
		}
		return false;
	}

	public boolean haFirmado(String id) {
		for (Firmante firmante : todos) {
			if (firmante.idvalor != null && firmante.idvalor.equals(id) && firmante.fechaFirma != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		String out = "";
		for (Firmante firmante : todos) {
			out += firmante.toString();
		}
		return out;
	}

	// === MANUAL REGION END ===

}
