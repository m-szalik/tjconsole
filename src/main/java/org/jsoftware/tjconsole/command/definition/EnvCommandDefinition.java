package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.console.Output;

import java.util.List;
import java.util.Map;

/**
 * Set environment variable like username or password
 *
 * @author szalik
 */
public class EnvCommandDefinition extends AbstractCommandDefinition {

    public EnvCommandDefinition() {
        super("Set environment variable like username or password", "env <variableName>=<newValue>", "env", false);
    }


    public CommandAction action(String input) {
        String[] data = input.substring(prefix.length()).trim().split("=", 2);
        if (data.length < 2) {
            return new CommandAction() {
                @Override
                public void doAction(TJContext tjContext, Output output) {
                    for(Map.Entry<String,Object> me : tjContext.getEnvironment().entrySet()) {
                        output.println("@|red " + me.getKey() + "|@ = " + me.getValue());
                    }
                }
            };
        } else {
            final String key = data[0].trim();
            final String valueStr = data[1].trim();
            return new CommandAction() {
                @Override
                public void doAction(TJContext tjContext, Output output) {
                    try {
                        tjContext.setEnvironmentVariable(key, valueStr, true);
                        output.println("@|red " + key + "|@ = " + valueStr);
                    } catch (IllegalArgumentException ex) {
                        tjContext.fail(this, 60);
                        output.outError("Cannot update " + key + " - " + ex.getLocalizedMessage());
                    }
                }
            };
        }
    }


    @Override
    public Completer getCompleter(final TJContext tjContext) {
        return new Completer() {
            @Override
            public int complete(String buffer, int cursor, List<CharSequence> candidates) {
                if (matches(buffer)) {
                    String namePrefix = buffer.substring(prefix.length()).trim();
                    for (String key : tjContext.getEnvironment().keySet()) {
                        if (key.startsWith(namePrefix)) {
                            candidates.add(" " + key + "=");
                        }
                    }
                    return prefix.length();
                }
                return -1;
            }
        };
    }

}


