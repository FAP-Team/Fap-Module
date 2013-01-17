package controllers.fap;

import java.util.ArrayList;
import java.util.List;

import messages.Messages;
import models.Agente;
import models.ResolucionFAP;
import resolucion.ResolucionBase;
import resolucion.ResolucionMultipleTotal;
import resolucion.ResolucionParcial;
import resolucion.ResolucionSimple;
import tags.ComboItem;
import enumerado.fap.gen.EstadoResolucionEnum;
import enumerado.fap.gen.ResolucionesDefinidasEnum;
import models.Agente;
import models.ResolucionFAP;

public class ResolucionControllerFAP extends InvokeClassController {

	/**
	 * Devolverá los posibles tipos de resolución que existan en la app.
	 * Para mostrar en el combo (Y seleccionarlos).
	 */
	public static List<ComboItem> getTiposResolucion() {
		return ComboItem.listFromTableOfTable("resolucionesDefinidas");
	}

	public static ResolucionBase getResolucionObject(Long idResolucion) {
		ResolucionFAP resolucion = ResolucionFAP.findById(idResolucion);
		/**
		 *  Según el tipo de la entidad resolución, deberíamos devolver un tipo u otro
		 *  de los que extiendan de ResoluciónBase.
		 */
		if (resolucion.tipoDefinidoResolucion.equals(ResolucionesDefinidasEnum.multipleTotal.name())) {
			return new ResolucionMultipleTotal(resolucion);
		} else if (resolucion.tipoDefinidoResolucion.equals(ResolucionesDefinidasEnum.multipleParcialExpedientes.name())) {
			return new ResolucionParcial(resolucion);
		} else if (resolucion.tipoDefinidoResolucion.equals(ResolucionesDefinidasEnum.simpleTotal.name())) {
			return new ResolucionSimple(resolucion);
		}
		return new ResolucionBase(resolucion);
	}
	
	/**
	 * Una vez se haya establecido el tipo de resolución,
	 * se inicializan los datos de la resolución.
	 * @param idResolucion
	 */
	public static void inicializaResolucion(Long idResolucion) {
		getResolucionObject(idResolucion).initResolucion(idResolucion);
	}
	
	
	/**
	 * Comprobar que las resoluciones existentes estén finalizadas. En el caso
	 * de que haya una resolución que haya sido finalizada no se podrá crear
	 * una nueva.
	 */
	public static void validarInicioResolucion() {
		List<ResolucionFAP> resoluciones = ResolucionFAP.findAll();
		for (int i = 0; i < resoluciones.size(); i++) {
			if (!resoluciones.get(i).estado.equals(EstadoResolucionEnum.finalizada.name())) {
				play.Logger.error("No se puede crear una nueva resolución habiendo otra activa.");
				Messages.error("No se puede crear una nueva resolución habiendo otra activa.");
				Messages.keep();
			}
		}
	}
	
	/**
	 * 
	 * @return La lista de Jefes de Servicio
	 */
	public static List<String> getJefeServicio() {
		List<Agente> agentes = Agente.findAll();
		List<String> listaJS = new ArrayList<String>();
		for (int i = 0; i < agentes.size(); i++) {
			if (agentes.get(i).roles.contains("jefeServicio")) {
				listaJS.add(agentes.get(i).username);
			}
		}		
		return listaJS;
	}
	
	/**
	 * Obtenemos los jefes de Servicio de la Aplicación
	 * @return Jefes de Servicio
	 */
	public static List<Agente> getJefesServicio () {
		List<Agente> listaJefes = new ArrayList<Agente>();
		listaJefes = Agente.find("select agente from Agente agente join agente.roles rol where rol = 'administrador'").fetch();		
		//listaJefes = Agente.find("select agente from Agente agente join agente.roles rol where rol = 'jefeServicio'").fetch();		
		return listaJefes;
	}
}
