package org.armada.spi.open.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

/**
 * 工具类
 *
 * @author xiaobo
 * @date 2021/4/23 10:07 上午
 */
public class HaiqSdkUtil {

    private static KryoPool kryoPool = new KryoPool.Builder(new KryoFactory() {
        public Kryo create() {
            final Kryo kryo = new Kryo();
            kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new org.objenesis.strategy.StdInstantiatorStrategy()));
            kryo.setRegistrationRequired(false);
            kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
            return kryo;
        }
    }).softReferences().build();

    public static byte[] serialize(Object obj) {

        Kryo kryo = null;
        try {
            kryo = kryoPool.borrow();
            Output output = new Output(10000, -1);

            kryo.writeObject(output, obj);
            output.close();

            return output.toBytes();
        } finally {
            kryoPool.release(kryo);
        }
    }

    public static <T> T deserialize(byte[] by, Class<T> type) {
        if (by == null) {
            return null;
        }
        Kryo kryo = null;
        try {

            kryo = kryoPool.borrow();

            Input input = new Input(by);

            T outObject = kryo.readObject(input, type);
            input.close();

            return outObject;
        }
        catch (Exception e){
            throw new RuntimeException("反序列化异常，请检查生产者与消费者class结构、字段类型是否一致，（如内部有 List、Map类型，请使用常规实现类，如 ArrayList HashMap）", e);
        }
        finally {
            kryoPool.release(kryo);
        }
    }

    /**
     * 字节数组合并 shar256
     *
     * @param bufs
     * @return
     */
    public static String getByateArraysSHA256(byte[]... bufs) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            for (byte[] buf : bufs) {
                bos.write(buf);
            }
            byte[] result = bos.toByteArray();
            String shar = getSHA256(result);

            bos.close();
            result = null;

            return shar;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String getSHA256(byte[] bufs) {
        MessageDigest messageDigest;
        String encodestr = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bufs);
            encodestr = byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return encodestr;
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
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * @param inputByte 待解压缩的字节数组
     * @return 解压缩后的字节数组
     * @throws IOException
     */
    public static byte[] uncompress(byte[] inputByte) throws IOException {
        int len = 0;
        Inflater infl = new Inflater(true);

        infl.setInput(inputByte);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] outByte = new byte[1024];
        try {
            while (!infl.finished()) {
                // 解压缩并将解压缩后的内容输出到字节输出流bos中
                len = infl.inflate(outByte);
                if (len == 0) {
                    break;
                }
                bos.write(outByte, 0, len);
            }
            infl.end();
        } catch (Exception e) {
            //
        } finally {
            bos.close();
        }
        return bos.toByteArray();
    }

    /**
     * 压缩.
     *
     * @param inputByte 待压缩的字节数组
     * @return 压缩后的数据
     * @throws IOException
     */
    public static byte[] compress(byte[] inputByte) throws IOException {
        int len = 0;
        Deflater defl = new Deflater(4, true);
        defl.setInput(inputByte);
        defl.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] outputByte = new byte[1024];
        try {
            while (!defl.finished()) {
                // 压缩并将压缩后的内容输出到字节输出流bos中
                len = defl.deflate(outputByte);
                bos.write(outputByte, 0, len);
            }
            defl.end();
        } finally {
            bos.close();
        }
        return bos.toByteArray();
    }


    public static <T> List<T> asList(T... a) {
        ArrayList<T> list = new ArrayList<T>();
        for (T t : a) {
            list.add(t);
        }
        return list;
    }

}
