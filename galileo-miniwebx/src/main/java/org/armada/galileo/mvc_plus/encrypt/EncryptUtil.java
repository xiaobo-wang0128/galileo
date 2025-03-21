package org.armada.galileo.mvc_plus.encrypt;

import java.io.File;
import java.io.RandomAccessFile;
import java.security.MessageDigest;

import org.armada.galileo.common.util.CommonUtil;
import org.slf4j.LoggerFactory;

/**
 * 加密工具类
 *
 * @author wang xiaobo
 */
public class EncryptUtil {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(EncryptUtil.class);


    public static String aesEncode(String content, String key) {
        try {
            return byte2Hex(BackAES.encrypt(content.getBytes("utf-8"), key, 0));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("aes 加密异常");
        }
    }

    public static String aesDecode(String content, String key) {
        try {
            return new String(BackAES.decrypt(hex2Byte(content), key, 0), "utf-8");
        } catch (Exception e) {
            log.warn("aesDecode error: " + content);
            throw new RuntimeException("参数解密异常");
        }
    }




    public static String aesEncodeBase64(String content, String key) {
        try {
            return CommonUtil.base64Encode(BackAES.encrypt(content.getBytes("utf-8"), key, 0));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("aes 加密异常");
        }
    }

    public static String aesDecodeBase64(String content, String key) {
        try {
            //CommonUtil.base64Decode() ;;
            return new String(BackAES.decrypt(CommonUtil.base64Decode(content), key, 0), "utf-8");
        } catch (Exception e) {
            log.warn("aesDecode error: " + content);
            throw new RuntimeException("参数解密异常");
        }
    }


    public static void main(String[] args) {

        String key = "13d3030dd33a30";


        String s = "SBDEAFDFEE";

        String a = aesEncodeBase64(s, key);

        String b = aesDecodeBase64(a, key);

        System.out.println(a);
        System.out.println(b);

    }


    /**
     * 将二进制转换成16进制字符串
     *
     * @param buf
     * @return
     */
    public static String byte2Hex(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * java将16进制字符串转换为二进制数组
     *
     * @param hexStr
     * @return
     */
    public static byte[] hex2Byte(String hexStr) {
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

    public static String getHash(File file) throws Exception {
        return getHashByAlgiruthm(file, "SHA-256", file.length());
    }

    public static String getMd5(File file) throws Exception {
        return getHashByAlgiruthm(file, "MD5", file.length());
    }

    private static String getHashByAlgiruthm(File file, String algorithm, long offset) throws Exception {
        int buff = 16384;
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        MessageDigest hashSum = MessageDigest.getInstance(algorithm);
        byte[] buffer = new byte[16384];
        byte[] partialHash = null;
        long read = 0;
        int unitsize;

        while (read < offset) {
            unitsize = (int) (((offset - read) >= buff) ? buff : (offset - read));
            randomAccessFile.read(buffer, 0, unitsize);
            hashSum.update(buffer, 0, unitsize);
            read += unitsize;
        }
        randomAccessFile.close();
        partialHash = hashSum.digest();
        return byte2Hex(partialHash);
    }

    public static String getFileMd5(File file, Long offset) throws Exception {
        return getHashByAlgiruthm(file, "MD5", offset);
    }


}
