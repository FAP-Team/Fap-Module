package services;
 
 public class ComunicacionesInternasServiceException extends Exception {
   public ComunicacionesInternasServiceException(String msg){
     super(msg);
   }
   
   public ComunicacionesInternasServiceException(String message, Throwable cause){
         super(message, cause);
     }
 }