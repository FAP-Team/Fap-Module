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

import config.InjectorConfig;

import properties.FapProperties;
import reports.Report;
import services.FirmaService;
import services.FirmaServiceException;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import sun.print.resources.serviceui;
import enumerado.fap.gen.EstadosPeticionEnum;
import enumerado.fap.gen.ListaCesionesEnum;
import enumerado.fap.gen.ListaEstadosEnum;
import enumerado.fap.gen.ListaOrigenEnum;
import enumerado.fap.gen.TipoDocIdCesionEnum;

import messages.Messages;
import models.CesionPDF;
import models.Cesiones;
import models.Documento;
import models.PeticionCesiones;
import models.SolicitudGenerica;
import models.Trabajador;

public class PeticionINSSR001 extends PeticionBase{

	static final int nDocumentoCte = 10;
	static final int motivoCte = 25;
	static final int leyCte = 8;
	static final int iniID = 3;
	static final int finID = 12;
	static final int tipoCte = 1;
	static final int iniAnio = 10;
	static final int iniMes = 14;
	static final int iniDia = 16;
	static final int iniHora = 18;
	static final int iniMin = 20;
	static final int finMin = 22;
	static final int iniEstadoCte = 37; 
	static final int finEstadoCte = 39;
	static final int iniIdEstado = 0;
	static final int finIdEstado = 2;
	
	private final static String BODY_REPORT = "reports/bodyPeticionINSSR001.html";
	private final static String HEADER_REPORT = "reports/headerPeticion.html";
	private final static String FOOTER_REPORT = "reports/footer-cesion.html";
	
	@Override
	public void generarPeticionBase(PeticionCesiones pt, List<Long> idsSeleccionados) {
		String motivo = String.format("%"+motivoCte+"s", "")+"\n";
		String ley = String.format("%"+leyCte+"s", "")+"\n";
		File file = null;
		BufferedWriter bw = null;
		
		try{
			file = File.createTempFile("INSS"+obtenerFechaFormato(), ".txt");
			bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
		}
		catch (IOException e) {
			Messages.error("Error generando el fichero de peticion, intentelo de nuevo");
		}
		//Contenido = cabecera1 + cabecera2 + Registro de detalle de los datos
		String contenido = "1C"+ley+"2001"+motivo;
		for (Long solId : idsSeleccionados) {
			SolicitudGenerica s = SolicitudGenerica.findById(solId);
			if (s.solicitante.isPersonaFisica()){
				if (s.solicitante.fisica.nip.isNif()){
					String tipoDoc = TipoDocIdCesionEnum._1.name().replace("_", "");
					contenido += "3"+tipoDoc+String.format("%"+(nDocumentoCte-s.solicitante.fisica.nip.valor.length())+"s", "0")+s.solicitante.fisica.nip.valor+"\n";
				}
				if (s.solicitante.fisica.nip.isPasaporte()){
					String tipoDoc = TipoDocIdCesionEnum._2.name().replace("_", "");
					contenido += "3"+tipoDoc+String.format("%"+(nDocumentoCte-s.solicitante.fisica.nip.valor.length())+"s", "0")+s.solicitante.fisica.nip.valor+"\n";
				}
				if (s.solicitante.fisica.nip.isNie()){ 
					String tipoDoc = TipoDocIdCesionEnum._6.name().replace("_", "");
					contenido += "3"+tipoDoc+String.format("%"+(nDocumentoCte-s.solicitante.fisica.nip.valor.length())+"s", "0")+s.solicitante.fisica.nip.valor+"\n";
				}
			}
			if (s.solicitante.isPersonaJuridica()){
				String tipoDoc = TipoDocIdCesionEnum._9.name().replace("_", "");
				contenido += "3"+tipoDoc+String.format("%"+(nDocumentoCte-s.solicitante.juridica.cif.length())+"s", "0")+s.solicitante.juridica.cif+"\n";
			}
		}
		try {
			if ((contenido != null) && (bw != null)){
				bw.write(contenido);
				bw.close();			
				
				Documento doc = new Documento();
            	doc.tipo = FapProperties.get("fap.aed.tiposdocumentos.peticionINSSR001");
            	doc.descripcion = "Descripcion Peticion INSS";
            	gestorDocumentalService.saveDocumentoTemporal(doc, new FileInputStream(file), FapProperties.get("fap.prefijo.peticion.provincia")+" INSSR001"+obtenerFechaNombre()+".txt");
            	pt.fichPeticion.tipo = FapProperties.get("fap.aed.tiposdocumentos.peticionINSSR001");
            	pt.fichPeticion.uri =  doc.uri; //Almaceno donde está ANTES getAbsolutepath
				pt.estado = EstadosPeticionEnum.creada.name();
				pt.fechaGen=DateTime.now();
				pt.fechaValidez = pt.fechaGen.plusMonths(Integer.parseInt(FapProperties.get("fap.cesiondatos.validezPeticion")));
				pt.save();
				pt.fichPeticion.save();
				play.Logger.info("Fichero de peticion de cesión de datos al INSS creado correctamente");
			}
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
		String motivo = String.format("%"+motivoCte+"s", "");

		try { //Abrir en modo lectura
			fr = new FileReader (fich);
			BufferedReader br = new BufferedReader(fr);
			String linea = null;
	        CesionPDF inss = new CesionPDF(); //Entidad con datos parseados
	        
			//Registro cabecera 1
	        inss.cabeceraPrimera=br.readLine(); //1C+Ley(8blancos)+año+mes+dia+h+m+s+"s"idTransmision(6 digitos -> hasta TGSS)+TGSS
			DateTime fechaGeneracion = obtenerFecha(inss.cabeceraPrimera); //Parsear la fecha de generacion de consulta
			pt.respCesion.fechaGeneracion = fechaGeneracion;
			
			//Registro cabecera 2
			inss.cabeceraSegunda=br.readLine();
			//Los ejemplos traen más de 25 caracteres en blanco en motivo
			/*if (!inss.cabeceraSegunda.equals("2001"+motivo))
				Messages.error("Error de formato en la cabecera segunda del archivo recibido");*/
			
			//Registro detalle
			while((linea=br.readLine())!=null){ // TipoReg+TipoDoc(1)+NumDoc(10)+Nombre(25)+estado[2](TABLA)+literalEstado(17) 
				 inss.registro.nDocumento = linea.substring(iniID, finID);
				 inss.registro.tipoRegistro = linea.substring(tipoCte, tipoCte+1).toString();
				 inss.registro.estado = "_"+linea.substring(iniEstadoCte, finEstadoCte).toString(); 
				 generarPdfINSSR001(pt, inss);
			}		
			fr.close();
		} catch (Exception e) {
			Messages.error("Error parseando el documento de respuesta del INSS, compruebe que el fichero es correcto");
			play.Logger.info("Error parseando el documento de respuesta del INSS");
		}
	}

	@Override
	public void aplicarCambiosBase(SolicitudGenerica solicitud, PeticionCesiones pt, Documento doc, String estado) {
		Cesiones cesion = new Cesiones();
		cesion.tipo = pt.tipo;
		cesion.idUnico = Long.toString(pt.id);
		cesion.fechaPeticion = pt.respCesion.fechaGeneracion;
		cesion.fechaValidez = pt.respCesion.fechaGeneracion.plusMonths(Integer.parseInt(FapProperties.get("fap.cesiondatos.validezPeticion")));;;
		cesion.origen = ListaOrigenEnum.cesion.name();
		cesion.firmada = false;
		cesion.documento = doc;
		//Estado de la Cesion (positivo, negativo, nodatos, error)
		if (pt.tipo.equals(ListaCesionesEnum.inssR001.name())){
			if ((estado.equals("_05")) || (estado.equals("_06")))
				cesion.estado = ListaEstadosEnum._04.name();
			else
				cesion.estado = ListaEstadosEnum.valueOf(estado).name();	
		}else{
			cesion.estado = estado;
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
			play.Logger.error("No se pudo firmar en Servidor: "+e);
		} 
	}

	public File generarPdfINSSR001(PeticionCesiones pt, CesionPDF inss){
		File report =  new File ("reports/bodyPeticion.html");
        List<SolicitudGenerica> solicitud = getSolicitudes(getTipoId(inss.registro.tipoRegistro), inss.registro.nDocumento);
        if((!Messages.hasErrors()) && (!solicitud.isEmpty())){   
            try {
            	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("pt", pt);
            	for (SolicitudGenerica sol : solicitud) {
            		List<Cesiones> cesionesTipo = Cesiones.find("select cesiones from SolicitudGenerica solicitud join solicitud.cesion.cesiones cesiones where  cesiones.tipo = ? and cesiones.idUnico = ? and solicitud.id = ?", "inssR001", pt.id.toString(), sol.id).fetch();
            		if (cesionesTipo.isEmpty()){ 
            			play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("inss", inss);
	            		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", sol);
	                	report = new Report("reports/bodyPeticionINSSR001.html").header("reports/headerPeticion.html").footer("reports/footer-cesion.html").renderTmpFile(sol, pt, inss);
	                	Documento doc = new Documento();
	                	doc.tipo = FapProperties.get("fap.aed.tiposdocumentos.respuestaINSSR001");
	                	doc.descripcion = "Descripcion INSSR001";
	                	gestorDocumentalService.saveDocumentoTemporal(doc, new FileInputStream(report), "cesionINSSR001"+obtenerFechaNombre()+".pdf");
	                	sol.documentacionCesion.documentos.add(doc);
	                	pt.respCesion.fechaActuacionGestor = new DateTime();
	                	pt.respCesion.uri = doc.uri;
	                	aplicarCambiosBase(sol, pt, doc, inss.registro.estado);
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
        	Messages.warning("La cesion de datos para "+inss.registro.nDocumento+", no se corresponde con ninguna solicitud");
        	play.Logger.info("La cesion de datos para "+inss.registro.nDocumento+", no se corresponde con ninguna solicitud");
        }
        return report;
    } 
	
	public static DateTime obtenerFecha(String fecha){
		int anio = Integer.parseInt(fecha.substring(iniAnio, iniMes));
		int mes = Integer.parseInt(fecha.substring(iniMes, iniDia));
		int dia = Integer.parseInt(fecha.substring(iniDia, iniHora));
		int hora = Integer.parseInt(fecha.substring(iniHora, iniMin));
		int min = Integer.parseInt(fecha.substring(iniMin, finMin));
		DateTime fechaGeneracion = new DateTime(anio, mes, dia, hora, min);
		return fechaGeneracion;
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
	
	public static DateTime obtenerFechaA008(String fecha){
		int anio = Integer.parseInt(fecha.substring(0, 4));
		int mes = Integer.parseInt(fecha.substring(4, 6));
		int dia = Integer.parseInt(fecha.substring(6, 8));
		DateTime fechaGeneracion = new DateTime(anio, mes, dia, 0, 0);
		return fechaGeneracion;
	}


	@Override
	public String getTipoId(String dato) {
		if ((("_"+dato).equals(TipoDocIdCesionEnum._1.name())) || (("_"+dato).equals(TipoDocIdCesionEnum._2.name())) || (("_"+dato).equals(TipoDocIdCesionEnum._6.name())))
			return "NIP";
		else if (("_"+dato).equals(TipoDocIdCesionEnum._9.name()))
			return "CIF";
		else
			return null;
	}


	@Override
	public List<SolicitudGenerica> getSolicitudes(String tipo, String id) {
		List<SolicitudGenerica> solicitudes = new ArrayList<SolicitudGenerica>();
		 if (tipo.equals("NIP"))
	       	solicitudes = SolicitudGenerica.find("Select solicitud from Solicitud solicitud where solicitud.solicitante.fisica.nip.valor = ?", id).fetch();
	     if (tipo.endsWith("CIF"))
	      	solicitudes = SolicitudGenerica.find("Select solicitud from Solicitud solicitud where solicitud.solicitante.juridica.cif = ?", id).fetch();
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
