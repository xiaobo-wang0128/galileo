package org.armada.galileo.mvc_plus.encrypt;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class BackAES {

	/**
	 * 加密解决算法
	 */
	// private static String sKey = "1234567890123456";
	private static String ivParameter = "1234567890123456";// 默认偏移

	private static String WAYS = "AES";
	private static String MODE = "";
	private static boolean isPwd = false;
	private static String ModeCode = "PKCS5Padding";
	private static int type = 0;// 默认

	private static int pwdLenght = 16;
	private static String val = "0";

	public static String selectMod(int type) {
		// ECB("ECB", "0"), CBC("CBC", "1"), CFB("CFB", "2"), OFB("OFB", "3");
        switch (type) {
            case 0:
                isPwd = false;
                MODE = WAYS + "/" + AESType.ECB.key() + "/" + ModeCode;

                break;
            case 1:
                isPwd = true;
                MODE = WAYS + "/" + AESType.CBC.key() + "/" + ModeCode;
                break;
            case 2:
                isPwd = true;
                MODE = WAYS + "/" + AESType.CFB.key() + "/" + ModeCode;
                break;
            case 3:
                isPwd = true;
                MODE = WAYS + "/" + AESType.OFB.key() + "/" + ModeCode;
                break;
            default:
                break;

		}
		return MODE;

	}

	/******************************** 方法一，密匙必须为16位 **********************************************/
	// 加密
	public static byte[] encrypt(byte[] sourceBytes, String sKey, int type) throws Exception {
		sKey = toMakekey(sKey, pwdLenght, val);
		Cipher cipher = Cipher.getInstance(selectMod(type));
		byte[] raw = sKey.getBytes("ASCII");
		SecretKeySpec skeySpec = new SecretKeySpec(raw, WAYS);

		if (isPwd == false) {// ECB 不用密码
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		}
		else {
			IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		}

		byte[] encrypted = cipher.doFinal(sourceBytes);
		return (encrypted);// 此处使用BASE64做转码。
	}

	// 解密
	public static byte[] decrypt(byte[] encrypted, String sKey, int type) throws Exception {
		sKey = toMakekey(sKey, pwdLenght, val);
		try {
			byte[] raw = sKey.getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, WAYS);
			Cipher cipher = Cipher.getInstance(selectMod(type));

			if (isPwd == false) {// ECB 不用密码
				cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			}
			else {
				IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
				cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			}
			byte[] original = cipher.doFinal(encrypted);

			return original;
		} catch (Exception ex) {
			return null;
		}
	}

	// key
	public static String toMakekey(String str, int strLength, String val) {

		int strLen = str.length();
		if (strLen < strLength) {
			while (strLen < strLength) {
				StringBuffer buffer = new StringBuffer();
				buffer.append(str).append(val);
				str = buffer.toString();
				strLen = str.length();
			}
		}
		return str;
	}

	/*********************************** 第二种 ***********************************************/

	@Deprecated
	public static byte[] newEncrypt(String content, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128, new SecureRandom(password.getBytes()));
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建AES加密编码器
			byte[] byteContent = content.getBytes("UTF-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化AES加密
			byte[] result = cipher.doFinal(byteContent);
			return result; // AES加密结果
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * @param content 待解密内容,格式为byte数组
	 * 
	 * @param password AES解密使用的密钥
	 * 
	 * @return
	 */
	@Deprecated
	public static byte[] newDecrypt(byte[] content, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128, new SecureRandom(password.getBytes()));
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建AES加密编码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化AES加密
			byte[] result = cipher.doFinal(content);
			return result; // 得到AES解密结果
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将二进制转换成16进制字符串
	 * 
	 * @param buf
	 * @return
	 */
	public static String byte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * java将16进制字符串转换为二进制数组
	 * 
	 * @param hexStr
	 * @return
	 */
	public static byte[] hexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	public static String encodeByte2String(byte[] src, String sKey) {
		try {

			sKey = toMakekey(sKey, pwdLenght, val);
			Cipher cipher = Cipher.getInstance(selectMod(type));
			byte[] raw = sKey.getBytes();
			SecretKeySpec skeySpec = new SecretKeySpec(raw, WAYS);

			IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
			if (isPwd == false) {// ECB 不用密码
				cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			}
			else {
				cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			}

			byte[] encrypted = cipher.doFinal(src);

			return byte2HexStr(encrypted);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decodeString2Byte(String sSrc, String sKey) {
		sKey = toMakekey(sKey, pwdLenght, val);
		try {
			byte[] raw = sKey.getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, WAYS);
			Cipher cipher = Cipher.getInstance(selectMod(type));
			IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
			if (isPwd == false) {// ECB 不用密码
				cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			}
			else {
				cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			}
			byte[] original = cipher.doFinal(hexStr2Byte(sSrc));
			return original;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		String key = "aaa";
		System.out.println(key);
		for (int i = 0; i < 100; i++) {
			String a = "dddd" + i;
			String cookie = encodeByte2String(a.getBytes(), key);

			System.out.println("cookie: " + cookie);
			String b = new String(decodeString2Byte(cookie, key));
			System.out.println(a.equals(b));

			// String ddd = "基大大在 工菜工";
			// String d2 = parseByte2HexStr(ddd.getBytes());
			// String d3 = new String(parseHexStr2Byte(d2));
			// System.out.println(ddd.equals(d3));
		}
	}

}
