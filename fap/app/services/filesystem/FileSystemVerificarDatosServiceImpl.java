package services.filesystem;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.xml.ws.soap.MTOMFeature;

import es.gobcan.platino.servicios.svd.*;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import platino.PlatinoProxy;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import es.gobcan.platino.servicios.edmyce.dominio.comun.ArrayOfCorreoElectronicoType;
import es.gobcan.platino.servicios.edmyce.dominio.comun.ArrayOfUriRemesaType;
import es.gobcan.platino.servicios.edmyce.dominio.comun.CanalMensajeEnumType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.ArrayOfMensajeOficioType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.ArrayOfMensajeType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.MensajeAreaType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.MensajeCriteriaType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.MensajeOficioType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.MensajeType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.RemesaType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.ResultadoBusquedaMensajeType;
import es.gobcan.platino.servicios.edmyce.mensajes.*;
import es.gobcan.platino.servicios.registro.Registro_Service;
import es.gobcan.platino.servicios.svd.peticionpeticionpdf.PeticionPdf;
import es.gobcan.platino.servicios.svd.peticionpeticionrecover.PeticionRecover;
import es.gobcan.platino.servicios.svd.peticionpeticionsincrona.PeticionSincrona;
import es.gobcan.platino.servicios.svd.solicitudrespuestasolicitudrespuesta.SolicitudRespuesta;
import services.MensajeServiceException;
import services.VerificarDatosServiceException;
import utils.WSUtils;

@InjectSupport
public class FileSystemVerificarDatosServiceImpl implements services.VerificarDatosService {

	private PropertyPlaceholder propertyPlaceholder;
	
	private ScspwsService verificaPort;
    
    @Override
    public boolean isConfigured() {
        //No necesita configuración
        return true;
    }
	
	 @Override
	 public void mostrarInfoInyeccion() {
		 if (isConfigured())
			play.Logger.info("El servicio de SVD ha sido inyectado con FileSystem y está operativo.");
		else
			play.Logger.info("El servicio de SVD ha sido inyectado con FileSystem y NO está operativo.");
	 }
	 
	 @Override
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
		
	 @Override
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
		
	 @Override
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
	 
		public Respuesta peticionSincronaIdentidad(String codigoCertificado, String uidUsuario, String idSolicitante, String nombreSolicitante, 
				String finalidad, String idExpediente, String unidadTramitadora, String codigoProc, String nombreProc, String nombreCompletoFuncionario, String nif, 
				String valorConsentimiento, String documentacion, String nombreCompleto, String nombre, String apellido1, String apellido2, String tipoDoc) throws VerificarDatosServiceException{
			
			PeticionSincrona peticion = new PeticionSincrona();
			peticion.setUidUsuario(uidUsuario);
			//peticion.setAtributos(setAtributos(codigoCertificado));
			//peticion.setSolicitudes(setSolicitud(idSolicitante, nombreSolicitante, unidadTramitadora, codigoProc, nombreProc, nombreCompletoFuncionario, 
			//		nif, valorConsentimiento, documentacion, nombreCompleto, tipoDoc));
			try{
				return verificaPort.peticionSincrona(peticion);
			}
			catch (Exception e){
				System.out.println("No se ha podido realizar la petición. Causa: " + e);
				throw new VerificarDatosServiceException("Error al realizar la petición sincrona");
			}
		}
		
		public Respuesta peticionAsincronaIdentidad(SolicitudesIdResi solicitud, String codigoCertificado, String uidUsuario, String idSolicitante, String nombreSolicitante,
				String finalidad, String idExpediente, String unidadTramitadora, String codigoProc, String nombreProc, String nombreCompletoFuncionario, String nif, 
				String valorConsentimiento, String documentacion, String nombreCompleto, String nombre, String apellido1, String apellido2, String tipoDoc) throws VerificarDatosServiceException{
			
			PeticionSincrona peticion = new PeticionSincrona();
			peticion.setUidUsuario(uidUsuario);
//			peticion.setAtributos(setAtributos(codigoCertificado));
//			peticion.setSolicitudes(solicitud);
			try{
				return verificaPort.peticionSincrona(peticion);
			}
			catch (Exception e){
				System.out.println("No se ha podido realizar la petición. Causa: " + e);
				throw new VerificarDatosServiceException("Error al realizar la petición sincrona");
			}
		}
	
}
