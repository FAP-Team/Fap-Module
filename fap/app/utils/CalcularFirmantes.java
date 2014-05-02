package utils;

import java.util.ArrayList;
import java.util.List;

import models.Agente;
import models.Firmante;

public class CalcularFirmantes {

	public static List<Firmante> getGestoresComoFirmantes () {
		ArrayList<Firmante> firmantes = new ArrayList<Firmante>();
		List<Agente> agentes = Agente.findAll();
		for (Agente agente : agentes) {
			if (agente.roles.contains("gestor") || agente.roles.contains("gestorTenerife") || agente.roles.contains("gestorLasPalmas")) {
				Firmante f = new Firmante(agente);
				firmantes.add(f);
			}
		}
		return firmantes;
	}
	
	public static List<Firmante> getGestorComoFirmante (String valorDocumento) {
		ArrayList<Firmante> firmantes = new ArrayList<Firmante>();
		List<Agente> agentes = Agente.find("select agente from Agente agente where agente.username=?", valorDocumento).fetch();
		for (Agente agente : agentes) {
			if (agente.roles.contains("gestor") || agente.roles.contains("gestorTenerife") || agente.roles.contains("gestorLasPalmas")) {
				Firmante f = new Firmante(agente);
				firmantes.add(f);
				return firmantes;
			}
		}
		return firmantes;
	}
}
