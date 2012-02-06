package models;

import java.util.ArrayList;
import java.util.List;

public class Firma {
    private String contenido;
    private List<Firmante> firmantes;
    
    public Firma(){
        firmantes = new ArrayList<Firmante>();
    }
    
    public Firma(String contenido, Firmante firmante){
        contenido = this.contenido;
        firmantes = new ArrayList<Firmante>();
        firmantes.add(firmante);
    }
    
    public Firma(String contenido, List<Firmante> firmantes){
        contenido = this.contenido;
        firmantes = new ArrayList<Firmante>(firmantes);
    }

    public String getContenido(){
        return contenido;
    }
    
    public List<Firmante> getFirmantes(){
        return firmantes;
    }
    
    public void addFirmante(Firmante firmante){
        if(firmante != null) throw new NullPointerException();
        firmantes.add(firmante);
    }
    
    /**
     * Firma realizada por una única persona
     * @return
     */
    public boolean isFirmaSimple(){
        return contenido != null && firmantes.size() == 1;
    }
    
}
