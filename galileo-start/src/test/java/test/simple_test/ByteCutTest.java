package test.simple_test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author xiaobo
 * @date 2022/1/16 7:43 下午
 */
public class ByteCutTest {


    public static void main(String[] args) throws Exception {

        InputStream is = new FileInputStream("/Users/wangxiaobo/Downloads/test.txt");

        byte[] startSign = "<STX>".getBytes("utf-8");
        byte[] endSign = "<ETX>".getBytes("utf-8");

        ByteArrayOutputStreamNew os = new ByteArrayOutputStreamNew(startSign, endSign);

        byte[] bufs = new byte[100000];

        int len = 0;


        int i = 1;

        while (true) {

            len = is.read(bufs);

            if (len <= 0) {
                continue;
            }

            os.write(bufs, 0, len);

            while (true) {

                byte[] inputPackage = os.readPackage();

                if (inputPackage == null) {
                    break;
                }

                System.out.println(i++ + ": " + new String(inputPackage).trim());
            }
        }

    }


    static class ByteArrayOutputStreamNew extends ByteArrayOutputStream {


        private byte[] startSign;
        private byte[] endSign;

        public ByteArrayOutputStreamNew(byte[] startSign, byte[] endSign) {
            this.startSign = startSign;
            this.endSign = endSign;
        }

        private boolean readHead = true;

        private int position = 0;

        private int packageStart = 0;

        public byte[] readPackage() {

            byte[] sample = new byte[readHead ? startSign.length : endSign.length];

            for (; position <= this.count - sample.length; position++) {

                for (int k = 0; k < sample.length; k++) {
                    sample[k] = this.buf[position + k];
                }

                if (readHead) {
                    if (byteArrEquals(startSign, sample)) {
                        readHead = false;
                        position += startSign.length;
                        packageStart = position;
                    }
                } else {
                    if (byteArrEquals(endSign, sample)) {
                        readHead = true;

                        byte[] result = Arrays.copyOfRange(this.buf, packageStart, position);

                        this.buf = Arrays.copyOfRange(this.buf, position + endSign.length, this.count);

                        this.count = buf.length;

                        this.position = 0;

                        return result;
                    }
                }
            }
            return null;
        }


        private boolean byteArrEquals(byte[] b1, byte[] b2) {
            if (b1 == null && b2 == null) {
                return false;
            }

            if (b1.length != b2.length) {
                return false;
            }

            for (int i = 0; i < b1.length; i++) {
                if (b1[i] != b2[i]) {
                    return false;
                }
            }
            return true;
        }


    }


}
