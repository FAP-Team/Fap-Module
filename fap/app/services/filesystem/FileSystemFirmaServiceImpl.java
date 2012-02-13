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
        FileSystemFirma fsFirma = FileSystemFirma.encode("APP", "APPNIF", new String(texto));
        return fsFirma.encode();
    }

    @Override
    public boolean validarFirmaTexto(byte[] texto, String firma) throws FirmaServiceException {
        FileSystemFirma decodeFirma = FileSystemFirma.decode(firma);
        return new String(texto).equals(decodeFirma.getFirma());
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
        FileSystemFirma decode = FileSystemFirma.decode(firma);
        InfoCert info = new InfoCert();
        info.nombrecompleto = decode.getNombre();
        info.nif = decode.getNif();
        return info;
    }

 
}
