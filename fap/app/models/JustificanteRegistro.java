package models;

import org.joda.time.DateTime;

import utils.BinaryResponse;

public class JustificanteRegistro {

    private String numeroRegistro;
    
    private DateTime fechaRegistro;
    
    private BinaryResponse documento;

    public JustificanteRegistro(String numeroRegistro, DateTime fechaRegistro, BinaryResponse documento) {
        super();
        this.numeroRegistro = numeroRegistro;
        this.fechaRegistro = fechaRegistro;
        this.documento = documento;
    }

    public String getNumeroRegistro() {
        return numeroRegistro;
    }

    public DateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public BinaryResponse getDocumento() {
        return documento;
    }
    
}
