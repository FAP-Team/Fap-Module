package peticionCesion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;

import org.joda.time.DateTime;

import properties.FapProperties;

import reports.Report;
import services.FirmaService;
import services.FirmaServiceException;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import validation.CifCheck;
import config.InjectorConfig;
import enumerado.fap.gen.CodigoCertEnum;
import enumerado.fap.gen.EstadosPeticionEnum;
import enumerado.fap.gen.ListaEstadosEnum;
import enumerado.fap.gen.ListaOrigenEnum;

import messages.Messages;
import models.CesionPDF;
import models.Cesiones;
import models.Documento;
import models.PeticionCesiones;
import models.SolicitudGenerica;

public class PeticionAEAT extends PeticionBase{
	static final int sinContenidoCte = 21;
	static final int datosCte = 20;
	static final int nombreCompletoCte = 50;
	
	static final int iniID = 0;
	static final int finID = 8;
	static final int iniNombre = 9;
	static final int iniIDENT = 59;
	static final int iniCERT = 60;
	static final int iniNEGAT = 61; 
	static final int iniFecha = 63;
	static final int mesCte = 2;
	static final int diaCte = 4;
	static final int iniDatosPpios = 69;
	static final int iniReferencia = 89;
	static final int finReferencia = 100;
	static final int neg = 1;
	static final int longitudRegistro = 100;
	
	private final static String BODY_REPORT = "reports/bodyPeticionAEAT.html";
	private final static String HEADER_REPORT = "reports/headerPeticion.html";
	private final static String FOOTER_REPORT = "reports/footer-cesion.html";

	@Override
	public void generarPeticionBase(PeticionCesiones pt, List<Long> idsSeleccionados) {
		File file = null;
		BufferedWriter bw = null;
		try {
			file = File.createTempFile("AEAT"+obtenerFechaFormato(), ".txt");
			bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
		} catch (IOException e) {
			Messages.error("Error generando el fichero de peticion, intentelo de nuevo");
		}
		// Contenido = NDoc+Nombre+sinContenido(21blancos)+DatosPropios(20blancos)
		String contenido = "";
		String sinContenido = String.format("%" + sinContenidoCte + "s", "");
		String datosPropios = String.format("%" + datosCte + "s", "") + "\n";

		for (Long solId : idsSeleccionados) {
			SolicitudGenerica s = SolicitudGenerica.findById(solId);
			String nombreCompleto = s.solicitante.nombreCompleto;
			nombreCompleto += String.format("%"+ (nombreCompletoCte - s.solicitante.nombreCompleto.length()) + "s", "");
			if (s.solicitante.isPersonaFisica()) {
				contenido += s.solicitante.fisica.nip.valor
						+ nombreCompleto.substring(0, nombreCompletoCte)
						+ sinContenido + datosPropios;
			}
			if (s.solicitante.isPersonaJuridica()) {
				contenido += s.solicitante.juridica.cif
						+ nombreCompleto.substring(0, nombreCompletoCte)
						+ sinContenido + datosPropios;
			}
		}
		try {
			bw.write(contenido);
			bw.close();
			Documento doc = new Documento();
        	doc.tipo = FapProperties.get("fap.aed.tiposdocumentos.peticionAEAT");
        	doc.descripcion = "Descripcion Peticion AEAT";
        	gestorDocumentalService.saveDocumentoTemporal(doc, new FileInputStream(file), FapProperties.get("fap.prefijo.peticion.provincia")+" AEAT"+obtenerFechaNombre()+".txt");
        	pt.fichPeticion.tipo = FapProperties.get("fap.aed.tiposdocumentos.peticionAEAT");
        	pt.fichPeticion.uri =  doc.uri; //Almaceno donde está ANTES getAbsolutepath
			pt.estado = EstadosPeticionEnum.creada.name();
			pt.fechaGen=DateTime.now();
			pt.fechaValidez = pt.fechaGen.plusMonths(Integer.parseInt(FapProperties.get("fap.cesiondatos.validezPeticion")));
			pt.save();
			pt.fichPeticion.save();
		} catch (IOException e) {
			Messages.error("Error escribiendo en el fichero de peticion, intentelo de nuevo");
		} catch (GestorDocumentalServiceException e) {
			Messages.error("Error subiendo el fichero de petición al AED");
			play.Logger.info("Error subiendo el fichero de petición al AED");
		}
	}

	@Override
	public void parsearPeticionBase(PeticionCesiones pt, File fich) {
		FileReader fr = null;
		try { //Abrir en modo lectura
			fr = new FileReader (fich);
			BufferedReader br = new BufferedReader(fr);
			String linea = null;
			CesionPDF aeat = new CesionPDF();
			while((linea=br.readLine())!=null){ 
				if (linea.length() != longitudRegistro)
					throw new Exception(new Throwable());
				else{
					aeat.registro.nDocumento = linea.substring(iniID, iniNombre);
					aeat.registro.nombre =  linea.substring(iniNombre, iniIDENT).trim();
					aeat.registro.ident = linea.substring(iniIDENT, iniCERT);
					aeat.registro.cert = linea.substring(iniCERT, iniCERT+1);
					aeat.registro.negat = null;
					String fecha;
					if (aeat.registro.cert.equals(CodigoCertEnum.N.name())){
						aeat.registro.negat = linea.substring(iniNEGAT, iniNEGAT+1);
						fecha = linea.substring(iniFecha, iniDatosPpios); //+neg
						pt.respCesion.fechaGeneracion = obtenerFechaParseada(fecha);
						aeat.registro.datosPropios = linea.substring(iniDatosPpios, iniReferencia);
						aeat.registro.referencia = linea.substring(iniReferencia, finReferencia);
					}else if (aeat.registro.cert.equals(CodigoCertEnum.P.name())){
						fecha = linea.substring(iniFecha, iniDatosPpios);
						pt.respCesion.fechaGeneracion = obtenerFechaParseada(fecha);
						aeat.registro.datosPropios = linea.substring(iniDatosPpios, iniReferencia);
						aeat.registro.referencia = linea.substring(iniReferencia, finReferencia);
					}
					generarPdfAEAT(pt, aeat);
				}
			}	
			fr.close();
		} catch (Exception e) {
			Messages.error("Error parseando el documento de respuesta del AEAT, compruebe que el fichero es correcto");
			play.Logger.info("Error parseando el documento de respuesta del AEAT");
		}
	}

	@Override
	public void aplicarCambiosBase(SolicitudGenerica solicitud, PeticionCesiones pt, Documento doc, String estado) {
		Cesiones cesion = new Cesiones();
		//pt.estado = EstadosPeticionEnum.sinFirmar.name();
		cesion.tipo = pt.tipo;
		cesion.idUnico = Long.toString(pt.id);
		cesion.fechaPeticion = pt.respCesion.fechaGeneracion;
		cesion.fechaValidez = pt.respCesion.fechaGeneracion.plusMonths(Integer.parseInt(FapProperties.get("fap.cesiondatos.validezPeticion")));;
		cesion.origen = ListaOrigenEnum.cesion.name();
		cesion.firmada = false;
		cesion.documento = doc;
		//Estado de la Cesion (positivo, negativo, nodatos, error)
		if (estado.equals("P"))
			cesion.estado = ListaEstadosEnum._01.name();
		else{
			cesion.estado = ListaEstadosEnum._02.name();
		}
		solicitud.cesion.cesiones.add(cesion);
		firmarDocumentoBase(solicitud, cesion);
	}

	@Override
	public void firmarDocumentoBase(SolicitudGenerica solicitud, Cesiones cesion) {
		//Firma del documento generado
		FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);
		try {
			firmaService.firmarEnServidor(cesion.documento);
			cesion.firmada = true;
			cesion.save();
			solicitud.save(); 
			play.Logger.info("Aplicados cambios de cesión de datos en la solicitud "+solicitud.id);
		} catch (FirmaServiceException e) {
			// TODO Auto-generated catch block
			play.Logger.error("No se pudo firmar en Servidor: "+e);
		} 	
	}

	private File generarPdfAEAT(PeticionCesiones pt, CesionPDF aeat){
		File report =  new File ("reports/bodyPeticion.html");
		List<SolicitudGenerica> solicitud = getSolicitudes("", aeat.registro.nDocumento);
        if((!Messages.hasErrors()) && (!solicitud.isEmpty())){
        	try {
            	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("pt", pt);
            	for (SolicitudGenerica sol : solicitud) {
            		List<Cesiones> cesionesTipo = Cesiones.find("select cesiones from SolicitudGenerica solicitud join solicitud.cesion.cesiones cesiones where  cesiones.tipo = ? and cesiones.idUnico = ? and solicitud.id = ?", "aeat", pt.id.toString(), sol.id).fetch();
            		if (cesionesTipo.isEmpty()){ //No se han creado cesiones a partir de este fichero -> Crear
	            		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("aeat", aeat);
	            		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", sol);
	                	report = new Report("reports/bodyPeticionAEAT.html").header("reports/headerPeticion.html").footer("reports/footer-cesion.html").renderTmpFile(sol, pt, aeat);
	                	Documento doc = new Documento();
	                	doc.tipo = FapProperties.get("fap.aed.tiposdocumentos.respuestaAEAT");
	                	doc.descripcion = "Descripcion AEAT";
	                	gestorDocumentalService.saveDocumentoTemporal(doc, new FileInputStream(report), "cesionAEAT"+obtenerFechaNombre()+".pdf");
	                	sol.documentacionCesion.documentos.add(doc);
	                	pt.respCesion.fechaActuacionGestor = new DateTime();
	                	pt.respCesion.uri = doc.uri;
	                	aplicarCambiosBase(sol, pt, doc, aeat.registro.cert);
            		}
            		else if (!cesionesTipo.get(0).firmada){ //La solicitud ya dispone de una cesion con este id
            			play.Logger.info("La solicitud "+sol.id+" ya dispone de una cesión creada y no firmada para el fichero de respuesta con identificador "+pt.id+" se procede a firmarla");
            			firmarDocumentoBase(sol, cesionesTipo.get(0)); //Solo debe ser una
            		}
            		else {
            			Messages.info("La Solicitud "+sol.id+" ya dispone de una cesión firmada creada con esta cesión de datos");
            		}
            	}
            } catch (Exception ex2) {
                Messages.error("Error generando el documento pdf: "+ex2.getMessage());
                play.Logger.error("Error generando el documento pdf: "+ex2.getMessage());
            }
        }
        else{
          	Messages.warning("La cesion de datos para "+aeat.registro.nDocumento+", no se corresponde con ninguna solicitud");
          	play.Logger.info("La cesion de datos para "+aeat.registro.nDocumento+", no se corresponde con ninguna solicitud");
        }
        return report;
	}
	
	private static String obtenerFechaFormato() {
		Calendar fecha =  Calendar.getInstance();
		int dia = fecha.get(Calendar.DATE);
		int mes = fecha.get(Calendar.MONTH);
		int anyo = fecha.get(Calendar.YEAR);
		Formatter fmtMes = new Formatter();
		fmtMes.format("%02d", mes);
		Formatter fmtDia = new Formatter();
		fmtDia.format("%02d", dia);
		return fmtDia.toString()+fmtMes.toString()+anyo;
	}

	private static String obtenerFechaNombre() {
		Calendar fecha =  Calendar.getInstance();
		int dia = fecha.get(Calendar.DATE);
		int mes = fecha.get(Calendar.MONTH);
		int anyo = fecha.get(Calendar.YEAR);
		int hora = fecha.get(Calendar.HOUR);
		int min = fecha.get(Calendar.MINUTE);
		Formatter fmtMes = new Formatter();
		fmtMes.format("%02d", mes);
		Formatter fmtDia = new Formatter();
		fmtDia.format("%02d", dia);
		return fmtDia.toString()+fmtMes.toString()+anyo+hora+min;
	}
	
	private static DateTime obtenerFechaParseada(String fecha){
		int anyo = 2000+Integer.parseInt(fecha.substring(0, mesCte));
		int mes =  Integer.parseInt(fecha.substring(mesCte, diaCte));
		int dia =  Integer.parseInt(fecha.substring(diaCte, diaCte+2));
		DateTime fechaRespuesta = new DateTime(anyo, mes, dia, 0, 0); //Hora y Min = 0
		return fechaRespuesta;
	}

	@Override
	public String getTipoId(String dato) {
		StringBuilder texto = new StringBuilder();
		if (CifCheck.validaCif(dato, texto)) //Si es un cif
			return "CIF";
		else{ 
			return "NIP";
		}
	}

	@Override
	public List<SolicitudGenerica> getSolicitudes(String tipo, String id) {
		tipo = getTipoId(id);
		List<SolicitudGenerica> solicitudes = new ArrayList<SolicitudGenerica>();
        if (tipo.equals("NIP")){ //dni, pasaporte,..
        	solicitudes = SolicitudGenerica.find("Select solicitud from Solicitud solicitud where solicitud.solicitante.fisica.nip.valor = ?", id).fetch();
        }
        else if (tipo.equals("CIF")){
        	solicitudes = SolicitudGenerica.find("Select solicitud from Solicitud solicitud where solicitud.solicitante.juridica.cif = ?", id).fetch();
        }
		return solicitudes;
	}
	
	@Override
	public String getBodyReport() {
		return BODY_REPORT;
	}

	@Override
	public String getHeaderReport() {
		return HEADER_REPORT;
	}

	@Override
	public String getFooterReport() {
		return FOOTER_REPORT;
	}
}
