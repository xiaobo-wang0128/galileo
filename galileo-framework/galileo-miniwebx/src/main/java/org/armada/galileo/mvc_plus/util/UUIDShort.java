package org.armada.galileo.mvc_plus.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * 长度为16的 UUID 字符串生成器， 仿 java.util.UUID 实现，修改内容如下： 将16位byte数组缩短为10位，将16进制字符转换改为32进制转换
 * 
 * @author Wang Xiaobo
 * @date 2012-5-6
 */
public class UUIDShort {

	private static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	private static String toUnsignedString(long i, int shift) {
		char[] buf = new char[64];
		int charPos = 64;
		int radix = 1 << shift;
		long mask = radix - 1;
		do {
			buf[--charPos] = digits[(int) (i & mask)];
			i >>>= shift;
		} while (i != 0);
		return new String(buf, charPos, (64 - charPos));
	}

	private static SecureRandom ng = new SecureRandom();

	/**
	 * 生成一个长度为16的随机字符串
	 * 
	 * @return
	 */
	public static String random16() {
		byte[] data = new byte[10];
		ng.nextBytes(data);
		long msb = 0;
		long lsb = 0;
		for (int i = 0; i < 5; i++) {
			msb = (msb << 8) | (data[i] & 0xff);
		}
		for (int i = 5; i < 10; i++) {
			lsb = (lsb << 8) | (data[i] & 0xff);
		}
		String random = toUnsignedString(msb, 5) + toUnsignedString(lsb, 5);
		if (random.length() < 16) {
			for (int i = 0; i < 16 - random.length(); i++) {
				random = digits[ng.nextInt(36)] + random;
			}
		}
		return random;
	}

	public static String random32() {
		return UUID.randomUUID().toString();
	}

	
	public static void main(String[] args) {
		for (int i = 0; i < 1000; i++) {
			System.out.println(random32());
		}
	}
	
}
