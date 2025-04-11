package org.armada.galileo.autoconfig.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

public class ObjectDefaultValue {

	private static Logger log = LoggerFactory.getLogger(ObjectDefaultValue.class);

	private static List<String> BASE_TYPE = Arrays.asList("short", "byte", "int", "float", "double", "boolean");

	private Type type;

	private Map<String, Boolean> hashExist = new HashMap<String, Boolean>();

	public ObjectDefaultValue(Type type) {
		this.type = type;
	}

	public Object generateMock() {
		return generateMock(type);
	}

	private Object generateMock(Type innerType) {

		try {

			if (BASE_TYPE.contains(innerType.getTypeName())) {

				if (innerType.getTypeName().equals("int")) {
					return new Integer(0);
				} else if (innerType.getTypeName().equals("long")) {
					return 0L;
				} else if (innerType.getTypeName().equals("double")) {
					return 0D;
				} else if (innerType.getTypeName().equals("float")) {
					return 0F;
				} else if (innerType.getTypeName().equals("boolean")) {
					return false;
				}
			}

			Object obj = null;

			String typeName = innerType.getTypeName();

			if (innerType instanceof ParameterizedType) {

				if (typeName.startsWith("java.util.List") || typeName.startsWith("java.util.Set")) {

					List<Object> list = new ArrayList<Object>();

					String innerTypeName = ((ParameterizedType) innerType).getActualTypeArguments()[0].getTypeName();

					log.info("innerTypeName: " + innerTypeName);

					if (innerTypeName.startsWith("java.util.Map")) {
						return null;
					}
					Class<?> cls = Class.forName(innerTypeName);
					if (cls.getName().equals("java.lang.Class")) {
						return null;
					}

					if (innerTypeName.equals(type.getTypeName())) {
						return null;
					}

					list.add(generateMock(Class.forName(innerTypeName)));

					obj = list;
				} else {
					obj = null;
				}
			} else if (typeName.equals("java.lang.String")) {
				obj = "";
			} else if (typeName.equals("java.lang.Integer") || typeName.equals("int")) {
				obj = new Integer(0);
			} else if (typeName.equals("java.lang.Byte") || typeName.equals("byte")) {
				obj = (byte) 0;
			} else if (typeName.equals("java.lang.Float") || typeName.equals("float")) {
				obj = 0F;
			} else if (typeName.equals("java.lang.Double") || typeName.equals("double")) {
				obj = 0D;
			} else if (typeName.equals("java.lang.Long") || typeName.equals("long")) {
				obj = 0L;
			} else if (typeName.equals("java.lang.Boolean") || typeName.equals("boolean")) {
				obj = false;
			} else if (typeName.equals("java.math.BigDecimal")) {
				obj = BigDecimal.ZERO;
			} else if (typeName.equals("java.util.Date")) {
				obj = new Date();
			} else {

				if (typeName.endsWith("[]")) {

					Class<?> cls = Class.forName(typeName.substring(0, typeName.indexOf("[")));

					Object value = generateMock(cls);

					Object array = Array.newInstance(cls, 2);
					Array.set(array, 0, value);
					Array.set(array, 1, value);

					obj = array;
				} else {

					if (hashExist.get(typeName) != null && hashExist.get(typeName)) {
						return null;
					}

					Class<?> cls = Class.forName(typeName);
					if (cls.getName().equals("java.lang.Class")) {
						return null;
					}

					if (cls.getName().equals("java.lang.Boolean")) {
						return "true";
					}

					try {
						obj = cls.newInstance();
					} catch (Exception e) {
						log.warn("无法实例化:" + cls.getName());
						return null;
					}

					Field[] fs = cls.getDeclaredFields();

					hashExist.put(typeName, true);

					for (Field field : fs) {

						if (field.getType().getName().equals(type.getTypeName())) {
							continue;
						}

						ReflectionUtils.makeAccessible(field);

						Object innerFieldValue = generateMock(field.getGenericType());

						try {
							ReflectionUtils.setField(field, obj, innerFieldValue);
						} catch (Exception e) {
							log.error(e.getMessage());
						}
					}

				}
			}

			return obj;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
