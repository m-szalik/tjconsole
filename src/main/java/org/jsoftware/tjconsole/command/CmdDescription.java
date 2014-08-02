package org.jsoftware.tjconsole.command;

import org.jsoftware.tjconsole.TJContext;

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
    private final boolean requiresBean;

    public CmdDescription(String description, String full, String prefix, boolean requiresBean) {
        super();
        this.description = description;
        this.full = full;
        this.prefix = prefix;
        this.requiresBean = requiresBean;
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

    public boolean canBeUsed(TJContext tjContext) {
        return !requiresBean || (requiresBean && tjContext.isBeanSelected());
    }


}
