package org.jsoftware.tjconsole.localjvm;

/**
 * Check if tools.jar is available
 * Receive local jvm java PIDs
 * Try to load agent for JMX
 * @author szalik
 */
public class ProcessListManagerFactory {
    public static final String LOCAL_PREFIX = "LOCAL:";
    private static ProcessListManager processListManager;

    public static boolean isToolsJarAvailable() {
        try {
            Class.forName("com.sun.tools.attach.VirtualMachine");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static ProcessListManager getProcessListManager() throws ToolsNotAvailableException {
        if (! isToolsJarAvailable()) {
            throw new ToolsNotAvailableException();
        }
        synchronized (ProcessListManagerFactory.class) {
            if (processListManager == null) {
                processListManager = new ProcessListManager();
            }
            return processListManager;
        }
    }

    /**
     * @param url url to check
     * @return true if url string is local jvm process url
     */
    public static boolean isLocalProcess(String url) {
        return url.startsWith(LOCAL_PREFIX);
    }


}
