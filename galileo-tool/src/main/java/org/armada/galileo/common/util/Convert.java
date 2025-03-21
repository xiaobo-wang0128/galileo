package org.armada.galileo.common.util;


public class Convert {

    public static boolean asBoolean(Object value) {
        return asBoolean(value, false);
    }

    public static boolean asBoolean(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        try {
            return Boolean.valueOf(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static byte asByte(Object value) {
        return asByte(value, (byte) 0);
    }

    public static byte asByte(Object value, Byte defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Byte) {
            return (Byte) value;
        }

        try {
            return Byte.valueOf(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static char asChar(Object value) {
        return asChar(value, '0');
    }

    public static char asChar(Object value, char defaultValue) {
        if (value == null) {
            return defaultValue;
        }


        try {
            return value.toString().toCharArray()[0];
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static Double asDouble(Object value) {
        return asDouble(value, 0d);
    }

    public static Double asDouble(Object value, Double defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Double) {
            return (Double) value;
        }


        try {
            return Double.valueOf(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static Float asFloat(Object value) {
        return asFloat(value, 0f);
    }

    public static Float asFloat(Object value, Float defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Float) {
            return (Float) value;
        }


        try {
            return Float.valueOf(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static Integer asInt(Object value) {
        return asInt(value, 0);
    }

    public static Integer asInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        try {
            return Double.valueOf(value.toString()).intValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static Long asLong(Object value) {
        return asLong(value, 0l);
    }

    public static Long asLong(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        try {
            String valueString = value.toString();
            if (valueString.contains(".")) {
                return Double.valueOf(valueString).longValue();
            } else {
                return Long.parseLong(valueString);
            }
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static short asShort(Object value) {
        return asShort(value, (short) 0);
    }

    public static short asShort(Object value, Short defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Short) {
            return (Short) value;
        }

        try {
            return Short.valueOf(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String asString(Object value) {
        return asString(value, "");
    }

    public static String asString(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    public static Object asType(Object targetType, Object value) {
        return targetType;
    }

    public static Object asType(Object targetType, Object value, Object defaultValue) {
        return targetType;
    }

}
