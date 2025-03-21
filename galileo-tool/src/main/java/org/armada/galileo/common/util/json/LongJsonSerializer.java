package org.armada.galileo.common.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * @author xiaobo
 * @date 2022/4/25 9:56 PM
 */
public class LongJsonSerializer implements JsonSerializer<Long> {
    @Override
    public JsonElement serialize(Long src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }
        if (src > 9007199254740991L) {
            // 1785130080777879554
            // 9007199254740991L
            return new JsonPrimitive(String.valueOf(src));
        } else {
            return new JsonPrimitive(src);
        }
    }
}
