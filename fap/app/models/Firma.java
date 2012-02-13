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
        this.contenido = contenido;
        this.firmantes = new ArrayList<Firmante>();
        this.firmantes.add(firmante);
    }
    
    public Firma(String contenido, List<Firmante> firmantes){
        this.contenido = contenido;
        this.firmantes = new ArrayList<Firmante>(firmantes);
    }

    public String getContenido(){
        return this.contenido;
    }
    
    public List<Firmante> getFirmantes(){
        return this.firmantes;
    }
    
    public void addFirmante(Firmante firmante){
        if(firmante != null) throw new NullPointerException();
        this.firmantes.add(firmante);
    }
    
    /**
     * Firma realizada por una única persona
     * @return
     */
    public boolean isFirmaSimple(){
        return firmantes.size() == 1;
    }
    
}
