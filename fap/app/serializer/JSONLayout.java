package serializer;

import models.Agente;
import models.Log;

import org.apache.log4j.Layout;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import controllers.fap.SecureController;
import emails.Mails;

import java.util.Date;
import java.text.DateFormat;
import java.math.BigInteger;

public class JSONLayout extends Layout
{
    // Requested options.
    private boolean prettyPrint = true;
    private boolean ignoreThrowable = false;

    // Active options.
    private boolean activePrettyPrint = prettyPrint;
    private boolean activeIgnoreThrowable = ignoreThrowable;

    public java.lang.String format(LoggingEvent loggingEvent) {
        // First we create a JSON logging object.
        JsonObject lLogObj = new JsonObject();
        // Now we can fill the object with the event attributes.

        String user = "anónimo";
        if (loggingEvent.getMDC("username") != null)
        	user = loggingEvent.getMDC("username").toString();

        lLogObj.add("time", new JsonPrimitive(new ISO8601DateFormat().format(new Date(loggingEvent.timeStamp))));
        lLogObj.add("level", new JsonPrimitive(loggingEvent.getLevel().toString()));
        lLogObj.add("class_", new JsonPrimitive(loggingEvent.getLoggerName()));
        lLogObj.add("user", new JsonPrimitive(user));
        lLogObj.add("message", new JsonPrimitive(loggingEvent.getMessage().toString()));
        
        String lExcMsgs = "";
        ThrowableInformation lTi = loggingEvent.getThrowableInformation();
        if(lTi != null && !activeIgnoreThrowable)
        {

            lExcMsgs = lTi.getThrowable().getMessage() + "<br/>";
            String[] lThrRep = loggingEvent.getThrowableStrRep();
            if(lThrRep != null && lThrRep.length > 0)
            {
                for (String lMsg : lThrRep)
                {
                    lExcMsgs += lMsg + "<br/>";
                }

            }
        } 
        lLogObj.add("trace", new JsonPrimitive(lExcMsgs));
       
        return lLogObj + "\n";
    }

    
    
    public void activateOptions()
    {
        activePrettyPrint = prettyPrint;
        activeIgnoreThrowable = ignoreThrowable;
    }

    public boolean ignoresThrowable()
    {
        return ignoreThrowable;
    }

    public boolean isPrettyPrint()
    {
        return prettyPrint;
    }

    public void setPrettyPrint(String prettyPrint)
    {
        this.prettyPrint = Boolean.parseBoolean(prettyPrint);
    }

    public boolean isIgnoreThrowable()
    {
        return ignoreThrowable;
    }

    public void setIgnoreThrowable(String ignoreThrowable)
    {
        this.ignoreThrowable = Boolean.parseBoolean(ignoreThrowable);
    }
}
