package org.armada.spi.open.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 远程动态代理执行器
 *
 * @author xiaobo
 * @date 2021/4/23 10:07 上午
 */
public class HaiqOpenInvocationHandler implements InvocationHandler {

	private Logger log = LoggerFactory.getLogger(HaiqOpenInvocationHandler.class);

	/**
	 * 每个租户的唯一标识
	 */
	private String appId;

	/**
	 * http通信密钥
	 */
	private String appSecret;

	/**
	 * http通信地址
	 */
	private String httpUrl;

	private Class<?> cls;

	private HaiqOpenInvocationHandler(Class<?> cls, String appId, String appSecret, String httpUrl) {
		this.cls = cls;
		this.appId = appId;
		this.appSecret = appSecret;
		this.httpUrl = httpUrl;
	}


	public static <T> T getInstance(Class<T> type, String appId, String appSecret, String httpUrl) {
		HaiqOpenInvocationHandler handler = new HaiqOpenInvocationHandler(type, appId, appSecret, httpUrl);

		return (T) Proxy.newProxyInstance(HaiqOpenInvocationHandler.class.getClassLoader(), new Class<?>[] { type }, handler);
	}

	/**
	 * 远程调用类
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return invokeByHttp(proxy, method, args);
	}

	public Object invokeByHttp(Object proxy, Method method, Object[] args) throws Throwable {

		long l1 = System.currentTimeMillis();
		/** ---- step 1: 构造反映调用所需参数 ------ */

		String className = cls.getName();
		String methodName = method.getName();
		// 入参类型
		Class<?>[] paramTypes = method.getParameterTypes();
		String[] paramTypeNames = null;
		if (paramTypes != null && paramTypes.length > 0) {
			paramTypeNames = new String[paramTypes.length];
			for (int i = 0; i < paramTypes.length; i++) {
				paramTypeNames[i] = paramTypes[i].getName();
			}
		}
		// 业务请求封装类
		HaiqRequestDomain domain = new HaiqRequestDomain();

		// 业务参数
		domain.setClassName(className);
		domain.setParamTypeNames(paramTypeNames);
		domain.setMethodName(methodName);
		domain.setParamInputs(args);

		/** ---- step 2: 序列化 压缩 签名------ */

		// 时间戳
		String dc = String.valueOf(System.currentTimeMillis());

		// 序列化
		byte[] inputBytes = HaiqSdkUtil.serialize(domain);

		// 压缩
		inputBytes = HaiqSdkUtil.compress(inputBytes);

		// 计算签名
		String sign = HaiqSdkUtil.getByateArraysSHA256(appSecret.getBytes("utf-8"), inputBytes, dc.getBytes());

		/** ---- step 3: 网络请求 返回结果 反序列化------ */
		HaiqSdkPostParam postPram = new HaiqSdkPostParam(appId, sign, dc, inputBytes);

		int requestFileSize = inputBytes.length;

		byte[] result = HaiqHttpPostUtil.request(httpUrl, postPram);

		long l2 = System.currentTimeMillis();
		log.info("[haigui-api] 接口调用耗时: {} ms, size: {}, method: {}", (l2 - l1), caculate(requestFileSize), methodName);

		HaiqActionResult har = null;
		try {
			har = HaiqSdkUtil.deserialize(result, HaiqActionResult.class);
		} catch (Exception e) {
			log.error("[haigui-api] 远程调用出错, method: {}, message: {}", methodName, e.getMessage());
			throw new HaiqApiException("远程调用出错：" + e.getMessage());
		}

		if (har == null) {
			log.error("[haigui-api] 远程调用出错, method: {}", methodName);
			throw new HaiqApiException("远程调用出错");
		}
		if (har.getCode() != 0) {
			log.error("[haigui-api] 远程调用出错, method: {}, message: {}", methodName, har.getMessage());
			throw new HaiqApiException("远程调用出错：" + har.getMessage());
		}
		return har.getData();
	}

	private static final BigDecimal GB = BigDecimal.valueOf(1024 * 1024 * 1024);

	private static final BigDecimal MB = BigDecimal.valueOf(1024 * 1024);

	private static final BigDecimal KB = BigDecimal.valueOf(1024);

	private static String caculate(Integer bytes) {
		if (bytes == null) {
			return null;
		}

		BigDecimal byteBig = BigDecimal.valueOf(bytes);

		if (byteBig.compareTo(GB) >= 0) {
			BigDecimal result = BigDecimal.valueOf(bytes).divide(GB, 2, RoundingMode.HALF_DOWN);
			return result.toString() + " gb";
		}

		if (byteBig.compareTo(MB) >= 0) {
			BigDecimal result = BigDecimal.valueOf(bytes).divide(MB, 2, RoundingMode.HALF_DOWN);
			return result.toString() + " mb";
		}

		if (byteBig.compareTo(KB) >= 0) {
			BigDecimal result = BigDecimal.valueOf(bytes).divide(KB, 2, RoundingMode.HALF_DOWN);
			return result.toString() + " kb";
		}

		return bytes + " byte";
	}
}
