package utils;

import models.Agente;
import tags.ComboItem;

import java.util.ArrayList;
import java.util.List;

public class ComboUtils {

    public static List<ComboItem> gestorAFirmar() {
        List<ComboItem> result = new ArrayList<ComboItem>();
        List<Agente> listaAgentes = Agente.findAll();
        if (listaAgentes != null){
            for (Agente ag : listaAgentes) {
                List<String> roles = ag.getSortRoles();
                if (roles != null){
                    for(String rol : roles){
                        if ((rol != null) && ((rol.equals("gestor") || rol.equals("gestorTenerife") || rol.equals("gestorLasPalmas")))){
                            result.add(new ComboItem(ag.username, ag.username +" - "+ag.name));
                        }
                    }
                }
            }
        }
        return result;
    }

}
