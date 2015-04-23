package org.jsoftware.tjconsole.command.definition;

import org.jsoftware.tjconsole.TJContext;
import org.jsoftware.tjconsole.command.CommandAction;
import org.jsoftware.tjconsole.console.Output;
import org.jsoftware.tjconsole.localjvm.JvmPid;
import org.jsoftware.tjconsole.localjvm.ProcessListManager;

import java.util.*;
import java.util.prefs.BackingStoreException;

/**
 * Command to display local java processes list
 *
 * @author szalik
 */
public class PsCommandDefinition extends AbstractCommandDefinition {
    private final ProcessListManager processListManager = new ProcessListManager();

    public PsCommandDefinition() throws BackingStoreException {
        super("Display local java processes list", "Display local java processes list", "ps", false);
    }


    @Override
    public CommandAction action(final String input) {
        return new CommandAction() {
            @Override
            public void doAction(TJContext ctx, Output output) throws Exception {
                List<String> out = new LinkedList<String>();
                for(JvmPid jvmPid : processListManager.getLocalProcessList()) {
                    out.add("@|red " + jvmPid.getPid() + "|@ @|green " + jvmPid.getCommand() + "|@");
                }
                for(String s : out) {
                    output.println(s);
                }
            }
        };
    }

}

