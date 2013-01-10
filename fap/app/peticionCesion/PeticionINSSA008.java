package peticionCesion;

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

import properties.FapProperties;

import reports.Report;
import services.FirmaService;
import services.FirmaServiceException;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import config.InjectorConfig;
import enumerado.fap.gen.EstadosPeticionEnum;
import enumerado.fap.gen.ListaCesionesEnum;
import enumerado.fap.gen.ListaEstadosEnum;
import enumerado.fap.gen.ListaOrigenEnum;

import messages.Messages;
import models.CesionPDF;
import models.Cesiones;
import models.Documento;
import models.PeticionCesiones;
import models.SolicitudGenerica;
import models.Trabajador;

public class PeticionINSSA008 extends PeticionBase{
	static final int leyCte = 8;
	static final int motivoCte = 25;
	static final int correcto = 87;
	static final int error = 96;
	static final int rechazo = 100;
	static final int txtError = 80;
	
	private final static String BODY_REPORT = "reports/bodyPeticionINSSA008.html";
	
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
			//InSSA008 pide datos de los trabajadores -> Buscar cuentas de cotizacion ->Registro de detalle
			for (Trabajador trabajador : s.cesion.autorizacionCesion.trabajadores) {
				contenido += "1"+trabajador.regimen+trabajador.codigoCuenta+"\n"; 
			}
		}
		try {
			if ((contenido != null) && (bw != null)){
				bw.write(contenido);
				bw.close();			
				
				Documento doc = new Documento();
            	doc.tipo = FapProperties.get("fap.aed.tiposdocumentos.peticionINSSA008");
            	doc.descripcion = "Descripcion Peticion INSS";
            	gestorDocumentalService.saveDocumentoTemporal(doc, new FileInputStream(file), FapProperties.get("fap.prefijo.peticion.provincia")+" INSSA008"+obtenerFechaNombre()+".txt");
            	pt.fichPeticion.tipo = FapProperties.get("fap.aed.tiposdocumentos.peticionINSSA008");
            	pt.fichPeticion.uri =  doc.uri; 
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
			//Registro cabecera 2
			inss.cabeceraSegunda=br.readLine();
			//if (!inss.cabeceraSegunda.equals("2001"+motivo)){
			//	System.out.println("Fallo de cabecera 2");
			//	throw new Exception(new Throwable());
			//}
			//Registro detalle
			while((linea=br.readLine())!=null){
				if (linea.length() == correcto){ //Correcto
					inss.registro.tipoRegistro = linea.substring(0, 1); //Comprobar que empieza en 0
					inss.registro.regimen = linea.substring(1, 5);
					inss.registro.cccPpal = linea.substring(5, 16);
					inss.registro.numMedioTrabajadores = linea.substring(16, 24);
					inss.registro.nombre = linea.substring(24, 79);
					inss.registro.fechaSolicitud = obtenerFechaA008(linea.substring(79, 87));;
					pt.respCesion.fechaGeneracion = inss.registro.fechaSolicitud;
					inss.registro.estado = ListaEstadosEnum._01.name();
					generarPdfINSSA008(pt, inss);
				}
				else if(linea.length() == error){ //Error
					Messages.warning("Error de acceso al fichero general de Afiliación: "+linea.substring((error-txtError), error)+" no se creará fichero de cesión de datos");
					
				}
				else if (linea.length() == rechazo){ //Rechazo -> Error en cabecera
					Messages.warning("Rechazo en el registro: "+linea.substring(1, rechazo)+" no se creará fichero de cesión de datos");
				}
				else{
					play.Logger.info("Error de formato en el fichero de respuesta aportado");
					throw new Exception(new Throwable());
				}
			}
			fr.close();
		}
		catch (Exception e) {
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
	
	public File generarPdfINSSA008(PeticionCesiones pt, CesionPDF inss){
		File report =  new File ("reports/bodyPeticion.html");
		List<SolicitudGenerica> solicitud = getSolicitudes(inss.registro.regimen, inss.registro.cccPpal);
		
		 if((!Messages.hasErrors()) && (!solicitud.isEmpty())){        	
	            try {
	            	play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("pt", pt);
	            	for (SolicitudGenerica sol : solicitud) {
	            		List<Cesiones> cesionesTipo = Cesiones.find("select cesiones from SolicitudGenerica solicitud join solicitud.cesion.cesiones cesiones where  cesiones.tipo = ? and cesiones.idUnico = ? and solicitud.id = ?", "inssR001", pt.id.toString(), sol.id).fetch();
	            		if (cesionesTipo.isEmpty()){ //No se han creado cesiones a partir de este fichero -> Creo
		            		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("inss", inss);
		            		play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer.addVariable("solicitud", sol);
		                	report = new Report("reports/bodyPeticionINSSA008.html").header("reports/headerPeticion.html").footer("reports/footer-cesion.html").renderTmpFile(sol, pt, inss);
		                	Documento doc = new Documento();
		                	doc.tipo = FapProperties.get("fap.aed.tiposdocumentos.respuestaINSSA008");
		                	doc.descripcion = "Descripcion INSSA008";
		                	gestorDocumentalService.saveDocumentoTemporal(doc, new FileInputStream(report), "cesionINSSA008"+obtenerFechaNombre()+".pdf");
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
			 Messages.warning("La cesion de datos para "+inss.registro.regimen+inss.registro.cccPpal+", no se corresponde con ninguna solicitud");
			 play.Logger.info("La cesion de datos para "+inss.registro.regimen+inss.registro.cccPpal+", no se corresponde con ninguna solicitud");
	      }
		return report;
	}

	public static DateTime obtenerFechaA008(String fecha){
		int anio = Integer.parseInt(fecha.substring(0, 4));
		int mes = Integer.parseInt(fecha.substring(4, 6));
		int dia = Integer.parseInt(fecha.substring(6, 8));
		DateTime fechaGeneracion = new DateTime(anio, mes, dia, 0, 0);
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

	@Override
	public String getTipoId(String dato) {
		//No se hace NADA en este tipo de petición con este método
		return null;
	}

	@Override
	public List<SolicitudGenerica> getSolicitudes(String tipo, String id) {
		//Tipo es el regimen
		//Id es el cccPpal
		return SolicitudGenerica.find("Select solicitud from Solicitud solicitud join solicitud.cesion.autorizacionCesion.trabajadores trabajadores where trabajadores.regimen = ? and trabajadores.codigoCuenta = ? and solicitud.cesion.autorizacionCesion.inssA008 = ?", tipo, id, true).fetch();
	}
	
	@Override
	public String getBodyReport() {
		return BODY_REPORT;
	}

}
