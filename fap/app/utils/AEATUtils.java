package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;

import org.joda.time.DateTime;
import org.omg.CORBA.portable.ValueOutputStream;

import config.InjectorConfig;

import play.modules.pdf.PDF;
import properties.FapProperties;
import reports.Report;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import sun.misc.Regexp;
import validation.CifCheck;
import validation.NipCheck;
import enumerado.fap.gen.CodigoAEATNegatEnum;
import enumerado.fap.gen.CodigoCertEnum;
import enumerado.fap.gen.EstadosPeticionEnum;
import enumerado.fap.gen.ListaEstadosEnum;
import enumerado.fap.gen.ListaOrigenEnum;
import enumerado.fap.gen.TipoDocIdCesionEnum;

import messages.Messages;
import models.AEAT;
import models.Cesiones;
import models.Documento;
import models.Nip;
import models.PeticionCesiones;
import models.SolicitudGenerica;

public class AEATUtils {

	static final int sinContenidoCte = 21;
	static final int datosCte = 20;
	static final int nombreCompletoCte = 50;
	
	private static final String NIF_NIE_ASOCIATION = "TRWAGMYFPDXBNJZSQVHLCKET";
	
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
	
	
	//Inyeccion manual	
	static GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
	
	public static void peticionAEAT(PeticionCesiones pt, List<Long> idsSeleccionados) {
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
        	gestorDocumentalService.saveDocumentoTemporal(doc, new FileInputStream(file), "AEAT"+obtenerFechaNombre()+".txt");
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
		}
	}

	public static void parsearAEAT(PeticionCesiones pt, File fich) {
		FileReader fr = null;
		try { //Abrir en modo lectura
			fr = new FileReader (fich);
			BufferedReader br = new BufferedReader(fr);
			String linea = null;
			AEAT aeat = new AEAT();
			while((linea=br.readLine())!=null){ 
				aeat.nDocumento = linea.substring(iniID, iniNombre);
				aeat.nombre =  linea.substring(iniNombre, iniIDENT).trim();
				aeat.ident = linea.substring(iniIDENT, iniCERT);
				aeat.cert = linea.substring(iniCERT, iniCERT+1);
				aeat.negat = null;
				String fecha;
				
				if (aeat.cert.equals(CodigoCertEnum.N.name())){
					aeat.negat = linea.substring(iniNEGAT, iniNEGAT+1);
					aeat.estadoNegat = CodigoAEATNegatEnum.valueOf(aeat.negat).value();
					fecha = linea.substring(iniFecha+neg, iniDatosPpios+neg);
					pt.respCesion.fechaGeneracion = obtenerFechaParseada(fecha);
					aeat.datosPropios = linea.substring(iniDatosPpios+neg, iniReferencia+neg);
					aeat.referencia = linea.substring(iniReferencia+neg, finReferencia+neg);
				}else if (aeat.cert.equals(CodigoCertEnum.P.name())){
					fecha = linea.substring(iniFecha, iniDatosPpios);
					pt.respCesion.fechaGeneracion = obtenerFechaParseada(fecha);
					aeat.datosPropios = linea.substring(iniDatosPpios, iniReferencia);
					aeat.referencia = linea.substring(iniReferencia, finReferencia);
				}
				generarPdfAEAT(pt, aeat);
			}	
			fr.close();
		} catch (Exception e) {
			Messages.error("Error parseando el documento de respuesta del AEAT, compruebe que el fichero es correcto");
		}
	}

	private static File generarPdfAEAT(PeticionCesiones pt, AEAT aeat){
		File report =  new File ("reports/bodyPeticion.html");
		String tipo = getTipo(aeat.nDocumento);
        //Obtener solicitud correspondiente
        //Tipo dice si es dni, nie, pasaporte, cif
        List<SolicitudGenerica> solicitud = null;
        if (tipo.equals("NIE")){ //dni, pasaporte,..
        	solicitud = SolicitudGenerica.find("Select solicitud from Solicitud solicitud where solicitud.solicitante.fisica.nip.valor = ?", aeat.nDocumento).fetch();
        }
        else if (tipo.equals("CIF")){
        	solicitud = SolicitudGenerica.find("Select solicitud from Solicitud solicitud where solicitud.solicitante.juridica.cif = ?", aeat.nDocumento).fetch();
        }
        if((!Messages.hasErrors()) && (!solicitud.isEmpty())){
        	try {
            	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("pt", pt);
            	for (SolicitudGenerica sol : solicitud) {
            		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("aeat", aeat);
            		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("sol", sol);
                	report = new Report("reports/bodyPeticionAEAT.html").header("reports/headerPeticion.html").footer("reports/footer-cesion.html").renderTmpFile(sol, pt, aeat);
                	Documento doc = new Documento();
                	doc.tipo = FapProperties.get("fap.aed.tiposdocumentos.respuestaAEAT");
                	doc.descripcion = "Descripcion AEAT";
                	gestorDocumentalService.saveDocumentoTemporal(doc, new FileInputStream(report), "cesionAEAT"+obtenerFechaNombre()+".pdf");
                	sol.documentacionCesion.documentos.add(doc);
                	pt.respCesion.fechaActuacionGestor = new DateTime();
                	pt.respCesion.uri = doc.uri;
                	aplicarCambios(sol, pt, doc, aeat);
            	}
            } catch (Exception ex2) {
                Messages.error("Error generando el documento pdf: "+ex2.getMessage());
            }
        }
        else{
          	Messages.info("La cesion de datos para "+aeat.nDocumento+", no se corresponde con ninguna solicitud");
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
	
	private static String getTipo (String numdoc){
		StringBuilder texto = new StringBuilder();
		if (CifCheck.validaCif(numdoc, texto)) //Si es un cif
			return "CIF";
		else if (checkNifNieLetter(numdoc)){ //Si es dni la letra tiene que ser correcta
			return "NIE";
		}
		else
			return "ERROR";	
	}
	
	private static boolean checkNifNieLetter(String numero){
		int digitosNif = Integer.parseInt(numero.substring(0,8));
		int letraEsperada = NIF_NIE_ASOCIATION.charAt(digitosNif % 23); 
		int letraActual = numero.charAt(8);
		return (letraEsperada ==  letraActual);
	}

	private static void aplicarCambios(SolicitudGenerica solicitud, PeticionCesiones pt, Documento doc, AEAT aeat){
		Cesiones cesion = new Cesiones();
		cesion.tipo = pt.tipo;
		cesion.fechaPeticion = pt.respCesion.fechaGeneracion;
		cesion.fechaValidez = pt.fechaValidez;
		cesion.origen = ListaOrigenEnum.cesion.name();
		cesion.documento = doc;
		//cesion.documento.urlDescarga = doc.urlDescarga;
		//Estado de la Cesion (positivo, negativo, nodatos, error)
		if (aeat.cert.equals("P"))
			cesion.estado = ListaEstadosEnum._01.name();
		else{
			cesion.estado = ListaEstadosEnum._02.name();
		}
		solicitud.cesion.cesiones.add(cesion);
		solicitud.save(); //Guardar cambios en la solicitud
	}
}
