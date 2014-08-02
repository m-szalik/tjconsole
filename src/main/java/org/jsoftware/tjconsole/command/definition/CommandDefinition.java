package org.jsoftware.tjconsole.command.definition;

import jline.console.completer.Completer;
import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CmdDescription;
import org.jsoftware.tjconsole.command.CommandAction;

/**
 * Command definition
 *
 * @author szalik
 */
public interface CommandDefinition {

    /**
     * If input matches the command
     *
     * @param input input to check
     * @return true if matched, otherwise false
     */
    boolean matches(String input);


    /**
     * Create command to execute
     *
     * @param input command input
     * @throws Exception if command cannot be parsed properly
     */
    CommandAction action(String input) throws Exception;


    /**
     * @return get help description for the command
     */
    CmdDescription getDescription();

    /**
     * @return can be null if completion is not supported by this command
     */
    Completer getCompleter(TJContext tjContext);

}
