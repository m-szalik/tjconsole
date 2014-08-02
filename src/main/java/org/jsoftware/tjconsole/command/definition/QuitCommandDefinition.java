package org.jsoftware.tjconsole.command.definition;

import org.jsoftware.tjconsole.Output;
import org.jsoftware.tjconsole.TJConsole;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CmdDescription;
import org.jsoftware.tjconsole.command.CommandAction;

/**
 * Quit command. Quits an application.
 *
 * @author szalik
 */
public class QuitCommandDefinition extends AbstractCommandDefinition {


    public QuitCommandDefinition() {
        super("Quit.", "q", "quit", false);
    }

    @Override
    public CommandAction action(String input) throws Exception {
        return new CommandAction() {
            @Override
            public void doAction(TJContext tjContext, Output output) throws Exception {
                System.exit(0);
            }
        };
    }

    @Override
    public boolean matches(String input) {
        return input.equalsIgnoreCase("q") || input.equalsIgnoreCase("quit");
    }


}
