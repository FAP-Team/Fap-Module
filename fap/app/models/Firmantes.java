package models;

import java.util.ArrayList;
import java.util.List;

public class Firmantes {

    private List<Firmante> firmantes;
    
    public Firmantes(List<Firmante> firmantes){
        this.firmantes = new ArrayList<Firmante>(firmantes);
    }
    
    public boolean hanFirmadoTodos() {
        boolean multiple = true;
        for (Firmante f : firmantes) {
            // Firmante único que ya ha firmado
            if (f.cardinalidad.equals("unico") && f.fechaFirma != null)
                return true;

            // Uno de los firmantes multiples no ha firmado
            if (f.cardinalidad.equals("multiple") && f.fechaFirma == null)
                multiple = false;
        }

        // En el caso de que no haya firmado ningún único
        // Se devuelve true si todos los múltiples han firmado
        return multiple;
    }

    /**
     * Borra una lista de firmantes, borrando cada uno de los firmantes y
     * vaciando la lista
     * 
     * @param firmantes
     */
    public void borrarFirmantes(List<Firmante> firmantes) {
        List<Firmante> firmantesBack = new ArrayList<Firmante>(firmantes);
        firmantes.clear();

        for (Firmante f : firmantesBack)
            f.delete();
    }

    public static Firmantes calcularFirmanteFromSolicitante(Solicitante solicitante) {
        if (solicitante == null)
            throw new NullPointerException();

        List<Firmante> firmantes = new ArrayList<Firmante>();

        // Solicitante de la solicitud
        Firmante firmanteSolicitante = new Firmante(solicitante, "unico");
        firmantes.add(firmanteSolicitante);

        // Comprueba los representantes
        if (solicitante.isPersonaFisica() && solicitante.representado) {
            // Representante de persona física
            Firmante representante = new Firmante(solicitante.representante, "representante", "unico");
            firmantes.add(representante);
        } else if (solicitante.isPersonaJuridica()) {
            // Representantes de la persona jurídica
            for (RepresentantePersonaJuridica r : solicitante.representantes) {
                String cardinalidad = null;
                if (r.tipoRepresentacion.equals("mancomunado")) {
                    cardinalidad = "multiple";
                } else if ((r.tipoRepresentacion.equals("solidario"))
                        || (r.tipoRepresentacion.equals("administradorUnico"))) {
                    cardinalidad = "unico";
                }
                Firmante firmante = new Firmante(r, "representante", cardinalidad);
                firmantes.add(firmante);
            }
        }
        return new Firmantes(firmantes);
    }
    
    public boolean containsFirmanteConId(String id){
        for(Firmante firmante : firmantes){
            if(firmante.idvalor != null && firmante.idvalor.equals(id)){
                return true;
            }
        }
        return false;
    }
    
}
