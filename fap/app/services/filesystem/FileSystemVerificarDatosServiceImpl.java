package services.filesystem;

import play.modules.guice.InjectSupport;
import properties.PropertyPlaceholder;
import services.VerificarDatosServiceException;
import es.gobcan.platino.servicios.svd.Atributos;
import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.RespuestaPdf;
import es.gobcan.platino.servicios.svd.ScspwsService;
import es.gobcan.platino.servicios.svd.Solicitudes;
import es.gobcan.platino.servicios.svd.peticionpeticionpdf.PeticionPdf;
import es.gobcan.platino.servicios.svd.peticionpeticionrecover.PeticionRecover;
import es.gobcan.platino.servicios.svd.peticionpeticionsincrona.PeticionSincrona;
import es.gobcan.platino.servicios.svd.solicitudrespuestasolicitudrespuesta.SolicitudRespuesta;

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

		@Override
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

		public Respuesta peticionAsincronaIdentidad(Solicitudes solicitud, String codigoCertificado, String uidUsuario, String idSolicitante, String nombreSolicitante,
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
