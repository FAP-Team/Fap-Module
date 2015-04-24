package utils;

import java.util.ArrayList;
import java.util.List;

import models.Agente;
import tags.ComboItem;

public class ComboUtils {

    public static List<ComboItem> gestorAFirmar() {
        List<ComboItem> result = new ArrayList<ComboItem>();
        List<Agente> listaAgentes = Agente.findAll();
        if (listaAgentes != null){
            for (Agente ag : listaAgentes) {
            	if (ag.roles.contains("gestor") || ag.roles.contains("gestorTenerife") || ag.roles.contains("gestorLasPalmas")) {
        			result.add(new ComboItem(ag.username, ag.username +" - "+ag.name));
                }
            }
        }
        return result;
    }

}
