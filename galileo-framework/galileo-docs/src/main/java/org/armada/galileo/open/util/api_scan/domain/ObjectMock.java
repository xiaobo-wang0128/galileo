package org.armada.galileo.open.util.api_scan.domain;//package org.armada.galileo.document.domain;
//
//import org.armada.galileo.document.util.doc.OpenApiGenerate;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.util.ReflectionUtils;
//
//import java.lang.reflect.Array;
//import java.lang.reflect.Field;
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.util.*;
//
//public class ObjectMock {
//
//
//    private static Logger log = LoggerFactory.getLogger(ObjectMock.class);
//
//
//    private String type;
//
//    private Map<String, Boolean> hashExist = new HashMap<String, Boolean>();
//
//    public ObjectMock(String type) {
//        this.type = type;
//    }
//
//    public Object generateMock() {
//        return generateMock(type);
//    }
//
//    private Object generateMock(String typeName) {
//
//        try {
//
//
//            if (OpenApiGenerate.Base_Type.contains(typeName)) {
//                if (typeName.equals("int") || typeName.equals("Integer")) {
//                    return 1;
//                } else if (typeName.equals("byte") || typeName.equals("Byte")) {
//                    return (byte) 1;
//                } else if (typeName.equals("long") || typeName.equals("Long")) {
//                    return 1;
//                } else if (typeName.equals("double") || typeName.equals("Double") || typeName.equals("Bigdecimal")) {
//                    return 1.12;
//                } else if (typeName.equals("float") || typeName.equals("Float")) {
//                    return 1.12;
//                } else if (typeName.equals("boolean") || typeName.equals("Boolean")) {
//                    return false;
//                } else if (typeName.equals("Date")) {
//                    return new Date();
//                }
//            }
//
//
//            OpenApiGenerate.TypeDesc typeDesc = OpenApiGenerate.getTypeDesc(typeName);
//
//
//            if (typeName.equals("List") || typeName.equals("Set")) {
//                List<Object> list = new ArrayList<Object>();
//                if(typeDesc.getHasInnerClass()){
//                    list.add(generateMock(typeDesc.getInnerType()));
//                }
//            }
//            else if(typeName.equals("Map")){
//                Map<String,Object> map = new HashMap<>();
//                return map;
//            }
//            // object
//            else{
//
//
//                if(typeDesc.getHasInnerClass()){
//                    list.add(generateMock(typeDesc.getInnerType()));
//                }
//
//            }
//
//
//            Object obj = null;
//
//
//            if (typeDesc.getHasInnerClass()) {
//
//                if (typeName.startsWith("java.util.List") || typeName.startsWith("java.util.Set")) {
//
//                    List<Object> list = new ArrayList<Object>();
//
//                    String innerTypeName = ((ParameterizedType) typeName).getActualTypeArguments()[0].getTypeName();
//
//                    log.info("innerTypeName: " + innerTypeName);
//
//                    if (innerTypeName.startsWith("java.util")) {
//                        return null;
//                    }
//                    Class<?> cls = Class.forName(innerTypeName);
//                    if (cls.getName().equals("java.lang.Class")) {
//                        return null;
//                    }
//
//                    if (innerTypeName.equals(type.getTypeName())) {
//                        return null;
//                    }
//
//                    list.add(generateMock(cls));
//
//                    obj = list;
//                } else if (typeName.startsWith(java.util.Map.class.getName())) {
//                    obj = new HashMap<>();
//                } else if (typeName.startsWith(java.util.TreeMap.class.getName())) {
//                    obj = new TreeMap<>();
//                } else {
//                    obj = null;
//                }
//            } else if (typeName.equals("java.lang.String")) {
//                obj = "string value";
//            } else if (typeName.equals("java.lang.Integer") || typeName.equals("int")) {
//                obj = 0;
//            } else if (typeName.equals("java.lang.Byte") || typeName.equals("byte")) {
//                obj = 1;
//            } else if (typeName.equals("java.lang.Float") || typeName.equals("float")) {
//                obj = 1.0F;
//            } else if (typeName.equals("java.lang.Double") || typeName.equals("double")) {
//                obj = 1.0D;
//            } else if (typeName.equals("java.lang.Long") || typeName.equals("long")) {
//                obj = 0L;
//            } else if (typeName.equals("java.lang.Boolean") || typeName.equals("boolean")) {
//                obj = Math.random() > 0.5 ? true : false;
//            } else if (typeName.equals("java.math.BigDecimal")) {
//                obj = 1.0;
//            } else if (typeName.equals("java.util.Date")) {
//                obj = new Date();
//            } else {
//
//                if (typeName.endsWith("[]")) {
//
//                    String tmpInnerName = typeName.substring(0, typeName.indexOf("["));
//
//                    if (tmpInnerName.equals("int")) {
//                        return new int[]{1, 2};
//                    } else if (tmpInnerName.equals("byte")) {
//                        return new byte[]{(byte) 1, (byte) 2};
//                    } else if (tmpInnerName.equals("long")) {
//                        return new long[]{1L, 2L};
//                    } else if (tmpInnerName.equals("double")) {
//                        return new double[]{1.1d, 2.2d};
//                    } else if (tmpInnerName.equals("float")) {
//                        return new float[]{1.1f, 2.2f};
//                    } else if (tmpInnerName.equals("boolean")) {
//                        return new boolean[]{false, false};
//                    } else {
//                        Class<?> cls = Class.forName(tmpInnerName);
//                        Object value = generateMock(cls);
//
//                        Object array = Array.newInstance(cls, 2);
//                        Array.set(array, 0, value);
//                        Array.set(array, 1, value);
//
//                        obj = array;
//                    }
//
//                } else {
//
//                    if (hashExist.get(typeName) != null && hashExist.get(typeName)) {
//                        return null;
//                    }
//
//                    Class<?> cls = Class.forName(typeName);
//                    if (cls.getName().equals("java.lang.Class")) {
//                        return null;
//                    }
//
//                    if (cls.getName().equals("java.lang.Boolean")) {
//                        return "true";
//                    }
//
//                    try {
//                        obj = cls.newInstance();
//                    } catch (Exception e) {
//                        log.warn("无法实例化:" + cls.getName());
//                        return null;
//                    }
//
//                    Field[] fs = cls.getDeclaredFields();
//
//                    hashExist.put(typeName, true);
//
//                    for (Field field : fs) {
//
//                        if (field.getType().getName().equals(type.getTypeName())) {
//                            continue;
//                        }
//
//                        ReflectionUtils.makeAccessible(field);
//
//                        Object innerFieldValue = generateMock(field.getGenericType());
//
//                        try {
//                            ReflectionUtils.setField(field, obj, innerFieldValue);
//                        } catch (Exception e) {
//                            log.error(e.getMessage());
//                        }
//                    }
//
//                }
//            }
//
//            return obj;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//}
