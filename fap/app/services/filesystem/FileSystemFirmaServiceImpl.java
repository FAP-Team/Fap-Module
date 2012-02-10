package services.filesystem;

import java.util.ArrayList;
import java.util.List;

import platino.InfoCert;
import play.libs.Codec;
import play.libs.Crypto;
import services.FirmaService;
import services.FirmaServiceException;

public class FileSystemFirmaServiceImpl implements FirmaService {

    @Override
    public boolean isConfigured() {
        //No necesita configuración
        return true;
    }

    public List<String> getFirmaEnClienteJS() {
        List<String> jsclient = new ArrayList<String>();
        jsclient.add("/public/javascripts/firma/base64.js");
        jsclient.add("/public/javascripts/firma/firma.js");
        jsclient.add("/public/javascripts/firma/firma-fs.js");
        return jsclient;
    }
    
    @Override
    public String firmarTexto(byte[] texto) throws FirmaServiceException {
        FSFirma fsFirma = new FSFirma("APP", "appnif", new String(texto));
        return fsFirma.encode();
    }

    @Override
    public boolean validarFirmaTexto(byte[] texto, String firma) throws FirmaServiceException {
        FSFirma decodeFirma = FSFirma.decode(firma);
        play.Logger.info("texto %s firma %s", texto, decodeFirma.firma);
        return new String(texto).equals(decodeFirma.firma);
    }
    
    @Override
    public String firmarDocumento(byte[] contenidoDocumento) throws FirmaServiceException {
        return firmarTexto(contenidoDocumento); 
    }

    @Override
    public boolean validarFirmaDocumento(byte[] contenidoDocumento, String firma) throws FirmaServiceException {
        return false;
    }

    @Override
    public InfoCert extraerCertificado(String firma) throws FirmaServiceException {
        FSFirma decode = FSFirma.decode(firma);
        InfoCert info = new InfoCert();
        info.nombrecompleto = decode.nombre;
        info.nif = decode.nif;
        return info;
    }

    private static class FSFirma {
        private String nombre;
        private String nif;
        private String firma;
        
        public FSFirma(String nombre, String nif, String firma){
            this.nombre = nombre;
            this.nif = nif;
            this.firma = firma;
        }
        
        private String encode(){
            return String.format("%s#%s#%s", nombre, nif, firma);
        }
        
        public String toString(){
            return encode();
        }
        
        public static FSFirma decode(String firma) throws FirmaServiceException {
            String decode = new String(Codec.decodeBASE64(firma));
            String[] splitted = decode.split("#");
            if(splitted.length != 3)
                throw new FirmaServiceException("La firma no es correta");
            return new FSFirma(splitted[0], splitted[1], splitted[2]);
        }
        
    }
}
