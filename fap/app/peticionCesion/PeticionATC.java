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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import config.InjectorConfig;

import properties.FapProperties;
import reports.Report;
import services.FirmaService;
import services.FirmaServiceException;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import validation.CifCheck;
import enumerado.fap.gen.CodigoRespuestaEnum;
import enumerado.fap.gen.EstadosPeticionEnum;
import enumerado.fap.gen.ListaEstadosEnum;
import enumerado.fap.gen.ListaOrigenEnum;

import messages.Messages;
import models.CesionPDF;
import models.Cesiones;
import models.Documento;
import models.PeticionCesiones;
import models.SolicitudGenerica;

public class PeticionATC extends PeticionBase{

	static final int nombreCte = 75;
	static final int libreCte = 72;
	
	static final int initipoReg = 0;
	static final int inifecha = 1;
	static final int iniNumero = 9;
	static final int ininDoc = 1;
	static final int iniNombre = 10;
	static final int iniCodResp = 85;
	
	@Override
	public
	void generarPeticionBase(PeticionCesiones pt, List<Long> idsSeleccionados) {
		File file = null;
		BufferedWriter bw = null;
		try{
			file = File.createTempFile("ATC"+obtenerFechaFormato(), ".txt");
			bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
		}
		catch (IOException e) {
			Messages.error("Error generando el fichero de peticion, intentelo de nuevo");
		}
		//Contenido = RegCabecera (tipoReg + fecha + numSol + libre) + RegDetalle (tipoReg + NDoc + Nombre)
		Formatter fmt = new Formatter();
		fmt.format("%04d", idsSeleccionados.size());
		String numSol = ""+fmt;
		String libre = String.format("%"+libreCte+"s", "");
		String contenido ="0"+fechaFormato()+numSol+libre+"\n";
		Set idNoRepetidos = new HashSet(); 
		
		//No puede haber dni/cif repetidos
		for (Long solId : idsSeleccionados) {
			SolicitudGenerica s = SolicitudGenerica.findById(solId);
				if ((s.solicitante.isPersonaFisica()) && (!idNoRepetidos.contains(s.solicitante.fisica.nip.valor))){
					contenido += "1";
					String nombre = s.solicitante.nombreCompleto;
					String nombre75 = nombre + String.format("%"+(nombreCte-nombre.length())+"s", "");
					contenido += s.solicitante.fisica.nip.valor+nombre75.substring(0, nombreCte)+"\n";				
					idNoRepetidos.add(s.solicitante.fisica.nip.valor);
				}
				if ((s.solicitante.isPersonaJuridica()) && (!idNoRepetidos.contains(s.solicitante.juridica.cif))){
					contenido += "1";
					String nombre = s.solicitante.nombreCompleto;
					String nombre75 = nombre + String.format("%"+(nombreCte-nombre.length())+"s", "");
					contenido += s.solicitante.juridica.cif+nombre75.substring(0, nombreCte)+"\n";
					idNoRepetidos.add(s.solicitante.juridica.cif);
				}
		}
		try {
			bw.write(contenido);
			bw.close();
			Documento doc = new Documento();
        	doc.tipo = FapProperties.get("fap.aed.tiposdocumentos.peticionATC");
        	doc.descripcion = "Descripcion Peticion ATC";
        	gestorDocumentalService.saveDocumentoTemporal(doc, new FileInputStream(file), FapProperties.get("fap.prefijo.peticion.provincia")+" ATC"+obtenerFechaNombre()+".txt");
        	pt.fichPeticion.tipo = FapProperties.get("fap.aed.tiposdocumentos.peticionATC");
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
	public
	void parsearPeticionBase(PeticionCesiones pt, File fich) {
		FileReader fr = null;
		try { //Abrir en modo lectura
			fr = new FileReader (fich);
			BufferedReader br = new BufferedReader(fr);
			String linea = null;
			CesionPDF atc = new CesionPDF();
			//Lectura de la cabecera
			linea=br.readLine();
			pt.respCesion.fechaGeneracion = obtenerFecha(linea.substring(inifecha, iniNumero));
			//Lectura de los ficheros de registro:
			while((linea=br.readLine())!=null){
				atc.registro.tipoRegistro = linea.substring(initipoReg, inifecha);
				atc.registro.nDocumento = linea.substring(ininDoc, iniNombre);
				atc.registro.nombre = linea.substring(iniNombre, iniCodResp);
				atc.registro.estado  = "_"+linea.substring(iniCodResp, linea.length()); //Nombre
				generarPdfATC(pt, atc);
			}
			fr.close();
		} catch (Exception e) {
			Messages.error("Error parseando el documento de respuesta del ATC, compruebe que el fichero es correcto");
			play.Logger.info("Error parseando el documento de respuesta del ATC");
		}
	}

	@Override
	public
	void aplicarCambiosBase(SolicitudGenerica solicitud, PeticionCesiones pt, Documento doc, String estado) {
		Cesiones cesion = new Cesiones();
		cesion.tipo = pt.tipo;
		cesion.idUnico = Long.toString(pt.id);
		cesion.fechaPeticion = pt.respCesion.fechaGeneracion;
		cesion.fechaValidez = pt.respCesion.fechaGeneracion.plusMonths(Integer.parseInt(FapProperties.get("fap.cesiondatos.validezPeticion")));;;
		cesion.origen = ListaOrigenEnum.cesion.name();
		cesion.firmada = false;
		cesion.documento = doc;
		if (estado.equals(CodigoRespuestaEnum._00.name())){
			cesion.estado = ListaEstadosEnum._01.name();	
		}
		else if (estado.equals(CodigoRespuestaEnum._01.name())){
			cesion.estado = ListaEstadosEnum._02.name();
		}
		else if (estado.equals(CodigoRespuestaEnum._02.name())){
			cesion.estado = ListaEstadosEnum._03.name();
		}
		else if (estado.equals(CodigoRespuestaEnum._03.name())){
			cesion.estado = ListaEstadosEnum._04.name();
		}
		solicitud.cesion.cesiones.add(cesion);
		firmarDocumentoBase(solicitud, cesion);
	}

	@Override
	public
	void firmarDocumentoBase(SolicitudGenerica solicitud, Cesiones cesion) {
		//Firma del documento generado
		FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);
		try {
			firmaService.firmarEnServidor(cesion.documento);
			cesion.firmada = true;
			cesion.save();
			solicitud.save(); 
			play.Logger.info("Aplicados cambios de cesión de datos en la solicitud "+solicitud.id);
		} catch (FirmaServiceException e) {
			play.Logger.error("No se pudo firmar en Servidor: "+e);
		} 
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
	
	private static String fechaFormato() {
		Calendar fecha =  Calendar.getInstance();
		int dia = fecha.get(Calendar.DATE);
		int mes = fecha.get(Calendar.MONTH);
		int anyo = fecha.get(Calendar.YEAR);
		Formatter fmtMes = new Formatter();
		fmtMes.format("%02d", mes);
		Formatter fmtDia = new Formatter();
		fmtDia.format("%02d", dia);
		return anyo+fmtMes.toString()+fmtDia.toString();
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
	
	public static DateTime obtenerFecha(String fecha){
		int anio = Integer.parseInt(fecha.substring(0, 4));
		int mes = Integer.parseInt(fecha.substring(4, 6));
		int dia = Integer.parseInt(fecha.substring(6, fecha.length()));
		
		DateTime fechaGeneracion = new DateTime(anio, mes, dia, 0, 0);
		return fechaGeneracion;
	}
	
	private File generarPdfATC(PeticionCesiones pt, CesionPDF atc){
		File report =  new File ("reports/bodyPeticion.html");
        //Obtener solicitud correspondiente
        //Tipo dice si es dni, nie, pasaporte, cif
        List<SolicitudGenerica> solicitud = getSolicitudes(getTipoId(atc.registro.nDocumento), atc.registro.nDocumento);
        
        if((!Messages.hasErrors()) && (!solicitud.isEmpty())){
        	try {
            	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("pt", pt);
            	for (SolicitudGenerica sol : solicitud) {
            		List<Cesiones> cesionesTipo = Cesiones.find("select cesiones from SolicitudGenerica solicitud join solicitud.cesion.cesiones cesiones where  cesiones.tipo = ? and cesiones.idUnico = ? and solicitud.id = ?", "atc", pt.id.toString(), sol.id).fetch();
            		if (cesionesTipo.isEmpty()){ //No se han creado cesiones a partir de este fichero -> Creo
	            		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("atc", atc);
	            		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", sol);

	            		report = new Report("reports/bodyPeticionATC.html").header("reports/headerPeticion.html").footer("reports/footer-cesion.html").renderTmpFile(sol, pt, atc);
	                	Documento doc = new Documento();
	                	doc.tipo = FapProperties.get("fap.aed.tiposdocumentos.respuestaATC");
	                	doc.descripcion = "Descripcion ATC";
	                	gestorDocumentalService.saveDocumentoTemporal(doc, new FileInputStream(report), "cesionATC"+obtenerFechaNombre()+".pdf");
	                	sol.documentacionCesion.documentos.add(doc);
	                	pt.respCesion.fechaActuacionGestor = new DateTime();
	                	pt.respCesion.uri = doc.uri;
	                	aplicarCambiosBase(sol, pt, doc, atc.registro.estado);
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
          	Messages.warning("La cesion de datos para "+atc.registro.nDocumento+", no se corresponde con ninguna solicitud");
          	play.Logger.info("La cesion de datos para "+atc.registro.nDocumento+", no se corresponde con ninguna solicitud");
        }
		
		return null;
	}

	@Override
	public String getTipoId(String dato) {
		StringBuilder texto = new StringBuilder();
		if (CifCheck.validaCif(dato, texto)) //Si es un cif
			return "CIF";
		else
			return "NIP";
	}

	@Override
	public List<SolicitudGenerica> getSolicitudes(String tipo, String id) {
		List<SolicitudGenerica> solicitudes = new ArrayList<SolicitudGenerica>();
		if (tipo.equals("NIP")){ //dni, pasaporte,..
        	solicitudes = SolicitudGenerica.find("Select solicitud from Solicitud solicitud where solicitud.solicitante.fisica.nip.valor = ?", id).fetch();
        }
        else if (tipo.equals("CIF")){
        	solicitudes = SolicitudGenerica.find("Select solicitud from Solicitud solicitud where solicitud.solicitante.juridica.cif = ?", id).fetch();
        }
		return solicitudes;
	}
}
