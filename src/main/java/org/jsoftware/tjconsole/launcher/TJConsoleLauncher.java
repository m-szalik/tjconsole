package org.jsoftware.tjconsole.launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Launch TJConsole using special class loader
 *
 * @author szalik
 */
public class TJConsoleLauncher {

    public static void main(String[] args) throws FileNotFoundException, MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String toolsJarPath = System.getProperty("java.home") + File.separator + ".." + File.separator + "lib" + File.separator + "tools.jar";
        File toolsJarFile = new File(toolsJarPath);
        if (!toolsJarFile.exists()) {
            throw new FileNotFoundException("Cannot find tools.jar in " + toolsJarFile.getParentFile().getAbsolutePath());
        }
        URLClassLoader toolsClassLoader = URLClassLoader.newInstance(new URL[]{toolsJarFile.toURI().toURL()}, TJConsoleLauncher.class.getClassLoader());

        ClassLoader classLoader = new LauncherClassLoader(toolsClassLoader, "org.jsoftware");
        Class<?> clazz = classLoader.loadClass("org.jsoftware.tjconsole.TJConsole");
        Method method = clazz.getMethod("start", String[].class);
        method.invoke(null, new Object[]{args});
    }

}
