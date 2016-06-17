package org.jsoftware.tjconsole.localjvm;

import com.sun.tools.attach.*;

import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Util to resolve Java PIDs and resolve mxBeanServer URL by PID
 */
public class ProcessListManager {
    private final static Pattern SPLITTER = Pattern.compile("[:\\- ]");

    public Collection<JvmPid> getLocalProcessList() {
        List<VirtualMachineDescriptor> localVms = VirtualMachine.list();
        List<JvmPid> out = new ArrayList<JvmPid>();
        for (VirtualMachineDescriptor vmd : localVms) {
            out.add(new JvmPid(vmd.id(), vmd.displayName()));
        }
        return out;
    }


    /**
     * @param url string of local jvm mxBeanServer url
     * @return MxBeanServer URL for local jvm process
     * @throws IOException
     * @see #getLocalProcessList()
     */
    public JMXServiceURL getLocalServiceURL(String url) throws IOException, AgentLoadException, AgentInitializationException, LocalJvmAttachException {
        String pid = url.trim();
        Matcher matcher = SPLITTER.matcher(pid);
        if (matcher.find()) {
            int index = matcher.start();
            pid = pid.substring(0, index);
        }
        pid = pid.trim();
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            String connectorAddress = vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
            if (connectorAddress == null) { // no JMX - enable it
                Properties remoteProperties = vm.getSystemProperties();
                String separator = remoteProperties.getProperty("file.separator", File.separator);
                vm.loadAgent(remoteProperties.getProperty("java.home") + separator + "lib" + separator + "management-agent.jar");
                connectorAddress = vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
            }
            return new JMXServiceURL(connectorAddress);
        } catch (AttachNotSupportedException ex) {
            throw new LocalJvmAttachException(ex.getMessage());
        }
    }

    public static boolean isLocalProcess(String url) {
        String[] parts = url.trim().split(":", 2);
        Pattern digits = Pattern.compile("\\d*");
        return digits.matcher(parts[0]).matches() && (parts.length < 2 || !digits.matcher(parts[1]).matches());
    }
}
