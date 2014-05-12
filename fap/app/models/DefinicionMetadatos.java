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
public class DefinicionMetadatos extends FapModel {
	// Código de los atributos

	public String nombre;

	public String descripcion;

	public boolean autogenerado;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "definicionmetadatos_listametadatos")
	public List<Metadato> listaMetadatos;

	public DefinicionMetadatos() {
		init();
	}

	public void init() {

		if (listaMetadatos == null)
			listaMetadatos = new ArrayList<Metadato>();

		postInit();
	}

	// === MANUAL REGION START ===
	@Override
	public void postInit() {
		super.postInit();
		valoresPosibles = new ArrayList<String>();
		valoresPorDefecto = new ArrayList<String>();
	}

	public boolean esValido(String valor) {
        return valoresPosibles.contains(valor);
	}

	public static int deleteAllDefiniciones() {
		JPA.em().createNativeQuery("delete from definicionmetadatos_valorespordefecto").executeUpdate();
		JPA.em().createNativeQuery("delete from definicionmetadatos_valoresposibles").executeUpdate();
		JPA.em().createNativeQuery("delete from tipodocumento_definicionmetadatos").executeUpdate();
		List<Metadato> metadatos = Metadato.findAll();
		for (Metadato metadato : metadatos) {
			metadato.delete();
		}
		List<DefinicionMetadatos> definiciones = DefinicionMetadatos.findAll();
		for (DefinicionMetadatos def : definiciones) {
			def.delete();
		}
		return definiciones.size();
	}

	public List<Metadato> crearMetadatosPorDefecto(Documento doc) {
		List<Metadato> lista = new ArrayList<Metadato>();
        try {
            for (String valor : valoresPorDefecto) {
                Metadato md = new Metadato(this, valor, doc);
                md.save();
                lista.add(md);
            }
        } catch (IllegalStateException e) {
            String mensajeError = "Error al guardar los Metadatos asociados al documento."
                    + ". Tanto la DefinicionMetadatos como el Documento han de estar en BBDD primero.";
            play.Logger.error(mensajeError);
            throw e;
        }
        return lista;
	}
	// === MANUAL REGION END ===

}
