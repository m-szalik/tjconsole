package org.jsoftware.tjconsole.command;

import org.jsoftware.tjconsole.console.Output;
import org.jsoftware.tjconsole.TJContext;

/**
 * @author szalik
 */
public interface CommandAction {

    void doAction(TJContext tjContext, Output output) throws Exception;

}
