package org.armada.galileo.open.util;

import com.itranswarp.compiler.JavaStringCompiler;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.exception.BizException;

import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
public class DynamicClassCompileUtil {

//    public static Class<?> compileJavaFile(String javaContent) throws Exception {
//
//        String tmp = new String(javaContent);
//
//        tmp = tmp.trim();
//
//        // 生成一个随机的包名
//        String randomPackage = "org.armada.random" + CommonUtil.getRandomNumber(16);
//
//        // 获取 class name
//        String className = CommonUtil.getMatchedByGroup(tmp, "\\s+public\\s+class\\s+(\\w+)\\s?.*?\\{");
//
//        // 重定位包路径
//        if (tmp.startsWith("package")) {
//            tmp = tmp.replaceAll("package\\s+.*;", "package " + randomPackage + ";");
//        } else {
//            tmp = "package " + randomPackage + ";" + "\n\n" + tmp;
//        }
//        // 加载 class
//        String newClassName = randomPackage + "." + className;
//
//        String class_name = newClassName;
//
//        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//
//        MyLaunchedURLClassLoader loader = new MyLaunchedURLClassLoader(urlClassLoader.getURLs(), ClassLoader.getSystemClassLoader());
//
//        //CusCompiler compiler = new CusCompiler((URLClassLoader)getClass().getClassLoader());
//        CusCompiler compiler = new CusCompiler(loader);
//
//        Map<String, byte[]> results = compiler.compile(class_name, tmp);
//        Class<?> clazz = compiler.loadClass(class_name, results);
//
//        return clazz;
//    }

    public static Class<?> compileJavaFile__backup(String javaContent) throws Exception {

        String tmp = new String(javaContent);

        tmp = tmp.trim();

        // 生成一个随机的包名
        String randomPackage = "random" + CommonUtil.getRandomNumber(16);

        // 获取 class name
        String className = CommonUtil.getMatchedByGroup(tmp, "\\s+public\\s+class\\s+(\\w+)\\s?.*?\\{");

        // 重定位包路径
        if (tmp.startsWith("package")) {
            tmp = tmp.replaceAll("package\\s+.*;", "package " + randomPackage + ";");
        } else {
            tmp = "package " + randomPackage + ";" + "\n\n" + tmp;
        }

        // 加载 class
        String newClassName = randomPackage + "." + className;

        //动态编译
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

        // --------
        StringBuilder sb = new StringBuilder();
        // 设置类加载器
        //        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        //        for (URL url : urlClassLoader.getURLs()) {
        //            sb.append(url.getFile()).append(File.pathSeparator);
        //        }

        sb.append(CommonUtil.join(readClasspathFrom(), ":"));
        //-------

        ByteArrayInputStream is = new ByteArrayInputStream(tmp.getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        String javaFilePath = System.getProperty("user.home") + "/tmp_java/source/" + newClassName.replaceAll("\\.", "/") + ".java";
        String javaFileFolder = System.getProperty("user.home") + "/tmp_java/source/" + randomPackage.replaceAll("\\.", "/");
        CommonUtil.makeSureFolderExists(javaFilePath);
        FileOutputStream tmpFile = new FileOutputStream(javaFilePath);
        tmpFile.write(tmp.getBytes(StandardCharsets.UTF_8));
        tmpFile.close();

        String cssFileName = System.getProperty("user.home") + "/tmp_java/class/" + newClassName.replaceAll("\\.", "/") + ".class";
        CommonUtil.makeSureFolderExists(cssFileName);

        String classFileFolder = System.getProperty("user.home") + "/tmp_java/class/";

        // log.info("classpath: " + sb.toString());

        int status = javac.run(null, bos, bos, "-d", classFileFolder, "-classpath", sb.toString(), javaFilePath);

        String cmd = CommonUtil.format("javac -d {} -classpath {} {}", classFileFolder, sb.toString(), javaFilePath);
        System.out.println("---------------------------");
        System.out.println(cmd);
        System.out.println("---------------------------");
        if (status != 0) {

            log.error("没有编译成功！");

            String output = new String(bos.toByteArray());

            throw new BizException(output);
        }

        Class<?> cls = new DynamicClassLoader(null).defineClass(newClassName, cssFileName);

        log.info("class 动态编译成功: " + newClassName);

        return cls;
        // return Class.forName(newClassName);
    }


    static final class FileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {

        public FileManagerImpl(JavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public ClassLoader getClassLoader(JavaFileManager.Location location) {
            new Exception().printStackTrace();
            return Thread.currentThread().getContextClassLoader();
        }

    }

    public static Class<?> compileJavaFile2222(String javaContent) throws Exception {

        String tmp = new String(javaContent);

        tmp = tmp.trim();

        // 生成一个随机的包名
        String randomPackage = "org.armada.random" + CommonUtil.getRandomNumber(16);

        // 获取 class name
        String className = CommonUtil.getMatchedByGroup(tmp, "\\s+public\\s+class\\s+(\\w+)\\s?.*?\\{");

        // 重定位包路径
        if (tmp.startsWith("package")) {
            tmp = tmp.replaceAll("package\\s+.*;", "package " + randomPackage + ";");
        } else {
            tmp = "package " + randomPackage + ";" + "\n\n" + tmp;
        }

        // 加载 class
        String newClassName = randomPackage + "." + className;

        // 编译器
        JavaStringCompiler compiler = new JavaStringCompiler();
        // 编译：compiler.compile("Main.java", source)

        // log.info("[now-sdk] 开启编译代码: \n" + tmp);
        Map<String, byte[]> results = compiler.compile(className + ".java", tmp);

        Class<?> clazz = compiler.loadClass(newClassName, results);

        return clazz;
    }


    private static List<String> readClasspathFrom() {

        String folder = "/Users/wangxiaobo/Downloads/galileo-start/";
        String path = folder + "META-INF/MANIFEST.MF";

        byte[] bufs = CommonUtil.readFileFromLocal(path);

        String content = new String(bufs);

        String[] tmps = content.split("\n");

        List<String> result = new ArrayList<String>();

        for (String str : tmps) {

            String[] tt = str.split(":");

            if (tt.length != 2) {
                continue;
            }

            String key = tt[0].trim();
            String value = tt[1].trim();

            if ("Spring-Boot-Classes".equals(key)) {
                result.add(folder + value);
                continue;
            }

            if ("Spring-Boot-Lib".equals(key)) {
                String libPath = folder + value;
                File libFolder = new File(libPath);
                if (libFolder.exists() && libFolder.isDirectory()) {
                    File[] fs = libFolder.listFiles();
                    for (File libFile : fs) {
                        result.add(libFile.getAbsolutePath());
                    }
                }
            }
        }

//
//        Spring-Boot-Classes: BOOT-INF/classes/
//                Spring-Boot-Lib: BOOT-INF/lib/

        return result;
    }

}