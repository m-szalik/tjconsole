package com.gruszecm.tjconsole.command;

import com.gruszecm.tjconsole.TJContext;

import java.io.Serializable;

/**
 * Help description for a command
 *
 * @author szalik
 */
public class CmdDescription implements Serializable {
    private static final long serialVersionUID = 974727180626249506L;
    private final String description;
    private final String full;
    private final String prefix;

    public CmdDescription(String description, String full, String prefix) {
        super();
        this.description = description;
        this.full = full;
        this.prefix = prefix;
    }

    public String getDescription() {
        return description;
    }

    public String getFull() {
        return full;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isProper(TJContext ctx) {
        return true;
    }

}
