package org.armada.spi.open.util;

/**
 * @author xiaobo
 * @date 2021/4/26 8:21 下午
 */
public class HqiqAssert {

    public static void assertIsNull(String input, String name) {
		if (input == null) {
			throw new RuntimeException(name + "is null");
		}
	}
}
