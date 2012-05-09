
			package controllers;

			import java.util.List;

import models.Exclusion;
import models.TipoCodigoExclusion;
import controllers.gen.ExclusionControllerGen;
			
			public class ExclusionController extends ExclusionControllerGen {
//				public static void tablaexclusion(Long idSolicitud, Long idEntidad){
//					
//					Long id = idSolicitud != null? idSolicitud : idEntidad;
//					Exclusion exclusion = Exclusion.find("select solicitud.exclusion from SolicitudGenerica solicitud where solicitud.id=?", id ).first();
//					//java.util.List<TipoCodigoExclusion> rows = TipoCodigoExclusion.find( "select tipoCodigoExclusion from 
//					
//					java.util.List<TipoCodigoExclusion> rows = exclusion.getCodigosExclusion();
//					System.out.println("Rows: "+rows.size());
//					
//					List<TipoCodigoExclusion> rowsFiltered = rows; //Tabla sin permisos, no filtra
//					
//					tables.TableRenderResponse<TipoCodigoExclusion> response = new tables.TableRenderResponse<TipoCodigoExclusion>(rowsFiltered);
//					renderJSON(response.toJSON("descripcionCorta", "codigo" ,"id"));
//						
//			}
			}
		