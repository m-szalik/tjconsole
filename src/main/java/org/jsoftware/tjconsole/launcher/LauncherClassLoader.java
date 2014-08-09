package org.jsoftware.tjconsole.launcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Force load some classes with different classLoader to change parent classLoader.
 * @author szalik
 */
class LauncherClassLoader extends ClassLoader {
    private final ClassLoader parent;
    private final String forcePrefix;

    public LauncherClassLoader(ClassLoader parent, String forcePrefix) {
        super(parent);
        this.parent = parent;
        this.forcePrefix = forcePrefix;
    }


    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz;
        if (name.startsWith(forcePrefix)) {
             clazz = forceLoadClass(name);
        } else {
            clazz = super.loadClass(name);
        }
        return clazz;
    }


    private Class<?> forceLoadClass(final String name) throws ClassNotFoundException {
        String resourceName = name.replace('.', '/').concat(".class");
        InputStream inputStream = null;
        try {
            ClassLoader cl = parent;
            do {
                inputStream = cl.getResourceAsStream(resourceName);
                if (inputStream == null) {
                    cl = cl.getParent();
                } else {
                    break;
                }
            } while(cl != null);
            if (inputStream == null) {
                throw new ClassNotFoundException("Cannot find resource '" + resourceName + "'");
            }
            byte[] data = toByteArray(inputStream);
            return defineClass(name, data, 0, data.length);
        } catch (IOException ex) {
            throw new ClassNotFoundException("Cannot read resource '" + resourceName + "'", ex);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) { /* ignore */ }
            }
        }
    }



    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int r;
        byte[] data = new byte[1024];
        while ((r = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, r);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
}
