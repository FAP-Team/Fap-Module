package utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.libs.Crypto;
import play.mvc.Router;

public class AedUtils {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private static final Pattern pattern = Pattern.compile("date=\\[(.*?)\\]&uri=\\[(.*?)\\]");
	
	private static String actualDate(){
		return dateFormat.format(new Date());
	}
	
	public static String encriptarUri(String uri){
		String tmp = "date=[" + actualDate() + "]&uri=[" + uri + "]";
		return Crypto.encryptAES(tmp);
	}
	
	public static String desencriptarUri(String uri){
		String tmp = Crypto.decryptAES(uri);
		// Compile and use regular expression
		Matcher matcher = pattern.matcher(tmp);
		if (matcher.find()) {
			if(matcher.group(1).equals(actualDate())){
				return matcher.group(2);
			}
		}
		return null;
	}
	
	public static String crearUrl(String uri){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("k", encriptarUri(uri));
		return Router.getFullUrl("fap.DescargasAedController.descargar", params);		
	}
}
