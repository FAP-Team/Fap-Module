package tramitacion;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityTransaction;

import org.joda.time.DateTime;

import play.db.jpa.JPA;

import services.FirmaService;
import services.GestorDocumentalServiceException;
import services.RegistroServiceException;

import config.InjectorConfig;
import controllers.fap.FirmaController;
import emails.Mails;
import messages.Messages;
import models.Agente;
import models.Documento;
import models.Firmante;
import models.Firmantes;
import models.JustificanteRegistro;
import models.Persona;
import models.Solicitud;
import models.SolicitudGenerica;

public class TramiteFirmaDoble extends TramiteAplicacion{

	private final static String BODY_REPORT = "reports/bodyMultiplesFirmantes.html";
	
	@Inject
	private static FirmaService firmaService = InjectorConfig.getInjector().getInstance(FirmaService.class);
	
	public TramiteFirmaDoble(SolicitudGenerica solicitud) {
		super(solicitud);
	}

	public boolean hanFirmadoTodos() {
		boolean multiple = true;
		boolean result = false;
		boolean extra = false;
				
		//Extraer firmantes extra:
		Solicitud misolicitud = (Solicitud)this.solicitud;
		String firmanteExtra = misolicitud.personaDirector.numeroId;
		
		//Suponemos que primero debe firmar el solicitante y luego el extra
		for (Firmante f : registro.firmantes.todos) {
			//Comprobando que entre las firmas se encuentra la del solicitante
			if ((f.cardinalidad.equals("unico") && f.fechaFirma != null)){
				if (f.idvalor.equals(firmanteExtra)){ //Indico que ha firmado el firmante añadido
					extra = true;
					multiple = false;
				}else{
					result=true;
					multiple = false;
				}
			}
				
			// Uno de los firmantes multiples no ha firmado
			if ((f.cardinalidad.equals("multiple") && f.fechaFirma == null)){
				multiple = false;
			}
		}
	
		if (extra && (result || multiple))
			return true;
	return false;
	}
    
    public void calcularFirmantes(){
        if(!Messages.hasErrors()){
        	//Firmantes de la solicitud
            registro.firmantes = Firmantes.calcularFirmanteFromSolicitante(solicitud.solicitante);
            
            //Nuevos firmantes dependientes de mi trámite que quiero añadir
            Solicitud misolicitud = (Solicitud)this.solicitud;
            Firmante firmanteExtra = new Firmante(misolicitud.personaDirector, "unico");
    		registro.firmantes.todos.add(firmanteExtra);
        }
    }
    
	@Override
	public String getBodyReport() {
		return TramiteFirmaDoble.BODY_REPORT;
	}
	
}
