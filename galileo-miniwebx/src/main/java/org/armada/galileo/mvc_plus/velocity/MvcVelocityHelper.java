package org.armada.galileo.mvc_plus.velocity;

import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.armada.galileo.common.loader.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MvcVelocityHelper {

	private static Logger log = LoggerFactory.getLogger(MvcVelocityHelper.class);

	/** 单态实例 */
	private static final MvcVelocityHelper instance = new MvcVelocityHelper();

	/** 私有构造函数 */
	private MvcVelocityHelper() {

		// 初始化
		try {
			Velocity.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <pre>
	 * 
	 * 取得实例
	 * </pre>
	 */
	public static MvcVelocityHelper getInstance() {
		return instance;
	}

	/**
	 * <pre>
	 *  
	 * 渲染：从reader到writer
	 * </pre>
	 * 
	 * @param context
	 * @param writer
	 * @param reader
	 * @return
	 */
	private boolean evaluate(VelocityContext context, Writer writer, Reader reader) {
		try {
			return Velocity.evaluate(context, writer, "", reader);
		} catch (Exception e) {
			throw new RuntimeException("velocity evaluate error! detail [" + e.getMessage() + "]");
		}
	}

	/**
	 * <pre>
	 * 
	 * 通过Map过滤一个输入流
	 * </pre>
	 * 
	 * @param map
	 * @param reader
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Writer evaluateToWriter(Map<String, Object> map, Reader reader) {
		try {
			VelocityContext context = convertVelocityContext(map);
			CharArrayWriter writer = new CharArrayWriter();
			this.evaluate(context, writer, reader);

			return writer;
		} catch (Exception e) {
			throw new RuntimeException("velocity evaluate error! detail [" + e.getMessage() + "]");
		}
	}

	/**
	 * <pre>
	 * 
	 * 把Map转换成Context
	 * </pre>
	 */
	public static VelocityContext convertVelocityContext(Map<String, Object> map) {
		VelocityContext context = new VelocityContext();
		if (map == null) {
			return context;
		}
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			context.put(entry.getKey(), entry.getValue());
		}
		return context;
	}

	/**
	 * <pre>
	 * 
	 * 把Map转换成Context
	 * </pre>
	 */
	public static VelocityContext convertVelocityContext(HttpServletRequest request) {
		VelocityContext context = new VelocityContext();
		if (request == null) {
			return context;
		}
		Enumeration<String> en = request.getAttributeNames();
		while (en.hasMoreElements()) {
			String key = en.nextElement();
			context.put(key, request.getAttribute(key));
		}
		return context;
	}

	public static String render(String filePath, Map<String, Object> params) {
		try {
			InputStream is = ConfigLoader.loadResource(filePath, false, false);

			Reader reader = new InputStreamReader(is, "utf-8");
			Writer writer = MvcVelocityHelper.getInstance().evaluateToWriter(params, reader);
			return writer.toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	public static String render(InputStream inputStream, Map<String, Object> params) {
		try {
			Reader reader = new InputStreamReader(inputStream, "utf-8");
			Writer writer = MvcVelocityHelper.getInstance().evaluateToWriter(params, reader);
			return writer.toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	public static String renderByTpl(String tplContent, Map<String, Object> params) {
		try {
			StringReader reader = new StringReader(tplContent);
			Writer writer = MvcVelocityHelper.getInstance().evaluateToWriter(params, reader);
			return writer.toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

}