package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import messages.Messages;
import messages.Messages.MessageType;
import models.Agente;
import models.DatosEspecificosRespuestaSVDFAP;
import models.DatosGenericosPeticionSVDFAP;
import models.DatosGenericosRespuestaSVDFAP;
import models.NacimientoSVDFAP;
import models.ParametroSVD;
import models.ParametrosServicio;
import models.PeticionSVDFAP;
import models.ResidenciaSVDFAP;
import models.SolicitudEspecificaSVDFAP;
import models.SolicitudGenerica;
import models.SolicitudTransmisionSVDFAP;
import models.TableKeyValue;
import models.TitularSVDFAP;
import models.TransmisionDatosRespuestaSVDFAP;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import play.mvc.Util;
import controllers.fap.AgenteController;
import es.gobcan.platino.servicios.svd.Nacimiento;
import es.gobcan.platino.servicios.svd.Residencia;
import es.gobcan.platino.servicios.svd.Respuesta;
import es.gobcan.platino.servicios.svd.SolicitanteDatos;
import es.gobcan.platino.servicios.svd.Solicitud;
import es.gobcan.platino.servicios.svd.SolicitudTransmision;
import es.gobcan.platino.servicios.svd.Solicitudes;
import es.gobcan.platino.servicios.svd.TransmisionDatos;
import es.gobcan.platino.servicios.svd.Transmisiones;
import es.gobcan.platino.servicios.svd.peticionatributos.Atributos;
import es.gobcan.platino.servicios.svd.peticionconsentimiento.Consentimiento;
import es.gobcan.platino.servicios.svd.peticiondatosgenericos.DatosGenericos;
import es.gobcan.platino.servicios.svd.peticionfuncionario.Funcionario;
import es.gobcan.platino.servicios.svd.peticionpdresiespanol.Espanol;
import es.gobcan.platino.servicios.svd.peticionpeticionasincrona.PeticionAsincrona;
import es.gobcan.platino.servicios.svd.peticionpeticionsincrona.PeticionSincrona;
import es.gobcan.platino.servicios.svd.peticionprocedimiento.Procedimiento;
import es.gobcan.platino.servicios.svd.peticionsolicitante.Solicitante;
import es.gobcan.platino.servicios.svd.peticiontipodocumentacion.TipoDocumentacion;
import es.gobcan.platino.servicios.svd.peticiontitularpet.Titularpet;

public class SVDUtils {

	//Parsea una Petición Síncrona de FAP a una Petición Síncrona de Platino
	public static PeticionSincrona peticionSincronaFAPToPeticionSincronaPlatino(PeticionSVDFAP peticion) {

		PeticionSincrona peticionPlatino = new PeticionSincrona();

		//uidUsuario y NifFuncionario
		peticionPlatino.setUidUsuario(peticion.uidUsuario);
		String nifFuncionario = ParametroSVD.find("select valor from ParametroSVD parametroSVD where clave=?", "nifFuncionario").first();
		peticionPlatino.setNifFuncionario(nifFuncionario);

		//Atributos
		es.gobcan.platino.servicios.svd.peticionatributos.Atributos atributosPlatino = setAtributosPlatino(peticion);
		peticionPlatino.setAtributos(atributosPlatino);

		//Solicitudes
		Solicitudes solicitudesPlatino = new Solicitudes();
		SolicitudTransmision solicitudTransmision = solicitudTransmisionFAPToSolicitudTransmisionPlatino(peticion, peticion.solicitudesTransmision.get(0));
		solicitudesPlatino.getSolicitudTransmision().add(solicitudTransmision);

		peticionPlatino.setSolicitudes(solicitudesPlatino);

		return peticionPlatino;
	}

	//TODO: Unificar los métodos de parseo de Petición Síncrona y Asíncrona para optimizar el código
	//Parsea una Petición Asíncrona de FAP a una Petición Asíncrona de Platino
	public static PeticionAsincrona peticionAsincronaFAPToPeticionAsincronaPlatino(PeticionSVDFAP peticion) {

		PeticionAsincrona peticionPlatino = new PeticionAsincrona();

		//uidUsuario y NifFuncionario
		peticionPlatino.setUidUsuario(peticion.uidUsuario);

		String nifFuncionario = ParametroSVD.find("select valor from ParametroSVD parametroSVD where clave=?", "nifFuncionario").first();
		peticionPlatino.setNifFuncionario(nifFuncionario);

		//Atributos
		es.gobcan.platino.servicios.svd.peticionatributos.Atributos atributosPlatino = setAtributosPlatino(peticion);
		peticionPlatino.setAtributos(atributosPlatino);

		//Solicitudes
		Solicitudes solicitudesPlatino = new Solicitudes();
		SolicitudTransmision solicitudTransmision = solicitudTransmisionFAPToSolicitudTransmisionPlatino(peticion, peticion.solicitudesTransmision.get(0));
		solicitudesPlatino.getSolicitudTransmision().add(solicitudTransmision);

		peticionPlatino.setSolicitudes(solicitudesPlatino);

		return peticionPlatino;

	}

	//Mapeo de una solicitud de transmision de FAP a una de Platino (de Identidad o de Residencia)
	public static SolicitudTransmision solicitudTransmisionFAPToSolicitudTransmisionPlatino(PeticionSVDFAP peticion, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {

		SolicitudTransmision solicitudTransmisionPlatino = new SolicitudTransmision();

		//Datos Genericos
		DatosGenericos datosGenericos = new DatosGenericos();
		datosGenericos.setSolicitante(setSolicitante(peticion, solicitudTransmisionSVDFAP.datosGenericos));
		datosGenericos.setTitular(setTitular(solicitudTransmisionSVDFAP.datosGenericos.titular));
		solicitudTransmisionPlatino.setDatosGenericos(datosGenericos);

		return solicitudTransmisionPlatino;
	}

	public static Atributos setAtributosPlatino (PeticionSVDFAP peticionSVDFAP){

		Atributos atributos = new Atributos();
		if (peticionSVDFAP.getAtributos() != null) {
			if (peticionSVDFAP.getAtributos().getCodigoCertificado() != null) {
				atributos.setCodigoCertificado(peticionSVDFAP.getAtributos().getCodigoCertificado());
			}
		}

		return atributos;
	}

	//Parseo de Atributos de FAP a Atributos Platino para Solicitud de Respuesta
	public static es.gobcan.platino.servicios.svd.solicitudrespuestaatributos.Atributos setAtributosSolicitudRespuestaPlatino (PeticionSVDFAP peticionSVDFAP) {

		es.gobcan.platino.servicios.svd.solicitudrespuestaatributos.Atributos atributos = new es.gobcan.platino.servicios.svd.solicitudrespuestaatributos.Atributos();
		if (peticionSVDFAP.getAtributos() != null) {
			if (peticionSVDFAP.getAtributos().getIdPeticion() != null)
				atributos.setIdPeticion(peticionSVDFAP.getAtributos().getIdPeticion());
			if (peticionSVDFAP.getAtributos().getCodigoCertificado() != null)
				atributos.setCodigoCertificado(peticionSVDFAP.getAtributos().getCodigoCertificado());
			if (peticionSVDFAP.getAtributos().getNumElementos() != null)
				atributos.setNumElementos(peticionSVDFAP.getAtributos().getNumElementos());
		}

		return atributos;
	}


	public static Titularpet setTitular (TitularSVDFAP titularSVDFAP){

		Titularpet titular = new Titularpet();

		if (titularSVDFAP.getDocumentacion() != null)
			titular.setDocumentacion(titularSVDFAP.getDocumentacion());
		else
			titular.setDocumentacion("");

		if (titularSVDFAP.getNombreCompleto() != null)
			titular.setNombreCompleto(titularSVDFAP.getNombreCompleto());
		else
			titular.setNombreCompleto("");

		if (titularSVDFAP.getNombre() != null)
			titular.setNombre(titularSVDFAP.getNombre());
		else
			titular.setNombre("");

		if (titularSVDFAP.getApellido1() != null)
			titular.setApellido1(titularSVDFAP.getApellido1());
		else
			titular.setApellido1("");

		if (titularSVDFAP.getApellido2() != null)
			titular.setApellido2(titularSVDFAP.getApellido2());
		else
			titular.setApellido2("");

		titular.setTipoDocumentacion(setTipoDocumentacion(titularSVDFAP.getTipoDocumentacion()));

		return titular;
	}

	public static TipoDocumentacion setTipoDocumentacion (String tipoDocumentacionSVDFAP) {

		TipoDocumentacion tipoDocumentacion = null;

		switch(tipoDocumentacionSVDFAP) {
			case "nif": {
							tipoDocumentacion = tipoDocumentacion.NIF;
							break;
						}
			case "cif": {
							tipoDocumentacion = tipoDocumentacion.CIF;
							break;
						}
			case "dni": {
							tipoDocumentacion = tipoDocumentacion.DNI;
							break;
						}
			case "pasaporte": {
									tipoDocumentacion = tipoDocumentacion.PASAPORTE;
									break;
								}
			case "nie": {
							tipoDocumentacion = tipoDocumentacion.NIE;
							break;
						}
		}

		return tipoDocumentacion;

	}

	public static Solicitante setSolicitante (PeticionSVDFAP peticion, DatosGenericosPeticionSVDFAP datosGenericos) {

		Solicitante solicitante = new Solicitante();
		solicitante.setIdentificadorSolicitante(datosGenericos.getSolicitante().getIdentificadorSolicitante());
		solicitante.setNombreSolicitante(datosGenericos.getSolicitante().getNombreSolicitante());
		solicitante.setFinalidad(datosGenericos.getSolicitante().getFinalidad());
		solicitante.setUnidadTramitadora(datosGenericos.getSolicitante().getUnidadTramitadora());
		solicitante.setIdExpediente(datosGenericos.getSolicitante().getIdExpediente());
		solicitante.setConsentimiento(setConsentimiento(datosGenericos.getSolicitante().getConsentimiento()));
		solicitante.setProcedimiento(setProcedimiento(datosGenericos.getSolicitante().getProcedimiento().getCodigoProcedimiento(), datosGenericos.getSolicitante().getProcedimiento().getNombreProcedimiento()));
		solicitante.setFuncionario(setFuncionario(datosGenericos.getSolicitante().getFuncionario().getNombreCompletoFuncionario()));

		return solicitante;
	}

	public static Consentimiento setConsentimiento(String consentimientoSVDFAP) {

		Consentimiento consentimiento = null;
		switch(consentimientoSVDFAP) {
			case "Ley": consentimiento = consentimiento.LEY;
			case "Si": consentimiento = consentimiento.SI;
		}

		return consentimiento;
	}

	public static Procedimiento setProcedimiento (String codigoProcedimiento, String nombreProcedimiento){

		Procedimiento procedimiento = new Procedimiento();
		procedimiento.setCodProcedimiento(codigoProcedimiento);
		procedimiento.setNombreProcedimiento(nombreProcedimiento);

		return procedimiento;
	}

	public static Funcionario setFuncionario (String nombreCompletoFuncionario){

		Funcionario funcionario = new Funcionario();
		funcionario.setNombreCompletoFuncionario(nombreCompletoFuncionario);

		return funcionario;
	}

	public static SolicitanteDatos setSolicitanteDatos (String tipoSolicitante){

		SolicitanteDatos solicitanteDatos = new SolicitanteDatos();
		solicitanteDatos.setTipo(tipoSolicitante);

		return solicitanteDatos;
	}

	public static Solicitud setSolicitud (SolicitudEspecificaSVDFAP solicitudSVDFAP){

		Solicitud solicitud = new Solicitud();
		solicitud.setResidencia(setResidencia(solicitudSVDFAP.residencia));
		solicitud.setNacimiento(setNacimiento(solicitudSVDFAP.solicitudNacimiento));
		solicitud.setEspanol(setEspanol(solicitudSVDFAP));

		return solicitud;
	}

	public static Residencia setResidencia (ResidenciaSVDFAP residenciaSVDFAP){

		Residencia residencia = new Residencia();
		residencia.setMunicipio(residenciaSVDFAP.getMunicipio());
		residencia.setProvincia(residenciaSVDFAP.getProvincia());

		return residencia;
	}

	public static Nacimiento setNacimiento (NacimientoSVDFAP nacimientoSVDFAP){

		Nacimiento nacimiento = new Nacimiento();
		nacimiento.setMunicipio(nacimientoSVDFAP.getMunicipio());
		nacimiento.setProvincia(nacimientoSVDFAP.getProvincia());
		if (nacimientoSVDFAP.getFecha() != null)
			nacimiento.setFecha(nacimientoSVDFAP.getFecha().toString());
		else
			nacimiento.setFecha(null);

		return nacimiento;
	}


	public static Espanol setEspanol (SolicitudEspecificaSVDFAP solicitudEspecificaSVDFAP){

		Espanol espanol = null;

		switch(solicitudEspecificaSVDFAP.espanol) {
			case "s": 	{
							espanol = Espanol.S;
							break;
						}
			case "n": 	{
							espanol = Espanol.N;
							break;
						}
		}

		return espanol;
	}


	public static void respuestaPlatinoToRespuestaFAP(Respuesta respuesta, PeticionSVDFAP peticion) {

		try {

			//Atributos
			peticion.atributos.codigoCertificado = respuesta.getAtributos().getCodigoCertificado();
			peticion.atributos.idPeticion = respuesta.getAtributos().getIdPeticion();
			peticion.atributos.timestamp = respuesta.getAtributos().getTimestamp();
			peticion.atributos.numElementos = respuesta.getAtributos().getNumElementos();

			//Estado
			peticion.atributos.estado.literalError = respuesta.getAtributos().getEstado().getLiteralError();

			//Transmisiones
			Transmisiones transmisiones = respuesta.getTransmisiones();
			int actual = 0;
			for (SolicitudTransmisionSVDFAP solicitudTransmision: peticion.solicitudesTransmision) {

				TransmisionDatos transmisionDatos = transmisiones.getTransmisionDatos().get(actual);

				solicitudTransmision.respuesta = new TransmisionDatosRespuestaSVDFAP();
				setDatosGenericosSVDFAP(solicitudTransmision.respuesta.datosGenericos, transmisionDatos);
				setDatosEspecificosSVDFAP(solicitudTransmision.respuesta.datosEspecificos, transmisionDatos);
				solicitudTransmision.respuesta.fechaRespuesta = parseFechaHora(peticion.atributos.getTimestamp());

				solicitudTransmision.save();
				actual++;
			}

			if (!Messages.hasErrors()) {
				peticion.save();
			}

		} catch (Exception ex){
			play.Logger.error("Se ha producido un error parseando la respuesta de Platino a FAP");
		}

	}


	public static DatosGenericosRespuestaSVDFAP setDatosGenericosSVDFAP(DatosGenericosRespuestaSVDFAP datosGenericos, TransmisionDatos transmisionDatos) {

		//Emisor
		datosGenericos.emisor.nif = transmisionDatos.getDatosGenericos().getEmisor().getNifEmisor();
		datosGenericos.emisor.nombreEmisor = transmisionDatos.getDatosGenericos().getEmisor().getNombreEmisor();

		//Solicitante
		datosGenericos.solicitante.identificadorSolicitante = transmisionDatos.getDatosGenericos().getSolicitante().getIdentificadorSolicitante();
		datosGenericos.solicitante.nombreSolicitante = SVDUtils.convertToUTF8(transmisionDatos.getDatosGenericos().getSolicitante().getNombreSolicitante());
		datosGenericos.solicitante.finalidad = SVDUtils.convertToUTF8(transmisionDatos.getDatosGenericos().getSolicitante().getFinalidad());
		datosGenericos.solicitante.consentimiento = transmisionDatos.getDatosGenericos().getSolicitante().getConsentimiento().toString();


		//Funcionario

		datosGenericos.solicitante.funcionario.nombreCompletoFuncionario = SVDUtils.convertToUTF8(transmisionDatos.getDatosGenericos().getSolicitante().getFuncionario().getNombreCompletoFuncionario());

		//Titular
		datosGenericos.titular.documentacion = transmisionDatos.getDatosGenericos().getTitular().getDocumentacion();
		datosGenericos.titular.apellido1 = SVDUtils.convertToUTF8(transmisionDatos.getDatosGenericos().getTitular().getApellido1());
		datosGenericos.titular.apellido2 = SVDUtils.convertToUTF8(transmisionDatos.getDatosGenericos().getTitular().getApellido2());
		datosGenericos.titular.nombreCompleto = SVDUtils.convertToUTF8(transmisionDatos.getDatosGenericos().getTitular().getNombreCompleto());
		datosGenericos.titular.tipoDocumentacion = transmisionDatos.getDatosGenericos().getTitular().getTipoDocumentacion().toString();

		//Transmision
		datosGenericos.transmision.codigoCertificado = transmisionDatos.getDatosGenericos().getTransmision().getCodigoCertificado();
		datosGenericos.transmision.idSolicitud = transmisionDatos.getDatosGenericos().getTransmision().getIdSolicitud();
		datosGenericos.transmision.idTransmision = transmisionDatos.getDatosGenericos().getTransmision().getIdTransmision();
		//convertir a DateTime
		datosGenericos.transmision.fechaGeneracion = SVDUtils.parseFechaHora(transmisionDatos.getDatosGenericos().getTransmision().getFechaGeneracion());

		return datosGenericos;
	}

	public static DatosEspecificosRespuestaSVDFAP setDatosEspecificosSVDFAP(DatosEspecificosRespuestaSVDFAP datosEspecificos, TransmisionDatos transmisionDatos) {

		//Estado
		datosEspecificos.estado.literalError = transmisionDatos.getDatosEspecificos().getEstado().getLiteralError();
		datosEspecificos.estadoResultado.literalError = transmisionDatos.getDatosEspecificos().getEstadoResultado().getLiteralError();

		//Domicilio
		if (transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDomicilio() != null) {
			datosEspecificos.domicilio.provincia = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDomicilio().getProvinciaRespuesta().getCodigo();
			datosEspecificos.domicilio.municipio = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDomicilio().getMunicipioRespuesta().getCodigo();
			datosEspecificos.domicilio.entColectiva.codigo = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDomicilio().getEntColectiva().getCodigo();
			datosEspecificos.domicilio.entSingular.codigo = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDomicilio().getEntSingular().getCodigo();
			datosEspecificos.domicilio.nucleo.codigo = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDomicilio().getNucleo().getCodigo();
			datosEspecificos.domicilio.direccion.via.nombre = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDomicilio().getDireccion().getVia().getNombre();
			datosEspecificos.domicilio.direccion.numero.valor = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDomicilio().getDireccion().getNumero().getValor();
			datosEspecificos.domicilio.direccion.numeroSuperior.valor = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDomicilio().getDireccion().getNumeroSuperior().getValor();
		}

		//Titular
		if (transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular() != null) {
			datosEspecificos.datosTitular.identificador = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getIdentificador();
			datosEspecificos.datosTitular.numeroSoporte = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getNumSoporte();
			datosEspecificos.datosTitular.nombre = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getNombre();
			datosEspecificos.datosTitular.nacionalidad = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getNacionalidad();
			datosEspecificos.datosTitular.apellido1 = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getApellido1();
			datosEspecificos.datosTitular.apellido2 = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getApellido2();
			datosEspecificos.datosTitular.nombreMadre = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getNomMadre();
			datosEspecificos.datosTitular.nombrePadre = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getNomPadre();

			datosEspecificos.datosTitular.fechacaducidad = SVDUtils.parseFecha(transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getFechaCaducidad());
			datosEspecificos.datosTitular.sexo.nombre = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getSexo().toString();

			//Datos Nacimiento
			if (transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getDatosNacimiento() != null) {
				datosEspecificos.datosTitular.datosNacimiento.fecha = SVDUtils.parseFecha(transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getDatosNacimiento().getFechaNacimiento());
				datosEspecificos.datosTitular.datosNacimiento.municipio = SVDUtils.parseLugar(transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getDatosNacimiento().getLocalidad(), "municipios");
				datosEspecificos.datosTitular.datosNacimiento.provincia = SVDUtils.parseLugar(transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getDatosNacimiento().getProvincia(), "provincias");
			}
			//Datos Direccion
			if (transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getDatosDireccion() != null) {
				datosEspecificos.datosTitular.datosDireccion.localidad = SVDUtils.parseLugar(transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getDatosDireccion().getLocalidad(), "municipios");
				datosEspecificos.datosTitular.datosDireccion.provincia = SVDUtils.parseLugar(transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getDatosDireccion().getProvincia(), "provincias");
				datosEspecificos.datosTitular.datosDireccion.datosVia.nombre = transmisionDatos.getDatosEspecificos().getDatosEspecificosIdResi().getDatosTitular().getDatosDireccion().getDatosVia();
			}
		}

		return datosEspecificos;
	}

	//Parser de fecha en formato yyyy-MM-ddTHH:mm:ss.msmsms+HH:mm
	public static DateTime parseFechaHora (String fecha) {

		if (!fecha.equals("")) {
			try {
				DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
				String date = fecha.substring(0, fecha.indexOf('T'));
				String time = fecha.substring(fecha.indexOf('T')+1, fecha.indexOf('.'));
				DateTime dateTime = formatter.parseDateTime(date + " " + time);

				return dateTime;
			} catch (Exception e) {
				play.Logger.error("Se ha producido un error parseando la fecha: " + e);
			}
		}

		return null;
	}

	//Parser de fecha en formato yyyyMMdd
	public static DateTime parseFecha (String fecha) {

		if (!fecha.equals("")) {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			try {
				Date date = format.parse(fecha);
				DateTime dateTime = new DateTime(date);
				return dateTime;
			} catch (Exception e) {
				play.Logger.error("Se ha producido un error parseando la fecha de nacimiento: " + e);
			}
		}

		return null;
	}


	//Parser de Municipios, Localidades, Provincias...
	public static String parseLugar (String lugar, String tipo) {

		String codigo = TableKeyValue.find("select key from TableKeyValue t where t.value=? AND t.table=?", lugar, tipo).first();

		return codigo;
	}

	public static String convertToUTF8 (String string) {

		String out = null;

        try {
            out = new String(string.getBytes("UTF-8"), "ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }

        return out;
    }


	//MÉTODO CREAR

	public static SolicitudTransmisionSVDFAP crearSolicitudTransmisionSVDFAP(String tipoServicio, Long idSolicitud) {

		SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP = new SolicitudTransmisionSVDFAP();
		crearLogica(tipoServicio, idSolicitud, solicitudTransmisionSVDFAP);

		return solicitudTransmisionSVDFAP;
	}

	@Util
	public static void crearLogica(String tipoServicio, Long idSolicitud, SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP) {

		SolicitudGenerica solicitud = getSolicitud(idSolicitud);

		Agente agente = AgenteController.getAgente();

		if (!Messages.hasErrors()) {

			solicitudTransmisionSVDFAP.solicitud = solicitud;
			solicitudTransmisionSVDFAP.nombreServicio = tipoServicio;

			//Se asigna la documentación del titular directamente de la solicitud
			//Se pasa provisionalmente para testeo el NIF que aparece en la documentación de Platino
//			solicitudTransmisionSVDFAP.datosGenericos.titular.documentacion = solicitud.solicitante.numeroId;
			solicitudTransmisionSVDFAP.datosGenericos.titular.documentacion = "77779221Y";
			solicitudTransmisionSVDFAP.datosGenericos.titular.tipoDocumentacion = solicitud.solicitante.fisica.nip.tipo;
			solicitudTransmisionSVDFAP.datosGenericos.solicitante.identificadorSolicitante = ParametroSVD.find("select valor from ParametroSVD parametroSVD where clave=?", "identificadorSolicitante").first();
			solicitudTransmisionSVDFAP.datosGenericos.solicitante.nombreSolicitante = ParametroSVD.find("select valor from ParametroSVD parametroSVD where clave=?", "nombreSolicitante").first();
			solicitudTransmisionSVDFAP.datosGenericos.solicitante.finalidad = ParametroSVD.find("select valor from ParametroSVD parametroSVD where clave=?", "finalidad").first();
			solicitudTransmisionSVDFAP.datosGenericos.solicitante.unidadTramitadora = ParametroSVD.find("select valor from ParametroSVD parametroSVD where clave=?", "unidadTramitadora").first();
			solicitudTransmisionSVDFAP.datosGenericos.solicitante.procedimiento.codigoProcedimiento = ParametroSVD.find("select valor from ParametroSVD parametroSVD where clave=?", "codProcedimiento").first();
			solicitudTransmisionSVDFAP.datosGenericos.solicitante.procedimiento.nombreProcedimiento = ParametroSVD.find("select valor from ParametroSVD parametroSVD where clave=?", "nombreProcedimiento").first();

			//CONSULTAR idExpediente
			//Provisional idExpediente de documentación de Platino
//			solicitudTransmisionSVDFAP.datosGenericos.solicitante.idExpediente="EXP22/05/2012";
			solicitudTransmisionSVDFAP.datosGenericos.solicitante.idExpediente = solicitud.expedienteAed.idAed;

			//Comprobar si existe Consentimiento por Ley o si el Solicitante ha autorizado
			String consentimiento = null;
			if (ParametrosServicio.find("select consentimientoLey from ParametrosServicio parametrosServicio where nombreServicio=?", "residencia").first())
				consentimiento = "Ley";

			SolicitudGenerica solicitudAutorizada = null;

			if (tipoServicio == "identidad")
				solicitudAutorizada = SolicitudGenerica.find(	"select solicitud from SolicitudGenerica solicitud, "+
																"Cesion cesion, AutorizacionCesion autorizacionCesion " +
																"where (solicitud.cesion = cesion.autorizacionCesion) " +
																" and (autorizacionCesion = cesion.autorizacionCesion) " +
																" and (autorizacionCesion.identidad=?)", true).first();
			else if (tipoServicio == "residencia")
				solicitudAutorizada = SolicitudGenerica.find(	"select solicitud from SolicitudGenerica solicitud, "+
																"Cesion cesion, AutorizacionCesion autorizacionCesion " +
																"where (solicitud.cesion = cesion.autorizacionCesion) " +
																" and (autorizacionCesion = cesion.autorizacionCesion) " +
																" and (autorizacionCesion.residencia=?)", true).first();

			if (solicitudAutorizada != null)
				consentimiento = "Si";

			solicitudTransmisionSVDFAP.datosGenericos.solicitante.consentimiento = consentimiento;

			//PROVISIONAL PRUEBAS, CAMBIAR POR AGENTE ACTUAL
//			solicitudTransmisionSVDFAP.datosGenericos.solicitante.funcionario.nombreCompletoFuncionario = agente.name;
			solicitudTransmisionSVDFAP.datosGenericos.solicitante.funcionario.nombreCompletoFuncionario = "Daniel";

			solicitudTransmisionSVDFAP.save();
		}

	}


	@Util
	public static SolicitudTransmisionSVDFAP getSolicitudTransmisionSVDFAP(Long idSolicitudTransmisionSVDFAP) {
		SolicitudTransmisionSVDFAP solicitudTransmisionSVDFAP = null;
		if (idSolicitudTransmisionSVDFAP == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitudTransmisionSVDFAP"))
				Messages.fatal("Falta parámetro idSolicitudTransmisionSVDFAP");
		} else {
			solicitudTransmisionSVDFAP = SolicitudTransmisionSVDFAP.findById(idSolicitudTransmisionSVDFAP);
			if (solicitudTransmisionSVDFAP == null) {
				Messages.fatal("Error al recuperar SolicitudTransmisionSVDFAP");
			}
		}
		return solicitudTransmisionSVDFAP;
	}


	@Util
	public static SolicitudTransmisionSVDFAP getSolicitudTransmisionSVDFAP() {
		return new SolicitudTransmisionSVDFAP();
	}

	//Obtiene solicitud Generica a partir del id
	@Util
	public static SolicitudGenerica getSolicitud(Long idSolicitud) {
		SolicitudGenerica solicitud = null;
		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		} else {
			solicitud = SolicitudGenerica.findById(idSolicitud);
			if (solicitud == null) {
				Messages.fatal("Error al recuperar SolicitudTransmisionSVDFAP");
			}
		}
		return solicitud;
	}

}
