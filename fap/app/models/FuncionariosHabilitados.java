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
public class FuncionariosHabilitados extends Singleton {
	// Código de los atributos

	public String texto;

	public void init() {
		super.init();

		postInit();
	}

	// === MANUAL REGION START ===
	public static List<Firmante> getFirmantes() {
		play.Logger.info("getFirmantes");
		List<Firmante> todos = new ArrayList<Firmante>();
		List<models.Agente> agentes = models.Agente.findAll();
		for (models.Agente agent : agentes) {
			if (agent.funcionario) {
				Firmante firmante = new Firmante(agent);
				todos.add(firmante);
			}
		}
		return todos;
	}
	// === MANUAL REGION END ===

}
