package services;

 public class SVDServiceException extends Exception {
   public SVDServiceException(String msg){
     super(msg);
   }

   public SVDServiceException(String message, Throwable cause){
         super(message, cause);
     }
 }