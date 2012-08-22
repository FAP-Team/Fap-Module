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
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import utils.ModelUtils;
import play.data.binding.Binder;
import play.data.binding.ParamNode;
import play.data.binding.RootParamNode;
import play.Play;
import java.lang.reflect.Field;

// === IMPORT REGION END ===

@Entity
public class ConfigurarMensaje extends FapModel {
	// Código de los atributos

	@ValueFromTable("tipoMensaje")
	public String tipoMensaje;

	public String tituloMensaje;

	@Column(columnDefinition = "LONGTEXT")
	public String contenido;

	public Boolean habilitar;

	@Transient
	public String habilitarText;

	public String nombrePagina;

	public String formulario;

	@Transient
	public String formularioNombreText;

	public void init() {

		postInit();
	}

	// === MANUAL REGION START ===

	public String getHabilitarText() {
		if (habilitar)
			return "Sí";
		return "No";
	}

	public String getFormularioNombreText() {
		return formulario + "-" + nombrePagina;
	}

	// === MANUAL REGION END ===

}
