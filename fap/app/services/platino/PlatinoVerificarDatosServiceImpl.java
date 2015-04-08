package services.platino;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import platino.PlatinoProxy;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import es.gobcan.platino.servicios.registro.Registro_Service;
import es.gobcan.platino.servicios.svd.*;
import es.gobcan.platino.servicios.svd.peticionconsentimiento.Consentimiento;
import es.gobcan.platino.servicios.svd.peticiondatosgenericos.DatosGenericos;
import es.gobcan.platino.servicios.svd.peticionfuncionario.Funcionario;
import es.gobcan.platino.servicios.svd.peticionpeticionpdf.PeticionPdf;
import es.gobcan.platino.servicios.svd.peticionpeticionrecover.PeticionRecover;
import es.gobcan.platino.servicios.svd.peticionpeticionsincrona.PeticionSincrona;
import es.gobcan.platino.servicios.svd.peticionprocedimiento.Procedimiento;
import es.gobcan.platino.servicios.svd.peticionsolicitante.Solicitante;
import es.gobcan.platino.servicios.svd.peticiontipodocumentacion.TipoDocumentacion;
import es.gobcan.platino.servicios.svd.peticiontitularpet.Titularpet;
import es.gobcan.platino.servicios.svd.solicitudrespuestasolicitudrespuesta.SolicitudRespuesta;
import models.SolicitudGenerica;
import services.VerificarDatosService;
import services.MensajeServiceException;
import services.VerificarDatosServiceException;
import utils.WSUtils;

@InjectSupport
public class PlatinoVerificarDatosServiceImpl implements services.VerificarDatosService {

	private PropertyPlaceholder propertyPlaceholder;
	
	private ScspwsService verificaPort;
	
	@Inject
	public PlatinoVerificarDatosServiceImpl(PropertyPlaceholder propertyPlaceholder) {
		
        this.propertyPlaceholder = propertyPlaceholder;

        URL wsdlURL = PlatinoVerificarDatosServiceImpl.class.getClassLoader().getResource("wsdl/svd.wsdl");
        verificaPort = new ScspwsService_Service(wsdlURL).getScspwsServicePort();

        WSUtils.configureEndPoint(verificaPort, getEndPoint());
        WSUtils.configureSecurityHeaders(verificaPort, propertyPlaceholder);
        PlatinoProxy.setProxy(verificaPort, propertyPlaceholder);
    }
    

	
	public boolean isConfigured(){
	    return hasConnection();
	}
	
	@Override
    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de SVD ha sido inyectado con Platino y está operativo.");
		else
			play.Logger.info("El servicio de SVD ha sido inyectado con Platino y NO está operativo.");
    }
	
	private boolean hasConnection() {
		boolean hasConnection = false;
		try {
			hasConnection =  getVersion() != null;
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "? :"+hasConnection);
		}catch(Exception e){
			play.Logger.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}
	
    private String getVersion() throws SvdException {
        return verificaPort.getVersion();
    }
    
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.platino.svd.url");
	}
	
	private Solicitante setSolicitante (String idSolicitante, String nombreSolicitante, String finalidad, String unidadTramitadora, 
			String idExpediente, Procedimiento procedimiento, Funcionario funcionario, Consentimiento consentimiento){
		Solicitante solicitante = new Solicitante();
		solicitante.setIdentificadorSolicitante(idSolicitante);
		solicitante.setNombreSolicitante(nombreSolicitante);
		solicitante.setFinalidad(finalidad);
		solicitante.setUnidadTramitadora(unidadTramitadora);
		solicitante.setIdExpediente(idExpediente);
		solicitante.setProcedimiento(procedimiento);
		solicitante.setFuncionario(funcionario);
		solicitante.setConsentimiento(consentimiento);
		//solicitante.setProcedimiento(setProcedimiento(codigoProc, nombreProc));
		//solicitante.setFuncionario(setFuncionario(nombreCompletoFuncionario, nif));
		//solicitante.setConsentimiento(setValorConsentimiento(valorConsentimiento));
		return solicitante;
	}
	
	private Procedimiento setProcedimiento (String codigoProcedimiento, String nombreProcedimiento){
		Procedimiento procedimiento = new Procedimiento();
		procedimiento.setCodProcedimiento(codigoProcedimiento);
		procedimiento.setNombreProcedimiento(nombreProcedimiento);
		
		return procedimiento;
	}
	
	private Funcionario setFuncionario (String nombreCompletoFuncionario, String nif){
		Funcionario funcionario = new Funcionario();
		funcionario.setNombreCompletoFuncionario(nombreCompletoFuncionario);
//		funcionario.setNifFuncionario(nif);
		return funcionario;
	}
	
	private Consentimiento setValorConsentimiento (String valor){
		Consentimiento consentimiento = null;
		if ("SI".equalsIgnoreCase(valor)){
			consentimiento = Consentimiento.fromValue(valor);
		}
		else if ("LEY".equalsIgnoreCase(valor)){
			consentimiento = Consentimiento.fromValue(valor);	
		}
		
		return consentimiento;
	}
	
	private TipoDocumentacion setValorTipoDocumentacion (String valor){
		TipoDocumentacion tipoDocumentacion = null;
		if ("DNI".equalsIgnoreCase(valor)){
			tipoDocumentacion = TipoDocumentacion.fromValue("DNI");
		}else if ("NIE".equalsIgnoreCase(valor)){
			tipoDocumentacion = TipoDocumentacion.fromValue("NIE");
		}else if ("NIF".equalsIgnoreCase(valor)){
			tipoDocumentacion = TipoDocumentacion.fromValue("NIF");
		}else if ("CIF".equalsIgnoreCase(valor)){
			tipoDocumentacion = TipoDocumentacion.fromValue("CIF");
		}else if ("PASAPORTE".equalsIgnoreCase(valor)){
			tipoDocumentacion = TipoDocumentacion.fromValue("PASAPORTE");
		}
		
		return tipoDocumentacion;
	}
	
	private Titularpet setTitular (String documentacion, String nombreCompleto, String nombre, String apellido1, String apellido2, TipoDocumentacion tipodoc){
		Titularpet titularpet = new Titularpet();
		titularpet.setDocumentacion(documentacion);
		titularpet.setNombreCompleto(nombreCompleto);
		titularpet.setNombre(nombre);
		titularpet.setApellido1(apellido1);
		titularpet.setApellido2(apellido2);
		titularpet.setTipoDocumentacion(tipodoc);
		
		return titularpet;
	}
	
	private es.gobcan.platino.servicios.svd.peticionatributos.Atributos setAtributos (String codigoCertificado){
		es.gobcan.platino.servicios.svd.peticionatributos.Atributos atributos = new es.gobcan.platino.servicios.svd.peticionatributos.Atributos();
		atributos.setCodigoCertificado(codigoCertificado);
		
		return atributos;
	}
	
	private DatosGenericos setDatos (Solicitante solicitante, Titularpet titular){
		
		DatosGenericos datos = new DatosGenericos();
	//	datos.setSolicitante(setSolicitante(idSolicitante, nombreSolicitante, finalidad, unidadTramitadora, idExpediente, codigoProc,
	//			nombreProc, nombreCompletoFuncionario, nif, valorConsentimiento));
	//	datos.setTitular(setTitular(documentacion, nombreCompleto, nombre, apellido1, apellido2, tipoDoc));
		datos.setSolicitante(solicitante);
		datos.setTitular(titular);		
		return datos;
	}
	
	
	//EN TEORÍA SE RECORRE UNA LISTA DE SOLICITUDES Y se van obteniendo los datos 
	
//	public Solicitudes rellenarSolicitudes (List<SolicitudGenerica> solicitudesGenericas){
//		
//		Solicitudes solicitudSVD = new Solicitudes();
//		for (SolicitudGenerica solicitud:solicitudesGenericas){	
//			Procedimiento procedimiento = setProcedimiento(codigoProc, nombreProc);
//			Funcionario funcionario = setFuncionario(nombreCompletoFuncionario, nif);
//			Consentimiento consentimiento = setValorConsentimiento(valorConsentimiento);
//			Solicitante solicitante = setSolicitante(idSolicitante, nombreSolicitante, finalidad, unidadTramitadora, idExpediente, 
//					procedimiento, funcionario, consentimiento);
//			
//			TipoDocumentacion tipoDocumento = setValorTipoDocumentacion(tipoDoc);
//			Titularpet titular = setTitular(documentacion, nombreCompleto, nombre, apellido1, apellido2, tipoDocumento);
//			
//			DatosGenericos datosGenericos = setDatos(solicitante, titular);
//			
//			SolicitudTransmision solicitudTransmision = setSolicitudTransmision(datosGenericos);
//			
//
//			solicitudSVD.getSolicitudTransmision().add(solicitudTransmision);
//		}
//		return solicitudSVD;
//	}
	
	private SolicitudTransmision setSolicitudTransmision (DatosGenericos datosGenericos){
		
		SolicitudTransmision solicitudTransmision = new SolicitudTransmision();
		solicitudTransmision.setDatosGenericos(datosGenericos);
		return solicitudTransmision;
	}
	
	private Solicitudes setSolicitud (Solicitudes solicitud, SolicitudTransmision solicitudTransmision){
		solicitud.getSolicitudTransmision().add(solicitudTransmision);
		return solicitud;
	}
	
	public Respuesta peticionSincronaIdentidad(String codigoCertificado, String uidUsuario, String idSolicitante, String nombreSolicitante, 
			String finalidad, String idExpediente, String unidadTramitadora, String codigoProc, String nombreProc, String nombreCompletoFuncionario, 
			String nif, String valorConsentimiento, String documentacion, String nombreCompleto, String nombre, String apellido1, 
			String apellido2, String tipoDoc) throws VerificarDatosServiceException{
		
		PeticionSincrona peticion = new PeticionSincrona();
		peticion.setUidUsuario(uidUsuario);
		peticion.setAtributos(setAtributos(codigoCertificado));
		
		Procedimiento procedimiento = setProcedimiento(codigoProc, nombreProc);
		Funcionario funcionario = setFuncionario(nombreCompletoFuncionario, nif);
		Consentimiento consentimiento = setValorConsentimiento(valorConsentimiento);
		Solicitante solicitante = setSolicitante(idSolicitante, nombreSolicitante, finalidad, unidadTramitadora, idExpediente, 
				procedimiento, funcionario, consentimiento);
		
		TipoDocumentacion tipoDocumento = setValorTipoDocumentacion(tipoDoc);
		Titularpet titular = setTitular(documentacion, nombreCompleto, nombre, apellido1, apellido2, tipoDocumento);
		
		DatosGenericos datosGenericos = setDatos(solicitante, titular);
		
		SolicitudTransmision solicitudTransmision = setSolicitudTransmision(datosGenericos);
		
		Solicitudes solicitud = new Solicitudes();
		solicitud.getSolicitudTransmision().add(solicitudTransmision);

		peticion.setSolicitudes(solicitud);

		try{
			return verificaPort.peticionSincrona(peticion);
		}
		catch (Exception e){
			System.out.println("No se ha podido realizar la petición. Causa: " + e);
			throw new VerificarDatosServiceException("Error al realizar la petición sincrona");
		}
	}
	
	public Respuesta peticionAsincronaIdentidad(Solicitudes solicitud, String codigoCertificado, String uidUsuario, String idSolicitante, String nombreSolicitante, 
			String finalidad, String idExpediente, String unidadTramitadora, String codigoProc, String nombreProc, String nombreCompletoFuncionario, String nif, 
			String valorConsentimiento, String documentacion, String nombreCompleto, String nombre, String apellido1, String apellido2, String tipoDoc) throws VerificarDatosServiceException{
		
		PeticionSincrona peticion = new PeticionSincrona();
		peticion.setUidUsuario(uidUsuario);
		peticion.setAtributos(setAtributos(codigoCertificado));
		peticion.setSolicitudes(solicitud);
		try{
			return verificaPort.peticionSincrona(peticion);
		}
		catch (Exception e){
			System.out.println("No se ha podido realizar la petición. Causa: " + e);
			throw new VerificarDatosServiceException("Error al realizar la petición sincrona");
		}
	}
	
	public Respuesta peticionRecover(String uidUsuario, String IdPeticion) throws VerificarDatosServiceException {
		PeticionRecover peticion = new PeticionRecover();
		peticion.setIdPeticion(IdPeticion);
		peticion.setUidUsuario(uidUsuario);
		try {
			return verificaPort.peticionRecover(peticion);
		}
		catch (Exception e){
			System.out.println("No se ha encontrado la petición: " + e);
			throw new VerificarDatosServiceException("No se pudo encontrar la petición");
		}
	}
	
	public RespuestaPdf peticionPDF (String IdPeticion, String IdTransmision, String uidUsuario) throws VerificarDatosServiceException{
		PeticionPdf peticion = new PeticionPdf();
		peticion.setIdPeticion(IdPeticion);
		peticion.setIdTransmision(IdTransmision);
		peticion.setUidUsuario(uidUsuario);
		try {
			return verificaPort.peticionPdf(peticion);
		}
		catch (Exception e){
			System.out.println("No se ha encontrado el pdf: " + e);
			throw new VerificarDatosServiceException("No se pudo encontrar el pdf solicitado");
		}
		
	}
	
	public Respuesta solicitudRespuesta (String uidUsuario, String idPeticion, String codigoCertificado, Integer numElementos) throws VerificarDatosServiceException{
		
		SolicitudRespuesta solicitud = new SolicitudRespuesta();
		solicitud.setUidUsuario(uidUsuario);
		
		Atributos atributos = new Atributos();
		atributos.setIdPeticion(idPeticion);
		atributos.setCodigoCertificado(codigoCertificado);
		atributos.setNumElementos(numElementos);
		
		try{
			return verificaPort.solicitudRespuesta(solicitud);
		}
		catch(Exception e){
			System.out.println("Se ha producido el error: " + e.getMessage());
			throw new VerificarDatosServiceException("No se ha podido dar respuesta");
		}
	}
}