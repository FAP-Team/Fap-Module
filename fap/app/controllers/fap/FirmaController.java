package controllers.fap;

import javax.inject.Inject;

import platino.InfoCert;
import play.modules.guice.InjectSupport;
import services.FirmaService;
import messages.Messages;
import models.Firma;
import models.Firmante;

@InjectSupport
public class FirmaController {

    @Inject
    private static FirmaService firmaService;
    
    public static Firmante getFirmanteFromFirma(String xmlSignature) {
        try {
            InfoCert certificado = firmaService.extraerCertificado(xmlSignature);
           
            Firmante firmante = new Firmante();
            firmante.nombre = certificado.getNombreCompleto();
            firmante.idvalor = certificado.getId();
            
            return firmante;
        }catch(Exception e){
            String msg = "Error extrayendo el certificado de la firma";
            Messages.error(msg);
            play.Logger.error(e, msg);
        }
        return null;
    }
    
}
