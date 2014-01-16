package platino;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import play.libs.Codec;


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
			passArray = password.getBytes("UTF-16LE");
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(passArray);
			byte[] digest = md.digest();
			String passMD5= Codec.encodeBASE64(digest);
			byte[] passASCII = Codec.decodeBASE64(passMD5);
			String hiperpasswordUNICODE = new String(passASCII, "UTF-16LE");

			return hiperpasswordUNICODE;
		} catch (UnsupportedEncodingException e) {
			play.Logger.error("Error Codificando el password en Comunicaciones Internas");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			play.Logger.error("Error Codificando en Comunicaciones Internas con el algoritmo MD5");
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
