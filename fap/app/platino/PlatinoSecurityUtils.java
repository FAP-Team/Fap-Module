package platino;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;

public class PlatinoSecurityUtils {

	public static String encriptarPassword(String password) throws Exception {
		byte[] passArray = password.getBytes("UTF-16BE");
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(passArray);
		byte[] digest = md.digest();

		return Base64Utility.encode(digest);
	}
	
	public static String encriptarPasswordComunicacionesInternas (String password){
		byte[] passArray;
		try {
			passArray = password.getBytes("UTF-16BE");
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(passArray);
			byte[] digest = md.digest();
			//org.apache.commons.codec.binary.Base64 b64 = new org.apache.commons.codec.binary.Base64();
			//Muestra los numeros en unicode si los emparejas de 2 en 2
//			for (byte b : digest) {
//				System.out.println("Digest: "+b);
//				System.out.println("Int.toString: "+Integer.toString((b&0xff)+0x100, 16).substring(1));
//				
//			}
			
		 	// En este punto tenemos codificado en MD5 el password
		 	String passMD5=Base64Utility.encode(digest);
		 	  
		 	// Ahora necesitamos transformarlo al formato unicode aceptado por HiperReg
		 	byte[] passASCII = Base64Utility.decode(passMD5.getBytes("US-ASCII").toString());
			String hiperpasswordUNICODE = new String(passASCII, "UTF-16LE");
			System.out.println("Encriptado: "+hiperpasswordUNICODE);
			return hiperpasswordUNICODE;
		} catch (UnsupportedEncodingException e) {
			System.out.println("Error codificando");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error en el digest");
			e.printStackTrace();
		} catch (Base64Exception e) {
			System.out.println("Error decodificando");
			e.printStackTrace();
		}
		return "";
	}
	
	public static byte[] obtenerHash(InputStream documento) throws Exception {
		return obtenerHash(toByteArray(documento));
	}

	public static byte[] obtenerHash(byte[] documento) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] hash = md.digest(documento);
		return hash;
	}

	private static byte[] toByteArray(InputStream inputStream)
			throws IOException {
		ByteArrayOutputStream tmp = new ByteArrayOutputStream();
		copyStream(inputStream, tmp);
		return tmp.toByteArray();
	}

	private static void copyStream(InputStream origen, OutputStream destino) throws IOException {
		byte[] data = new byte[4096];
		int l = 0;
		while ((l = origen.read(data)) >= 0)
			destino.write(data, 0, l);
	}
}
