package services.filesystem;

import play.libs.Codec;
import services.FirmaServiceException;

public class FileSystemFirma {
        private String nombre;
        private String nif;
        private String data;
        
        private FileSystemFirma(String nombre, String nif, String data){
            this.nombre = nombre;
            this.nif = nif;
            this.data = data;
        }
        
        public String encode(){
            return Codec.encodeBASE64(String.format("%s#%s#%s", nombre, nif, data));
        }
        
        public String toString(){
            return encode();
        }
        
        public String getNombre() {
            return nombre;
        }

        public String getNif() {
            return nif;
        }

        public String getFirma() {
            return data;
        }

        public static FileSystemFirma encode(String nombre, String nif, String data){
            FileSystemFirma encode = new FileSystemFirma(nombre, nif, data);
            return encode;
        }
        
        public static FileSystemFirma decode(String firma) throws FirmaServiceException {
            String decode = new String(Codec.decodeBASE64(firma));
            
            String[] splitted = decode.split("#");
            if(splitted.length == 3){
                //
                return new FileSystemFirma(splitted[0], splitted[1], splitted[2]);
            }else if(splitted.length == 1){
                //la firma no tiene información
                return new FileSystemFirma("", "", splitted[0]);
            }else{
                throw new FirmaServiceException("la firma no es correcta");
            }
        }
}
