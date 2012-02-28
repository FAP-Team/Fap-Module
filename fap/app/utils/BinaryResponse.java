package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

public class BinaryResponse {
	public String nombre;
	public DataHandler contenido;

	public static BinaryResponse fromFile(File file){
	    BinaryResponse binaryResponse = new BinaryResponse();
	    binaryResponse.nombre = file.getName();
	    binaryResponse.contenido =  new DataHandler(new FileDataSource(file));
	    return binaryResponse;
	}
	
	public byte[] getBytes() throws IOException {
		InputStream is = contenido.getInputStream();
		byte[] result = StreamUtils.is2byteArray(is);
		return result;
	}
	
}
