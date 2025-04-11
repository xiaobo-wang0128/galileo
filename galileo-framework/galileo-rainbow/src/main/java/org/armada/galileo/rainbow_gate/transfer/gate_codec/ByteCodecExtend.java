package org.armada.galileo.rainbow_gate.transfer.gate_codec;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.rainbow_gate.transfer.util.RainbowException;
import org.armada.galileo.rainbow_gate.transfer.util.RainbowUtil;

public class ByteCodecExtend {

	public static void main(String[] args) throws Exception {

		FileOutputStream fos = new FileOutputStream("/Users/xiaobowang/Downloads/test.db");

		Writer writer = new Writer(fos);

		writer.writeInteger(1333);
		writer.writeString("人好");
		writer.writeBytes(new byte[] { 1, 3, 4, 3, 31 });

		fos.flush();
		fos.close();

		FileInputStream fis = new FileInputStream("/Users/xiaobowang/Downloads/test.db");

		Reader reader = new Reader(fis);

//		System.out.println(reader.readInteger());
//		System.out.println(reader.readString());
//		System.out.println(JsonUtil.toJson(reader.readBytes()));

	}

	public static class Reader {

		private InputStream is;

		public Reader(InputStream is) {
			this.is = is;
		}

		public Integer readInteger() {
			try {
				byte[] bufs = new byte[4];
				is.read(bufs, 0, 4);

				int value = 0;
				for (int i = 0; i < 4; i++) {
					int shift = (3 - i) * 8;
					value += (bufs[i] & 0xFF) << shift;
				}
				return value;
			} catch (Exception e) {
				throw new RainbowException(e);
			}
		}

		public String readString() throws Exception {
			int len = readInteger();
			if (len == 0) {
				return null;
			}
			byte[] news = new byte[len];

			is.read(news, 0, news.length);

			return new String(news, "utf-8");
		}

		public byte[] readBytes() throws Exception {
			int len = readInteger();
			if (len == 0) {
				return null;
			}
			byte[] news = new byte[len];
			is.read(news, 0, news.length);
			return news;
		}

		public Object readObject() throws Exception {
			String clsName = readString();
			if (clsName == null) {
				return null;
			}
			byte[] bufs = readBytes();

			Object r = RainbowUtil.deserialize(bufs, Class.forName(clsName));
			return r;
		}

		public Object[] readObjectArray() throws Exception {
			int len = readInteger();
			if (len == 0) {
				return null;
			}

			Object[] result = new Object[len];
			for (int i = 0; i < len; i++) {
				result[i] = readObject();
			}

			return result;
		}

		public String[] readStringArray() throws Exception {
			int len = readInteger();
			if (len == 0) {
				return null;
			}
			String[] result = new String[len];
			for (int i = 0; i < len; i++) {
				result[i] = readString();
			}
			return result;
		}

		public byte readByte() {
			try {
				byte[] bufs = new byte[1];
				is.read(bufs, 0, 1);
				return bufs[0];
			} catch (Exception e) {
				throw new RainbowException(e);
			}
		}

	}

	public static class Writer {

		private OutputStream bos;

		private static byte[] ZERO = RainbowUtil.intToByteArray(0);

		public Writer(OutputStream bos) {
			this.bos = bos;
		}

		public void writeInteger(Integer value) throws Exception {
			byte[] buf = RainbowUtil.intToByteArray(value);
			bos.write(buf);
		}

		public void writeString(String value) throws Exception {
			if (value == null || value.matches("\\s*")) {
				bos.write(ZERO);
				return;
			}
			byte[] bufs = value.getBytes("utf-8");
			this.writeInteger(bufs.length);
			bos.write(bufs);
		}

		public void writeBytes(byte[] bufs) throws Exception {
			if (bufs == null || bufs.length == 0) {
				bos.write(ZERO);
				return;
			}
			this.writeInteger(bufs.length);
			bos.write(bufs);
		}

		public void writeByte(Byte b) throws Exception {
			bos.write(new byte[] { b });
		}

		public void writeObject(Object obj) throws Exception {
			if (obj == null) {
				bos.write(ZERO);
				return;
			}
			String type = obj.getClass().getName();
			this.writeString(type);
			byte[] buf = RainbowUtil.serialize(obj);
			this.writeBytes(buf);
		}

		public void writeObjectArray(Object[] objs) throws Exception {
			if (objs == null || objs.length == 0) {
				bos.write(ZERO);
				return;
			}
			this.writeInteger(objs.length);
			for (Object object : objs) {
				this.writeObject(object);
			}
		}

		public void writeStringArray(String[] strings) throws Exception {
			if (strings == null || strings.length == 0) {
				bos.write(ZERO);
				return;
			}
			this.writeInteger(strings.length);
			for (String str : strings) {
				this.writeString(str);
			}
		}

	}
}
