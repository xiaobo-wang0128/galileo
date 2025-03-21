package org.armada.galileo.rainbow_gate.transfer.gate_codec;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.armada.galileo.rainbow_gate.transfer.util.RainbowUtil;

public class ByteCodec {

    public static class Reader {

        private byte[] bytes;

        private int index = 0;

        public Reader(byte[] bytes) {
            this.bytes = bytes;
        }

        public Integer readInteger() {
            int value = 0;
            int limit = index + 4;
            int lastIndex = index + 3;
            for (; index < limit; index++) {
                int shift = (lastIndex - index) * 8;
                value += (bytes[index] & 0xFF) << shift;
            }
            return value;
        }

        public String readString() throws Exception {
            int len = readInteger();
            if (len == 0) {
                return null;
            }
            if (len > bytes.length) {
                return null;
            }

            byte[] news = new byte[len];
            System.arraycopy(bytes, index, news, 0, len);

            index += len;

            return new String(news, "utf-8");
        }

        public byte[] readBytes() throws Exception {
            int len = readInteger();
            if (len == 0) {
                return null;
            }
            byte[] news = Arrays.copyOfRange(bytes, index, index += len);
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
            return bytes[index++];
        }

    }

    public static class Writer {

        private ByteArrayOutputStream bos = new ByteArrayOutputStream();

        private static byte[] ZERO = RainbowUtil.intToByteArray(0);

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
            bos.write(new byte[]{b});
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

        public byte[] toByte() {
            try {
                bos.flush();
                bos.close();
            } catch (Exception e) {
            }
            return bos.toByteArray();
        }

    }
}
