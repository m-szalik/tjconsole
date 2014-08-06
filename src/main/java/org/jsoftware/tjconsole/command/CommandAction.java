package org.jsoftware.tjconsole.command;

import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.console.Output;

/**
 * @author szalik
 */
public interface CommandAction {

    void doAction(TJContext tjContext, Output output) throws Exception;

}
