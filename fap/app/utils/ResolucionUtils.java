package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import properties.FapProperties;

import models.CEconomico;
import models.Criterio;
import models.Evaluacion;
import models.LineaResolucionFAP;
import models.ResolucionFAP;
import models.SolicitudGenerica;


public class ResolucionUtils {
	
	public static class LineasResolucionSortComparator implements Comparator {
		static final int BEFORE = -1;
		static final int AFTER = 1;
		static final int IGUALES = 0;
		
		  public int compare(Object o1, Object o2) {
			  LineaResolucionFAP u1 = (LineaResolucionFAP) o1;
			  LineaResolucionFAP u2 = (LineaResolucionFAP) o2;
			  int result = 0;
			  if (u1.puntuacionBaremacion == null){
				  return BEFORE;
			  }
			  else if (u2.puntuacionBaremacion == null){
				  return AFTER;
			  } else
				  result = u2.puntuacionBaremacion.compareTo(u1.puntuacionBaremacion);
			  
			  if ((result == 0) && (u2.puntuacionBaremacion != 0)){ //Si son iguales, se compara por criterios.
				  result = comparacionPorCriterios(u1, u2);
			  }
			  return result;
		  }
		  public boolean equals(Object o) {
			  return this == o;
		  }
		  
		  public int comparacionPorCriterios(LineaResolucionFAP u1, LineaResolucionFAP u2){
			  Evaluacion evaluacionU1 = Evaluacion.find("select evaluacion from Evaluacion evaluacion where evaluacion.estado = ? and evaluacion.solicitud = ?", "evaluada", u1.solicitud).first();
			  Evaluacion evaluacionU2 = Evaluacion.find("select evaluacion from Evaluacion evaluacion where evaluacion.estado = ? and evaluacion.solicitud = ?", "evaluada", u2.solicitud).first();
			  int result = 0;
			  BaremacionUtils.ordenarCriterios(evaluacionU1.criterios);
			  BaremacionUtils.ordenarCriterios(evaluacionU2.criterios);
			  
			  for (int i = 0; i < evaluacionU1.criterios.size(); i++) {
				 if (!evaluacionU1.criterios.get(i).tipo.jerarquia.contains(".")){ //Si no contiene . es criterio primer orden
					if (evaluacionU1.criterios.get(i).tipo.esIgual(evaluacionU2.criterios.get(i).tipo)){ //misma jerarquia
						result = evaluacionU2.criterios.get(i).valor.compareTo(evaluacionU1.criterios.get(i).valor);
						if (result != 0){
							return result;
						}
					}
				}
			}
			  return IGUALES;
		  }
	}
	
	public static void ordenarLineasResolucion (ResolucionFAP resolucion){
		Collections.sort(resolucion.lineasResolucion, new LineasResolucionSortComparator());
	}
	
	/**
	 * Se comprueba si la resolución tiene solicitudes de firma del portafirma
	 * de la ACIISI que no son del tipo nuevo SolicitudFirmaPortafirma. En caso
	 * afirmartivo, se actualiza la solicitud de firma de portafirma
	 * correspondiente.
	 * @param resolucionFAP
	 */
	public static void actualizarSolicitudesFirmaPortafirmaAntiguasResolucion (ResolucionFAP resolucionFAP) {
		if (((resolucionFAP.idSolicitudFirma != null) && (!resolucionFAP.idSolicitudFirma.isEmpty()))
				&& ((resolucionFAP.solicitudFirmaPortafirma != null) && ((resolucionFAP.solicitudFirmaPortafirma.uriSolicitud == null) || (resolucionFAP.solicitudFirmaPortafirma.uriSolicitud.isEmpty())))) {
			resolucionFAP.solicitudFirmaPortafirma.uriSolicitud = resolucionFAP.idSolicitudFirma;
			resolucionFAP.solicitudFirmaPortafirma.idSolicitante = FapProperties.get("portafirma.usuario");
			resolucionFAP.solicitudFirmaPortafirma.idDestinatario = resolucionFAP.jefeDeServicio;
			resolucionFAP.solicitudFirmaPortafirma.agenteHaceSolicitud = resolucionFAP.hacePeticionPortafirma;
			resolucionFAP.save();
		}
		if (((resolucionFAP.idSolicitudFirmaOficiosRemision != null) && (!resolucionFAP.idSolicitudFirmaOficiosRemision.isEmpty()))
				&& ((resolucionFAP.solicitudFirmaPortafirmaOficioRemision != null) && ((resolucionFAP.solicitudFirmaPortafirmaOficioRemision.uriSolicitud == null) || (resolucionFAP.solicitudFirmaPortafirmaOficioRemision.uriSolicitud.isEmpty())))) {
			resolucionFAP.solicitudFirmaPortafirmaOficioRemision.uriSolicitud = resolucionFAP.idSolicitudFirmaOficiosRemision;
			resolucionFAP.solicitudFirmaPortafirmaOficioRemision.idSolicitante = FapProperties.get("portafirma.usuario");
			resolucionFAP.solicitudFirmaPortafirmaOficioRemision.idDestinatario = resolucionFAP.destinatarioOficioRemisionPortafirma;
			resolucionFAP.solicitudFirmaPortafirmaOficioRemision.agenteHaceSolicitud = resolucionFAP.hacePeticionPortafirmaOficiosRemision;
			resolucionFAP.save();
		}
	}

}
