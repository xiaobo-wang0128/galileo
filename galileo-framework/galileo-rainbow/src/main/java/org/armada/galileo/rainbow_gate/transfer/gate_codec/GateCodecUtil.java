package org.armada.galileo.rainbow_gate.transfer.gate_codec;

import java.util.Map;
import java.util.UUID;

import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.AppRequestDomain;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.AppResponseDomain;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.RainbowRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GateCodecUtil {

    private static Logger log = LoggerFactory.getLogger(GateCodecUtil.class);

//    public static void main(String[] args) {
//        AppRequestDomain d = new AppRequestDomain();
//        d.setClassName("com.xx.ab.haigui.AA");
//        d.setMethodName("test");
//        d.setRequestNo(UUID.randomUUID().toString());
//        d.setParamTypeNames(new String[]{"abc,", ""});
//        d.setRequestType("check");
//
//        byte[] buf = encodeRequest(d);
//
//        AppRequestDomain d2 = decodeRequest(buf);
//        System.out.println(JsonUtil.toJson(d2));
//    }

    /**
     * 请求参数编码
     *
     * @param domain
     * @return
     */
    public static byte[] encodeRequest(AppRequestDomain domain) {

        try {

            ByteCodec.Writer writer = new ByteCodec.Writer();
            writer.writeInteger(domain.getRequestType());
            if (RainbowRequestType.Ping.equals(domain.getRequestType()) || RainbowRequestType.Ping_Double.equals(domain.getRequestType())) {
                return writer.toByte();
            }
            writer.writeString(domain.getRequestNo());
            writer.writeString(domain.getRouteKey());
            writer.writeString(domain.getGroupValue());
            writer.writeString(domain.getClassName());
            writer.writeString(domain.getMethodName());
            writer.writeStringArray(domain.getParamTypeNames());
            writer.writeObjectArray(domain.getParamInputs());
            writer.writeObject(domain.getContext());

            return writer.toByte();

        } catch (Exception e) {
            log.error("[GateCodecUtil] appRequestDomain encode error：" + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 请求参数解码（全量）
     *
     * @param buffer
     * @return
     */
    public static AppRequestDomain decodeRequest(byte[] buffer) {
        try {
            ByteCodec.Reader reader = new ByteCodec.Reader(buffer);

            Integer requestType = reader.readInteger();
            if (RainbowRequestType.Ping.equals(requestType) || RainbowRequestType.Ping_Double.equals(requestType)) {
                AppRequestDomain domain = new AppRequestDomain();
                domain.setRequestType(requestType);
                return domain;
            }

            String requestNo = reader.readString();
            String routeKey = reader.readString();
            String groupValue = reader.readString();
            String className = reader.readString();
            String methodName = reader.readString();
            String[] paramTypeNames = reader.readStringArray();
            Object[] paramInputs = reader.readObjectArray();
            Map<String, Object> context = (Map<String, Object>) reader.readObject();

            AppRequestDomain domain = new AppRequestDomain();
            domain.setRequestType(requestType);
            domain.setRequestNo(requestNo);
            domain.setRouteKey(routeKey);
            domain.setGroupValue(groupValue);
            domain.setClassName(className);
            domain.setMethodName(methodName);
            domain.setParamTypeNames(paramTypeNames);
            domain.setParamInputs(paramInputs);
            domain.setContext(context);

            return domain;

        } catch (Exception e) {
            log.error("[GateCodecUtil] appRequestDomain decode error：" + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 请求参数解码（头信息）
     *
     * @param bufs
     * @return
     */
    public static AppRequestDomain decodeRequestHead(byte[] bufs) {
        try {
            ByteCodec.Reader reader = new ByteCodec.Reader(bufs);

            Integer requestType = reader.readInteger();
            if (RainbowRequestType.Ping.equals(requestType) || RainbowRequestType.Ping_Double.equals(requestType)) {
                AppRequestDomain domain = new AppRequestDomain();
                domain.setRequestType(requestType);
                return domain;
            }


            String requestNo = reader.readString();
            String routeKey = reader.readString();
            String groupValue = reader.readString();
            String className = reader.readString();
            String methodName = reader.readString();

            String[] paramTypeNames = null;
            Object[] paramInputs = null;

            AppRequestDomain domain = new AppRequestDomain();
            domain.setRequestType(requestType);
            domain.setRequestNo(requestNo);
            domain.setRouteKey(routeKey);
            domain.setClassName(className);
            domain.setMethodName(methodName);
            domain.setParamTypeNames(paramTypeNames);
            domain.setParamInputs(paramInputs);

            return domain;

        } catch (Exception e) {
            log.error("[GateCodecUtil] appRequestDomain decode error：" + e.getMessage(), e);
        }
        return null;
    }
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    /**
     * 响应结果编码
     *
     * @param domain
     * @return
     */
    public static byte[] encodeResponse(AppResponseDomain domain) {
        try {

            ByteCodec.Writer writer = new ByteCodec.Writer();
            writer.writeInteger(domain.getCode());
            writer.writeInteger(domain.getCostTime());
            writer.writeString(domain.getMessage());
            writer.writeObject(domain.getResult());

            return writer.toByte();
        } catch (Exception e) {
            log.error("[GateCodecUtil] appResponseDomain encode error：" + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 响应结果解码（全量）
     *
     * @param buffer
     * @return
     */
    public static AppResponseDomain decodeResponse(byte[] buffer) {

        try {
            ByteCodec.Reader reader = new ByteCodec.Reader(buffer);

            Integer code = reader.readInteger();
            Integer costTime = reader.readInteger();
            String message = reader.readString();
            Object result = reader.readObject();

            AppResponseDomain domain = new AppResponseDomain();
            domain.setCode(code);
            domain.setCostTime(costTime);
            domain.setMessage(message);
            domain.setResult(result);

            return domain;

        } catch (Exception e) {
            try {
                log.error(new String(buffer, "utf-8"));
                log.error(new String(buffer, "gbk"));
            } catch (Exception e2) {
            }
            log.error("[GateCodecUtil] appResponseDomain decode error：" + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 响应结果解码（头部）
     *
     * @param buffer
     * @return
     */
    public static AppResponseDomain decodeResponseHead(byte[] buffer) {

        try {
            ByteCodec.Reader reader = new ByteCodec.Reader(buffer);

            Integer code = reader.readInteger();
            Integer costTime = reader.readInteger();
            String message = reader.readString();
            // Object result = reader.readObject();

            AppResponseDomain domain = new AppResponseDomain();
            domain.setCode(code);
            domain.setCostTime(costTime);
            domain.setMessage(message);
            // domain.setResult(result);

            return domain;

        } catch (Exception e) {
            log.error("[GateCodecUtil] appResponseDomain decode error：" + e.getMessage(), e);
        }
        return null;
    }

}
