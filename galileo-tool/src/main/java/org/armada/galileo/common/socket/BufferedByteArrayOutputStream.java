package org.armada.galileo.common.socket;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * 用于从流中截取指定标记的包
 *
 * @author xiaobo
 * @date 2022/1/21 10:37 上午
 */
public class BufferedByteArrayOutputStream extends ByteArrayOutputStream {


    private byte[] startSign;
    private byte[] endSign;

    /**
     * 初始化实例
     *
     * @param startSign 包的开始标记
     * @param endSign   包的结束标记
     */
    public BufferedByteArrayOutputStream(byte[] startSign, byte[] endSign) {
        this.startSign = startSign;
        this.endSign = endSign;
    }

    private boolean readHead = true;

    private int position = 0;

    private int packageStart = 0;

    /**
     * 从字节流中读取数据包，数据包未准备就续时，可能返回空
     *
     * @return
     */
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