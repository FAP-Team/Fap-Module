package serializer;

import java.lang.reflect.Type;

import org.joda.time.DateTime;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonDateTimeSerializer implements JsonSerializer<DateTime> {

	public JsonElement serialize(DateTime value, Type type, JsonSerializationContext arg2) {
		return new JsonPrimitive(value.toString());
	}

}
