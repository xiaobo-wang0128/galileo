//package org.armada.galileo.document.util.compile;
//
//import org.armada.galileo.common.loader.ClassHelper;
//import org.springframework.boot.loader.jar.Handler;
//
//import java.io.IOException;
//import java.net.JarURLConnection;
//import java.net.URL;
//import java.net.URLClassLoader;
//import java.net.URLConnection;
//import java.util.Enumeration;
//
//public class MyLaunchedURLClassLoader extends URLClassLoader {
//
//    public MyLaunchedURLClassLoader(URL[] urls, ClassLoader parent) {
//        super(urls, parent);
//    }
//
//    public URL findResource(String name) {
//        try {
//            Enumeration<URL> urls = ClassHelper.getClassLoader().getResources(name);
//            if (urls.hasMoreElements()) {
//                return urls.nextElement();
//            }
//            return null;
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    public Enumeration<URL> findResources(String name) throws IOException {
//        return ClassHelper.getClassLoader().getResources(name);
//    }
//
//    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//        return super.loadClass(name, resolve);
//    }
//
//
//}
