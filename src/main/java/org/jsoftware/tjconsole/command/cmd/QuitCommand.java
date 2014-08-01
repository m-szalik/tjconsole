package org.jsoftware.tjconsole.command.cmd;

import org.jsoftware.tjconsole.Output;
import org.jsoftware.tjconsole.TJConsole;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CmdDescription;

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
        return input.equalsIgnoreCase("q") || input.equalsIgnoreCase("quit");
    }

    @Override
    public CmdDescription getHelp() {
        return new CmdDescription("Quit.", "quit", "q");
    }


}
