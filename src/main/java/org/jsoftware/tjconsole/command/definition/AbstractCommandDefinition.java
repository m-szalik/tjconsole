package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CmdDescription;

import java.util.Observable;

/**
 * @author szalik
 */
abstract class AbstractCommandDefinition extends Observable implements CommandDefinition {
    private final CmdDescription description;
    protected final String prefix;


    public AbstractCommandDefinition(String description, String full, String prefix, boolean requiresBean) {
        this.description = new CmdDescription(description, full, prefix, requiresBean);
        this.prefix = prefix;
    }

    @Override
    public boolean matches(String input) {
        return input.trim().startsWith(prefix);
    }

    @Override
    public CmdDescription getDescription() {
        return description;
    }

    @Override
    public Completer getCompleter(TJContext tjContext) {
        return null;
    }
}
