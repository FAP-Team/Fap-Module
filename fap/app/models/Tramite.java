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
public class Tramite extends FapModel {
	// Código de los atributos

	public String uri;

	public String nombre;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "tramite_documentos")
	public List<TipoDocumento> documentos;

	public Tramite() {
		init();
	}

	public void init() {

		if (documentos == null)
			documentos = new ArrayList<TipoDocumento>();

		postInit();
	}

	// === MANUAL REGION START ===
	public static List<TipoDocumento> findTipoDocumentosFrom(String tramite) {
		List<TipoDocumento> tiposDocumentos = TipoDocumento.find("select tipoDocumento from Tramite tramite " + "join tramite.documentos tipoDocumento where tramite.nombre=?", tramite).fetch();
		return tiposDocumentos;
	}

	public static List<TipoDocumento> findTipoDocumentosAportadosPor(String tramite, String aportadoPor) {
		List<TipoDocumento> tiposDocumentos = TipoDocumento.find("select tipoDocumento from Tramite tramite " + "join tramite.documentos tipoDocumento where tramite.nombre=? " + "and tipoDocumento.aportadoPor = ?", tramite, aportadoPor).fetch();
		return tiposDocumentos;
	}

	public static List<TipoDocumento> findTipoDocumentosAportadosPor(String aportadoPor) {
		List<TipoDocumento> tiposDocumentos = TipoDocumento.find("select tipoDocumento from Tramite tramite " + "join tramite.documentos tipoDocumento where tipoDocumento.aportadoPor = ?", aportadoPor).fetch();
		return tiposDocumentos;
	}

	public static void deleteAllTramitesAndTipoDocumentos() {
		List<Tramite> tramites = Tramite.findAll();
		for (Tramite tramite : tramites) {
			tramite.documentos = null;
			tramite.delete();
		}
		TipoDocumento.deleteAll();
		TipoCodigoExclusion.deleteAll();
		TiposCodigoRequerimiento.deleteAll();
	}

	/**
	 * Crea un nuevo documento con los datos pasados por argumento y lo añade a la lista documentos
	 * 
	 * @param nombre
	 * @param tipo
	 * @param cardinalidad
	 * @return
	 */
	public String setDocumentoEnTramite(String nombre, String tipo, String cardinalidad) {
		TipoDocumento tipoDocumento = new TipoDocumento();
		tipoDocumento.nombre = nombre;
		tipoDocumento.uri = tipo;
		tipoDocumento.cardinalidad = cardinalidad;
		tipoDocumento.tramitePertenece = this.uri;
		tipoDocumento.aportadoPor = "CIUDADANO";
		tipoDocumento.obligatoriedad = "OBLIGATORIO";
		tipoDocumento.save();
		this.documentos.add(tipoDocumento);
		return tipoDocumento.uri;
	}

	/**
	 * Establece los códigos de requerimiento para el documento pasado por argumento.
	 * 
	 * @param uriDocumento
	 * @param args Lista de objetos TiposCodigoRequerimiento
	 */
	public void setCodigosRequerimiento(String uriTipoDocumento, TiposCodigoRequerimiento... codigosReq) {
		for (TiposCodigoRequerimiento codigoReq : codigosReq) {
			TiposCodigoRequerimiento tipoCodReqdb = new TiposCodigoRequerimiento();
			tipoCodReqdb.codigo = codigoReq.codigo;
			tipoCodReqdb.descripcion = codigoReq.descripcion;
			tipoCodReqdb.descripcionCorta = codigoReq.descripcionCorta;
			tipoCodReqdb.uriTipoDocumento = uriTipoDocumento;
			tipoCodReqdb.uriTramite = this.uri;
			tipoCodReqdb.save();
		}
	}

	public boolean existTipoDocumentoAportadoPorCiudadano() {
		List<TipoDocumento> documentosCiudadanoTramite = Tramite.findTipoDocumentosAportadosPor(this.nombre, "CIUDADANO");
		if ((documentosCiudadanoTramite == null) || (documentosCiudadanoTramite.isEmpty()))
			return false;
		return true;
	}

	// === MANUAL REGION END ===

}
