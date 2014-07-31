package org.jsoftware.tjconsole.command;

import org.jsoftware.tjconsole.Output;
import org.jsoftware.tjconsole.TJContext;

/**
 * Command
 *
 * @author szalik
 */
public abstract class AbstractCommand {
    protected final TJContext ctx;
    protected final Output output;

    public AbstractCommand(TJContext ctx, Output output) {
        super();
        this.ctx = ctx;
        this.output = output;
    }


    /**
     * If input matches the command
     *
     * @param input input to check
     * @return true if matched, otherwise false
     */
    public abstract boolean matches(String input);


    /**
     * Execute command
     *
     * @param input command input
     * @throws Exception if command cannot be executed properly
     */
    public abstract void action(String input) throws Exception;


    /**
     * @return get help description for the command
     */
    public abstract CmdDescription getHelp();

}
