package org.armada.galileo.common.util.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class StringJsonDeserializer implements JsonDeserializer<String> {

	public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		String string = json.getAsString();
		
		if(string==null || string.matches("\\s*")) {
			return null;
		}
		
		return string.trim();
		
		
	}
}
