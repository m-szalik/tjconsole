package org.jsoftware.tjconsole.localjvm;

/**
 * Check if tools.jar is available
 * Receive localjvm java PIDs
 * Try to load agent for JMX
 * @author szalik
 */
public class ProcessListManagerLoader {
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
        synchronized (ProcessListManagerLoader.class) {
            if (processListManager == null) {
                processListManager = new ProcessListManager();
            }
            return processListManager;
        }
    }

    /**
     * @param url url to check
     * @return true if url string is localjvm process url
     */
    public static boolean isLocalProcess(String url) {
        return url.startsWith(LOCAL_PREFIX);
    }


}
