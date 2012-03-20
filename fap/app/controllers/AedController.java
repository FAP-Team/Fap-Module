package controllers;

import java.util.List;

import javax.inject.Inject;

import messages.Messages;
import models.TableKeyValue;
import models.TipoDocumento;
import models.Tramite;
import play.mvc.Util;
import properties.FapProperties;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.aed.ProcedimientosService;
import utils.Fixtures;
import controllers.gen.AedControllerGen;
import es.gobcan.eadmon.aed.ws.AedExcepcion;
import es.gobcan.eadmon.aed.ws.excepciones.CodigoErrorEnum;

public class AedController extends AedControllerGen {

    @Inject
    static GestorDocumentalService gestorDocumentalService;

    public static void configurar() {
        if (configurarHasAccess()) {
            try {
                gestorDocumentalService.configure();
                Messages.ok("Se configuró correctamente el gestor documental");
            } catch (GestorDocumentalServiceException e) {
                play.Logger.error(e, "Error configurando el gestor documental");
                Messages.error("Se produjo un error configurando el gestor documental");
            }
        }
        AedController.configurarRender();
    }

    private static boolean configurarHasAccess() {
        checkAuthenticity();
        boolean access = permisoConfigurar("editar");
        if (!access) {
            Messages.error("No tiene permisos suficientes para realizar la acción");
        }
        return access;
    }

    public static void actualizarTramites() {
        if (actualizarTramitesHasAccess()) {
            try {
                List<Tramite> tramites = gestorDocumentalService.getTramites();
                deleteTramites();
                saveTramites(tramites);
                updateTableKeyValueTiposDocumentos();
                Messages.ok("Recuperados " + tramites.size() + " tramites");
            } catch (GestorDocumentalServiceException e) {
                Messages.error("Error al actualizar los trámites", e);
            }
        }
        AedController.actualizarTramitesRender();
    }

    private static boolean actualizarTramitesHasAccess() {
        checkAuthenticity();
        boolean access = permisoActualizarTramites("editar");
        if (!access) {
            Messages.error("No tiene permisos suficientes para realizar la acción");
        }
        return access;
    }

    private static void deleteTramites() {
        Tramite.deleteAllTramitesAndTipoDocumentos();
    }

    private static void saveTramites(List<Tramite> tramites) {
        for (Tramite t : tramites) {
            t.save();
        }
    }

    private static void updateTableKeyValueTiposDocumentos() {
        List<models.TipoDocumento> tiposDocumentos = models.TipoDocumento.findAll();
        String table = "tiposDocumentos";
        TableKeyValue.deleteTable(table);
        for (models.TipoDocumento tipo : tiposDocumentos) {
            TableKeyValue.setValue(table, tipo.uri, tipo.nombre, false);
        }
        TableKeyValue.renewCache(table); // Renueva la cache una única vez
    }

}
