package controllers;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.gson.reflect.TypeToken;

import messages.Messages;
import models.DefinicionMetadatos;
import models.Metadato;
import models.TableKeyValue;
import models.TipoCriterio;
import models.TipoDocumento;
import models.Tramite;

import play.Play;

import models.TramitesVerificables;
import models.VerificacionTramites;

import play.mvc.Util;
import properties.FapProperties;
import services.GestorDocumentalService;
import services.GestorDocumentalServiceException;
import services.aed.ProcedimientosService;
import utils.Fixtures;
import utils.MetadatosUtils;

import utils.JsonUtils;

import utils.ModelUtils;

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
            	deleteTramites();
                List<Tramite> tramites = gestorDocumentalService.getTramites();
                saveTramites(tramites);
                ModelUtils.actualizarTramitesVerificables(tramites);
                updateTableKeyValueTiposDocumentos();
                gestorDocumentalService.actualizarCodigosExclusion();
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
        	play.Logger.info("Añado: "+tipo.uri);
        	if (!TableKeyValue.contains("tiposDocumentos", tipo.uri))
        		TableKeyValue.setValue(table, tipo.uri, tipo.nombre, false, false);
        }
        TableKeyValue.renewCache(table); // Renueva la cache una única vez
    }

    /**
     * Carga las definiciones de metadatos según tipo de documento
     * Carga las asociaciones de metadatos por defecto según tipo de documento
     * 
     * @param actualizarMetadatos
     */
	public static void actualizarMetadatos(String actualizarMetadatos) {
		DefinicionMetadatos.deleteAllDefiniciones();
		List<TipoDocumento> tiposDocumento = TipoDocumento.findAll();
        try {
            MetadatosUtils.cargarDefinicionesMetadatosPorTipo(tiposDocumento);
            Messages.ok("Definiciones de metadatos asociadas a documentos cargadas correctamente");
        } catch (IllegalArgumentException e) {
            Messages.error("Error cargando las definiciones de metadatos por tipo de documento");
        }
		if (!Messages.hasErrors()) {
			try {
				MetadatosUtils.cargarJsonMetadatosTipoDocumento();
				Messages.ok("Metadatos asociados a tipos de documentos cargados correctamente");
			} catch(IllegalArgumentException | NullPointerException  e) {
				Messages.error("Error cargando los metadatos: " + e.getMessage());
			}
		}
		Messages.keep();
		AedController.actualizarMetadatosRender();
	}

}
