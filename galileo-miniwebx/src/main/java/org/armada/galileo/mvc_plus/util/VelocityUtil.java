package org.armada.galileo.mvc_plus.util;

import java.io.CharArrayWriter;
import java.util.Map;

import org.apache.velocity.app.Velocity;
import org.armada.galileo.mvc_plus.velocity.MvcVelocityHelper;


public class VelocityUtil {

	public static String render(String htmlTemplate, Map<String, Object> params) {
		CharArrayWriter writer = new CharArrayWriter();

		Velocity.evaluate(MvcVelocityHelper.convertVelocityContext(params), writer, "", htmlTemplate);

		String result = writer.toString();

		return result;
	}
}
