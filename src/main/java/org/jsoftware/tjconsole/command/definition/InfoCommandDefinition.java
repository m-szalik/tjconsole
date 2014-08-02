package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.console.Output;

import javax.management.MBeanServerConnection;
import java.util.List;

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
                add(output, "Server", tjContext.getServerURL());
                add(output, "Bean", tjContext.getObjectName());
                add(output, "SSL", tjContext.getEnvironment().get("SSL"));
                add(output, "Username", tjContext.getEnvironment().get("USERNAME"));
                Object password = tjContext.getEnvironment().get("PASSWORD");
                add(output, "Password", password != null && password.toString().trim().length() > 0 ? "***" : null);
            }

            private void add(Output output, String name, Object value) {
                output.println("@|yellow " + name + "|@ = @|yellow " + (value == null ? "" : value.toString()) + " |@");
            }
        };
    }


}


