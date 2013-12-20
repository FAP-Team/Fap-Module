package services;
 
 public class VerificarDatosServiceException extends Exception {
   public VerificarDatosServiceException(String msg){
     super(msg);
   }
   
   public VerificarDatosServiceException(String message, Throwable cause){
         super(message, cause);
     }
 }