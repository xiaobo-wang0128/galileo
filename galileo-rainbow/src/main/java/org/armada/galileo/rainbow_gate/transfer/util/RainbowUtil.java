package org.armada.galileo.rainbow_gate.transfer.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;


public class RainbowUtil {

	private static final String FRONT = "front";

	/**
	 * 计算超时时间，默认 60秒内传完 10M 内容，<br/>
	 * 按 200k/s 的速度计算超时时间
	 * 
	 * @param byteSize
	 * @return
	 */
	public static int getMaxFtpWaitTime(int byteSize) {
		// 默认60秒限制
		int maxWaitTime = 600000; // 60000;
		// 超过5m， 每 10k 加1秒
		if (byteSize > 5000000) {
			maxWaitTime += ((byteSize - 5000000) / 10000) * 1000;
		}
		return maxWaitTime;
	}

	/**
	 * ftp sftp 上传时生成的临时文件名
	 * 
	 * @param folder
	 * @param ignoreKeyPosition
	 * @param ignoreKeyWord
	 * @return
	 */
	public static String generateTmpFileName(String folder, String ignoreKeyPosition, String ignoreKeyWord) {
		String tmpName = null;
		if (FRONT.equals(ignoreKeyPosition)) {
			if (folder != null) {
				tmpName = folder + "/" + ignoreKeyWord + "_" + UUID.randomUUID().toString();
			}
			else {
				tmpName = ignoreKeyWord + "_" + UUID.randomUUID().toString();
			}

		}
		else {
			if (folder != null) {
				tmpName = folder + "/" + UUID.randomUUID().toString() + "_" + ignoreKeyWord;
			}
			else {
				tmpName = UUID.randomUUID().toString() + "_" + ignoreKeyWord;
			}

		}
		return tmpName;
	}

	/**
	 * int到byte[] 由高位到低位
	 * 
	 * @param i 需要转换为byte数组的整行值。
	 * @return byte数组
	 */
	public static byte[] intToByteArray(int i) {
		byte[] result = new byte[4];
		result[0] = (byte) ((i >> 24) & 0xFF);
		result[1] = (byte) ((i >> 16) & 0xFF);
		result[2] = (byte) ((i >> 8) & 0xFF);
		result[3] = (byte) (i & 0xFF);
		return result;
	}

	/**
	 * byte[]转int
	 * 
	 * @param bytes 需要转换成int的数组
	 * @return int值
	 */
	public static int byteArrayToInt(byte[] bytes) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (3 - i) * 8;
			value += (bytes[i] & 0xFF) << shift;
		}
		return value;
	}

	public static void main(String[] args) throws Exception {

		String str = "this is a test 你好";

		byte[] word = str.getBytes("utf-8");

		byte[] wordLength = RainbowUtil.intToByteArray(word.length);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(wordLength);
		bos.write(word);
		bos.write("this is a anthoer 好".getBytes("utf-8"));

		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

		bos.close();

		int totalSize = bis.available();

		byte[] classNameLengthInt = new byte[4];
		bis.read(classNameLengthInt, 0, 4);
		int datalen = RainbowUtil.byteArrayToInt(classNameLengthInt);

		byte[] calssNameStringBytes = new byte[datalen];
		bis.read(calssNameStringBytes, 0, datalen);

		String readClassName = new String(calssNameStringBytes, "utf-8");
		System.out.println("readClassName: " + readClassName);

		byte[] dataBuf = new byte[totalSize - 4 - datalen];
		bis.read(dataBuf, 0, dataBuf.length);

		bis.close();

		System.out.println("left string: " + new String(dataBuf, "utf-8"));

	}

	public static boolean equals(List<String> list1, List<String> list2) {
		if (list1 == null || list2 == null) {
			return false;
		}

		// 长度都为0
		if (list1.size() == 0 && list2.size() == 0) {
			return true;
		}

		// 长度不相等 返回 false
		if (list1.size() != list2.size()) {
			return false;
		}

		for (String str1 : list1) {
			if (!list2.contains(str1)) {
				return false;
			}
		}

		return true;
	}
	
	
	

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
        } finally {
            kryoPool.release(kryo);
        }
    }


}
