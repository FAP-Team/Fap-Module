package utilsTesting;

import models.DefinicionMetadatos;
import models.Documento;
import models.TipoDocumento;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Fixtures {
    public static final String URI_DOCUMENTO = "uri://de-prueba";
    public static final String DEF_METADATOS_NOMBRE = "NuevaDefinicionMD";

    public static final List<String> DEF_METADATOS_VALORES_POSIBLES =
            Arrays.asList("valor1", "valor2", "valor3", "valor4");
    public static final List<String> DEF_METADATOS_VALORES_POR_DEFECTO =
            Arrays.asList("valor1", "valor3");
    public static final List<String> DEF_METADATOS_VALORES_NO_VALIDOS =
            Arrays.asList("valor2", "valor4");

    public static DefinicionMetadatos getNuevaDefinicion() {
        DefinicionMetadatos dmd = new DefinicionMetadatos();
        dmd.nombre = DEF_METADATOS_NOMBRE;
        dmd.valoresPosibles =
                new ArrayList<String> (DEF_METADATOS_VALORES_POSIBLES);
        dmd.valoresPorDefecto =
                new ArrayList<String> (DEF_METADATOS_VALORES_POR_DEFECTO);
        return dmd;
    }

    public static Documento getNuevoDocumento() {
        Documento doc = new Documento();
        return doc;
    }

    public static TipoDocumento getNuevoTipoDocumento() {
        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.uri = URI_DOCUMENTO;
        return tipoDocumento;
    }
}