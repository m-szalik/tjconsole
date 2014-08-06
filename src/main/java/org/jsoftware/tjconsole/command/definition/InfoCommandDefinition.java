package org.jsoftware.tjconsole.command.definition;

import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.console.Output;

/**
 * Show current state
 *
 * @author szalik
 */
public class InfoCommandDefinition extends AbstractCommandDefinition {

    public InfoCommandDefinition() {
        super("Display current state.", "info", "info", false);
    }


    public CommandAction action(String input) throws Exception {
        return new CommandAction() {
            @Override
            public void doAction(TJContext tjContext, Output output) {
                add(output, "CURRENT JMX SERVER", tjContext.getServerURL());
                add(output, "CURRENT JMX BEAN", tjContext.getObjectName());
                add(output, "SSL", tjContext.getEnvironment().get("SSL"));
                add(output, "USERNAME", tjContext.getEnvironment().get("USERNAME"));
                Object password = tjContext.getEnvironment().get("PASSWORD");
                add(output, "PASSWORD", password != null && password.toString().trim().length() > 0 ? "***" : null);
                add(output, "DATE_FORMAT", tjContext.getEnvironment().get("DATE_FORMAT"));
            }

            private void add(Output output, String name, Object value) {
                output.println("@|yellow " + name + "|@ = @|yellow " + (value == null ? "" : value.toString()) + " |@");
            }
        };
    }


}


