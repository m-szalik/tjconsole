package org.jsoftware.tjconsole.command;

import org.jsoftware.tjconsole.Output;
import org.jsoftware.tjconsole.TJContext;

import java.io.IOException;

/**
 * @author szalik
 */
public interface CommandAction {

    void doAction(TJContext tjContext, Output output) throws Exception;

}
