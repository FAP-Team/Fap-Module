package models;

import org.joda.time.DateTime;

import utils.BinaryResponse;

public class JustificanteRegistro {

    private BinaryResponse documento;
    
    private DateTime fechaRegistro;
    
    private String unidadOrganica;
    
    private String numeroRegistro;
    
    private String numeroRegistroGeneral;

    public JustificanteRegistro(BinaryResponse documento, DateTime fechaRegistro, String unidadOrganica,
            String numeroRegistro, String numeroRegistroGeneral) {
        super();
        this.documento = documento;
        this.fechaRegistro = fechaRegistro;
        this.unidadOrganica = unidadOrganica;
        this.numeroRegistro = numeroRegistro;
        this.numeroRegistroGeneral = numeroRegistroGeneral;
    }

    public BinaryResponse getDocumento() {
        return documento;
    }

    public DateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public String getUnidadOrganica() {
        return unidadOrganica;
    }

    public String getNumeroRegistro() {
        return numeroRegistro;
    }

    public String getNumeroRegistroGeneral() {
        return numeroRegistroGeneral;
    }

    
    
}
