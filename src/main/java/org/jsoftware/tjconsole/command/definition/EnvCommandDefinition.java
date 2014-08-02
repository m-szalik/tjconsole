package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.console.Output;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;

import java.util.List;

/**
 * Set environment variable like username or password
 *
 * @author szalik
 */
public class EnvCommandDefinition extends AbstractCommandDefinition {

    public EnvCommandDefinition() {
        super("Set environment variable like username or password", "env", "env", false);
    }


    public CommandAction action(String input) throws Exception {
        String[] data = input.substring(prefix.length()).trim().split("=", 2);
        final String attribute = data[0].trim();
        final String valueStr = data[1].trim();
        return new CommandAction() {
            @Override
            public void doAction(TJContext tjContext, Output output) {
                tjContext.setEnvironmentVariable(attribute, valueStr);
                output.outApp(attribute + '=' + valueStr);
            }
        };
    }


    @Override
    public Completer getCompleter(final TJContext tjContext) {
        return new Completer() {
            @Override
            public int complete(String buffer, int cursor, List<CharSequence> candidates) {
                if (matches(buffer)) {
                    for (String key : tjContext.getEnvironment().keySet()) {
                        candidates.add(key + " ");
                    }
                }
                return prefix.length() +1;
            }
        };
    }

}


