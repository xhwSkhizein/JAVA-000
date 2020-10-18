package demo.classloader;


import sun.misc.Launcher;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * @author hongweixu
 * @since 2020-10-15 21:37
 */
public class JVMClassLoaderPrintPath {
    public static void main(String[] args) {

        // 启动类加载器
        URL[] urls = Launcher.getBootstrapClassPath().getURLs();
        System.out.println("启动类加载器");
        for (URL url : urls) {
            System.out.println(" ==> " + url.toExternalForm());
        }
        
        // 扩展类加载器
        printClassLoader("扩展类加载器", JVMClassLoaderPrintPath.class.getClassLoader().getParent());

        // 应用类加载器
        printClassLoader("应用类加载器", JVMClassLoaderPrintPath.class.getClassLoader());


        System.exit(0);
    }

    private static void printClassLoader(String name, ClassLoader classLoader) {
        if (classLoader != null) {
            System.out.println(name + " ClassLoader -> " + classLoader.toString());
            printURLForClassLoader(classLoader);
        } else {
            System.out.println(name + " ClassLoader -> null");
        }
    }

    private static void printURLForClassLoader(ClassLoader classLoader) {
        Object ucp = insightField(classLoader, "ucp");
        ArrayList path = (ArrayList) insightField(ucp, "path");
        if (path == null) {
            return;
        }
        for (Object p : path) {
            System.out.println(" ==> " + p.toString());
        }
    }

    private static Object insightField(Object obj, String fName) {
        try {
            Field f;
            if (obj instanceof URLClassLoader) {
                f = URLClassLoader.class.getDeclaredField(fName);
            } else {
                f = obj.getClass().getDeclaredField(fName);
            }
            f.setAccessible(true);

            return f.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
