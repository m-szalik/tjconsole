package org.jsoftware.tjconsole.localjvm;

/**
 * @author szalik
 */
public class JvmPid {
    private final String pid;
    private final String command;

    JvmPid(String pid, String command) {
        this.pid = pid;
        this.command = command;
    }

    public String getPid() {
        return pid;
    }

    public String getCommand() {
        return command;
    }

    public String getFullName() {
        return pid + ":" + command;
    }

    @Override
    public String toString() {
        return "jvm(" + pid + ":" + command + ")";
    }
}
