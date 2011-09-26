
package controllers.gen.popups;

import play.*;
import play.mvc.*;
import play.db.jpa.Model;
import controllers.fap.*;
import validation.*;
import messages.Messages;

import models.*;
import tags.ReflectionUtils;

import java.util.List;
import java.util.Map;
import java.util.HashMap;



public class PopUpDocProcesosControllerGen extends GenericController {

    
            
				@Util
				protected static SolicitudGenerica getSolicitudGenerica(Long idSolicitud){
					SolicitudGenerica solicitud = null;
					if(idSolicitud == null){
						Messages.fatal("Falta parámetro idSolicitud");
					}else{
						solicitud = SolicitudGenerica.findById(idSolicitud);
						if(solicitud == null){
							Messages.fatal("Error al recuperar SolicitudGenerica");
						}
					}
					return solicitud;
				}
			
            
	@Util
    protected static Documento getDocumento(Long idSolicitud, Long idDocumento){
        Documento documento = null;
        if(idSolicitud == null){
            Messages.fatal("Falta parámetro idSolicitud");
        }else if(idDocumento == null){
            Messages.fatal("Falta parámetro idDocumento");
        }else{
            documento = Documento.find("select documento from SolicitudGenerica solicitud join solicitud.aportaciones.actual.documentos documento where solicitud.id=? and documento.id=?", idSolicitud, idDocumento).first();
            if(documento == null){
                Messages.fatal("Error al recuperar Documento");
            }
        }
        return documento;
    }
        
            

    
	public static void abrir(String accion,Long idDocumento,Long idSolicitud){
		Documento documento;
		if(accion.equals("crear")){
            documento = new Documento();
		}else{
		    documento = getDocumento(idSolicitud, idDocumento);
		}

		if (!permiso(accion)){
			Messages.fatal("No tiene permisos suficientes para realizar esta acción");
		}

		renderArgs.put("controllerName", "PopUpDocProcesosControllerGen");
		renderTemplate("gen/popups/PopUpDocProcesos.html",accion,idDocumento,documento,idSolicitud);
	}
        

    
			@Util
            protected static boolean permiso(String accion) {
                //Sobreescribir para incorporar permisos a mano
			return true;
            }
        

    

    
                public static void editar(Long idSolicitud,Long idDocumento,Documento documento){
                    checkAuthenticity();
                    if(!permiso("update")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    
            Documento dbDocumento = null;
            SolicitudGenerica solicitud = null;
            if(!Messages.hasErrors()){
                dbDocumento = getDocumento(idSolicitud, idDocumento);
                solicitud = getSolicitudGenerica(idSolicitud);
            }
            

                    if(!Messages.hasErrors()){
                        PopUpDocProcesosValidateCopy(dbDocumento, documento);;
                    }

                    if(!Messages.hasErrors()){
                        dbDocumento.save();
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro actualizado correctamente"));
                    }else{
                        Messages.keep();
                        abrir("editar",idDocumento,idSolicitud);
                    }

                }
            

    
                public static void borrar(Long idSolicitud,Long idDocumento){
                    checkAuthenticity();
                    if(!permiso("delete")){
                        Messages.error("No tiene permisos suficientes para realizar la acción");
                    }

                    
            Documento dbDocumento = null;
            SolicitudGenerica solicitud = null;
            if(!Messages.hasErrors()){
                dbDocumento = getDocumento(idSolicitud, idDocumento);
                solicitud = getSolicitudGenerica(idSolicitud);
            }
            

                    if(!Messages.hasErrors()){
                        solicitud.aportaciones.actual.documentos.remove(dbDocumento);
                solicitud.save();
                dbDocumento.delete();
                
                    }

                    if(!Messages.hasErrors()){
                        renderJSON(utils.RestResponse.ok("Registro borrado correctamente"));
                    }else{
                        Messages.keep();
                        abrir("borrar",idDocumento,idSolicitud);
                    }
                }
            

    
			@Util
			protected static void PopUpDocProcesosValidateCopy(Documento dbDocumento, Documento documento){
				CustomValidation.clearValidadas();
				CustomValidation.required("documento", documento);
dbDocumento.tipo = documento.tipo;
dbDocumento.descripcion = documento.descripcion;

				
		// No es necesario ya ponerlo aqui
		// dbDocumento.tipo = documento.tipo;
		// dbDocumento.descripcion = documento.descripcion;
		if(!validation.hasErrors()){
			try {
				aed.AedClient.actualizarTipoDescripcion(dbDocumento);
			}catch(es.gobcan.eadmon.aed.ws.AedExcepcion e){
				validation.addError("", "Error al actualizar el tipo y la descripción en el AED");
			}
		}
		
			}
		

    
}
