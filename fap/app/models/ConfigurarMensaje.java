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

	public static Model loadModel(Object key, Pattern keyPattern, LinkedHashMap<Object, Map<?, ?>> objects, Object o) {

		//System.out.println("key: " + key);
		Matcher matcher = keyPattern.matcher(key.toString().trim());
		if (matcher.matches()) {
			// Type of the object. i.e. models.employee
			String type = matcher.group(1);
			// Id of the entity i.e. nicolas
			String id = matcher.group(2);
			//System.out.println("id: " + id + "   type: " + type);

			if (!type.startsWith("models.")) {
				type = "models." + type;
			}

			// Was the entity already defined?

			// Those are the properties that were parsed from the YML
			// file
			final Map<?, ?> entityValues = objects.get(key);

			// Prefix is object, why is that?
			final Map<String, String[]> fields = ModelUtils.serialize(entityValues, "object");

			try {

				@SuppressWarnings("unchecked")
				Class<play.db.Model> cType = (Class<play.db.Model>) Play.classloader.loadClass(type);
				final Map<String, String[]> resolvedFields = ModelUtils.resolveDependencies(cType, fields, (Map<String, Object>) o);

				RootParamNode rootParamNode = ParamNode.convert(resolvedFields);
				// This is kind of hacky. This basically says that if we
				// have an embedded class we should ignore it.
				if (Model.class.isAssignableFrom(cType)) {

					Model model = (Model) Binder.bind(rootParamNode, "object", cType, cType, null);
					for (Field f : model.getClass().getFields()) {
						if (f.getType().isAssignableFrom(Map.class)) {
							f.set(model, objects.get(key).get(f.getName()));
						}
						if (f.getType().equals(byte[].class)) {
							f.set(model, objects.get(key).get(f.getName()));
						}
					}
					return model;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// === MANUAL REGION END ===

}
