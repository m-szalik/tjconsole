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
                Object password = tjContext.getEnvironment().get("TRUST_STORE_PASSWORD");
                add(output, "TRUST_STORE_PASSWORD", password != null && password.toString().trim().length() > 0 ? "***" : null);
                add(output, "TRUST_STORE", tjContext.getEnvironment().get("TRUST_STORE"));
            }

            private void add(Output output, String name, Object value) {
                output.println("@|yellow " + name + "|@ = @|yellow " + (value == null ? "" : value.toString()) + " |@");
            }
        };
    }


}


