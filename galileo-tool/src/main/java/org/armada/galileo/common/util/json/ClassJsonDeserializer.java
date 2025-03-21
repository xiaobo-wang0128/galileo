package org.armada.galileo.common.util.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ClassJsonDeserializer extends TypeAdapter<Class<?>> {

	public void write(JsonWriter out, Class<?> value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		out.value(value.getName());
	}

	public Class<?> read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		String className = in.nextString();
		try {
			return Class.forName(className);
		} catch (Exception e) {
			return null;
		}
	}
}
