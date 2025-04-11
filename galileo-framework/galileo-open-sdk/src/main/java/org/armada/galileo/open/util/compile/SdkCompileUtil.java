package org.armada.galileo.open.util.compile;

import com.itranswarp.compiler.JavaStringCompiler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.open.util.DynamicClassLoader;
import org.armada.galileo.exception.BizException;

import java.io.*;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author xiaobo
 * @date 2021/12/9 2:39 下午
 */
@Slf4j
public class SdkCompileUtil {

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    public static class CompileResult {

        private String clsName;

        private Boolean isInner = false;

        private byte[] classBytes;

        private Class<?> cls;

    }

    public static List<CompileResult> compile(String javaFileContent) throws Exception {
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

        if (urlClassLoader.getURLs() == null || urlClassLoader.getURLs().length == 0) {
            log.info("class path 路径获取失败");
            throw new BizException("class path 路径获取失败");
        }

//        log.info("打印 classLoader url: ");
//        for (URL url : urlClassLoader.getURLs()) {
//            log.info(url.getPath());
//        }

        if (urlClassLoader.getURLs().length <= 3) {
            String springBootJarPath = urlClassLoader.getURLs()[0].getPath();
            log.info("spring-boot java path: " + springBootJarPath);

            return compileBySpringBootEnv(javaFileContent, springBootJarPath, null);

        } else {

            return compileByLocal(javaFileContent);

        }
    }


    /**
     * 使用 spring boot 环境编译
     *
     * @param javaFileContent
     * @return
     * @throws Exception
     */
    private synchronized static List<CompileResult> compileBySpringBootEnv(String javaFileContent, String springBootJarPath, List<String> clsPaths) throws Exception {

        List<CompileResult> compileResultList = new ArrayList<>();

        // 目录定义
        String classpath = System.getProperty("user.home") + File.separator + "build" + File.separator + "classpath" + File.separator;
        String sourcepath = System.getProperty("user.home") + File.separator + "build" + File.separator + "source" + File.separator;
        String targetpath = System.getProperty("user.home") + File.separator + "build" + File.separator + "target" + File.separator;

        // 创建目录
        makeFolderExist(classpath);
        makeFolderExist(sourcepath);
        makeFolderExist(targetpath);

        // spring-boot jar
        if (CommonUtil.isNotEmpty(springBootJarPath)) {
            // 解压 spring-boot.jar
            String MANIFEST = classpath + "META-INF" + File.separator + "MANIFEST.MF";
            if (!new File(MANIFEST).exists()) {
                String cmd = CommonUtil.format("unzip -q -o {} -d {}", springBootJarPath, classpath);
                log.info("解压 spring-boot jar: " + cmd);
                execCmd(cmd, null);
            } else {
                log.info("classpath 已存在，无需解压, " + springBootJarPath);
            }

            clsPaths = readClasspathFrom(classpath);
        } else {
            if (clsPaths == null || clsPaths.isEmpty()) {
                throw new BizException("cls path error");
            }
        }

        // 编译
        JavaObject javaObject = saveJaveFile(sourcepath, targetpath, javaFileContent);
        // -Xlint:unchecked
        String cmd = CommonUtil.format("javac -d {} -classpath {} -encoding utf-8 {}", targetpath, CommonUtil.join(clsPaths, ":"), javaObject.getSourceFilePath());
        execCmd(cmd, javaObject.getClassFilePath());

        // 当前 class loader
        ClassLoader appClassLoader = Thread.currentThread().getContextClassLoader();
        DynamicClassLoader loader = new DynamicClassLoader(appClassLoader);

        // 加载主类
        Class<?> cls = loader.defineClass(javaObject.getClassName(), javaObject.getClassFilePath());
        CompileResult mainCompileResult = new CompileResult(javaObject.getClassName(), false, CommonUtil.readFileFromLocal(javaObject.getClassFilePath()), cls);
        compileResultList.add(mainCompileResult);

        // 加载内部类
        String targetFolderPath = javaObject.getClassFilePath().substring(0, javaObject.getClassFilePath().lastIndexOf(File.separator));
        File targetFolder = new File(targetFolderPath);

        if (targetFolder.exists()) {
            File[] fs = targetFolder.listFiles();
            if (fs.length > 1) {

                String mainClsName = javaObject.getClassName().substring(javaObject.getClassName().lastIndexOf(".") + 1);

                for (File ff : fs) {

                    String innerFileName = ff.getName().substring(0, ff.getName().lastIndexOf("."));

                    if (mainClsName.equals(innerFileName)) {
                        continue;
                    }

                    String innerClsName = javaObject.getPackageName() + "." + innerFileName;

                    JavaObject jo = new JavaObject();
                    jo.setClassName(innerClsName);
                    jo.setClassFilePath(ff.getAbsolutePath());

                    loader.defineClass(jo.getClassName(), jo.getClassFilePath());
                    CompileResult innerCompileResult = new CompileResult(jo.getClassName(), true, CommonUtil.readFileFromLocal(jo.getClassFilePath()), null);
                    compileResultList.add(innerCompileResult);
                }
            }
        }

        log.info("class 动态编译成功: " + javaObject.getClassName());

        return compileResultList;
    }


    private static JavaObject saveJaveFile(String sourcepath, String targetpath, String javaContent) throws Exception {

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

        //String javaFilePath = sourcepath + newClassName.replaceAll("\\.", File.separator) + ".java";
        String javaFilePath = sourcepath + CommonUtil.replaceAll(newClassName, ".", File.separator) + ".java";

        makeFolderExist(javaFilePath);
        FileOutputStream tmpFile = new FileOutputStream(javaFilePath);
        tmpFile.write(tmp.getBytes(StandardCharsets.UTF_8));
        tmpFile.close();

        String classFilePath = targetpath + CommonUtil.replaceAll(newClassName, ".", File.separator) + ".class";
        JavaObject javaObject = new JavaObject();
        javaObject.setPackageName(randomPackage);
        javaObject.setClassName(newClassName);
        javaObject.setSourceFilePath(javaFilePath);
        javaObject.setClassFilePath(classFilePath);

        return javaObject;
    }

    @Data
    private static class JavaObject {
        // 包路径
        private String packageName;
        // 完整类名，带包路径
        private String className;
        // 源码路径
        private String sourceFilePath;
        // 字节码路径
        private String classFilePath;
    }

    private static void makeFolderExist(String path) {
        // 路径是一个文件名
        int point = path.lastIndexOf(".");
        if (point != -1) {
            int tmp = path.lastIndexOf(File.separator);
            if (tmp != -1 && point > tmp) {
                String folderPath = path.substring(0, tmp);
                File folder = new File(folderPath);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                return;
            }
        }

        File folder = new File(path);
        if (folder.exists()) {
            return;
        }
        folder.mkdirs();
    }

    private static void execCmd(String cmd, String classTargetPath) throws Exception {

        // log.info("cmd: " + cmd);
        Process p;

        //执行命令
        p = Runtime.getRuntime().exec(cmd);
        //取得命令结果的输出流
        InputStream fis = p.getErrorStream();
        //用一个读输出流类去读
        InputStreamReader isr = new InputStreamReader(fis);
        //用缓冲器读行
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        //直到读完为止
        List<String> errors = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            log.error("build error: " + line);
            errors.add(line);
        }

        if (CommonUtil.isNotEmpty(classTargetPath)) {

            String classFilePath = classTargetPath.substring(0, classTargetPath.lastIndexOf(File.separator));

            if ((!new File(classFilePath).exists() || new File(classFilePath).listFiles().length == 0)) {
                throw new BizException("编译出错: <br/> " + CommonUtil.join(errors, "<br/>"));
            }
        }

    }


    private static List<String> readClasspathFrom(String classpathFolder) {

        String path = classpathFolder + "META-INF/MANIFEST.MF";

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
                result.add(classpathFolder + value);
                continue;
            }

            if ("Spring-Boot-Lib".equals(key)) {
                String libPath = classpathFolder + value;
                File libFolder = new File(libPath);
                if (libFolder.exists() && libFolder.isDirectory()) {
                    File[] fs = libFolder.listFiles();
                    for (File libFile : fs) {
                        result.add(libFile.getAbsolutePath());
                    }
                }
            }
        }

        return result;
    }


    /**
     * 使用本地 classpath 环境编译
     *
     * @param javaFileContent
     * @return
     * @throws Exception
     */
    public static List<CompileResult> compileByLocal(String javaFileContent) throws Exception {

        String tmp = new String(javaFileContent);

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

        // Class<?> clazz = compiler.loadClass(newClassName, results);
        // 当前 class loader
        ClassLoader appClassLoader = Thread.currentThread().getContextClassLoader();
        DynamicClassLoader loader = new DynamicClassLoader(appClassLoader);


        List<CompileResult> result = new ArrayList<>();

        for (Map.Entry<String, byte[]> entry : results.entrySet()) {
            String clsName = entry.getKey();
            boolean isInner = clsName.indexOf("$") != -1;

            Class<?> clazz = loader.defineClass(clsName, entry.getValue());

            CompileResult cr = new CompileResult(entry.getKey(), isInner, entry.getValue(), clazz);

            result.add(cr);
        }
        return result;

    }


}
