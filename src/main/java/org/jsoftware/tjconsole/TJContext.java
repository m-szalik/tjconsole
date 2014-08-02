package org.jsoftware.tjconsole;

import jline.console.ConsoleReader;

import javax.management.*;
import java.io.IOException;
import java.util.*;

/**
 * Connection context.
 * What user is connected to (server, bean)
 *
 * @author szalik
 */
public class TJContext {
    private MBeanServerConnection serverConnection;
    private ObjectName objectName;
    private final Map<String, Object> environment;

    public TJContext() {
        environment = new LinkedHashMap<String, Object>();
        environment.put("SSL", Boolean.FALSE);
        environment.put("USERNAME", "");
        environment.put("PASSWORD", "");
    }

    public Map<String, Object> getEnvironment() {
        return Collections.unmodifiableMap(environment);
    }

    public void setEnvironmentVariable(String key, Object value) {
        if (!environment.containsKey(key)) {
            throw new IllegalArgumentException("Invalid key - " + key);
        }
        Object old = environment.get(key);
        if (!old.getClass().equals(value.getClass())) {
            throw new IllegalArgumentException("Invalid value type - " + value.getClass().getName() + " should be " + old.getClass().getName());
        }
        environment.put(key, value);
    }

    public MBeanServerConnection getServer() {
        return serverConnection;
    }

    public void setServer(MBeanServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    public boolean isConnected() {
        return serverConnection != null;
    }

    public List<MBeanAttributeInfo> getAttributes() throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
        if (serverConnection == null || objectName == null) {
            return Collections.emptyList();
        }
        MBeanInfo beanInfo = serverConnection.getMBeanInfo(objectName);
        return Arrays.asList(beanInfo.getAttributes());
    }

    public boolean isBeanSelected() {
        return objectName != null;
    }


}


