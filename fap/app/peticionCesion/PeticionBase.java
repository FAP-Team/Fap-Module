package peticionCesion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import properties.FapProperties;

import services.FirmaService;
import services.FirmaServiceException;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import validation.CifCheck;
import config.InjectorConfig;
import enumerado.fap.gen.EstadosPeticionEnum;
import enumerado.fap.gen.ListaEstadosEnum;
import enumerado.fap.gen.ListaOrigenEnum;

import messages.Messages;
import models.Cesiones;
import models.Documento;
import models.PeticionCesiones;
import models.SolicitudGenerica;

public abstract class PeticionBase {
	
	private final static String HEADER_REPORT = "reports/headerPeticion.html";
	private final static String FOOTER_REPORT = "reports/footer-cesion.html";
	
	static GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
	
	public abstract void generarPeticionBase(PeticionCesiones pt, List<Long> idsSeleccionados);
	
	public abstract void parsearPeticionBase(PeticionCesiones pt, File fich) ;
	
	public abstract void aplicarCambiosBase(SolicitudGenerica solicitud, PeticionCesiones pt, Documento doc, String estado);
	
	public void firmarDocumentoBase(SolicitudGenerica solicitud, Cesiones cesion) {
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
	
	public String getTipoId(String dato) {
		StringBuilder texto = new StringBuilder();
		if (CifCheck.validaCif(dato, texto)) //Si es un cif
			return "CIF";
		else{ 
			return "NIP";
		}
	}
	
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
	
	public abstract String getBodyReport();


	public String getHeaderReport() {
		return HEADER_REPORT;
	}

	public String getFooterReport() {
		return FOOTER_REPORT;
	}
}
