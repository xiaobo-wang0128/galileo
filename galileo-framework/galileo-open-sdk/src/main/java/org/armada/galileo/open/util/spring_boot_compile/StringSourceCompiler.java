package org.armada.galileo.open.util.spring_boot_compile;

/**
 * @author xiaobo
 * @date 2021/12/9 1:55 下午
 */

import javax.tools.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义编译模块
 */
public class StringSourceCompiler {
    private static Map<String,JavaFileObject> fileObjectMap = new ConcurrentHashMap<>();

    //预编译正则表达式,用来匹配源码字符串中的类名
    private static Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s*");

    public static byte[] compile(String source, DiagnosticCollector<JavaFileObject> compileCollector){
        //获取Java语言编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        /**
         * 通过以下两步获得自己的JavaFIleManager
         */
        //获取Java语言编译器的标准文件管理器实现的新实例
        JavaFileManager j = compiler.getStandardFileManager(compileCollector,null,null);
        //将文件管理器传入FordingJavaFileManager的构造器，获得一个我们自定义的JavaFileManager
        JavaFileManager javaFileManager = new MyJavaFileManager(j);

        //从源码字符串中匹配类名
        Matcher matcher = CLASS_PATTERN.matcher(source);
        String className;
        if(matcher.find()) {
            className = matcher.group(1);
        }else {
            throw new IllegalArgumentException("No valid class");
        }

        //将源码字符串封装为一个sourceJavaFileObject，供自定义的编译器使用
        JavaFileObject sourceJavaFileObject = new MyJavaFileObject(className,source);
        /**
         * 1、编译器得到源码，进行编译，得到字节码，源码封装在sourceJavaFIleObject中
         * 2、通过调用JavaFileManager的getJavaFileForOutput()方法创建一个MyJavaFileObject对象，用于存放编译生成的字节码
         *       |----->然后将存放了字节码的JavaFileObject放在Map<className,JavaFileObject>中，以便后面取用。
         * 3、通过类名从map中获取到存放字节码的MyJavaFileObject
         * 4、通过MyJavaFileObject对象获取到存放编译结果的输出流
         * 5、调用getCompiledBytes()方法将输出流内容转换为字节数组
         */
        //开始执行编译，通过传入自己的JavaFileManager为编译器创建存放字节码的JavaFIleObject对象
        Boolean result = compiler.getTask(null,javaFileManager,compileCollector, null,null, Arrays.asList(sourceJavaFileObject)).call();
        //3、
        JavaFileObject byteJavaFileObject = fileObjectMap.get(className);
        if(result && byteJavaFileObject != null){
            //4、5、
//            System.out.print("获取到字节码数组");
//            String bytes = new String(((MyJavaFileObject)byteJavaFileObject).getCompiledBytes());
//            System.out.print(bytes);
            return ((MyJavaFileObject)byteJavaFileObject).getCompiledBytes();
        }
        return null;
    }


}