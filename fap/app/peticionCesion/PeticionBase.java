package peticionCesion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;

import properties.FapProperties;

import services.FirmaService;
import services.FirmaServiceException;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
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
	
	static GestorDocumentalService gestorDocumentalService = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
	
	public abstract void generarPeticionBase(PeticionCesiones pt, List<Long> idsSeleccionados);
	
	public abstract void parsearPeticionBase(PeticionCesiones pt, File fich) ;
	
	public abstract void aplicarCambiosBase(SolicitudGenerica solicitud, PeticionCesiones pt, Documento doc, String estado);
	
	public abstract void  firmarDocumentoBase(SolicitudGenerica solicitud, Cesiones cesion);
	
	public abstract String getTipoId(String dato);
	
	public abstract List<SolicitudGenerica> getSolicitudes(String tipo, String id);
	
	public abstract String getBodyReport();

	public abstract String getHeaderReport();

	public abstract String getFooterReport();
}
