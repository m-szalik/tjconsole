package com.gruszecm.tjconsole.command;

import com.gruszecm.tjconsole.Output;
import com.gruszecm.tjconsole.TJConsole;
import com.gruszecm.tjconsole.TJContext;

/**
 * Quit command. Quits an application.
 *
 * @author szalik
 */
public class QuitCommand extends AbstractCommand {
    private final TJConsole console;

    public QuitCommand(TJContext context, Output output, TJConsole console) {
        super(context, output);
        this.console = console;
    }

    @Override
    public void action(String input) throws Exception {
        console.quit();
    }

    @Override
    public boolean matches(String input) {
        return input.equalsIgnoreCase("\\q") || input.equalsIgnoreCase("quit");
    }

    @Override
    public CmdDescription getHelp() {
        return new CmdDescription("Quit.", "\\q", "\\q");
    }


}
