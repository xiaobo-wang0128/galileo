package org.armada.galileo.open.util.spring_boot_compile;

/**
 * @author xiaobo
 * @date 2021/12/9 1:53 下午
 */

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * 用于封装表示源码与字节码的对象
 */
public class MyJavaFileObject extends SimpleJavaFileObject {
    private String source;
    private ByteArrayOutputStream byteArrayOutputStream;

    /**
     * 构造用于存放源代码的对象
     */
    public MyJavaFileObject(String name, String source) {
        super(URI.create("String:///" + name + Kind.SOURCE.extension), Kind.SOURCE);
        this.source = source;
    }

    /**
     * 构建用于存放字节码的JavaFileObject
     */
    public MyJavaFileObject(String name, Kind kind) {
        super(URI.create("String:///" + name + Kind.SOURCE.extension), kind);
    }

    /**
     * 获取源代码字符序列
     */
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        if (source == null)
            throw new IllegalArgumentException("source == null");
        return source;
    }

    /**
     * 得到JavaFileObject中用于存放字节码的输出流
     */
    @Override
    public OutputStream openOutputStream() throws IOException {
        byteArrayOutputStream = new ByteArrayOutputStream();
        return byteArrayOutputStream;
    }

    /**
     * 将输出流的内容转化为byte数组
     */
    public byte[] getCompiledBytes() {
        return byteArrayOutputStream.toByteArray();
    }
}