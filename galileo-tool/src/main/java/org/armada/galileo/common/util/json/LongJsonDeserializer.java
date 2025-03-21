package org.armada.galileo.common.util.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class LongJsonDeserializer implements JsonDeserializer<Long> {

	public Long deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		String string = json.getAsString();
		try {
			return Long.valueOf(string);
		} catch (Exception e) {
		}
		return null;
	}
}
