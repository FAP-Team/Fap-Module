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
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import utils.AedUtils;
import utils.DocumentosUtils;
import properties.FapProperties;
import config.InjectorConfig;

// === IMPORT REGION END ===

@Entity
public class Documento extends FapModel {
	// Código de los atributos

	public String uri;

	public String uriPlatino;

	@ValueFromTable("tiposDocumentos")
	public String tipo;

	public String descripcion;

	@Transient
	public String descripcionVisible;

	public Boolean clasificado;

	public String hash;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaSubida"), @Column(name = "fechaSubidaTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaSubida;

	@org.hibernate.annotations.Columns(columns = { @Column(name = "fechaRegistro"), @Column(name = "fechaRegistroTZ") })
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTimeWithZone")
	public DateTime fechaRegistro;

	@Transient
	public String urlDescarga;

	@Transient
	public String enlaceDescarga;

	@Transient
	public String urlDescargaFirmado;

	@Transient
	public String enlaceDescargaFirmado;

	@Transient
	public String enlaceDescargaFirmadoLocal;

	public Boolean verificado;

	public Boolean refAed;

	@ValueFromTable("estadoNotificacion")
	public String estadoDocumento;

	public Boolean firmado;

	@Transient
	public String firmadoVisible;

	@Transient
	public String firmadoVisibleLocal;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Firmantes firmantes;

	public Documento() {
		init();
	}

	public void init() {

		if (uriPlatino == null)
			uriPlatino = "null";

		if (clasificado == null)
			clasificado = false;

		if (firmado == null)
			firmado = false;

		if (firmantes == null)
			firmantes = new Firmantes();
		else
			firmantes.init();

		postInit();
	}

	// === MANUAL REGION START ===
	//	public Documento() {
	//		clasificado = false;
	//		uriPlatino = null;
	//	}

	public boolean isMultiple() {
		return (tipo != null && DocumentosUtils.esTipoMultiple(tipo));
	}

	public String getUrlDescarga() {
		return AedUtils.crearUrl(uri);
	}

	public String getUrlDescargaFirmado() {
		return AedUtils.crearUrlConInformeDeFirma(uri);
	}

	public String getEnlaceDescarga() {
		if (uri != null) {
			String ret = "<a href=\"";
			ret += AedUtils.crearUrl(uri);
			ret += "\" target=\"_blank\">Descargar</a>";
			return ret;
		}
		return "";
	}

	public String getEnlaceDescargaFirmado() {
		if (uri != null) {
			GestorDocumentalService gestorDocumental = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
			try {
				String firma = gestorDocumental.getDocumentoFirmaByUri(uri);
				if (firma != null && !firma.isEmpty()) {
					String ret = "<a href=\"";
					ret += AedUtils.crearUrlConInformeDeFirma(uri);
					ret += "\" target=\"_blank\">Descargar Firmado</a>";
					return ret;
				}
			} catch (Exception e) {
				play.Logger.error("Error al recuperar el documento con uri: " + uri + " del Gestor Documental con Informe de Firma");
			}
		}
		return "";
	}

	public String getEnlaceDescargaFirmadoLocal() {
		if (uri != null && firmado != null && firmado == true) {
			String ret = "<a href=\"";
			ret += AedUtils.crearUrlConInformeDeFirma(uri);
			ret += "\" target=\"_blank\">Descargar Firmado</a>";
			return ret;
		}
		return "";
	}

	public boolean isObligatorio() {
		return (tipo != null && DocumentosUtils.esTipoObligatorio(tipo));
	}

	public boolean isImprescindible() {
		return (tipo != null && DocumentosUtils.esTipoImprescindible(tipo));
	}

	public boolean isCondicionadoAutomatico() {
		return (tipo != null && DocumentosUtils.esTipoCondicionadoAutomatico(tipo));
	}

	public boolean isCondicionadoManual() {
		return (tipo != null && DocumentosUtils.esTipoCondicionadoManual(tipo));
	}

	public String getTipoObligatoriedad() {
		if (tipo != null)
			return DocumentosUtils.getTipoObligatoriedad(tipo);
		return null;
	}

	public boolean isAportadoCiudadano() {
		return (tipo != null && DocumentosUtils.esAportadoCiudadano(tipo));
	}

	public boolean isAportadoOrganismo() {
		return (tipo != null && DocumentosUtils.esAportadoOrganismo(tipo));
	}

	public String getAportadoPor() {
		if (tipo != null)
			return DocumentosUtils.getAportadoPor(tipo);
		return null;
	}

	public Tramite getTramitePertenece() {
		if (tipo != null)
			return DocumentosUtils.getTramitePertenece(tipo);
		return null;
	}

	/**
	 * Transformamos la entidad Documento del gestor documental del Gobierno de Canarias en una entidad Documento de FAP.
	 * 
	 */
	public void docAed2Doc(es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento propiedadesDoc, String tipoDocumento) {
		uri = propiedadesDoc.getUri();
		tipo = tipoDocumento;
		descripcion = propiedadesDoc.getDescripcion();
	}

	public static Documento findByUri(String uri) {
		Documento documento = models.Documento.find("byUri", uri).first();
		return documento;
	}

	public static Documento findByUriPlatino(String uriPlatino) {
		Documento documento = models.Documento.find("byUriPlatino", uriPlatino).first();
		return documento;
	}

	public String getDescripcionVisible() {
		String descripcionDevolver = "";
		if ((this.descripcion != null) && !(this.descripcion.trim().equals("")))
			return this.descripcion;
		descripcionDevolver = TableKeyValue.getValue("tiposDocumentos", tipo);
		if ((descripcionDevolver == null) || (descripcionDevolver.trim().equals(""))) {
			play.Logger.error("La descripción no se pudo obtener a partir del tipo: " + tipo);
			return this.descripcion;
		}
		return descripcionDevolver;
	}

	public String getFirmadoVisible() {
		GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		try {
			String firma = gestorDocumentalService.getDocumentoFirmaByUri(uri);
			if (firma != null && !firma.isEmpty()) {
				play.Logger.info("El documento " + descripcionVisible + " tiene la firma (" + firma + ")");
				return "Sí";
			}
		} catch (Exception e) {
			play.Logger.error("Error al obtener el documento " + descripcionVisible + " firmado");
		}
		return "No";
	}

	public String getFirmadoVisibleLocal() {
		if (firmado == null || firmado == false) {
			return "No";
		}
		return "Sí";
	}

	/*
	 * Duplicamos todos los campos de un documento (no hacemos doc1 = doc2 porque también duplica el id)
	 * 
	 */
	public void duplicar(Documento doc) {
		uri = doc.uri;
		tipo = doc.tipo;
		descripcion = doc.descripcion;
		clasificado = doc.clasificado;
		hash = doc.hash;
		fechaSubida = doc.fechaSubida;
		fechaRegistro = doc.fechaRegistro;
		verificado = doc.verificado;
		refAed = doc.refAed;
		estadoDocumento = doc.estadoDocumento;
		firmado = doc.firmado;
		firmantes = doc.firmantes;
	}

	// === MANUAL REGION END ===

}
