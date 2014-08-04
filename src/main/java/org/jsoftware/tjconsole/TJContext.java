package org.jsoftware.tjconsole;

import org.apache.commons.beanutils.ConvertUtils;
import org.jsoftware.tjconsole.command.CommandAction;

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
    private Object serverURL;
    private int exitCode = 0;

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
        if (! old.getClass().equals(value.getClass())) {
            value = ConvertUtils.convert(value, old.getClass());
            if (! old.getClass().equals(value.getClass())) {
                throw new IllegalArgumentException("Invalid value type - " + value.getClass().getName() + " should be " + old.getClass().getName());
            }
        }
        environment.put(key, value);
    }

    public MBeanServerConnection getServer() {
        return serverConnection;
    }

    public void setServer(MBeanServerConnection serverConnection, String url) {
        this.serverConnection = serverConnection;
        this.serverURL = url;
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


    public Object getServerURL() {
        return serverURL;
    }

    public void fail(CommandAction action, int code) {
        if (exitCode == 0) {
            exitCode = code;
        }
    }

    public int getExitCode() {
        return exitCode;
    }
}


