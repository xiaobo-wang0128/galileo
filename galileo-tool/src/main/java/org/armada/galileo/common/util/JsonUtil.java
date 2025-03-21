package org.armada.galileo.common.util;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.armada.galileo.common.util.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class JsonUtil {

    // Gson源码修改日志：
    // 修改 ReflectiveTypeAdapterFactory 119行，对空字符串进行处理
    // 修改 DefaultDateTypeAdapter 68行，对日期进行简化处理，去除 00:00:00

    // static final GsonBuilder gsonBuilder = new
    // GsonBuilder().setDateFormat("yyyy-MM-dd
    // HH:mm:ss").excludeFieldsWithModifiers(Modifier.PROTECTED, Modifier.STATIC);

    static final GsonBuilder gsonBuilder = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED, Modifier.STATIC, Modifier.FINAL);

    static {

        gsonBuilder.disableHtmlEscaping();

        gsonBuilder.registerTypeAdapter(Long.class, new LongJsonDeserializer());

        gsonBuilder.registerTypeAdapter(Integer.class, new IntegerJsonDeserializer());
        gsonBuilder.registerTypeAdapter(Float.class, new FloatJsonDeserializer());
        gsonBuilder.registerTypeAdapter(Double.class, new DoubleJsonDeserializer());
        gsonBuilder.registerTypeAdapter(BigDecimal.class, new BigDecimalJsonDeserializer());
        // 去首尾空格
        gsonBuilder.registerTypeAdapter(String.class, new StringJsonDeserializer());
        gsonBuilder.registerTypeAdapter(Date.class, new DateJsonSerializer("yyyy-MM-dd HH:mm:ss"));

        gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                Date d = CommonUtil.parse(json.getAsJsonPrimitive().getAsString(), "yyyy-MM-dd HH:mm:ss");
                return d;
            }
        });
    }

    public static void registerDatePraser(String[] formats) {
        DateJsonDeserializer dd = new DateJsonDeserializer(formats);
        gsonBuilder.registerTypeAdapter(Date.class, dd);
    }

    static final GsonBuilder gsonPrettyBuilder = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        gsonPrettyBuilder.disableHtmlEscaping();

        gsonPrettyBuilder.registerTypeAdapter(Long.class, new LongJsonDeserializer());
        gsonPrettyBuilder.registerTypeAdapter(Integer.class, new IntegerJsonDeserializer());
        gsonPrettyBuilder.registerTypeAdapter(Float.class, new FloatJsonDeserializer());
        gsonPrettyBuilder.registerTypeAdapter(Double.class, new DoubleJsonDeserializer());
        gsonPrettyBuilder.registerTypeAdapter(BigDecimal.class, new BigDecimalJsonDeserializer());
    }


    static final GsonBuilder gsonBuilderWeb = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED, Modifier.STATIC, Modifier.FINAL);

    static {
        gsonBuilderWeb.disableHtmlEscaping();

        // 与普通的 json 转换多了下面一行， 用于解决 long 类型传到前端后，前端解析后精度丢失的问题
        gsonBuilderWeb.registerTypeAdapter(Long.class, new LongJsonSerializer());
        gsonBuilderWeb.registerTypeAdapter(Long.class, new LongJsonDeserializer());

        gsonBuilderWeb.registerTypeAdapter(Integer.class, new IntegerJsonDeserializer());
        gsonBuilderWeb.registerTypeAdapter(Float.class, new FloatJsonDeserializer());
        gsonBuilderWeb.registerTypeAdapter(Double.class, new DoubleJsonDeserializer());
        gsonBuilderWeb.registerTypeAdapter(BigDecimal.class, new BigDecimalJsonDeserializer());
        // 去首尾空格
        gsonBuilderWeb.registerTypeAdapter(String.class, new StringJsonDeserializer());
        gsonBuilderWeb.registerTypeAdapter(Date.class, new DateJsonSerializer("yyyy-MM-dd HH:mm:ss"));

        gsonBuilderWeb.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                Date d = CommonUtil.parse(json.getAsJsonPrimitive().getAsString(), "yyyy-MM-dd HH:mm:ss");
                return d;
            }
        });
    }


    private static Logger log = LoggerFactory.getLogger(JsonUtil.class);

    private static Gson gson = gsonBuilder.create();
    private static Gson gsonPretty = gsonPrettyBuilder.create();
    private static Gson gsonWeb = gsonBuilderWeb.create();

    /**
     * 将对象转换成 json 字符串. 带格式美化
     *
     * @param input
     * @return
     */
    public static String toJsonPretty(Object input) {
        if (input == null || input.equals("null")) {
            return null;
        }
        if (input instanceof Optional) {
            input = ((Optional) input).orElse(null);
        }
        if (input == null) {
            return null;
        }
        return gsonPretty.toJson(input);
    }

    /**
     * 将对象转换成 json 字符串
     *
     * @param input
     * @return
     */
    public static String toJson(Object input) {
        if (input == null || input.equals("null")) {
            return null;
        }
        // Gson gson = gsonBuilder.create();
        return gson.toJson(input);
    }


    /**
     * 将对象转换成 json 字符串.web前端专用，主要是将 long 转成 字符输出， 解决前端 long 类型精度丢失的问题
     *
     * @param input
     * @return
     */
    public static String toJson4Web(Object input) {
        if (input == null || input.equals("null")) {
            return null;
        }
        return gsonWeb.toJson(input);
    }

    /**
     * 将json串转换成 对象
     *
     * @param json
     * @param classOfT
     * @return
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            if (json == null || json.matches("\\s*")) {
                return null;
            }
            return gson.fromJson(json, classOfT);
        } catch (Exception e) {
            log.error("json解析出错：" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将json字符串转换成指定的ArrayList对象 <br/>
     * <br/>
     * Type typeOfT = new TypeToken<Collection<Foo>>(){}.getType();
     *
     * @param jsonElement
     * @param typeOf
     * @return
     */
    public static <T> T fromJson(JsonElement jsonElement, Type typeOf) {
        return gson.fromJson(jsonElement, typeOf);
    }
    

    /**
     * 将json字符串转换成指定的ArrayList对象 <br/>
     * <br/>
     * Type typeOfT = new TypeToken<Collection<Foo>>(){}.getType();
     *
     * @param json
     * @param typeOf
     * @return
     */
    public static <T> T fromJson(String json, Type typeOf) {
        if (json == null || json.matches("\\s*")) {
            return null;
        }
        return gson.fromJson(json, typeOf);
    }

    public static boolean mapContain(String jsonMap, String inpu) {
        if (inpu == null) {
            return false;
        }

        Map<String, Object> map = fromJson(jsonMap, new TypeToken<Map<String, Object>>() {
        }.getType());

        return map.get(inpu) != null;
    }

    public static boolean collectionContain(String jsonList, Object input) {
        if (input == null) {
            return false;
        }

        List<Object> list = fromJson(jsonList, new TypeToken<List<Object>>() {
        }.getType());
        for (Object object : list) {
            if (object != null) {
                if (object.toString().equals(input.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 用于判断 json 数组是否为空
     *
     * @param json
     * @return
     */
    public static boolean isEmpty(String json) {
        List<?> list = JsonUtil.fromJson(json, new TypeToken<List<Long>>() {
        }.getType());
        if (json == null || json.isEmpty()) {
            return true;
        }
        return false;
    }

}
