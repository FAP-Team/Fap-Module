package utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.Play;
import play.libs.Crypto;
import play.libs.Time;
import play.mvc.Http;
import play.mvc.Scope;

public class SessionUtils {

	//static Pattern sessionParser = Pattern.compile("\u0000([^:]*):([^\u0000]*)\u0000");
    static Pattern sessionParser = Pattern.compile("(([^&]+)=([^&]+))+");
    static final String TS_KEY = "___TS";

    public static Scope.Session parseSession(String sessionCookieValue) {
        Scope.Session session = new Scope.Session();
        if (sessionCookieValue != null && !sessionCookieValue.trim().equals("")) {
            String sign = sessionCookieValue.substring(0, sessionCookieValue.indexOf("-"));
            String data = sessionCookieValue.substring(sessionCookieValue.indexOf("-") + 1);
            if (sign.equals(Crypto.sign(data, Play.secretKey.getBytes()))) {
                try {
                    String sessionData = URLDecoder.decode(data, "utf-8");
                    Matcher matcher = sessionParser.matcher(sessionData);
                    while (matcher.find()) {
                        session.put(matcher.group(2), matcher.group(3)); 
                    }
                }catch(Exception e){
                    throw new RuntimeException(e);
                }
            }
            if (Scope.COOKIE_EXPIRE != null) {
                // Verify that the session contains a timestamp, and that it's
                // not expired
                if (!session.contains(TS_KEY)) {
                    session = new Scope.Session();
                } else {
                    if (Long.parseLong(session.get(TS_KEY)) < System.currentTimeMillis()) {
                        // Session expired
                        session = new Scope.Session();
                    }
                }
                session.put(TS_KEY, System.currentTimeMillis() + (Time.parseDuration(Scope.COOKIE_EXPIRE) * 1000));
            }
        } else {
            // no previous cookie to restore; but we may have to set the
            // timestamp in the new cookie
            if (Scope.COOKIE_EXPIRE != null) {
                session.put(TS_KEY, System.currentTimeMillis() + (Time.parseDuration(Scope.COOKIE_EXPIRE) * 1000));
            }
        }

        return session;
    }
    
    public static String saveSession(Scope.Session session){
        StringBuilder value = new StringBuilder();
        Map<String, String> sessionMap = session.all();
        for (String key : sessionMap.keySet()) {
            value.append("\u0000");
            value.append(key);
            value.append(":");
            value.append(sessionMap.get(key));
            value.append("\u0000");
        }
        String sessionData;
        try {
            sessionData = URLEncoder.encode(value.toString(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String sign = Crypto.sign(sessionData, Play.secretKey.getBytes());
        return sign;
    }

}
