package com.gruszecm.tjconsole;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;


public class TJContext { 
	private MBeanServerConnection serverConnection;
	private ObjectName objectName;
	private Map<String,Object> enviroment;
	
	public TJContext() {
		enviroment = new LinkedHashMap<String, Object>();
		enviroment.put("SSL", Boolean.FALSE);
		enviroment.put("USERNAME", "");
		enviroment.put("PASSWORD", "");
	}
	
	public Map<String, Object> getEnviroment() {
		return Collections.unmodifiableMap(enviroment);
	}
	
	public void setEvniromentVariable(String key, Object value) {
		if (! enviroment.containsKey(key)) throw new IllegalArgumentException("Invalid key - " + key);
		Object old = enviroment.get(key);
		if (! old.getClass().equals(value.getClass())){
			throw new IllegalArgumentException("Invalid value type - " + value.getClass().getName() + " should be " + old.getClass().getName());
		}
		enviroment.put(key, value);
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
		if (serverConnection == null || objectName == null) return Collections.emptyList();
		MBeanInfo beanInfo = serverConnection.getMBeanInfo(objectName);
		return Arrays.asList(beanInfo.getAttributes());
	}
}
