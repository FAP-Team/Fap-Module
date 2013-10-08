package services.async.aed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPFaultException;

import models.Agente;
import models.Convocatoria;
import models.ExpedienteAed;
import models.InformacionRegistro;
import models.RepresentantePersonaJuridica;
import models.ResolucionFAP;
import models.SolicitudGenerica;
import models.Tramite;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import config.InjectorConfig;
import controllers.fap.AgenteController;

import platino.PlatinoProxy;
import play.db.jpa.GenericModel.JPAQuery;
import play.libs.F.Promise;
import play.libs.MimeTypes;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.aed.Interesados;
import services.aed.ProcedimientosService;
import services.aed.TiposDocumentosService;
import services.async.GenericServiceAsyncImpl;
import services.async.GestorDocumentalServiceAsync;
import services.filesystem.TipoDocumentoEnTramite;
import services.filesystem.TipoDocumentoGestorDocumental;
import services.ticketing.TicketingService;
import tramitacion.Documentos;
import utils.BinaryResponse;
import utils.StreamUtils;
import utils.WSUtils;
import enumerado.fap.gen.TipoCrearExpedienteAedEnum;
import es.gobcan.eadmon.aed.ws.Aed;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.AedPortType;
import es.gobcan.eadmon.aed.ws.dominio.DocumentoEnUbicacion;
import es.gobcan.eadmon.aed.ws.dominio.Expediente;
import es.gobcan.eadmon.aed.ws.dominio.Solicitud;
import es.gobcan.eadmon.aed.ws.dominio.Ubicaciones;
import es.gobcan.eadmon.aed.ws.excepciones.CodigoErrorEnum;
import es.gobcan.eadmon.aed.ws.servicios.ObtenerDocumento;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Contenido;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Documento;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firma;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Firmante;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesAdministrativas;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesAvanzadas;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.PropiedadesDocumento;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.RegistroDocumento;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.Resolucion;
import es.gobcan.eadmon.gestordocumental.ws.gestionelementos.dominio.TipoPropiedadAvanzadaEnum;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.TiposDocumentosExcepcion;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;
import es.gobcan.eadmon.procedimientos.ws.ProcedimientosExcepcion;
import es.gobcan.eadmon.verificacion.ws.dominio.ListaDocumentosVerificacion;

import static com.google.common.base.Preconditions.*;

public class GestorDocumentalServiceAsyncImpl extends GenericServiceAsyncImpl implements GestorDocumentalServiceAsync {
	

	static GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);

    public Promise<Integer> configure() throws GestorDocumentalServiceException {
		return (Promise<Integer>) execute(gestorDocumentalService, "configure");
    }

	public Promise<Boolean> isConfigured() {
		play.Logger.info("isConfigured " + gestorDocumentalService);
		gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		for (int i = 0; i < 10; i++) {
			play.Logger.info("isConfigured " +  i);
		}
		return (Promise<Boolean>) execute(gestorDocumentalService, "isConfigured");
	}

    public Promise<Integer> mostrarInfoInyeccion() {
		return (Promise<Integer>)  execute(gestorDocumentalService, "mostrarInfoInyeccion");
    }
	
	protected Promise<String> getVersion() throws AedExcepcion {
        return (Promise<String>) execute(gestorDocumentalService, "getVersion");
	}

    public Promise<String> crearExpediente(SolicitudGenerica solicitud) throws GestorDocumentalServiceException {
		Object[] params = {solicitud};
		Class[] types = {SolicitudGenerica.class};
        return (Promise<String>) execute(gestorDocumentalService, "crearExpediente", params, types);
    }

    public Promise<List<String>> getDocumentosEnExpediente(String expediente) throws GestorDocumentalServiceException {
		Object[] params = {expediente};
		Class[] types = {String.class};
        return (Promise<List<String>>) execute(gestorDocumentalService, "getDocumentosEnExpediente", params, types);       
    }

    public Promise<List<models.Documento>> getDocumentosPorTipo(String tipoDocumento) throws GestorDocumentalServiceException {
    	Object[] params = {tipoDocumento};
		Class[] types = {String.class};
		return (Promise<List<models.Documento>>) execute(gestorDocumentalService, "getDocumentosPorTipo", params, types);
    }

    public Promise<BinaryResponse> getDocumento(models.Documento documento) throws GestorDocumentalServiceException {
    	Object[] params = {documento};
		Class[] types = {models.Documento.class};
		return (Promise<BinaryResponse>) execute(gestorDocumentalService, "getDocumento", params, types);
    }

    public Promise<BinaryResponse> getDocumentoConInformeDeFirma(models.Documento documento) throws GestorDocumentalServiceException {
    	Object[] params = {documento};
		Class[] types = {models.Documento.class};
		return (Promise<BinaryResponse>) execute(gestorDocumentalService, "getDocumentoConInformeDeFirma", params, types);
    }

    public Promise<BinaryResponse> getDocumentoByUri(String uriDocumento) throws GestorDocumentalServiceException {
    	Object[] params = {uriDocumento};
		Class[] types = {String.class};
		return (Promise<BinaryResponse>) execute(gestorDocumentalService, "getDocumentoByUri", params, types);
    }

    public Promise<BinaryResponse> getDocumentoConInformeDeFirmaByUri(String uriDocumento) throws GestorDocumentalServiceException {
    	Object[] params = {uriDocumento};
		Class[] types = {String.class};
		return (Promise<BinaryResponse>) execute(gestorDocumentalService, "getDocumentoConInformeDeFirmaByUri", params, types);
    }

	public Promise<String> saveDocumentoTemporal(models.Documento documento, InputStream contenido, String filename) throws GestorDocumentalServiceException {
    	Object[] params = {documento, contenido, filename};
		Class[] types = {models.Documento.class, InputStream.class, String.class};
		return (Promise<String>) execute(gestorDocumentalService, "saveDocumentoTemporal", params, types);
	}

    public Promise<String> saveDocumentoTemporal(models.Documento documento, File file) throws GestorDocumentalServiceException {
    	Object[] params = {documento, file};
		Class[] types = {models.Documento.class, File.class};
		return (Promise<String>) execute(gestorDocumentalService, "saveDocumentoTemporal", params, types);
    }

    public Promise<Integer> updateDocumento(models.Documento documento) throws GestorDocumentalServiceException {
    	Object[] params = {documento};
		Class[] types = {models.Documento.class};
		return (Promise<Integer>) execute(gestorDocumentalService, "updateDocumento", params, types);
    }

    public Promise<Integer> deleteDocumento(models.Documento documento) throws GestorDocumentalServiceException {
    	Object[] params = {documento};
		Class[] types = {models.Documento.class};
		return (Promise<Integer>) execute(gestorDocumentalService, "deleteDocumento", params, types);
    }	

    public Promise<Integer> clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos, InformacionRegistro informacionRegistro, boolean notificable) throws GestorDocumentalServiceException {
    	Object[] params = {solicitud, documentos, informacionRegistro, notificable};
		Class[] types = {SolicitudGenerica.class, List.class, InformacionRegistro.class, boolean.class};
		return (Promise<Integer>) execute(gestorDocumentalService, "deleteDocumento", params, types);
    }

    public Promise<Integer> clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos, InformacionRegistro informacionRegistro) throws GestorDocumentalServiceException {
    	Object[] params = {solicitud, documentos, informacionRegistro};
		Class[] types = {SolicitudGenerica.class, List.class, InformacionRegistro.class};
		return (Promise<Integer>) execute(gestorDocumentalService, "clasificarDocumentos", params, types);
    }

    public Promise<Integer> clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos) throws GestorDocumentalServiceException {
    	Object[] params = {solicitud, documentos};
		Class[] types = {SolicitudGenerica.class, List.class};
		return (Promise<Integer>) execute(gestorDocumentalService, "clasificarDocumentos", params, types);
    }

    public Promise<Integer> clasificarDocumentos(SolicitudGenerica solicitud, List<models.Documento> documentos, boolean notificable) throws GestorDocumentalServiceException {
        return clasificarDocumentos(solicitud, documentos, null, notificable);
    }

    public Promise<Integer> agregarFirma(models.Documento documento, models.Firma firma) throws GestorDocumentalServiceException {
    	Object[] params = {documento, firma};
		Class[] types = {models.Documento.class, models.Firma.class};
		return (Promise<Integer>) execute(gestorDocumentalService, "agregarFirma", params, types);
    }

	public Promise<Integer> agregarFirma(models.Documento documento, String firmaStr) throws GestorDocumentalServiceException {
    	Object[] params = {documento, firmaStr};
		Class[] types = {models.Documento.class, String.class};
		return (Promise<Integer>) execute(gestorDocumentalService, "agregarFirma", params, types);
	}

    public Promise<models.Firma> getFirma(models.Documento documento) throws GestorDocumentalServiceException {
    	Object[] params = {documento};
		Class[] types = {models.Documento.class};
		return (Promise<models.Firma>) execute(gestorDocumentalService, "getFirma", params, types);
    }

    public Promise<List<Tramite>> getTramites() throws GestorDocumentalServiceException {
		return (Promise<List<Tramite>>) execute(gestorDocumentalService, "getTramites");
    }

    public Promise<Integer> actualizarCodigosExclusion() {
    	return (Promise<Integer>) execute(gestorDocumentalService, "actualizarCodigosExclusion");
    }

    public Promise<String> crearExpediente(ExpedienteAed expedienteAed) throws GestorDocumentalServiceException {        
    	Object[] params = {expedienteAed};
		Class[] types = {ExpedienteAed.class};
		return (Promise<String>) execute(gestorDocumentalService, "crearExpediente", params, types);
    }

	public Promise<String> modificarInteresados (ExpedienteAed expedienteAed, SolicitudGenerica solicitud) throws GestorDocumentalServiceException {
    	Object[] params = {expedienteAed, solicitud};
		Class[] types = {ExpedienteAed.class, SolicitudGenerica.class};
		return (Promise<String>) execute(gestorDocumentalService, "modificarInteresados", params, types);
	}
	
	public Promise<List<TipoDocumentoEnTramite>> getTiposDocumentosAportadosCiudadano (models.Tramite tramite) {
    	Object[] params = {tramite};
		Class[] types = {models.Tramite.class};
		return (Promise<List<TipoDocumentoEnTramite>>) execute(gestorDocumentalService, "getTiposDocumentosAportadosCiudadano", params, types);
	}
	
	public Promise<List<TipoDocumentoGestorDocumental>> getListTiposDocumentosAportadosCiudadano (models.Tramite tramite) {
    	Object[] params = {tramite};
		Class[] types = {models.Tramite.class};
		return (Promise<List<TipoDocumentoGestorDocumental>>) execute(gestorDocumentalService, "getListTiposDocumentosAportadosCiudadano", params, types);
	}

	public Promise<String> getExpReg(){
		return (Promise<String>) execute(gestorDocumentalService, "getExpReg");
	}

	public Promise<Integer> duplicarDocumentoSubido(String uriDocumento) throws GestorDocumentalServiceException {
    	Object[] params = {uriDocumento};
		Class[] types = {String.class};
		return (Promise<Integer>) execute(gestorDocumentalService, "duplicarDocumentoSubido", params, types);
	}

	public Promise<Integer> duplicarDocumentoSubido(String uriDocumento, String descripcionDocumento, models.Documento dbDocumento) throws GestorDocumentalServiceException {
    	Object[] params = {uriDocumento, descripcionDocumento, dbDocumento};
		Class[] types = {String.class, String.class, models.Documento.class};
		return (Promise<Integer>) execute(gestorDocumentalService, "duplicarDocumentoSubido", params, types);
	}

	public Promise<Integer> clasificarDocumentoResolucion(ResolucionFAP resolucionFap) throws GestorDocumentalServiceException {
    	Object[] params = {resolucionFap};
		Class[] types = {ResolucionFAP.class};
		return (Promise<Integer>) execute(gestorDocumentalService, "clasificarDocumentoResolucion", params, types);
	}

	public Promise<String> crearExpedienteConvocatoria() throws GestorDocumentalServiceException {
		return (Promise<String>) execute(gestorDocumentalService, "crearExpedienteConvocatoria");
	}

	public Promise<String> getDocumentoFirmaByUri(String uriDocumento) throws GestorDocumentalServiceException {
    	Object[] params = {uriDocumento};
		Class[] types = {String.class};
		return (Promise<String>) execute(gestorDocumentalService, "getDocumentoFirmaByUri", params, types);
	}

	public Promise<Integer> copiarDocumentoEnExpediente(String uri, List<ExpedienteAed> expedientesAed) throws GestorDocumentalServiceException {
    	Object[] params = {uri, expedientesAed};
		Class[] types = {String.class, List.class};
		return (Promise<Integer>) execute(gestorDocumentalService, "copiarDocumentoEnExpediente", params, types);
	}

	@Deprecated
	public void duplicarDocumentoSubido(String uriDocumento,
			SolicitudGenerica solicitud)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}

	@Deprecated
	public void duplicarDocumentoSubido(String uriDocumento,
			String descripcionDocumento, models.Documento dbDocumento,
			SolicitudGenerica solicitud)
			throws GestorDocumentalServiceException {
		// TODO Auto-generated method stub
		
	}
}
