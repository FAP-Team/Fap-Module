package services.platino;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import models.AtributosSVDFAP;
import models.DatosGenericosPeticionSVDFAP;
import models.PeticionSVDFAP;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import platino.PlatinoProxy;
import properties.PropertyPlaceholder;
import services.SVDService;
import services.VerificarDatosServiceException;
import utils.WSUtils;
import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.RespuestaPdf;
import es.gobcan.platino.servicios.svd.ScspwsService;
import es.gobcan.platino.servicios.svd.ScspwsService_Service;
import es.gobcan.platino.servicios.svd.SolicitudTransmisionIdResi;
import es.gobcan.platino.servicios.svd.SolicitudesIdResi;
import es.gobcan.platino.servicios.svd.SvdException;
import es.gobcan.platino.servicios.svd.peticiondatosgenericos.DatosGenericos;
import es.gobcan.platino.servicios.svd.peticionfuncionario.Funcionario;
import es.gobcan.platino.servicios.svd.peticionpeticionsincrona.PeticionSincrona;
import es.gobcan.platino.servicios.svd.peticionprocedimiento.Procedimiento;
import es.gobcan.platino.servicios.svd.peticionsolicitante.Solicitante;
import es.gobcan.platino.servicios.svd.peticiontitularpet.Titularpet;

public class PlatinoSVDImpl implements SVDService {


	private PropertyPlaceholder propertyPlaceholder;
	private ScspwsService verificaPort;

	@Inject
	public PlatinoSVDImpl(PropertyPlaceholder propertyPlaceholder) {

        this.propertyPlaceholder = propertyPlaceholder;

        URL wsdlURL = PlatinoVerificarDatosServiceImpl.class.getClassLoader().getResource("wsdl/svd.wsdl");
        verificaPort = new ScspwsService_Service(wsdlURL).getScspwsServicePort();

        WSUtils.configureEndPoint(verificaPort, getEndPoint());
        WSUtils.configureSecurityHeaders(verificaPort, propertyPlaceholder);
        PlatinoProxy.setProxy(verificaPort, propertyPlaceholder);
    }

	@Override
	public boolean isConfigured() {
		return hasConnection();
	}

	@Override
	public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de SVD ha sido inyectado con Platino y está operativo.");
		else
			play.Logger.info("El servicio de SVD ha sido inyectado con Platino y NO está operativo.");
	}


	@Override
	public void crearPeticion(PeticionSVDFAP peticion, List<SolicitudGenerica> solicitudes) {
		peticion.rellenarSolicitud(solicitudes);
	}

	@Override
	public Respuesta enviarPeticionSincrona(PeticionSVDFAP peticion) throws VerificarDatosServiceException {
		try {
			PeticionSincrona peticionPlatino = peticionSincronaFAPToPeticionSincronaPlatino(peticion);
			return verificaPort.peticionSincrona(peticionPlatino);
		}
		catch (Exception e) {
			System.out.println("No se ha podido enviar la petición Síncrona. Causa: " + e);
			throw new VerificarDatosServiceException("Error al realizar la petición síncrona");
		}
	}

	@Override
	public Respuesta enviarPeticionAsincrona(PeticionSVDFAP peticion) throws VerificarDatosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Respuesta solicitarRespuestaAsincrona(String idRespuesta) throws VerificarDatosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RespuestaPdf generarPDFRespuesta() throws VerificarDatosServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Respuesta peticionRecover() throws VerificarDatosServiceException {
		// TODO Auto-generated method stub
		return null;
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

	private PeticionSincrona peticionSincronaFAPToPeticionSincronaPlatino(PeticionSVDFAP peticion) {

		PeticionSincrona peticionPlatino = new PeticionSincrona();

		//Atributos
		AtributosSVDFAP atributos = peticion.atributos;
		String codigoCertificado = atributos.getCodigoCertificado();
		es.gobcan.platino.servicios.svd.peticionatributos.Atributos atributosPlatino = setAtributosPlatino(codigoCertificado);

		//Solicitudes
		SolicitudesIdResi solicitudesPlatino = new SolicitudesIdResi();
		SolicitudTransmisionIdResi solicitudTransmision = solicitudTransmisionFAPToSolicitudTransmisionPlatino(peticion, peticion.solicitudesTransmision.get(0));
		solicitudesPlatino.getSolicitudTransmision().add(solicitudTransmision);

		peticionPlatino.setAtributos(atributosPlatino);
		peticionPlatino.setSolicitudes(solicitudesPlatino);
		peticionPlatino.setUidUsuario(peticion.uidUsuario);

		return peticionPlatino;
	}

	private SolicitudTransmisionIdResi solicitudTransmisionFAPToSolicitudTransmisionPlatino(PeticionSVDFAP peticion, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {

		SolicitudTransmisionIdResi solicitudTransmisionPlatino = new SolicitudTransmisionIdResi();
		DatosGenericos datosGenericos = new DatosGenericos();
		Titularpet titular = new Titularpet();

		datosGenericos.setSolicitante(setSolicitante(peticion, solicitudTransmisionSVDFAP.datosGenericos));
		datosGenericos.setTitular(titular);

		return solicitudTransmisionPlatino;
	}

	private es.gobcan.platino.servicios.svd.peticionatributos.Atributos setAtributosPlatino (String codigoCertificado){
		es.gobcan.platino.servicios.svd.peticionatributos.Atributos atributos = new es.gobcan.platino.servicios.svd.peticionatributos.Atributos();
		atributos.setCodigoCertificado(codigoCertificado);

		return atributos;
	}

	private Solicitante setSolicitante (PeticionSVDFAP peticion, DatosGenericosPeticionSVDFAP datosGenericos) {

		Solicitante solicitante = new Solicitante();
		solicitante.setIdentificadorSolicitante(datosGenericos.getSolicitante().getIdentificadorSolicitante());
		solicitante.setNombreSolicitante(datosGenericos.getSolicitante().getNombreSolicitante());
		solicitante.setFinalidad(datosGenericos.getSolicitante().getFinalidad());
		solicitante.setUnidadTramitadora(datosGenericos.getSolicitante().getUnidadTramitadora());
		solicitante.setIdExpediente(datosGenericos.getSolicitante().getIdExpediente());

		solicitante.setProcedimiento(setProcedimiento(peticion.atributos.getCodigoCertificado(), "motivo petición"));
		solicitante.setFuncionario(setFuncionario(datosGenericos.getSolicitante().funcionario.nombreCompletoFuncionario, datosGenericos.getSolicitante().funcionario.nifFuncionario));

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
		funcionario.setNifFuncionario(nif);

		return funcionario;
	}

}
