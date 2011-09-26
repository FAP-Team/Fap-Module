package utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class CharsetUtils {

	public static String fromUTF82ISO(String str){
		Charset from = Charset.forName("UTF-8");
		Charset to = Charset.forName("ISO-8859-1");
		return convert(str, from, to);
	}
	
	public static String fromISO2UTF8(String str){
		Charset from = Charset.forName("ISO-8859-1");
		Charset to = Charset.forName("UTF-8");
		return convert(str, from, to);		
	}
	
	public static String convert(String str, Charset from, Charset to){
		byte[] f = str.getBytes(from);
		String result = new String(f, to);
		return result;
	}
	
}
