package org.armada.galileo.mvc_plus.velocity;

import java.util.HashMap;
import java.util.Map;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.mvc_plus.support.ControllInterface;
import org.springframework.beans.factory.annotation.Autowired;

public class ControllUtil {

	private static Map<String, ControllInterface> maps = new HashMap<String, ControllInterface>();

	private static String rootPath = null;

	@Autowired(required = false)
	private VelocityToolInterface velocityTool;

	private static CommonUtil util = new CommonUtil();

	public static void putPaths(String path, ControllInterface controllObject) {
		maps.put(path, controllObject);
	}

	public String include(String vmPath, Object... args) {
		Map<String, Object> context = new HashMap<String, Object>();
		// 通用参数
		if (velocityTool != null) {
			velocityTool.fillMap(context);
		}

		if (maps.get(vmPath) != null) {
			ControllInterface obj = maps.get(vmPath);
			obj.execute(context);
		}

		if (args != null && args.length % 2 == 0) {
			for (int i = 0; i < args.length; i += 2) {
				context.put(String.valueOf(args[i]), args[i + 1]);
			}
		}
		context.put("util", util);

		String s = MvcVelocityHelper.render(rootPath + vmPath, context);
		return s;
	}

	public void setRootPath(String rootPath) {
		rootPath = rootPath + "/controll";
		ControllUtil.rootPath = rootPath;
	}

}
