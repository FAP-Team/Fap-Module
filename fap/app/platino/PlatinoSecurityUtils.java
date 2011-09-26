package platino;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import org.apache.cxf.common.util.Base64Utility;

public class PlatinoSecurityUtils {

	public static String encriptarPassword(String password) throws Exception {
		byte[] passArray = password.getBytes("UTF-16LE");
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(passArray);
		byte[] digest = md.digest();
		return Base64Utility.encode(digest);
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
