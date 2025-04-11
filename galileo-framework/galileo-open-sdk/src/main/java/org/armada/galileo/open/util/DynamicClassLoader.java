package org.armada.galileo.open.util;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.exception.BizException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * @author xiaobo
 * @date 2021/12/8 9:52 下午
 */


@Slf4j
public class DynamicClassLoader extends ClassLoader {

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * 动态编译
     *
     * @param className
     * @param classPath class 文件路径
     * @return
     */
    public Class<?> defineClass(String className, String classPath) {
        log.info("动态编译, className:{}, path:{}", className, classPath);
        byte[] clazz = loadClazz(classPath);
        return defineClass(className, clazz, 0, clazz.length);
    }

    /**
     * 动态编译
     *
     * @param className
     * @param clazz     字节码
     * @return
     */
    public Class<?> defineClass(String className, byte[] clazz) {
        log.info("[galileo-sdk] 动态加载 class 至上下文, className:{} ", className);
        return defineClass(className, clazz, 0, clazz.length);
    }

    public byte[] loadClazz(String classPath) {
        try {
            FileInputStream in = new FileInputStream(new File(classPath));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) != -1) {
                baos.write(b);
            }
            in.close();
            return baos.toByteArray();
        } catch (Throwable e) {
            throw new BizException(e);
        }
    }

}