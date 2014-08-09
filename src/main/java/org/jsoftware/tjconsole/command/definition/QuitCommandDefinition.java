package org.jsoftware.tjconsole.command.definition;

import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.console.EndOfInputException;
import org.jsoftware.tjconsole.console.Output;

/**
 * Quit command. Quits an application.
 *
 * @author szalik
 */
public class QuitCommandDefinition extends AbstractCommandDefinition {


    public QuitCommandDefinition() {
        super("Quit.", "q or quit", "quit", false);
    }

    @Override
    public CommandAction action(String input) {
        return new CommandAction() {
            @Override
            public void doAction(TJContext tjContext, Output output) throws Exception {
                throw new EndOfInputException(); // it's not good for flow control, but....
            }
        };
    }

    @Override
    public boolean matches(String input) {
        return input.equalsIgnoreCase("q") || input.equalsIgnoreCase("quit");
    }


}
