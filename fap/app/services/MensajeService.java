package services;

import java.util.List;

import es.gobcan.platino.servicios.edmyce.dominio.comun.ArrayOfUriRemesaType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.ArrayOfMensajeType;
import es.gobcan.platino.servicios.edmyce.dominio.mensajes.ResultadoBusquedaMensajeType;
import es.gobcan.platino.servicios.edmyce.mensajes.MensajeException;
import platino.*;

public interface MensajeService {

	public boolean isConfigured();
    
    public void mostrarInfoInyeccion();
    
    public String enviarMensajeOficio (String mensaje, String correo)throws MensajeServiceException;
    
    public ArrayOfUriRemesaType enviarMensajesOficio (List<String> mensajes, String correo) throws MensajeServiceException;
    
    public  String enviarMensajeOficioaVarios (String mensaje, List<String> correos) throws MensajeServiceException;
    
    public ResultadoBusquedaMensajeType buscarMensaje (String canal, String mail, Integer repeticiones) throws MensajeServiceException;
    
    public  ArrayOfMensajeType obtenerMensajes (String uriRemesa) throws MensajeServiceException;
    
 //   public String enviarMensajeArea (String mensaje, String correo)throws MensajeException;
}
