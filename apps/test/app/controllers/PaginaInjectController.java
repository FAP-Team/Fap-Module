package controllers;

import javax.inject.Inject;

import services.GestorDocumentalService;

import messages.Messages;
import models.Solicitud;
import controllers.gen.PaginaInjectControllerGen;

public class PaginaInjectController extends PaginaInjectControllerGen {
    
    @Inject
    private static GestorDocumentalService gestorDocumentalService;
    
    public static void index(String accion, Long idSolicitud) {
        
        if(gestorDocumentalService == null){
            Messages.fatal("El servicio no se inyectó correctamente");
        }
        
        Solicitud solicitud = getSolicitud(idSolicitud);
        renderTemplate("gen/PaginaInject/PaginaInject.html", accion, idSolicitud, solicitud);
    }
}
