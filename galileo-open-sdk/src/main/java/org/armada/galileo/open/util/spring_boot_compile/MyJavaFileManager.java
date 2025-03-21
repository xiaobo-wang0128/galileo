package org.armada.galileo.open.util.spring_boot_compile;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2021/12/9 1:53 下午
 */
public class MyJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {


    private static Map<String, JavaFileObject> fileObjectMap = new HashMap<>();


    public MyJavaFileManager(JavaFileManager javaFileManager) {
        super(javaFileManager);
    }

    /**
     * 这个方法的作用是从指定位置读取Java程序并生成JavaFIleObject对象供编译器使用，本项目中用处不大
     */
    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        JavaFileObject javaFileObject = fileObjectMap.get(className);
        if (javaFileObject == null) {
            return super.getJavaFileForInput(location, className, kind);
        }
        return javaFileObject;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        JavaFileObject javaFileObject = new MyJavaFileObject(className, kind);
        fileObjectMap.put(className, javaFileObject);
        return javaFileObject;
    }

}