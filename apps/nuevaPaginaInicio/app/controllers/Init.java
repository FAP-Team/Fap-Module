package controllers;

import play.Logger;
import models.ExpedienteGenerico;
import models.Solicitud;
import controllers.fap.AgenteController;
import controllers.InitControllerNuevaPaginaInicio;

public class Init extends InitControllerNuevaPaginaInicio {

	public static Object inicialize() {
		ExpedienteGenerico exp = new ExpedienteGenerico();
		exp.save();
		Logger.info("Creando pruebaInstancia " + exp.id);
		return exp;
	}
}