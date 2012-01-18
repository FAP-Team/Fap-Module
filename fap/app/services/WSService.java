package services;

public interface WSService {

	/**
	 * Devuelve true si el servicio tiene conexion 
	 * @return
	 */
	public boolean hasConnection();

	/**
	 * Dirección del servidor al que se conecta el servicio
	 * @return
	 */
	public String getEndPoint();
	
}
