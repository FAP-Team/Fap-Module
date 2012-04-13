package services;

import java.util.ArrayList;
import java.util.List;

import messages.Messages;
import models.Agente;
import models.Documento;
import models.Firmante;
import models.Persona;
import models.RepresentantePersonaFisica;
import models.RepresentantePersonaJuridica;
import models.Solicitante;


import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import platino.Firma;

import aed.AedClient;
import es.gobcan.eadmon.aed.ws.AedExcepcion;

public class FirmaService {

	private static Logger log = Logger.getLogger(FirmaService.class);
	
	/**
	 * Valida la firma y la almacena en el AED
	 * @param documento Documento firmado
	 * @param firmantes Lista de firmantes. Se comprueba que la persona no haya firmado ya.
	 * @param firma Firma
	 */
	public static void firmar(Documento documento, List<Firmante> firmantes, Firma firma){	
		firmar(documento, firmantes, firma, null);
	}
	
	/**
	 * Valida la firma y la almacena en el AED
	 * @param documento Documento firmado
	 * @param firmantes Lista de firmantes. Se comprueba que la persona no haya firmado ya.
	 * @param firma Firma
	 * @param valorDocumentofirmanteSolicitado En el caso de que sea != null se comprueba que el certificado del firmante coincida
	 */
	public static void firmar(Documento documento, List<Firmante> firmantes, Firma firma, String valorDocumentofirmanteSolicitado){
		play.Logger.debug("Firmar -> documento: "+documento.uri+", valorDocumento: "+valorDocumentofirmanteSolicitado);
		Firmante firmanteCertificado = firma.validaFirmayObtieneFirmante(documento);
		
		if(firmanteCertificado != null){
			System.out.println("Firmante validado");
			log.info("Firmante validado");
			
			int index = firmantes.indexOf(firmanteCertificado);
			Firmante firmante = null;
			if(index == -1){
				Messages.error("El certificado no se corresponde con uno que debe firmar la solicitud.");
			}else{
				firmante = firmantes.get(index);
				if(firmante.fechaFirma != null){
					Messages.error("Ya ha firmado la solicitud");
				}
				
				log.info("Firmante encontrado " + firmante.idvalor );
				log.info("Esperado " + valorDocumentofirmanteSolicitado);
				if(valorDocumentofirmanteSolicitado != null && !firmante.idvalor.equalsIgnoreCase(valorDocumentofirmanteSolicitado)){
					Messages.error("Se esperaba la firma de " + valorDocumentofirmanteSolicitado);
				}
			}
			
			if(!Messages.hasErrors()){
				// Guarda la firma en el AED
				try {
					log.info("Guardando firma en el aed");
					firmante.fechaFirma = new DateTime();
					AedClient.agregarFirma(documento.uri, firmante, firma.firma);
					firmante.save();
					
					log.info("Firma del documento " + documento.uri + " guardada en el AED");
				}catch(AedExcepcion e){
					log.error("Error guardando la firma en el aed");
					Messages.error("Error al guardar la firma");
				}				
			}
		}else{
			log.error("firmanteCertificado == null????");
		}
	}

	
	
	/**
	 * Permite a un funcionario habilitado firmar, valida la firma y la almacena en el AED
	 * @param documento Documento firmado
	 * @param firma Firma
	 */
	public static void firmarFH(Documento documento, Firma firma){		
		Firmante firmante = firma.validaFirmayObtieneFirmante(documento);
		
		if((firmante != null)&&(firmante.esFuncionarioHabilitado())){
			log.info("Funcionario habilitado validado");
			log.info("Firmante encontrado " + firmante.idvalor );
			
			if(!Messages.hasErrors()){
				// Guarda la firma en el AED
				try {
					log.info("Guardando firma en el aed");
					firmante.fechaFirma = new DateTime();
					AedClient.agregarFirma(documento.uri, firmante, firma.firma);
					firmante.save();
					
					log.info("Firma del documento " + documento.uri + " guardada en el AED");
				}catch(AedExcepcion e){
					log.error("Error guardando la firma en el aed");
					Messages.error("Error al guardar la firma");
				}				
			}
		}else{
			Messages.error("El certificado no se corresponde con uno que debe firmar la solicitud.");
		}
	}

	
	/**
	 * Comprueba que al menos uno de los firmantes únicos ha firmado
	 * o que hayan firmado todos los firmantes multiples
	 * @param firmantes Lista de firmantes
	 * @return
	 */
	public static boolean hanFirmadoTodos(List<Firmante> firmantes){
		boolean multiple = true;
		for(Firmante f : firmantes){
			//Firmante único que ya ha firmado
			if(f.cardinalidad.equals("unico") && f.fechaFirma != null)
				return true;
			
			//Uno de los firmantes multiples no ha firmado
			if(f.cardinalidad.equals("multiple") && f.fechaFirma == null)
				multiple = false;
		}
		
		//En el caso de que no haya firmado ningún único
		//Se devuelve true si todos los múltiples han firmado
		return multiple;
	}
	
	/**
	 * Borra una lista de firmantes, borrando cada uno de los firmantes y vaciando la lista
	 * @param firmantes
	 */
	public static void borrarFirmantes(List<Firmante> firmantes){
		List<Firmante> firmantesBack = new ArrayList<Firmante>(firmantes);
		firmantes.clear();
		
		for(Firmante f : firmantesBack)
			f.delete();
	}
	
	/**
	 * Dado el solicitante, calcula la lista de persona
	 * que pueden firmar la solicitud
	 * 
	 * @param solicitante
	 * @param firmantes
	 */
	public static void calcularFirmantes(Solicitante solicitante, List<Firmante> firmantes){
		if(solicitante.isPersonaFisica()){
			//Firma con el certificado del representante
			Firmante f = new Firmante();
			f.nombre = solicitante.fisica.getNombreCompleto();
			f.tipo = "personafisica";
			f.cardinalidad = "unico";
			f.setIdentificador(solicitante.fisica.nip);
			firmantes.add(f);
			//Añade el representante
			if (solicitante.representado) {
				RepresentantePersonaFisica r = solicitante.representante;
				Firmante fr = new Firmante();
				fr.nombre = r.getNombreCompleto();
				fr.tipo = "representante";
				fr.cardinalidad = "unico";
				fr.setIdentificador(r);
				firmantes.add(fr);
			}
		}else if(solicitante.isPersonaJuridica()){
			//Firma con certificado de empresa
			Firmante f = new Firmante();
			f.nombre = solicitante.juridica.entidad;
			f.tipo = "personajuridica";
			f.cardinalidad = "unico";
			f.setIdentificador(solicitante.getNumeroId());
			firmantes.add(f);
			
			//Añade los representantes
			for(RepresentantePersonaJuridica r : solicitante.representantes){
				Firmante fr = new Firmante();
				fr.nombre = r.getNombreCompleto();
				fr.tipo = "representante";
				if(r.tipo.equals("mancomunado")){
					fr.cardinalidad = "multiple";
				}else if((r.tipo.equals("solidario")) || (r.tipo.equals("administradorUnico"))){
					fr.cardinalidad = "unico";
				}
				fr.setIdentificador(r);
				firmantes.add(fr);
			}
		}		
	}
	
	/**
	 * Calcula los firmantes que pueden firmar un requerimiento
	 * @param firmantes
	 */
	public static List<Firmante> calcularFirmantesRequerimiento () {
		List<Firmante> firmantes = new ArrayList<Firmante>();
		List<Agente> lAgentes = Agente.findAll();
		for (Agente agente: lAgentes) {
			// Si el agente no tiene password
			if ((agente.password == null) && (agente.roles.contains("gestor"))) {
				Firmante nFirmante = new Firmante();
				nFirmante.nombre = agente.username;
				nFirmante.tipo = "gestor";
				nFirmante.cardinalidad = "unico";
				firmantes.add(nFirmante);
			}
		}
		return firmantes;
	}
	
}
